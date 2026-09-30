package io.github.bingehanpi.funny_cat_stick.worldgen;

import io.github.bingehanpi.funny_cat_stick.FunnyCatStick;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class DswhaleVillageSavedData extends SavedData {

    private static final String DATA_NAME = FunnyCatStick.MOD_ID + "_dswhale_villages";
    private static final String VILLAGES_TAG = "Villages";
    private static final String X_TAG = "X";
    private static final String Z_TAG = "Z";
    private static final String STATUS_TAG = "Status";
    private static final String PLACEMENT_TAG = "Placement";
    private static final String ORIGIN_TAG = "Origin";
    private static final String ROTATION_TAG = "Rotation";
    private static final String FAILURE_REASON_TAG = "FailureReason";
    private static final String ADDITIONAL_RESIDENCES_TAG = "AdditionalResidences";

    private final Map<Long, VillageRecord> villages = new LinkedHashMap<>();
    private final List<PlacementPlan> additionalResidences = new ArrayList<>();

    public static DswhaleVillageSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                DswhaleVillageSavedData::load,
                DswhaleVillageSavedData::new,
                DATA_NAME
        );
    }

    public static DswhaleVillageSavedData load(CompoundTag compound) {
        DswhaleVillageSavedData data = new DswhaleVillageSavedData();
        ListTag villages = compound.getList(VILLAGES_TAG, Tag.TAG_COMPOUND);
        for (Tag tag : villages) {
            CompoundTag village = (CompoundTag) tag;
            if (!village.contains(X_TAG, Tag.TAG_ANY_NUMERIC)
                    || !village.contains(Z_TAG, Tag.TAG_ANY_NUMERIC)
                    || !village.contains(STATUS_TAG, Tag.TAG_STRING)) {
                continue;
            }

            Status.fromSerializedName(village.getString(STATUS_TAG)).ifPresent(status -> {
                ChunkPos startChunk = new ChunkPos(village.getInt(X_TAG), village.getInt(Z_TAG));
                PlacementPlan placement = readPlacement(village);
                String failureReason = village.contains(FAILURE_REASON_TAG, Tag.TAG_STRING)
                        ? village.getString(FAILURE_REASON_TAG)
                        : null;
                data.villages.putIfAbsent(
                        startChunk.toLong(),
                        new VillageRecord(status, placement, failureReason)
                );
            });
        }
        ListTag additionalResidences = compound.getList(ADDITIONAL_RESIDENCES_TAG, Tag.TAG_COMPOUND);
        for (Tag tag : additionalResidences) {
            PlacementPlan placement = readPlacementTag((CompoundTag) tag);
            if (placement != null && !data.additionalResidences.contains(placement)) {
                data.additionalResidences.add(placement);
            }
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag compound) {
        ListTag villagesTag = new ListTag();
        villages.forEach((packedPos, record) -> {
            ChunkPos startChunk = new ChunkPos(packedPos);
            CompoundTag village = new CompoundTag();
            village.putInt(X_TAG, startChunk.x);
            village.putInt(Z_TAG, startChunk.z);
            village.putString(STATUS_TAG, record.status().serializedName());
            if (record.placement() != null) {
                village.put(PLACEMENT_TAG, writePlacement(record.placement()));
            }
            if (record.failureReason() != null && !record.failureReason().isBlank()) {
                village.putString(FAILURE_REASON_TAG, record.failureReason());
            }
            villagesTag.add(village);
        });
        compound.put(VILLAGES_TAG, villagesTag);

        ListTag additionalResidencesTag = new ListTag();
        additionalResidences.forEach(placement -> additionalResidencesTag.add(writePlacement(placement)));
        compound.put(ADDITIONAL_RESIDENCES_TAG, additionalResidencesTag);
        return compound;
    }

    public Optional<Status> getStatus(ChunkPos startChunk) {
        VillageRecord record = villages.get(startChunk.toLong());
        return record == null ? Optional.empty() : Optional.of(record.status());
    }

    public Optional<PlacementPlan> getPlacementPlan(ChunkPos startChunk) {
        VillageRecord record = villages.get(startChunk.toLong());
        return record == null ? Optional.empty() : Optional.ofNullable(record.placement());
    }

    public Optional<String> getFailureReason(ChunkPos startChunk) {
        VillageRecord record = villages.get(startChunk.toLong());
        return record == null ? Optional.empty() : Optional.ofNullable(record.failureReason());
    }

    public boolean recordIfAbsent(ChunkPos startChunk, Status status) {
        if (villages.putIfAbsent(startChunk.toLong(), new VillageRecord(status, null, null)) != null) {
            return false;
        }
        setDirty();
        return true;
    }

    public boolean markPlaced(ChunkPos startChunk) {
        return transitionPending(startChunk, Status.PLACED, null);
    }

    public boolean markFailed(ChunkPos startChunk, String reason) {
        return transitionPending(startChunk, Status.FAILED, reason);
    }

    public boolean setPlacementPlan(ChunkPos startChunk, BlockPos templateOrigin, Rotation rotation) {
        long packedPos = startChunk.toLong();
        VillageRecord record = villages.get(packedPos);
        if (record == null || record.status() != Status.PENDING) {
            return false;
        }
        if (record.placement() != null) {
            return record.placement().templateOrigin().equals(templateOrigin)
                    && record.placement().rotation() == rotation;
        }
        villages.put(packedPos, new VillageRecord(
                record.status(),
                new PlacementPlan(templateOrigin.immutable(), rotation),
                null
        ));
        setDirty();
        return true;
    }

    private boolean transitionPending(ChunkPos startChunk, Status target, String failureReason) {
        long packedPos = startChunk.toLong();
        VillageRecord record = villages.get(packedPos);
        if (record == null || record.status() != Status.PENDING) {
            return false;
        }
        villages.put(packedPos, new VillageRecord(target, record.placement(), failureReason));
        setDirty();
        return true;
    }

    public int size() {
        return villages.size();
    }

    public Map<Status, Integer> countByStatus() {
        Map<Status, Integer> counts = new EnumMap<>(Status.class);
        for (Status status : Status.values()) {
            counts.put(status, 0);
        }
        villages.values().forEach(record -> counts.compute(
                record.status(),
                (ignored, count) -> count + 1
        ));
        return counts;
    }

    public List<ChunkPos> pendingVillages() {
        List<ChunkPos> pending = new ArrayList<>();
        villages.forEach((packedPos, record) -> {
            if (record.status() == Status.PENDING) {
                pending.add(new ChunkPos(packedPos));
            }
        });
        return List.copyOf(pending);
    }

    public List<VillageEntry> villagesWithStatus(Status status) {
        List<VillageEntry> entries = new ArrayList<>();
        villages.forEach((packedPos, record) -> {
            if (record.status() == status) {
                entries.add(new VillageEntry(
                        new ChunkPos(packedPos),
                        record.status(),
                        Optional.ofNullable(record.placement()),
                        Optional.ofNullable(record.failureReason())
                ));
            }
        });
        return List.copyOf(entries);
    }

    public boolean recordAdditionalResidence(PlacementPlan placement) {
        PlacementPlan immutable = new PlacementPlan(
                placement.templateOrigin().immutable(),
                placement.rotation()
        );
        if (placedResidences().contains(immutable)) {
            return false;
        }
        additionalResidences.add(immutable);
        setDirty();
        return true;
    }

    public boolean hasRecordedResidence(PlacementPlan placement) {
        return placedResidences().contains(placement);
    }

    public List<PlacementPlan> placedResidences() {
        List<PlacementPlan> placements = new ArrayList<>(additionalResidences);
        villages.values().stream()
                .filter(record -> record.status() == Status.PLACED)
                .map(VillageRecord::placement)
                .filter(java.util.Objects::nonNull)
                .filter(placement -> !placements.contains(placement))
                .forEach(placements::add);
        return List.copyOf(placements);
    }

    private static PlacementPlan readPlacement(CompoundTag village) {
        if (!village.contains(PLACEMENT_TAG, Tag.TAG_COMPOUND)) {
            return null;
        }
        return readPlacementTag(village.getCompound(PLACEMENT_TAG));
    }

    private static PlacementPlan readPlacementTag(CompoundTag placement) {
        if (!placement.contains(ORIGIN_TAG, Tag.TAG_COMPOUND)
                || !placement.contains(ROTATION_TAG, Tag.TAG_STRING)) {
            return null;
        }
        try {
            BlockPos origin = NbtUtils.readBlockPos(placement.getCompound(ORIGIN_TAG));
            Rotation rotation = Rotation.valueOf(placement.getString(ROTATION_TAG));
            return new PlacementPlan(origin, rotation);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private static CompoundTag writePlacement(PlacementPlan placementPlan) {
        CompoundTag placement = new CompoundTag();
        placement.put(ORIGIN_TAG, NbtUtils.writeBlockPos(placementPlan.templateOrigin()));
        placement.putString(ROTATION_TAG, placementPlan.rotation().name());
        return placement;
    }

    private record VillageRecord(Status status, PlacementPlan placement, String failureReason) {
    }

    public record PlacementPlan(BlockPos templateOrigin, Rotation rotation) {
    }

    public record VillageEntry(
            ChunkPos startChunk,
            Status status,
            Optional<PlacementPlan> placement,
            Optional<String> failureReason
    ) {
    }

    public enum Status {
        CHECKED,
        PENDING,
        PLACED,
        FAILED;

        public String serializedName() {
            return name().toLowerCase(Locale.ROOT);
        }

        public static Optional<Status> fromSerializedName(String name) {
            try {
                return Optional.of(valueOf(name.toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException exception) {
                return Optional.empty();
            }
        }
    }
}
