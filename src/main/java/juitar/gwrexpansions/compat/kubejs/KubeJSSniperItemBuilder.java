package juitar.gwrexpansions.compat.kubejs;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

public class KubeJSSniperItemBuilder extends AbstractKubeJSGunBuilder {
    public KubeJSSniperItemBuilder(ResourceLocation id) {
        super(id, new ResourceLocation("gunswithoutroses", "gun/sniper"));
        headshotMultiplier = 2.0D;
        projectileSpeed = 5.0D;
        inaccuracy = 0.0D;
    }
    @Override public Item createObject() { return new KubeJSGunItem(createItemProperties(), this); }
}
