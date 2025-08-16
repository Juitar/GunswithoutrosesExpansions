package juitar.gwrexpansions.item.BOMD;

import juitar.gwrexpansions.config.GWREConfig;
import juitar.gwrexpansions.entity.BOMD.BudBulletEntity;
import juitar.gwrexpansions.item.ConfigurableBurstGunItem;
import juitar.gwrexpansions.item.ConfigurableGunItem;
import juitar.gwrexpansions.registry.*;
import lykrast.gunswithoutroses.entity.BulletEntity;
import lykrast.gunswithoutroses.item.IBullet;
import lykrast.gunswithoutroses.registry.GWRItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

/**
 * 虚空花 - 将普通弹药转化为花苞弹
 * 花苞弹击中实体后向四处分散孢子子弹
 * 孢子子弹速度慢且持续减速，造成伤害可给射手治疗
 */
public class Voidflower extends ConfigurableBurstGunItem {
    /**
     * 创建可配置的枪支
     *
     * @param properties       物品属性
     * @param bonusDamage      额外伤害（会被配置覆盖）
     * @param damageMultiplier 伤害倍率（会被配置覆盖）
     * @param fireDelay        射击延迟（会被配置覆盖）
     * @param inaccuracy       不精确度（会被配置覆盖）
     * @param enchantability   附魔能力
     * @param configSupplier   配置供应器
     */
    public Voidflower(Properties properties, int bonusDamage, double damageMultiplier, int fireDelay, double inaccuracy, int enchantability,int burstSize, int burstFireDelay, Supplier<GWREConfig.BurstgunConfig> configSupplier) {
        super(properties, bonusDamage, damageMultiplier, fireDelay, inaccuracy, enchantability,burstSize,burstFireDelay,configSupplier);
    }

    @Override
    protected ItemStack overrideFiredStack(LivingEntity shooter, ItemStack gun, ItemStack ammo, IBullet bulletItem, boolean bulletFree) {
        if(ammo.is(GWRItems.ironBullet.get()) || ammo.is(VanillaItem.maws_of_gluttony.get()))
            return new ItemStack(CompatBOMD.bud_bullet.get());
        return ammo;
    }


    @Override
    protected void addExtraStatsTooltip(ItemStack stack, @Nullable Level world, List<Component> tooltip) {
        tooltip.add(Component.translatable("tooltip.gwrexpansions.voidflower.line1")
            .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.gwrexpansions.voidflower.line2")
            .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.gwrexpansions.voidflower.line3")
            .withStyle(ChatFormatting.GRAY));
    }
}
