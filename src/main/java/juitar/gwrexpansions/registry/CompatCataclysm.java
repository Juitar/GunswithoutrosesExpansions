package juitar.gwrexpansions.registry;

import juitar.gwrexpansions.GWRexpansions;
import juitar.gwrexpansions.item.cataclysm.CursiumBulletItem;
import juitar.gwrexpansions.item.cataclysm.LavapowerGunItem;
import juitar.gwrexpansions.item.cataclysm.LavapowerBulletItem;
import lykrast.gunswithoutroses.item.BulletItem;
import lykrast.gunswithoutroses.item.GunItem;
import lykrast.gunswithoutroses.registry.GWRSounds;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraftforge.registries.RegistryObject;
import net.minecraft.world.item.Item;

public class CompatCataclysm {
    public static RegistryObject<GunItem> cursium_sniper,netherite_monster_shotgun;
    public static RegistryObject<BulletItem> cursium_bullet,lavapower_bullet;
    public static TagKey<Item> tagBaseBullets = ItemTags.create(GWRexpansions.resource("lavapowerbullet_base"));
    public static void registerItems() {
        netherite_monster_shotgun = GWREItem.initItem( () -> new LavapowerGunItem(GWREItem.defP().durability(1000).fireResistant(),0,0.8,25,3,10).projectiles(2).fireSound(GWRSounds.shotgun),"netherite_monster_shotgun");
        lavapower_bullet = GWREItem.initItem( () -> new LavapowerBulletItem(GWREItem.defP().fireResistant(),9),"lavapower_bullet");
        cursium_bullet = GWREItem.initItem( () -> new CursiumBulletItem(GWREItem.defP().fireResistant(),14),"cursium_bullet");
    }
}
