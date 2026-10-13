package io.github.bingehanpi.funny_cat_stick.event;

import io.github.bingehanpi.funny_cat_stick.FunnyCatStick;
import io.github.bingehanpi.funny_cat_stick.stasis.CatgirlStasis;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FunnyCatStick.MOD_ID)
public final class CatgirlStasisEvents {

    private CatgirlStasisEvents() {
    }

    @SubscribeEvent
    public static void onTeleport(EntityTeleportEvent event) {
        if (CatgirlStasis.isActive(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onTravelToDimension(EntityTravelToDimensionEvent event) {
        if (CatgirlStasis.isActive(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onMount(EntityMountEvent event) {
        if (event.isMounting()
                && (CatgirlStasis.isActive(event.getEntityMounting())
                || CatgirlStasis.isActive(event.getEntityBeingMounted()))) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        Entity attacker = event.getSource().getEntity();
        if (attacker != null && CatgirlStasis.isActive(attacker)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide
                && event.getEntity() instanceof Projectile projectile
                && projectile.getOwner() != null
                && CatgirlStasis.isActive(projectile.getOwner())) {
            event.setCanceled(true);
        }
    }
}
