package io.github.bingehanpi.funny_cat_stick.network;

import io.github.bingehanpi.funny_cat_stick.FunnyCatStick;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ModNetworking {

    private static final String PROTOCOL_VERSION = "3";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            ResourceLocation.fromNamespaceAndPath(FunnyCatStick.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private ModNetworking() {
    }

    public static void register() {
        CHANNEL.messageBuilder(PlayIkunMusicPacket.class, 0, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(PlayIkunMusicPacket::encode)
                .decoder(PlayIkunMusicPacket::decode)
                .consumerMainThread(PlayIkunMusicPacket::handle)
                .add();
    }

    public static void playIkunMusic(ServerPlayer player) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new PlayIkunMusicPacket());
    }
}
