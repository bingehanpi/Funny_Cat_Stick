package io.github.bingehanpi.funny_cat_stick.worldgen;

import com.mojang.logging.LogUtils;
import io.github.bingehanpi.funny_cat_stick.FunnyCatStick;
import io.github.bingehanpi.funny_cat_stick.entity.DswhaleEntity;
import io.github.bingehanpi.funny_cat_stick.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.StructureTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.phys.AABB;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;

import it.unimi.dsi.fastutil.longs.LongSet;

/**
 * Places a residence only after chunk generation has returned to the server thread.
 * Candidate chunks are never force-loaded. Work remains persistent while nearby
 * chunks load naturally, but each in-memory retry burst is bounded.
 */
public final class DswhaleResidencePlacement {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation TEMPLATE_ID =
            ResourceLocation.fromNamespaceAndPath(FunnyCatStick.MOD_ID, "dswhale_architecture");
    private static final ResourceLocation WORLDGEN_STRUCTURE_ID =
            ResourceLocation.fromNamespaceAndPath(FunnyCatStick.MOD_ID, "dswhale_residence");
    private static final ResourceLocation LOOT_TABLE_ID =
            ResourceLocation.fromNamespaceAndPath(FunnyCatStick.MOD_ID, "chests/dswhale_architecture");

    private static final BlockPos HOME_ANCHOR = new BlockPos(7, 2, 7);
    private static final BlockPos RESIDENT_SPAWN = new BlockPos(7, 2, 7);
    private static final BlockPos BED_HEAD = new BlockPos(9, 2, 4);
    private static final BlockPos ENTRANCE = new BlockPos(4, 2, 9);
    private static final BlockPos LOOT_CHEST = new BlockPos(6, 5, 4);

    private static final int VILLAGE_CLEARANCE = 6;
    private static final int STRUCTURE_CLEARANCE = 3;
    private static final int MAX_HEIGHT_DIFFERENCE = 4;
    private static final double MAX_SOLID_OBSTRUCTION_RATIO = 0.15D;
    private static final int MAX_SURFACE_SCAN_DEPTH = 24;
    private static final int CANDIDATE_SPACING = 12;
    private static final int SEARCH_GAP_STEP = 16;
    private static final int MAX_SEARCH_GAP = 64;
    private static final int MAX_LATERAL_EXTENSION = 48;
    private static final int WAKE_DISTANCE_CHUNKS = 20;
    private static final int RETRY_DELAY_TICKS = 20;
    private static final int MAX_LOAD_RETRIES = 5;

    private static final Map<MinecraftServer, Map<TaskKey, PendingTask>> PENDING_TASKS =
            new WeakHashMap<>();
    private static final Map<MinecraftServer, Map<TaskKey, GeneratedPendingTask>> GENERATED_TASKS =
            new WeakHashMap<>();

    private DswhaleResidencePlacement() {
    }

    public static BlockPos residenceHome(DswhaleVillageSavedData.PlacementPlan plan) {
        return transform(plan.templateOrigin(), placementSettings(plan.rotation()), HOME_ANCHOR);
    }

    public static CommandPlacementResult placeAt(ServerLevel level, BlockPos templateOrigin) {
        if (!level.getServer().isSameThread()) {
            throw new IllegalStateException("Dswhale residences must be placed on the server thread");
        }

        Optional<StructureTemplate> template = level.getStructureManager().get(TEMPLATE_ID);
        if (template.isEmpty()) {
            return CommandPlacementResult.TEMPLATE_MISSING;
        }

        DswhaleVillageSavedData.PlacementPlan plan = new DswhaleVillageSavedData.PlacementPlan(
                templateOrigin.immutable(),
                Rotation.NONE
        );
        if (DswhaleVillageSavedData.get(level).hasRecordedResidence(plan)) {
            return CommandPlacementResult.ALREADY_RECORDED;
        }

        StructurePlaceSettings settings = placementSettings(plan.rotation());
        BoundingBox residenceBox = template.get().getBoundingBox(settings, plan.templateOrigin());
        if (residenceBox.minY() < level.getMinBuildHeight()
                || residenceBox.maxY() >= level.getMaxBuildHeight()) {
            return CommandPlacementResult.OUTSIDE_BUILD_HEIGHT;
        }

        loadChunks(level, residenceBox);
        CriticalPositions positions = criticalPositions(plan.templateOrigin(), settings);
        List<BlockSnapshot> snapshot = captureBlocks(level, residenceBox);
        try {
            boolean placed = template.get().placeInWorld(
                    level,
                    plan.templateOrigin(),
                    plan.templateOrigin(),
                    settings,
                    RandomSource.create(level.getSeed() ^ plan.templateOrigin().asLong()),
                    2
            );
            ensureLootChest(level, positions.chest());
            if (!placed || !hasCompleteStructure(level, positions)) {
                restoreBlocks(level, snapshot);
                return CommandPlacementResult.INCOMPLETE_TEMPLATE;
            }

            AttemptResult result = finishResidentAndState(
                    level,
                    new ChunkPos(plan.templateOrigin()),
                    positions,
                    plan,
                    true
            );
            if (result != AttemptResult.PLACED) {
                restoreBlocks(level, snapshot);
                return CommandPlacementResult.RESIDENT_FAILED;
            }
            return CommandPlacementResult.PLACED;
        } catch (RuntimeException exception) {
            restoreBlocks(level, snapshot);
            LOGGER.error(
                    "Failed to place commanded dswhale residence at {}",
                    plan.templateOrigin().toShortString(),
                    exception
            );
            return CommandPlacementResult.EXCEPTION;
        }
    }

