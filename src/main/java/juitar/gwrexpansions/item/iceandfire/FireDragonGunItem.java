package juitar.gwrexpansions.item.iceandfire;


import juitar.gwrexpansions.config.GWREConfig;
import juitar.gwrexpansions.entity.cataclysm.LavapowerBulletEntity;
import juitar.gwrexpansions.entity.iceandfire.FireDragonSteelBulletEntity;
import juitar.gwrexpansions.item.ConfigurableGunItem;
import juitar.gwrexpansions.registry.CompatIceandfire;
import lykrast.gunswithoutroses.entity.BulletEntity;
import lykrast.gunswithoutroses.item.GunItem;
import lykrast.gunswithoutroses.item.IBullet;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

public class FireDragonGunItem extends ConfigurableGunItem {
    public FireDragonGunItem(Properties properties, int bonusDamage, double damageMultiplier, int fireDelay, double inaccuracy, int enchantability, Supplier<GWREConfig.GunConfig> configSupplier) {
        super(properties, bonusDamage, damageMultiplier, fireDelay, inaccuracy, enchantability, configSupplier);
    }
    @Override
    protected ItemStack overrideFiredStack(LivingEntity shooter, ItemStack gun, ItemStack ammo, IBullet bulletItem, boolean bulletFree) {
        if (ammo.is(CompatIceandfire.tagBaseBullets)) return new ItemStack(CompatIceandfire.dragonsteel_fire_bullet.get());
        else return ammo;
    }
    @Override
    protected void affectBulletEntity(LivingEntity shooter, ItemStack gun, BulletEntity bullet, boolean bulletFree) {
        super.affectBulletEntity(shooter, gun, bullet, bulletFree);
        if (bullet instanceof FireDragonSteelBulletEntity) {
            bullet.setDamage(bullet.getDamage() + 3.0F);
        }
    }
    @Override
    protected void addExtraStatsTooltip(ItemStack stack, @Nullable Level world, List<Component> tooltip){
        tooltip.add(Component.translatable("tooltip.gwrexpansions.fire_dragon_gun.desc").withStyle(ChatFormatting.GRAY));
    }
}
