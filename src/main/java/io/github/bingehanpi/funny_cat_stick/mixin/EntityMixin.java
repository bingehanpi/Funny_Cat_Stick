package io.github.bingehanpi.funny_cat_stick.mixin;

import io.github.bingehanpi.funny_cat_stick.stasis.CatgirlStasis;
import io.github.bingehanpi.funny_cat_stick.stasis.CatgirlStasisAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.RelativeMovement;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Set;

@Mixin(Entity.class)
public abstract class EntityMixin implements CatgirlStasisAccess {

    @Unique
    private boolean funny_cat_stick$catgirlStasisActive;

    @Override
    public boolean funnyCatStick$isCatgirlStasisActive() {
        return funny_cat_stick$catgirlStasisActive;
    }

    @Override
    public void funnyCatStick$setCatgirlStasisActive(boolean active) {
        funny_cat_stick$catgirlStasisActive = active;
    }

    @Inject(method = "load", at = @At("TAIL"))
    private void funny_cat_stick$restoreStasisState(CompoundTag tag, CallbackInfo callback) {
        CatgirlStasis.onEntityLoaded((Entity) (Object) this, tag);
    }

    @Inject(method = "move", at = @At("HEAD"), cancellable = true)
    private void funny_cat_stick$blockMovement(
            MoverType moverType,
            Vec3 movement,
            CallbackInfo callback
    ) {
        if (CatgirlStasis.shouldBlockPositionChange((Entity) (Object) this)) {
            callback.cancel();
        }
    }

    @Inject(method = "setPosRaw", at = @At("HEAD"), cancellable = true)
    private void funny_cat_stick$blockRawPositionChange(
            double x,
            double y,
            double z,
            CallbackInfo callback
    ) {
        if (CatgirlStasis.shouldBlockPositionChange((Entity) (Object) this)) {
            callback.cancel();
        }
    }

    @Inject(
            method = "teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDLjava/util/Set;FF)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void funny_cat_stick$blockCrossDimensionTeleport(
            ServerLevel destination,
            double x,
            double y,
            double z,
            Set<RelativeMovement> relativeMovements,
            float yRot,
            float xRot,
            CallbackInfoReturnable<Boolean> callback
    ) {
        if (CatgirlStasis.shouldBlockPositionChange((Entity) (Object) this)) {
            callback.setReturnValue(false);
        }
    }
}
