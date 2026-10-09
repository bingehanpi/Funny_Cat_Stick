package io.github.bingehanpi.funny_cat_stick.block;

import io.github.bingehanpi.funny_cat_stick.blockentity.FunnyCatStickDisplayStandBlockEntity;
import io.github.bingehanpi.funny_cat_stick.registry.ModEnchantments;
import io.github.bingehanpi.funny_cat_stick.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public final class FunnyCatStickDisplayStandBlock extends BaseEntityBlock {

    private static final VoxelShape SHAPE = Shapes.or(
            box(1.5D, 0.0D, 1.5D, 14.5D, 1.65D, 14.5D),
            box(6.45D, 1.45D, 6.45D, 9.55D, 10.85D, 9.55D),
            box(4.0D, 7.75D, 3.5D, 12.0D, 10.65D, 4.55D),
            box(4.0D, 7.75D, 11.45D, 12.0D, 10.65D, 12.5D),
            box(11.45D, 7.75D, 4.4D, 12.5D, 10.65D, 11.6D),
            box(3.5D, 7.75D, 4.4D, 4.55D, 10.65D, 11.6D)
    );

    public FunnyCatStickDisplayStandBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FunnyCatStickDisplayStandBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult
    ) {
        if (!(level.getBlockEntity(pos) instanceof FunnyCatStickDisplayStandBlockEntity stand)) {
            return InteractionResult.PASS;
        }

        ItemStack heldStack = player.getItemInHand(hand);
        boolean canTransform = isRitualCatalyst(heldStack) && stand.hasFourNormallyEnchantedSticks();
        if (canTransform) {
            if (level.isClientSide) {
                return InteractionResult.SUCCESS;
            }

            transformHeldStack(player, hand, heldStack);
            stand.clearAllContents();
            summonLightning((ServerLevel) level, pos);
            level.destroyBlock(pos, false);
            return InteractionResult.CONSUME;
        }

        Direction face = hitResult.getDirection();
        if (!face.getAxis().isHorizontal()) {
            return InteractionResult.PASS;
        }

        ItemStack displayedStack = stand.getItem(face);
        boolean holdingFunnyCatStick = heldStack.is(ModItems.FUNNY_CAT_STICK.get());

        if (level.isClientSide) {
            boolean canTake = heldStack.isEmpty() && !displayedStack.isEmpty();
            return holdingFunnyCatStick || canTake ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }

        if (holdingFunnyCatStick) {
            if (!displayedStack.isEmpty()) {
                return InteractionResult.CONSUME;
            }

            ItemStack insertedStack = heldStack.copy();
            insertedStack.setCount(1);
            if (stand.setItem(face, insertedStack) && !player.getAbilities().instabuild) {
                heldStack.shrink(1);
            }
            return InteractionResult.CONSUME;
        }

        if (heldStack.isEmpty() && !displayedStack.isEmpty()) {
            ItemStack removedStack = stand.removeItem(face);
            player.getInventory().add(removedStack);
            if (!removedStack.isEmpty()) {
                player.drop(removedStack, false);
            }
            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
    }

    private static boolean isRitualCatalyst(ItemStack stack) {
        return stack.is(ModItems.FUNNY_CAT_STICK.get())
                && EnchantmentHelper.getItemEnchantmentLevel(ModEnchantments.IKUN_CATGIRL.get(), stack) > 0;
    }

    private static void transformHeldStack(Player player, InteractionHand hand, ItemStack heldStack) {
        ItemStack remainingStack = heldStack.copy();
        remainingStack.shrink(1);
        player.setItemInHand(hand, new ItemStack(ModItems.FUNNY_QUESTION_STICK.get()));

        if (!remainingStack.isEmpty()) {
            player.getInventory().add(remainingStack);
            if (!remainingStack.isEmpty()) {
                player.drop(remainingStack, false);
            }
        }
    }

    private static void summonLightning(ServerLevel level, BlockPos pos) {
        LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(level);
        if (lightning != null) {
            lightning.moveTo(pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D);
            level.addFreshEntity(lightning);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof FunnyCatStickDisplayStandBlockEntity stand) {
                stand.dropAllContents(level, pos);
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
