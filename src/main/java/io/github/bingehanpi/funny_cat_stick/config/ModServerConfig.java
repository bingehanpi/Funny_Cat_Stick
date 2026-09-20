package io.github.bingehanpi.funny_cat_stick.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class ModServerConfig {

    public static final ForgeConfigSpec SPEC;
    private static final ForgeConfigSpec.BooleanValue IKUN_MUSIC_ENABLED;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("music");
        IKUN_MUSIC_ENABLED = builder
                .comment("Whether an IKUN personality Funny Cat Stick plays its music for the owner and target.")
                .define("ikunMusicEnabled", false);
        builder.pop();
        SPEC = builder.build();
    }

    private ModServerConfig() {
    }

    public static boolean isIkunMusicEnabled() {
        return IKUN_MUSIC_ENABLED.get();
    }

    public static void setIkunMusicEnabled(boolean enabled) {
        IKUN_MUSIC_ENABLED.set(enabled);
        SPEC.save();
    }
}