    public static void scheduleIfPending(ServerLevel level, ChunkPos startChunk) {
        MinecraftServer server = level.getServer();
        if (!server.isSameThread()) {
            server.execute(() -> scheduleIfPending(level, startChunk));
            return;
        }
        if (!Level.OVERWORLD.equals(level.dimension())
                || DswhaleVillageSavedData.get(level).getStatus(startChunk)
                .orElse(null) != DswhaleVillageSavedData.Status.PENDING) {
            return;
        }

        Map<TaskKey, PendingTask> tasks = PENDING_TASKS.computeIfAbsent(server, ignored -> new HashMap<>());
        TaskKey key = new TaskKey(level.dimension(), startChunk.toLong());
        tasks.putIfAbsent(key, new PendingTask(server.getTickCount() + RETRY_DELAY_TICKS, 0));
    }

    public static void schedulePendingNear(ServerLevel level, ChunkPos loadedChunk) {
        MinecraftServer server = level.getServer();
        if (!server.isSameThread()) {
            server.execute(() -> schedulePendingNear(level, loadedChunk));
            return;
        }
        if (!Level.OVERWORLD.equals(level.dimension())) {
            return;
        }
        for (ChunkPos startChunk : DswhaleVillageSavedData.get(level).pendingVillages()) {
            if (Math.abs(startChunk.x - loadedChunk.x) <= WAKE_DISTANCE_CHUNKS
                    && Math.abs(startChunk.z - loadedChunk.z) <= WAKE_DISTANCE_CHUNKS) {
                scheduleIfPending(level, startChunk);
            }
        }
    }

    public static void scheduleGeneratedResidence(ServerLevel level, ChunkAccess loadedChunk) {
        MinecraftServer server = level.getServer();
        if (!server.isSameThread()) {
            server.execute(() -> scheduleGeneratedResidence(level, loadedChunk));
            return;
        }
        if (!Level.OVERWORLD.equals(level.dimension())) {
            return;
        }

        Registry<Structure> structures = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        Structure residence = structures.get(WORLDGEN_STRUCTURE_ID);
        if (residence == null) {
            return;
        }

        scheduleGeneratedStart(level, loadedChunk.getStartForStructure(residence));
        for (long startChunkLong : loadedChunk.getReferencesForStructure(residence)) {
            ChunkPos startChunk = new ChunkPos(startChunkLong);
            LevelChunk loadedStart = level.getChunkSource().getChunkNow(startChunk.x, startChunk.z);
            if (loadedStart != null) {
                scheduleGeneratedStart(level, loadedStart.getStartForStructure(residence));
            }
        }
    }

    private static void scheduleGeneratedStart(ServerLevel level, StructureStart start) {
        if (start == null || !start.isValid()) {
            return;
        }

        Optional<PoolElementStructurePiece> rootPiece = start.getPieces().stream()
                .filter(PoolElementStructurePiece.class::isInstance)
                .map(PoolElementStructurePiece.class::cast)
                .findFirst();
        if (rootPiece.isEmpty()) {
            LOGGER.warn(
                    "Generated dswhale residence at chunk [{}, {}] has no jigsaw root piece",
                    start.getChunkPos().x,
                    start.getChunkPos().z
            );
            return;
        }

        DswhaleVillageSavedData.PlacementPlan plan = new DswhaleVillageSavedData.PlacementPlan(
                rootPiece.get().getPosition().immutable(),
                rootPiece.get().getRotation()
        );
        if (DswhaleVillageSavedData.get(level).hasRecordedResidence(plan)) {
            return;
        }

        MinecraftServer server = level.getServer();
        Map<TaskKey, GeneratedPendingTask> tasks =
                GENERATED_TASKS.computeIfAbsent(server, ignored -> new HashMap<>());
        TaskKey key = new TaskKey(level.dimension(), start.getChunkPos().toLong());
        tasks.putIfAbsent(
                key,
                new GeneratedPendingTask(plan, server.getTickCount() + RETRY_DELAY_TICKS, 0)
        );
    }

    public static void tick(MinecraftServer server) {
        tickVillageTasks(server);
        tickGeneratedTasks(server);
    }

    private static void tickVillageTasks(MinecraftServer server) {
        Map<TaskKey, PendingTask> tasks = PENDING_TASKS.get(server);
        if (tasks == null || tasks.isEmpty()) {
            return;
        }

        int currentTick = server.getTickCount();
        List<TaskKey> dueTasks = tasks.entrySet().stream()
                .filter(entry -> entry.getValue().nextAttemptTick() <= currentTick)
                .map(Map.Entry::getKey)
                .toList();
        for (TaskKey key : dueTasks) {
            PendingTask task = tasks.get(key);
            ServerLevel level = server.getLevel(key.dimension());
            if (task == null || level == null) {
                tasks.remove(key);
                continue;
            }

            ChunkPos startChunk = new ChunkPos(key.startChunk());
            AttemptResult result = attemptPlacement(level, startChunk);
            if (result != AttemptResult.WAITING_FOR_CHUNKS) {
                tasks.remove(key);
                continue;
            }

            int retries = task.loadRetries() + 1;
            if (retries >= MAX_LOAD_RETRIES) {
                tasks.remove(key);
            } else {
                tasks.put(key, new PendingTask(currentTick + RETRY_DELAY_TICKS, retries));
            }
        }

        if (tasks.isEmpty()) {
            PENDING_TASKS.remove(server);
        }
    }

