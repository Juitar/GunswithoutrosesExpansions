package juitar.gwrexpansions.item;

import juitar.gwrexpansions.config.GWREConfig;
import lykrast.gunswithoutroses.item.GatlingItem;
import lykrast.gunswithoutroses.registry.GWRAttributes;
import lykrast.gunswithoutroses.registry.GWREnchantments;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * 可配置的加特林类
 * 使用GWREConfig中的配置项来动态设置武器属性
 */
public class ConfigurableGatlingItem extends GatlingItem {
    protected final Supplier<GWREConfig.GunConfig> config;
    
    /**
     * 创建可配置的加特林
     * @param properties 物品属性
     * @param bonusDamage 额外伤害（会被配置覆盖）
     * @param damageMultiplier 伤害倍率（会被配置覆盖）
     * @param fireDelay 射击延迟（会被配置覆盖）
     * @param inaccuracy 不精确度（会被配置覆盖）
     * @param enchantability 附魔能力
     * @param configSupplier 配置供应器
     */
    public ConfigurableGatlingItem(Properties properties, 
                               int bonusDamage,
                               double damageMultiplier,
                               int fireDelay,
                               double inaccuracy,
                               int enchantability,
                               Supplier<GWREConfig.GunConfig> configSupplier) {
        super(properties, bonusDamage, damageMultiplier, fireDelay, inaccuracy, enchantability);
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
        return config.get().bonusDamage.get() + bonus;
    }

    @Override
    public double getDamageMultiplier(ItemStack stack, @Nullable LivingEntity shooter) {
        // 保留原版属性计算
        if (shooter != null && shooter.getAttribute(GWRAttributes.dmgTotal.get()) != null) {
            return config.get().damageMultiplier.get() * shooter.getAttributeValue(GWRAttributes.dmgTotal.get());
        }
        return config.get().damageMultiplier.get();
    }

    @Override
    public int getFireDelay(ItemStack stack, @Nullable LivingEntity shooter) {
        // 从配置获取基础值
        int baseDelay = config.get().fireDelay.get();

        // 保留原版附魔计算
        int sleight = stack.getEnchantmentLevel(GWREnchantments.sleightOfHand.get());
        int delay = sleight > 0 ? GWREnchantments.sleightModify(sleight, baseDelay) : baseDelay;

        // 保留原版属性计算
        if (shooter != null && shooter.getAttribute(GWRAttributes.fireDelay.get()) != null) {
            delay = (int)(delay * shooter.getAttributeValue(GWRAttributes.fireDelay.get()));
        }

        return Math.max(1, delay);
    }

    @Override
    public double getInaccuracy(ItemStack stack, @Nullable LivingEntity shooter) {
        // 从配置获取基础值
        double baseInaccuracy = config.get().inaccuracy.get();

        // 保留原版属性计算
        if (shooter != null && shooter.getAttribute(GWRAttributes.spread.get()) != null) {
            baseInaccuracy *= shooter.getAttributeValue(GWRAttributes.spread.get());
        }

        // 保留原版附魔计算
        int bullseye = stack.getEnchantmentLevel(GWREnchantments.bullseye.get());
        return Math.max(0.0, bullseye >= 1 ?
                GWREnchantments.bullseyeModify(bullseye, baseInaccuracy) :
                baseInaccuracy);
    }
}
