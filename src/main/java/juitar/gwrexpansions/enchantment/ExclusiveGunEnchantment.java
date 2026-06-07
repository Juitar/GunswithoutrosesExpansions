package juitar.gwrexpansions.enchantment;

import juitar.gwrexpansions.config.GWREConfig;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

import java.util.function.Predicate;

public class ExclusiveGunEnchantment extends Enchantment {
    private final Predicate<ItemStack> target;

    public ExclusiveGunEnchantment(Predicate<ItemStack> target) {
        super(Rarity.UNCOMMON, EnchantmentCategory.BREAKABLE, new EquipmentSlot[] { EquipmentSlot.MAINHAND });
        this.target = target;
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }

    @Override
    public int getMinCost(int level) {
        return 10;
    }

    @Override
    public int getMaxCost(int level) {
        return 60;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
        return target.test(stack);
    }

    @Override
    public boolean canEnchant(ItemStack stack) {
        return target.test(stack);
    }

    @Override
    public boolean isTradeable() {
        return GWREConfig.GENERAL.enableCataclysmEnchantmentLibrarianTrades.get();
    }
}
