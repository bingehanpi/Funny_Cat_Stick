package io.github.bingehanpi.funny_cat_stick.registry;

import io.github.bingehanpi.funny_cat_stick.FunnyCatStick;
import io.github.bingehanpi.funny_cat_stick.item.FunnyCatStickItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, FunnyCatStick.MOD_ID);

    public static final RegistryObject<Item> FUNNY_CAT_STICK = ITEMS.register(
            "funny_cat_stick",
            () -> new FunnyCatStickItem(new Item.Properties().rarity(Rarity.UNCOMMON))
    );

    public static final RegistryObject<Item> DSWHALE_SPAWN_EGG = ITEMS.register(
            "dswhale_spawn_egg",
            () -> new ForgeSpawnEggItem(
                    ModEntities.DSWHALE,
                    0x26395F,
                    0xF6D8CF,
                    new Item.Properties()
            )
    );

    private ModItems() {
    }

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
