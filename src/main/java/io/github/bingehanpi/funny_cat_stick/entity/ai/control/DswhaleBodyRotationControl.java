package io.github.bingehanpi.funny_cat_stick.entity.ai.control;

import io.github.bingehanpi.funny_cat_stick.entity.DswhaleEntity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.BodyRotationControl;

/**
 * Keeps the dswhale's body independent from small head movements while still
 * allowing the body to catch up when the requested look angle is too large.
 */
public final class DswhaleBodyRotationControl extends BodyRotationControl {

    private static final double MOVEMENT_EPSILON = 2.500000277905201E-7D;
    private static final float MOVING_BODY_TURN_SPEED = 10.0F;
    private static final float LOOKING_BODY_TURN_SPEED = 4.0F;

    private final DswhaleEntity mob;

    public DswhaleBodyRotationControl(DswhaleEntity mob) {
        super(mob);
        this.mob = mob;
    }

    @Override
    public void clientTick() {
        if (isMoving()) {
            // Movement owns the body's facing, but it should not snap to a new yaw.
            mob.yBodyRot = Mth.approachDegrees(mob.yBodyRot, mob.getYRot(), MOVING_BODY_TURN_SPEED);
            clampHeadToBody();
            return;
        }

        if (mob.getFirstPassenger() instanceof Mob) {
            return;
        }

        float headBodyDelta = Mth.wrapDegrees(mob.yHeadRot - mob.yBodyRot);
        float maxHeadYaw = mob.getMaxHeadYRot();
        if (Math.abs(headBodyDelta) > maxHeadYaw) {
            float desiredBodyYaw = mob.yHeadRot - Mth.clamp(headBodyDelta, -maxHeadYaw, maxHeadYaw);
            mob.yBodyRot = Mth.approachDegrees(mob.yBodyRot, desiredBodyYaw, LOOKING_BODY_TURN_SPEED);
        }

        clampHeadToBody();
    }

    private boolean isMoving() {
        double deltaX = mob.getX() - mob.xo;
        double deltaZ = mob.getZ() - mob.zo;
        return deltaX * deltaX + deltaZ * deltaZ > MOVEMENT_EPSILON;
    }

    private void clampHeadToBody() {
        mob.yHeadRot = Mth.rotateIfNecessary(mob.yHeadRot, mob.yBodyRot, mob.getMaxHeadYRot());
    }
}
