package io.github.bingehanpi.funny_cat_stick.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.function.Supplier;

public final class PlayIkunMusicPacket {

    private static Runnable clientHandler = () -> {
    };

    public static void encode(PlayIkunMusicPacket packet, FriendlyByteBuf buffer) {
    }

    public static PlayIkunMusicPacket decode(FriendlyByteBuf buffer) {
        return new PlayIkunMusicPacket();
    }

    public static void handle(PlayIkunMusicPacket packet, Supplier<NetworkEvent.Context> context) {
        clientHandler.run();
    }

    public static void setClientHandler(Runnable handler) {
        clientHandler = Objects.requireNonNull(handler);
    }
}