    private static void tickGeneratedTasks(MinecraftServer server) {
        Map<TaskKey, GeneratedPendingTask> tasks = GENERATED_TASKS.get(server);
        if (tasks == null || tasks.isEmpty()) {
            return;
        }

        int currentTick = server.getTickCount();
        List<TaskKey> dueTasks = tasks.entrySet().stream()
                .filter(entry -> entry.getValue().nextAttemptTick() <= currentTick)
                .map(Map.Entry::getKey)
                .toList();
        for (TaskKey key : dueTasks) {
            GeneratedPendingTask task = tasks.get(key);
            ServerLevel level = server.getLevel(key.dimension());
            if (task == null || level == null) {
                tasks.remove(key);
                continue;
            }

            AttemptResult result = initializeGeneratedResidence(level, key, task.plan());
            if (result != AttemptResult.WAITING_FOR_CHUNKS) {
                tasks.remove(key);
                continue;
            }

            int retries = task.loadRetries() + 1;
            if (retries >= MAX_LOAD_RETRIES) {
                tasks.remove(key);
            } else {
                tasks.put(
                        key,
                        new GeneratedPendingTask(task.plan(), currentTick + RETRY_DELAY_TICKS, retries)
                );
            }
        }

        if (tasks.isEmpty()) {
            GENERATED_TASKS.remove(server);
        }
    }

    public static void clear(MinecraftServer server) {
        PENDING_TASKS.remove(server);
        GENERATED_TASKS.remove(server);
    }

    private static AttemptResult initializeGeneratedResidence(
            ServerLevel level,
            TaskKey key,
            DswhaleVillageSavedData.PlacementPlan plan
    ) {
        if (DswhaleVillageSavedData.get(level).hasRecordedResidence(plan)) {
            return AttemptResult.NO_ACTION;
        }

        Optional<StructureTemplate> template = level.getStructureManager().get(TEMPLATE_ID);
        if (template.isEmpty()) {
            LOGGER.error("The generated dswhale residence template could not be loaded");
            return AttemptResult.FAILED;
        }

        StructurePlaceSettings settings = placementSettings(plan.rotation());
        BoundingBox residenceBox = template.get().getBoundingBox(settings, plan.templateOrigin());
        if (!areChunksLoaded(
                level,
                residenceBox.minX(),
                residenceBox.minZ(),
                residenceBox.maxX(),
                residenceBox.maxZ()
        )) {
            return AttemptResult.WAITING_FOR_CHUNKS;
        }

        CriticalPositions positions = criticalPositions(plan.templateOrigin(), settings);
        if (!hasCompleteStructure(level, positions)) {
            return AttemptResult.WAITING_FOR_CHUNKS;
        }

        ensureLootChest(level, positions.chest());
        AttemptResult result = finishResidentAndState(
                level,
                new ChunkPos(key.startChunk()),
                positions,
                plan,
                true
        );
        if (result == AttemptResult.PLACED) {
            ChunkPos startChunk = new ChunkPos(key.startChunk());
            LOGGER.info(
                    "Initialized world-generated dswhale residence from structure start chunk [{}, {}]",
                    startChunk.x,
                    startChunk.z
            );
        }
        return result;
    }

    private static AttemptResult attemptPlacement(ServerLevel level, ChunkPos startChunk) {
        if (DswhaleVillageSavedData.get(level).getStatus(startChunk).orElse(null)
                != DswhaleVillageSavedData.Status.PENDING) {
            return AttemptResult.NO_ACTION;
        }

        LevelChunk startLevelChunk = level.getChunkSource().getChunkNow(startChunk.x, startChunk.z);
        if (startLevelChunk == null) {
            return AttemptResult.WAITING_FOR_CHUNKS;
        }

        Optional<StructureStart> villageStart = findVillageStart(level, startLevelChunk, startChunk);
        if (villageStart.isEmpty()) {
            fail(level, startChunk, "the recorded village start is no longer available");
            return AttemptResult.FAILED;
        }

        Optional<StructureTemplate> template = level.getStructureManager().get(TEMPLATE_ID);
        if (template.isEmpty()) {
            fail(level, startChunk, "the residence template could not be loaded");
            return AttemptResult.FAILED;
        }

        DswhaleVillageSavedData data = DswhaleVillageSavedData.get(level);
        Optional<DswhaleVillageSavedData.PlacementPlan> savedPlan = data.getPlacementPlan(startChunk);
        if (savedPlan.isPresent()) {
            return placeFromPlan(level, startChunk, template.get(), savedPlan.get(), true);
        }

        CandidateSearchResult search = findCandidate(level, startChunk, villageStart.get(), template.get());
        if (search.plan() == null) {
            if (search.encounteredUnloadedChunks()) {
                return AttemptResult.WAITING_FOR_CHUNKS;
            }
            fail(level, startChunk, "no safe nearby site: " + search.rejectionSummary());
            return AttemptResult.FAILED;
        }

        if (!data.setPlacementPlan(startChunk, search.plan().templateOrigin(), search.plan().rotation())) {
            return AttemptResult.NO_ACTION;
        }
        return placeFromPlan(level, startChunk, template.get(), search.plan(), false);
    }

    private static Optional<StructureStart> findVillageStart(
            ServerLevel level,
            LevelChunk chunk,
            ChunkPos expectedStartChunk
    ) {
        Registry<Structure> structures = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        return chunk.getAllStarts().entrySet().stream()
                .filter(entry -> structures.wrapAsHolder(entry.getKey()).is(StructureTags.VILLAGE))
                .map(Map.Entry::getValue)
                .filter(StructureStart::isValid)
                .filter(start -> expectedStartChunk.equals(start.getChunkPos()))
                .findFirst();
    }

