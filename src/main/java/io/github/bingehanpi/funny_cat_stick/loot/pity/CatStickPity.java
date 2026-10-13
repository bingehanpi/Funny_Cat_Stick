package io.github.bingehanpi.funny_cat_stick.loot.pity;

import io.github.bingehanpi.funny_cat_stick.registry.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.common.util.FakePlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public final class CatStickPity {
    private static final ThreadLocal<Opening> OPENING = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> PITY_FILL = new ThreadLocal<>();

    private CatStickPity() {
    }

    public static boolean suppressesBaseRoll() {
        return Boolean.TRUE.equals(PITY_FILL.get());
    }

    public static <T> T generateForFill(Container container, LootParams params, Supplier<T> generator) {
        Opening opening = OPENING.get();
        if (!(params.getParamOrNull(LootContextParams.THIS_ENTITY) instanceof ServerPlayer player)
                || player instanceof FakePlayer || player.isSpectator()
                || !((opening != null && opening.player == player)
                || (LootrPityBridge.isLootrInventory(container)
                && LootrPityBridge.containerId(container, player.level()) != null))) {
            return generator.get();
        }
        Boolean previous = PITY_FILL.get();
        PITY_FILL.set(true);
        try {
            return generator.get();
        } finally {
            if (previous == null) {
                PITY_FILL.remove();
            } else {
                PITY_FILL.set(previous);
            }
        }
    }

    /** One successful menu creation is one roll, including both halves of a vanilla double chest. */
    public static AbstractContainerMenu createMenu(ServerPlayer player, Supplier<AbstractContainerMenu> factory) {
        Opening previous = OPENING.get();
        Opening opening = new Opening(player);
        OPENING.set(opening);
        try {
            AbstractContainerMenu menu = factory.get();
            if (menu != null && !opening.containers.isEmpty()) {
                award(player, opening.containers, null);
            }
            return menu;
        } finally {
            if (previous == null) {
                OPENING.remove();
            } else {
                OPENING.set(previous);
            }
        }
    }

    /** Called only after the actual loot-table fill, never for a mere loot-list preview or /loot roll. */
    public static void onFilled(Container container, LootParams params, ResourceLocation table) {
        if (table == null || !table.getPath().startsWith("chests/")
                || !(params.getParamOrNull(LootContextParams.THIS_ENTITY) instanceof ServerPlayer player)
                || player instanceof FakePlayer || player.isSpectator()) {
            return;
        }
        if (LootrPityBridge.isLootrInventory(container)) {
            UUID id = LootrPityBridge.containerId(container, player.level());
            if (id != null) {
                CatStickPitySavedData data = CatStickPitySavedData.get(player.server);
                if (data.hasVisitedLootr(player.getUUID(), id)) {
                    // A refreshed personal inventory retains the base drop chance, but cannot farm pity.
                    if (!containsStick(List.of(container)) && player.getRandom().nextDouble() < CatStickPityCurve.BASE_CHANCE) {
                        deliver(player, List.of(container));
                    }
                } else {
                    award(player, List.of(container), id);
                }
            }
            return;
        }
        Opening opening = OPENING.get();
        if (opening != null && opening.player == player && !opening.containers.contains(container)) {
            opening.containers.add(container);
        }
    }

    private static void award(ServerPlayer player, List<Container> containers, UUID lootrId) {
        CatStickPitySavedData data = CatStickPitySavedData.get(player.server);
        int attempt = data.failures(player.getUUID()) + 1;
        boolean awarded = containsStick(containers);
        if (!awarded && (attempt >= CatStickPityCurve.GUARANTEE_AT
                || player.getRandom().nextDouble() < CatStickPityCurve.chance(attempt))) {
            awarded = deliver(player, containers);
        }
        data.record(player.getUUID(), lootrId, awarded);
    }

    private static boolean containsStick(List<Container> containers) {
        for (Container container : containers) {
            for (int slot = 0; slot < container.getContainerSize(); slot++) {
                if (container.getItem(slot).is(ModItems.FUNNY_CAT_STICK.get())) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean deliver(ServerPlayer player, List<Container> containers) {
        ItemStack reward = new ItemStack(ModItems.FUNNY_CAT_STICK.get());
        for (Container container : containers) {
            for (int slot = 0; slot < container.getContainerSize(); slot++) {
                if (container.getItem(slot).isEmpty()) {
                    container.setItem(slot, reward);
                    container.setChanged();
                    return true;
                }
            }
        }
        if (player.getInventory().add(reward)) {
            return true;
        }
        return player.drop(reward, false) != null;
    }

    private static final class Opening {
        private final ServerPlayer player;
        private final List<Container> containers = new ArrayList<>();

        private Opening(ServerPlayer player) {
            this.player = player;
        }
    }
}
