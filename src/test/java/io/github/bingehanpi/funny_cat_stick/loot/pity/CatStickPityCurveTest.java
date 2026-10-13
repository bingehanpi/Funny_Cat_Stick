package io.github.bingehanpi.funny_cat_stick.loot.pity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CatStickPityCurveTest {
    @Test
    void everyEarlyAttemptStaysAtOrBelowTwoPercentAndCurveIsMonotonic() {
        double previous = 0;
        for (int attempt = 1; attempt <= 100; attempt++) {
            double chance = CatStickPityCurve.chance(attempt);
            assertTrue(chance >= previous, "Probability regressed at attempt " + attempt);
            assertTrue(chance <= (attempt <= 80 ? 0.02D : 1.0D) + 1.0E-12);
            previous = chance;
        }
        assertEquals(0.006D, CatStickPityCurve.chance(1), 1.0E-12);
        assertEquals(0.02D, CatStickPityCurve.chance(80), 1.0E-12);
        assertEquals(0.265D, CatStickPityCurve.chance(90), 1.0E-12);
        assertEquals(1.0D, CatStickPityCurve.chance(100));
        assertEquals(1.0D, CatStickPityCurve.chance(101));
    }
}
