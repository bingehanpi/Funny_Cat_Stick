package io.github.bingehanpi.funny_cat_stick.entity.trade;

import io.github.bingehanpi.funny_cat_stick.entity.DswhaleEntity;
import io.github.bingehanpi.funny_cat_stick.registry.ModEnchantments;
import io.github.bingehanpi.funny_cat_stick.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.StructureTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

public final class DswhaleTradeOffers {

    public static final int SECOND_STAGE_TRADE_COUNT = 10;

    private static final int COMMON_MAX_USES = 12;
    private static final int MAP_MAX_USES = 6;
    private static final int GRASS_BLOCK_MAX_USES = 8;
    private static final int TREASURE_MAX_USES = 1;
    private static final int FIRST_STAGE_XP = 1;
    private static final int SECOND_STAGE_XP = 0;
    private static final int EXPLORER_MAP_SEARCH_RADIUS = 100;

    private static final List<MineralTrade> MINERAL_TRADES = List.of(
            mineral(Items.COAL_ORE, Items.COAL),
            mineral(Items.DEEPSLATE_COAL_ORE, Items.COAL),
            mineral(Items.COPPER_ORE, Items.COPPER_INGOT),
            mineral(Items.DEEPSLATE_COPPER_ORE, Items.COPPER_INGOT),
            mineral(Items.RAW_COPPER, Items.COPPER_INGOT),
            mineral(Items.IRON_ORE, Items.IRON_INGOT),
            mineral(Items.DEEPSLATE_IRON_ORE, Items.IRON_INGOT),
            mineral(Items.RAW_IRON, Items.IRON_INGOT),
            mineral(Items.GOLD_ORE, Items.GOLD_INGOT),
            mineral(Items.DEEPSLATE_GOLD_ORE, Items.GOLD_INGOT),
            mineral(Items.RAW_GOLD, Items.GOLD_INGOT),
            mineral(Items.REDSTONE_ORE, Items.REDSTONE),
            mineral(Items.DEEPSLATE_REDSTONE_ORE, Items.REDSTONE),
            mineral(Items.LAPIS_ORE, Items.LAPIS_LAZULI),
            mineral(Items.DEEPSLATE_LAPIS_ORE, Items.LAPIS_LAZULI),
            mineral(Items.DIAMOND_ORE, Items.DIAMOND),
            mineral(Items.DEEPSLATE_DIAMOND_ORE, Items.DIAMOND),
            mineral(Items.EMERALD_ORE, Items.EMERALD),
            mineral(Items.DEEPSLATE_EMERALD_ORE, Items.EMERALD),
            mineral(Items.NETHER_QUARTZ_ORE, Items.QUARTZ)
    );

    private DswhaleTradeOffers() {
    }

    public static void addFirstStageOffers(
            DswhaleEntity dswhale,
            MerchantOffers offers,
            RandomSource random
    ) {
        MineralTrade mineralTrade = MINERAL_TRADES.get(random.nextInt(MINERAL_TRADES.size()));
        offers.add(new MerchantOffer(
                new ItemStack(ModItems.BIG_WHITE_RICE.get(), between(random, 10, 20)),
                new ItemStack(mineralTrade.input()),
                new ItemStack(mineralTrade.output(), 5),
                COMMON_MAX_USES,
                FIRST_STAGE_XP,
                0.0F
        ));

        offers.add(createNormalEnchantedBookOffer(
                ModItems.BIG_WHITE_RICE.get(),
                between(random, 15, 30),
                COMMON_MAX_USES,
                FIRST_STAGE_XP,
                random
        ));

        MerchantOffer explorerMapOffer = createExplorerMapOffer(dswhale, random);
        if (explorerMapOffer != null) {
            offers.add(explorerMapOffer);
        }
    }

    public static void addSecondStageOffers(MerchantOffers offers, RandomSource random) {
        offers.add(createNormalEnchantedBookOffer(
                ModItems.FRAGRANT_BIG_WHITE_RICE.get(),
                between(random, 10, 20),
                COMMON_MAX_USES,
                SECOND_STAGE_XP,
                random
        ));
        offers.add(new MerchantOffer(
                new ItemStack(ModItems.FRAGRANT_BIG_WHITE_RICE.get(), between(random, 20, 50)),
                new ItemStack(Items.GRASS_BLOCK),
                GRASS_BLOCK_MAX_USES,
                SECOND_STAGE_XP,
                0.0F
        ));
        offers.add(new MerchantOffer(
                new ItemStack(ModItems.FRAGRANT_BIG_WHITE_RICE.get(), between(random, 10, 25)),
                new ItemStack(Items.LIGHT_BLUE_WOOL, 16),
                COMMON_MAX_USES,
                SECOND_STAGE_XP,
                0.0F
        ));
    }

