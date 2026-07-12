package juitar.gwrexpansions.compat.kubejs;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class KubeJSGunItemBuilder extends AbstractKubeJSGunBuilder {
    public KubeJSGunItemBuilder(ResourceLocation id) { super(id, new ResourceLocation("gunswithoutroses", "gun/pistol")); }
    @Override public Item createObject() { return new KubeJSGunItem(createItemProperties(), this); }
}
