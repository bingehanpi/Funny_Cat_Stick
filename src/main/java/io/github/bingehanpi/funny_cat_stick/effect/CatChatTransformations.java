package io.github.bingehanpi.funny_cat_stick.effect;

import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;

import java.util.List;
import java.util.function.Function;

public final class CatChatTransformations {

    private static final List<Function<Component, Component>> CLASSIC = List.of(
            message -> Component.literal("\u4e3b\u4eba,")
                    .append(message.copy())
                    .append(",\u8bf7\u60e9\u7f5a\u6211\u55b5!~"),
            message -> message.copy().append("(\u2032\u3078`\u3001 )"),
            message -> Component.literal("\u55b5\u545c~ ")
                    .append(message.copy())
                    .append(" (=^\uff65\u03c9\uff65^=)"),
            message -> Component.literal("\u4eba\u5bb6\u53ea\u662f\u60f3\u8bf4:")
                    .append(message.copy())
                    .append("\u55b5~"),
            message -> Component.literal("(\u5c0f\u58f0)")
                    .append(message.copy())
                    .append("..."),
            message -> message.copy().append(",\u624d\u3001\u624d\u4e0d\u662f\u6545\u610f\u8bf4\u7ed9\u4f60\u542c\u7684\u55b5!"),
            message -> Component.literal("\u4e3b\u4eba\u8bf7\u542c\u597d,")
                    .append(message.copy())
                    .append("!"),
            message -> message.copy().append(" (=\u00b4\u03c9`=)"),
            message -> Component.literal("\u545c\u55b5...")
                    .append(message.copy())
                    .append(",\u53ef\u4ee5\u6478\u6478\u5934\u5417?"),
            message -> Component.literal("\u6700\u559c\u6b22\u4e3b\u4eba\u4e86,\u6240\u4ee5\u8981\u8bf4:")
                    .append(message.copy())
    );

    private CatChatTransformations() {
    }

    public static void validateResources() {
        IkunChatTransformations.validateResources();
    }

    public static Component transform(Component message, CatPersonality personality, RandomSource random) {
        if (personality == CatPersonality.IKUN) {
            return IkunChatTransformations.transform(message, random);
        }

        if (personality != CatPersonality.CLASSIC) {
            int index = random.nextInt(personality.getTransformationCount());
            return Component.translatable(
                    "message.funny_cat_stick.personality."
                            + personality.getSerializedName()
                            + "."
                            + index,
                    message.copy()
            );
        }

        int index = random.nextInt(CLASSIC.size());
        return CLASSIC.get(index).apply(message);
    }
}
