package juitar.gwrexpansions.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import juitar.gwrexpansions.GWRexpansions;
import juitar.gwrexpansions.entity.BOMD.ObsidianCoreEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class ObsidianCoreRenderer extends EntityRenderer<ObsidianCoreEntity> {
    // 使用原版黑曜石方块纹理
    private static final ResourceLocation TEXTURE = new ResourceLocation("minecraft", "textures/block/obsidian.png");
    // 锁链纹理
    private static final ResourceLocation CHAIN_TEXTURE = new ResourceLocation(GWRexpansions.MODID, "textures/entity/obsidian_core_chain.png");
    // 使用固定的RenderType提高性能
    private static final RenderType CHAIN_LAYER = RenderType.entityCutoutNoCull(CHAIN_TEXTURE);
    // 备用纯色渲染类型
    private static final RenderType FALLBACK_LAYER = RenderType.lines();
    
    // AOE粒子效果参数
    private static final int PARTICLE_COUNT = 32; // 粒子数量
    private static final float PARTICLE_SPEED = 0.2f; // 粒子速度
    private static final float PARTICLE_SIZE = 0.5f; // 粒子大小
    private static final int PARTICLE_LIFETIME = 20; // 粒子生命周期（tick）

    public ObsidianCoreRenderer(EntityRendererProvider.Context context) {
       super(context);
       this.shadowRadius = 0.0F;
    }

    @Override
    public void render(ObsidianCoreEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, 
                     MultiBufferSource buffer, int packedLight) {
        // 渲染黑曜石核心
        poseStack.pushPose();
        
        // 根据实体的旋转设置模型方向
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, entity.yRotO, entity.getYRot()) - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTicks, entity.xRotO, entity.getXRot()) + 90.0F));
        
        // 渲染核心
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(getTextureLocation(entity)));
        renderCore(poseStack, vertexConsumer, packedLight);
        
        poseStack.popPose();
        
        // 渲染锁链
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
        
        // 渲染AOE粒子效果
        if (entity.isAoeActive()) {
            renderAoeParticles(entity, partialTicks);
        }
        
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private void renderCore(PoseStack poseStack, VertexConsumer consumer, int light) {
        // 渲染一个3D立方体，大小为0.75（1.5倍的0.5）
       float size = 0.75f;
       float halfSize = size / 2;
       
       poseStack.translate(0, 0.125f, 0);
       
        // 前面
       consumer.vertex(poseStack.last().pose(), -halfSize, -halfSize, halfSize)
              .color(255, 255, 255, 255)
              .uv(0, 0)
              .overlayCoords(OverlayTexture.NO_OVERLAY)
              .uv2(light)
              .normal(poseStack.last().normal(), 0, 0, 1)
              .endVertex();
       consumer.vertex(poseStack.last().pose(), halfSize, -halfSize, halfSize)
              .color(255, 255, 255, 255)
              .uv(1, 0)
              .overlayCoords(OverlayTexture.NO_OVERLAY)
              .uv2(light)
              .normal(poseStack.last().normal(), 0, 0, 1)
              .endVertex();
       consumer.vertex(poseStack.last().pose(), halfSize, halfSize, halfSize)
              .color(255, 255, 255, 255)
              .uv(1, 1)
              .overlayCoords(OverlayTexture.NO_OVERLAY)
              .uv2(light)
              .normal(poseStack.last().normal(), 0, 0, 1)
              .endVertex();
       consumer.vertex(poseStack.last().pose(), -halfSize, halfSize, halfSize)
              .color(255, 255, 255, 255)
              .uv(0, 1)
              .overlayCoords(OverlayTexture.NO_OVERLAY)
              .uv2(light)
              .normal(poseStack.last().normal(), 0, 0, 1)
              .endVertex();

        // 后面
       consumer.vertex(poseStack.last().pose(), -halfSize, -halfSize, -halfSize)
              .color(255, 255, 255, 255)
              .uv(0, 0)
              .overlayCoords(OverlayTexture.NO_OVERLAY)
              .uv2(light)
              .normal(poseStack.last().normal(), 0, 0, -1)
              .endVertex();
       consumer.vertex(poseStack.last().pose(), -halfSize, halfSize, -halfSize)
              .color(255, 255, 255, 255)
              .uv(0, 1)
              .overlayCoords(OverlayTexture.NO_OVERLAY)
              .uv2(light)
              .normal(poseStack.last().normal(), 0, 0, -1)
              .endVertex();
       consumer.vertex(poseStack.last().pose(), halfSize, halfSize, -halfSize)
              .color(255, 255, 255, 255)
              .uv(1, 1)
              .overlayCoords(OverlayTexture.NO_OVERLAY)
              .uv2(light)
              .normal(poseStack.last().normal(), 0, 0, -1)
              .endVertex();
       consumer.vertex(poseStack.last().pose(), halfSize, -halfSize, -halfSize)
              .color(255, 255, 255, 255)
              .uv(1, 0)
              .overlayCoords(OverlayTexture.NO_OVERLAY)
              .uv2(light)
              .normal(poseStack.last().normal(), 0, 0, -1)
              .endVertex();

        // 上面
        consumer.vertex(poseStack.last().pose(), -halfSize, halfSize, -halfSize)
               .color(255, 255, 255, 255)
               .uv(0, 0)
               .overlayCoords(OverlayTexture.NO_OVERLAY)
               .uv2(light)
               .normal(poseStack.last().normal(), 0, 1, 0)
               .endVertex();
        consumer.vertex(poseStack.last().pose(), -halfSize, halfSize, halfSize)
               .color(255, 255, 255, 255)
               .uv(0, 1)
               .overlayCoords(OverlayTexture.NO_OVERLAY)
               .uv2(light)
               .normal(poseStack.last().normal(), 0, 1, 0)
               .endVertex();
        consumer.vertex(poseStack.last().pose(), halfSize, halfSize, halfSize)
               .color(255, 255, 255, 255)
               .uv(1, 1)
               .overlayCoords(OverlayTexture.NO_OVERLAY)
               .uv2(light)
               .normal(poseStack.last().normal(), 0, 1, 0)
               .endVertex();
        consumer.vertex(poseStack.last().pose(), halfSize, halfSize, -halfSize)
               .color(255, 255, 255, 255)
               .uv(1, 0)
               .overlayCoords(OverlayTexture.NO_OVERLAY)
               .uv2(light)
               .normal(poseStack.last().normal(), 0, 1, 0)
               .endVertex();

        // 下面
        consumer.vertex(poseStack.last().pose(), -halfSize, -halfSize, -halfSize)
               .color(255, 255, 255, 255)
               .uv(0, 0)
               .overlayCoords(OverlayTexture.NO_OVERLAY)
               .uv2(light)
               .normal(poseStack.last().normal(), 0, -1, 0)
               .endVertex();
        consumer.vertex(poseStack.last().pose(), halfSize, -halfSize, -halfSize)
               .color(255, 255, 255, 255)
               .uv(1, 0)
               .overlayCoords(OverlayTexture.NO_OVERLAY)
               .uv2(light)
               .normal(poseStack.last().normal(), 0, -1, 0)
               .endVertex();
        consumer.vertex(poseStack.last().pose(), halfSize, -halfSize, halfSize)
               .color(255, 255, 255, 255)
               .uv(1, 1)
               .overlayCoords(OverlayTexture.NO_OVERLAY)
               .uv2(light)
               .normal(poseStack.last().normal(), 0, -1, 0)
               .endVertex();
        consumer.vertex(poseStack.last().pose(), -halfSize, -halfSize, halfSize)
               .color(255, 255, 255, 255)
               .uv(0, 1)
               .overlayCoords(OverlayTexture.NO_OVERLAY)
               .uv2(light)
               .normal(poseStack.last().normal(), 0, -1, 0)
               .endVertex();

        // 右面
        consumer.vertex(poseStack.last().pose(), halfSize, -halfSize, -halfSize)
               .color(255, 255, 255, 255)
               .uv(0, 0)
               .overlayCoords(OverlayTexture.NO_OVERLAY)
               .uv2(light)
               .normal(poseStack.last().normal(), 1, 0, 0)
               .endVertex();
        consumer.vertex(poseStack.last().pose(), halfSize, halfSize, -halfSize)
               .color(255, 255, 255, 255)
               .uv(0, 1)
               .overlayCoords(OverlayTexture.NO_OVERLAY)
               .uv2(light)
               .normal(poseStack.last().normal(), 1, 0, 0)
               .endVertex();
        consumer.vertex(poseStack.last().pose(), halfSize, halfSize, halfSize)
               .color(255, 255, 255, 255)
               .uv(1, 1)
               .overlayCoords(OverlayTexture.NO_OVERLAY)
               .uv2(light)
               .normal(poseStack.last().normal(), 1, 0, 0)
               .endVertex();
        consumer.vertex(poseStack.last().pose(), halfSize, -halfSize, halfSize)
               .color(255, 255, 255, 255)
               .uv(1, 0)
               .overlayCoords(OverlayTexture.NO_OVERLAY)
               .uv2(light)
               .normal(poseStack.last().normal(), 1, 0, 0)
               .endVertex();

        // 左面
        consumer.vertex(poseStack.last().pose(), -halfSize, -halfSize, -halfSize)
               .color(255, 255, 255, 255)
               .uv(0, 0)
               .overlayCoords(OverlayTexture.NO_OVERLAY)
               .uv2(light)
               .normal(poseStack.last().normal(), -1, 0, 0)
               .endVertex();
        consumer.vertex(poseStack.last().pose(), -halfSize, -halfSize, halfSize)
               .color(255, 255, 255, 255)
               .uv(1, 0)
               .overlayCoords(OverlayTexture.NO_OVERLAY)
               .uv2(light)
               .normal(poseStack.last().normal(), -1, 0, 0)
               .endVertex();
        consumer.vertex(poseStack.last().pose(), -halfSize, halfSize, halfSize)
               .color(255, 255, 255, 255)
               .uv(1, 1)
               .overlayCoords(OverlayTexture.NO_OVERLAY)
               .uv2(light)
               .normal(poseStack.last().normal(), -1, 0, 0)
               .endVertex();
        consumer.vertex(poseStack.last().pose(), -halfSize, halfSize, -halfSize)
               .color(255, 255, 255, 255)
               .uv(0, 1)
               .overlayCoords(OverlayTexture.NO_OVERLAY)
               .uv2(light)
               .normal(poseStack.last().normal(), -1, 0, 0)
               .endVertex();
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
            
            x = Mth.lerp((double) partialTicks, player.xo, player.getX()) - d1 * d2 - d0 * 0.2D;
            y = player.yo + (double) player.getEyeHeight() + (player.getY() - player.yo) * (double) partialTicks - 0.45D;
            z = Mth.lerp((double) partialTicks, player.zo, player.getZ()) - d0 * d2 + d1 * 0.2D;
            f3 = (player.isCrouching() ? -0.1875F : 0.0F);
        }
        
        return new Vec3(x, y + f3, z);
    }

    private void renderAoeParticles(ObsidianCoreEntity entity, float partialTicks) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return;

        double x = entity.getX();
        double y = entity.getY();
        double z = entity.getZ();
        float aoeRadius = entity.getAoeRadius();
        int age = entity.getAoeAge();

        // 计算粒子位置和速度
        for (int i = 0; i < PARTICLE_COUNT; i++) {
            float angle = (float) (i * 2 * Math.PI / PARTICLE_COUNT);
            float distance = aoeRadius * (age / (float)PARTICLE_LIFETIME);
            
            double particleX = x + Math.cos(angle) * distance;
            double particleZ = z + Math.sin(angle) * distance;
            
            // 创建黑曜石粒子
            minecraft.particleEngine.createParticle(
                new BlockParticleOption(ParticleTypes.BLOCK, Blocks.OBSIDIAN.defaultBlockState()),
                particleX, y, particleZ,
                Math.cos(angle) * PARTICLE_SPEED,
                0.0,
                Math.sin(angle) * PARTICLE_SPEED
            );
        }
    }

    @Override
    public ResourceLocation getTextureLocation(ObsidianCoreEntity entity) {
        return TEXTURE;
    }
} 