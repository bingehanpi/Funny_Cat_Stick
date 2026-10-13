package io.github.bingehanpi.funny_cat_stick.stasis;

import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CatgirlStasisStateTest {

    @Test
    void roundTripsEveryValue() {
        CatgirlStasisState original = new CatgirlStasisState(
                1200,
                12.25D,
                -4.5D,
                99.75D,
                135.0F,
                -20.0F,
                true,
                false
        );

        assertEquals(original, CatgirlStasisState.load(original.save()).orElseThrow());
    }

    @Test
    void rejectsIncompleteOrInvalidState() {
        assertTrue(CatgirlStasisState.load(new CompoundTag()).isEmpty());

        CatgirlStasisState state = new CatgirlStasisState(
                1,
                Double.NaN,
                0.0D,
                0.0D,
                0.0F,
                0.0F,
                false,
                false
        );
        assertTrue(CatgirlStasisState.load(state.save()).isEmpty());
        assertTrue(CatgirlStasisState.load(state.withRemainingTicks(-1).save()).isEmpty());
    }
}
