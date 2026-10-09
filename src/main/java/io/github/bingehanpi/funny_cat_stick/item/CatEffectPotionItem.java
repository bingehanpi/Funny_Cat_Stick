package io.github.bingehanpi.funny_cat_stick.item;

import io.github.bingehanpi.funny_cat_stick.effect.CatChatEffect;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

import javax.annotation.Nullable;
import java.util.List;

public final class CatEffectPotionItem extends Item {

    private static final int DRINK_DURATION_TICKS = 32;

    private final long durationChangeMillis;
    @Nullable
    private final String tooltipKey;

    public CatEffectPotionItem(
            Properties properties,
            long durationChangeMillis,
            @Nullable String tooltipKey
    ) {
        super(properties);
        this.durationChangeMillis = durationChangeMillis;
        this.tooltipKey = tooltipKey;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        Player player = entity instanceof Player ? (Player) entity : null;
        if (player instanceof ServerPlayer serverPlayer) {
            CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayer, stack);
            applyDurationChange(serverPlayer);
        }

        if (player != null) {
            player.awardStat(Stats.ITEM_USED.get(this));
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }

        if (player == null || !player.getAbilities().instabuild) {
            if (stack.isEmpty()) {
                entity.gameEvent(GameEvent.DRINK);
                return new ItemStack(Items.GLASS_BOTTLE);
            }

            if (player != null) {
                ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
                if (!player.getInventory().add(bottle)) {
                    player.drop(bottle, false);
                }
            }
        }

        entity.gameEvent(GameEvent.DRINK);
        return stack;
    }

    private void applyDurationChange(ServerPlayer player) {
        if (durationChangeMillis < 0) {
            CatChatEffect.reduce(player, -durationChangeMillis);
        } else if (durationChangeMillis > 0) {
            CatChatEffect.extend(player, durationChangeMillis);
        }
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return DRINK_DURATION_TICKS;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public void appendHoverText(
            ItemStack stack,
            @Nullable Level level,
            List<Component> tooltip,
            TooltipFlag flag
    ) {
        if (tooltipKey != null) {
            tooltip.add(Component.translatable(tooltipKey).withStyle(ChatFormatting.GRAY));
        }
    }
}
