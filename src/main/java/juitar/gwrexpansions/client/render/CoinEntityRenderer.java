package juitar.gwrexpansions.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import juitar.gwrexpansions.GWRexpansions;
import juitar.gwrexpansions.client.model.coin;
import juitar.gwrexpansions.entity.BOMD.CoinEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/**
 * 硬币实体渲染器
 */
public class CoinEntityRenderer extends EntityRenderer<CoinEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(GWRexpansions.MODID, "textures/entity/coin.png");
    private final coin<CoinEntity> model;

    public CoinEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new coin<>(context.bakeLayer(coin.LAYER_LOCATION));
    }
    
    @Override
    public void render(CoinEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        // 修正Y轴偏移 - 与碰撞体积对齐
        poseStack.translate(0.0, 0.0, 0.0);
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, entity.yRotO, entity.getYRot()) - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTicks, entity.xRotO, entity.getXRot())));

        // 添加旋转动画
        float rotation = (entity.tickCount + partialTicks) * 10.0F;
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));

        // 缩放 - 稍微增大一点便于击中
        poseStack.scale(0.6F, 0.6F, 0.6F);

        // 使用模型渲染硬币 - 使用环境光照
        model.renderToBuffer(
            poseStack,
            buffer.getBuffer(model.renderType(getTextureLocation(entity))),
            15728880, // 使用最大光照值 (240, 240) 避免过暗
            0, // 正常overlay
            1.0F, 1.0F, 1.0F, 1.0F // RGBA: 白色，完全不透明
        );

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
    
    @Override
    public ResourceLocation getTextureLocation(CoinEntity entity) {
        return TEXTURE;
    }
}
