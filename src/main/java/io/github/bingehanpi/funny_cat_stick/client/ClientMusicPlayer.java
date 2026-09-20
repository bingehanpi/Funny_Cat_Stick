package io.github.bingehanpi.funny_cat_stick.client;

import io.github.bingehanpi.funny_cat_stick.registry.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraftforge.event.TickEvent;

public final class ClientMusicPlayer {

    private static final int STARTUP_GRACE_TICKS = 20;

    private static SoundInstance currentMusic;
    private static int startupGraceTicks;

    private ClientMusicPlayer() {
    }

    public static void play() {
        Minecraft minecraft = Minecraft.getInstance();
        SoundManager soundManager = minecraft.getSoundManager();

        soundManager.stop(null, SoundSource.MUSIC);
        soundManager.stop(null, SoundSource.RECORDS);

        currentMusic = new SimpleSoundInstance(
                ModSounds.IKUN_MUSIC.get().getLocation(),
                SoundSource.RECORDS,
                1.0F,
                1.0F,
                RandomSource.create(),
                false,
                0,
                SoundInstance.Attenuation.NONE,
                0.0D,
                0.0D,
                0.0D,
                true
        );
        startupGraceTicks = STARTUP_GRACE_TICKS;
        soundManager.play(currentMusic);
    }

    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || currentMusic == null) {
            return;
        }

        SoundManager soundManager = Minecraft.getInstance().getSoundManager();
        if (startupGraceTicks > 0) {
            startupGraceTicks--;
        } else if (!soundManager.isActive(currentMusic)) {
            currentMusic = null;
            return;
        }

        // The custom track uses RECORDS, so this only suppresses background music while it is active.
        soundManager.stop(null, SoundSource.MUSIC);
    }
}
