package juitar.gwrexpansions.client.render;

import com.github.L_Ender.cataclysm.client.render.CMRenderTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import juitar.gwrexpansions.entity.cataclysm.TidalRiftEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class TidalRiftRenderer extends EntityRenderer<TidalRiftEntity> {
    private static final ResourceLocation TEXTURE_IDLE_1 = texture("dimensional_rift_idle1.png");
    private static final ResourceLocation TEXTURE_IDLE_2 = texture("dimensional_rift_idle2.png");
    private static final ResourceLocation TEXTURE_IDLE_3 = texture("dimensional_rift_idle3.png");
    private static final ResourceLocation TEXTURE_IDLE_4 = texture("dimensional_rift_idle4.png");
    private static final ResourceLocation TEXTURE_GROW_1 = texture("dimensional_rift_grow_0.png");
    private static final ResourceLocation TEXTURE_GROW_2 = texture("dimensional_rift_grow_1.png");
    private static final ResourceLocation TEXTURE_GROW_3 = texture("dimensional_rift_grow_2.png");
    private static final ResourceLocation TEXTURE_GROW_4 = texture("dimensional_rift_grow_3.png");

    public TidalRiftRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(TidalRiftEntity entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        ResourceLocation texture = getRiftTexture(entity);

        poseStack.pushPose();
        poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.scale(7.0F, 7.0F, 7.0F);

        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();
        VertexConsumer consumer = buffer.getBuffer(CMRenderTypes.getfullBright(texture));

        vertex(consumer, matrix, normal, packedLight, 0.0F, 0, 0, 1);
        vertex(consumer, matrix, normal, packedLight, 1.0F, 0, 1, 1);
        vertex(consumer, matrix, normal, packedLight, 1.0F, 1, 1, 0);
        vertex(consumer, matrix, normal, packedLight, 0.0F, 1, 0, 0);
        poseStack.popPose();

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal, int light,
                               float x, int y, int u, int v) {
        consumer.vertex(matrix, x - 0.5F, y - 0.25F, 0.0F)
                .color(255, 255, 255, 255)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(normal, 0.0F, 1.0F, 0.0F)
                .endVertex();
    }

    private ResourceLocation getRiftTexture(TidalRiftEntity entity) {
        int stage = entity.getStage();
        if (stage < 1) {
            return TEXTURE_GROW_1;
        }
        if (stage < 2) {
            return TEXTURE_GROW_2;
        }
        if (stage < 3) {
            return TEXTURE_GROW_3;
        }
        if (stage < 4) {
            return TEXTURE_GROW_4;
        }
        return getIdleTexture(entity.tickCount % 9);
    }

    public ResourceLocation getIdleTexture(int age) {
        if (age < 3) {
            return TEXTURE_IDLE_1;
        }
        if (age < 6) {
            return TEXTURE_IDLE_2;
        }
        if (age < 10) {
            return TEXTURE_IDLE_3;
        }
        return TEXTURE_IDLE_4;
    }

    @Override
    public ResourceLocation getTextureLocation(TidalRiftEntity entity) {
        return TEXTURE_IDLE_1;
    }

    private static ResourceLocation texture(String fileName) {
        return new ResourceLocation("cataclysm", "textures/entity/leviathan/dimensional_rift/" + fileName);
    }
}
