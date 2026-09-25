package io.github.bingehanpi.funny_cat_stick.event;

import io.github.bingehanpi.funny_cat_stick.FunnyCatStick;
import io.github.bingehanpi.funny_cat_stick.effect.CatChatEffect;
import io.github.bingehanpi.funny_cat_stick.effect.CatChatTransformations;
import io.github.bingehanpi.funny_cat_stick.command.FunnyCatStickCommands;
import io.github.bingehanpi.funny_cat_stick.command.DswhaleResidenceLocateCommand;
import io.github.bingehanpi.funny_cat_stick.command.LootTestCommand;
import io.github.bingehanpi.funny_cat_stick.worldgen.DswhaleVillageDiscovery;
import io.github.bingehanpi.funny_cat_stick.worldgen.DswhaleResidencePlacement;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod.EventBusSubscriber(modid = FunnyCatStick.MOD_ID)
public final class CommonEvents {

    private CommonEvents() {
    }

    @SubscribeEvent
    public static void onServerChat(ServerChatEvent event) {
        if (!CatChatEffect.isActive(event.getPlayer())) {
            return;
        }

        event.setMessage(CatChatTransformations.transform(
                event.getMessage(),
                CatChatEffect.getPersonality(event.getPlayer()),
                event.getPlayer().getRandom()
        ));
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        FunnyCatStickCommands.register(event.getDispatcher());
        DswhaleResidenceLocateCommand.register(event.getDispatcher());
        if (!FMLEnvironment.production) {
            LootTestCommand.register(event.getDispatcher());
        }
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel level) {
            if (event.isNewChunk()) {
                DswhaleVillageDiscovery.inspectNewChunk(level, event.getChunk());
            }
            DswhaleResidencePlacement.schedulePendingNear(level, event.getChunk().getPos());
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            DswhaleResidencePlacement.tick(event.getServer());
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        DswhaleResidencePlacement.clear(event.getServer());
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        CatChatEffect.copyToClone(event.getOriginal(), event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            CatChatEffect.onLogin(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END
                && event.player instanceof ServerPlayer player
                && player.tickCount % 20 == 0) {
            CatChatEffect.tick(player);
        }
    }
}
