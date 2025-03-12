package juitar.gwrexpansions.registry;

import juitar.gwrexpansions.GWRexpansions;
import juitar.gwrexpansions.item.cataclysm.*;
import lykrast.gunswithoutroses.item.BulletItem;
import lykrast.gunswithoutroses.item.GunItem;
import lykrast.gunswithoutroses.registry.GWRSounds;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraftforge.registries.RegistryObject;
import net.minecraft.world.item.Item;

public class CompatCataclysm {
    public static RegistryObject<GunItem> ignitium_gatling,cursium_sniper,netherite_monster_shotgun;
    public static RegistryObject<BulletItem> ignitium_bullet,cursium_bullet,lavapower_bullet;
    public static TagKey<Item> tagBaseBullets = ItemTags.create(GWRexpansions.resource("lavapowerbullet_base"));
    public static void registerItems() {

        netherite_monster_shotgun = GWREItem.initItem( () -> new LavapowerGunItem(GWREItem.defP().durability(10000).fireResistant(),0,0.8,25,2,20).projectiles(5).fireSound(GWRSounds.shotgun),"netherite_monster_shotgun");
        cursium_sniper = GWREItem.initItem( () -> new CursiumGunItem(GWREItem.defP().durability(10000).fireResistant(),0,2,25,0,20).projectileSpeed(4).headshotMult(1.5).fireSound(GWRSounds.sniper),"cursium_sniper");
        ignitium_gatling = GWREItem.initItem( () -> new IgnitiumGatlingItem(GWREItem.defP().durability(10000).fireResistant(),4,1,4,3,20),"ignitium_gatling");

        ignitium_bullet = GWREItem.initItem( () -> new IgnitiumBulletItem(GWREItem.defP().fireResistant(),12),"ignitium_bullet");
        lavapower_bullet = GWREItem.initItem( () -> new LavapowerBulletItem(GWREItem.defP().fireResistant(),9),"lavapower_bullet");
        cursium_bullet = GWREItem.initItem( () -> new CursiumBulletItem(GWREItem.defP().fireResistant(),14),"cursium_bullet");
    }
}
