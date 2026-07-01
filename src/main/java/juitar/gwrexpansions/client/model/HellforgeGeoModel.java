package juitar.gwrexpansions.client.model;

import juitar.gwrexpansions.GWRexpansions;
import juitar.gwrexpansions.item.BOMD.Hellforge;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class HellforgeGeoModel extends GeoModel<Hellforge> {
    @Override
    public ResourceLocation getModelResource(Hellforge animatable) {
        return GWRexpansions.resource("geo/hellforge.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Hellforge animatable) {
        return GWRexpansions.resource("textures/item/hellforge.png");
    }

    @Override
    public ResourceLocation getAnimationResource(Hellforge animatable) {
        return GWRexpansions.resource("animations/hellforge.animation.json");
    }
}
