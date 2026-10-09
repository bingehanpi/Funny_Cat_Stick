package io.github.bingehanpi.funny_cat_stick.item;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class FunnyQuestionStickItem extends Item {

    private static final double MESSAGE_RANGE_SQUARED = 16.0D * 16.0D;
    private static final String MESSAGE_KEY = "message.funny_cat_stick.question_stick.meow";

    public FunnyQuestionStickItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult interactLivingEntity(
            ItemStack stack,
            Player player,
            LivingEntity target,
            InteractionHand hand
    ) {
        if (target instanceof Player) {
            return InteractionResult.PASS;
        }

        if (player.level().isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (!(target.level() instanceof ServerLevel serverLevel)) {
            return InteractionResult.PASS;
        }

        Component message = Component.translatable(MESSAGE_KEY, target.getDisplayName());
        for (ServerPlayer nearbyPlayer : serverLevel.players()) {
            if (nearbyPlayer.distanceToSqr(target) <= MESSAGE_RANGE_SQUARED) {
                nearbyPlayer.sendSystemMessage(message);
            }
        }

        stack.shrink(1);
        return InteractionResult.CONSUME;
    }
}
