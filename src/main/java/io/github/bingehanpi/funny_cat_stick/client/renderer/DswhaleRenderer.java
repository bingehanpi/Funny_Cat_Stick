package io.github.bingehanpi.funny_cat_stick.client.renderer;

import io.github.bingehanpi.funny_cat_stick.client.model.DswhaleModel;
import io.github.bingehanpi.funny_cat_stick.entity.DswhaleEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public final class DswhaleRenderer extends GeoEntityRenderer<DswhaleEntity> {

    public DswhaleRenderer(EntityRendererProvider.Context context) {
        super(context, new DswhaleModel());
        shadowRadius = 0.45F;
    }
}
