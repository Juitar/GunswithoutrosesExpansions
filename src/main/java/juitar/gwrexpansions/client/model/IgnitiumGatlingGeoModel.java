package juitar.gwrexpansions.client.model;

import juitar.gwrexpansions.GWRexpansions;
import juitar.gwrexpansions.item.cataclysm.IgnitiumGatlingItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.constant.DataTickets;
import net.minecraft.client.Minecraft;

/** The complete weapon and its animated effects share one geometry. */
public class IgnitiumGatlingGeoModel extends GeoModel<IgnitiumGatlingItem> {
    @Override
    public ResourceLocation getModelResource(IgnitiumGatlingItem animatable) {
        return GWRexpansions.resource("geo/ignitium_gatling.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(IgnitiumGatlingItem animatable) {
        return animatable.getGeoTexture(IgnitiumGatlingItem.Part.BODY);
    }

    @Override
    public ResourceLocation getAnimationResource(IgnitiumGatlingItem animatable) {
        return GWRexpansions.resource("animations/ignitium_gatling.animation.json");
    }

    @Override
    public void setCustomAnimations(IgnitiumGatlingItem item, long instanceId, AnimationState<IgnitiumGatlingItem> state) {
        var stack = state.getData(DataTickets.ITEMSTACK);
        // The export has no fireball-mode idle clip. Keep the vanes open between shots
        // without introducing a new charge or replaying the entry animation.
        if (stack != null && IgnitiumGatlingItem.isFireballMode(stack)
                && (IgnitiumGatlingItem.currentAnimation(stack).isEmpty() || IgnitiumGatlingItem.isSoulTransition(stack))) {
            var level = Minecraft.getInstance().level;
            float angle = level == null ? 12 : IgnitiumGatlingItem.modeVaneAngle(stack, level.getGameTime(), state.getPartialTick());
            // The entry mechanism has its own clock so a simultaneous full-length soul
            // sweep cannot hide it. Shot/recoil clips otherwise keep their authored tracks.
            getBone("Heat_Vane_L").ifPresent(b -> b.setRotY((float) Math.toRadians(-angle)));
            getBone("Heat_Vane_R").ifPresent(b -> b.setRotY((float) Math.toRadians(angle)));
        }
    }
}
