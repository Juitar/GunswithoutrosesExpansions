package juitar.gwrexpansions.registry;

import juitar.gwrexpansions.config.GWREConfig;
import juitar.gwrexpansions.item.BOMD.Skullcrusher;
import juitar.gwrexpansions.item.cataclysm.LavapowerGunItem;
import juitar.gwrexpansions.item.meetyourfight.DestinyGunItem;
import lykrast.gunswithoutroses.item.BulletItem;
import lykrast.gunswithoutroses.item.GunItem;
import lykrast.gunswithoutroses.item.PiercingBulletItem;
import lykrast.gunswithoutroses.registry.GWRSounds;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.RegistryObject;

public class CompatBOMD {
    public static RegistryObject<GunItem> skullcrusher_pulverizer;
    public static RegistryObject<BulletItem> skulls,bone_scrap;
    public static void registerItems(){
        skullcrusher_pulverizer  = GWREItems.initItem( () -> new Skullcrusher(GWREItems.defP().durability(6666).fireResistant().rarity(Rarity.EPIC)
                        ,0,0.8,6,2,20,
                        () -> GWREConfig.GATLING.skull).fireSound(GWRESounds.skullcrusher).repair(() -> Ingredient.of(Tags.Items.BONES)),
                "skullcrusher_pulverizer");
        skulls = GWREItems.initItem(()->new BulletItem(GWREItems.defP(), 1),"skulls");
        bone_scrap = GWREItems.REG.register("bone_scrap", () -> new PiercingBulletItem(GWREItems.defP(), 1,2));
    }
}
