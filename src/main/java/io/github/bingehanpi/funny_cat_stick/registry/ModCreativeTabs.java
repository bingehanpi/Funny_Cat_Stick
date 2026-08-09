package io.github.bingehanpi.funny_cat_stick.registry;

import io.github.bingehanpi.funny_cat_stick.FunnyCatStick;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
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
                    .displayItems((parameters, output) -> output.accept(ModItems.FUNNY_CAT_STICK.get()))
                    .build()
    );

    private ModCreativeTabs() {
    }

    public static void register(IEventBus modEventBus) {
        CREATIVE_MODE_TABS.register(modEventBus);
    }
}
