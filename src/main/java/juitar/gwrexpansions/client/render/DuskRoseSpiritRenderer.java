package juitar.gwrexpansions.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import juitar.gwrexpansions.entity.meetyourfight.DuskRoseSpiritEntity;
import lykrast.meetyourfight.entity.RoseSpiritEntity;
import lykrast.meetyourfight.renderer.RoseSpiritRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Entity;

public class DuskRoseSpiritRenderer extends RoseSpiritRenderer {
    public DuskRoseSpiritRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(RoseSpiritEntity entity, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
        if (!(entity instanceof DuskRoseSpiritEntity spirit)) {
            return;
        }

        Entity owner = spirit.getSyncedOwnerEntity();
        if (owner == null) {
            return;
        }

        float x = (float) (owner.getX() - spirit.getX());
        float y = (float) (owner.getY() - spirit.getY());
        float z = (float) (owner.getZ() - spirit.getZ());
        RoseSpiritRenderer.renderCrystalBeams(x, y, z, partialTick, spirit.tickCount, poseStack, buffer, packedLight);
    }
}
