package juitar.gwrexpansions.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import juitar.gwrexpansions.client.model.IgnitiumGatlingGeoModel;
import juitar.gwrexpansions.item.cataclysm.IgnitiumGatlingItem;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.cache.object.GeoCube;
import software.bernie.geckolib.cache.object.GeoQuad;
import software.bernie.geckolib.cache.texture.AnimatableTexture;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;
import software.bernie.geckolib.util.RenderUtils;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import java.util.ArrayList;

/** One geometry and one animation pose; materials are selected on its existing bones. */
public class IgnitiumGatlingGeoRenderer extends GeoItemRenderer<IgnitiumGatlingItem> {
    private boolean clipMaterial;
    private boolean clipSoul;
    private final Vector4f sweepPlane = new Vector4f();
    private boolean complementPass;
    private IgnitiumGatlingItem.Part material = IgnitiumGatlingItem.Part.BODY;
    private MultiBufferSource materialBuffers;
    private IgnitiumGatlingItem renderedItem;
    private ItemStack renderedStack;

    public IgnitiumGatlingGeoRenderer() {
        super(new IgnitiumGatlingGeoModel());
        addRenderLayer(new SoulSweepLayer());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack pose,
                             MultiBufferSource buffers, int light, int overlay) {
        IgnitiumGatlingItem.setRenderStack(stack);
        clipMaterial = IgnitiumGatlingItem.isSoulColorSweep(stack);
        clipSoul = IgnitiumGatlingItem.isBlueFireActive(stack);
        complementPass = false;
        materialBuffers = buffers;
        renderedStack = stack;
        try {
            super.renderByItem(stack, context, pose, buffers, light, overlay);
        } finally {
            IgnitiumGatlingItem.clearRenderStack();
            clipMaterial = false;
            materialBuffers = null;
            renderedItem = null;
            renderedStack = null;
        }
    }

    @Override
    public void renderRecursively(PoseStack pose, IgnitiumGatlingItem item, GeoBone bone, RenderType type,
                                  MultiBufferSource buffers, VertexConsumer buffer, boolean reRender,
                                  float partialTick, int light, int overlay, float red, float green, float blue, float alpha) {
        if (clipMaterial && bone.getName().equals("Gatling_Root")) {
            // Capture the same root transform used by GeckoLib, after animation evaluation.
            // The boundary uses the sigil's sampled pose, never a separate wall-clock timer.
            pose.pushPose();
            RenderUtils.prepMatrixForBone(pose, bone);
            Matrix4f inverse = new Matrix4f(pose.last().pose()).invert();
            pose.popPose();
            GeoBone sigil = getGeoModel().getBone("Soul_Sigil").orElseThrow();
            float depth = (sigil.getPivotZ() + sigil.getPosZ()) / 16F;
            sweepPlane.set(IgnitiumSoulSweep.planeInRenderSpace(inverse, depth));
        }
        var previous = material;
        material = materialFor(bone.getName());
        renderedItem = item;
        try {
            // Reactivate the bone's buffer after a sibling used another material.
            var texture = item.getGeoTexture(material, clipSoul);
            AnimatableTexture.setAndUpdate(texture);
            RenderType boneType = material == IgnitiumGatlingItem.Part.BODY
                    ? getRenderType(item, texture, buffers, partialTick) : RenderType.entityTranslucent(texture);
            int boneLight = material == IgnitiumGatlingItem.Part.BODY || material == IgnitiumGatlingItem.Part.SMOKE
                    ? light : LightTexture.FULL_BRIGHT;
            super.renderRecursively(pose, item, bone, boneType, buffers, buffers.getBuffer(boneType), reRender,
                    partialTick, boneLight, overlay, red, green, blue, alpha);
        } finally { material = previous; }
    }

    private static IgnitiumGatlingItem.Part materialFor(String name) {
        return switch (name) {
            case "Lava_Receiver", "Lava_Furnace" -> IgnitiumGatlingItem.Part.LAVA;
            case "Flash" -> IgnitiumGatlingItem.Part.FLASH;
            case "Soul_Sigil" -> IgnitiumGatlingItem.Part.SIGIL;
            case "Abyss_Flash" -> IgnitiumGatlingItem.Part.ABYSS_FLASH;
            case "FB_Vent_L", "FB_Vent_R" -> IgnitiumGatlingItem.Part.VENT;
            case "FB_Fireball" -> IgnitiumGatlingItem.Part.FIREBALL;
            case "FB_Ember_1", "FB_Ember_2", "FB_Ember_3" -> IgnitiumGatlingItem.Part.EMBER;
            case "FB_Smoke" -> IgnitiumGatlingItem.Part.SMOKE;
            default -> IgnitiumGatlingItem.Part.BODY;
        };
    }

    @Override
    public void renderCube(PoseStack pose, GeoCube cube, VertexConsumer buffer, int light, int overlay,
                           float red, float green, float blue, float alpha) {
        var previous = material;
        if (material == IgnitiumGatlingItem.Part.ABYSS_FLASH && Math.abs(cube.size().x - 5.6) < 0.001)
            material = IgnitiumGatlingItem.Part.BURST_FRONT;
        try {
            if (complementPass && material != IgnitiumGatlingItem.Part.BODY && material != IgnitiumGatlingItem.Part.LAVA) return;
            var texture = renderedItem.getGeoTexture(material, clipSoul);
            RenderType type = RenderType.entityTranslucent(texture);
            super.renderCube(pose, cube, materialBuffers.getBuffer(type), light, overlay, red, green, blue, alpha);
        } finally { material = previous; }
    }

