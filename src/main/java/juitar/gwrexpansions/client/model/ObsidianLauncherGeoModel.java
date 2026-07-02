package juitar.gwrexpansions.client.model;

import juitar.gwrexpansions.GWRexpansions;
import juitar.gwrexpansions.item.BOMD.ObsidianLauncher;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class ObsidianLauncherGeoModel extends GeoModel<ObsidianLauncher> {
    @Override
    public ResourceLocation getModelResource(ObsidianLauncher animatable) {
        return GWRexpansions.resource("geo/obsidian_launcher.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(ObsidianLauncher animatable) {
        return GWRexpansions.resource("textures/item/obsidian_launcher.png");
    }

    @Override
    public ResourceLocation getAnimationResource(ObsidianLauncher animatable) {
        return GWRexpansions.resource("animations/obsidian_launcher.animation.json");
    }
}
