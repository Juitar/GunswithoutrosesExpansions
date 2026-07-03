package juitar.gwrexpansions.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import juitar.gwrexpansions.client.model.SupershotgunGeoModel;
import juitar.gwrexpansions.item.vanilla.Supershotgun;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class SupershotgunGeoRenderer extends GeoItemRenderer<Supershotgun> {
    public SupershotgunGeoRenderer() {
        super(new SupershotgunGeoModel());
    }

    @Override
    public void renderRecursively(PoseStack poseStack, Supershotgun animatable, GeoBone bone, RenderType renderType,
                                  MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender,
                                  float partialTick, int packedLight, int packedOverlay,
                                  float red, float green, float blue, float alpha) {
        if (isMuzzleFlashBone(bone.getName()) && !Supershotgun.isFlashAnimationActive(getCurrentItemStack())) {
            return;
        }

        super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender,
            partialTick, packedLight, packedOverlay, red, green, blue, alpha);
    }

    private static boolean isMuzzleFlashBone(String boneName) {
        return "Flash".equals(boneName) || "Flash2".equals(boneName);
    }
}
