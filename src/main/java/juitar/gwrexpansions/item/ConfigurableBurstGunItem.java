package juitar.gwrexpansions.item;

import juitar.gwrexpansions.config.GWREConfig;
import lykrast.gunswithoutroses.item.BurstGunItem;
import lykrast.gunswithoutroses.registry.GWRAttributes;
import lykrast.gunswithoutroses.registry.GWREnchantments;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class ConfigurableBurstGunItem extends BurstGunItem {
    protected final Supplier<GWREConfig.BurstgunConfig> config;
    public ConfigurableBurstGunItem(Properties properties,
                                 int bonusDamage,
                                 double damageMultiplier,
                                 int fireDelay,
                                 double inaccuracy,
                                 int enchantability,
                                 int burstSize,
                                 int burstFireDelay,
                                 Supplier<GWREConfig.BurstgunConfig> configSupplier) {
        super(properties, bonusDamage, damageMultiplier, fireDelay, inaccuracy, enchantability,burstSize,burstFireDelay);
        this.config = configSupplier;
    }
    // 动态获取配置值
    @Override
    public double getBonusDamage(ItemStack stack, @Nullable LivingEntity shooter) {
        // 保留原版附魔计算
        int impact = stack.getEnchantmentLevel(GWREnchantments.impact.get());
        double bonus = impact >= 1 ? GWREnchantments.impactBonus(impact) : 0.0;

        // 保留原版属性计算
        if (shooter != null && shooter.getAttribute(GWRAttributes.dmgBase.get()) != null) {
            bonus += shooter.getAttributeValue(GWRAttributes.dmgBase.get());
        }

        // 使用配置值替换原版固定值
        return config.get().gunConfig.bonusDamage.get() + bonus;
    }

    @Override
    public double getDamageMultiplier(ItemStack stack, @Nullable LivingEntity shooter) {
        // 保留原版属性计算
        if (shooter != null && shooter.getAttribute(GWRAttributes.dmgTotal.get()) != null) {
            return config.get().gunConfig.damageMultiplier.get() * shooter.getAttributeValue(GWRAttributes.dmgTotal.get());
        }
        return config.get().gunConfig.damageMultiplier.get();
    }
    @Override
    public int getBurstSize(ItemStack stack) {
        return config.get().burstSize.get();
    }
    @Override
    public int getBurstFireDelay(ItemStack stack) {
        return config.get().burstDelay.get();
    }

}
