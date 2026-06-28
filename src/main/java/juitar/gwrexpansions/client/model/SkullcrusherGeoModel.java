package juitar.gwrexpansions.client.model;

import juitar.gwrexpansions.GWRexpansions;
import juitar.gwrexpansions.item.BOMD.Skullcrusher;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class SkullcrusherGeoModel extends GeoModel<Skullcrusher> {
    @Override
    public ResourceLocation getModelResource(Skullcrusher animatable) {
        return GWRexpansions.resource("geo/skullcrusher_pulverizer.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Skullcrusher animatable) {
        return GWRexpansions.resource("textures/item/skullcrusher_pulverizer.png");
    }

    @Override
    public ResourceLocation getAnimationResource(Skullcrusher animatable) {
        return GWRexpansions.resource("animations/skullcrusher_pulverizer.animation.json");
    }
}
