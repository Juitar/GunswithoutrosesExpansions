package juitar.gwrexpansions.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import juitar.gwrexpansions.item.cataclysm.RemnantFangshotItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class RemnantFangshotHudRenderer {
    private static final ResourceLocation RAGE_BAR =
            new ResourceLocation("cataclysm", "textures/gui/boss_bar/remnant_rage_bar_base.png");

    private static final int BAR_WIDTH = 48;
    private static final int BAR_HEIGHT = 5;
    private static final int TEXTURE_WIDTH = 256;
    private static final int TEXTURE_HEIGHT = 16;

    @SubscribeEvent
    public void onRenderGui(RenderGuiEvent event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.options.hideGui) {
            return;
        }

        ItemStack stack = findHeldFangshot(player);
        if (stack.isEmpty()) {
            return;
        }

        boolean awakened = RemnantFangshotItem.isAwakened(stack);
        int maxRage = RemnantFangshotItem.getMaxRage();
        int maxAwakenedTicks = RemnantFangshotItem.getMaxAwakenedTicks();
        float progress = awakened
                ? Math.min(1.0F, RemnantFangshotItem.getAwakenedTicks(stack) / (float) maxAwakenedTicks)
                : RemnantFangshotItem.getRage(stack) / (float) maxRage;

        GuiGraphics guiGraphics = event.getGuiGraphics();
        PoseStack poseStack = guiGraphics.pose();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        int x = (screenWidth - BAR_WIDTH) / 2;
        int y = screenHeight / 2 + 13;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, awakened ? 1.0F : 0.9F);

        poseStack.pushPose();
        guiGraphics.blit(RAGE_BAR, x, y, 0.0F, 0.0F, BAR_WIDTH, BAR_HEIGHT, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        int progressWidth = Math.min(BAR_WIDTH, Math.max(0, (int) Math.ceil(BAR_WIDTH * progress)));
        if (progressWidth > 0) {
            guiGraphics.blit(RAGE_BAR, x, y, 0.0F, BAR_HEIGHT, progressWidth, BAR_HEIGHT,
                    TEXTURE_WIDTH, TEXTURE_HEIGHT);
        }

        poseStack.popPose();

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }

    private static ItemStack findHeldFangshot(Player player) {
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.getItem() instanceof RemnantFangshotItem) {
            return mainHand;
        }

        ItemStack offHand = player.getOffhandItem();
        if (offHand.getItem() instanceof RemnantFangshotItem) {
            return offHand;
        }

        return ItemStack.EMPTY;
    }
}
