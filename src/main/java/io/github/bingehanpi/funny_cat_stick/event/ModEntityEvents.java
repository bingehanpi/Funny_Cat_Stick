package io.github.bingehanpi.funny_cat_stick.event;

import io.github.bingehanpi.funny_cat_stick.FunnyCatStick;
import io.github.bingehanpi.funny_cat_stick.entity.DswhaleEntity;
import io.github.bingehanpi.funny_cat_stick.registry.ModEntities;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FunnyCatStick.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModEntityEvents {

    private ModEntityEvents() {
    }

    @SubscribeEvent
    public static void onCreateEntityAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.DSWHALE.get(), DswhaleEntity.createAttributes().build());
    }
}
