package juitar.gwrexpansions.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import juitar.gwrexpansions.entity.cataclysm.IgnitiumFlameSigilEntity;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;

public class IgnitiumFlameSigilRenderer extends EntityRenderer<IgnitiumFlameSigilEntity> {
    public IgnitiumFlameSigilRenderer(EntityRendererProvider.Context context) { super(context); }
    @Override public ResourceLocation getTextureLocation(IgnitiumFlameSigilEntity entity) {
        return new ResourceLocation("cataclysm", "textures/entity/" + (entity.isSoul() ? "soul_" : "") + "flame_strike_sigil.png");
    }
    @Override public void render(IgnitiumFlameSigilEntity entity, float yaw, float partialTick,
                                  PoseStack pose, MultiBufferSource buffers, int light) {
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees((entity.tickCount + partialTick) * 2));
        var vertex = buffers.getBuffer(RenderType.entityTranslucent(getTextureLocation(entity)));
        var matrix = pose.last().pose();
        var normal = pose.last().normal();
        float r = entity.blastRadius();
        vertex.vertex(matrix, -r, 0, -r).color(255, 255, 255, 230).uv(0, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normal, 0, 1, 0).endVertex();
        vertex.vertex(matrix, -r, 0, r).color(255, 255, 255, 230).uv(0, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normal, 0, 1, 0).endVertex();
        vertex.vertex(matrix, r, 0, r).color(255, 255, 255, 230).uv(1, 1).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normal, 0, 1, 0).endVertex();
        vertex.vertex(matrix, r, 0, -r).color(255, 255, 255, 230).uv(1, 0).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT).normal(normal, 0, 1, 0).endVertex();
        pose.popPose();
        super.render(entity, yaw, partialTick, pose, buffers, light);
    }
}
