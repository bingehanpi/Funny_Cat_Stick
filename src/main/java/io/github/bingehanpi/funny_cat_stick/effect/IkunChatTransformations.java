package io.github.bingehanpi.funny_cat_stick.effect;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

final class IkunChatTransformations {

    private static final String RESOURCE_PATH = "/data/funny_cat_stick/personality/ikun.json";
    private static final MessageData MESSAGES = loadMessages();

    private IkunChatTransformations() {
    }

    static Component transform(Component message, RandomSource random) {
        if (random.nextFloat() < MESSAGES.bothChance()) {
            String prefix = MESSAGES.prefixes().get(random.nextInt(MESSAGES.prefixes().size()));
            String suffix = MESSAGES.suffixes().get(random.nextInt(MESSAGES.suffixes().size()));
            return Component.literal(prefix).append(message.copy()).append(suffix);
        }

        int index = random.nextInt(MESSAGES.totalCount());
        if (index < MESSAGES.prefixes().size()) {
            return Component.literal(MESSAGES.prefixes().get(index)).append(message.copy());
        }
        return message.copy().append(MESSAGES.suffixes().get(index - MESSAGES.prefixes().size()));
    }

    static void validateResources() {
        if (MESSAGES.totalCount() == 0) {
            throw new IllegalStateException("No IKUN personality messages were loaded");
        }
    }

    private static MessageData loadMessages() {
        try (InputStream stream = IkunChatTransformations.class.getResourceAsStream(RESOURCE_PATH)) {
            if (stream == null) {
                throw new IllegalStateException("Missing personality messages: " + RESOURCE_PATH);
            }
            try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                JsonObject personality = root.getAsJsonObject("kun_style");
                if (personality == null) {
                    throw new IllegalStateException("Missing personality object: kun_style");
                }
                float bothChance = personality.get("both_chance").getAsFloat();
                if (bothChance < 0.0F || bothChance > 1.0F) {
                    throw new IllegalStateException("both_chance must be between 0 and 1");
                }
                return new MessageData(
                        readMessages(personality, "prefix"),
                        readMessages(personality, "suffix"),
                        bothChance
                );
            }
        } catch (IOException | RuntimeException exception) {
            throw new IllegalStateException("Failed to load personality messages: " + RESOURCE_PATH, exception);
        }
    }

    private static List<String> readMessages(JsonObject root, String fieldName) {
        JsonArray array = root.getAsJsonArray(fieldName);
        if (array == null || array.size() == 0) {
            throw new IllegalStateException("Missing or empty message array: " + fieldName);
        }

        List<String> messages = new ArrayList<>(array.size());
        for (JsonElement element : array) {
            String message = element.getAsString();
            if (message.isEmpty()) {
                throw new IllegalStateException("Empty message in array: " + fieldName);
            }
            messages.add(message);
        }
        return List.copyOf(messages);
    }

    private record MessageData(List<String> prefixes, List<String> suffixes, float bothChance) {

        private int totalCount() {
            return prefixes.size() + suffixes.size();
        }
    }
}
