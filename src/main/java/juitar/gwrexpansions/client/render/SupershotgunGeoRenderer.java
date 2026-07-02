package juitar.gwrexpansions.client.render;

import juitar.gwrexpansions.client.model.SupershotgunGeoModel;
import juitar.gwrexpansions.item.vanilla.Supershotgun;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class SupershotgunGeoRenderer extends GeoItemRenderer<Supershotgun> {
    public SupershotgunGeoRenderer() {
        super(new SupershotgunGeoModel());
    }
}
