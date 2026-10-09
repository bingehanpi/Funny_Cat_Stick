package io.github.bingehanpi.funny_cat_stick.registry;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.brewing.BrewingRecipeRegistry;
import net.minecraftforge.common.brewing.IBrewingRecipe;
import org.jetbrains.annotations.NotNull;

public final class ModBrewingRecipes {

    private ModBrewingRecipes() {
    }

    public static void register() {
        BrewingRecipeRegistry.addRecipe(new MundanePotionRecipe());
        BrewingRecipeRegistry.addRecipe(
                Ingredient.of(ModItems.UNFINISHED_ANTI_MEOW_POTION.get()),
                Ingredient.of(ModItems.FRAGRANT_BIG_WHITE_RICE.get()),
                new ItemStack(ModItems.ANTI_MEOW_POTION.get())
        );
    }

    private static final class MundanePotionRecipe implements IBrewingRecipe {

        @Override
        public boolean isInput(@NotNull ItemStack stack) {
            return stack.is(Items.POTION) && PotionUtils.getPotion(stack) == Potions.MUNDANE;
        }

        @Override
        public boolean isIngredient(@NotNull ItemStack stack) {
            return stack.is(ModItems.BIG_WHITE_RICE.get());
        }

        @Override
        public ItemStack getOutput(ItemStack input, ItemStack ingredient) {
            if (!isInput(input) || !isIngredient(ingredient)) {
                return ItemStack.EMPTY;
            }
            return new ItemStack(ModItems.UNFINISHED_ANTI_MEOW_POTION.get());
        }
    }
}
