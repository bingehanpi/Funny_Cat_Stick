package io.github.bingehanpi.funny_cat_stick.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import io.github.bingehanpi.funny_cat_stick.effect.CatChatEffect;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class FunnyCatStickCommands {

    private static final Pattern DURATION_PATTERN = Pattern.compile("^(\\d+(?:\\.\\d+)?)([smhd])$", Pattern.CASE_INSENSITIVE);
    private static final DynamicCommandExceptionType INVALID_DURATION = new DynamicCommandExceptionType(
            value -> Component.translatable("command.funny_cat_stick.invalid_duration", value)
    );

    private FunnyCatStickCommands() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("funnycatstick")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("tame")
                        .then(Commands.argument("owner", EntityArgument.player())
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .then(Commands.argument("duration", StringArgumentType.word())
                                                .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                                        new String[]{"10s", "5m", "30m", "1h"}, builder))
                                                .executes(context -> tame(
                                                        context.getSource(),
                                                        EntityArgument.getPlayer(context, "owner"),
                                                        EntityArgument.getPlayers(context, "targets"),
                                                        parseDuration(context, "duration")
                                                ))))))
                .then(Commands.literal("query")
                        .then(Commands.argument("targets", EntityArgument.players())
                                .executes(context -> query(
                                        context.getSource(),
                                        EntityArgument.getPlayers(context, "targets")
                                ))))
                .then(Commands.literal("reduce")
                        .then(Commands.argument("targets", EntityArgument.players())
                                .then(Commands.argument("duration", StringArgumentType.word())
                                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                                new String[]{"10s", "1m", "5m", "10m"}, builder))
                                        .executes(context -> reduce(
                                                context.getSource(),
                                                EntityArgument.getPlayers(context, "targets"),
                                                parseDuration(context, "duration")
                                        ))))));
    }

    private static int tame(
            CommandSourceStack source,
            ServerPlayer owner,
            Collection<ServerPlayer> targets,
            long durationMillis
    ) {
        for (ServerPlayer target : targets) {
            CatChatEffect.apply(target, owner, durationMillis);
            CatChatEffect.announceTaming(owner, target);
        }
        source.sendSuccess(
                () -> Component.translatable("command.funny_cat_stick.tame.success", targets.size()),
                false
        );
        return targets.size();
    }

    private static int query(CommandSourceStack source, Collection<ServerPlayer> targets) {
        for (ServerPlayer target : targets) {
            long remaining = CatChatEffect.getRemainingMillis(target);
            if (remaining > 0) {
                source.sendSuccess(
                        () -> Component.translatable(
                                "command.funny_cat_stick.query.active",
                                target.getDisplayName(),
                                formatDuration(remaining)
                        ),
                        false
                );
            } else {
                source.sendSuccess(
                        () -> Component.translatable(
                                "command.funny_cat_stick.query.inactive",
                                target.getDisplayName()
                        ),
                        false
                );
            }
        }
        return targets.size();
    }

    private static int reduce(
            CommandSourceStack source,
            Collection<ServerPlayer> targets,
            long amountMillis
    ) {
        for (ServerPlayer target : targets) {
            long remaining = CatChatEffect.reduce(target, amountMillis);
            if (remaining < 0) {
                source.sendSuccess(
                        () -> Component.translatable(
                                "command.funny_cat_stick.query.inactive",
                                target.getDisplayName()
                        ),
                        false
                );
            } else {
                source.sendSuccess(
                        () -> Component.translatable(
                                "command.funny_cat_stick.reduce.success",
                                target.getDisplayName(),
                                formatDuration(remaining)
                        ),
                        false
                );
            }
        }
        return targets.size();
    }

    private static long parseDuration(com.mojang.brigadier.context.CommandContext<CommandSourceStack> context, String argumentName)
            throws CommandSyntaxException {
        return parseDuration(StringArgumentType.getString(context, argumentName));
    }

    private static long parseDuration(String input) throws CommandSyntaxException {
        Matcher matcher = DURATION_PATTERN.matcher(input.toLowerCase(Locale.ROOT));
        if (!matcher.matches()) {
            throw INVALID_DURATION.create(input);
        }

        double value;
        try {
            value = Double.parseDouble(matcher.group(1));
        } catch (NumberFormatException exception) {
            throw INVALID_DURATION.create(input);
        }

        long unitMillis = switch (matcher.group(2)) {
            case "s" -> 1_000L;
            case "m" -> 60_000L;
            case "h" -> 3_600_000L;
            case "d" -> 86_400_000L;
            default -> 0L;
        };
        double result = value * unitMillis;
        if (!Double.isFinite(result) || result < 1 || result > Long.MAX_VALUE - System.currentTimeMillis()) {
            throw INVALID_DURATION.create(input);
        }
        return Math.round(result);
    }

    private static String formatDuration(long durationMillis) {
        long totalSeconds = Math.max(0, (durationMillis + 999) / 1_000);
        long days = totalSeconds / 86_400;
        long hours = totalSeconds % 86_400 / 3_600;
        long minutes = totalSeconds % 3_600 / 60;
        long seconds = totalSeconds % 60;

        StringBuilder result = new StringBuilder();
        appendPart(result, days, "d");
        appendPart(result, hours, "h");
        appendPart(result, minutes, "m");
        if (seconds > 0 || result.isEmpty()) {
            appendPart(result, seconds, "s");
        }
        return result.toString();
    }

    private static void appendPart(StringBuilder builder, long value, String unit) {
        if (value <= 0) {
            return;
        }
        if (!builder.isEmpty()) {
            builder.append(' ');
        }
        builder.append(value).append(unit);
    }
}
