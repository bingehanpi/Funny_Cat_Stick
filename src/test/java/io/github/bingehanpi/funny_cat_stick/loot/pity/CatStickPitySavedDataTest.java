package io.github.bingehanpi.funny_cat_stick.loot.pity;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CatStickPitySavedDataTest {
    @Test
    void playersHaveIndependentProgressAndCanBothVisitTheSameLootrChest() {
        CatStickPitySavedData data = new CatStickPitySavedData();
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        UUID chest = UUID.randomUUID();
        data.record(a, chest, false);
        assertEquals(1, data.failures(a));
        assertEquals(0, data.failures(b));
        assertFalse(data.hasVisitedLootr(b, chest));
        data.record(b, chest, false);
        data.record(a, chest, true);
        assertEquals(1, data.failures(a), "Repeat/refresh must neither increment nor reset progress");
        assertEquals(1, data.failures(b));
    }

    @Test
    void savesProgressAndVisitedChestsAcrossReloadAndKeepsVisitsAfterWinning() {
        CatStickPitySavedData data = new CatStickPitySavedData();
        UUID player = UUID.randomUUID();
        UUID chest = UUID.randomUUID();
        data.record(player, chest, false);
        for (int i = 0; i < 98; i++) {
            data.record(player, null, false);
        }
        data = CatStickPitySavedData.load(data.save(new CompoundTag()));
        assertEquals(99, data.failures(player));
        assertTrue(data.hasVisitedLootr(player, chest));
        assertEquals(1.0D, CatStickPityCurve.chance(data.failures(player) + 1));
        data.record(player, null, true);
        assertEquals(0, data.failures(player));
        data = CatStickPitySavedData.load(data.save(new CompoundTag()));
        assertEquals(0, data.failures(player));
        assertTrue(data.hasVisitedLootr(player, chest));
    }
}
