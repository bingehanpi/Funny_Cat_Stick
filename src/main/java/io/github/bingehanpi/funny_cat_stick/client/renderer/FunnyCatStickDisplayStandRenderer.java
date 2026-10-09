package io.github.bingehanpi.funny_cat_stick.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.bingehanpi.funny_cat_stick.blockentity.FunnyCatStickDisplayStandBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public final class FunnyCatStickDisplayStandRenderer
        implements BlockEntityRenderer<FunnyCatStickDisplayStandBlockEntity> {

    private static final float ITEM_HEIGHT = 10.45F / 16.0F;
    private static final float NORTH_WEST_OFFSET = 4.0F / 16.0F;
    private static final float SOUTH_EAST_OFFSET = 12.0F / 16.0F;
    private static final float ITEM_SCALE = 0.58F;
    private static final float DIAGONAL_TO_HORIZONTAL_ROTATION = -45.0F;

    private final ItemRenderer itemRenderer;

    public FunnyCatStickDisplayStandRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(
            FunnyCatStickDisplayStandBlockEntity stand,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay
    ) {
        renderItem(stand, Direction.NORTH, 0.5F, ITEM_HEIGHT, NORTH_WEST_OFFSET,
                0.0F, poseStack, bufferSource, packedLight, packedOverlay);
        renderItem(stand, Direction.SOUTH, 0.5F, ITEM_HEIGHT, SOUTH_EAST_OFFSET,
                180.0F, poseStack, bufferSource, packedLight, packedOverlay);
        renderItem(stand, Direction.EAST, SOUTH_EAST_OFFSET, ITEM_HEIGHT, 0.5F,
                90.0F, poseStack, bufferSource, packedLight, packedOverlay);
        renderItem(stand, Direction.WEST, NORTH_WEST_OFFSET, ITEM_HEIGHT, 0.5F,
                -90.0F, poseStack, bufferSource, packedLight, packedOverlay);
    }

    private void renderItem(
            FunnyCatStickDisplayStandBlockEntity stand,
            Direction direction,
            float x,
            float y,
            float z,
            float yRotation,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            int packedOverlay
    ) {
        ItemStack stack = stand.getItem(direction);
        if (stack.isEmpty()) {
            return;
        }

        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.mulPose(Axis.YP.rotationDegrees(yRotation));
        poseStack.mulPose(Axis.ZP.rotationDegrees(DIAGONAL_TO_HORIZONTAL_ROTATION));
        poseStack.scale(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE);
        itemRenderer.renderStatic(
                stack,
                ItemDisplayContext.FIXED,
                packedLight,
                packedOverlay,
                poseStack,
                bufferSource,
                stand.getLevel(),
                0
        );
        poseStack.popPose();
    }
}
