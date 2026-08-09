package io.github.bingehanpi.funny_cat_stick.event;

import io.github.bingehanpi.funny_cat_stick.FunnyCatStick;
import io.github.bingehanpi.funny_cat_stick.effect.CatChatEffect;
import io.github.bingehanpi.funny_cat_stick.command.FunnyCatStickCommands;
import io.github.bingehanpi.funny_cat_stick.command.LootTestCommand;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLEnvironment;

import java.util.List;
import java.util.function.Function;

@Mod.EventBusSubscriber(modid = FunnyCatStick.MOD_ID)
public final class CommonEvents {

    private static final List<Function<Component, Component>> CHAT_TRANSFORMATIONS = List.of(
            message -> Component.literal("\u4e3b\u4eba,")
                    .append(message.copy())
                    .append(",\u8bf7\u60e9\u7f5a\u6211\u55b5!~"),
            message -> message.copy().append("(\u2032\u3078`\u3001 )"),
            message -> Component.literal("\u55b5\u545c~ ")
                    .append(message.copy())
                    .append(" (=^\uff65\u03c9\uff65^=)"),
            message -> Component.literal("\u4eba\u5bb6\u53ea\u662f\u60f3\u8bf4:")
                    .append(message.copy())
                    .append("\u55b5~"),
            message -> Component.literal("(\u5c0f\u58f0)")
                    .append(message.copy())
                    .append("..."),
            message -> message.copy().append(",\u624d\u3001\u624d\u4e0d\u662f\u6545\u610f\u8bf4\u7ed9\u4f60\u542c\u7684\u55b5!"),
            message -> Component.literal("\u4e3b\u4eba\u8bf7\u542c\u597d,")
                    .append(message.copy())
                    .append("!"),
            message -> message.copy().append(" (=\u00b4\u03c9`=)"),
            message -> Component.literal("\u545c\u55b5...")
                    .append(message.copy())
                    .append(",\u53ef\u4ee5\u6478\u6478\u5934\u5417?"),
            message -> Component.literal("\u6700\u559c\u6b22\u4e3b\u4eba\u4e86,\u6240\u4ee5\u8981\u8bf4:")
                    .append(message.copy())
    );

    private CommonEvents() {
    }

    @SubscribeEvent
    public static void onServerChat(ServerChatEvent event) {
        if (!CatChatEffect.isActive(event.getPlayer())) {
            return;
        }

        int index = event.getPlayer().getRandom().nextInt(CHAT_TRANSFORMATIONS.size());
        event.setMessage(CHAT_TRANSFORMATIONS.get(index).apply(event.getMessage()));
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        FunnyCatStickCommands.register(event.getDispatcher());
        if (!FMLEnvironment.production) {
            LootTestCommand.register(event.getDispatcher());
        }
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
