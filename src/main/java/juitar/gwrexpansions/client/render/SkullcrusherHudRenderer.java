package juitar.gwrexpansions.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import juitar.gwrexpansions.GWRexpansions;
import juitar.gwrexpansions.config.ClientConfig;
import juitar.gwrexpansions.item.BOMD.Skullcrusher;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * 骷髅粉碎者HUD渲染器
 * 在持有骷髅粉碎者武器时，在准星下方显示连续射击进度条
 */
public class SkullcrusherHudRenderer {
    // 使用与Skullcrusher类相同的常量
    private static final String CONSECUTIVE_TIME_KEY = "ConsecutiveShootTime";
    private static final int MAX_CONSECUTIVE_TIME = 100; // 更新为与武器类相同的值
    private static final ResourceLocation ARC_EMPTY = new ResourceLocation(GWRexpansions.MODID,
            "textures/gui/skullcrusher/arc_empty.png");
    private static final ResourceLocation ARC_CHARGED = new ResourceLocation(GWRexpansions.MODID,
            "textures/gui/skullcrusher/arc_charged.png");
    private static final ResourceLocation ARC_EMPTY_FLIPPED = new ResourceLocation(GWRexpansions.MODID,
            "textures/gui/skullcrusher/arc_empty_flipped.png");
    private static final ResourceLocation ARC_CHARGED_FLIPPED = new ResourceLocation(GWRexpansions.MODID,
            "textures/gui/skullcrusher/arc_charged_flipped.png");
    private static final int ARC_TEXTURE_WIDTH = 16;
    private static final int ARC_TEXTURE_HEIGHT = 66;
    private static final int MIN_RADIUS = 8;
    private static final int MAX_RADIUS = 15;
    private static final int HUD_BOUNDS_SIZE = 80;
    
    @SubscribeEvent
    public void onRenderGui(RenderGuiEvent event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || !ClientConfig.getBoolean(ClientConfig.INSTANCE.skullcrusherHudEnabled, true)) return;
        
        // 检查玩家是否手持Skullcrusher
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        ItemStack gunStack = null;
        
        if (mainHand.getItem() instanceof Skullcrusher) {
            gunStack = mainHand;
        } else if (offHand.getItem() instanceof Skullcrusher) {
            gunStack = offHand;
        }
        
        if (gunStack == null) return;
        
        // 获取连续射击时间
        int consecutiveTime = gunStack.getOrCreateTag().getInt(CONSECUTIVE_TIME_KEY);
        
        // 计算进度，直接使用连续射击时间的比例
        float progress = Math.min(1.0f, consecutiveTime / (float)MAX_CONSECUTIVE_TIME);
        
        // 渲染准星环形蓄能和扩散提示
        GuiGraphics guiGraphics = event.getGuiGraphics();
        PoseStack poseStack = guiGraphics.pose();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        
        double offsetX = ClientConfig.getDouble(ClientConfig.INSTANCE.skullcrusherHudOffsetX, 0.0D);
        double offsetY = ClientConfig.getDouble(ClientConfig.INSTANCE.skullcrusherHudOffsetY, 10.0D);
        HudCollisionLayout.Bounds bounds = HudCollisionLayout.claim(event,
                (screenWidth - HUD_BOUNDS_SIZE) / 2 + offsetX,
                (screenHeight - HUD_BOUNDS_SIZE) / 2.0D + offsetY,
                HUD_BOUNDS_SIZE, HUD_BOUNDS_SIZE, screenWidth, screenHeight);
        double centerX = bounds.x + bounds.fracX + HUD_BOUNDS_SIZE / 2.0D;
        double centerY = bounds.y + bounds.fracY + HUD_BOUNDS_SIZE / 2.0D
                + ClientConfig.getDouble(ClientConfig.INSTANCE.skullcrusherHudCenterYAdjust, -13.0D);
        
