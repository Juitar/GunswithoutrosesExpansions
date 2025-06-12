package juitar.gwrexpansions.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import org.joml.Vector3f;
import juitar.gwrexpansions.GWRexpansions;
import juitar.gwrexpansions.item.vanilla.Supershotgun;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class HudRenderHandler {
    // 肉钩图标资源位置
    private static final ResourceLocation MEAT_HOOK_ICON = new ResourceLocation(GWRexpansions.MODID, "textures/gui/meat_hook.png");
    // 图标尺寸
    private static final int ICON_SIZE = 8;
    // 肉钩最大距离
    private static final double MAX_HOOK_DISTANCE = 32.0D;
    // 准星检测角度（弧度）
    private static final double MAX_ANGLE = Math.toRadians(2.0D); // 减小角度，提高准确性

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGuiOverlayEvent.Post event) {
        // 确保我们在渲染十字准星后渲染
        if (event.getOverlay() != VanillaGuiOverlay.CROSSHAIR.type()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        
        if (player == null || mc.options.hideGui) {
            return;
        }

        // 检查玩家手中的物品
        ItemStack mainHandItem = player.getMainHandItem();
        ItemStack offHandItem = player.getOffhandItem();
        
        // 检查是否持有超级霰弹枪并且肉钩已冷却完成
        boolean hookReady = (mainHandItem.getItem() instanceof Supershotgun && 
                ((Supershotgun) mainHandItem.getItem()).isHookReady(mainHandItem)) ||
                (offHandItem.getItem() instanceof Supershotgun && 
                ((Supershotgun) offHandItem.getItem()).isHookReady(offHandItem));
        
        // 如果肉钩已准备好，寻找可钩取的目标
        if (hookReady) {
            // 获取玩家视线方向
            Vec3 lookVec = player.getViewVector(1.0F);
            Vec3 eyePos = player.getEyePosition();
            
            // 获取附近的实体
            List<LivingEntity> nearbyEntities = findNearbyLivingEntities(player, MAX_HOOK_DISTANCE);
            
            // 找出最接近准星的实体
            LivingEntity targetEntity = findBestTarget(player, eyePos, lookVec, nearbyEntities);
            
            // 如果找到目标实体，在其位置渲染图标
            if (targetEntity != null) {
                renderHookIconAtEntity(event.getGuiGraphics(), mc, targetEntity);
            }
        }
    }
    
    /**
     * 查找玩家附近的生物实体
     */
    private List<LivingEntity> findNearbyLivingEntities(Player player, double maxDistance) {
        List<LivingEntity> result = new ArrayList<>();
        
        // 创建搜索范围
        AABB searchBox = player.getBoundingBox().inflate(maxDistance);
        
        // 获取范围内的所有实体
        List<Entity> entities = player.level().getEntities(player, searchBox, 
                entity -> entity instanceof LivingEntity && entity != player && entity.isAlive());
        
        // 过滤并转换为LivingEntity
        for (Entity entity : entities) {
            if (entity instanceof LivingEntity livingEntity) {
                // 检查距离
                if (player.distanceTo(livingEntity) <= maxDistance) {
                    result.add(livingEntity);
                }
            }
        }
        
        return result;
    }
    
    /**
     * 检查实体是否可见（没有被方块阻挡）
     */
    private boolean isEntityVisible(Player player, LivingEntity entity) {
        // 获取玩家眼睛位置和实体位置
        Vec3 eyePos = player.getEyePosition();
        Vec3 entityPos = entity.getEyePosition();
        
        // 执行射线追踪，检查是否有方块阻挡
        BlockHitResult blockHit = player.level().clip(new ClipContext(
                eyePos, 
                entityPos, 
                ClipContext.Block.COLLIDER, 
                ClipContext.Fluid.NONE, 
                player));
        
        // 如果射线击中方块，且击中点距离玩家比实体更近，则实体被阻挡
        if (blockHit.getType() == HitResult.Type.BLOCK) {
            double blockDist = blockHit.getLocation().distanceTo(eyePos);
            double entityDist = entityPos.distanceTo(eyePos);
            return blockDist >= entityDist;
        }
        
        return true;
    }
    
    /**
     * 找出最接近准星的实体
     */
    private LivingEntity findBestTarget(Player player, Vec3 eyePos, Vec3 lookVec, List<LivingEntity> entities) {
        if (entities.isEmpty()) {
            return null;
        }
        
        // 首先尝试使用Minecraft的射线追踪找到直接瞄准的实体
        HitResult hitResult = Minecraft.getInstance().hitResult;
        if (hitResult instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof LivingEntity) {
            LivingEntity hitEntity = (LivingEntity) entityHit.getEntity();
            // 确保实体在列表中且在有效距离内
            if (entities.contains(hitEntity) && player.distanceTo(hitEntity) <= MAX_HOOK_DISTANCE) {
                return hitEntity;
            }
        }
        
        // 如果没有直接瞄准的实体，找出视线角度最小且可见的实体
        LivingEntity bestTarget = null;
        double smallestAngle = MAX_ANGLE;
        
        for (LivingEntity entity : entities) {
            // 检查实体是否可见
            if (!isEntityVisible(player, entity)) {
                continue;
            }
            
            // 计算到实体的向量
            Vec3 toEntity = entity.getEyePosition().subtract(eyePos).normalize();
            
            // 计算与视线的夹角
            double dot = lookVec.dot(toEntity);
            double angle = Math.acos(dot);
            
            // 如果角度小于当前最小角度，更新最佳目标
            if (angle < smallestAngle) {
                smallestAngle = angle;
                bestTarget = entity;
            }
        }
        
        return bestTarget;
    }
    
    /**
     * 在实体位置渲染肉钩图标
     */
    private void renderHookIconAtEntity(GuiGraphics guiGraphics, Minecraft mc, LivingEntity entity) {
        // 使用Minecraft的项目工具将3D坐标转换为2D屏幕坐标
        Vec3 entityPos = entity.getEyePosition();
        
        // 获取屏幕尺寸
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        
        // 使用更精确的3D到2D转换
        boolean visible = false;
        net.minecraft.client.renderer.Rect2i rect = null;
        
        // 使用Minecraft的实体渲染器获取实体在屏幕上的位置
        net.minecraft.client.renderer.entity.EntityRenderDispatcher dispatcher = mc.getEntityRenderDispatcher();
        
        // 获取相机信息
        Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();
        
        // 计算实体相对于相机的位置
        double dx = entityPos.x - cameraPos.x;
        double dy = entityPos.y - cameraPos.y;
        double dz = entityPos.z - cameraPos.z;
        
        // 计算实体距离
        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
        
        // 如果实体太远，不渲染
        if (distance > MAX_HOOK_DISTANCE) {
            return;
        }
        
        // 使用更简单但更可靠的方法计算屏幕坐标
        // 获取视角矩阵
        PoseStack poseStack = new PoseStack();
        poseStack.pushPose();
        
        // 使用简单的视角计算
        Vector3f cameraLookVector = mc.gameRenderer.getMainCamera().getLookVector();
        Vec3 lookVec = new Vec3(cameraLookVector.x, cameraLookVector.y, cameraLookVector.z);
        
        // 计算到目标的向量
        Vec3 toTarget = entityPos.subtract(cameraPos).normalize();
        
        // 计算点积（确定实体是否在视野前方）
        double dot = lookVec.dot(toTarget);
        
        // 如果实体在视野后方，不渲染
        if (dot < 0.5) {
            poseStack.popPose();
            return;
        }
        
        // 使用更精确的方法计算屏幕坐标
        // 获取实体的边界框
        AABB boundingBox = entity.getBoundingBox();
        Vec3 center = boundingBox.getCenter();
        
        // 计算屏幕坐标
        // 这是一个简化的计算，实际上需要考虑FOV和其他因素
        double fov = mc.options.fov().get();
        double aspectRatio = (double)screenWidth / (double)screenHeight;
        
        // 计算相对于视线的偏移
        Vec3 up = new Vec3(0, 1, 0);
        Vec3 right = lookVec.cross(up).normalize();
        up = right.cross(lookVec).normalize();
        
        // 计算投影
        double rightOffset = toTarget.dot(right);
        double upOffset = toTarget.dot(up);
        
        // 考虑FOV和距离
        double scale = Math.tan(Math.toRadians(fov * 0.5)) * 2.0;
        double adjustedScale = scale / dot;
        
        // 转换为屏幕坐标
        int screenX = (int)(screenWidth / 2 + (rightOffset / adjustedScale) * screenWidth);
        int screenY = (int)(screenHeight / 2 - (upOffset / adjustedScale) * screenHeight);
        
        // 确保图标在屏幕内
        if (screenX >= 0 && screenX <= screenWidth && screenY >= 0 && screenY <= screenHeight) {
            // 渲染图标
            RenderSystem.setShaderTexture(0, MEAT_HOOK_ICON);
            RenderSystem.enableBlend();
            // 将图标居中于计算出的位置
            guiGraphics.blit(MEAT_HOOK_ICON, screenX - ICON_SIZE / 2, screenY - ICON_SIZE / 2, 
                    0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
            RenderSystem.disableBlend();
        }
        
        poseStack.popPose();
    }
} 