package juitar.gwrexpansions.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import juitar.gwrexpansions.GWRexpansions;
import juitar.gwrexpansions.config.ClientConfig;
import juitar.gwrexpansions.item.vanilla.Supershotgun;
import juitar.gwrexpansions.registry.GWRESounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

/**
 * Super Shotgun meat hook HUD.
 * Shows cooldown as a compact skill icon near the crosshair, then flashes a ready banner.
 */
public class SupershotgunHudRenderer {
    private static final ResourceLocation HOOK_HUD_ICON =
            new ResourceLocation(GWRexpansions.MODID, "textures/gui/meat_hook_hud.png");
    private static final ResourceLocation RING_SEGMENT_READY =
            new ResourceLocation(GWRexpansions.MODID, "textures/gui/meat_hook_ring_segment_ready.png");
    private static final ResourceLocation RING_SEGMENT_EMPTY =
            new ResourceLocation(GWRexpansions.MODID, "textures/gui/meat_hook_ring_segment_empty.png");
    private static final String NBT_HOOK_COOLDOWN = "Hook_Cooldown";
    private static final String NBT_COOLDOWN_PAUSED = "Cooldown_Paused";
    private static final int MAX_COOLDOWN = 100;
    private static final int READY_BANNER_TICKS = 35;
    private static final int COOLDOWN_SIZE = 28;
    private static final int ICON_TEXTURE_SIZE = 64;
    private static final int ICON_DISPLAY_SIZE = 15;
    private static final int READY_ICON_DISPLAY_SIZE = 14;
    private static final int RING_SEGMENT_TEXTURE_SIZE = 32;
    private static final int COOLDOWN_SEGMENTS = 6;
    private static final float RING_START_ANGLE_DEGREES = 30.0F;
    private static final float RING_SEGMENT_SCALE = 0.40F;
    private static final float RING_SEGMENT_DISTANCE = 9.0F;
    private static final int READY_WIDTH = 58;
    private static final int READY_HEIGHT = 30;
    private static final int HIGHLIGHT_COLOR = 0xFFFFC66D;
    private static final int READY_STREAK_COLOR = 0xFFFF8A2A;
    private static final int READY_TEXT_COLOR = 0xFFFFE2B0;

