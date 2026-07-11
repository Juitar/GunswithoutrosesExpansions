package juitar.gwrexpansions.client.render;

import juitar.gwrexpansions.client.model.CursiumBallistaGeoModel;
import juitar.gwrexpansions.item.cataclysm.CursiumGunItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class CursiumBallistaGeoRenderer extends GeoItemRenderer<CursiumGunItem> {
    public CursiumBallistaGeoRenderer() {
        super(new CursiumBallistaGeoModel());
    }
}
