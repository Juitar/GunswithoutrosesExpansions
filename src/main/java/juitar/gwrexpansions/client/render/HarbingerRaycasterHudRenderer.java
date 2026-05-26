package juitar.gwrexpansions.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import juitar.gwrexpansions.config.ClientConfig;
import juitar.gwrexpansions.item.cataclysm.HarbingerRaycasterItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class HarbingerRaycasterHudRenderer {
    private static final int BODY_WIDTH = 44;
    private static final int BODY_HEIGHT = 9;
    private static final int NUB_WIDTH = 2;
    private static final int INNER_X = 3;
    private static final int INNER_Y = 2;
    private static final int INNER_WIDTH = 36;
    private static final int INNER_HEIGHT = 5;

    private static final int FRAME_COLOR = 0xCC2B1414;
    private static final int FRAME_HIGHLIGHT = 0xCC8E3C24;
    private static final int EMPTY_COLOR = 0x80321418;
    private static final int CHARGED_COLOR = 0xFFE3241F;
    private static final int CHARGED_HIGHLIGHT = 0xFFFF8E3D;
    private static final int OVERLOAD_COLOR = 0xFFFFD15C;

    @SubscribeEvent
    public void onRenderGui(RenderGuiEvent event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.options.hideGui || !isEnabled()) {
            return;
        }

        ItemStack stack = findHeldRaycaster(player);
        if (stack.isEmpty()) {
            return;
        }

        int maxOverload = HarbingerRaycasterItem.getMaxOverload();
        int overload = Math.min(maxOverload, HarbingerRaycasterItem.getHudOverload(stack));
        boolean active = HarbingerRaycasterItem.isOverloadActive(stack);
        double activeProgress = active
                ? HarbingerRaycasterItem.getOverloadActiveTicks(stack)
                / (double) HarbingerRaycasterItem.getOverloadDurationTicks()
                : 0.0D;

        GuiGraphics guiGraphics = event.getGuiGraphics();
        PoseStack poseStack = guiGraphics.pose();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        HudCollisionLayout.Bounds bounds = HudCollisionLayout.claim(event,
                (screenWidth - BODY_WIDTH - NUB_WIDTH) / 2 + getOffsetX(),
                screenHeight / 2 + getOffsetY(), BODY_WIDTH + NUB_WIDTH, BODY_HEIGHT,
                screenWidth, screenHeight);
        int x = bounds.x;
        int y = bounds.y;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        poseStack.pushPose();
        drawBattery(guiGraphics, x, y, overload, maxOverload, active, activeProgress);
        poseStack.popPose();
        RenderSystem.disableBlend();
    }

    private static void drawBattery(GuiGraphics guiGraphics, int x, int y, int overload, int maxOverload,
                                    boolean active, double activeProgress) {
        guiGraphics.fill(x + 1, y, x + BODY_WIDTH - 1, y + 1, FRAME_HIGHLIGHT);
        guiGraphics.fill(x, y + 1, x + BODY_WIDTH, y + BODY_HEIGHT - 1, FRAME_COLOR);
        guiGraphics.fill(x + 1, y + BODY_HEIGHT - 1, x + BODY_WIDTH - 1, y + BODY_HEIGHT, FRAME_COLOR);
        guiGraphics.fill(x + BODY_WIDTH, y + 3, x + BODY_WIDTH + NUB_WIDTH, y + 6, FRAME_COLOR);
        guiGraphics.fill(x + INNER_X, y + INNER_Y, x + INNER_X + INNER_WIDTH, y + INNER_Y + INNER_HEIGHT,
                EMPTY_COLOR);

        int chargeColor = active ? OVERLOAD_COLOR : CHARGED_COLOR;
        int activeFillWidth = active
                ? Math.max(0, Math.min(INNER_WIDTH, (int) Math.ceil(INNER_WIDTH * activeProgress)))
                : 0;
        for (int i = 0; i < maxOverload; i++) {
            int start = x + INNER_X + (int) Math.floor(i * (INNER_WIDTH / (double) maxOverload));
            int end = x + INNER_X + (int) Math.floor((i + 1) * (INNER_WIDTH / (double) maxOverload));
            if (end <= start) {
                end = start + 1;
            }

            if (active) {
                int activeEnd = x + INNER_X + activeFillWidth;
                int fillStart = start + (end - start > 2 ? 1 : 0);
                int fillEnd = Math.min(activeEnd, end - (end - start > 2 ? 1 : 0));
                if (fillEnd > fillStart) {
                    guiGraphics.fill(fillStart, y + INNER_Y + 1, fillEnd, y + INNER_Y + INNER_HEIGHT - 1,
                            chargeColor);
                }
            } else if (i < overload) {
                int fillStart = start + (end - start > 2 ? 1 : 0);
                int fillEnd = end - (end - start > 2 ? 1 : 0);
                guiGraphics.fill(fillStart, y + INNER_Y + 1, fillEnd, y + INNER_Y + INNER_HEIGHT - 1, chargeColor);
                if (!active && fillEnd - fillStart > 2) {
                    guiGraphics.fill(fillStart, y + INNER_Y + 1, fillEnd, y + INNER_Y + 2, CHARGED_HIGHLIGHT);
                }
            }

            if (i > 0 && end - start > 2) {
                guiGraphics.fill(start, y + INNER_Y, start + 1, y + INNER_Y + INNER_HEIGHT, FRAME_COLOR);
            }
        }
    }

    private static ItemStack findHeldRaycaster(Player player) {
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.getItem() instanceof HarbingerRaycasterItem) {
            return mainHand;
        }

        ItemStack offHand = player.getOffhandItem();
        if (offHand.getItem() instanceof HarbingerRaycasterItem) {
            return offHand;
        }

        return ItemStack.EMPTY;
    }

    private static boolean isEnabled() {
        return ClientConfig.getBoolean(ClientConfig.INSTANCE.harbingerOverloadHudEnabled, true);
    }

    private static int getOffsetX() {
        return ClientConfig.getInt(ClientConfig.INSTANCE.harbingerOverloadHudOffsetX, 0);
    }

    private static int getOffsetY() {
        return ClientConfig.getInt(ClientConfig.INSTANCE.harbingerOverloadHudOffsetY, 32);
    }
}
