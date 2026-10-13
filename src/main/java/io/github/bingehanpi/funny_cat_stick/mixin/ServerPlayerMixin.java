package io.github.bingehanpi.funny_cat_stick.mixin;

import io.github.bingehanpi.funny_cat_stick.loot.pity.CatStickPity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin {
    @Redirect(method = "openMenu", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/world/MenuProvider;createMenu(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/entity/player/Player;)Lnet/minecraft/world/inventory/AbstractContainerMenu;"))
    private AbstractContainerMenu funny_cat_stick$createMenuWithPity(MenuProvider provider, int id, Inventory inventory, Player player) {
        return CatStickPity.createMenu((ServerPlayer) player, () -> provider.createMenu(id, inventory, player));
    }
}
