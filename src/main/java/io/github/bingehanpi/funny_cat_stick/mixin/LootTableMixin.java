package io.github.bingehanpi.funny_cat_stick.mixin;

import io.github.bingehanpi.funny_cat_stick.loot.pity.CatStickPity;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LootTable.class)
public abstract class LootTableMixin {
    @Shadow
    private ObjectArrayList<ItemStack> getRandomItems(LootContext context) {
        throw new AssertionError();
    }

    @Redirect(method = "fill", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/world/level/storage/loot/LootTable;getRandomItems(Lnet/minecraft/world/level/storage/loot/LootContext;)Lit/unimi/dsi/fastutil/objects/ObjectArrayList;"))
    private ObjectArrayList<ItemStack> funny_cat_stick$generatePlayerLoot(
            LootTable table, LootContext context, Container container, LootParams params, long seed) {
        return CatStickPity.generateForFill(container, params, () -> getRandomItems(context));
    }

    @Inject(method = "fill", at = @At("RETURN"))
    private void funny_cat_stick$afterLootFilled(Container container, LootParams params, long seed, CallbackInfo callback) {
        CatStickPity.onFilled(container, params, ((LootTable) (Object) this).getLootTableId());
    }
}
