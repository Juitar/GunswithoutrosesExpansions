package juitar.gwrexpansions.item;

import juitar.gwrexpansions.config.GWREConfig;
import lykrast.gunswithoutroses.item.BurstGunItem;
import lykrast.gunswithoutroses.registry.GWRAttributes;
import lykrast.gunswithoutroses.registry.GWREnchantments;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

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
        super(properties, bonusDamage, damageMultiplier, fireDelay, inaccuracy, enchantability, burstSize, burstFireDelay);
        this.config = configSupplier;
    }

    @Override
    public double getBonusDamage(ItemStack stack, @Nullable LivingEntity shooter) {
        int impact = stack.getEnchantmentLevel(GWREnchantments.impact.get());
        double bonus = impact >= 1 ? GWREnchantments.impactBonus(impact) : 0.0D;

        if (shooter != null && shooter.getAttribute(GWRAttributes.dmgBase.get()) != null) {
            bonus += shooter.getAttributeValue(GWRAttributes.dmgBase.get());
        }

        return config.get().gunConfig.bonusDamage.get() + bonus;
    }

    @Override
    public double getDamageMultiplier(ItemStack stack, @Nullable LivingEntity shooter) {
        if (shooter != null && shooter.getAttribute(GWRAttributes.dmgTotal.get()) != null) {
            return config.get().gunConfig.damageMultiplier.get() * shooter.getAttributeValue(GWRAttributes.dmgTotal.get());
        }
        return config.get().gunConfig.damageMultiplier.get();
    }

    @Override
    public int getFireDelay(ItemStack stack, @Nullable LivingEntity shooter) {
        int baseDelay = config.get().gunConfig.fireDelay.get();
        int sleight = stack.getEnchantmentLevel(GWREnchantments.sleightOfHand.get());
        int delay = sleight > 0 ? GWREnchantments.sleightModify(sleight, baseDelay) : baseDelay;

        if (shooter != null && shooter.getAttribute(GWRAttributes.fireDelay.get()) != null) {
            delay = (int)(delay * shooter.getAttributeValue(GWRAttributes.fireDelay.get()));
        }

        return Math.max(1, delay);
    }

    @Override
    public double getInaccuracy(ItemStack stack, @Nullable LivingEntity shooter) {
        double baseInaccuracy = config.get().gunConfig.inaccuracy.get();

        if (shooter != null && shooter.getAttribute(GWRAttributes.spread.get()) != null) {
            baseInaccuracy *= shooter.getAttributeValue(GWRAttributes.spread.get());
        }

        int bullseye = stack.getEnchantmentLevel(GWREnchantments.bullseye.get());
        return Math.max(0.0D, bullseye >= 1 ? GWREnchantments.bullseyeModify(bullseye, baseInaccuracy) : baseInaccuracy);
    }

    @Override
    public double getHeadshotMultiplier(ItemStack stack, @Nullable LivingEntity shooter) {
        double mult = config.get().gunConfig.headshotMultiplier.get();
        int deadeye = stack.getEnchantmentLevel((Enchantment)GWREnchantments.deadeye.get());
        if (deadeye >= 1) {
            mult = GWREnchantments.deadeyeModify(deadeye, mult);
        }

        if (shooter != null && shooter.getAttribute((Attribute)GWRAttributes.sniperMult.get()) != null) {
            mult += shooter.getAttributeValue((Attribute)GWRAttributes.sniperMult.get());
        }

        return Math.max(1.0D, mult);
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