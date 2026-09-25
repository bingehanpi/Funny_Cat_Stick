package io.github.bingehanpi.funny_cat_stick.command;

import com.mojang.brigadier.CommandDispatcher;
import io.github.bingehanpi.funny_cat_stick.worldgen.DswhaleResidencePlacement;
import io.github.bingehanpi.funny_cat_stick.worldgen.DswhaleVillageSavedData;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;

import java.util.Comparator;
import java.util.Optional;

public final class DswhaleResidenceLocateCommand {

    private DswhaleResidenceLocateCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("fcs")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("locate")
                        .then(Commands.literal("dswhale_residence")
                                .executes(context -> locateNearest(context.getSource())))));
    }

    private static int locateNearest(CommandSourceStack source) {
        ServerLevel overworld = source.getServer().getLevel(Level.OVERWORLD);
        if (overworld == null) {
            source.sendFailure(Component.literal("Overworld is unavailable"));
            return 0;
        }

        double sourceX = source.getPosition().x;
        double sourceZ = source.getPosition().z;
        Optional<LocatedResidence> nearest = DswhaleVillageSavedData.get(overworld)
                .villagesWithStatus(DswhaleVillageSavedData.Status.PLACED)
                .stream()
                .flatMap(entry -> entry.placement().stream())
                .map(plan -> new LocatedResidence(
                        DswhaleResidencePlacement.residenceHome(plan),
                        horizontalDistanceSquared(sourceX, sourceZ, DswhaleResidencePlacement.residenceHome(plan))
                ))
                .min(Comparator.comparingDouble(LocatedResidence::distanceSquared));

        if (nearest.isEmpty()) {
            source.sendFailure(Component.literal("No generated dswhale residence is currently recorded"));
            return 0;
        }

        BlockPos position = nearest.get().position();
        int distance = Mth.floor(Math.sqrt(nearest.get().distanceSquared()));
        Component coordinates = Component.literal("[" + position.getX() + ", ~, " + position.getZ() + "]")
                .withStyle(ChatFormatting.GREEN)
                .withStyle(style -> style
                        .withClickEvent(new ClickEvent(
                                ClickEvent.Action.SUGGEST_COMMAND,
                                "/tp @s " + position.getX() + " ~ " + position.getZ()
                        ))
                        .withHoverEvent(new HoverEvent(
                                HoverEvent.Action.SHOW_TEXT,
                                Component.literal("Click to prepare a teleport command")
                        )));
        source.sendSuccess(
                () -> Component.literal("Nearest generated dswhale residence: ")
                        .append(coordinates)
                        .append(Component.literal(" (" + distance + " blocks)")),
                false
        );
        return distance;
    }

    private static double horizontalDistanceSquared(double sourceX, double sourceZ, BlockPos target) {
        double deltaX = target.getX() - sourceX;
        double deltaZ = target.getZ() - sourceZ;
        return deltaX * deltaX + deltaZ * deltaZ;
    }

    private record LocatedResidence(BlockPos position, double distanceSquared) {
    }
}
