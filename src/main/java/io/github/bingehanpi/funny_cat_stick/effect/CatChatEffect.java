package io.github.bingehanpi.funny_cat_stick.effect;

import io.github.bingehanpi.funny_cat_stick.FunnyCatStick;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.time.Duration;

public final class CatChatEffect {

    private static final String EXPIRES_AT_KEY = FunnyCatStick.MOD_ID + "_cat_chat_expires_at";
    private static final String LAST_REMAINING_KEY = FunnyCatStick.MOD_ID + "_cat_chat_last_remaining";
    private static final String OWNER_UUID_KEY = FunnyCatStick.MOD_ID + "_cat_chat_owner_uuid";
    private static final String OWNER_NAME_KEY = FunnyCatStick.MOD_ID + "_cat_chat_owner_name";
    private static final String PERSONALITY_KEY = FunnyCatStick.MOD_ID + "_cat_chat_personality";
    public static final long DEFAULT_DURATION_MILLIS = Duration.ofMinutes(30).toMillis();
    private static final int TAMING_MESSAGE_COUNT = 5;
    private static final long[] REMINDER_THRESHOLDS = {
            Duration.ofMinutes(20).toMillis(),
            Duration.ofMinutes(10).toMillis(),
            Duration.ofMinutes(5).toMillis(),
            Duration.ofMinutes(1).toMillis(),
            Duration.ofSeconds(10).toMillis()
    };
    private static final String[] REMINDER_KEYS = {
            "message.funny_cat_stick.remaining.20_minutes",
            "message.funny_cat_stick.remaining.10_minutes",
            "message.funny_cat_stick.remaining.5_minutes",
            "message.funny_cat_stick.remaining.1_minute",
            "message.funny_cat_stick.remaining.10_seconds"
    };

    private CatChatEffect() {
    }

    public static void apply(Player target, Player owner) {
        apply(target, owner, DEFAULT_DURATION_MILLIS, CatPersonality.CLASSIC);
    }

    public static void apply(Player target, Player owner, CatPersonality personality) {
        apply(target, owner, DEFAULT_DURATION_MILLIS, personality);
    }

    public static void apply(Player target, Player owner, long durationMillis) {
        apply(target, owner, durationMillis, CatPersonality.CLASSIC);
    }

    public static void apply(Player target, Player owner, long durationMillis, CatPersonality personality) {
        CompoundTag data = target.getPersistentData();
        long now = System.currentTimeMillis();
        long expiresAt = durationMillis > Long.MAX_VALUE - now ? Long.MAX_VALUE : now + durationMillis;
        data.putLong(EXPIRES_AT_KEY, expiresAt);
        data.putLong(LAST_REMAINING_KEY, durationMillis);
        data.putUUID(OWNER_UUID_KEY, owner.getUUID());
        data.putString(OWNER_NAME_KEY, owner.getGameProfile().getName());
        data.putString(PERSONALITY_KEY, personality.getSerializedName());
    }

    public static void announceTaming(Player owner, ServerPlayer target) {
        MinecraftServer server = target.getServer();
        if (server == null) {
            return;
        }

        int messageIndex = owner.getRandom().nextInt(TAMING_MESSAGE_COUNT);
        server.getPlayerList().broadcastSystemMessage(
                Component.translatable(
                        "message.funny_cat_stick.tamed." + messageIndex,
                        target.getDisplayName(),
                        owner.getDisplayName()
                ),
                false
        );
    }

    public static boolean isActive(Player player) {
        CompoundTag data = player.getPersistentData();
        long expiresAt = data.getLong(EXPIRES_AT_KEY);

        if (expiresAt <= System.currentTimeMillis()) {
            return false;
        }

        return true;
    }

    public static long getRemainingMillis(Player player) {
        if (!isActive(player)) {
            return 0;
        }
        return Math.max(0, player.getPersistentData().getLong(EXPIRES_AT_KEY) - System.currentTimeMillis());
    }

    public static CatPersonality getPersonality(Player player) {
        CompoundTag data = player.getPersistentData();
        if (!data.contains(PERSONALITY_KEY, Tag.TAG_STRING)) {
            return CatPersonality.CLASSIC;
        }
        return CatPersonality.fromSerializedName(data.getString(PERSONALITY_KEY));
    }

