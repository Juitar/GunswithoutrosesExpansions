package juitar.gwrexpansions.registry;

import juitar.gwrexpansions.GWRexpansions;
import juitar.gwrexpansions.enchantment.ExclusiveGunEnchantment;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class GWRECataclysmEnchantments {
    public static final DeferredRegister<Enchantment> REG =
            DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, GWRexpansions.MODID);

    public static final RegistryObject<Enchantment> ELEMENTAL_RESONANCE = REG.register("elemental_resonance",
            () -> new ExclusiveGunEnchantment(stack -> isTarget(stack, () -> CompatCataclysm.ceraunus_burst)));
    public static final RegistryObject<Enchantment> ABYSSAL_CHARGE = REG.register("abyssal_charge",
            () -> new ExclusiveGunEnchantment(stack -> isTarget(stack, () -> CompatCataclysm.tidal_pistol)));
    public static final RegistryObject<Enchantment> CURSIUM_DRAIN = REG.register("cursium_drain",
            () -> new ExclusiveGunEnchantment(stack -> isTarget(stack, () -> CompatCataclysm.cursium_sniper)));
    public static final RegistryObject<Enchantment> BLUE_FLAME_BRINK = REG.register("blue_flame_brink",
            () -> new ExclusiveGunEnchantment(stack -> isTarget(stack, () -> CompatCataclysm.ignitium_gatling)));
    public static final RegistryObject<Enchantment> REMNANT_FERVOR = REG.register("remnant_fervor",
            () -> new ExclusiveGunEnchantment(stack -> isTarget(stack, () -> CompatCataclysm.remnant_fangshot)));
    public static final RegistryObject<Enchantment> OVERLOAD_CALIBRATION = REG.register("overload_calibration",
            () -> new ExclusiveGunEnchantment(stack -> isTarget(stack, () -> CompatCataclysm.harbinger_raycaster)));

    public static void register(IEventBus eventBus) {
        REG.register(eventBus);
    }

    public static boolean has(ItemStack stack, RegistryObject<Enchantment> enchantment) {
        return enchantment.isPresent() && stack.getEnchantmentLevel(enchantment.get()) > 0;
    }

    private static boolean isTarget(ItemStack stack, Supplier<RegistryObject<? extends Item>> itemSupplier) {
        RegistryObject<? extends Item> item = itemSupplier.get();
        return item != null && item.isPresent() && stack.is(item.get());
    }
}
