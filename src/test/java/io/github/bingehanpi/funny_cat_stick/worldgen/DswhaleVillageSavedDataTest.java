package io.github.bingehanpi.funny_cat_stick.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Rotation;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class DswhaleVillageSavedDataTest {

    @Test
    void roundTripsPendingVillageAndPlacementPlan() {
        ChunkPos startChunk = new ChunkPos(-12, 34);
        BlockPos templateOrigin = new BlockPos(-180, 71, 552);
        DswhaleVillageSavedData original = new DswhaleVillageSavedData();

        assertTrue(original.recordIfAbsent(startChunk, DswhaleVillageSavedData.Status.PENDING));
        assertTrue(original.setPlacementPlan(startChunk, templateOrigin, Rotation.CLOCKWISE_90));

        DswhaleVillageSavedData restored = DswhaleVillageSavedData.load(original.save(new CompoundTag()));

        assertEquals(DswhaleVillageSavedData.Status.PENDING, restored.getStatus(startChunk).orElseThrow());
        DswhaleVillageSavedData.PlacementPlan placement = restored.getPlacementPlan(startChunk).orElseThrow();
        assertEquals(templateOrigin, placement.templateOrigin());
        assertEquals(Rotation.CLOCKWISE_90, placement.rotation());
        assertTrue(restored.getFailureReason(startChunk).isEmpty());
    }

    @Test
    void preservesPlacementAndFailureReasonWhenPlacementRollsBack() {
        ChunkPos startChunk = new ChunkPos(7, -9);
        BlockPos templateOrigin = new BlockPos(120, 64, -140);
        DswhaleVillageSavedData data = new DswhaleVillageSavedData();
        data.recordIfAbsent(startChunk, DswhaleVillageSavedData.Status.PENDING);
        data.setPlacementPlan(startChunk, templateOrigin, Rotation.COUNTERCLOCKWISE_90);

        assertTrue(data.markFailed(startChunk, "resident_spawn_failed"));
        assertFalse(data.markPlaced(startChunk));

        DswhaleVillageSavedData restored = DswhaleVillageSavedData.load(data.save(new CompoundTag()));
        assertEquals(DswhaleVillageSavedData.Status.FAILED, restored.getStatus(startChunk).orElseThrow());
        assertEquals("resident_spawn_failed", restored.getFailureReason(startChunk).orElseThrow());
        assertEquals(templateOrigin, restored.getPlacementPlan(startChunk).orElseThrow().templateOrigin());
    }

    @Test
    void rejectsDuplicateRecordsAndInvalidStateTransitions() {
        ChunkPos startChunk = new ChunkPos(2, 3);
        DswhaleVillageSavedData data = new DswhaleVillageSavedData();

        assertTrue(data.recordIfAbsent(startChunk, DswhaleVillageSavedData.Status.CHECKED));
        assertFalse(data.recordIfAbsent(startChunk, DswhaleVillageSavedData.Status.PENDING));
        assertFalse(data.setPlacementPlan(startChunk, BlockPos.ZERO, Rotation.NONE));
        assertFalse(data.markPlaced(startChunk));
        assertFalse(data.markFailed(startChunk, "unexpected"));
        assertEquals(DswhaleVillageSavedData.Status.CHECKED, data.getStatus(startChunk).orElseThrow());
    }

    @Test
    void ignoresMalformedAndUnknownSavedRecords() {
        CompoundTag root = new CompoundTag();
        ListTag villages = new ListTag();

        CompoundTag missingCoordinates = new CompoundTag();
        missingCoordinates.putString("Status", "pending");
        villages.add(missingCoordinates);

        CompoundTag unknownStatus = new CompoundTag();
        unknownStatus.putInt("X", 1);
        unknownStatus.putInt("Z", 2);
        unknownStatus.putString("Status", "future_status");
        villages.add(unknownStatus);

        root.put("Villages", villages);
        DswhaleVillageSavedData restored = DswhaleVillageSavedData.load(root);

        assertEquals(0, restored.size());
        assertEquals(
                Map.of(
                        DswhaleVillageSavedData.Status.CHECKED, 0,
                        DswhaleVillageSavedData.Status.PENDING, 0,
                        DswhaleVillageSavedData.Status.PLACED, 0,
                        DswhaleVillageSavedData.Status.FAILED, 0
                ),
                restored.countByStatus()
        );
    }
}