    private static CandidateSearchResult findCandidate(
            ServerLevel level,
            ChunkPos startChunk,
            StructureStart villageStart,
            StructureTemplate template
    ) {
        List<HorizontalCandidate> candidates = createCandidates(villageStart.getBoundingBox(), template);
        Collections.shuffle(candidates, new java.util.Random(placementSeed(level.getSeed(), startChunk)));
        candidates.sort(Comparator.comparingInt(
                candidate -> distanceFromVillage(villageStart.getBoundingBox(), candidate)
        ));

        boolean encounteredUnloadedChunks = false;
        Map<SiteRejection, Integer> rejections = new EnumMap<>(SiteRejection.class);
        for (HorizontalCandidate candidate : candidates) {
            if (!areChunksLoaded(level, candidate.minX(), candidate.minZ(), candidate.maxX(), candidate.maxZ())) {
                encounteredUnloadedChunks = true;
                countRejection(rejections, SiteRejection.UNLOADED_CHUNKS);
                continue;
            }

            TerrainCheck terrainCheck = assessTerrain(level, candidate, template.getSize().getY());
            if (terrainCheck.assessment() == null) {
                countRejection(rejections, terrainCheck.rejection());
                continue;
            }
            TerrainAssessment terrain = terrainCheck.assessment();

            StructurePlaceSettings settings = placementSettings(candidate.rotation());
            BoundingBox zeroBox = template.getBoundingBox(settings, BlockPos.ZERO);
            BlockPos templateOrigin = new BlockPos(
                    candidate.minX() - zeroBox.minX(),
                    terrain.baseY() - zeroBox.minY(),
                    candidate.minZ() - zeroBox.minZ()
            );
            BoundingBox residenceBox = template.getBoundingBox(settings, templateOrigin);
            if (villageStart.getBoundingBox().inflatedBy(VILLAGE_CLEARANCE).intersects(residenceBox)) {
                countRejection(rejections, SiteRejection.VILLAGE_OVERLAP);
                continue;
            }
            StructureConflict conflict = checkOtherStructure(level, residenceBox, villageStart);
            if (conflict == StructureConflict.UNLOADED) {
                encounteredUnloadedChunks = true;
                countRejection(rejections, SiteRejection.UNLOADED_CHUNKS);
                continue;
            }
            if (conflict == StructureConflict.INTERSECTS) {
                countRejection(rejections, SiteRejection.OTHER_STRUCTURE);
                continue;
            }

            return new CandidateSearchResult(
                    new DswhaleVillageSavedData.PlacementPlan(templateOrigin, candidate.rotation()),
                    encounteredUnloadedChunks,
                    summarizeRejections(rejections)
            );
        }
        return new CandidateSearchResult(null, encounteredUnloadedChunks, summarizeRejections(rejections));
    }

    private static List<HorizontalCandidate> createCandidates(BoundingBox village, StructureTemplate template) {
        Set<HorizontalCandidate> candidates = new LinkedHashSet<>();
        int centerX = (village.minX() + village.maxX()) / 2;
        int centerZ = (village.minZ() + village.maxZ()) / 2;

        for (Rotation rotation : Rotation.values()) {
            StructurePlaceSettings settings = placementSettings(rotation);
            BoundingBox zeroBox = template.getBoundingBox(settings, BlockPos.ZERO);
            int width = zeroBox.getXSpan();
            int depth = zeroBox.getZSpan();
            int xOffsetLimit = village.getXSpan() / 2 + MAX_LATERAL_EXTENSION;
            int zOffsetLimit = village.getZSpan() / 2 + MAX_LATERAL_EXTENSION;
            for (int gap = VILLAGE_CLEARANCE; gap <= MAX_SEARCH_GAP; gap += SEARCH_GAP_STEP) {
                for (int offset : symmetricOffsets(xOffsetLimit)) {
                    candidates.add(new HorizontalCandidate(
                            centerX - width / 2 + offset,
                            village.minZ() - gap - depth,
                            width,
                            depth,
                            rotation
                    ));
                    candidates.add(new HorizontalCandidate(
                            centerX - width / 2 + offset,
                            village.maxZ() + gap + 1,
                            width,
                            depth,
                            rotation
                    ));
                }
                for (int offset : symmetricOffsets(zOffsetLimit)) {
                    candidates.add(new HorizontalCandidate(
                            village.minX() - gap - width,
                            centerZ - depth / 2 + offset,
                            width,
                            depth,
                            rotation
                    ));
                    candidates.add(new HorizontalCandidate(
                            village.maxX() + gap + 1,
                            centerZ - depth / 2 + offset,
                            width,
                            depth,
                            rotation
                    ));
                }
            }
        }
        return new ArrayList<>(candidates);
    }

    private static List<Integer> symmetricOffsets(int limit) {
        List<Integer> offsets = new ArrayList<>();
        offsets.add(0);
        for (int offset = CANDIDATE_SPACING; offset <= limit; offset += CANDIDATE_SPACING) {
            offsets.add(offset);
            offsets.add(-offset);
        }
        return offsets;
    }

    private static int distanceFromVillage(BoundingBox village, HorizontalCandidate candidate) {
        int deltaX = Math.max(
                Math.max(village.minX() - candidate.maxX(), candidate.minX() - village.maxX()),
                0
        );
        int deltaZ = Math.max(
                Math.max(village.minZ() - candidate.maxZ(), candidate.minZ() - village.maxZ()),
                0
        );
        return Math.max(deltaX, deltaZ);
    }

