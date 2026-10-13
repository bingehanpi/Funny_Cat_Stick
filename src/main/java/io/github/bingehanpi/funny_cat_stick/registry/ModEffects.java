package io.github.bingehanpi.funny_cat_stick.registry;

import io.github.bingehanpi.funny_cat_stick.FunnyCatStick;
import io.github.bingehanpi.funny_cat_stick.effect.CatgirlStasisEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEffects {

    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, FunnyCatStick.MOD_ID);

    public static final RegistryObject<MobEffect> CATGIRL_STASIS = EFFECTS.register(
            "catgirl_stasis",
            CatgirlStasisEffect::new
    );

    private ModEffects() {
    }

    public static void register(IEventBus eventBus) {
        EFFECTS.register(eventBus);
    }
}
