package juitar.gwrexpansions.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import juitar.gwrexpansions.entity.meetyourfight.MirecallerBombBulletEntity;
import lykrast.meetyourfight.renderer.SwampMineModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class MirecallerBombBulletRenderer extends EntityRenderer<MirecallerBombBulletEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation("meetyourfight", "textures/entity/swampmine.png");
    private static final float MODEL_SCALE = 0.3125F;

    private final SwampMineModel model;

    public MirecallerBombBulletRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new SwampMineModel(context.bakeLayer(SwampMineModel.MODEL));
        this.shadowRadius = 0.12F;
    }

    @Override
    public void render(MirecallerBombBulletEntity entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        float yaw = Mth.rotLerp(partialTicks, entity.yRotO, entity.getYRot());
        float pitch = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - yaw));
        poseStack.mulPose(Axis.XP.rotationDegrees(-pitch));
        poseStack.scale(MODEL_SCALE, MODEL_SCALE, MODEL_SCALE);
        poseStack.translate(0.0D, -0.5D, 0.0D);

        VertexConsumer vertexConsumer = bufferSource.getBuffer(model.renderType(TEXTURE));
        model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY,
                1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(MirecallerBombBulletEntity entity) {
        return TEXTURE;
    }
}
