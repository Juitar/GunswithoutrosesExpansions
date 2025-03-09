package juitar.gwrexpansions.registry;

import juitar.gwrexpansions.item.iceandfire.FireDragonSteelBulletItem;
import juitar.gwrexpansions.item.iceandfire.IceDragonSteelBulletItem;
import juitar.gwrexpansions.item.iceandfire.LightningDragonSteelBulletItem;
import juitar.gwrexpansions.item.iceandfire.SilverBulletItem;
import lykrast.gunswithoutroses.item.BulletItem;
import lykrast.gunswithoutroses.item.GunItem;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.RegistryObject;

public class CompatIceandfire {
    public static RegistryObject<GunItem> dragonsteel_fire_shotgun,dragonsteel_fire_sniper,dragonsteel_fire_gatling,
                                dragonsteel_ice_shotgun,dragonsteel_ice_sniper,dragonsteel_ice_gatling,
                                dragonsteel_lightning_shotgun,dragonsteel_lightning_sniper,dragonsteel_lightning_gatling;
    public static RegistryObject<BulletItem> silver_bullet,dragonsteel_fire_bullet,dragonsteel_ice_bullet,dragonsteel_lightning_bullet;

    public static void registerItems(){


        silver_bullet = GWREItem.initItem(()->new SilverBulletItem(GWREItem.defP(),6),"silver_bullet");
        dragonsteel_ice_bullet = GWREItem.initItem(()->new IceDragonSteelBulletItem(GWREItem.defP(),10),"dragonsteel_ice_bullet");
        dragonsteel_fire_bullet = GWREItem.initItem(()->new FireDragonSteelBulletItem(GWREItem.defP(),10),"dragonsteel_fire_bullet");
        dragonsteel_lightning_bullet = GWREItem.initItem(()->new LightningDragonSteelBulletItem(GWREItem.defP(),10),"dragonsteel_lightning_bullet");
    }
}
