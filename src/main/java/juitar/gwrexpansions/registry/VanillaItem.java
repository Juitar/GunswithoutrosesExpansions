package juitar.gwrexpansions.registry;

import juitar.gwrexpansions.config.GWREConfig;
import juitar.gwrexpansions.item.ConfigurableGatlingItem;
import juitar.gwrexpansions.item.ConfigurableGunItem;
import juitar.gwrexpansions.item.vanilla.GoldenBulletItem;
import juitar.gwrexpansions.item.vanilla.ReshotableBulletItem;
import juitar.gwrexpansions.item.vanilla.SlimeBulletItem;
import juitar.gwrexpansions.item.vanilla.SplinterBulletItem;
import lykrast.gunswithoutroses.item.BulletItem;
import lykrast.gunswithoutroses.item.GunItem;
import lykrast.gunswithoutroses.item.PiercingBulletItem;
import lykrast.gunswithoutroses.registry.GWRSounds;

import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.RegistryObject;

public class VanillaItem {
    public static RegistryObject<GunItem> netherite_sniper, netherite_gatling, netherite_shotgun;
    public static RegistryObject<BulletItem> slime_bullet,diamond_bullet,netherite_bullet,golden_bullet;
    public static RegistryObject<BulletItem> diamond_bullet_shrapnel;

    public static void registerItems() {
        netherite_shotgun = GWREItem.initItem(() -> new ConfigurableGunItem(
            GWREItem.defP().durability(4000).fireResistant(),
            0, 0.0, 0, 0.0, 0,
            () -> GWREConfig.SHOTGUN.netherite
        )
            .projectiles(5)
            .fireSound(GWRSounds.shotgun)
            .repair(() -> Ingredient.of(Tags.Items.INGOTS_NETHERITE)), 
        "netherite_shotgun");

        netherite_sniper = GWREItem.initItem(() -> new ConfigurableGunItem(
            GWREItem.defP().durability(4000).fireResistant(),
            0, 0.0, 0, 0.0, 0,
            () -> GWREConfig.SNIPER.netherite
        )
            .projectileSpeed(4)
            .headshotMult(1.5)
            .fireSound(GWRSounds.sniper)
            .repair(() -> Ingredient.of(Tags.Items.INGOTS_NETHERITE)), 
        "netherite_sniper");

        netherite_gatling = GWREItem.initItem(() -> new ConfigurableGatlingItem(
            GWREItem.defP().durability(4000).fireResistant(),
            0, 0.0, 0, 0.0, 0,
            () -> GWREConfig.GATLING.netherite
        )
            .repair(() -> Ingredient.of(Tags.Items.INGOTS_NETHERITE)),
        "netherite_gatling");

        slime_bullet = GWREItem.initItem(()-> new SlimeBulletItem(GWREItem.defP(),6,3),"slime_bullet");
        diamond_bullet = GWREItem.initItem(()-> new SplinterBulletItem(GWREItem.defP(),7),"diamond_bullet");
        golden_bullet = GWREItem.initItem(()-> new GoldenBulletItem(GWREItem.defP(),5),"golden_bullet");
        netherite_bullet = GWREItem.initItem(()-> new ReshotableBulletItem(GWREItem.defP().fireResistant(),8),"netherite_bullet");
        diamond_bullet_shrapnel = GWREItem.REG.register("diamond_bullet_shrapnel", () -> new PiercingBulletItem(GWREItem.defP(), 3,2));
    }
}