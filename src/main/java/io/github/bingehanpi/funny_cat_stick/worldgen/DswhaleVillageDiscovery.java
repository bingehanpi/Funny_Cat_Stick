package io.github.bingehanpi.funny_cat_stick.worldgen;

import com.mojang.logging.LogUtils;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.slf4j.Logger;

import java.util.Map;

public final class DswhaleVillageDiscovery {

    public static final double CANDIDATE_CHANCE = 0.15D;

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final long X_MULTIPLIER = 0x632BE59BD9B4E019L;
    private static final long Z_MULTIPLIER = 0x9E3779B97F4A7C15L;

    private DswhaleVillageDiscovery() {
    }

    public static void inspectNewChunk(ServerLevel level, ChunkAccess chunk) {
        if (!Level.OVERWORLD.equals(level.dimension())) {
            return;
        }

        ChunkPos chunkPos = chunk.getPos();
        Registry<Structure> structureRegistry = level.registryAccess().registryOrThrow(Registries.STRUCTURE);
        for (Map.Entry<Structure, StructureStart> entry : chunk.getAllStarts().entrySet()) {
            StructureStart start = entry.getValue();
            if (!structureRegistry.wrapAsHolder(entry.getKey()).is(StructureTags.VILLAGE)
                    || !start.isValid()
                    || !chunkPos.equals(start.getChunkPos())) {
                continue;
            }

            recordVillage(level, start.getChunkPos());
        }
    }

    private static void recordVillage(ServerLevel level, ChunkPos startChunk) {
        DswhaleVillageSavedData data = DswhaleVillageSavedData.get(level);
        if (data.getStatus(startChunk).isPresent()) {
            return;
        }

        boolean candidate = isCandidate(level.getSeed(), level.dimension(), startChunk);
        DswhaleVillageSavedData.Status status = candidate
                ? DswhaleVillageSavedData.Status.PENDING
                : DswhaleVillageSavedData.Status.CHECKED;
        if (data.recordIfAbsent(startChunk, status)) {
            LOGGER.info(
                    "Recorded newly generated village at start chunk [{}, {}] as {} for dswhale residence processing",
                    startChunk.x,
                    startChunk.z,
                    status.serializedName()
            );
        }
    }

    public static boolean isCandidate(long worldSeed, ResourceKey<Level> dimension, ChunkPos startChunk) {
        long value = worldSeed;
        value ^= stableStringHash(dimension.location().toString());
        value ^= (long) startChunk.x * X_MULTIPLIER;
        value ^= (long) startChunk.z * Z_MULTIPLIER;
        long mixed = mix64(value);
        double sample = (mixed >>> 11) * 0x1.0p-53;
        return sample < CANDIDATE_CHANCE;
    }

    private static long stableStringHash(String value) {
        long hash = 0xcbf29ce484222325L;
        for (int index = 0; index < value.length(); index++) {
            hash ^= value.charAt(index);
            hash *= 0x100000001b3L;
        }
        return hash;
    }

    private static long mix64(long value) {
        value ^= value >>> 33;
        value *= 0xff51afd7ed558ccdL;
        value ^= value >>> 33;
        value *= 0xc4ceb9fe1a85ec53L;
        return value ^ value >>> 33;
    }
}
