package io.github.bingehanpi.funny_cat_stick.entity.ai.goal;

import io.github.bingehanpi.funny_cat_stick.entity.DswhaleEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.pathfinder.Path;

import java.util.EnumSet;
import java.util.Optional;

/**
 * Returns a dswhale to its bound bed at night and lets vanilla sleeping APIs
 * manage its sleeping pose and the bed's occupied state.
 */
public final class DswhaleSleepGoal extends Goal {

    private static final double SPEED_MODIFIER = 0.9D;
    private static final double MAX_HOME_DISTANCE_SQUARED = 48.0D * 48.0D;
    private static final double SLEEP_DISTANCE = 2.0D;
    private static final int MAX_PATH_ATTEMPTS = 3;
    private static final int PATH_RETRY_DELAY = 40;
    private static final int FAILED_PATH_COOLDOWN = 200;

    private final DswhaleEntity mob;
    private BlockPos bedPos;
    private int pathAttempts;
    private long nextPathAttemptTime;
    private long nextStartTime;
    private boolean finished;

    public DswhaleSleepGoal(DswhaleEntity mob) {
        this.mob = mob;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (mob.level().isClientSide
                || mob.isSleeping()
                || mob.isTrading()
                || !mob.canStartSleepingNow()
                || !mob.level().isNight()
                || mob.level().getGameTime() < nextStartTime) {
            return false;
        }

        Optional<DswhaleEntity.Residence> residence = mob.getResidence();
        if (residence.isEmpty()) {
            return false;
        }

        BlockPos candidateBed = residence.get().bed();
        if (!isBoundBed(candidateBed, true)
                || mob.distanceToSqr(candidateBed.getX() + 0.5D, candidateBed.getY() + 0.5D,
                candidateBed.getZ() + 0.5D) > MAX_HOME_DISTANCE_SQUARED) {
            return false;
        }

        bedPos = candidateBed.immutable();
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (bedPos == null || !mob.level().isNight() || (!mob.isSleeping() && mob.isTrading())) {
            return false;
        }

        if (mob.isSleeping()) {
            return mob.getSleepingPos().filter(bedPos::equals).isPresent()
                    && isBoundBed(bedPos, false);
        }

        return !finished
                && mob.canStartSleepingNow()
                && isBoundBed(bedPos, true)
                && mob.distanceToSqr(bedPos.getX() + 0.5D, bedPos.getY() + 0.5D,
                bedPos.getZ() + 0.5D) <= MAX_HOME_DISTANCE_SQUARED;
    }

    @Override
    public void start() {
        pathAttempts = 0;
        nextPathAttemptTime = 0L;
        finished = false;
        mob.getNavigation().stop();
    }

    @Override
    public void stop() {
        mob.getNavigation().stop();
        if (mob.isSleeping()) {
            mob.stopSleeping();
        }
        bedPos = null;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        if (mob.isSleeping() || bedPos == null) {
            return;
        }

        if (!mob.canStartSleepingNow()) {
            finished = true;
            mob.getNavigation().stop();
            return;
        }

        mob.getLookControl().setLookAt(
                bedPos.getX() + 0.5D,
                bedPos.getY() + 0.5D,
                bedPos.getZ() + 0.5D,
                10.0F,
                mob.getMaxHeadXRot()
        );

        if (bedPos.closerToCenterThan(mob.position(), SLEEP_DISTANCE)) {
            if (isBoundBed(bedPos, true)) {
                mob.getNavigation().stop();
                mob.startSleeping(bedPos);
            }
            return;
        }

        long gameTime = mob.level().getGameTime();
        if (mob.getNavigation().isDone() && gameTime >= nextPathAttemptTime) {
            tryPathToBed(gameTime);
        }
    }

    private void tryPathToBed(long gameTime) {
        pathAttempts++;
        Path path = mob.getNavigation().createPath(bedPos, 1);
        if (path != null && path.canReach() && mob.getNavigation().moveTo(path, SPEED_MODIFIER)) {
            return;
        }

        if (pathAttempts >= MAX_PATH_ATTEMPTS) {
            finished = true;
            nextStartTime = gameTime + FAILED_PATH_COOLDOWN;
        } else {
            nextPathAttemptTime = gameTime + PATH_RETRY_DELAY;
        }
    }

    private boolean isBoundBed(BlockPos expectedBed, boolean requireUnoccupied) {
        Optional<DswhaleEntity.Residence> residence = mob.getResidence();
        if (residence.isEmpty()
                || !residence.get().bed().equals(expectedBed)
                || !mob.hasUsableResidence()
                || !mob.level().hasChunkAt(expectedBed)) {
            return false;
        }

        BlockState bedState = mob.level().getBlockState(expectedBed);
        if (!bedState.is(BlockTags.BEDS)
                || !bedState.hasProperty(BedBlock.PART)
                || bedState.getValue(BedBlock.PART) != BedPart.HEAD
                || !bedState.hasProperty(BedBlock.OCCUPIED)) {
            return false;
        }

        return !requireUnoccupied || !bedState.getValue(BedBlock.OCCUPIED);
    }
}
