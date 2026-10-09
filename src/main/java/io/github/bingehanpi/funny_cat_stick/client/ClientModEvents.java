package io.github.bingehanpi.funny_cat_stick.client;

import io.github.bingehanpi.funny_cat_stick.FunnyCatStick;
import io.github.bingehanpi.funny_cat_stick.client.renderer.DswhaleRenderer;
import io.github.bingehanpi.funny_cat_stick.client.renderer.FunnyCatStickDisplayStandRenderer;
import io.github.bingehanpi.funny_cat_stick.network.PlayIkunMusicPacket;
import io.github.bingehanpi.funny_cat_stick.registry.ModBlockEntities;
import io.github.bingehanpi.funny_cat_stick.registry.ModEntities;
import io.github.bingehanpi.funny_cat_stick.registry.ModItems;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Mod.EventBusSubscriber(modid = FunnyCatStick.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientModEvents {

    private static final int ANTI_MEOW_POTION_COLOR = 0xA8DDF0;
    private static final int UNFINISHED_ANTI_MEOW_POTION_COLOR = 0x24518F;

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
        event.registerBlockEntityRenderer(
                ModBlockEntities.FUNNY_CAT_STICK_DISPLAY_STAND.get(),
                FunnyCatStickDisplayStandRenderer::new
        );
    }

    @SubscribeEvent
    public static void onRegisterItemColors(RegisterColorHandlersEvent.Item event) {
        event.register(
                (stack, tintIndex) -> tintIndex == 0 ? ANTI_MEOW_POTION_COLOR : -1,
                ModItems.ANTI_MEOW_POTION.get()
        );
        event.register(
                (stack, tintIndex) -> tintIndex == 0 ? UNFINISHED_ANTI_MEOW_POTION_COLOR : -1,
                ModItems.UNFINISHED_ANTI_MEOW_POTION.get()
        );
    }
}
