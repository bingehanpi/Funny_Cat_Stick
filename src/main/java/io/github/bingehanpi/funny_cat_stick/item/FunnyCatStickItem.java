package io.github.bingehanpi.funny_cat_stick.item;

import io.github.bingehanpi.funny_cat_stick.effect.CatChatEffect;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class FunnyCatStickItem extends Item {

    public FunnyCatStickItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult interactLivingEntity(
            ItemStack stack,
            Player player,
            LivingEntity target,
            InteractionHand hand
    ) {
        if (!(target instanceof Player)) {
            return InteractionResult.PASS;
        }

        if (player.level().isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (target instanceof ServerPlayer targetPlayer) {
            CatChatEffect.apply(targetPlayer, player);
            CatChatEffect.announceTaming(player, targetPlayer);
            stack.shrink(1);
            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
    }
}
