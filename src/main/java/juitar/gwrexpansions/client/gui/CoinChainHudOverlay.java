package juitar.gwrexpansions.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import juitar.gwrexpansions.GWRexpansions;
import juitar.gwrexpansions.client.CoinHitFeedbackClient;
import juitar.gwrexpansions.item.BOMD.Hellforge;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = GWRexpansions.MODID, value = Dist.CLIENT)
public class CoinChainHudOverlay {
    private static final int WINDOW_TICKS = 40;
    private static final int OVERHEAT_TICKS = Hellforge.COIN_OVERHEAT_TICKS;
    private static final int RANK_ICON_SIZE = 32;
    private static final int RANK_TEXTURE_WIDTH = 160;
    private static final int RANK_TEXTURE_HEIGHT = 32;
    private static final long RANK_POP_DURATION_MS = 150L;
    private static final ResourceLocation RANK_TEXTURE = new ResourceLocation(GWRexpansions.MODID, "textures/gui/hell_forge/rank.png");
    private static int lastDisplayedHits;
    private static long rankPopStartMs;

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.options.hideGui) {
            return;
        }

        ItemStack stack = getHeldHellforge(player);
        if (stack.isEmpty()) {
            resetRankPop();
            return;
        }

        CompoundTag tag = stack.getOrCreateTag();
        int hits = Math.max(tag.getInt("CoinChainHits"), CoinHitFeedbackClient.getCurrentHits());
        int timer = Math.max(tag.getInt("CoinChainTimer"), CoinHitFeedbackClient.getChainTimer());
        int overheatTimer = Math.max(tag.getInt(Hellforge.NBT_COIN_OVERHEAT_TIMER), CoinHitFeedbackClient.getOverheatTimer());
        if (CoinHitFeedbackClient.isClearSuppressed()) {
            hits = 0;
            timer = 0;
        }
        if ((hits <= 0 || timer <= 0) && overheatTimer <= 0) {
            resetRankPop();
            return;
        }

        if (hits > 0 && timer > 0 && hits != lastDisplayedHits) {
            lastDisplayedHits = hits;
            rankPopStartMs = System.currentTimeMillis();
        } else if (hits <= 0 || timer <= 0) {
            resetRankPop();
        }

        render(event.getGuiGraphics(), event.getWindow().getGuiScaledWidth(), hits, timer, overheatTimer);
    }

    private static void resetRankPop() {
        lastDisplayedHits = 0;
        rankPopStartMs = 0L;
    }

    private static ItemStack getHeldHellforge(LocalPlayer player) {
        if (player.getMainHandItem().getItem() instanceof Hellforge) {
            return player.getMainHandItem();
        }
        if (player.getOffhandItem().getItem() instanceof Hellforge) {
            return player.getOffhandItem();
        }
        return ItemStack.EMPTY;
    }

    private static void render(GuiGraphics graphics, int screenWidth, int hits, int timer, int overheatTimer) {
        Font font = Minecraft.getInstance().font;
        String grade = Hellforge.getCoinChainGrade(Math.max(1, hits));
        int x = screenWidth - 122;
        int y = 30;
        int color = getGradeColor(grade);
        boolean hasChain = hits > 0 && timer > 0;
        boolean overheated = overheatTimer > 0;

        RenderSystem.enableBlend();

        int barWidth = 72;
        int barHeight = 3;
        int barX = x + 4;
        int barY = y + 34;
        if (hasChain) {
            drawRank(graphics, font, grade, hits, x, y, color, overheated);
            int filled = Math.max(0, Math.min(barWidth, (int)(barWidth * (timer / (float)WINDOW_TICKS))));
            graphics.fill(barX, barY, barX + barWidth, barY + barHeight, 0xAA111111);
            graphics.fill(barX, barY, barX + filled, barY + barHeight, color);
        }

        if (overheated) {
            int overheatY = hasChain ? barY + 8 : y + 10;
            renderOverheat(graphics, font, barX, overheatY, barWidth, overheatTimer);
        }
        RenderSystem.disableBlend();
    }

    private static void renderCoinHitFlash(GuiGraphics graphics, int x, int y, int width, int color) {
        int flashTicks = CoinHitFeedbackClient.getFlashTicks();
        if (flashTicks <= 0) {
            return;
        }
        int alpha = Math.min(150, flashTicks * 18);
        int flashColor = (alpha << 24) | (color & 0x00FFFFFF);
        int pulseWidth = Math.min(width, 8 + flashTicks * 5);
        graphics.fill(x - 2, y - 2, x + pulseWidth, y - 1, flashColor);
        graphics.fill(x - 2, y + 4, x + pulseWidth, y + 5, flashColor);
    }
    private static void drawRank(GuiGraphics graphics, Font font, String grade, int hits, int x, int y, int color, boolean overheated) {
        String text = "\u00D7 " + hits;
        long time = System.currentTimeMillis();
        float popProgress = rankPopStartMs <= 0L ? 0.0F : Math.max(0.0F, 1.0F - ((time - rankPopStartMs) / (float)RANK_POP_DURATION_MS));
        int extraSize = Math.round(popProgress * popProgress * 8.0F);
        int drawX = x;
        int drawY = y;

        if (overheated) {
            drawX += Math.round((float)Math.sin(time / 37.0D) * 1.5F);
            drawY += Math.round((float)Math.sin(time / 85.0D) * 1.0F);
            extraSize += Math.round(Math.abs((float)Math.sin(time / 85.0D)) * 2.0F);
        }

        int iconSize = RANK_ICON_SIZE + extraSize;
        int iconX = drawX - extraSize / 2;
        int iconY = drawY - extraSize / 2;
        graphics.blit(RANK_TEXTURE, iconX, iconY, iconSize, iconSize,
            (float)getRankTextureX(grade), 0.0F, RANK_ICON_SIZE, RANK_ICON_SIZE,
            RANK_TEXTURE_WIDTH, RANK_TEXTURE_HEIGHT);
        graphics.drawString(font, text, x + RANK_ICON_SIZE + 8, y + 12, color, false);
    }

    private static void renderOverheat(GuiGraphics graphics, Font font, int x, int y, int width, int timer) {
        int capped = Math.max(0, Math.min(OVERHEAT_TICKS, timer));
        int filled = Math.max(0, Math.min(width, (int)(width * (capped / (float)OVERHEAT_TICKS))));
        int hotColor = 0xFFFF6A1A;
        graphics.drawString(font, "OVERHEAT", x - 2, y, hotColor, true);
        int barY = y + 10;
        graphics.fill(x, barY, x + width, barY + 3, 0xAA180604);
        graphics.fill(x, barY, x + filled, barY + 3, hotColor);
        graphics.fill(x, barY, x + filled, barY + 1, 0xFFFFF2A0);
    }

    private static int getRankTextureX(String grade) {
        return switch (grade) {
            case "S" -> 4 * RANK_ICON_SIZE;
            case "A" -> 3 * RANK_ICON_SIZE;
            case "B" -> 2 * RANK_ICON_SIZE;
            case "C" -> RANK_ICON_SIZE;
            default -> 0;
        };
    }

    private static int getGradeColor(String grade) {
        return switch (grade) {
            case "S" -> 0xFFD2464D;
            case "A" -> 0xFFD3BA65;
            case "B" -> 0xFF57B0D3;
            case "C" -> 0xFF21C627;
            default -> 0xFFB9B9B9;
        };
    }
}