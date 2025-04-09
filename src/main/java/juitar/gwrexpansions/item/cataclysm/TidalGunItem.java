package juitar.gwrexpansions.item.cataclysm;

import juitar.gwrexpansions.config.GWREConfig;
import juitar.gwrexpansions.entity.cataclysm.LavapowerBulletEntity;
import juitar.gwrexpansions.item.ConfigurableGunItem;
import juitar.gwrexpansions.registry.CompatCataclysm;
import lykrast.gunswithoutroses.entity.BulletEntity;
import lykrast.gunswithoutroses.item.IBullet;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;


import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

public class TidalGunItem extends ConfigurableGunItem {
    public TidalGunItem(Properties properties, int bonusDamage, double damageMultiplier, int fireDelay, double inaccuracy, int enchantability, Supplier<GWREConfig.GunConfig> configSupplier) {
        super(properties, bonusDamage, damageMultiplier, fireDelay, inaccuracy, enchantability, configSupplier);
    }
    @Override
    protected ItemStack overrideFiredStack(LivingEntity shooter, ItemStack gun, ItemStack ammo, IBullet bulletItem, boolean bulletFree) {
        if (ammo.is(CompatCataclysm.tagBaseBullets)) return new ItemStack(CompatCataclysm.tidal_bullet.get());
        else return ammo;
    }
    @Override
    protected void addExtraStatsTooltip(ItemStack stack, @Nullable Level world, List<Component> tooltip){
        tooltip.add(Component.translatable("tooltip.gwrexpansions.tidal_pistol.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.gwrexpansions.tidal_pistol.desc2").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.gwrexpansions.tidal_pistol.desc3").withStyle(ChatFormatting.GRAY));
    }
}
