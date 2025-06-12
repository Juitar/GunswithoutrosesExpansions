package juitar.gwrexpansions.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import juitar.gwrexpansions.GWRexpansions;
import juitar.gwrexpansions.item.vanilla.Supershotgun;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * 超级霰弹枪HUD渲染器
 * 在持有超级霰弹枪时，在准星下方显示肉钩冷却进度条
 * 只在冷却中显示，冷却完成后不显示
 */
public class SupershotgunHudRenderer {
    // 肉钩冷却NBT键
    private static final String NBT_HOOK_COOLDOWN = "Hook_Cooldown";
    private static final String NBT_COOLDOWN_PAUSED = "Cooldown_Paused";
    private static final int MAX_COOLDOWN = 100; // 最大冷却时间
    
    @SubscribeEvent
    public void onRenderGui(RenderGuiEvent event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;
        
        // 检查玩家是否手持超级霰弹枪
        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();
        ItemStack gunStack = null;
        
        if (mainHand.getItem() instanceof Supershotgun) {
            gunStack = mainHand;
        } else if (offHand.getItem() instanceof Supershotgun) {
            gunStack = offHand;
        }
        
        if (gunStack == null) return;
        
        // 获取冷却时间
        int cooldown = gunStack.getOrCreateTag().getInt(NBT_HOOK_COOLDOWN);
        boolean isPaused = gunStack.getOrCreateTag().getBoolean(NBT_COOLDOWN_PAUSED);
        
        // 如果没有冷却或冷却已完成，不显示进度条
        if (cooldown <= 0) return;
        
        // 计算进度，从0.0（刚开始冷却）到1.0（冷却完成）
        float progress = 1.0f - (cooldown / (float)MAX_COOLDOWN);
        
        // 渲染进度条
        GuiGraphics guiGraphics = event.getGuiGraphics();
        PoseStack poseStack = guiGraphics.pose();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        
        // 进度条尺寸和位置
        int barWidth = 15;
        int barHeight = 2;
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
            
            // 颜色计算，从红色（刚开始冷却）到绿色（接近完成）
            float red = 1.0f - progress;
            float green = progress;
            
            // 如果冷却暂停，使用黄色
            if (isPaused) {
                red = 1.0f;
                green = 1.0f;
            }
            
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