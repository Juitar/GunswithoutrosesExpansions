package juitar.gwrexpansions.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import juitar.gwrexpansions.GWRexpansions;
import juitar.gwrexpansions.config.ClientConfig;
import juitar.gwrexpansions.item.cataclysm.IgnitiumGatlingItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.io.IOException;

/** Existing art layers; only the glowing ring is clipped by a continuous angular shader mask. */
public class IgnitiumGatlingHudRenderer {
    private static final int SIZE = 48;
    private static ShaderInstance heatShader;
    private static ResourceLocation texture(String name) {
        return GWRexpansions.resource("textures/gui/ignitium_gatling/v1/" + name + ".png");
    }

    @Mod.EventBusSubscriber(modid = GWRexpansions.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class Shaders {
        @SubscribeEvent
        public static void register(RegisterShadersEvent event) throws IOException {
            event.registerShader(new ShaderInstance(event.getResourceProvider(),
                    GWRexpansions.resource("ignitium_heat"), DefaultVertexFormat.POSITION_TEX_COLOR),
                    shader -> heatShader = shader);
        }
    }

    @SubscribeEvent
    public void render(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.options.hideGui
                || !ClientConfig.getBoolean(ClientConfig.INSTANCE.ignitiumHudEnabled, true)) return;
        var stack = IgnitiumGatlingItem.findHeldStack(mc.player);
        if (stack.isEmpty() || !IgnitiumGatlingItem.isGunSkillEnabled(stack)) return;
        GuiGraphics gui = event.getGuiGraphics();
        int width = mc.getWindow().getGuiScaledWidth(), height = mc.getWindow().getGuiScaledHeight();
        float scale = (float) ClientConfig.getDouble(ClientConfig.INSTANCE.ignitiumHudScale, 1);
        int extent = (int) Math.ceil(SIZE * scale);
        var bounds = HudCollisionLayout.claim(event,
                (width - extent) / 2D + ClientConfig.getDouble(ClientConfig.INSTANCE.ignitiumHudOffsetX, 0),
                height / 2D + ClientConfig.getDouble(ClientConfig.INSTANCE.ignitiumHudOffsetY, 18),
                extent, extent, width, height);
        boolean soul = IgnitiumGatlingItem.isBlueFireActive(stack);
        String palette = soul ? "soul" : "fire";
        float time = mc.level.getGameTime() + event.getPartialTick();
        gui.pose().pushPose();
        gui.pose().translate(bounds.x + bounds.fracX, bounds.y + bounds.fracY, 0);
        gui.pose().scale(scale, scale, 1);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        try {
            blit(gui, "sigil_empty", 0, 0, SIZE);
            float fraction = IgnitiumGatlingItem.getHeatFraction(stack);
            drawHeat(gui, texture("ring_" + palette + "_full"), fraction);
            blit(gui, "core_" + palette + "_" + (fraction < 0.3F ? "ember" : fraction < 0.8F ? "medium" : "hot"), 0, 0, SIZE);
            boolean mode = IgnitiumGatlingItem.isFireballMode(stack);
            boolean heavyReady = IgnitiumGatlingItem.isAbyssReady(stack, mc.level.getGameTime());
            if (mode) blit(gui, heavyReady ? "heavy_ready" : "heavy_unavailable", 18, 28, 12);
            if (!mode && fraction >= 1.0F) {
                RenderSystem.setShaderColor(1, 1, 1, 0.55F + 0.25F * (float) Math.sin(time * 0.25F));
                blit(gui, "ready_flash_" + palette, 0, 0, SIZE);
                RenderSystem.setShaderColor(1, 1, 1, 1);
            }
            String animation = IgnitiumGatlingItem.currentAnimation(stack);
            if (IgnitiumGatlingItem.animationEndsAt(stack) > time
                    && (animation.contains(".fireball_shot") || animation.contains(".abyss_shot"))) {
                RenderSystem.setShaderColor(1, 1, 1, 0.65F);
                blit(gui, animation.contains(".abyss_shot") ? "release_abyss" : "release_" + palette, 0, 0, SIZE);
                RenderSystem.setShaderColor(1, 1, 1, 1);
            }
            gui.flush();
        } finally {
            RenderSystem.setShaderColor(1, 1, 1, 1);
            RenderSystem.setShader(net.minecraft.client.renderer.GameRenderer::getPositionTexColorShader);
            RenderSystem.disableBlend();
            gui.pose().popPose();
        }
    }

    private static void blit(GuiGraphics gui, String name, int x, int y, int size) {
        gui.blit(texture(name), x, y, 0, 0, size, size, size, size);
    }

    private static void drawHeat(GuiGraphics gui, ResourceLocation texture, float heat) {
        if (heat <= 0 || heatShader == null) return;
        gui.flush();
        RenderSystem.setShader(() -> heatShader);
        RenderSystem.setShaderTexture(0, texture);
        heatShader.getUniform("HeatFraction").set(heat);
        var matrix = gui.pose().last().pose();
        var builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        builder.vertex(matrix, 0, SIZE, 0).uv(0, 1).color(255, 255, 255, 255).endVertex();
        builder.vertex(matrix, SIZE, SIZE, 0).uv(1, 1).color(255, 255, 255, 255).endVertex();
        builder.vertex(matrix, SIZE, 0, 0).uv(1, 0).color(255, 255, 255, 255).endVertex();
        builder.vertex(matrix, 0, 0, 0).uv(0, 0).color(255, 255, 255, 255).endVertex();
        BufferUploader.drawWithShader(builder.end());
        RenderSystem.setShader(net.minecraft.client.renderer.GameRenderer::getPositionTexColorShader);
    }
}
