package io.github.bingehanpi.funny_cat_stick.blockentity;

import io.github.bingehanpi.funny_cat_stick.registry.ModBlockEntities;
import io.github.bingehanpi.funny_cat_stick.registry.ModEnchantments;
import io.github.bingehanpi.funny_cat_stick.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;

public final class FunnyCatStickDisplayStandBlockEntity extends BlockEntity {

    private static final Direction[] DISPLAY_DIRECTIONS = {
            Direction.NORTH,
            Direction.SOUTH,
            Direction.EAST,
            Direction.WEST
    };

    private final Map<Direction, ItemStack> displayedSticks = new EnumMap<>(Direction.class);

    public FunnyCatStickDisplayStandBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FUNNY_CAT_STICK_DISPLAY_STAND.get(), pos, state);
        clearWithoutUpdate();
    }

    public ItemStack getItem(Direction direction) {
        return isDisplayDirection(direction)
                ? displayedSticks.getOrDefault(direction, ItemStack.EMPTY)
                : ItemStack.EMPTY;
    }

    public boolean setItem(Direction direction, ItemStack stack) {
        if (!isDisplayDirection(direction)
                || stack.isEmpty()
                || !stack.is(ModItems.FUNNY_CAT_STICK.get())
                || !getItem(direction).isEmpty()) {
            return false;
        }

        ItemStack storedStack = stack.copy();
        storedStack.setCount(1);
        displayedSticks.put(direction, storedStack);
        setChangedAndSync();
        return true;
    }

    public ItemStack removeItem(Direction direction) {
        if (!isDisplayDirection(direction)) {
            return ItemStack.EMPTY;
        }

        ItemStack removedStack = displayedSticks.getOrDefault(direction, ItemStack.EMPTY);
        if (removedStack.isEmpty()) {
            return ItemStack.EMPTY;
        }

        displayedSticks.put(direction, ItemStack.EMPTY);
        setChangedAndSync();
        return removedStack;
    }

    public boolean hasFourNormallyEnchantedSticks() {
        for (Direction direction : DISPLAY_DIRECTIONS) {
            ItemStack stack = displayedSticks.getOrDefault(direction, ItemStack.EMPTY);
            if (!stack.is(ModItems.FUNNY_CAT_STICK.get())
                    || ModEnchantments.NORMAL_PERSONALITY_ENCHANTMENTS.stream().noneMatch(enchantment ->
                            EnchantmentHelper.getItemEnchantmentLevel(enchantment.get(), stack) > 0
                    )) {
                return false;
            }
        }
        return true;
    }

    public void clearAllContents() {
        clearWithoutUpdate();
        setChangedAndSync();
    }

    public void dropAllContents(Level level, BlockPos pos) {
        if (level.isClientSide) {
            return;
        }

        for (Direction direction : DISPLAY_DIRECTIONS) {
            ItemStack stack = displayedSticks.getOrDefault(direction, ItemStack.EMPTY);
            if (!stack.isEmpty()) {
                Containers.dropItemStack(
                        level,
                        pos.getX() + 0.5D,
                        pos.getY() + 0.5D,
                        pos.getZ() + 0.5D,
                        stack
                );
                displayedSticks.put(direction, ItemStack.EMPTY);
            }
        }
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        for (Direction direction : DISPLAY_DIRECTIONS) {
            ItemStack stack = displayedSticks.getOrDefault(direction, ItemStack.EMPTY);
            // Keep empty slots in the update tag as well. Minecraft converts a completely empty
            // BlockEntity update tag to null, so omitting every slot would leave the client's last
            // rendered stick in place when the stand becomes empty.
            tag.put(direction.getSerializedName(), stack.save(new CompoundTag()));
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        clearWithoutUpdate();
        for (Direction direction : DISPLAY_DIRECTIONS) {
            String key = direction.getSerializedName();
            if (!tag.contains(key, CompoundTag.TAG_COMPOUND)) {
                continue;
            }

            ItemStack stack = ItemStack.of(tag.getCompound(key));
            if (stack.is(ModItems.FUNNY_CAT_STICK.get())) {
                stack.setCount(1);
                displayedSticks.put(direction, stack);
            }
        }
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private void clearWithoutUpdate() {
        for (Direction direction : DISPLAY_DIRECTIONS) {
            displayedSticks.put(direction, ItemStack.EMPTY);
        }
    }

    private void setChangedAndSync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    private static boolean isDisplayDirection(Direction direction) {
        return direction != null && direction.getAxis().isHorizontal();
    }
}
