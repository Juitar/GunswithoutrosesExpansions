package juitar.gwrexpansions.client.model;

import juitar.gwrexpansions.GWRexpansions;
import juitar.gwrexpansions.item.meetyourfight.DestinyGunItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class DestinySevenGeoModel extends GeoModel<DestinyGunItem> {
    @Override
    public ResourceLocation getModelResource(DestinyGunItem animatable) {
        return GWRexpansions.resource("geo/destiny_seven.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(DestinyGunItem animatable) {
        return GWRexpansions.resource("textures/item/destiny_seven.png");
    }

    @Override
    public ResourceLocation getAnimationResource(DestinyGunItem animatable) {
        return GWRexpansions.resource("animations/destiny_seven.animation.json");
    }
}