    private static TerrainCheck assessTerrain(
            ServerLevel level,
            HorizontalCandidate candidate,
            int structureHeight
    ) {
        int width = candidate.width();
        int depth = candidate.depth();
        int[] surfaceHeights = new int[width * depth];
        int minimum = Integer.MAX_VALUE;
        int maximum = Integer.MIN_VALUE;

        for (int localZ = 0; localZ < depth; localZ++) {
            for (int localX = 0; localX < width; localX++) {
                int x = candidate.minX() + localX;
                int z = candidate.minZ() + localZ;
                int surfaceY = findNaturalSurface(level, x, z);
                if (surfaceY == Integer.MIN_VALUE) {
                    return TerrainCheck.rejected(SiteRejection.INVALID_SURFACE);
                }
                BlockPos surfacePos = new BlockPos(x, surfaceY, z);
                BlockState surface = level.getBlockState(surfacePos);
                if (!surface.getFluidState().isEmpty()
                        || surface.hasBlockEntity()
                        || surface.getDestroySpeed(level, surfacePos) < 0.0F) {
                    return TerrainCheck.rejected(SiteRejection.INVALID_SURFACE);
                }
                surfaceHeights[localZ * width + localX] = surfaceY;
                minimum = Math.min(minimum, surfaceY);
                maximum = Math.max(maximum, surfaceY);
            }
        }

        if (maximum - minimum > MAX_HEIGHT_DIFFERENCE
                || maximum < level.getMinBuildHeight() + MAX_HEIGHT_DIFFERENCE
                || maximum + structureHeight >= level.getMaxBuildHeight()) {
            return TerrainCheck.rejected(SiteRejection.UNEVEN_TERRAIN);
        }

        int checkedBlocks = 0;
        int solidObstructions = 0;
        for (int y = maximum + 1; y < maximum + structureHeight; y++) {
            for (int localZ = 0; localZ < depth; localZ++) {
                for (int localX = 0; localX < width; localX++) {
                    int x = candidate.minX() + localX;
                    int z = candidate.minZ() + localZ;
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    if (!state.getFluidState().isEmpty()
                            || state.hasBlockEntity()
                            || state.getDestroySpeed(level, pos) < 0.0F
                            || state.is(BlockTags.FEATURES_CANNOT_REPLACE)) {
                        return TerrainCheck.rejected(SiteRejection.PROTECTED_BLOCK);
                    }
                    checkedBlocks++;
                    if (!state.isAir()
                            && !state.canBeReplaced()
                            && !state.is(BlockTags.LEAVES)
                            && !state.is(BlockTags.SNOW)
                            && !state.is(BlockTags.REPLACEABLE_BY_TREES)) {
                        solidObstructions++;
                    }
                }
            }
        }
        if (solidObstructions > Math.ceil(checkedBlocks * MAX_SOLID_OBSTRUCTION_RATIO)) {
            return TerrainCheck.rejected(SiteRejection.TOO_OBSTRUCTED);
        }
        return TerrainCheck.accepted(new TerrainAssessment(maximum, surfaceHeights));
    }

