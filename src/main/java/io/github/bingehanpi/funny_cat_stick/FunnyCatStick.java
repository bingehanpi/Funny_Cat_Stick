package io.github.bingehanpi.funny_cat_stick;

import io.github.bingehanpi.funny_cat_stick.network.ModNetworking;
import io.github.bingehanpi.funny_cat_stick.registry.ModBlocks;
import io.github.bingehanpi.funny_cat_stick.registry.ModCreativeTabs;
import io.github.bingehanpi.funny_cat_stick.registry.ModItems;
import io.github.bingehanpi.funny_cat_stick.registry.ModLootModifiers;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(FunnyCatStick.MOD_ID)
public final class FunnyCatStick {

    public static final String MOD_ID = "funny_cat_stick";

    public FunnyCatStick(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        ModLootModifiers.register(modEventBus);
        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(ModNetworking::register);
    }
}
