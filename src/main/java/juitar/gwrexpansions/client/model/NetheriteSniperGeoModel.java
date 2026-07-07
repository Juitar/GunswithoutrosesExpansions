package juitar.gwrexpansions.client.model;

import juitar.gwrexpansions.GWRexpansions;
import juitar.gwrexpansions.item.vanilla.NetheriteSniper;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class NetheriteSniperGeoModel extends GeoModel<NetheriteSniper> {
    @Override
    public ResourceLocation getModelResource(NetheriteSniper animatable) {
        return GWRexpansions.resource("geo/netherite_sniper.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(NetheriteSniper animatable) {
        return GWRexpansions.resource("textures/item/netherite_sniper.png");
    }

    @Override
    public ResourceLocation getAnimationResource(NetheriteSniper animatable) {
        return GWRexpansions.resource("animations/netherite_sniper.animation.json");
    }
}
