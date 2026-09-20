package io.github.bingehanpi.funny_cat_stick.registry;

import com.mojang.serialization.Codec;
import io.github.bingehanpi.funny_cat_stick.FunnyCatStick;
import io.github.bingehanpi.funny_cat_stick.loot.AddFunnyCatStickLootModifier;
import io.github.bingehanpi.funny_cat_stick.loot.AddIkunEnchantedBookLootModifier;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModLootModifiers {

    private static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIERS =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, FunnyCatStick.MOD_ID);

    public static final RegistryObject<Codec<AddFunnyCatStickLootModifier>> ADD_FUNNY_CAT_STICK =
            LOOT_MODIFIERS.register("add_funny_cat_stick", () -> AddFunnyCatStickLootModifier.CODEC);
    public static final RegistryObject<Codec<AddIkunEnchantedBookLootModifier>> ADD_IKUN_ENCHANTED_BOOK =
            LOOT_MODIFIERS.register("add_ikun_enchanted_book", () -> AddIkunEnchantedBookLootModifier.CODEC);

    private ModLootModifiers() {
    }

    public static void register(IEventBus modEventBus) {
        LOOT_MODIFIERS.register(modEventBus);
    }
}