    public static long reduce(ServerPlayer player, long amountMillis) {
        long remaining = getRemainingMillis(player);
        if (remaining <= 0) {
            return -1;
        }

        if (amountMillis >= remaining) {
            clear(player, true);
            return 0;
        }

        CompoundTag data = player.getPersistentData();
        long newRemaining = remaining - amountMillis;
        data.putLong(EXPIRES_AT_KEY, System.currentTimeMillis() + newRemaining);
        data.putLong(LAST_REMAINING_KEY, newRemaining);
        return newRemaining;
    }

    public static long extend(ServerPlayer player, long amountMillis) {
        long remaining = getRemainingMillis(player);
        if (remaining <= 0) {
            return -1;
        }
        if (amountMillis <= 0) {
            return remaining;
        }

        CompoundTag data = player.getPersistentData();
        long expiresAt = data.getLong(EXPIRES_AT_KEY);
        long newExpiresAt = amountMillis > Long.MAX_VALUE - expiresAt
                ? Long.MAX_VALUE
                : expiresAt + amountMillis;
        long newRemaining = Math.max(0, newExpiresAt - System.currentTimeMillis());
        data.putLong(EXPIRES_AT_KEY, newExpiresAt);
        data.putLong(LAST_REMAINING_KEY, newRemaining);
        return newRemaining;
    }

    public static void tick(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        if (!data.contains(EXPIRES_AT_KEY, Tag.TAG_LONG)) {
            return;
        }

        long remaining = data.getLong(EXPIRES_AT_KEY) - System.currentTimeMillis();
        if (remaining <= 0) {
            clear(player, true);
            return;
        }

        long previousRemaining = data.contains(LAST_REMAINING_KEY, Tag.TAG_LONG)
                ? data.getLong(LAST_REMAINING_KEY)
                : remaining;

        for (int i = 0; i < REMINDER_THRESHOLDS.length; i++) {
            long threshold = REMINDER_THRESHOLDS[i];
            if (previousRemaining > threshold && remaining <= threshold) {
                player.sendSystemMessage(Component.translatable(REMINDER_KEYS[i]));
                break;
            }
        }

        data.putLong(LAST_REMAINING_KEY, remaining);
    }

    public static void onLogin(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        if (!data.contains(EXPIRES_AT_KEY, Tag.TAG_LONG)) {
            return;
        }

        long remaining = data.getLong(EXPIRES_AT_KEY) - System.currentTimeMillis();
        if (remaining > 0) {
            data.putLong(LAST_REMAINING_KEY, remaining);
        }
    }

    public static void copyToClone(Player original, Player replacement) {
        CompoundTag originalData = original.getPersistentData();
        long expiresAt = originalData.getLong(EXPIRES_AT_KEY);

        if (expiresAt > System.currentTimeMillis()) {
            CompoundTag replacementData = replacement.getPersistentData();
            replacementData.putLong(EXPIRES_AT_KEY, expiresAt);
            replacementData.putLong(LAST_REMAINING_KEY, expiresAt - System.currentTimeMillis());
            if (originalData.hasUUID(OWNER_UUID_KEY)) {
                replacementData.putUUID(OWNER_UUID_KEY, originalData.getUUID(OWNER_UUID_KEY));
            }
            if (originalData.contains(OWNER_NAME_KEY, Tag.TAG_STRING)) {
                replacementData.putString(OWNER_NAME_KEY, originalData.getString(OWNER_NAME_KEY));
            }
            if (originalData.contains(PERSONALITY_KEY, Tag.TAG_STRING)) {
                replacementData.putString(PERSONALITY_KEY, originalData.getString(PERSONALITY_KEY));
            }
        }
    }

    private static void clear(ServerPlayer player, boolean notifyPlayer) {
        CompoundTag data = player.getPersistentData();
        data.remove(EXPIRES_AT_KEY);
        data.remove(LAST_REMAINING_KEY);
        data.remove(OWNER_UUID_KEY);
        data.remove(OWNER_NAME_KEY);
        data.remove(PERSONALITY_KEY);
        if (notifyPlayer) {
            player.sendSystemMessage(Component.translatable("message.funny_cat_stick.expired"));
        }
    }
}