    public static void addTreasureOffer(MerchantOffers offers) {
        ItemStack book = EnchantedBookItem.createForEnchantment(
                new EnchantmentInstance(ModEnchantments.IKUN_CATGIRL.get(), 1)
        );
        offers.add(new MerchantOffer(
                new ItemStack(ModItems.FRAGRANT_BIG_WHITE_RICE.get(), 64),
                book,
                TREASURE_MAX_USES,
                SECOND_STAGE_XP,
                0.0F
        ));
    }

    public static void addDisplayStandOffer(MerchantOffers offers, RandomSource random) {
        offers.add(new MerchantOffer(
                new ItemStack(ModItems.FRAGRANT_BIG_WHITE_RICE.get(), between(random, 10, 20)),
                new ItemStack(ModItems.IRON_POT.get()),
                new ItemStack(ModItems.FUNNY_CAT_STICK_DISPLAY_STAND.get()),
                TREASURE_MAX_USES,
                SECOND_STAGE_XP,
                0.0F
        ));
    }

    static List<MineralTrade> mineralTrades() {
        return MINERAL_TRADES;
    }

    private static MerchantOffer createNormalEnchantedBookOffer(
            Item currency,
            int cost,
            int maxUses,
            int xp,
            RandomSource random
    ) {
        List<RegistryObject<Enchantment>> enchantments = ModEnchantments.NORMAL_PERSONALITY_ENCHANTMENTS;
        Enchantment enchantment = enchantments.get(random.nextInt(enchantments.size())).get();
        ItemStack book = EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchantment, 1));
        return new MerchantOffer(new ItemStack(currency, cost), book, maxUses, xp, 0.0F);
    }

    private static MerchantOffer createExplorerMapOffer(DswhaleEntity dswhale, RandomSource random) {
        if (!(dswhale.level() instanceof ServerLevel serverLevel)) {
            return null;
        }

        boolean woodlandFirst = random.nextBoolean();
        ItemStack map = woodlandFirst
                ? createWoodlandMap(serverLevel, dswhale.blockPosition())
                : createOceanMap(serverLevel, dswhale.blockPosition());
        if (map.isEmpty()) {
            map = woodlandFirst
                    ? createOceanMap(serverLevel, dswhale.blockPosition())
                    : createWoodlandMap(serverLevel, dswhale.blockPosition());
        }
        if (map.isEmpty()) {
            return null;
        }

        return new MerchantOffer(
                new ItemStack(ModItems.BIG_WHITE_RICE.get(), between(random, 20, 40)),
                map,
                MAP_MAX_USES,
                FIRST_STAGE_XP,
                0.0F
        );
    }

    private static ItemStack createWoodlandMap(ServerLevel level, BlockPos origin) {
        BlockPos target = level.findNearestMapStructure(
                StructureTags.ON_WOODLAND_EXPLORER_MAPS,
                origin,
                EXPLORER_MAP_SEARCH_RADIUS,
                true
        );
        return createExplorerMap(
                level,
                target,
                MapDecoration.Type.MANSION,
                "filled_map.mansion"
        );
    }

    private static ItemStack createOceanMap(ServerLevel level, BlockPos origin) {
        BlockPos target = level.findNearestMapStructure(
                StructureTags.ON_OCEAN_EXPLORER_MAPS,
                origin,
                EXPLORER_MAP_SEARCH_RADIUS,
                true
        );
        return createExplorerMap(
                level,
                target,
                MapDecoration.Type.MONUMENT,
                "filled_map.monument"
        );
    }

    private static ItemStack createExplorerMap(
            ServerLevel level,
            BlockPos target,
            MapDecoration.Type decoration,
            String displayName
    ) {
        if (target == null) {
            return ItemStack.EMPTY;
        }

        ItemStack map = MapItem.create(level, target.getX(), target.getZ(), (byte) 2, true, true);
        MapItem.renderBiomePreviewMap(level, map);
        MapItemSavedData.addTargetDecoration(map, target, "+", decoration);
        map.setHoverName(Component.translatable(displayName));
        return map;
    }

    private static int between(RandomSource random, int minimum, int maximum) {
        return Mth.nextInt(random, minimum, maximum);
    }

    private static MineralTrade mineral(Item input, Item output) {
        return new MineralTrade(input, output);
    }

    record MineralTrade(Item input, Item output) {
    }
}