    private static int findNaturalSurface(ServerLevel level, int x, int z) {
        int topY = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z) - 1;
        int minimumY = Math.max(level.getMinBuildHeight(), topY - MAX_SURFACE_SCAN_DEPTH);
        for (int y = topY; y >= minimumY; y--) {
            BlockPos pos = new BlockPos(x, y, z);
            BlockState state = level.getBlockState(pos);
            if (!state.getFluidState().isEmpty()) {
                return Integer.MIN_VALUE;
            }
            if (state.isAir()
                    || state.canBeReplaced()
                    || state.is(BlockTags.LEAVES)
                    || state.is(BlockTags.LOGS)
                    || state.is(BlockTags.SNOW)
                    || state.is(BlockTags.REPLACEABLE_BY_TREES)) {
                continue;
            }
            if (state.hasBlockEntity() || state.getDestroySpeed(level, pos) < 0.0F) {
                return Integer.MIN_VALUE;
            }
            return y;
        }
        return Integer.MIN_VALUE;
    }

    private static StructureConflict checkOtherStructure(
            ServerLevel level,
            BoundingBox residenceBox,
            StructureStart villageStart
    ) {
        BoundingBox searchBox = residenceBox.inflatedBy(STRUCTURE_CLEARANCE);
        int minChunkX = SectionRange.minChunk(searchBox.minX());
        int maxChunkX = SectionRange.maxChunk(searchBox.maxX());
        int minChunkZ = SectionRange.minChunk(searchBox.minZ());
        int maxChunkZ = SectionRange.maxChunk(searchBox.maxZ());
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(chunkX, chunkZ);
                if (chunk == null) {
                    return StructureConflict.UNLOADED;
                }
                for (StructureStart otherStart : chunk.getAllStarts().values()) {
                    if (!isSameStructureStart(otherStart, villageStart)
                            && otherStart.isValid()
                            && otherStart.getBoundingBox().intersects(searchBox)) {
                        return StructureConflict.INTERSECTS;
                    }
                }
                for (Map.Entry<Structure, LongSet> references : chunk.getAllReferences().entrySet()) {
                    for (long packedStartChunk : references.getValue()) {
                        ChunkPos otherStartPos = new ChunkPos(packedStartChunk);
                        LevelChunk otherStartChunk = level.getChunkSource().getChunkNow(
                                otherStartPos.x,
                                otherStartPos.z
                        );
                        if (otherStartChunk == null) {
                            return StructureConflict.UNLOADED;
                        }
                        StructureStart otherStart = otherStartChunk.getStartForStructure(references.getKey());
                        if (otherStart != null
                                && !isSameStructureStart(otherStart, villageStart)
                                && otherStart.isValid()
                                && otherStart.getBoundingBox().intersects(searchBox)) {
                            return StructureConflict.INTERSECTS;
                        }
                    }
                }
            }
        }
        return StructureConflict.CLEAR;
    }

    private static boolean isSameStructureStart(StructureStart left, StructureStart right) {
        return left == right
                || left.getStructure() == right.getStructure()
                && left.getChunkPos().equals(right.getChunkPos());
    }

    private static void countRejection(Map<SiteRejection, Integer> rejections, SiteRejection rejection) {
        if (rejection != null) {
            rejections.merge(rejection, 1, Integer::sum);
        }
    }

    private static String summarizeRejections(Map<SiteRejection, Integer> rejections) {
        if (rejections.isEmpty()) {
            return "no candidates were produced";
        }
        return rejections.entrySet().stream()
                .map(entry -> entry.getKey().serializedName() + "=" + entry.getValue())
                .reduce((left, right) -> left + ", " + right)
                .orElse("unknown");
    }

    private static AttemptResult placeFromPlan(
            ServerLevel level,
            ChunkPos startChunk,
            StructureTemplate template,
            DswhaleVillageSavedData.PlacementPlan plan,
            boolean recovering
    ) {
        StructurePlaceSettings settings = placementSettings(plan.rotation());
        BoundingBox residenceBox = template.getBoundingBox(settings, plan.templateOrigin());
        if (!areChunksLoaded(
                level,
                residenceBox.minX(),
                residenceBox.minZ(),
                residenceBox.maxX(),
                residenceBox.maxZ()
        )) {
            return AttemptResult.WAITING_FOR_CHUNKS;
        }

        CriticalPositions positions = criticalPositions(plan.templateOrigin(), settings);
        boolean completeStructure = hasCompleteStructure(level, positions);
        if (completeStructure) {
            return finishResidentAndState(level, startChunk, positions, plan, false);
        }

        HorizontalCandidate horizontal = new HorizontalCandidate(
                residenceBox.minX(),
                residenceBox.minZ(),
                residenceBox.getXSpan(),
                residenceBox.getZSpan(),
                plan.rotation()
        );
        TerrainAssessment terrain = null;
        if (!recovering || !hasAnyCriticalStructure(level, positions)) {
            TerrainCheck terrainCheck = assessTerrain(level, horizontal, residenceBox.getYSpan());
            terrain = terrainCheck.assessment();
            if (terrain == null || terrain.baseY() != residenceBox.minY()) {
                String detail = terrainCheck.rejection() == null
                        ? "base height changed"
                        : terrainCheck.rejection().serializedName();
                fail(level, startChunk, "the selected site changed before placement: " + detail);
                return AttemptResult.FAILED;
            }
        }

        BoundingBox snapshotBox = new BoundingBox(
                residenceBox.minX(),
                residenceBox.minY() - MAX_HEIGHT_DIFFERENCE,
                residenceBox.minZ(),
                residenceBox.maxX(),
                residenceBox.maxY(),
                residenceBox.maxZ()
        );
        List<BlockSnapshot> snapshot = captureBlocks(level, snapshotBox);
        try {
            if (terrain != null) {
                placeFoundation(level, horizontal, terrain);
            }
            boolean placed = template.placeInWorld(
                    level,
                    plan.templateOrigin(),
                    plan.templateOrigin(),
                    settings,
                    RandomSource.create(placementSeed(level.getSeed(), startChunk)),
                    2
            );
            ensureLootChest(level, positions.chest());
            if (!placed || !hasCompleteStructure(level, positions)) {
                LOGGER.warn(
                        "Incomplete dswhale residence at {} after template placement: placed={}, {}",
                        plan.templateOrigin().toShortString(),
                        placed,
                        describeCriticalState(level, positions)
                );
                restoreBlocks(level, snapshot);
                fail(level, startChunk, "the template did not place all required blocks");
                return AttemptResult.FAILED;
            }

            AttemptResult result = finishResidentAndState(
                    level,
                    startChunk,
                    positions,
                    plan,
                    false
            );
            if (result != AttemptResult.PLACED) {
                restoreBlocks(level, snapshot);
            }
            return result;
        } catch (RuntimeException exception) {
            restoreBlocks(level, snapshot);
            fail(level, startChunk, "an exception occurred while placing the template");
            LOGGER.error(
                    "Failed to place dswhale residence for village [{}, {}]",
                    startChunk.x,
                    startChunk.z,
                    exception
            );
            return AttemptResult.FAILED;
        }
    }

    private static AttemptResult finishResidentAndState(
            ServerLevel level,
            ChunkPos startChunk,
            CriticalPositions positions,
            DswhaleVillageSavedData.PlacementPlan plan,
            boolean additionalResidence
    ) {
        List<DswhaleEntity> existing = level.getEntitiesOfClass(
                DswhaleEntity.class,
                new AABB(positions.spawn()).inflate(16.0D),
                entity -> entity.getResidence()
                        .filter(residence -> residence.dimension().equals(level.dimension()))
                        .filter(residence -> residence.bed().equals(positions.bed()))
                        .isPresent()
        );
        if (!existing.isEmpty()) {
            existing.stream().skip(1).forEach(DswhaleEntity::discard);
            return recordCompletedPlacement(level, startChunk, plan, additionalResidence)
                    ? AttemptResult.PLACED
                    : AttemptResult.NO_ACTION;
        }

        DswhaleEntity resident = ModEntities.DSWHALE.get().create(level);
        if (resident == null) {
            failIfNatural(level, startChunk, additionalResidence, "the dswhale entity could not be created");
            return AttemptResult.FAILED;
        }

        double spawnX = positions.spawn().getX() + 0.5D;
        double spawnY = positions.spawn().getY();
        double spawnZ = positions.spawn().getZ() + 0.5D;
        double deltaX = positions.entrance().getX() - positions.spawn().getX();
        double deltaZ = positions.entrance().getZ() - positions.spawn().getZ();
        float facing = (float) (Mth.atan2(-deltaX, deltaZ) * Mth.RAD_TO_DEG);
        resident.moveTo(spawnX, spawnY, spawnZ, facing, 0.0F);
        resident.setYHeadRot(facing);
        resident.yBodyRot = facing;
        resident.finalizeSpawn(
                level,
                level.getCurrentDifficultyAt(positions.spawn()),
                MobSpawnType.STRUCTURE,
                null,
                null
        );
        resident.setPersistenceRequired();
        if (!resident.setResidence(level.dimension(), positions.home(), positions.bed())
                || !level.noCollision(resident)
                || !level.addFreshEntity(resident)) {
            resident.discard();
            failIfNatural(level, startChunk, additionalResidence,
                    "the bound resident could not be initialized safely");
            return AttemptResult.FAILED;
        }

        try {
            if (!recordCompletedPlacement(level, startChunk, plan, additionalResidence)) {
                resident.discard();
                return AttemptResult.NO_ACTION;
            }
        } catch (RuntimeException exception) {
            resident.discard();
            throw exception;
        }

        LOGGER.info(
                "Placed dswhale residence at [{}, {}, {}] with rotation {}",
                positions.home().getX(),
                positions.home().getY(),
                positions.home().getZ(),
                plan.rotation().name()
        );
        return AttemptResult.PLACED;
    }

    private static boolean recordCompletedPlacement(
            ServerLevel level,
            ChunkPos startChunk,
            DswhaleVillageSavedData.PlacementPlan plan,
            boolean additionalResidence
    ) {
        DswhaleVillageSavedData data = DswhaleVillageSavedData.get(level);
        return additionalResidence
                ? data.recordAdditionalResidence(plan)
                : data.markPlaced(startChunk);
    }

    private static void failIfNatural(
            ServerLevel level,
            ChunkPos startChunk,
            boolean additionalResidence,
            String reason
    ) {
        if (!additionalResidence) {
            fail(level, startChunk, reason);
        }
    }

    private static CriticalPositions criticalPositions(BlockPos origin, StructurePlaceSettings settings) {
        return new CriticalPositions(
                transform(origin, settings, HOME_ANCHOR),
                transform(origin, settings, RESIDENT_SPAWN),
                transform(origin, settings, BED_HEAD),
                transform(origin, settings, ENTRANCE),
                transform(origin, settings, LOOT_CHEST)
        );
    }

    private static BlockPos transform(BlockPos origin, StructurePlaceSettings settings, BlockPos local) {
        return StructureTemplate.calculateRelativePosition(settings, local).offset(origin);
    }

    private static boolean hasCompleteStructure(ServerLevel level, CriticalPositions positions) {
        BlockState bed = level.getBlockState(positions.bed());
        BlockState entrance = level.getBlockState(positions.entrance());
        BlockEntity chest = level.getBlockEntity(positions.chest());
        CompoundTag chestData = chest == null ? null : chest.saveWithFullMetadata();
        boolean validLoot = chestData != null
                && (LOOT_TABLE_ID.toString().equals(chestData.getString("LootTable"))
                || !chestData.getList("Items", Tag.TAG_COMPOUND).isEmpty());
        return level.getBlockState(positions.home()).isAir()
                && level.getBlockState(positions.home().above()).isAir()
                && bed.is(BlockTags.BEDS)
                && entrance.getBlock() instanceof DoorBlock
                && chest instanceof RandomizableContainerBlockEntity
                && validLoot;
    }

    private static boolean hasAnyCriticalStructure(ServerLevel level, CriticalPositions positions) {
        return level.getBlockState(positions.bed()).is(BlockTags.BEDS)
                || level.getBlockState(positions.entrance()).getBlock() instanceof DoorBlock
                || level.getBlockEntity(positions.chest()) instanceof RandomizableContainerBlockEntity;
    }

    private static String describeCriticalState(ServerLevel level, CriticalPositions positions) {
        BlockState bed = level.getBlockState(positions.bed());
        BlockState entrance = level.getBlockState(positions.entrance());
        BlockEntity chest = level.getBlockEntity(positions.chest());
        CompoundTag chestData = chest == null ? null : chest.saveWithFullMetadata();
        return "home=" + level.getBlockState(positions.home())
                + ", homeAbove=" + level.getBlockState(positions.home().above())
                + ", bed=" + bed
                + ", entrance=" + entrance
                + ", chest=" + (chest == null ? "null" : chest.getType())
                + ", lootTable=" + (chestData == null ? "null" : chestData.getString("LootTable"))
                + ", itemEntries=" + (chestData == null
                ? "null"
                : chestData.getList("Items", Tag.TAG_COMPOUND).size());
    }

    private static void ensureLootChest(ServerLevel level, BlockPos chestPos) {
        BlockEntity blockEntity = level.getBlockEntity(chestPos);
        if (!(blockEntity instanceof RandomizableContainerBlockEntity)) {
            return;
        }
        CompoundTag data = blockEntity.saveWithFullMetadata();
        if (data.getString("LootTable").isEmpty()
                && data.getList("Items", Tag.TAG_COMPOUND).isEmpty()) {
            RandomizableContainerBlockEntity.setLootTable(
                    level,
                    level.getRandom(),
                    chestPos,
                    LOOT_TABLE_ID
            );
        }
    }

    private static void placeFoundation(
            ServerLevel level,
            HorizontalCandidate candidate,
            TerrainAssessment terrain
    ) {
        for (int localZ = 0; localZ < candidate.depth(); localZ++) {
            for (int localX = 0; localX < candidate.width(); localX++) {
                int surfaceY = terrain.surfaceHeights()[localZ * candidate.width() + localX];
                for (int y = surfaceY + 1; y < terrain.baseY(); y++) {
                    level.setBlock(
                            new BlockPos(candidate.minX() + localX, y, candidate.minZ() + localZ),
                            Blocks.COBBLESTONE.defaultBlockState(),
                            2
                    );
                }
            }
        }
    }

    private static List<BlockSnapshot> captureBlocks(ServerLevel level, BoundingBox box) {
        List<BlockSnapshot> snapshots = new ArrayList<>(box.getXSpan() * box.getYSpan() * box.getZSpan());
        for (BlockPos pos : BlockPos.betweenClosed(
                box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ()
        )) {
            BlockPos immutable = pos.immutable();
            BlockEntity blockEntity = level.getBlockEntity(immutable);
            snapshots.add(new BlockSnapshot(
                    immutable,
                    level.getBlockState(immutable),
                    blockEntity == null ? null : blockEntity.saveWithFullMetadata()
            ));
        }
        return snapshots;
    }

    private static void restoreBlocks(ServerLevel level, List<BlockSnapshot> snapshots) {
        for (BlockSnapshot snapshot : snapshots) {
            level.setBlock(snapshot.pos(), snapshot.state(), 2);
            if (snapshot.blockEntityData() != null) {
                BlockEntity restored = level.getBlockEntity(snapshot.pos());
                if (restored != null) {
                    restored.load(snapshot.blockEntityData());
                    restored.setChanged();
                }
            }
        }
    }

    private static boolean areChunksLoaded(
            ServerLevel level,
            int minX,
            int minZ,
            int maxX,
            int maxZ
    ) {
        int minChunkX = SectionRange.minChunk(minX);
        int maxChunkX = SectionRange.maxChunk(maxX);
        int minChunkZ = SectionRange.minChunk(minZ);
        int maxChunkZ = SectionRange.maxChunk(maxZ);
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                if (!level.getChunkSource().hasChunk(chunkX, chunkZ)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static void loadChunks(ServerLevel level, BoundingBox box) {
        int minChunkX = SectionRange.minChunk(box.minX());
        int maxChunkX = SectionRange.maxChunk(box.maxX());
        int minChunkZ = SectionRange.minChunk(box.minZ());
        int maxChunkZ = SectionRange.maxChunk(box.maxZ());
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                level.getChunk(chunkX, chunkZ);
            }
        }
    }

    private static StructurePlaceSettings placementSettings(Rotation rotation) {
        return new StructurePlaceSettings()
                .setMirror(Mirror.NONE)
                .setRotation(rotation)
                .setRotationPivot(BlockPos.ZERO)
                .setIgnoreEntities(true)
                .setKeepLiquids(false);
    }

    private static long placementSeed(long worldSeed, ChunkPos startChunk) {
        long value = worldSeed ^ startChunk.toLong() ^ 0x44535748414C4535L;
        value ^= value >>> 33;
        value *= 0xff51afd7ed558ccdL;
        value ^= value >>> 33;
        value *= 0xc4ceb9fe1a85ec53L;
        return value ^ value >>> 33;
    }

    private static void fail(ServerLevel level, ChunkPos startChunk, String reason) {
        if (DswhaleVillageSavedData.get(level).markFailed(startChunk, reason)) {
            LOGGER.info(
                    "Skipped dswhale residence for village [{}, {}]: {}",
                    startChunk.x,
                    startChunk.z,
                    reason
            );
        }
    }

    private enum AttemptResult {
        PLACED,
        FAILED,
        WAITING_FOR_CHUNKS,
        NO_ACTION
    }

    public enum CommandPlacementResult {
        PLACED,
        TEMPLATE_MISSING,
        ALREADY_RECORDED,
        OUTSIDE_BUILD_HEIGHT,
        INCOMPLETE_TEMPLATE,
        RESIDENT_FAILED,
        EXCEPTION
    }

    private record TaskKey(ResourceKey<Level> dimension, long startChunk) {
    }

    private record PendingTask(int nextAttemptTick, int loadRetries) {
    }

    private record GeneratedPendingTask(
            DswhaleVillageSavedData.PlacementPlan plan,
            int nextAttemptTick,
            int loadRetries
    ) {
    }

    private record HorizontalCandidate(int minX, int minZ, int width, int depth, Rotation rotation) {
        int maxX() {
            return minX + width - 1;
        }

        int maxZ() {
            return minZ + depth - 1;
        }
    }

    private record TerrainAssessment(int baseY, int[] surfaceHeights) {
    }

    private record TerrainCheck(TerrainAssessment assessment, SiteRejection rejection) {
        static TerrainCheck accepted(TerrainAssessment assessment) {
            return new TerrainCheck(assessment, null);
        }

        static TerrainCheck rejected(SiteRejection rejection) {
            return new TerrainCheck(null, rejection);
        }
    }

    private record CandidateSearchResult(
            DswhaleVillageSavedData.PlacementPlan plan,
            boolean encounteredUnloadedChunks,
            String rejectionSummary
    ) {
    }

    private enum StructureConflict {
        CLEAR,
        INTERSECTS,
        UNLOADED
    }

    private enum SiteRejection {
        UNLOADED_CHUNKS("unloaded_chunks"),
        INVALID_SURFACE("invalid_surface"),
        UNEVEN_TERRAIN("uneven_terrain"),
        PROTECTED_BLOCK("protected_block"),
        TOO_OBSTRUCTED("too_obstructed"),
        VILLAGE_OVERLAP("village_overlap"),
        OTHER_STRUCTURE("other_structure");

        private final String serializedName;

        SiteRejection(String serializedName) {
            this.serializedName = serializedName;
        }

        String serializedName() {
            return serializedName;
        }
    }

    private record CriticalPositions(
            BlockPos home,
            BlockPos spawn,
            BlockPos bed,
            BlockPos entrance,
            BlockPos chest
    ) {
    }

    private record BlockSnapshot(BlockPos pos, BlockState state, CompoundTag blockEntityData) {
    }

    private static final class SectionRange {
        private SectionRange() {
        }

        static int minChunk(int blockCoordinate) {
            return SectionPos.blockToSectionCoord(blockCoordinate);
        }

        static int maxChunk(int blockCoordinate) {
            return SectionPos.blockToSectionCoord(blockCoordinate);
        }
    }
}
