package juitar.gwrexpansions.client.render;

import juitar.gwrexpansions.client.model.SkullcrusherGeoModel;
import juitar.gwrexpansions.item.BOMD.Skullcrusher;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class SkullcrusherGeoRenderer extends GeoItemRenderer<Skullcrusher> {
    public SkullcrusherGeoRenderer() {
        super(new SkullcrusherGeoModel());
    }
}
