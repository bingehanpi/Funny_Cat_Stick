package io.github.bingehanpi.funny_cat_stick.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Objects;
import java.util.function.Supplier;

public final class PotFoodItem extends Item {

    private final Supplier<? extends Item> emptyPotSupplier;

    public PotFoodItem(Properties properties, Supplier<? extends Item> emptyPotSupplier) {
        super(properties);
        this.emptyPotSupplier = Objects.requireNonNull(emptyPotSupplier);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        ItemStack remainingFood = super.finishUsingItem(stack, level, livingEntity);
        if (livingEntity instanceof Player player && player.getAbilities().instabuild) {
            return remainingFood;
        }

        ItemStack emptyPot = new ItemStack(emptyPotSupplier.get());
        if (remainingFood.isEmpty()) {
            return emptyPot;
        }

        if (!level.isClientSide) {
            if (livingEntity instanceof Player player) {
                if (!player.getInventory().add(emptyPot)) {
                    player.drop(emptyPot, false);
                }
            } else {
                livingEntity.spawnAtLocation(emptyPot);
            }
        }
        return remainingFood;
    }
}
