package io.github.bingehanpi.funny_cat_stick.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.bingehanpi.funny_cat_stick.registry.ModItems;
import io.github.bingehanpi.funny_cat_stick.registry.ModLootModifiers;
import io.github.bingehanpi.funny_cat_stick.loot.pity.CatStickPity;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

public final class AddFunnyCatStickLootModifier extends LootModifier {

    public static final Codec<AddFunnyCatStickLootModifier> CODEC = RecordCodecBuilder.create(
            instance -> codecStart(instance).apply(instance, AddFunnyCatStickLootModifier::new)
    );
    private static final float GENERATION_CHANCE = 0.006F;

    public AddFunnyCatStickLootModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(
            ObjectArrayList<ItemStack> generatedLoot,
            LootContext context
    ) {
        ResourceLocation lootTableId = context.getQueriedLootTableId();
        if (!lootTableId.getPath().startsWith("chests/")
                // Player-owned chest drops are awarded after fill, once per opening, with slot-safe pity.
                || CatStickPity.suppressesBaseRoll()
                || generatedLoot.stream().anyMatch(stack -> stack.is(ModItems.FUNNY_CAT_STICK.get()))) {
            return generatedLoot;
        }

        if (context.getRandom().nextFloat() < GENERATION_CHANCE) {
            generatedLoot.add(new ItemStack(ModItems.FUNNY_CAT_STICK.get()));
        }
        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return ModLootModifiers.ADD_FUNNY_CAT_STICK.get();
    }
}
