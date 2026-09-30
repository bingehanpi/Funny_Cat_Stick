package io.github.bingehanpi.funny_cat_stick.command;

import com.mojang.brigadier.CommandDispatcher;
import io.github.bingehanpi.funny_cat_stick.worldgen.DswhaleResidencePlacement;
import io.github.bingehanpi.funny_cat_stick.worldgen.DswhaleVillageSavedData;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
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
                                .executes(context -> locateNearest(context.getSource()))))
                .then(Commands.literal("place")
                        .then(Commands.literal("dswhale_residence")
                                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                                        .executes(context -> placeResidence(
                                                context.getSource(),
                                                BlockPosArgument.getBlockPos(context, "pos")
                                        ))))));
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
                .placedResidences()
                .stream()
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

    private static int placeResidence(CommandSourceStack source, BlockPos templateOrigin) {
        ServerLevel level = source.getLevel();
        if (!Level.OVERWORLD.equals(level.dimension())) {
            source.sendFailure(Component.literal("Dswhale residences can only be generated in the Overworld"));
            return 0;
        }

        DswhaleResidencePlacement.CommandPlacementResult result =
                DswhaleResidencePlacement.placeAt(level, templateOrigin);
        if (result != DswhaleResidencePlacement.CommandPlacementResult.PLACED) {
            source.sendFailure(Component.literal(switch (result) {
                case TEMPLATE_MISSING -> "The dswhale residence template could not be loaded";
                case ALREADY_RECORDED -> "A generated dswhale residence is already recorded at that origin";
                case OUTSIDE_BUILD_HEIGHT -> "The dswhale residence would extend outside the build height";
                case INCOMPLETE_TEMPLATE -> "The dswhale residence template could not be placed completely";
                case RESIDENT_FAILED -> "The residence was rolled back because its dswhale could not be initialized";
                case EXCEPTION -> "The residence was rolled back because an unexpected error occurred";
                case PLACED -> throw new IllegalStateException("Successful placement reached failure handling");
            }));
            return 0;
        }

        DswhaleVillageSavedData.PlacementPlan plan = new DswhaleVillageSavedData.PlacementPlan(
                templateOrigin,
                net.minecraft.world.level.block.Rotation.NONE
        );
        BlockPos home = DswhaleResidencePlacement.residenceHome(plan);
        source.sendSuccess(
                () -> Component.literal("Generated a natural dswhale residence at ")
                        .append(Component.literal("[" + home.getX() + ", " + home.getY() + ", " + home.getZ() + "]")
                                .withStyle(ChatFormatting.GREEN)),
                true
        );
        return 1;
    }

    private static double horizontalDistanceSquared(double sourceX, double sourceZ, BlockPos target) {
        double deltaX = target.getX() - sourceX;
        double deltaZ = target.getZ() - sourceZ;
        return deltaX * deltaX + deltaZ * deltaZ;
    }

    private record LocatedResidence(BlockPos position, double distanceSquared) {
    }
}
