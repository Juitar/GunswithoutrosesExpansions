package juitar.gwrexpansions.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import juitar.gwrexpansions.GWRexpansions;
import juitar.gwrexpansions.config.ClientConfig;
import juitar.gwrexpansions.item.cataclysm.CeraunusBurstItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CeraunusBurstHudRenderer {
    private static final ResourceLocation WATER_ICON =
            new ResourceLocation(GWRexpansions.MODID, "textures/gui/ceraunus_burst/water_rune.png");
    private static final ResourceLocation STORM_ICON =
            new ResourceLocation(GWRexpansions.MODID, "textures/gui/ceraunus_burst/storm_rune.png");
    private static final ResourceLocation LIGHTNING_ICON =
            new ResourceLocation(GWRexpansions.MODID, "textures/gui/ceraunus_burst/lightning_rune.png");
    private static final ResourceLocation SKILL_FRAME =
            new ResourceLocation(GWRexpansions.MODID, "textures/gui/ceraunus_burst/skill_frame.png");
    private static final ResourceLocation SKILL_FRAME_ACTIVE =
            new ResourceLocation(GWRexpansions.MODID, "textures/gui/ceraunus_burst/skill_frame_active.png");

    private static final int SLOT_SIZE = 18;
    private static final int ICON_SIZE = 18;
    private static final int SLOT_GAP = 2;
    private static final int TEXTURE_SIZE = 18;

    @SubscribeEvent
    public void onRenderGui(RenderGuiEvent event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.options.hideGui
                || !ClientConfig.getBoolean(ClientConfig.INSTANCE.ceraunusBurstHudEnabled, true)) {
            return;
        }

        ItemStack gun = findHeldCeraunusBurst(player);
        if (gun.isEmpty() || !CeraunusBurstItem.hasComboHud(gun)) {
            return;
        }

        GuiGraphics guiGraphics = event.getGuiGraphics();
        PoseStack poseStack = guiGraphics.pose();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();
        int rowWidth = SLOT_SIZE * 3 + SLOT_GAP * 2;
        int offsetX = ClientConfig.getInt(ClientConfig.INSTANCE.ceraunusBurstHudOffsetX, 0);
        int offsetY = ClientConfig.getInt(ClientConfig.INSTANCE.ceraunusBurstHudOffsetY, 18);
        HudCollisionLayout.Bounds bounds = HudCollisionLayout.claim(event, (screenWidth - rowWidth) / 2 + offsetX,
                screenHeight / 2 + offsetY, rowWidth, SLOT_SIZE, screenWidth, screenHeight);
        int x = bounds.x;
        int y = bounds.y;
        boolean completed = CeraunusBurstItem.isHudShowingLastCombo(gun);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, completed ? 1.0F : 0.9F);

        poseStack.pushPose();
        for (int i = 0; i < 3; i++) {
            int slotX = x + i * (SLOT_SIZE + SLOT_GAP);
            drawSlot(guiGraphics, slotX, y, completed);
            int element = CeraunusBurstItem.getHudElement(gun, i);
            drawElement(guiGraphics, element, slotX, y);
        }
        poseStack.popPose();

        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.disableBlend();
    }

    private static void drawSlot(GuiGraphics guiGraphics, int x, int y, boolean completed) {
        ResourceLocation frame = completed ? SKILL_FRAME_ACTIVE : SKILL_FRAME;
        guiGraphics.blit(frame, x, y, 0.0F, 0.0F, SLOT_SIZE, SLOT_SIZE, SLOT_SIZE, SLOT_SIZE);
    }

    private static void drawElement(GuiGraphics guiGraphics, int element, int x, int y) {
        ResourceLocation texture = textureForElement(element);
        if (texture == null) {
            return;
        }

        guiGraphics.blit(texture, x, y, 0.0F, 0.0F, ICON_SIZE, ICON_SIZE, TEXTURE_SIZE, TEXTURE_SIZE);
    }

    private static ResourceLocation textureForElement(int element) {
        if (element == CeraunusBurstItem.ELEMENT_WATER) {
            return WATER_ICON;
        }
        if (element == CeraunusBurstItem.ELEMENT_STORM) {
            return STORM_ICON;
        }
        if (element == CeraunusBurstItem.ELEMENT_LIGHTNING) {
            return LIGHTNING_ICON;
        }
        return null;
    }

    private static ItemStack findHeldCeraunusBurst(Player player) {
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.getItem() instanceof CeraunusBurstItem) {
            return mainHand;
        }

        ItemStack offHand = player.getOffhandItem();
        if (offHand.getItem() instanceof CeraunusBurstItem) {
            return offHand;
        }

        return ItemStack.EMPTY;
    }
}