    @Override
    public void createVerticesOfQuad(GeoQuad quad, Matrix4f matrix, Vector3f normal, VertexConsumer buffer,
                                     int light, int overlay, float red, float green, float blue, float alpha) {
        boolean swept = material == IgnitiumGatlingItem.Part.BODY || material == IgnitiumGatlingItem.Part.LAVA;
        if (!clipMaterial || !swept) {
            super.createVerticesOfQuad(quad, matrix, normal, buffer, light, overlay, red, green, blue, alpha);
            return;
        }
        var vertices = new ArrayList<IgnitiumSoulSweep.Vertex>(4);
        for (var vertex : quad.vertices()) {
            Vector3f position = matrix.transformPosition(new Vector3f(vertex.position()));
            float u = vertex.texU();
            float v = vertex.texV();
            if (renderedStack != null && IgnitiumGatlingItem.isAbyssAnimation(renderedStack)) {
                float[] mapped = remapAbyssUv(material, u, v);
                u = mapped[0]; v = mapped[1];
            }
            vertices.add(new IgnitiumSoulSweep.Vertex(position.x, position.y, position.z, u, v));
        }
        var polygon = IgnitiumSoulSweep.clip(vertices, sweepPlane, clipSoul);
        if (polygon.size() == 4) {
            for (var vertex : polygon) emit(vertex, normal, buffer, light, overlay, red, green, blue, alpha);
        } else {
            // RenderType uses QUADS: repeat each triangle's last vertex to form a degenerate quad.
            for (int i = 1; i + 1 < polygon.size(); i++) {
                emit(polygon.get(0), normal, buffer, light, overlay, red, green, blue, alpha);
                emit(polygon.get(i), normal, buffer, light, overlay, red, green, blue, alpha);
                emit(polygon.get(i + 1), normal, buffer, light, overlay, red, green, blue, alpha);
                emit(polygon.get(i + 1), normal, buffer, light, overlay, red, green, blue, alpha);
            }
        }
    }

    /** The atlas stores fire/soul and abyss panes in separate rectangles. */
    private static float[] remapAbyssUv(IgnitiumGatlingItem.Part part, float u, float v) {
        float[][] pair = switch (part) {
            case ABYSS_FLASH -> new float[][]{{158, 30, 32, 32}, {126, 0, 32, 32}};
            case BURST_FRONT -> new float[][]{{94, 0, 32, 32}, {203, 120, 32, 32}};
            case VENT -> new float[][]{{41, 58, 32, 32}, {56, 173, 32, 32}};
            case FIREBALL -> new float[][]{{56, 205, 32, 32}, {196, 71, 32, 32}};
            case EMBER -> new float[][]{{229, 196, 16, 16}, {172, 227, 16, 16}};
            default -> null;
        };
        if (pair == null) return new float[]{u, v};
        float px = u * 256F, py = v * 256F;
        float bx = pair[0][0], by = pair[0][1], w = pair[0][2], h = pair[0][3];
        if (px < bx - 0.01F || px > bx + w + 0.01F || py < by - 0.01F || py > by + h + 0.01F)
            return new float[]{u, v};
        return new float[]{(pair[1][0] + px - bx) / 256F, (pair[1][1] + py - by) / 256F};
    }

    private static void emit(IgnitiumSoulSweep.Vertex vertex, Vector3f normal, VertexConsumer buffer,
                             int light, int overlay, float red, float green, float blue, float alpha) {
        buffer.vertex(vertex.x(), vertex.y(), vertex.z(), red, green, blue, alpha, vertex.u(), vertex.v(),
                overlay, light, normal.x, normal.y, normal.z);
    }

    private final class SoulSweepLayer extends GeoRenderLayer<IgnitiumGatlingItem> {
        private SoulSweepLayer() {
            super(IgnitiumGatlingGeoRenderer.this);
        }

        @Override
        public void render(PoseStack pose, IgnitiumGatlingItem item, BakedGeoModel body, RenderType type,
                           MultiBufferSource buffers, VertexConsumer buffer, float partialTick, int light, int overlay) {
            if (!IgnitiumGatlingItem.isSoulColorSweep(getCurrentItemStack())) return;
            boolean previousSoul = clipSoul;
            try {
                complementPass = true;
                clipSoul = !previousSoul;
                var texture = item.getGeoTexture(IgnitiumGatlingItem.Part.BODY, clipSoul);
                RenderType opposite = RenderType.entityTranslucent(texture);
                // Reuse the same baked geometry and sampled pose, clipping only the
                // complementary body/lava surfaces. Effects are drawn once.
                getRenderer().reRender(body, pose, buffers, item, opposite, buffers.getBuffer(opposite),
                        partialTick, light, overlay, 1, 1, 1, 1);
            } finally {
                clipSoul = previousSoul;
                complementPass = false;
            }
        }
    }
}
