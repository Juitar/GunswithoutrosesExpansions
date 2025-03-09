package juitar.gwrexpansions.registry;

import juitar.gwrexpansions.GWRexpansions;
import juitar.gwrexpansions.item.iceandfire.*;
import lykrast.gunswithoutroses.item.BulletItem;
import lykrast.gunswithoutroses.item.GunItem;
import lykrast.gunswithoutroses.registry.GWRSounds;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.RegistryObject;

public class CompatIceandfire {
    public static RegistryObject<GunItem> dragonsteel_fire_shotgun,dragonsteel_fire_sniper,dragonsteel_fire_gatling,
                                dragonsteel_ice_shotgun,dragonsteel_ice_sniper,dragonsteel_ice_gatling,
                                dragonsteel_lightning_shotgun,dragonsteel_lightning_sniper,dragonsteel_lightning_gatling;
    public static RegistryObject<BulletItem> silver_bullet,dragonsteel_fire_bullet,dragonsteel_ice_bullet,dragonsteel_lightning_bullet;
    public static TagKey<Item> tagBaseBullets = ItemTags.create(GWRexpansions.resource("dragonsteelbullet_base"));
    public static void registerItems(){
        dragonsteel_fire_shotgun = GWREItem.initItem(()->new FireDragonGunItem(GWREItem.defP().durability(8000),0,0.75,20,4,15).projectiles(4).fireSound(GWRSounds.shotgun),"dragonsteel_fire_shotgun");
        dragonsteel_fire_sniper = GWREItem.initItem(()->new FireDragonGunItem(GWREItem.defP().durability(8000),0,1.9,24,0,15).projectileSpeed(4).fireSound(GWRSounds.sniper),"dragonsteel_fire_sniper");
        dragonsteel_fire_gatling = GWREItem.initItem(()->new FireDragonGunItem(GWREItem.defP().durability(8000),3,1,4,4,15),"dragonsteel_fire_gatling");

        dragonsteel_ice_shotgun = GWREItem.initItem(()->new IceDragonGunItem(GWREItem.defP().durability(8000),0,0.75,20,4,15).projectiles(4).fireSound(GWRSounds.shotgun),"dragonsteel_ice_shotgun");
        dragonsteel_ice_sniper = GWREItem.initItem(()->new IceDragonGunItem(GWREItem.defP().durability(8000),0,1.9,24,0,15).projectileSpeed(4).fireSound(GWRSounds.sniper),"dragonsteel_ice_sniper");
        dragonsteel_ice_gatling = GWREItem.initItem(()->new IceDragonGunItem(GWREItem.defP().durability(8000),3,1,4,4,15),"dragonsteel_ice_gatling");

        dragonsteel_lightning_shotgun = GWREItem.initItem(()->new LightningDragonGunItem(GWREItem.defP().durability(8000),0,0.75,20,4,15).projectiles(4).fireSound(GWRSounds.shotgun),"dragonsteel_lightning_shotgun");
        dragonsteel_lightning_sniper = GWREItem.initItem(()->new LightningDragonGunItem(GWREItem.defP().durability(8000),0,1.9,24,0,15).projectileSpeed(4).fireSound(GWRSounds.sniper),"dragonsteel_lightning_sniper");
        dragonsteel_lightning_gatling = GWREItem.initItem(()->new LightningDragonGunItem(GWREItem.defP().durability(8000),3,1,4,4,15),"dragonsteel_lightning_gatling");


        silver_bullet = GWREItem.initItem(()->new SilverBulletItem(GWREItem.defP(),6),"silver_bullet");
        dragonsteel_ice_bullet = GWREItem.initItem(()->new IceDragonSteelBulletItem(GWREItem.defP(),10),"dragonsteel_ice_bullet");
        dragonsteel_fire_bullet = GWREItem.initItem(()->new FireDragonSteelBulletItem(GWREItem.defP(),10),"dragonsteel_fire_bullet");
        dragonsteel_lightning_bullet = GWREItem.initItem(()->new LightningDragonSteelBulletItem(GWREItem.defP(),10),"dragonsteel_lightning_bullet");
    }
}
