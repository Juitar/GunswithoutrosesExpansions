package juitar.gwrexpansions.registry;

import juitar.gwrexpansions.item.alexscaves.MechanicPlaceholderItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.registries.RegistryObject;

public class CompatAlexsCaves {
    public static RegistryObject<Item> magnetic_gatling, licorice_fission_launcher, extinction_burstgun,
            blood_pact_revolver, abyssal_harpoon_gun, raycat_fusion_irradiator;

    public static void registerItems() {
        magnetic_gatling = GWREItems.initItem(() -> new MechanicPlaceholderItem(
                GWREItems.noStack().rarity(Rarity.RARE),
                "tooltip.gwrexpansions.magnetic_gatling.desc"), "magnetic_gatling");
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
