package io.github.bingehanpi.funny_cat_stick.enchantment;

import io.github.bingehanpi.funny_cat_stick.effect.CatPersonality;
import io.github.bingehanpi.funny_cat_stick.registry.ModItems;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public final class CatPersonalityEnchantment extends Enchantment {

    private static final int MIN_ENCHANTING_COST = 1;
    private static final int MAX_ENCHANTING_COST = 60;

    private static final EnchantmentCategory FUNNY_CAT_STICK_CATEGORY = EnchantmentCategory.create(
            "FUNNY_CAT_STICK_PERSONALITY",
            item -> item == ModItems.FUNNY_CAT_STICK.get()
    );

    private final CatPersonality personality;
    private final CatEnchantmentType type;

    public CatPersonalityEnchantment(CatPersonality personality, CatEnchantmentType type) {
        super(Rarity.RARE, FUNNY_CAT_STICK_CATEGORY, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
        this.personality = personality;
        this.type = type;
    }

    public CatPersonality getPersonality() {
        return personality;
    }

    public CatEnchantmentType getType() {
        return type;
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public int getMinCost(int level) {
        return MIN_ENCHANTING_COST;
    }

    @Override
    public int getMaxCost(int level) {
        return MAX_ENCHANTING_COST;
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return stack.is(ModItems.FUNNY_CAT_STICK.get());
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return type == CatEnchantmentType.NORMAL && stack.is(ModItems.FUNNY_CAT_STICK.get());
    }

    @Override
    public boolean isDiscoverable() {
        return type == CatEnchantmentType.NORMAL;
    }

    @Override
    public boolean isTradeable() {
        return type == CatEnchantmentType.NORMAL;
    }

    @Override
    public boolean isTreasureOnly() {
        return type == CatEnchantmentType.TREASURE;
    }

    @Override
    protected boolean checkCompatibility(Enchantment other) {
        return !(other instanceof CatPersonalityEnchantment) && super.checkCompatibility(other);
    }
}
