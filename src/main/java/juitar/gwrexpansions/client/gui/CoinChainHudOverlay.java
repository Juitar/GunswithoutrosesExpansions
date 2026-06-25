package juitar.gwrexpansions.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import juitar.gwrexpansions.GWRexpansions;
import juitar.gwrexpansions.client.CoinHitFeedbackClient;
import juitar.gwrexpansions.config.ClientConfig;
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
        if (!ClientConfig.getBoolean(ClientConfig.INSTANCE.hellforgeChainHudEnabled, true)) {
            resetRankPop();
            return;
        }

        ItemStack stack = getHeldHellforge(player);
        if (stack.isEmpty()) {
            resetRankPop();
            return;
        }

        CompoundTag tag = stack.getOrCreateTag();
        int hits = Math.max(tag.getInt(Hellforge.NBT_STYLE_SCORE), CoinHitFeedbackClient.getStyleScore());
        if (hits <= 0) {
            hits = Math.max(tag.getInt(Hellforge.NBT_COIN_CHAIN_HITS), CoinHitFeedbackClient.getCurrentHits());
        }
        int timer = Math.max(tag.getInt(Hellforge.NBT_STYLE_TIMER), CoinHitFeedbackClient.getChainTimer());
        if (timer <= 0) {
            timer = Math.max(tag.getInt(Hellforge.NBT_COIN_CHAIN_TIMER), CoinHitFeedbackClient.getChainTimer());
        }
        int heat = Math.max(tag.getInt(Hellforge.NBT_STYLE_HEAT), CoinHitFeedbackClient.getStyleHeat());
        int heatKeepTimer = Math.max(tag.getInt(Hellforge.NBT_HEAT_KEEP_TIMER), CoinHitFeedbackClient.getHeatKeepTimer());
        int overheatTimer = Math.max(tag.getInt(Hellforge.NBT_COIN_OVERHEAT_TIMER), CoinHitFeedbackClient.getOverheatTimer());
        if (CoinHitFeedbackClient.isClearSuppressed()) {
            hits = 0;
            timer = 0;
        }
        if ((hits <= 0 || timer <= 0) && overheatTimer <= 0 && heat <= 0) {
            resetRankPop();
            return;
        }

        if (hits > 0 && timer > 0 && hits != lastDisplayedHits) {
            lastDisplayedHits = hits;
            rankPopStartMs = System.currentTimeMillis();
        } else if (hits <= 0 || timer <= 0) {
            resetRankPop();
        }

        render(event.getGuiGraphics(), event.getWindow().getGuiScaledWidth(), hits, timer, heat, heatKeepTimer, overheatTimer);
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

    private static void render(GuiGraphics graphics, int screenWidth, int hits, int timer, int heat, int heatKeepTimer, int overheatTimer) {
        Font font = Minecraft.getInstance().font;
        String grade = Hellforge.getCoinChainGrade(Math.max(1, hits));
        int color = getGradeColor(grade);
        boolean hasChain = hits > 0 && timer > 0;
        boolean overheated = overheatTimer > 0;

        int offsetX = ClientConfig.getInt(ClientConfig.INSTANCE.hellforgeChainHudOffsetX, 0);
        int offsetY = ClientConfig.getInt(ClientConfig.INSTANCE.hellforgeChainHudOffsetY, 30);
        float scale = Math.max(0.5F, Math.min(2.0F, ClientConfig.getInt(ClientConfig.INSTANCE.hellforgeChainHudScale, 100) / 100.0F));
        int hudWidth = 122;
        int x = screenWidth - Math.round(hudWidth * scale) + offsetX;
        int y = offsetY;

        RenderSystem.enableBlend();
        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0.0F);
        graphics.pose().scale(scale, scale, 1.0F);

        int barWidth = 72;
        int barHeight = 3;
        int barX = 4;
        int barY = 34;
        if (hasChain) {
            drawRank(graphics, font, grade, hits, 0, 0, color, overheated);
            int windowTicks = Math.max(1, Hellforge.getStyleWindowTicks(grade));
            int filled = Math.max(0, Math.min(barWidth, (int)(barWidth * (timer / (float)windowTicks))));
            if (ClientConfig.getBoolean(ClientConfig.INSTANCE.hellforgeCoinHitHudFlashEnabled, true)) {
                renderCoinHitFlash(graphics, barX, barY, barWidth, color);
            }
            graphics.fill(barX, barY, barX + barWidth, barY + barHeight, 0xAA111111);
            graphics.fill(barX, barY, barX + filled, barY + barHeight, color);
        }

        int heatY = hasChain ? barY + 8 : 10;
        if (!overheated && (heat > 0 || hasChain)) {
            renderHeat(graphics, font, barX, heatY, barWidth, heat, heatKeepTimer, grade);
        }

        if (overheated) {
            int overheatY = heatY;
            renderOverheat(graphics, font, barX, overheatY, barWidth, heat, heatKeepTimer, overheatTimer, grade);
            renderActionFeed(graphics, font, barX, overheatY + 19);
        }

        graphics.pose().popPose();
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
        graphics.drawString(font, "STYLE", x + RANK_ICON_SIZE + 7, y + 5, 0xFFB9B9B9, false);
        graphics.drawString(font, getNextRankHint(grade), x + RANK_ICON_SIZE + 7, y + 16, color, false);
    }

    private static void renderHeat(GuiGraphics graphics, Font font, int x, int y, int width, int heat, int heatKeepTimer, String grade) {
        int capped = Math.max(0, Math.min(100, heat));
        int filled = Math.max(0, Math.min(width, (int)(width * (capped / 100.0F))));
        int windowTicks = Math.max(1, Math.min(110, Hellforge.getStyleWindowTicks(grade) + 20));
        int windowFilled = Math.max(0, Math.min(width, (int)(width * (heatKeepTimer / (float)windowTicks))));
        int color = capped >= 80 ? 0xFFFF6A1A : 0xFFFFC857;
        graphics.drawString(font, "HEAT", x - 2, y, color, true);
        int barY = y + 10;
        graphics.fill(x, barY, x + width, barY + 3, 0xAA17110A);
        graphics.fill(x, barY, x + filled, barY + 3, color);
        graphics.fill(x, barY + 4, x + width, barY + 5, 0x6617110A);
        graphics.fill(x, barY + 4, x + windowFilled, barY + 5, 0xCCFFF2A0);
        if (capped >= 80) {
            int pulse = 40 + (int)(Math.abs(Math.sin(System.currentTimeMillis() / 80.0D)) * 90.0D);
            graphics.fill(x - 1, barY - 1, x + filled + 1, barY, (pulse << 24) | (color & 0x00FFFFFF));
        }
    }

    private static void renderOverheat(GuiGraphics graphics, Font font, int x, int y, int width, int heat, int heatKeepTimer, int timer, String grade) {
        int overheatTicks = Math.max(Hellforge.hellforgeConfig().coinOverheatTicks.get(), Hellforge.hellforgeConfig().coinStrongOverheatTicks.get());
        int capped = Math.max(0, Math.min(overheatTicks, timer));
        int filled = Math.max(0, Math.min(width, (int)(width * (capped / (float)Math.max(1, overheatTicks)))));
        int heatCapped = Math.max(0, Math.min(100, heat));
        int heatFilled = Math.max(0, Math.min(width, (int)(width * (heatCapped / 100.0F))));
        int heatColor = heatCapped >= 80 ? 0xFFFFF2A0 : 0xFFFFC857;
        int hotColor = 0xFFFF6A1A;
        graphics.drawString(font, "OVERHEAT", x - 2, y, hotColor, true);
        int barY = y + 10;
        graphics.fill(x, barY, x + width, barY + 3, 0xAA180604);
        graphics.fill(x, barY, x + filled, barY + 3, hotColor);
        graphics.fill(x, barY, x + filled, barY + 1, 0xFFFFF2A0);
        graphics.fill(x, barY + 4, x + width, barY + 5, 0x66180604);
        graphics.fill(x, barY + 4, x + heatFilled, barY + 5, heatColor);
        if (heatCapped >= 80) {
            int pulse = 45 + (int)(Math.abs(Math.sin(System.currentTimeMillis() / 70.0D)) * 100.0D);
            graphics.fill(x - 1, barY + 3, x + heatFilled + 1, barY + 4,
                (pulse << 24) | (heatColor & 0x00FFFFFF));
        }
    }

    private static void renderActionFeed(GuiGraphics graphics, Font font, int x, int y) {
        for (int i = 0; i < 2; i++) {
            CoinHitFeedbackClient.ActionFeedEntry entry = CoinHitFeedbackClient.getActionFeedEntry(i);
            if (entry == null) {
                continue;
            }
            graphics.pose().pushPose();
            int drawX = x + entry.getSlideX() + entry.getShakeX(i);
            int drawY = y + i * 11 + entry.getScrollY() + entry.getShakeY(i);
            float scale = entry.getScale();
            int textWidth = font.width(entry.getText());
            int flashAlpha = entry.getFlashAlpha();
            if (flashAlpha > 0) {
                graphics.fill(drawX - 3, drawY - 2, drawX + textWidth + 5, drawY + 10,
                    (flashAlpha << 24) | (entry.getColor() & 0x00FFFFFF));
            }
            graphics.fill(drawX - 3, drawY + 9, drawX + textWidth + 5, drawY + 10,
                0x88000000 | (entry.getColor() & 0x00FFFFFF));
            graphics.pose().translate(drawX, drawY, 0.0F);
            graphics.pose().scale(scale, scale, 1.0F);
            graphics.drawString(font, entry.getText(), 0, 0, entry.getColor(), true);
            graphics.pose().popPose();
        }
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

    private static String getNextRankHint(String grade) {
        return switch (grade) {
            case "S" -> "MAX";
            case "A" -> "TO S";
            case "B" -> "TO A";
            case "C" -> "TO B";
            default -> "TO C";
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
