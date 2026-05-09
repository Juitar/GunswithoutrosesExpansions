package juitar.gwrexpansions.registry;

import juitar.gwrexpansions.item.alexscaves.MechanicPlaceholderItem;
import juitar.gwrexpansions.item.alexscaves.MagneticGatlingItem;
import juitar.gwrexpansions.config.GWREConfig;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class CompatAlexsCaves {
    public static RegistryObject<Item> magnetic_gatling, licorice_fission_launcher, extinction_burstgun,
            blood_pact_revolver, abyssal_harpoon_gun, raycat_fusion_irradiator;

    public static void registerItems() {
        magnetic_gatling = GWREItems.initItem(() -> new MagneticGatlingItem(
                GWREItems.defP().durability(3000).rarity(Rarity.RARE),
                0, 0.9, 4, 4.0, 12,
                () -> GWREConfig.GATLING.Magnetic)
                .repair(() -> Ingredient.of(ForgeRegistries.ITEMS.getValue(new ResourceLocation("alexscaves", "azure_neodymium_ingot")),
                        ForgeRegistries.ITEMS.getValue(new ResourceLocation("alexscaves", "scarlet_neodymium_ingot")))), "magnetic_gatling");
        licorice_fission_launcher = GWREItems.initItem(() -> new MechanicPlaceholderItem(
                GWREItems.noStack().rarity(Rarity.RARE),
                "tooltip.gwrexpansions.licorice_fission_launcher.desc"), "licorice_fission_launcher");
        extinction_burstgun = GWREItems.initItem(() -> new MechanicPlaceholderItem(
                GWREItems.noStack().rarity(Rarity.RARE),
                "tooltip.gwrexpansions.extinction_burstgun.desc"), "extinction_burstgun");
        blood_pact_revolver = GWREItems.initItem(() -> new MechanicPlaceholderItem(
                GWREItems.noStack().rarity(Rarity.RARE),
                "tooltip.gwrexpansions.blood_pact_revolver.desc"), "blood_pact_revolver");
        abyssal_harpoon_gun = GWREItems.initItem(() -> new MechanicPlaceholderItem(
                GWREItems.noStack().rarity(Rarity.RARE),
                "tooltip.gwrexpansions.abyssal_harpoon_gun.desc"), "abyssal_harpoon_gun");
        raycat_fusion_irradiator = GWREItems.initItem(() -> new MechanicPlaceholderItem(
                GWREItems.noStack().rarity(Rarity.RARE),
                "tooltip.gwrexpansions.raycat_fusion_irradiator.desc"), "raycat_fusion_irradiator");
    }

    private CompatAlexsCaves() {}
}
