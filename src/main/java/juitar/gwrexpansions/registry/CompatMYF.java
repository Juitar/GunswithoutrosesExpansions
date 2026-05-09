package juitar.gwrexpansions.registry;

import juitar.gwrexpansions.config.GWREConfig;
import juitar.gwrexpansions.item.meetyourfight.DestinyGunItem;
import juitar.gwrexpansions.item.meetyourfight.DuskfallEclipseBlasterItem;
import juitar.gwrexpansions.item.meetyourfight.MirecallerShotgunItem;
import lykrast.gunswithoutroses.item.GunItem;
import lykrast.meetyourfight.registry.MYFSounds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class CompatMYF {
    public static RegistryObject<GunItem> mirecaller_shotgun, duskfall_eclipse_blaster, destiny_seven;

    public static void registerItems(){
        mirecaller_shotgun = GWREItems.initItem(() -> new MirecallerShotgunItem(
                GWREItems.defP().durability(5000).rarity(Rarity.RARE),
                0, 0.55, 24, 4.5, 15,
                () -> GWREConfig.SHOTGUN.Mirecaller)
                .projectiles(3)
                .fireSound(MYFSounds.dredgedCannonadeShoot)
                .repair(() -> Ingredient.of(ForgeRegistries.ITEMS.getValue(new ResourceLocation("meetyourfight", "mossy_tooth")))),
                "mirecaller_shotgun");
        duskfall_eclipse_blaster = GWREItems.initItem(() -> new DuskfallEclipseBlasterItem(
                GWREItems.defP().durability(5000).rarity(Rarity.EPIC),
                0, 0.65, 28, 1.5, 20, 6, 3,
                () -> GWREConfig.BURSTGUN.duskfallEclipse)
                .fireSound(MYFSounds.roseSpiritShoot)
                .repair(() -> Ingredient.of(ForgeRegistries.ITEMS.getValue(new ResourceLocation("meetyourfight", "violet_bloom")))),
                "duskfall_eclipse_blaster");
        destiny_seven = GWREItems.initItem(()->new DestinyGunItem(GWREItems.defP().durability(5000).fireResistant().rarity(Rarity.EPIC),
                0,0,0,0,20,
                ()->GWREConfig.SNIPER.destiny_seven).projectileSpeed(4).headshotMult(1.5),"destiny_seven");
    }
}
