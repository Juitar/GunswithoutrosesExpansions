package juitar.gwrexpansions.client.render;

import juitar.gwrexpansions.client.model.DestinySevenGeoModel;
import juitar.gwrexpansions.item.meetyourfight.DestinyGunItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class DestinySevenGeoRenderer extends GeoItemRenderer<DestinyGunItem> {
    public DestinySevenGeoRenderer() {
        super(new DestinySevenGeoModel());
    }
}
