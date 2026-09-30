package io.github.bingehanpi.funny_cat_stick.registry;

import io.github.bingehanpi.funny_cat_stick.FunnyCatStick;
import io.github.bingehanpi.funny_cat_stick.item.FunnyCatStickItem;
import io.github.bingehanpi.funny_cat_stick.item.PotFoodItem;
import net.minecraft.world.food.FoodProperties;
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

    public static final RegistryObject<Item> BIG_WHITE_RICE = ITEMS.register(
            "big_white_rice",
            () -> new Item(new Item.Properties()
                    .stacksTo(64)
                    .food(food(2, 2.0F)))
    );

    public static final RegistryObject<Item> IRON_POT = ITEMS.register(
            "iron_pot",
            () -> new Item(new Item.Properties().stacksTo(64))
    );

    public static final RegistryObject<Item> POT_OF_WHITE_RICE = ITEMS.register(
            "pot_of_white_rice",
            () -> new PotFoodItem(
                    new Item.Properties()
                            .stacksTo(64)
                            .food(food(5, 3.0F)),
                    IRON_POT
            )
    );

    public static final RegistryObject<Item> FRAGRANT_BIG_WHITE_RICE = ITEMS.register(
            "fragrant_big_white_rice",
            () -> new Item(new Item.Properties()
                    .stacksTo(64)
                    .food(food(4, 4.0F)))
    );

    public static final RegistryObject<Item> POT_OF_FRAGRANT_RICE = ITEMS.register(
            "pot_of_fragrant_rice",
            () -> new PotFoodItem(
                    new Item.Properties()
                            .stacksTo(64)
                            .food(food(7, 7.0F)),
                    IRON_POT
            )
    );

    private ModItems() {
    }

    private static FoodProperties food(int nutrition, float saturation) {
        return new FoodProperties.Builder()
                .nutrition(nutrition)
                .saturationMod(saturation / (nutrition * 2.0F))
                .build();
    }

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
