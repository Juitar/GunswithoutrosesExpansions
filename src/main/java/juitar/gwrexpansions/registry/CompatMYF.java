package juitar.gwrexpansions.registry;

import juitar.gwrexpansions.config.GWREConfig;
import juitar.gwrexpansions.item.meetyourfight.DestinyGunItem;
import lykrast.gunswithoutroses.item.GunItem;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.RegistryObject;

public class CompatMYF {
    public static RegistryObject<GunItem> destiny_seven;
    public static void registerItems(){
        destiny_seven = GWREItems.initItem(()->new DestinyGunItem(GWREItems.defP().durability(5000).fireResistant().rarity(Rarity.EPIC),
                0,0,0,0,20,
                ()->GWREConfig.SNIPER.destiny_seven).projectileSpeed(4).headshotMult(1.5),"destiny_seven");
    }
}
