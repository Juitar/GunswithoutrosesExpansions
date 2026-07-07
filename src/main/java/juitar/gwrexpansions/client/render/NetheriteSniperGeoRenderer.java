package juitar.gwrexpansions.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import juitar.gwrexpansions.client.model.NetheriteSniperGeoModel;
import juitar.gwrexpansions.item.vanilla.NetheriteSniper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class NetheriteSniperGeoRenderer extends GeoItemRenderer<NetheriteSniper> {
    public NetheriteSniperGeoRenderer() {
        super(new NetheriteSniperGeoModel());
    }

    @Override
    public void renderRecursively(PoseStack poseStack, NetheriteSniper animatable, GeoBone bone,
                                  RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer,
                                  boolean isReRender, float partialTick, int packedLight, int packedOverlay,
                                  float red, float green, float blue, float alpha) {
        if (isFireOnlyBone(bone.getName()) && !NetheriteSniper.isFlashAnimationActive(getCurrentItemStack())) {
            return;
        }

        super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender,
                partialTick, packedLight, packedOverlay, red, green, blue, alpha);
    }

    private static boolean isFireOnlyBone(String boneName) {
        return "flash".equals(boneName) || "bullet".equals(boneName);
    }
}
