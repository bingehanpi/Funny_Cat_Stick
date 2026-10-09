package io.github.bingehanpi.funny_cat_stick.registry;

import io.github.bingehanpi.funny_cat_stick.FunnyCatStick;
import io.github.bingehanpi.funny_cat_stick.blockentity.FunnyCatStickDisplayStandBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, FunnyCatStick.MOD_ID);

    public static final RegistryObject<BlockEntityType<FunnyCatStickDisplayStandBlockEntity>>
            FUNNY_CAT_STICK_DISPLAY_STAND = BLOCK_ENTITY_TYPES.register(
                    "funny_cat_stick_display_stand",
                    () -> BlockEntityType.Builder.of(
                            FunnyCatStickDisplayStandBlockEntity::new,
                            ModBlocks.FUNNY_CAT_STICK_DISPLAY_STAND.get()
                    ).build(null)
            );

    private ModBlockEntities() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCK_ENTITY_TYPES.register(modEventBus);
    }
}
