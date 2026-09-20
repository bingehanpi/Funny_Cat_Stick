package io.github.bingehanpi.funny_cat_stick.item;

import io.github.bingehanpi.funny_cat_stick.config.ModServerConfig;
import io.github.bingehanpi.funny_cat_stick.effect.CatChatEffect;
import io.github.bingehanpi.funny_cat_stick.effect.CatPersonality;
import io.github.bingehanpi.funny_cat_stick.enchantment.CatEnchantmentType;
import io.github.bingehanpi.funny_cat_stick.enchantment.CatPersonalityEnchantment;
import io.github.bingehanpi.funny_cat_stick.network.ModNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

public final class FunnyCatStickItem extends Item {

    public FunnyCatStickItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public int getEnchantmentValue(ItemStack stack) {
        return 15;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return enchantment instanceof CatPersonalityEnchantment personalityEnchantment
                && personalityEnchantment.getType() == CatEnchantmentType.NORMAL;
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
            CatPersonality personality = CatPersonality.fromItemStack(stack);
            CatChatEffect.apply(targetPlayer, player, personality);
            CatChatEffect.announceTaming(player, targetPlayer);
            if (personality == CatPersonality.IKUN && ModServerConfig.isIkunMusicEnabled()) {
                ModNetworking.playIkunMusic(targetPlayer);
                if (player instanceof ServerPlayer ownerPlayer && ownerPlayer != targetPlayer) {
                    ModNetworking.playIkunMusic(ownerPlayer);
                }
            }
            stack.shrink(1);
            return InteractionResult.CONSUME;
        }

        return InteractionResult.PASS;
    }
}
