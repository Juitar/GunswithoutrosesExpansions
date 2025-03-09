package juitar.gwrexpansions.item.cataclysm;

import juitar.gwrexpansions.registry.CompatCataclysm;
import lykrast.gunswithoutroses.entity.BulletEntity;
import lykrast.gunswithoutroses.item.GunItem;
import lykrast.gunswithoutroses.item.IBullet;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import juitar.gwrexpansions.entity.cataclysm.LavapowerBulletEntity;

import javax.annotation.Nullable;
import java.util.List;

public class LavapowerGunItem extends GunItem {

    public LavapowerGunItem(Properties properties, int bonusDamage, double damageMultiplier, int fireDelay, double inaccuracy, int enchantability) {
        super(properties, bonusDamage, damageMultiplier, fireDelay, inaccuracy, enchantability);
    }

    @Override
    protected ItemStack overrideFiredStack(LivingEntity shooter, ItemStack gun, ItemStack ammo, IBullet bulletItem, boolean bulletFree) {
        if (ammo.is(CompatCataclysm.tagBaseBullets)) return new ItemStack(CompatCataclysm.lavapower_bullet.get());
        else return ammo;
    }


    @Override
    protected void addExtraStatsTooltip(ItemStack stack, @Nullable Level world, List<Component> tooltip){
        tooltip.add(Component.translatable("tooltip.gwrexpansions.lavapower_gun.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.gwrexpansions.lavapower_gun.desc2") .withStyle(ChatFormatting.GRAY));
    }

    @Override
    protected void affectBulletEntity(LivingEntity shooter, ItemStack gun, BulletEntity bullet, boolean bulletFree) {
        super.affectBulletEntity(shooter, gun, bullet, bulletFree);
        if (bullet instanceof LavapowerBulletEntity lavaBullet) {
            lavaBullet.setJetCount(lavaBullet.getJetCount() + 7);
        }
    }
}