package juitar.gwrexpansions.registry;

import juitar.gwrexpansions.item.minecraft.ReshotableBulletItem;
import juitar.gwrexpansions.item.minecraft.SlimeBulletItem;
import juitar.gwrexpansions.item.minecraft.SplinterBulletItem;
import lykrast.gunswithoutroses.item.BulletItem;
import lykrast.gunswithoutroses.item.GatlingItem;
import lykrast.gunswithoutroses.item.GunItem;
import lykrast.gunswithoutroses.registry.GWRSounds;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.RegistryObject;

public class VanillaItem {
    public static RegistryObject<GunItem> netherite_sniper, netherite_galtling, netherite_shotgun;
    public static RegistryObject<BulletItem> slime_bullet,diamond_bullet,netherite_bullet;
    public static RegistryObject<BulletItem> diamond_bullet_shrapnel;

    public static void registerItems() {
        netherite_shotgun = GWREItem.initItem(()-> new GunItem(GWREItem.defP().durability(4000).fireResistant(),0,0.6,20,5,7).projectiles(5).fireSound(GWRSounds.shotgun).repair(() -> Ingredient.of(Tags.Items.INGOTS_NETHERITE)), "netherite_shotgun");
        netherite_sniper = GWREItem.initItem(() -> new GunItem(GWREItem.defP().durability(4000).fireResistant(), 0, 1.8, 24, 0, 10).projectileSpeed(4).headshotMult(1.5).fireSound(GWRSounds.sniper).repair(() -> Ingredient.of(Tags.Items.INGOTS_NETHERITE)), "netherite_sniper");
        netherite_galtling = GWREItem.initItem(()-> new GatlingItem(GWREItem.defP().durability(4000).fireResistant(),1,1,4,3,10).repair(() -> Ingredient.of(Tags.Items.INGOTS_NETHERITE)),"netherite_gatling");
        slime_bullet = GWREItem.initItem(()-> new SlimeBulletItem(GWREItem.defP(),6,3),"slime_bullet");
        diamond_bullet = GWREItem.initItem(()-> new SplinterBulletItem(GWREItem.defP(),7),"diamond_bullet");

        netherite_bullet = GWREItem.initItem(()-> new ReshotableBulletItem(GWREItem.defP().fireResistant(),8),"netherite_bullet");
        diamond_bullet_shrapnel = GWREItem.REG.register("diamond_bullet_shrapnel", () -> new BulletItem(GWREItem.defP(), 3));
    }
}