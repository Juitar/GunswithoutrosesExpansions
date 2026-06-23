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
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = GWRexpansions.MODID, value = Dist.CLIENT)
public class CoinChainHudOverlay {
    private static final int WINDOW_TICKS = 40;

    @SubscribeEvent
    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.options.hideGui) {
            return;
        }

        ItemStack stack = getHeldHellforge(player);
        if (stack.isEmpty()) {
            return;
        }
        if (CoinHitFeedbackClient.isClearSuppressed()) {
            return;
        }

        CompoundTag tag = stack.getOrCreateTag();
        int hits = Math.max(tag.getInt("CoinChainHits"), CoinHitFeedbackClient.getCurrentHits());
        int timer = Math.max(tag.getInt("CoinChainTimer"), CoinHitFeedbackClient.getChainTimer());
        if (hits <= 0 || timer <= 0) {
            return;
        }

        render(event.getGuiGraphics(), event.getWindow().getGuiScaledWidth(), hits, timer);
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

    private static void render(GuiGraphics graphics, int screenWidth, int hits, int timer) {
        Font font = Minecraft.getInstance().font;
        String grade = Hellforge.getCoinChainGrade(hits);
        int x = screenWidth - 92;
        int y = 44;
        int color = getGradeColor(grade);

        RenderSystem.enableBlend();
        int flashAlpha = Math.min(110, CoinHitFeedbackClient.getFlashTicks() * 12);
        if (flashAlpha > 0) {
            graphics.fill(0, 0, screenWidth, 32, (flashAlpha << 24) | 0xFFD36A);
        }

        graphics.drawString(font, grade, x, y, color, true);
        int barWidth = 28;
        int barHeight = 3;
        int barX = x - 8;
        int barY = y + 14;
        int filled = Math.max(0, Math.min(barWidth, (int) (barWidth * (timer / (float) WINDOW_TICKS))));
        graphics.fill(barX, barY, barX + barWidth, barY + barHeight, 0xAA111111);
        graphics.fill(barX, barY, barX + filled, barY + barHeight, color);
        RenderSystem.disableBlend();
    }

    private static int getGradeColor(String grade) {
        return switch (grade) {
            case "S" -> 0xFFFF4444;
            case "A" -> 0xFFFFD700;
            case "B" -> 0xFF66CCFF;
            case "C" -> 0xFF77FF77;
            default -> 0xFFFFFFFF;
        };
    }
}
