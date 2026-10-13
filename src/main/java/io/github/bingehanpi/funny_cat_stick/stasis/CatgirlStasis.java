package io.github.bingehanpi.funny_cat_stick.stasis;

import io.github.bingehanpi.funny_cat_stick.FunnyCatStick;
import io.github.bingehanpi.funny_cat_stick.registry.ModEffects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public final class CatgirlStasis {

    public static final int DURATION_TICKS = 20 * 60;

    private static final int MARKER_DURATION_TICKS = Integer.MAX_VALUE;
    private static final String DATA_KEY = FunnyCatStick.MOD_ID + ":catgirl_stasis";
    private static final ThreadLocal<Integer> POSITION_GUARD_BYPASS =
            ThreadLocal.withInitial(() -> 0);

    private CatgirlStasis() {
    }

    public static boolean apply(LivingEntity target) {
        if (!(target.level() instanceof ServerLevel) || target instanceof Player || !target.isAlive()) {
            return false;
        }

        Optional<CatgirlStasisState> currentState = readState(target);
        if (isActive(target) && currentState.isPresent()) {
            CatgirlStasisState refreshed = currentState.get().withRemainingTicks(DURATION_TICKS);
            writeState(target, refreshed);
            enforceFrozenState(target, refreshed);
            ensureMarkerEffect(target);
            return true;
        }

        clearBrokenState(target);
        target.stopRiding();
        target.ejectPassengers();
        stopMotion(target, true);

        CatgirlStasisState newState = new CatgirlStasisState(
                DURATION_TICKS,
                target.getX(),
                target.getY(),
                target.getZ(),
                target.getYRot(),
                target.getXRot(),
                target.isNoGravity(),
                target instanceof Mob mob && mob.isNoAi()
        );
        writeState(target, newState);
        setActive(target, true);
        enforceFrozenState(target, newState);
        ensureMarkerEffect(target);
        return true;
    }

    public static boolean tickFrozen(Entity entity) {
        if (!(entity instanceof LivingEntity living) || !isActive(entity)) {
            return false;
        }

        Optional<CatgirlStasisState> loadedState = readState(entity);
        if (loadedState.isEmpty()) {
            clearBrokenState(living);
            return false;
        }

        CatgirlStasisState state = loadedState.get();
        if (!living.isAlive() || state.remainingTicks() == 0) {
            release(living, state);
            return false;
        }

        enforceFrozenState(living, state);
        tickDamageRecovery(living);
        ensureMarkerEffect(living);
        writeState(living, state.withRemainingTicks(state.remainingTicks() - 1));
        return true;
    }

    public static boolean isActive(Entity entity) {
        return entity instanceof CatgirlStasisAccess access
                && access.funnyCatStick$isCatgirlStasisActive();
    }

    public static boolean shouldBlockPositionChange(Entity entity) {
        return !entity.level().isClientSide
                && isActive(entity)
                && POSITION_GUARD_BYPASS.get() == 0;
    }

    public static void onEntityLoaded(Entity entity, CompoundTag serializedEntity) {
        if (!serializedEntity.contains("ForgeData", Tag.TAG_COMPOUND)) {
            setActive(entity, false);
            return;
        }

        CompoundTag forgeData = serializedEntity.getCompound("ForgeData");
        boolean hasValidState = forgeData.contains(DATA_KEY, Tag.TAG_COMPOUND)
                && CatgirlStasisState.load(forgeData.getCompound(DATA_KEY)).isPresent();
        setActive(entity, hasValidState);
    }

    private static void enforceFrozenState(LivingEntity target, CatgirlStasisState state) {
        if (target.isPassenger()) {
            target.stopRiding();
        }
        if (target.isVehicle()) {
            target.ejectPassengers();
        }

        stopMotion(target, false);
        target.fallDistance = 0.0F;
        if (!target.isNoGravity()) {
            target.setNoGravity(true);
        }
        if (target instanceof Mob mob && !mob.isNoAi()) {
            mob.setNoAi(true);
        }

        boolean moved = Double.compare(target.getX(), state.x()) != 0
                || Double.compare(target.getY(), state.y()) != 0
                || Double.compare(target.getZ(), state.z()) != 0;
        if (moved) {
            runWithoutPositionGuard(() -> target.moveTo(
                    state.x(),
                    state.y(),
                    state.z(),
                    state.yRot(),
                    state.xRot()
            ));
        } else {
            target.setYRot(state.yRot());
            target.setXRot(state.xRot());
        }
        target.setYBodyRot(state.yRot());
        target.setYHeadRot(state.yRot());
    }

    /**
     * Keeps vanilla damage recovery progressing while the entity's normal tick is suppressed.
     * Without this, the first hit leaves {@code invulnerableTime} permanently above the
     * threshold that rejects subsequent equal-or-weaker hits for the rest of the stasis.
     */
    private static void tickDamageRecovery(LivingEntity target) {
        if (target.hurtTime > 0) {
            --target.hurtTime;
        }
        if (target.invulnerableTime > 0) {
            --target.invulnerableTime;
        }
    }

    private static void release(LivingEntity target, CatgirlStasisState state) {
        setActive(target, false);
        target.getPersistentData().remove(DATA_KEY);
        target.removeEffect(ModEffects.CATGIRL_STASIS.get());
        stopMotion(target, true);
        target.setNoGravity(state.originalNoGravity());
        if (target instanceof Mob mob) {
            mob.setNoAi(state.originalNoAi());
        }
    }

    private static void ensureMarkerEffect(LivingEntity target) {
        if (!target.hasEffect(ModEffects.CATGIRL_STASIS.get())) {
            target.addEffect(new MobEffectInstance(
                    ModEffects.CATGIRL_STASIS.get(),
                    MARKER_DURATION_TICKS,
                    0,
                    true,
                    false,
                    false
            ));
        }
    }

    private static void stopMotion(LivingEntity target, boolean forceSync) {
        if (forceSync || !target.getDeltaMovement().equals(Vec3.ZERO)) {
            target.setDeltaMovement(Vec3.ZERO);
            target.hasImpulse = true;
            target.hurtMarked = true;
        }
    }

    private static Optional<CatgirlStasisState> readState(Entity entity) {
        CompoundTag persistentData = entity.getPersistentData();
        if (!persistentData.contains(DATA_KEY, Tag.TAG_COMPOUND)) {
            return Optional.empty();
        }
        return CatgirlStasisState.load(persistentData.getCompound(DATA_KEY));
    }

    private static void writeState(Entity entity, CatgirlStasisState state) {
        entity.getPersistentData().put(DATA_KEY, state.save());
    }

    private static void clearBrokenState(LivingEntity target) {
        setActive(target, false);
        target.getPersistentData().remove(DATA_KEY);
        target.removeEffect(ModEffects.CATGIRL_STASIS.get());
    }

    private static void setActive(Entity entity, boolean active) {
        if (entity instanceof CatgirlStasisAccess access) {
            access.funnyCatStick$setCatgirlStasisActive(active);
        }
    }

    private static void runWithoutPositionGuard(Runnable action) {
        int depth = POSITION_GUARD_BYPASS.get();
        POSITION_GUARD_BYPASS.set(depth + 1);
        try {
            action.run();
        } finally {
            if (depth == 0) {
                POSITION_GUARD_BYPASS.remove();
            } else {
                POSITION_GUARD_BYPASS.set(depth);
            }
        }
    }
}
