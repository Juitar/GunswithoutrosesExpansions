package juitar.gwrexpansions.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import juitar.gwrexpansions.GWRexpansions;
import juitar.gwrexpansions.client.model.MeatHookModel;
import juitar.gwrexpansions.entity.MeatHookEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MeatHookRenderer extends EntityRenderer<MeatHookEntity> {
    private static final Logger LOGGER = LoggerFactory.getLogger(MeatHookRenderer.class);
    // 使用一个通用的纹理资源或创建一个自定义的肉钩纹理
    private static final ResourceLocation TEXTURE = new ResourceLocation(GWRexpansions.MODID, "textures/entity/meat_hook.png");
    // 链条纹理
    private static final ResourceLocation CHAIN_TEXTURE = new ResourceLocation(GWRexpansions.MODID, "textures/entity/meat_hook_chain.png");
    // 肉钩尺寸
    private static final float HOOK_SIZE = 1.5f;
    // 使用固定的RenderType提高性能
    private static final RenderType CHAIN_LAYER = RenderType.entityCutoutNoCull(CHAIN_TEXTURE);
    // 备用纯色渲染类型，不依赖纹理
    private static final RenderType FALLBACK_LAYER = RenderType.lines();
    // 调试模式
    private static final boolean DEBUG = true;
    
    // 肉钩模型
    private final MeatHookModel model;

    public MeatHookRenderer(EntityRendererProvider.Context context) {
        super(context);
        // 设置阴影大小为0，不需要阴影
        this.shadowRadius = 0.0F;
        // 创建模型实例
        this.model = new MeatHookModel();

    }

    @Override
    public void render(MeatHookEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, 
                      MultiBufferSource buffer, int packedLight) {
        
        // 渲染3D肉钩模型
        poseStack.pushPose();
        
        // 根据实体的旋转设置模型方向 - 按照参考代码的方式
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, entity.yRotO, entity.getYRot()) - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTicks, entity.xRotO, entity.getXRot()) + 90.0F));
        
        // 缩放模型
        poseStack.scale(HOOK_SIZE, HOOK_SIZE, HOOK_SIZE);
        
        // 直接使用模型渲染
        VertexConsumer vertexConsumer = buffer.getBuffer(model.renderType(getTextureLocation(entity)));
        model.renderToBuffer(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        
        poseStack.popPose();
        
        // 渲染链条
        Entity owner = entity.getOwner();
        if (owner != null) {
                poseStack.pushPose();
                
                float entityX = (float)Mth.lerp(partialTicks, entity.xo, entity.getX());
                float entityY = (float)Mth.lerp(partialTicks, entity.yo, entity.getY());
                float entityZ = (float)Mth.lerp(partialTicks, entity.zo, entity.getZ());
                
                Vec3 ownerPos = getOwnerHandPosition(owner, partialTicks);
                Vec3 distVec = new Vec3(ownerPos.x - entityX, ownerPos.y - entityY, ownerPos.z - entityZ);
                
                renderChainCube(distVec, partialTicks, entity.tickCount, poseStack, buffer, packedLight);
                
                poseStack.popPose();

            }
        
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    // 获取玩家手的位置（参考代码中的实现）
    private Vec3 getOwnerHandPosition(Entity owner, float partialTicks) {
        double x = Mth.lerp(partialTicks, owner.xo, owner.getX());
        double y = Mth.lerp(partialTicks, owner.yo, owner.getY());
        double z = Mth.lerp(partialTicks, owner.zo, owner.getZ());
        float f3 = 0;
        
        if (owner instanceof Player) {
            Player player = (Player) owner;
            float f = player.getAttackAnim(partialTicks);
            float f1 = Mth.sin(Mth.sqrt(f) * (float) Math.PI);
            float f2 = Mth.lerp(partialTicks, player.yBodyRotO, player.yBodyRot) * ((float) Math.PI / 180F);
            int i = 1; // 假设主手是右手
            
            double d0 = (double) Mth.sin(f2);
            double d1 = (double) Mth.cos(f2);
            double d2 = (double) i * 0.35D;
            
            if (this.entityRenderDispatcher.options.getCameraType().isFirstPerson() && player == Minecraft.getInstance().player) {
                double d7 = 960.0D / (double) this.entityRenderDispatcher.options.fov().get();
                Vec3 vec3 = this.entityRenderDispatcher.camera.getNearPlane().getPointOnPlane((float) i * 0.6F, -1);
                vec3 = vec3.scale(d7);
                vec3 = vec3.yRot(f1 * 0.25F);
                vec3 = vec3.xRot(-f1 * 0.35F);
                x = Mth.lerp((double) partialTicks, player.xo, player.getX()) + vec3.x;
                y = Mth.lerp((double) partialTicks, player.yo, player.getY()) + vec3.y;
                z = Mth.lerp((double) partialTicks, player.zo, player.getZ()) + vec3.z;
                f3 = player.getEyeHeight() * 0.5F;
            } else {
                x = Mth.lerp((double) partialTicks, player.xo, player.getX()) - d1 * d2 - d0 * 0.2D;
                y = player.yo + (double) player.getEyeHeight() + (player.getY() - player.yo) * (double) partialTicks - 0.45D;
                z = Mth.lerp((double) partialTicks, player.zo, player.getZ()) - d0 * d2 + d1 * 0.2D;
                f3 = (player.isCrouching() ? -0.1875F : 0.0F);
            }
        }
        
        return new Vec3(x, y + f3, z);
    }

    private void renderChainCube(Vec3 from, float partialTicks, int age, PoseStack stack, MultiBufferSource provider, int light) {
        float lengthXY = Mth.sqrt((float) (from.x * from.x + from.z * from.z));
        float squaredLength = (float) (from.x * from.x + from.y * from.y + from.z * from.z);
        float length = Mth.sqrt(squaredLength);

        stack.pushPose();
        stack.mulPose(Axis.YP.rotation((float) (-Math.atan2(from.z, from.x)) - 1.5707964F));
        stack.mulPose(Axis.XP.rotation((float) (-Math.atan2(lengthXY, from.y)) - 1.5707964F));
        stack.mulPose(Axis.ZP.rotationDegrees(25));
        stack.pushPose();
        stack.translate(0.015, -0.2, 0);

        VertexConsumer vertexConsumer = provider.getBuffer(CHAIN_LAYER);
        float vertX1 = 0F;
        float vertY1 = 0.25F;
        float vertX2 = Mth.sin(6.2831855F) * 0.125F;
        float vertY2 = Mth.cos(6.2831855F) * 0.125F;
        float minU = 0F;
        float maxU = 0.1875F;
        float minV = 0.0F - ((float) age + partialTicks) * 0.01F;
        float maxV = Mth.sqrt(squaredLength) / 8F - ((float) age + partialTicks) * 0.01F;
        PoseStack.Pose entry = stack.last();
        Matrix4f matrix4f = entry.pose();
        Matrix3f matrix3f = entry.normal();

        vertexConsumer.vertex(matrix4f, vertX1, vertY1, 0F).color(255, 255, 255, 255).uv(minU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(matrix3f, 0.0F, -1.0F, 0.0F).endVertex();
        vertexConsumer.vertex(matrix4f, vertX1, vertY1, length).color(255, 255, 255, 255).uv(minU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(matrix3f, 0.0F, -1.0F, 0.0F).endVertex();
        vertexConsumer.vertex(matrix4f, vertX2, vertY2, length).color(255, 255, 255, 255).uv(maxU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(matrix3f, 0.0F, -1.0F, 0.0F).endVertex();
        vertexConsumer.vertex(matrix4f, vertX2, vertY2, 0F).color(255, 255, 255, 255).uv(maxU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(matrix3f, 0.0F, -1.0F, 0.0F).endVertex();

        stack.popPose();
        stack.mulPose(Axis.ZP.rotationDegrees(90));
        stack.translate(-0.015, -0.2, 0);

        entry = stack.last();
        matrix4f = entry.pose();
        matrix3f = entry.normal();

        vertexConsumer.vertex(matrix4f, vertX1, vertY1, 0F).color(255, 255, 255, 255).uv(minU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(matrix3f, 0.0F, -1.0F, 0.0F).endVertex();
        vertexConsumer.vertex(matrix4f, vertX1, vertY1, length).color(255, 255, 255, 255).uv(minU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(matrix3f, 0.0F, -1.0F, 0.0F).endVertex();
        vertexConsumer.vertex(matrix4f, vertX2, vertY2, length).color(255, 255, 255, 255).uv(maxU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(matrix3f, 0.0F, -1.0F, 0.0F).endVertex();
        vertexConsumer.vertex(matrix4f, vertX2, vertY2, 0F).color(255, 255, 255, 255).uv(maxU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(matrix3f, 0.0F, -1.0F, 0.0F).endVertex();

        stack.popPose();

    }


    @Override
    public ResourceLocation getTextureLocation(MeatHookEntity entity) {
        return TEXTURE;
    }
    
    // 确保肉钩在任何距离都可见
    @Override
    public boolean shouldRender(MeatHookEntity entity, net.minecraft.client.renderer.culling.Frustum frustum, double camX, double camY, double camZ) {
        if (DEBUG) {
            LOGGER.info("shouldRender调用: {}", entity.getId());
        }
        return true;
    }
} 