package juitar.gwrexpansions.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import juitar.gwrexpansions.config.ClientConfig;
import juitar.gwrexpansions.item.cataclysm.TidalGunItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class TidalPistolHudRenderer {
    private static final int BAR_WIDTH = 52;
    private static final int BAR_HEIGHT = 7;
    private static final int FRAME_COLOR = 0xCC1D1230;
    private static final int EMPTY_COLOR = 0x8033214D;
    private static final int ENERGY_COLOR = 0xFF9C4DFF;
    private static final int ENERGY_HIGHLIGHT = 0xFFE2C7FF;
    private static final int PORTAL_COLOR = 0xFF6B6BFF;
    private static final int RIFT_COLOR = 0xFFFFF2A6;

    @SubscribeEvent
    public void onRenderGui(RenderGuiEvent event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.options.hideGui
                || !ClientConfig.getBoolean(ClientConfig.INSTANCE.tidalPistolHudEnabled, true)) {
            return;
        }

        ItemStack stack = TidalGunItem.findHeldTidalPistol(player);
        if (stack.isEmpty() || !TidalGunItem.hasEnergyHud(stack)) {
            return;
        }

        GuiGraphics guiGraphics = event.getGuiGraphics();
        PoseStack poseStack = guiGraphics.pose();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        double offsetX = ClientConfig.getDouble(ClientConfig.INSTANCE.tidalPistolHudOffsetX, 0.0D);
        double offsetY = ClientConfig.getDouble(ClientConfig.INSTANCE.tidalPistolHudOffsetY, 42.0D);
        HudCollisionLayout.Bounds bounds = HudCollisionLayout.claim(event, (screenWidth - BAR_WIDTH) / 2 + offsetX,
                screenHeight / 2 + offsetY, BAR_WIDTH, BAR_HEIGHT + 5, screenWidth, screenHeight);
        int x = bounds.x;
        int y = bounds.y;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        poseStack.pushPose();
        poseStack.translate(bounds.fracX, bounds.fracY, 0.0F);
        drawEnergy(guiGraphics, stack, x, y);
        drawChargeStage(guiGraphics, stack, x, y + BAR_HEIGHT + 3);
        poseStack.popPose();
        RenderSystem.disableBlend();
    }

    private static void drawEnergy(GuiGraphics guiGraphics, ItemStack stack, int x, int y) {
        int max = Math.max(1, TidalGunItem.getMaxEnergy());
        int energy = Math.min(max, TidalGunItem.getEnergy(stack));
        int fill = (int) Math.round((BAR_WIDTH - 4) * (energy / (double) max));

        guiGraphics.fill(x, y, x + BAR_WIDTH, y + BAR_HEIGHT, FRAME_COLOR);
        guiGraphics.fill(x + 2, y + 2, x + BAR_WIDTH - 2, y + BAR_HEIGHT - 2, EMPTY_COLOR);
        if (fill > 0) {
            guiGraphics.fill(x + 2, y + 2, x + 2 + fill, y + BAR_HEIGHT - 2, ENERGY_COLOR);
            guiGraphics.fill(x + 2, y + 2, x + 2 + fill, y + 3, ENERGY_HIGHLIGHT);
        }
    }

    private static void drawChargeStage(GuiGraphics guiGraphics, ItemStack stack, int x, int y) {
        int charge = TidalGunItem.getHudChargeTicks(stack);
        int portalTicks = TidalGunItem.tidalConfig().portalChargeTicks.get();
        int riftTicks = TidalGunItem.tidalConfig().riftChargeTicks.get();
        int center = x + BAR_WIDTH / 2;

        guiGraphics.fill(center - 10, y, center - 4, y + 2, charge >= portalTicks ? PORTAL_COLOR : EMPTY_COLOR);
        guiGraphics.fill(center + 4, y, center + 10, y + 2, charge >= riftTicks ? RIFT_COLOR : EMPTY_COLOR);
    }
}
