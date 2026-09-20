package io.github.bingehanpi.funny_cat_stick.effect;

import io.github.bingehanpi.funny_cat_stick.enchantment.CatPersonalityEnchantment;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import java.util.Map;

public enum CatPersonality {
    CLASSIC("classic", 0),
    TOUGH("tough", 14),
    YANDERE("yandere", 12),
    TSUNDERE("tsundere", 12),
    ENERGETIC("energetic", 12),
    LAZY("lazy", 12),
    SHARP_TONGUED("sharp_tongued", 12),
    TIMID("timid", 12),
    AIRHEADED("airheaded", 12),
    NEKOMATA_DEITY("nekomata_deity", 12),
    IKUN("ikun", 0);

    private final String serializedName;
    private final int transformationCount;

    CatPersonality(String serializedName, int transformationCount) {
        this.serializedName = serializedName;
        this.transformationCount = transformationCount;
    }

    public String getSerializedName() {
        return serializedName;
    }

    public int getTransformationCount() {
        return transformationCount;
    }

    public static CatPersonality fromSerializedName(String serializedName) {
        for (CatPersonality personality : values()) {
            if (personality.serializedName.equals(serializedName)) {
                return personality;
            }
        }
        return CLASSIC;
    }

    public static CatPersonality fromItemStack(ItemStack stack) {
        for (Map.Entry<Enchantment, Integer> entry : EnchantmentHelper.getEnchantments(stack).entrySet()) {
            if (entry.getValue() > 0 && entry.getKey() instanceof CatPersonalityEnchantment personalityEnchantment) {
                return personalityEnchantment.getPersonality();
            }
        }
        return CLASSIC;
    }
}
