package juitar.gwrexpansions.registry;

import lykrast.gunswithoutroses.item.BulletItem;
import lykrast.gunswithoutroses.item.GatlingItem;
import lykrast.gunswithoutroses.item.GunItem;
import lykrast.gunswithoutroses.registry.GWRSounds;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.RegistryObject;

public class VanillaItem {
    public static RegistryObject<Item> netherite_sniper, netherite_galtling, netherite_shotgun;
    public static RegistryObject<Item> netherite_bullet;

    public static void registerItems() {
        netherite_shotgun = GWREItem.initItem(()-> new GunItem(GWREItem.defP().durability(4000),0,0.7,20,5,7).projectiles(5).fireSound(GWRSounds.shotgun).repair(() -> Ingredient.of(Tags.Items.INGOTS_NETHERITE)), "netherite_shotgun");
        netherite_sniper = GWREItem.initItem(() -> new GunItem(GWREItem.defP().durability(4000), 1, 1.8, 24, 0, 10).projectileSpeed(4).headshotMult(1.5).fireSound(GWRSounds.sniper).repair(() -> Ingredient.of(Tags.Items.INGOTS_NETHERITE)), "netherite_sniper");
        netherite_galtling = GWREItem.initItem(()-> new GatlingItem(GWREItem.defP().durability(4000),1,1,4,3,10).repair(() -> Ingredient.of(Tags.Items.INGOTS_NETHERITE)),"netherite_gatling");

        netherite_bullet = GWREItem.initItem(()-> new BulletItem(GWREItem.defP(),8),"netherite_bullet");
    }
}