    private int readyBannerTicks;
    private boolean wasCooling;
    private int lastCooldown;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && readyBannerTicks > 0) {
            readyBannerTicks--;
        }
    }

    @SubscribeEvent
    public void onRenderGui(RenderGuiEvent event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.options.hideGui
                || !ClientConfig.getBoolean(ClientConfig.INSTANCE.superShotgunHudEnabled, true)) {
            return;
        }

        ItemStack gunStack = findHeldSuperShotgun(player);
        if (gunStack == null) {
            wasCooling = false;
            lastCooldown = 0;
            return;
        }

        int cooldown = gunStack.getOrCreateTag().getInt(NBT_HOOK_COOLDOWN);
        boolean paused = gunStack.getOrCreateTag().getBoolean(NBT_COOLDOWN_PAUSED);
        if (cooldown > 0) {
            wasCooling = true;
            lastCooldown = cooldown;
            renderCooldown(event, mc, player, cooldown, paused);
            return;
        }

        if (wasCooling && lastCooldown > 0) {
            readyBannerTicks = READY_BANNER_TICKS;
            playReadySound(player);
        }
        wasCooling = false;
        lastCooldown = 0;

        if (readyBannerTicks > 0) {
            renderReadyBanner(event, mc);
        }
    }

    private static ItemStack findHeldSuperShotgun(Player player) {
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.getItem() instanceof Supershotgun) {
            return mainHand;
        }

        ItemStack offHand = player.getOffhandItem();
        if (offHand.getItem() instanceof Supershotgun) {
            return offHand;
        }

        return null;
    }

    private static void renderCooldown(RenderGuiEvent event, Minecraft mc, Player player, int cooldown, boolean paused) {
        GuiGraphics guiGraphics = event.getGuiGraphics();
        PoseStack poseStack = guiGraphics.pose();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        double offsetX = ClientConfig.getDouble(ClientConfig.INSTANCE.superShotgunHudOffsetX, 0.0D);
        double offsetY = ClientConfig.getDouble(ClientConfig.INSTANCE.superShotgunHudOffsetY, 10.0D);
        float hudScale = getHudScale();
        int scaledSize = Math.round(COOLDOWN_SIZE * hudScale);
        HudCollisionLayout.Bounds bounds = HudCollisionLayout.claim(event, screenWidth / 2 + 22 + offsetX,
                screenHeight / 2 + 16 + offsetY, scaledSize, scaledSize, screenWidth, screenHeight);

        int x = bounds.x;
        int y = bounds.y;
        float progress = Mth.clamp(1.0F - cooldown / (float) MAX_COOLDOWN, 0.0F, 1.0F);
        float activeAlpha = paused ? 0.65F : cooldown <= 20 && player.tickCount % 8 < 4 ? 0.78F : 1.0F;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        poseStack.pushPose();
        poseStack.translate(x + bounds.fracX, y + bounds.fracY, 0.0F);
        poseStack.scale(hudScale, hudScale, 1.0F);

        drawSegmentedCooldown(guiGraphics, COOLDOWN_SIZE / 2, COOLDOWN_SIZE / 2, progress, activeAlpha);
        drawScaledIcon(guiGraphics, (COOLDOWN_SIZE - ICON_DISPLAY_SIZE) / 2,
                (COOLDOWN_SIZE - ICON_DISPLAY_SIZE) / 2, ICON_DISPLAY_SIZE / (float) ICON_TEXTURE_SIZE);

        poseStack.popPose();
        RenderSystem.disableBlend();
    }

    private void renderReadyBanner(RenderGuiEvent event, Minecraft mc) {
        GuiGraphics guiGraphics = event.getGuiGraphics();
        PoseStack poseStack = guiGraphics.pose();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        double offsetX = ClientConfig.getDouble(ClientConfig.INSTANCE.superShotgunHudOffsetX, 0.0D);
        double offsetY = ClientConfig.getDouble(ClientConfig.INSTANCE.superShotgunHudOffsetY, 10.0D);
        float hudScale = getHudScale();
        int scaledWidth = Math.round(READY_WIDTH * hudScale);
        int scaledHeight = Math.round(READY_HEIGHT * hudScale);
        HudCollisionLayout.Bounds bounds = HudCollisionLayout.claim(event, (screenWidth - scaledWidth) / 2 + offsetX,
                screenHeight / 2 + 18 + offsetY, scaledWidth, scaledHeight, screenWidth, screenHeight);

        int age = READY_BANNER_TICKS - readyBannerTicks;
        float pop = 1.0F + Math.max(0.0F, 1.0F - age / 6.0F) * 0.24F;
        int alpha = readyBannerTicks < 10 ? Mth.clamp(readyBannerTicks * 25, 0, 255) : 255;
        double centerX = bounds.x + bounds.fracX + scaledWidth / 2.0D;
        double centerY = bounds.y + bounds.fracY + scaledHeight / 2.0D;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        poseStack.pushPose();
        poseStack.translate(centerX, centerY, 0.0F);
        poseStack.scale(hudScale, hudScale, 1.0F);
        poseStack.scale(pop, pop, 1.0F);

        drawReadyStreak(guiGraphics, -25, -5, 1, withAlpha(READY_STREAK_COLOR, alpha));
        drawReadyStreak(guiGraphics, 16, -5, -1, withAlpha(READY_STREAK_COLOR, alpha));
        drawScaledIcon(guiGraphics, -READY_ICON_DISPLAY_SIZE / 2, -14,
                READY_ICON_DISPLAY_SIZE / (float) ICON_TEXTURE_SIZE);

        String text = "READY";
        int textWidth = mc.font.width(text);
        guiGraphics.drawString(mc.font, text, -textWidth / 2, 6, withAlpha(READY_TEXT_COLOR, alpha), true);
        guiGraphics.fill(-13, 18, 13, 20, withAlpha(HIGHLIGHT_COLOR, alpha));

        poseStack.popPose();
        RenderSystem.disableBlend();
    }

    private static void drawReadyStreak(GuiGraphics guiGraphics, int x, int y, int direction, int color) {
        for (int i = 0; i < 4; i++) {
            int stepX = x + direction * i * 3;
            int stepY = y - i;
            guiGraphics.fill(stepX, stepY, stepX + 6, stepY + 2, color);
        }
    }

    private static void drawSegmentedCooldown(GuiGraphics guiGraphics, int centerX, int centerY, float progress,
                                              float activeAlpha) {
        int litSegments = Math.round(progress * COOLDOWN_SEGMENTS);
        for (int i = 0; i < COOLDOWN_SEGMENTS; i++) {
            drawRingSegment(guiGraphics, RING_SEGMENT_EMPTY, centerX, centerY, i, 1.0F);
        }
        for (int i = 0; i < litSegments; i++) {
            drawRingSegment(guiGraphics, RING_SEGMENT_READY, centerX, centerY, i, activeAlpha);
        }
    }

    private static void drawRingSegment(GuiGraphics guiGraphics, ResourceLocation texture, int centerX, int centerY,
                                        int segmentIndex, float alpha) {
        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate(centerX, centerY, 0.0F);
        poseStack.mulPose(Axis.ZP.rotationDegrees(RING_START_ANGLE_DEGREES
                + segmentIndex * 360.0F / COOLDOWN_SEGMENTS));
        poseStack.translate(0.0F, -RING_SEGMENT_DISTANCE, 0.0F);
        poseStack.scale(RING_SEGMENT_SCALE, RING_SEGMENT_SCALE, 1.0F);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);
        guiGraphics.blit(texture, -RING_SEGMENT_TEXTURE_SIZE / 2, -RING_SEGMENT_TEXTURE_SIZE / 2,
                0, 0, RING_SEGMENT_TEXTURE_SIZE, RING_SEGMENT_TEXTURE_SIZE,
                RING_SEGMENT_TEXTURE_SIZE, RING_SEGMENT_TEXTURE_SIZE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();
    }

    private static void drawScaledIcon(GuiGraphics guiGraphics, int x, int y, float scale) {
        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate(x, y, 0.0F);
        poseStack.scale(scale, scale, 1.0F);
        guiGraphics.blit(HOOK_HUD_ICON, 0, 0, 0, 0, ICON_TEXTURE_SIZE, ICON_TEXTURE_SIZE,
                ICON_TEXTURE_SIZE, ICON_TEXTURE_SIZE);
        poseStack.popPose();
    }

    private static int withAlpha(int color, int alpha) {
        return (Mth.clamp(alpha, 0, 255) << 24) | (color & 0x00FFFFFF);
    }

    private static float getHudScale() {
        return (float) Mth.clamp(ClientConfig.getDouble(ClientConfig.INSTANCE.superShotgunHudScale, 1.0D),
                0.5D, 2.0D);
    }

    private static void playReadySound(Player player) {
        player.level().playLocalSound(player.getX(), player.getY(), player.getZ(),
                GWRESounds.meat_hook_ready.get(), SoundSource.PLAYERS, 0.8F, 1.15F, false);
    }
}