        // 保存当前渲染状态
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        
        poseStack.pushPose();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        double inaccuracy = Math.max(0.0D, ((Skullcrusher)gunStack.getItem()).getInaccuracy(gunStack, player));
        double spreadMultiplier = ClientConfig.getDouble(ClientConfig.INSTANCE.skullcrusherHudSpreadGapMultiplier, 0.9D);
        int radius = MIN_RADIUS + Math.min(MAX_RADIUS - MIN_RADIUS, (int)Math.round(inaccuracy * spreadMultiplier));
        float pulse = progress >= 1.0F ? 0.5F + 0.5F * (float)Math.sin((player.tickCount + event.getPartialTick()) * 0.65F) : 0.0F;
        drawSpreadChargeArc(guiGraphics, centerX, centerY, radius, progress, pulse);
        
        poseStack.popPose();
        
        // 恢复渲染状态
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }

    private static void drawSpreadChargeArc(GuiGraphics guiGraphics, double centerX, double centerY, int radius,
                                            float progress, float pulse) {
        float arcScale = (float)ClientConfig.getDouble(ClientConfig.INSTANCE.skullcrusherHudArcScale, 0.5D);
        int arcRenderWidth = Math.round(ARC_TEXTURE_WIDTH * arcScale);
        int arcRenderHeight = Math.round(ARC_TEXTURE_HEIGHT * arcScale);
        int baseGap = ClientConfig.getInt(ClientConfig.INSTANCE.skullcrusherHudBaseGap, 4);
        int leftAdjust = ClientConfig.getInt(ClientConfig.INSTANCE.skullcrusherHudLeftArcXAdjust, 0);
        int rightAdjust = ClientConfig.getInt(ClientConfig.INSTANCE.skullcrusherHudRightArcXAdjust, -1);
        int centerGap = baseGap + radius;
        double y = centerY - arcRenderHeight / 2.0D;
        double rightX = centerX + centerGap + rightAdjust;
        double leftX = centerX - centerGap - arcRenderWidth + leftAdjust;

        drawArcTexture(guiGraphics, leftX, y, ARC_EMPTY, arcScale, 1.0F);
        drawArcTexture(guiGraphics, rightX, y, ARC_EMPTY_FLIPPED, arcScale, 1.0F);

        if (progress > 0.0F) {
            float alpha = Math.min(1.0F, 0.72F + progress * 0.28F + pulse * 0.12F);
            drawChargedArcTexture(guiGraphics, leftX, y, ARC_CHARGED, arcScale, progress, alpha);
            drawChargedArcTexture(guiGraphics, rightX, y, ARC_CHARGED_FLIPPED, arcScale, progress, alpha);
        }
    }

    private static void drawChargedArcTexture(GuiGraphics guiGraphics, double x, double y, ResourceLocation texture,
                                              float arcScale, float progress, float alpha) {
        int fillHeight = Math.max(1, Math.round(ARC_TEXTURE_HEIGHT * Math.min(1.0F, progress)));
        int sourceY = ARC_TEXTURE_HEIGHT - fillHeight;
        double drawY = y + sourceY * arcScale;
        drawArcTextureRegion(guiGraphics, x, drawY, texture, sourceY, fillHeight, arcScale, alpha);
    }

    private static void drawArcTexture(GuiGraphics guiGraphics, double x, double y, ResourceLocation texture,
                                       float arcScale, float alpha) {
        drawArcTextureRegion(guiGraphics, x, y, texture, 0, ARC_TEXTURE_HEIGHT, arcScale, alpha);
    }

    private static void drawArcTextureRegion(GuiGraphics guiGraphics, double x, double y, ResourceLocation texture,
                                             int sourceY, int sourceHeight, float arcScale, float alpha) {
        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
        poseStack.translate(x, y, 0.0F);
        poseStack.scale(arcScale, arcScale, 1.0F);
        guiGraphics.blit(texture, 0, 0, 0, sourceY, ARC_TEXTURE_WIDTH, sourceHeight,
                ARC_TEXTURE_WIDTH, ARC_TEXTURE_HEIGHT);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();
    }
} 
