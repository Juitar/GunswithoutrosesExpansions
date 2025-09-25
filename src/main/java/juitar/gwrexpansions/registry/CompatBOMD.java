package juitar.gwrexpansions.registry;

import juitar.gwrexpansions.config.GWREConfig;
import juitar.gwrexpansions.item.BOMD.BudBulletItem;
import juitar.gwrexpansions.item.BOMD.Hellforge;
import juitar.gwrexpansions.item.BOMD.ObsidianLauncher;
import juitar.gwrexpansions.item.BOMD.Skullcrusher;
import juitar.gwrexpansions.item.BOMD.Voidflower;
import lykrast.gunswithoutroses.item.BulletItem;
import lykrast.gunswithoutroses.item.GunItem;
import lykrast.gunswithoutroses.item.PiercingBulletItem;
import lykrast.gunswithoutroses.registry.GWRSounds;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class CompatBOMD {
    public static RegistryObject<GunItem> skullcrusher_pulverizer,obsidian_launcher,hellforge,voidspike;
    public static RegistryObject<BulletItem> bone_scrap,bud_bullet;
    public static RegistryObject<Item> skull,ObsidianCore,spore;
    public static void registerItems(){
        skullcrusher_pulverizer  = GWREItems.initItem( () -> new Skullcrusher(GWREItems.defP().durability(6666).fireResistant().rarity(Rarity.EPIC)
                        ,0,0.8,6,6.0,20,
                        () -> GWREConfig.GATLING.skull).fireSound(GWRESounds.skullcrusher).repair(() -> Ingredient.of(ForgeRegistries.ITEMS.getValue(new ResourceLocation("bosses_of_mass_destruction", "ancient_anima")))),
                "skullcrusher_pulverizer");
        obsidian_launcher = GWREItems.initItem(()->new ObsidianLauncher(GWREItems.defP().rarity(Rarity.EPIC).durability(10000).fireResistant(), 10, 1.0, 20, 0.0, 1,  () -> GWREConfig.LAUNCHER.Obisidian).fireSound(GWRESounds.OBSIDIAN_LAUNCHER_FIRE).repair(() -> Ingredient.of(ForgeRegistries.ITEMS.getValue(new ResourceLocation("bosses_of_mass_destruction", "obsidian_heart")))), "obsidian_launcher");
        hellforge = GWREItems.initItem(() -> new Hellforge(GWREItems.defP().rarity(Rarity.EPIC).durability(8000).fireResistant(),
                0, 1.2, 15, 1.0, 20, 6,
                () -> GWREConfig.PISTOL.hellforge).projectileSpeed(3).fireSound(GWRESounds.HELLFORGE_REVOLVER_SHOOT).repair(() -> Ingredient.of(ForgeRegistries.ITEMS.getValue(new ResourceLocation("bosses_of_mass_destruction", "blazing_eye")))), "hellforge_revolver");
        voidspike = GWREItems.initItem(() -> new Voidflower(GWREItems.defP().rarity(Rarity.RARE).durability(5000).fireResistant(),
                0, 1.0, 12, 0.0, 15,0,0,() -> GWREConfig.BURSTGUN.voidBurst).repair(() -> Ingredient.of(ForgeRegistries.ITEMS.getValue(new ResourceLocation("bosses_of_mass_destruction", "void_thorn")))), "voidspike");
        skull = GWREItems.initItem(()->new Item(GWREItems.defP()),"skull");
        ObsidianCore = GWREItems.initItem(()->new Item(GWREItems.defP()),"obsidian_core");
        bone_scrap = GWREItems.REG.register("bone_scrap", () -> new PiercingBulletItem(GWREItems.defP(), 1,2));
        bud_bullet = GWREItems.REG.register("bud_bullet", () -> new BudBulletItem(GWREItems.defP(), 5));
        spore = GWREItems.REG.register("spore", () -> new Item(GWREItems.defP()));
    }
}
