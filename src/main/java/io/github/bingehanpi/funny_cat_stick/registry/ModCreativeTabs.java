package io.github.bingehanpi.funny_cat_stick.registry;

import io.github.bingehanpi.funny_cat_stick.FunnyCatStick;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public final class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, FunnyCatStick.MOD_ID);

    public static final RegistryObject<CreativeModeTab> FUNNY_CAT_STICK_TAB = CREATIVE_MODE_TABS.register(
            "funny_cat_stick",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("creativetab.funny_cat_stick.funny_cat_stick"))
                    .icon(() -> ModItems.FUNNY_CAT_STICK.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.FUNNY_CAT_STICK.get());
                        output.accept(ModItems.FUNNY_CAT_STICK_DISPLAY_STAND.get());
                        output.accept(ModItems.ANTI_MEOW_POTION.get());
                        output.accept(ModItems.UNFINISHED_ANTI_MEOW_POTION.get());
                        output.accept(ModItems.DSWHALE_SPAWN_EGG.get());
                        output.accept(ModItems.BIG_WHITE_RICE.get());
                        output.accept(ModItems.POT_OF_WHITE_RICE.get());
                        output.accept(ModItems.IRON_POT.get());
                        output.accept(ModItems.FRAGRANT_BIG_WHITE_RICE.get());
                        output.accept(ModItems.POT_OF_FRAGRANT_RICE.get());
                        for (RegistryObject<net.minecraft.world.item.enchantment.Enchantment> enchantment
                                : ModEnchantments.PERSONALITY_ENCHANTMENTS) {
                            output.accept(EnchantedBookItem.createForEnchantment(
                                    new EnchantmentInstance(enchantment.get(), 1)
                            ));
                        }
                    })
                    .build()
    );

    private ModCreativeTabs() {
    }

    public static void register(IEventBus modEventBus) {
        CREATIVE_MODE_TABS.register(modEventBus);
    }
}
