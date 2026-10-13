package io.github.bingehanpi.funny_cat_stick.mixin;

import io.github.bingehanpi.funny_cat_stick.stasis.CatgirlStasis;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerLevel.class)
public abstract class ServerLevelMixin {

    @Redirect(
            method = "tickNonPassenger",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;tick()V")
    )
    private void funny_cat_stick$tickOrFreeze(Entity entity) {
        if (!CatgirlStasis.tickFrozen(entity)) {
            entity.tick();
        }
    }

    @Redirect(
            method = "tickPassenger",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;rideTick()V")
    )
    private void funny_cat_stick$tickPassengerOrFreeze(Entity entity) {
        if (!CatgirlStasis.tickFrozen(entity)) {
            entity.rideTick();
        }
    }
}
