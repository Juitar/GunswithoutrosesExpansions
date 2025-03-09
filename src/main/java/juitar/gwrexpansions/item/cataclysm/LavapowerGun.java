package juitar.gwrexpansions.item.cataclysm;

import juitar.gwrexpansions.registry.CompatCataclysm;
import juitar.gwrexpansions.registry.GWREItem;
import juitar.gwrexpansions.registry.VanillaItem;
import lykrast.gunswithoutroses.entity.BulletEntity;
import lykrast.gunswithoutroses.item.GunItem;
import lykrast.gunswithoutroses.item.IBullet;
import lykrast.gunswithoutroses.registry.GWRAttributes;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class LavapowerGun extends GunItem {

    public LavapowerGun(Properties properties, int bonusDamage, double damageMultiplier, int fireDelay, double inaccuracy, int enchantability) {
        super(properties, bonusDamage, damageMultiplier, fireDelay, inaccuracy, enchantability);
    }

    @Override
    protected ItemStack overrideFiredStack(LivingEntity shooter, ItemStack gun, ItemStack ammo, IBullet bulletItem, boolean bulletFree) {
        if (ammo.is(CompatCataclysm.tagBaseBullets)) return new ItemStack(CompatCataclysm.lavapower_bullet.get());
        else return ammo;
    }

    protected void shoot(Level world, Player player, ItemStack gun, ItemStack ammo, IBullet bulletItem, boolean bulletFree) {
        ItemStack override = this.overrideFiredStack(player, gun, ammo, bulletItem, bulletFree);
        if (override != ammo) {
            ammo = override;
            bulletItem = (IBullet)override.getItem();
        }

        int shots = this.getProjectilesPerShot(gun, player);

        for(int i = 0; i < shots; ++i) {
            BulletEntity shot = bulletItem.createProjectile(world, ammo, player);
            shot.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, (float)this.getProjectileSpeed(gun, player), (float)this.getInaccuracy(gun, player));
            shot.setDamage(Math.max((double)0.0F, shot.getDamage() + this.getBonusDamage(gun, player)) * this.getDamageMultiplier(gun, player));
            if (player.getAttribute((Attribute) GWRAttributes.knockback.get()) != null) {
                shot.setKnockbackStrength(shot.getKnockbackStrength() + player.getAttributeValue((Attribute)GWRAttributes.knockback.get()));
            }

            shot.setHeadshotMultiplier(this.getHeadshotMultiplier(gun, player));
            this.affectBulletEntity(player, gun, shot, bulletFree);
            world.addFreshEntity(shot);
        }

    }

    @Override
    protected void addExtraStatsTooltip(ItemStack stack, @Nullable Level world, List<Component> tooltip){
        tooltip.add(Component.translatable("tooltip.gwrexpansions.lavapower_gun.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.gwrexpansions.lavapower_gun.desc2") .withStyle(ChatFormatting.GRAY));
    }
}