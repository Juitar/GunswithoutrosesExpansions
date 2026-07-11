package juitar.gwrexpansions.client.model;

import juitar.gwrexpansions.GWRexpansions;
import juitar.gwrexpansions.item.cataclysm.CursiumGunItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class CursiumBallistaGeoModel extends GeoModel<CursiumGunItem> {
    @Override
    public ResourceLocation getModelResource(CursiumGunItem animatable) {
        return GWRexpansions.resource("geo/cursium_ballista.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(CursiumGunItem animatable) {
        return GWRexpansions.resource("textures/item/cursium_ballista.png");
    }

    @Override
    public ResourceLocation getAnimationResource(CursiumGunItem animatable) {
        return GWRexpansions.resource("animations/cursium_ballista.animation.json");
    }
}
