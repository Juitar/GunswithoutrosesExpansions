package juitar.gwrexpansions.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import juitar.gwrexpansions.config.ClientConfig;
import juitar.gwrexpansions.item.cataclysm.CursiumGunItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CursiumSniperHudRenderer {
    private static final ResourceLocation RAGE_BAR =
            new ResourceLocation("cataclysm", "textures/gui/boss_bar/maledictus_rage_bar_base.png");

    private static final int BAR_WIDTH = 48;
    private static final int BAR_HEIGHT = 5;
    private static final int TEXTURE_WIDTH = 256;
    private static final int TEXTURE_HEIGHT = 16;

    @SubscribeEvent
    public void onRenderGui(RenderGuiEvent event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.options.hideGui
                || !ClientConfig.getBoolean(ClientConfig.INSTANCE.cursiumSniperHudEnabled, true)) {
            return;
        }

        ItemStack stack = CursiumGunItem.findHeldCursiumSniper(player);
        if (stack.isEmpty()) {
            return;
        }

        GuiGraphics guiGraphics = event.getGuiGraphics();
        PoseStack poseStack = guiGraphics.pose();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        int offsetX = ClientConfig.getInt(ClientConfig.INSTANCE.cursiumSniperHudOffsetX, 0);
        int offsetY = ClientConfig.getInt(ClientConfig.INSTANCE.cursiumSniperHudOffsetY, 23);
        HudCollisionLayout.Bounds bounds = HudCollisionLayout.claim(event, (screenWidth - BAR_WIDTH) / 2 + offsetX,
                screenHeight / 2 + offsetY, BAR_WIDTH, BAR_HEIGHT, screenWidth, screenHeight);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.95F);
        poseStack.pushPose();
        drawRage(guiGraphics, stack, bounds.x, bounds.y);
        poseStack.popPose();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }

    private static void drawRage(GuiGraphics guiGraphics, ItemStack stack, int x, int y) {
        int max = Math.max(1, CursiumGunItem.getMaxRage());
        double rage = Math.min(max, CursiumGunItem.getRage(stack));
        float progress = (float) (rage / max);

        guiGraphics.blit(RAGE_BAR, x, y, 0.0F, 0.0F, BAR_WIDTH, BAR_HEIGHT,
                TEXTURE_WIDTH, TEXTURE_HEIGHT);
        int progressWidth = Math.min(BAR_WIDTH, Math.max(0, (int) Math.ceil(BAR_WIDTH * progress)));
        if (progressWidth > 0) {
            guiGraphics.blit(RAGE_BAR, x, y, 0.0F, BAR_HEIGHT, progressWidth, BAR_HEIGHT,
                    TEXTURE_WIDTH, TEXTURE_HEIGHT);
        }
    }
}
