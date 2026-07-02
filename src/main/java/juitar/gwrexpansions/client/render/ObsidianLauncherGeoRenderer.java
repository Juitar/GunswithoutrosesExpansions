package juitar.gwrexpansions.client.render;

import juitar.gwrexpansions.client.model.ObsidianLauncherGeoModel;
import juitar.gwrexpansions.item.BOMD.ObsidianLauncher;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class ObsidianLauncherGeoRenderer extends GeoItemRenderer<ObsidianLauncher> {
    public ObsidianLauncherGeoRenderer() {
        super(new ObsidianLauncherGeoModel());
    }
}
