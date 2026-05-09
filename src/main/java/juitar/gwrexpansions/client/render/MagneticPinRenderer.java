package juitar.gwrexpansions.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import juitar.gwrexpansions.entity.alexscaves.MagneticPinEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

public class MagneticPinRenderer extends EntityRenderer<MagneticPinEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("alexscaves", "textures/block/neodymium/azure_pillar_side.png");
    private static final float HALF_LENGTH = 0.48F;
    private static final float HALF_WIDTH = 0.075F;

    public MagneticPinRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0F;
    }

    @Override
    public void render(MagneticPinEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        Vec3 axis = entity.getMagneticAxis();
        poseStack.mulPose(new Quaternionf().rotationTo(1.0F, 0.0F, 0.0F, (float) axis.x, (float) axis.y, (float) axis.z));

        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutoutNoCull(getTextureLocation(entity)));
        renderBox(poseStack, consumer, packedLight);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private void renderBox(PoseStack poseStack, VertexConsumer consumer, int light) {
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();

        float x0 = -HALF_LENGTH;
        float x1 = HALF_LENGTH;
        float y0 = -HALF_WIDTH;
        float y1 = HALF_WIDTH;
        float z0 = -HALF_WIDTH;
        float z1 = HALF_WIDTH;

        quad(consumer, matrix, normal, x1, y0, z0, x1, y1, z0, x1, y1, z1, x1, y0, z1, 1, 0, 0, light);
        quad(consumer, matrix, normal, x0, y0, z1, x0, y1, z1, x0, y1, z0, x0, y0, z0, -1, 0, 0, light);
        quad(consumer, matrix, normal, x0, y1, z0, x0, y1, z1, x1, y1, z1, x1, y1, z0, 0, 1, 0, light);
        quad(consumer, matrix, normal, x0, y0, z1, x0, y0, z0, x1, y0, z0, x1, y0, z1, 0, -1, 0, light);
        quad(consumer, matrix, normal, x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1, 0, 0, 1, light);
        quad(consumer, matrix, normal, x1, y0, z0, x0, y0, z0, x0, y1, z0, x1, y1, z0, 0, 0, -1, light);
    }

    private void quad(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal,
                      float x1, float y1, float z1,
                      float x2, float y2, float z2,
                      float x3, float y3, float z3,
                      float x4, float y4, float z4,
                      float normalX, float normalY, float normalZ, int light) {
        vertex(consumer, matrix, normal, x1, y1, z1, 0.0F, 1.0F, normalX, normalY, normalZ, light);
        vertex(consumer, matrix, normal, x2, y2, z2, 0.0F, 0.0F, normalX, normalY, normalZ, light);
        vertex(consumer, matrix, normal, x3, y3, z3, 1.0F, 0.0F, normalX, normalY, normalZ, light);
        vertex(consumer, matrix, normal, x4, y4, z4, 1.0F, 1.0F, normalX, normalY, normalZ, light);
    }

    private void vertex(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal,
                        float x, float y, float z, float u, float v,
                        float normalX, float normalY, float normalZ, int light) {
        consumer.vertex(matrix, x, y, z)
                .color(255, 255, 255, 255)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(normal, normalX, normalY, normalZ)
                .endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(MagneticPinEntity entity) {
        return TEXTURE;
    }
}
