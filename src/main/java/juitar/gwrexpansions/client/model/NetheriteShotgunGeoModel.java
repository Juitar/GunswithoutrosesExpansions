package juitar.gwrexpansions.client.model;

import juitar.gwrexpansions.GWRexpansions;
import juitar.gwrexpansions.item.vanilla.NetheriteShotgun;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class NetheriteShotgunGeoModel extends GeoModel<NetheriteShotgun> {
    @Override
    public ResourceLocation getModelResource(NetheriteShotgun animatable) {
        return GWRexpansions.resource("geo/netherite_shotgun.json");
    }

    @Override
    public ResourceLocation getTextureResource(NetheriteShotgun animatable) {
        return GWRexpansions.resource("textures/item/netherite_shotgun.png");
    }

    @Override
    public ResourceLocation getAnimationResource(NetheriteShotgun animatable) {
        return GWRexpansions.resource("animations/netherite_shotgun.animation.json");
    }
}
