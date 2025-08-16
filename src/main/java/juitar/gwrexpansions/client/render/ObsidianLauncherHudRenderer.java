package juitar.gwrexpansions.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import juitar.gwrexpansions.item.BOMD.ObsidianLauncher;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * 黑曜石发射器HUD渲染器
 * 在持有黑曜石发射器武器时，在准星下方显示蓄力进度条
 */
public class ObsidianLauncherHudRenderer {
    // 与ConfigurableLauncherItem类相同的常量
    private static final String TAG_USE_TICKS = "UseTicks";
    private static final int MAX_USE_TICKS = 40; // 最大蓄力时间
    
    @SubscribeEvent
    public void onRenderGui(RenderGuiEvent event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;
        
        // 检查玩家是否手持ObsidianLauncher
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        ItemStack launcherStack = null;
        
        if (mainHand.getItem() instanceof ObsidianLauncher) {
            launcherStack = mainHand;
        } else if (offHand.getItem() instanceof ObsidianLauncher) {
            launcherStack = offHand;
        }
        
        if (launcherStack == null) return;
        
        // 检查玩家是否正在使用物品
        if (!player.isUsingItem()) return;
        
        // 获取蓄力时间
        int useTicks = launcherStack.getOrCreateTag().getInt(TAG_USE_TICKS);
        
        // 计算进度
        float progress = Math.min(1.0f, useTicks / (float)MAX_USE_TICKS);
        
        // 渲染进度条
        GuiGraphics guiGraphics = event.getGuiGraphics();
        PoseStack poseStack = guiGraphics.pose();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        
        // 进度条尺寸和位置
        int barWidth = 16;
        int barHeight = 4;
        int x = (screenWidth - barWidth) / 2;
        int y = screenHeight / 2 + 10; // 准星下方
        
        // 保存当前渲染状态
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        
        // 绘制背景条
        poseStack.pushPose();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        guiGraphics.fill(x, y, x + barWidth, y + barHeight, 0x80333333);
        
        // 绘制进度条
        if (progress > 0) {
            int progressWidth = Math.max(1, (int)(barWidth * progress));
            
            // 颜色从蓝色(低蓄力)到紫色(满蓄力)
            float blue = 1.0f;
            float red = progress;
            float green = 0.0f;
            
            // 确保透明度适中
            int color = ((int)(red * 255) << 16) | ((int)(green * 255) << 8) | ((int)(blue * 255)) | (192 << 24);
            
            guiGraphics.fill(x, y, x + progressWidth, y + barHeight, color);
        }
        
        poseStack.popPose();
        
        // 恢复渲染状态
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }
} 