package juitar.gwrexpansions.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import juitar.gwrexpansions.GWRexpansions;
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
    
    // 不再需要MAX_REDUCTION_THRESHOLD，进度条现在直接使用连续射击时间的比例
    
    @SubscribeEvent
    public void onRenderGui(RenderGuiEvent event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;
        
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
        
        // 渲染进度条
        GuiGraphics guiGraphics = event.getGuiGraphics();
        PoseStack poseStack = guiGraphics.pose();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        
        // 进度条尺寸和位置
        int barWidth = 15;
        int barHeight = 2;
        int x = (screenWidth - barWidth) / 2;
        int y = screenHeight / 2 + 10; // 准星下方更近
        
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
            
            // 修正颜色计算，确保从绿色到红色的平滑过渡
            // 从0%进度(绿色)到100%进度(红色)
            float red = progress;
            float green = 1.0f - progress;
            
            // 确保透明度适中
            int color = ((int)(red * 255) << 16) | ((int)(green * 255) << 8) | 0 | (192 << 24);
            
            guiGraphics.fill(x, y, x + progressWidth, y + barHeight, color);
        }
        
        poseStack.popPose();
        
        // 恢复渲染状态
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }
} 