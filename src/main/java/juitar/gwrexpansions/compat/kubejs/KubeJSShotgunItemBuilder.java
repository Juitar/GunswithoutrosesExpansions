package juitar.gwrexpansions.compat.kubejs;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class KubeJSShotgunItemBuilder extends AbstractKubeJSGunBuilder {
    public KubeJSShotgunItemBuilder(ResourceLocation id) { super(id, new ResourceLocation("gunswithoutroses", "gun/shotgun")); projectiles = 6; }
    @Override public Item createObject() { return new KubeJSShotgunItem(createItemProperties(), this); }
}
