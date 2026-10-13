package io.github.bingehanpi.funny_cat_stick.loot.pity;

import com.mojang.logging.LogUtils;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import java.lang.reflect.Method;
import java.util.UUID;

/** Uses Lootr's public inventory accessors without loading or linking any optional mod classes. */
public final class LootrPityBridge {
    private static final ClassValue<Access> ACCESS = new ClassValue<>() {
        @Override
        protected Access computeValue(Class<?> type) {
            for (Class<?> parent = type; parent != null; parent = parent.getSuperclass()) {
                if (parent.getName().equals("noobanidus.mods.lootr.data.SpecialChestInventory")) {
                    try {
                        return new Access(true, type.getMethod("getTileId"), type.getMethod("getEntity", Level.class));
                    } catch (ReflectiveOperationException exception) {
                        LogUtils.getLogger().warn("Lootr inventory accessors changed; skipping pity for {}", type.getName(), exception);
                        return new Access(true, null, null);
                    }
                }
            }
            return new Access(false, null, null);
        }
    };

    private LootrPityBridge() {
    }

    public static boolean isLootrInventory(Container container) {
        return ACCESS.get(container.getClass()).lootr();
    }

    public static UUID containerId(Container container, Level level) {
        Access access = ACCESS.get(container.getClass());
        if (access.tileId() == null) {
            return null;
        }
        try {
            Object id = access.tileId().invoke(container);
            if (id instanceof UUID uuid) {
                return uuid;
            }
            Object entity = access.entity().invoke(container, level);
            return entity instanceof Entity cart ? cart.getUUID() : null;
        } catch (ReflectiveOperationException exception) {
            LogUtils.getLogger().warn("Unable to identify Lootr inventory for pity", exception);
            return null;
        }
    }

    private record Access(boolean lootr, Method tileId, Method entity) {
    }
}
