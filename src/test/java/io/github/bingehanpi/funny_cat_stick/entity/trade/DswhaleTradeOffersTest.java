package io.github.bingehanpi.funny_cat_stick.entity.trade;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DswhaleTradeOffersTest {

    @BeforeAll
    static void bootstrapMinecraft() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void mineralPoolOnlyContainsVanillaItems() {
        assertTrue(DswhaleTradeOffers.mineralTrades().stream().allMatch(trade ->
                "minecraft".equals(BuiltInRegistries.ITEM.getKey(trade.input()).getNamespace())
                        && "minecraft".equals(BuiltInRegistries.ITEM.getKey(trade.output()).getNamespace())
        ));
    }

    @Test
    void mineralPoolExcludesNetherGoldOreAndAncientDebris() {
        assertFalse(hasInput(Items.NETHER_GOLD_ORE));
        assertFalse(hasInput(Items.ANCIENT_DEBRIS));
    }

    @Test
    void rawMineralsProduceTheirMatchingIngots() {
        Map<Item, Item> mappings = DswhaleTradeOffers.mineralTrades().stream().collect(Collectors.toMap(
                DswhaleTradeOffers.MineralTrade::input,
                DswhaleTradeOffers.MineralTrade::output
        ));

        assertEquals(Items.COPPER_INGOT, mappings.get(Items.RAW_COPPER));
        assertEquals(Items.IRON_INGOT, mappings.get(Items.RAW_IRON));
        assertEquals(Items.GOLD_INGOT, mappings.get(Items.RAW_GOLD));
    }

    @Test
    void secondStageUnlocksAfterTenFirstStageTrades() {
        assertEquals(10, DswhaleTradeOffers.SECOND_STAGE_TRADE_COUNT);
    }

    private static boolean hasInput(Item item) {
        return DswhaleTradeOffers.mineralTrades().stream().anyMatch(trade -> trade.input() == item);
    }
}
