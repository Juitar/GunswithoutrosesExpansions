package juitar.gwrexpansions.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.github.L_Ender.cataclysm.client.model.entity.Ignis_Fireball_Model;
import juitar.gwrexpansions.entity.cataclysm.IgnitiumFireballEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** Keep Ignis's original 3D model, textures and tumble on the ballistic projectile. */
public class IgnitiumFireballRenderer extends EntityRenderer<IgnitiumFireballEntity> {
    private static final ResourceLocation FIRE = new ResourceLocation("cataclysm", "textures/entity/ignis_fireball.png");
    private static final ResourceLocation SOUL = new ResourceLocation("cataclysm", "textures/entity/ignis_fireball_soul.png");
    private static final ResourceLocation ABYSS = new ResourceLocation("cataclysm", "textures/entity/ignis_fireball_abyss.png");
    private final Ignis_Fireball_Model model = new Ignis_Fireball_Model();

    public IgnitiumFireballRenderer(EntityRendererProvider.Context context) { super(context); }

    @Override public ResourceLocation getTextureLocation(IgnitiumFireballEntity entity) {
        return entity.isAbyss() ? ABYSS : entity.isSoul() ? SOUL : FIRE;
    }

    @Override protected int getBlockLightLevel(IgnitiumFireballEntity entity, BlockPos pos) { return 15; }

    @Override public void render(IgnitiumFireballEntity entity, float yaw, float partialTick,
                                 PoseStack pose, MultiBufferSource buffers, int light) {
        pose.pushPose();
        float age = entity.tickCount + partialTick;
        pose.translate(0, 0.3F, 0);
        pose.mulPose(Axis.YP.rotationDegrees(Mth.sin(age * 0.1F) * 180));
        pose.mulPose(Axis.XP.rotationDegrees(Mth.cos(age * 0.1F) * 180));
        pose.mulPose(Axis.ZP.rotationDegrees(Mth.sin(age * 0.15F) * 360));
        model.setupAnim(entity, 0, 0, 0,
                Mth.rotLerp(partialTick, entity.yRotO, entity.getYRot()),
                Mth.lerp(partialTick, entity.xRotO, entity.getXRot()));
        var vertex = buffers.getBuffer(model.renderType(getTextureLocation(entity)));
        model.renderToBuffer(pose, vertex, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
        pose.popPose();
        super.render(entity, yaw, partialTick, pose, buffers, light);
    }
}
