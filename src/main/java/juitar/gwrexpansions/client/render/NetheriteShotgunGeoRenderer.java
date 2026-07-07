package juitar.gwrexpansions.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import juitar.gwrexpansions.client.model.NetheriteShotgunGeoModel;
import juitar.gwrexpansions.item.vanilla.NetheriteShotgun;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class NetheriteShotgunGeoRenderer extends GeoItemRenderer<NetheriteShotgun> {
    public NetheriteShotgunGeoRenderer() {
        super(new NetheriteShotgunGeoModel());
    }

    @Override
    public void renderRecursively(PoseStack poseStack, NetheriteShotgun animatable, GeoBone bone,
                                  RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer,
                                  boolean isReRender, float partialTick, int packedLight, int packedOverlay,
                                  float red, float green, float blue, float alpha) {
        if (isFireOnlyBone(bone.getName()) && !NetheriteShotgun.isFlashAnimationActive(getCurrentItemStack())) {
            return;
        }

        super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender,
                partialTick, packedLight, packedOverlay, red, green, blue, alpha);
    }

    private static boolean isFireOnlyBone(String boneName) {
        return "fire".equals(boneName) || "shell".equals(boneName);
    }
}
