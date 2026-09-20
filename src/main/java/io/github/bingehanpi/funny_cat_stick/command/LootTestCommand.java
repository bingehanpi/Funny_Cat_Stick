package io.github.bingehanpi.funny_cat_stick.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import io.github.bingehanpi.funny_cat_stick.registry.ModEnchantments;
import io.github.bingehanpi.funny_cat_stick.registry.ModItems;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootDataId;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.fml.loading.FMLEnvironment;

import java.util.List;
import java.util.Locale;

/**
 * Development-only command that exercises loaded loot tables through their normal Forge loot path.
 */
public final class LootTestCommand {

    private static final int MAX_ITERATIONS = 100_000;
    private static final DynamicCommandExceptionType UNKNOWN_LOOT_TABLE = new DynamicCommandExceptionType(
            id -> Component.literal("Unknown loot table: " + id)
    );

    private LootTestCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        if (FMLEnvironment.production) {
            return;
        }

        dispatcher.register(Commands.literal("fcs_testloot")
                .requires(source -> source.hasPermission(4))
                .then(Commands.argument("loot_table", ResourceLocationArgument.id())
                        .suggests((context, builder) -> SharedSuggestionProvider.suggestResource(
                                context.getSource().getServer().getLootData().getKeys(LootDataType.TABLE),
                                builder
                        ))
                        .then(Commands.argument("iterations", IntegerArgumentType.integer(1, MAX_ITERATIONS))
                                .executes(context -> runTest(
                                        context.getSource(),
                                        ResourceLocationArgument.getId(context, "loot_table"),
                                        IntegerArgumentType.getInteger(context, "iterations")
                                )))));
    }

    private static int runTest(CommandSourceStack source, ResourceLocation lootTableId, int iterations)
            throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        LootTable lootTable = source.getServer().getLootData().getElement(
                new LootDataId<>(LootDataType.TABLE, lootTableId)
        );
        if (lootTable == null) {
            throw UNKNOWN_LOOT_TABLE.create(lootTableId);
        }

        boolean fishingLootTable = BuiltInLootTables.FISHING.equals(lootTableId);
        LootParams.Builder lootParamsBuilder = new LootParams.Builder(source.getLevel())
                .withParameter(LootContextParams.ORIGIN, source.getPosition());
        LootParams lootParams;
        if (fishingLootTable) {
            FishingHook fishingHook = new FishingHook(EntityType.FISHING_BOBBER, source.getLevel());
            lootParams = lootParamsBuilder
                    .withParameter(LootContextParams.TOOL, new ItemStack(Items.FISHING_ROD))
                    .withParameter(LootContextParams.THIS_ENTITY, fishingHook)
                    .create(LootContextParamSets.FISHING);
        } else {
            lootParams = lootParamsBuilder.create(LootContextParamSets.CHEST);
        }

        int runsWithFunnyCatStick = 0;
        long totalFunnyCatSticks = 0;
        int runsWithIkunBook = 0;
        long totalIkunBooks = 0;
        for (int iteration = 0; iteration < iterations; iteration++) {
            List<ItemStack> generatedLoot = lootTable.getRandomItems(lootParams);
            long generatedInThisRun = generatedLoot.stream()
                    .filter(stack -> stack.is(ModItems.FUNNY_CAT_STICK.get()))
                    .mapToLong(ItemStack::getCount)
                    .sum();
            if (generatedInThisRun > 0) {
                runsWithFunnyCatStick++;
                totalFunnyCatSticks += generatedInThisRun;
            }

            long ikunBooksInThisRun = generatedLoot.stream()
                    .filter(stack -> EnchantmentHelper.getEnchantments(stack)
                            .containsKey(ModEnchantments.IKUN_CATGIRL.get()))
                    .mapToLong(ItemStack::getCount)
                    .sum();
            if (ikunBooksInThisRun > 0) {
                runsWithIkunBook++;
                totalIkunBooks += ikunBooksInThisRun;
            }
        }

        double observedPercentage = runsWithFunnyCatStick * 100.0D / iterations;
        double observedIkunBookPercentage = runsWithIkunBook * 100.0D / iterations;
        boolean eligibleForInjection = lootTableId.getPath().startsWith("chests/");
        String report = String.format(
                Locale.ROOT,
                "Loot table: %s\nIterations: %d\nRuns with Funny Cat Stick: %d\nTotal Funny Cat Stick items: %d\nObserved stick generation: %.4f%%\nEligible for current chest injection: %s\nRuns with IKUN book: %d\nTotal IKUN books: %d\nObserved IKUN book generation: %.4f%%\nEligible for IKUN fishing injection: %s",
                lootTableId,
                iterations,
                runsWithFunnyCatStick,
                totalFunnyCatSticks,
                observedPercentage,
                eligibleForInjection,
                runsWithIkunBook,
                totalIkunBooks,
                observedIkunBookPercentage,
                fishingLootTable
        );
        source.sendSuccess(() -> Component.literal(report), false);
        return fishingLootTable ? runsWithIkunBook : runsWithFunnyCatStick;
    }
}
