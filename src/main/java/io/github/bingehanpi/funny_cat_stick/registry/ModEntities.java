package io.github.bingehanpi.funny_cat_stick.registry;

import io.github.bingehanpi.funny_cat_stick.FunnyCatStick;
import io.github.bingehanpi.funny_cat_stick.entity.DswhaleEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, FunnyCatStick.MOD_ID);

    public static final RegistryObject<EntityType<DswhaleEntity>> DSWHALE = ENTITY_TYPES.register(
            "dswhale",
            () -> EntityType.Builder.of(DswhaleEntity::new, MobCategory.CREATURE)
                    .sized(0.75F, 1.95F)
                    .clientTrackingRange(8)
                    .build(FunnyCatStick.MOD_ID + ":dswhale")
    );

    private ModEntities() {
    }

    public static void register(IEventBus modEventBus) {
        ENTITY_TYPES.register(modEventBus);
    }
}
