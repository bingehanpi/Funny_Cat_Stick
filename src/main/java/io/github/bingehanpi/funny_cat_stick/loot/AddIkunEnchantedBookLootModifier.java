package io.github.bingehanpi.funny_cat_stick.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.bingehanpi.funny_cat_stick.registry.ModEnchantments;
import io.github.bingehanpi.funny_cat_stick.registry.ModLootModifiers;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

public final class AddIkunEnchantedBookLootModifier extends LootModifier {

    public static final Codec<AddIkunEnchantedBookLootModifier> CODEC = RecordCodecBuilder.create(
            instance -> codecStart(instance)
                    .and(Codec.floatRange(0.0F, 1.0F).fieldOf("chance").forGetter(modifier -> modifier.chance))
                    .apply(instance, AddIkunEnchantedBookLootModifier::new)
    );

    private final float chance;

    public AddIkunEnchantedBookLootModifier(
            LootItemCondition[] conditions,
            float chance
    ) {
        super(conditions);
        this.chance = chance;
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(
            ObjectArrayList<ItemStack> generatedLoot,
            LootContext context
    ) {
        if (!BuiltInLootTables.FISHING.equals(context.getQueriedLootTableId())) {
            return generatedLoot;
        }

        if (!(context.getParamOrNull(LootContextParams.THIS_ENTITY) instanceof FishingHook fishingHook)
                || !fishingHook.isOpenWaterFishing()) {
            return generatedLoot;
        }

        if (context.getRandom().nextFloat() < chance) {
            generatedLoot.add(EnchantedBookItem.createForEnchantment(
                    new EnchantmentInstance(ModEnchantments.IKUN_CATGIRL.get(), 1)
            ));
        }
        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return ModLootModifiers.ADD_IKUN_ENCHANTED_BOOK.get();
    }
}
