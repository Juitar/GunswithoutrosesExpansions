package juitar.gwrexpansions.registry;

import juitar.gwrexpansions.config.GWREConfig;
import juitar.gwrexpansions.item.ConfigurableGatlingItem;
import juitar.gwrexpansions.item.ConfigurableGunItem;
import juitar.gwrexpansions.item.vanilla.*;
import lykrast.gunswithoutroses.item.BulletItem;
import lykrast.gunswithoutroses.item.GunItem;
import lykrast.gunswithoutroses.item.PiercingBulletItem;
import lykrast.gunswithoutroses.registry.GWRSounds;

import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.RegistryObject;

public class VanillaItem {
    public static RegistryObject<GunItem> netherite_sniper, netherite_gatling, netherite_shotgun,super_shotgun;
    public static RegistryObject<BulletItem> slime_bullet,diamond_bullet,netherite_bullet,golden_bullet,maws_of_gluttony,hunger_bullet;
    public static RegistryObject<BulletItem> diamond_bullet_shrapnel;

    public static void registerItems() {

        netherite_shotgun = GWREItems.initItem(() -> new ConfigurableGunItem(
            GWREItems.defP().durability(4000).fireResistant(),
            0, 0.0, 0, 0.0, 0,
            () -> GWREConfig.SHOTGUN.Netherite
        )
            .projectiles(5)
            .fireSound(GWRSounds.shotgun)
            .repair(() -> Ingredient.of(Tags.Items.INGOTS_NETHERITE)), 
        "netherite_shotgun");

        netherite_sniper = GWREItems.initItem(() -> new ConfigurableGunItem(
            GWREItems.defP().durability(4000).fireResistant(),
            0, 0.0, 0, 0.0, 0,
            () -> GWREConfig.SNIPER.netherite
        )
            .projectileSpeed(4)
            .headshotMult(1.5)
            .fireSound(GWRSounds.sniper)
            .repair(() -> Ingredient.of(Tags.Items.INGOTS_NETHERITE)), 
        "netherite_sniper");

        netherite_gatling = GWREItems.initItem(() -> new ConfigurableGatlingItem(
            GWREItems.defP().durability(4000).fireResistant(),
            0, 0.0, 0, 0.0, 0,
            () -> GWREConfig.GATLING.Netherite
        )
            .repair(() -> Ingredient.of(Tags.Items.INGOTS_NETHERITE)),
        "netherite_gatling");

        slime_bullet = GWREItems.initItem(()-> new SlimeBulletItem(GWREItems.defP(),6,3),"slime_bullet");
        diamond_bullet = GWREItems.initItem(()-> new SplinterBulletItem(GWREItems.defP(),7),"diamond_bullet");
        golden_bullet = GWREItems.initItem(()-> new GoldenBulletItem(GWREItems.defP(),5),"golden_bullet");
        netherite_bullet = GWREItems.initItem(()-> new ReshotableBulletItem(GWREItems.defP().fireResistant(),8),"netherite_bullet");
        maws_of_gluttony = GWREItems.initItem(() -> new HungerBulletItem(GWREItems.defP().fireResistant(), 5), "maws_of_gluttony");
        diamond_bullet_shrapnel = GWREItems.REG.register("diamond_bullet_shrapnel", () -> new PiercingBulletItem(GWREItems.defP(), 3,2));
        hunger_bullet = GWREItems.REG.register("hunger_bullet", () -> new BulletItem(GWREItems.defP().fireResistant(), 5));
        super_shotgun =  GWREItems.initItem(() -> new Supershotgun(
            GWREItems.defP().durability(4000).fireResistant(),
            0, 0.0, 0, 0.0, 0,
            () -> GWREConfig.SHOTGUN.Supershotgun
        )
            .projectiles(8)
            .projectileSpeed(4)
            .fireSound(GWRESounds.supershotgun)
            .repair(() -> Ingredient.of(Tags.Items.INGOTS_NETHERITE)),"super_shotgun"
        );
    }
}