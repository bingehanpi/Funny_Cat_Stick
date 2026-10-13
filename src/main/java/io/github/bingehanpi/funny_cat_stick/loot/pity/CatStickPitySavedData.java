package io.github.bingehanpi.funny_cat_stick.loot.pity;

import io.github.bingehanpi.funny_cat_stick.FunnyCatStick;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class CatStickPitySavedData extends SavedData {
    private static final String NAME = FunnyCatStick.MOD_ID + "_cat_stick_pity";
    private final Map<UUID, Progress> players = new HashMap<>();

    public static CatStickPitySavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(
                CatStickPitySavedData::load, CatStickPitySavedData::new, NAME);
    }

    public int failures(UUID player) {
        Progress progress = players.get(player);
        return progress == null ? 0 : progress.failures;
    }

    public boolean hasVisitedLootr(UUID player, UUID container) {
        Progress progress = players.get(player);
        return progress != null && progress.lootrContainers.contains(container);
    }

    public void record(UUID player, UUID lootrContainer, boolean awarded) {
        Progress progress = players.computeIfAbsent(player, ignored -> new Progress());
        if (lootrContainer != null && !progress.lootrContainers.add(lootrContainer)) {
            return;
        }
        progress.failures = awarded ? 0 : Math.min(CatStickPityCurve.GUARANTEE_AT - 1, progress.failures + 1);
        setDirty();
    }

    public static CatStickPitySavedData load(CompoundTag tag) {
        CatStickPitySavedData data = new CatStickPitySavedData();
        for (Tag entry : tag.getList("Players", Tag.TAG_COMPOUND)) {
            CompoundTag player = (CompoundTag) entry;
            if (!player.hasUUID("UUID")) {
                continue;
            }
            Progress progress = new Progress();
            progress.failures = Math.max(0, Math.min(99, player.getInt("Failures")));
            for (Tag visited : player.getList("LootrContainers", Tag.TAG_COMPOUND)) {
                CompoundTag container = (CompoundTag) visited;
                if (container.hasUUID("UUID")) {
                    progress.lootrContainers.add(container.getUUID("UUID"));
                }
            }
            data.players.put(player.getUUID("UUID"), progress);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag playerTags = new ListTag();
        players.forEach((uuid, progress) -> {
            CompoundTag player = new CompoundTag();
            player.putUUID("UUID", uuid);
            player.putInt("Failures", progress.failures);
            ListTag containers = new ListTag();
            progress.lootrContainers.forEach(id -> {
                CompoundTag container = new CompoundTag();
                container.putUUID("UUID", id);
                containers.add(container);
            });
            player.put("LootrContainers", containers);
            playerTags.add(player);
        });
        tag.put("Players", playerTags);
        return tag;
    }

    private static final class Progress {
        private int failures;
        private final Set<UUID> lootrContainers = new HashSet<>();
    }
}
