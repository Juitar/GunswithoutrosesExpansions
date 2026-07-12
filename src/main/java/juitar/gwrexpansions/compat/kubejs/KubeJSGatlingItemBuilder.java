package juitar.gwrexpansions.compat.kubejs;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class KubeJSGatlingItemBuilder extends AbstractKubeJSGunBuilder {
    protected double fireDelayFractional = 2.0D;
    public KubeJSGatlingItemBuilder(ResourceLocation id) { super(id, new ResourceLocation("gunswithoutroses", "gun/gatling")); }
    public KubeJSGatlingItemBuilder fireDelayFractional(double value) { fireDelayFractional = atLeast(value, 0.05D, "fireDelayFractional"); return this; }
    @Override public Item createObject() { return new KubeJSGatlingItem(createItemProperties(), this); }
}
