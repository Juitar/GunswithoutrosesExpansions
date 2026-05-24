package juitar.gwrexpansions.registry;

import juitar.gwrexpansions.CompatModids;
import juitar.gwrexpansions.GWRexpansions;
import juitar.gwrexpansions.config.GWREConfig;
import juitar.gwrexpansions.item.alexscaves.MechanicPlaceholderItem;
import juitar.gwrexpansions.item.cataclysm.*;
import com.github.L_Ender.cataclysm.init.ModSounds;
import lykrast.gunswithoutroses.item.BulletItem;
import lykrast.gunswithoutroses.item.GunItem;
import lykrast.gunswithoutroses.registry.GWRSounds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

public class CompatCataclysm {
    public static RegistryObject<GunItem> ignitium_gatling,cursium_sniper,netherite_monster_shotgun,tidal_pistol,harbinger_raycaster,remnant_fangshot,ceraunus_burst;
    public static RegistryObject<BulletItem> ignitium_bullet,cursium_bullet,lavapower_bullet;
    public static RegistryObject<Item> tidal_bullet,voidfall_launcher;
    public static TagKey<Item> tagBaseBullets = ItemTags.create(GWRexpansions.resource("lavapowerbullet_base"));

    public static Supplier<Item> CURSIUM_INGOT = () -> ForgeRegistries.ITEMS.getValue(
            new ResourceLocation(CompatModids.CATACLYSM,"cursium_ingot"));
    public static Supplier<Item> IGNITIUM_INGOT = () -> ForgeRegistries.ITEMS.getValue(
            new ResourceLocation(CompatModids.CATACLYSM,"ignitium_ingot"));
    public static Supplier<Item> MONSTROUS_HORN = () -> ForgeRegistries.ITEMS.getValue(
            new ResourceLocation(CompatModids.CATACLYSM,"monstrous_horn"));
    public static Supplier<Item> ANCIENT_METAL_INGOT = () -> ForgeRegistries.ITEMS.getValue(
            new ResourceLocation(CompatModids.CATACLYSM,"ancient_metal_ingot"));
    public static Supplier<Item> WITHERITE_INGOT = () -> ForgeRegistries.ITEMS.getValue(
            new ResourceLocation(CompatModids.CATACLYSM,"witherite_ingot"));

    public static void registerItems() {

        netherite_monster_shotgun = GWREItems.initItem( () -> new LavapowerGunItem(GWREItems.defP().durability(10000).fireResistant().rarity(Rarity.EPIC)
                ,0,0.8,25,4.0,20,
                () -> GWREConfig.SHOTGUN.NetheriteMonster).projectiles(5).fireSound(GWRSounds.shotgun).repair(()-> Ingredient.of(MONSTROUS_HORN.get())),
                "netherite_monster_shotgun");

        cursium_sniper = GWREItems.initItem( () -> new CursiumGunItem(GWREItems.defP().durability(10000).fireResistant().rarity(Rarity.EPIC),
                0,2,25,0,20,
                () -> GWREConfig.SNIPER.cursium).projectileSpeed(4).headshotMult(1.5).fireSound(GWRSounds.sniper).repair(()-> Ingredient.of(CURSIUM_INGOT.get())),
                "cursium_sniper");

        ignitium_gatling = GWREItems.initItem( () -> new IgnitiumGatlingItem(GWREItems.defP().durability(10000).fireResistant().rarity(Rarity.EPIC),
                4,1,4,3,20,
                () -> GWREConfig.GATLING.Ignitium).repair(()-> Ingredient.of(IGNITIUM_INGOT.get())),"ignitium_gatling");

        tidal_pistol = GWREItems.initItem( () -> new TidalGunItem(GWREItems.defP().durability(10000).fireResistant().rarity(Rarity.EPIC),
                0,1.5,0,2.0,0,
                () -> GWREConfig.PISTOL.tidal),"tidal_pistol");

        ignitium_bullet = GWREItems.initItem( () -> new IgnitiumBulletItem(GWREItems.defP().fireResistant(),12),"ignitium_bullet");
        lavapower_bullet = GWREItems.initItem( () -> new LavapowerBulletItem(GWREItems.defP().fireResistant(),9),"lavapower_bullet");
        cursium_bullet = GWREItems.initItem( () -> new CursiumBulletItem(GWREItems.defP().fireResistant(), 14),"cursium_bullet");
        tidal_bullet = GWREItems.REG.register("tidal_bullet",()-> new TidalBulletItem(GWREItems.defP().fireResistant(), 12));

        voidfall_launcher = GWREItems.initItem(() -> new MechanicPlaceholderItem(
                GWREItems.noStack().fireResistant().rarity(Rarity.EPIC),
                "tooltip.gwrexpansions.voidfall_launcher.desc"), "voidfall_launcher");
        harbinger_raycaster = GWREItems.initItem(() -> new HarbingerRaycasterItem(
                GWREItems.defP().durability(10000).fireResistant().rarity(Rarity.EPIC),
                0, 1.6, 26, 0.0, 20,
                () -> GWREConfig.SNIPER.harbingerRaycaster)
                .projectileSpeed(4.0).headshotMult(1.75).fireSound(ModSounds.HARBINGER_LASER)
                .repair(() -> Ingredient.of(WITHERITE_INGOT.get())),
                "harbinger_raycaster");
        remnant_fangshot = GWREItems.initItem(() -> new RemnantFangshotItem(
                GWREItems.defP().durability(10000).fireResistant().rarity(Rarity.EPIC),
                0, 0.65, 24, 4.0, 20,
                () -> GWREConfig.SHOTGUN.RemnantFangshot)
                .projectiles(4).fireSound(GWRSounds.shotgun).repair(() -> Ingredient.of(ANCIENT_METAL_INGOT.get())),
                "remnant_fangshot");
        ceraunus_burst = GWREItems.initItem(() -> new CeraunusBurstItem(
                GWREItems.defP().durability(10000).fireResistant().rarity(Rarity.EPIC),
                0, 0.72, 26, 1.35, 20, 3, 3,
                () -> GWREConfig.BURSTGUN.ceraunusBurst)
                .fireSound(GWRSounds.gun).repair(() -> Ingredient.of(ANCIENT_METAL_INGOT.get())),
                "ceraunus_burst");
    }
}
