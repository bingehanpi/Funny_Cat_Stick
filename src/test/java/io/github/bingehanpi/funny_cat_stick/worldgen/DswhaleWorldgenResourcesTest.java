package io.github.bingehanpi.funny_cat_stick.worldgen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class DswhaleWorldgenResourcesTest {

    @Test
    void keepsVillageAndWorldgenChancesAtTenPercent() {
        assertEquals(0.10D, DswhaleVillageDiscovery.CANDIDATE_CHANCE);

        JsonObject placement = resource(
                "/data/funny_cat_stick/worldgen/structure_set/dswhale_residences.json"
        ).getAsJsonObject("placement");
        assertEquals("minecraft:random_spread", placement.get("type").getAsString());
        assertEquals(0.10F, placement.get("frequency").getAsFloat());
        assertEquals("default", placement.get("frequency_reduction_method").getAsString());
    }

    @Test
    void exposesOneLocatableStructureAcrossPlainsAndBeach() {
        JsonObject structure = resource(
                "/data/funny_cat_stick/worldgen/structure/dswhale_residence.json"
        );
        assertEquals("minecraft:jigsaw", structure.get("type").getAsString());
        assertEquals(1, structure.get("size").getAsInt(),
                "Jigsaw size 0 produces no structure pieces in Minecraft 1.20.1");
        assertEquals(
                "#funny_cat_stick:has_structure/dswhale_residence",
                structure.get("biomes").getAsString()
        );
        assertEquals(
                "funny_cat_stick:dswhale_residence/start_pool",
                structure.get("start_pool").getAsString()
        );

        JsonArray biomes = resource(
                "/data/funny_cat_stick/tags/worldgen/biome/has_structure/dswhale_residence.json"
        ).getAsJsonArray("values");
        assertTrue(biomes.asList().stream().anyMatch(value -> value.getAsString().equals("minecraft:plains")));
        assertTrue(biomes.asList().stream().anyMatch(value -> value.getAsString().equals("minecraft:sunflower_plains")));
        assertTrue(biomes.asList().stream().anyMatch(value -> value.getAsString().equals("minecraft:beach")));
    }

    @Test
    void reusesTheOnlyResidenceTemplate() {
        JsonObject pool = resource(
                "/data/funny_cat_stick/worldgen/template_pool/dswhale_residence/start_pool.json"
        );
        JsonObject element = pool.getAsJsonArray("elements")
                .get(0)
                .getAsJsonObject()
                .getAsJsonObject("element");
        assertEquals("minecraft:single_pool_element", element.get("element_type").getAsString());
        assertEquals("funny_cat_stick:dswhale_architecture", element.get("location").getAsString());
    }

    private static JsonObject resource(String path) {
        try (InputStream stream = DswhaleWorldgenResourcesTest.class.getResourceAsStream(path)) {
            assertNotNull(stream, "Missing test resource " + path);
            return JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (IOException exception) {
            throw new AssertionError("Could not read test resource " + path, exception);
        }
    }
}
