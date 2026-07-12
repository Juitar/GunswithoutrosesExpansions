package juitar.gwrexpansions.compat.kubejs;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.registry.RegistryInfo;

public class GWREKubeJSPlugin extends KubeJSPlugin {
    @Override
    public void init() {
        RegistryInfo.ITEM.addType("gwrexpansions:gun", KubeJSGunItemBuilder.class, KubeJSGunItemBuilder::new);
        RegistryInfo.ITEM.addType("gwrexpansions:gatling", KubeJSGatlingItemBuilder.class, KubeJSGatlingItemBuilder::new);
        RegistryInfo.ITEM.addType("gwrexpansions:shotgun", KubeJSShotgunItemBuilder.class, KubeJSShotgunItemBuilder::new);
        RegistryInfo.ITEM.addType("gwrexpansions:sniper", KubeJSSniperItemBuilder.class, KubeJSSniperItemBuilder::new);
    }
}
