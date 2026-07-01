package juitar.gwrexpansions.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import juitar.gwrexpansions.client.model.HellforgeGeoModel;
import juitar.gwrexpansions.item.BOMD.Hellforge;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemDisplayContext;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class HellforgeGeoRenderer extends GeoItemRenderer<Hellforge> {
    public HellforgeGeoRenderer() {
        super(new HellforgeGeoModel());
    }

    @Override
    public void renderRecursively(PoseStack poseStack, Hellforge animatable, GeoBone bone, RenderType renderType,
                                  MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender,
                                  float partialTick, int packedLight, int packedOverlay,
                                  float red, float green, float blue, float alpha) {
        if (!isFirstPerson() && isFirstPersonOnlyBone(bone.getName())) {
            return;
        }

        super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender,
            partialTick, packedLight, packedOverlay, red, green, blue, alpha);
    }

    private boolean isFirstPerson() {
        return this.renderPerspective == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
            || this.renderPerspective == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
    }

    private static boolean isFirstPersonOnlyBone(String boneName) {
        return "hand".equals(boneName) || "Coin".equals(boneName) || "FlashCoin".equals(boneName);
    }
}
