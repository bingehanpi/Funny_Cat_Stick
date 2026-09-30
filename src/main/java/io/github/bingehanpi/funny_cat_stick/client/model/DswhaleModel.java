package io.github.bingehanpi.funny_cat_stick.client.model;

import io.github.bingehanpi.funny_cat_stick.FunnyCatStick;
import io.github.bingehanpi.funny_cat_stick.entity.DswhaleEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.model.data.EntityModelData;
import software.bernie.geckolib.model.GeoModel;

public final class DswhaleModel extends GeoModel<DswhaleEntity> {

    private static final String HEAD_BONE = "head";
    private static final String IRON_POT_BONE = "iron_pot";
    private static final float MAX_HEAD_YAW = 60.0F;
    private static final float MAX_HEAD_PITCH = 45.0F;

    private static final ResourceLocation MODEL =
            ResourceLocation.fromNamespaceAndPath(FunnyCatStick.MOD_ID, "geo/whale_maid_v2.geo.json");
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(FunnyCatStick.MOD_ID, "textures/entity/whale_maid_v2.png");
    private static final ResourceLocation ANIMATIONS =
            ResourceLocation.fromNamespaceAndPath(FunnyCatStick.MOD_ID, "animations/whale_maid_v2.animation.json");

    @Override
    public ResourceLocation getModelResource(DswhaleEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(DswhaleEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(DswhaleEntity animatable) {
        return ANIMATIONS;
    }

    @Override
    public void setCustomAnimations(DswhaleEntity animatable, long instanceId,
                                    AnimationState<DswhaleEntity> animationState) {
        CoreGeoBone ironPot = getAnimationProcessor().getBone(IRON_POT_BONE);
        if (ironPot != null) {
            ironPot.setHidden(!animatable.isWearingIronPot());
        }

        if (animatable.isSleeping()) {
            return;
        }

        EntityModelData entityModelData = animationState.getData(DataTickets.ENTITY_MODEL_DATA);
        CoreGeoBone head = getAnimationProcessor().getBone(HEAD_BONE);
        if (entityModelData == null || head == null) {
            return;
        }

        float headYaw = Mth.clamp(entityModelData.netHeadYaw(), -MAX_HEAD_YAW, MAX_HEAD_YAW);
        float headPitch = Mth.clamp(entityModelData.headPitch(), -MAX_HEAD_PITCH, MAX_HEAD_PITCH);

        // This hook runs after GeckoLib evaluates the active animation, so adding
        // the look angles preserves idle/walk motion without letting it erase AI look control.
        head.setRotY(head.getRotY() + headYaw * Mth.DEG_TO_RAD);
        head.setRotX(head.getRotX() + headPitch * Mth.DEG_TO_RAD);
    }
}
