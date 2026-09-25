package io.github.bingehanpi.funny_cat_stick;

import io.github.bingehanpi.funny_cat_stick.config.ModServerConfig;
import io.github.bingehanpi.funny_cat_stick.effect.CatChatTransformations;
import io.github.bingehanpi.funny_cat_stick.network.ModNetworking;
import io.github.bingehanpi.funny_cat_stick.registry.ModBlocks;
import io.github.bingehanpi.funny_cat_stick.registry.ModCreativeTabs;
import io.github.bingehanpi.funny_cat_stick.registry.ModEnchantments;
import io.github.bingehanpi.funny_cat_stick.registry.ModEntities;
import io.github.bingehanpi.funny_cat_stick.registry.ModItems;
import io.github.bingehanpi.funny_cat_stick.registry.ModLootModifiers;
import io.github.bingehanpi.funny_cat_stick.registry.ModSounds;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import software.bernie.geckolib.GeckoLib;

@Mod(FunnyCatStick.MOD_ID)
public final class FunnyCatStick {

    public static final String MOD_ID = "funny_cat_stick";

    public FunnyCatStick(FMLJavaModLoadingContext context) {
        GeckoLib.initialize();

        IEventBus modEventBus = context.getModEventBus();

        ModEntities.register(modEventBus);
        ModItems.register(modEventBus);
        ModEnchantments.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        ModLootModifiers.register(modEventBus);
        ModSounds.register(modEventBus);
        context.registerConfig(ModConfig.Type.SERVER, ModServerConfig.SPEC);
        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            CatChatTransformations.validateResources();
            ModNetworking.register();
        });
    }
}
