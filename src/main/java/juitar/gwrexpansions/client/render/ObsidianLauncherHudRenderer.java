package juitar.gwrexpansions.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import juitar.gwrexpansions.GWRexpansions;
import juitar.gwrexpansions.config.ClientConfig;
import juitar.gwrexpansions.item.BOMD.ObsidianLauncher;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class ObsidianLauncherHudRenderer {
    private static final ResourceLocation SLOT = new ResourceLocation(GWRexpansions.MODID,
            "textures/gui/obsidian_launcher/obsidian_spell_slot.png");
    private static final ResourceLocation FIRE_SPELL = new ResourceLocation(GWRexpansions.MODID,
            "textures/gui/obsidian_launcher/spell_fire.png");
    private static final ResourceLocation FROST_SPELL = new ResourceLocation(GWRexpansions.MODID,
            "textures/gui/obsidian_launcher/spell_frost.png");
    private static final ResourceLocation HOLY_SPELL = new ResourceLocation(GWRexpansions.MODID,
            "textures/gui/obsidian_launcher/spell_holy.png");
    private static final ResourceLocation FRENZY_BAR_BG = new ResourceLocation(GWRexpansions.MODID,
            "textures/gui/obsidian_launcher/frenzy_bar_bg.png.png");
    private static final int SLOT_SIZE = 20;
    private static final float SLOT_RENDER_SCALE = 0.8F;
    private static final int RENDERED_SLOT_SIZE = 16;
    private static final int SLOT_GAP = 2;
    private static final int SPELL_HUD_WIDTH = RENDERED_SLOT_SIZE * 3 + SLOT_GAP * 2;
    private static final int SPELL_HUD_HEIGHT = RENDERED_SLOT_SIZE;
    private static final int FRENZY_BAR_WIDTH = 58;
    private static final int FRENZY_BAR_HEIGHT = 6;
    private static final int FRENZY_FILL_X = 3;
    private static final int FRENZY_FILL_Y = 2;
    private static final int FRENZY_FILL_WIDTH = 52;
    private static final int FRENZY_FILL_HEIGHT = 2;
    private static final int POP_ANIMATION_TICKS = 12;
    private static final float POP_SCALE_BONUS = 0.35F;

    private int lastStoredSpellMask = -1;
    private int spellSlotsVisibleUntilTick = 0;
    private int firePopStartTick = -POP_ANIMATION_TICKS;
    private int frostPopStartTick = -POP_ANIMATION_TICKS;
    private int holyPopStartTick = -POP_ANIMATION_TICKS;

    @SubscribeEvent
    public void onRenderGui(RenderGuiEvent event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || !ClientConfig.getBoolean(ClientConfig.INSTANCE.obsidianLauncherHudEnabled, true)) return;

        ItemStack launcherStack = getHeldLauncher(player);
        if (launcherStack == null) {
            lastStoredSpellMask = -1;
            spellSlotsVisibleUntilTick = 0;
            return;
        }

        int frenzyTicks = ObsidianLauncher.getFrenzyTicks(launcherStack);
        int storedSpellMask = getStoredSpellMask(launcherStack);
        if (lastStoredSpellMask < 0) {
            lastStoredSpellMask = storedSpellMask;
        } else {
            int addedSpells = storedSpellMask & ~lastStoredSpellMask;
            if (addedSpells != 0) {
                spellSlotsVisibleUntilTick = player.tickCount + 60;
                if ((addedSpells & 1) != 0) {
                    firePopStartTick = player.tickCount;
                }
                if ((addedSpells & 2) != 0) {
                    frostPopStartTick = player.tickCount;
                }
                if ((addedSpells & 4) != 0) {
                    holyPopStartTick = player.tickCount;
                }
            }
            lastStoredSpellMask = storedSpellMask;
        }

        boolean showFrenzy = frenzyTicks > 0;
        boolean showSpellSlots = !showFrenzy && player.tickCount < spellSlotsVisibleUntilTick;
        if (!showFrenzy && !showSpellSlots) {
            return;
        }

        GuiGraphics guiGraphics = event.getGuiGraphics();
        PoseStack poseStack = guiGraphics.pose();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        int width = showFrenzy ? FRENZY_BAR_WIDTH : SPELL_HUD_WIDTH;
        int height = showFrenzy ? FRENZY_BAR_HEIGHT : SPELL_HUD_HEIGHT;
        int offsetX = ClientConfig.getInt(ClientConfig.INSTANCE.obsidianLauncherHudOffsetX, 0);
        int offsetY = ClientConfig.getInt(ClientConfig.INSTANCE.obsidianLauncherHudOffsetY, 10);
        HudCollisionLayout.Bounds bounds = HudCollisionLayout.claim(event, (screenWidth - width) / 2 + offsetX,
                screenHeight / 2 + offsetY, width, height, screenWidth, screenHeight);
        int x = bounds.x;
        int y = bounds.y;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        poseStack.pushPose();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        if (showFrenzy) {
            drawFrenzyTimer(guiGraphics, x, y, width, ObsidianLauncher.getFrenzyProgress(launcherStack));
        } else {
            drawSpellSlots(guiGraphics, x, y, launcherStack, player.tickCount + event.getPartialTick());
        }

        poseStack.popPose();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }

    private static int getStoredSpellMask(ItemStack stack) {
        int mask = 0;
        if (ObsidianLauncher.hasStoredFireSpell(stack)) {
            mask |= 1;
        }
        if (ObsidianLauncher.hasStoredFrostSpell(stack)) {
            mask |= 2;
        }
        if (ObsidianLauncher.hasStoredHolySpell(stack)) {
            mask |= 4;
        }
        return mask;
    }

    private void drawSpellSlots(GuiGraphics guiGraphics, int x, int y, ItemStack stack, float tickTime) {
        drawSpellSlot(guiGraphics, x, y, FIRE_SPELL, ObsidianLauncher.hasStoredFireSpell(stack),
                popScale(tickTime, firePopStartTick));
        drawSpellSlot(guiGraphics, x + RENDERED_SLOT_SIZE + SLOT_GAP, y, FROST_SPELL,
                ObsidianLauncher.hasStoredFrostSpell(stack), popScale(tickTime, frostPopStartTick));
        drawSpellSlot(guiGraphics, x + (RENDERED_SLOT_SIZE + SLOT_GAP) * 2, y, HOLY_SPELL,
                ObsidianLauncher.hasStoredHolySpell(stack), popScale(tickTime, holyPopStartTick));
    }

    private static float popScale(float tickTime, int startTick) {
        float age = tickTime - startTick;
        if (age < 0.0F || age >= POP_ANIMATION_TICKS) {
            return 1.0F;
        }

        float progress = age / POP_ANIMATION_TICKS;
        return 1.0F + (1.0F - progress) * POP_SCALE_BONUS;
    }

    private static void drawSpellSlot(GuiGraphics guiGraphics, int x, int y, ResourceLocation spellTexture,
                                      boolean filled, float popScale) {
        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        float totalScale = SLOT_RENDER_SCALE * popScale;
        float renderedSize = SLOT_SIZE * totalScale;
        float centeredX = x + (RENDERED_SLOT_SIZE - renderedSize) * 0.5F;
        float centeredY = y + (RENDERED_SLOT_SIZE - renderedSize) * 0.5F;
        poseStack.translate(centeredX, centeredY, 0.0F);
        poseStack.scale(totalScale, totalScale, 1.0F);

        guiGraphics.blit(SLOT, 0, 0, 0, 0, SLOT_SIZE, SLOT_SIZE, SLOT_SIZE, SLOT_SIZE);
        if (filled) {
            guiGraphics.blit(spellTexture, 0, 0, 0, 0, SLOT_SIZE, SLOT_SIZE, SLOT_SIZE, SLOT_SIZE);
        }

        poseStack.popPose();
    }

    private static void drawFrenzyTimer(GuiGraphics guiGraphics, int x, int y, int width, float progress) {
        guiGraphics.blit(FRENZY_BAR_BG, x, y, 0, 0, FRENZY_BAR_WIDTH, FRENZY_BAR_HEIGHT,
                FRENZY_BAR_WIDTH, FRENZY_BAR_HEIGHT);
        int fillWidth = Math.max(0, Math.min(FRENZY_FILL_WIDTH, (int)(FRENZY_FILL_WIDTH * progress)));
        if (fillWidth > 0) {
            guiGraphics.fill(x + FRENZY_FILL_X, y + FRENZY_FILL_Y,
                    x + FRENZY_FILL_X + fillWidth, y + FRENZY_FILL_Y + FRENZY_FILL_HEIGHT,
                    0xFFFF42D6);
            guiGraphics.fill(x + FRENZY_FILL_X, y + FRENZY_FILL_Y,
                    x + FRENZY_FILL_X + fillWidth, y + FRENZY_FILL_Y + 1,
                    0xFFFFA4F0);
        }
    }

    private static ItemStack getHeldLauncher(Player player) {
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.getItem() instanceof ObsidianLauncher) {
            return mainHand;
        }
        ItemStack offHand = player.getOffhandItem();
        if (offHand.getItem() instanceof ObsidianLauncher) {
            return offHand;
        }
        return null;
    }
}
