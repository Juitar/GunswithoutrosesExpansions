package juitar.gwrexpansions.registry;

import juitar.gwrexpansions.CompatModids;
import juitar.gwrexpansions.GWRexpansions;
import juitar.gwrexpansions.config.GWREConfig;
import juitar.gwrexpansions.item.cataclysm.*;
import lykrast.gunswithoutroses.item.BulletItem;
import lykrast.gunswithoutroses.item.GunItem;
import lykrast.gunswithoutroses.registry.GWRSounds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

public class CompatCataclysm {
    public static RegistryObject<GunItem> ignitium_gatling,cursium_sniper,netherite_monster_shotgun,tidal_pistol;
    public static RegistryObject<BulletItem> ignitium_bullet,cursium_bullet,lavapower_bullet;
    public static RegistryObject<Item> tidal_bullet;
    public static TagKey<Item> tagBaseBullets = ItemTags.create(GWRexpansions.resource("lavapowerbullet_base"));

    public static Supplier<Item> CURSIUM_INGOT = () -> ForgeRegistries.ITEMS.getValue(
            new ResourceLocation(CompatModids.CATACLYSM,"cursium_ingot"));
    public static Supplier<Item> IGNITIUM_INGOT = () -> ForgeRegistries.ITEMS.getValue(
            new ResourceLocation(CompatModids.CATACLYSM,"ignitium_ingot"));
    public static Supplier<Item> MONSTROUS_HORN = () -> ForgeRegistries.ITEMS.getValue(
            new ResourceLocation(CompatModids.CATACLYSM,"monstrous_horn"));

    public static void registerItems() {

        netherite_monster_shotgun = GWREItem.initItem( () -> new LavapowerGunItem(GWREItem.defP().durability(10000).fireResistant().rarity(Rarity.EPIC)
                ,0,0.8,25,2,20,
                () -> GWREConfig.SHOTGUN.netheriteMonster).projectiles(5).fireSound(GWRSounds.shotgun).repair(()-> Ingredient.of(MONSTROUS_HORN.get())),
                "netherite_monster_shotgun");

        cursium_sniper = GWREItem.initItem( () -> new CursiumGunItem(GWREItem.defP().durability(10000).fireResistant().rarity(Rarity.EPIC),
                0,2,25,0,20,
                () -> GWREConfig.SNIPER.cursium).projectileSpeed(4).headshotMult(1.5).fireSound(GWRSounds.sniper).repair(()-> Ingredient.of(CURSIUM_INGOT.get())),
                "cursium_sniper");

        ignitium_gatling = GWREItem.initItem( () -> new IgnitiumGatlingItem(GWREItem.defP().durability(10000).fireResistant().rarity(Rarity.EPIC),
                4,1,4,3,20,
                () -> GWREConfig.GATLING.ignitium).repair(()-> Ingredient.of(IGNITIUM_INGOT.get())),"ignitium_gatling");

        tidal_pistol = GWREItem.initItem( () -> new TidalGunItem(GWREItem.defP().durability(10000).fireResistant().rarity(Rarity.EPIC),
                0,1.5,0,0,0,
                () -> GWREConfig.PISTOL.tidal),"tidal_pistol");

        ignitium_bullet = GWREItem.initItem( () -> new IgnitiumBulletItem(GWREItem.defP().fireResistant(),12),"ignitium_bullet");
        lavapower_bullet = GWREItem.initItem( () -> new LavapowerBulletItem(GWREItem.defP().fireResistant(),9),"lavapower_bullet");
        cursium_bullet = GWREItem.initItem( () -> new CursiumBulletItem(GWREItem.defP().fireResistant(), 14),"cursium_bullet");
        tidal_bullet = GWREItem.REG.register("tidal_bullet",()-> new TidalBulletItem(GWREItem.defP().fireResistant(), 12));
    }
}
