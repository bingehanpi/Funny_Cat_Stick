package io.github.bingehanpi.funny_cat_stick.registry;

import io.github.bingehanpi.funny_cat_stick.FunnyCatStick;
import io.github.bingehanpi.funny_cat_stick.block.FunnyCatStickDisplayStandBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, FunnyCatStick.MOD_ID);

    public static final RegistryObject<Block> FUNNY_CAT_STICK_DISPLAY_STAND = BLOCKS.register(
            "funny_cat_stick_display_stand",
            () -> new FunnyCatStickDisplayStandBlock(
                    Block.Properties.copy(Blocks.OAK_PLANKS).strength(2.0F).noOcclusion()
            )
    );

    private ModBlocks() {
    }

    public static void register(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
    }
}
