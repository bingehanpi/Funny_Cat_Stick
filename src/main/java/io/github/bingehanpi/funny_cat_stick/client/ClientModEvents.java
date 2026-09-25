package io.github.bingehanpi.funny_cat_stick.client;

import io.github.bingehanpi.funny_cat_stick.FunnyCatStick;
import io.github.bingehanpi.funny_cat_stick.client.renderer.DswhaleRenderer;
import io.github.bingehanpi.funny_cat_stick.network.PlayIkunMusicPacket;
import io.github.bingehanpi.funny_cat_stick.registry.ModEntities;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Mod.EventBusSubscriber(modid = FunnyCatStick.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {

    private ClientModEvents() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        PlayIkunMusicPacket.setClientHandler(ClientMusicPlayer::play);
        MinecraftForge.EVENT_BUS.addListener(ClientMusicPlayer::onClientTick);
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.DSWHALE.get(), DswhaleRenderer::new);
    }
}
