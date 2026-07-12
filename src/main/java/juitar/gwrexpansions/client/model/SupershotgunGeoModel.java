package juitar.gwrexpansions.client.model;

import juitar.gwrexpansions.GWRexpansions;
import juitar.gwrexpansions.item.vanilla.Supershotgun;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class SupershotgunGeoModel extends GeoModel<Supershotgun> {
    @Override
    public ResourceLocation getModelResource(Supershotgun animatable) {
        return GWRexpansions.resource("geo/super_shotgun.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(Supershotgun animatable) {
        return GWRexpansions.resource("textures/item/super_shotgun.png");
    }

    @Override
    public ResourceLocation getAnimationResource(Supershotgun animatable) {
        return GWRexpansions.resource("animations/super_shotgun.animation.json");
    }
}
