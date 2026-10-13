package io.github.bingehanpi.funny_cat_stick.stasis;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.util.Optional;

record CatgirlStasisState(
        int remainingTicks,
        double x,
        double y,
        double z,
        float yRot,
        float xRot,
        boolean originalNoGravity,
        boolean originalNoAi
) {

    private static final String REMAINING_TICKS = "RemainingTicks";
    private static final String X = "X";
    private static final String Y = "Y";
    private static final String Z = "Z";
    private static final String Y_ROT = "YRot";
    private static final String X_ROT = "XRot";
    private static final String ORIGINAL_NO_GRAVITY = "OriginalNoGravity";
    private static final String ORIGINAL_NO_AI = "OriginalNoAi";

    CatgirlStasisState withRemainingTicks(int ticks) {
        return new CatgirlStasisState(
                ticks,
                x,
                y,
                z,
                yRot,
                xRot,
                originalNoGravity,
                originalNoAi
        );
    }

    CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt(REMAINING_TICKS, remainingTicks);
        tag.putDouble(X, x);
        tag.putDouble(Y, y);
        tag.putDouble(Z, z);
        tag.putFloat(Y_ROT, yRot);
        tag.putFloat(X_ROT, xRot);
        tag.putBoolean(ORIGINAL_NO_GRAVITY, originalNoGravity);
        tag.putBoolean(ORIGINAL_NO_AI, originalNoAi);
        return tag;
    }

    static Optional<CatgirlStasisState> load(CompoundTag tag) {
        if (!tag.contains(REMAINING_TICKS, Tag.TAG_ANY_NUMERIC)
                || !tag.contains(X, Tag.TAG_ANY_NUMERIC)
                || !tag.contains(Y, Tag.TAG_ANY_NUMERIC)
                || !tag.contains(Z, Tag.TAG_ANY_NUMERIC)
                || !tag.contains(Y_ROT, Tag.TAG_ANY_NUMERIC)
                || !tag.contains(X_ROT, Tag.TAG_ANY_NUMERIC)
                || !tag.contains(ORIGINAL_NO_GRAVITY, Tag.TAG_ANY_NUMERIC)
                || !tag.contains(ORIGINAL_NO_AI, Tag.TAG_ANY_NUMERIC)) {
            return Optional.empty();
        }

        CatgirlStasisState state = new CatgirlStasisState(
                tag.getInt(REMAINING_TICKS),
                tag.getDouble(X),
                tag.getDouble(Y),
                tag.getDouble(Z),
                tag.getFloat(Y_ROT),
                tag.getFloat(X_ROT),
                tag.getBoolean(ORIGINAL_NO_GRAVITY),
                tag.getBoolean(ORIGINAL_NO_AI)
        );
        if (state.remainingTicks < 0
                || !Double.isFinite(state.x)
                || !Double.isFinite(state.y)
                || !Double.isFinite(state.z)
                || !Float.isFinite(state.yRot)
                || !Float.isFinite(state.xRot)) {
            return Optional.empty();
        }
        return Optional.of(state);
    }
}
