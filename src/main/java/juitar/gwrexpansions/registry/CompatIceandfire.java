package juitar.gwrexpansions.registry;

import juitar.gwrexpansions.config.GWREConfig;
import juitar.gwrexpansions.CompatModids;
import juitar.gwrexpansions.GWRexpansions;
import juitar.gwrexpansions.item.iceandfire.*;
import lykrast.gunswithoutroses.item.BulletItem;
import lykrast.gunswithoutroses.item.GunItem;
import lykrast.gunswithoutroses.registry.GWRSounds;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Supplier;


public class CompatIceandfire {
    public static RegistryObject<GunItem> dragonsteel_fire_shotgun,dragonsteel_fire_sniper,dragonsteel_fire_gatling,
                                dragonsteel_ice_shotgun,dragonsteel_ice_sniper,dragonsteel_ice_gatling,
                                dragonsteel_lightning_shotgun,dragonsteel_lightning_sniper,dragonsteel_lightning_gatling;
    public static RegistryObject<BulletItem> silver_bullet,dragonsteel_fire_bullet,dragonsteel_ice_bullet,dragonsteel_lightning_bullet;
    public static TagKey<Item> tagBaseBullets = ItemTags.create(GWRexpansions.resource("dragonsteelbullet_base"));
    public static Supplier<Item> FIRE_DRAGON_STEEL_INGOT = () -> ForgeRegistries.ITEMS.getValue(
            new ResourceLocation(CompatModids.ICEANDFIRE, "dragonsteel_fire_ingot"));
    public static Supplier<Item> ICE_DRAGON_STEEL_INGOT = () -> ForgeRegistries.ITEMS.getValue(
            new ResourceLocation(CompatModids.ICEANDFIRE, "dragonsteel_ice_ingot"));
    public static Supplier<Item> LIGHTNING_DRAGON_STEEL_INGOT = () -> ForgeRegistries.ITEMS.getValue(
            new ResourceLocation(CompatModids.ICEANDFIRE, "dragonsteel_lightning_ingot"));
    public static void registerItems(){

        dragonsteel_fire_shotgun = GWREItems.initItem(()->new FireDragonGunItem(GWREItems.defP().durability(8000),0,0.75,20,4,15,()-> GWREConfig.SHOTGUN.DragonSteel).projectiles(4).fireSound(GWRSounds.shotgun).repair(()-> Ingredient.of(FIRE_DRAGON_STEEL_INGOT.get())),"dragonsteel_fire_shotgun");
        dragonsteel_fire_sniper = GWREItems.initItem(()->new FireDragonGunItem(GWREItems.defP().durability(8000),0,1.9,24,0,15,()-> GWREConfig.SNIPER.DragonSteel).projectileSpeed(4).headshotMult(2).fireSound(GWRSounds.sniper).repair(()-> Ingredient.of(FIRE_DRAGON_STEEL_INGOT.get())),"dragonsteel_fire_sniper");
        dragonsteel_fire_gatling = GWREItems.initItem(()->new FireDragonGatlingItem(GWREItems.defP().durability(8000),3,1,4,4,15,()-> GWREConfig.GATLING.DragonSteel).repair(()-> Ingredient.of(FIRE_DRAGON_STEEL_INGOT.get())),"dragonsteel_fire_gatling");

        dragonsteel_ice_shotgun = GWREItems.initItem(()->new IceDragonGunItem(GWREItems.defP().durability(8000),0,0.75,20,4,15,()-> GWREConfig.SHOTGUN.DragonSteel).projectiles(4).fireSound(GWRSounds.shotgun).repair(()-> Ingredient.of(ICE_DRAGON_STEEL_INGOT.get())),"dragonsteel_ice_shotgun");
        dragonsteel_ice_sniper = GWREItems.initItem(()->new IceDragonGunItem(GWREItems.defP().durability(8000),0,1.9,24,0,15,()-> GWREConfig.SNIPER.DragonSteel).projectileSpeed(4).headshotMult(2).fireSound(GWRSounds.sniper).repair(()-> Ingredient.of(ICE_DRAGON_STEEL_INGOT.get())),"dragonsteel_ice_sniper");
        dragonsteel_ice_gatling = GWREItems.initItem(()->new IceDragonGatlingItem(GWREItems.defP().durability(8000),3,1,4,4,15,()-> GWREConfig.GATLING.DragonSteel).repair(()-> Ingredient.of(ICE_DRAGON_STEEL_INGOT.get())),"dragonsteel_ice_gatling");

        dragonsteel_lightning_shotgun = GWREItems.initItem(()->new LightningDragonGunItem(GWREItems.defP().durability(8000),0,0.75,20,4,15,()-> GWREConfig.SHOTGUN.DragonSteel).projectiles(4).fireSound(GWRSounds.shotgun).repair(()-> Ingredient.of(LIGHTNING_DRAGON_STEEL_INGOT.get())),"dragonsteel_lightning_shotgun");
        dragonsteel_lightning_sniper = GWREItems.initItem(()->new LightningDragonGunItem(GWREItems.defP().durability(8000),0,1.9,24,0,15,()-> GWREConfig.SNIPER.DragonSteel).projectileSpeed(4).headshotMult(2).fireSound(GWRSounds.sniper).repair(()-> Ingredient.of(LIGHTNING_DRAGON_STEEL_INGOT.get())),"dragonsteel_lightning_sniper");
        dragonsteel_lightning_gatling = GWREItems.initItem(()->new LightningDragonGatlingItem(GWREItems.defP().durability(8000),3,1,4,4,15,()-> GWREConfig.GATLING.DragonSteel).repair(()-> Ingredient.of(LIGHTNING_DRAGON_STEEL_INGOT.get())),"dragonsteel_lightning_gatling");


        silver_bullet = GWREItems.initItem(()->new SilverBulletItem(GWREItems.defP(),6),"silver_bullet");
        dragonsteel_ice_bullet = GWREItems.initItem(()->new IceDragonSteelBulletItem(GWREItems.defP(),10),"dragonsteel_ice_bullet");
        dragonsteel_fire_bullet = GWREItems.initItem(()->new FireDragonSteelBulletItem(GWREItems.defP(),10),"dragonsteel_fire_bullet");
        dragonsteel_lightning_bullet = GWREItems.initItem(()->new LightningDragonSteelBulletItem(GWREItems.defP(),10),"dragonsteel_lightning_bullet");
    }
}
