package juitar.gwrexpansions.compat.kubejs;

import juitar.gwrexpansions.GWRexpansions;
import lykrast.gunswithoutroses.entity.BulletEntity;
import lykrast.gunswithoutroses.entity.PiercingBulletEntity;
import lykrast.gunswithoutroses.item.GunItem;
import lykrast.gunswithoutroses.item.IBullet;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

final class GunProjectileSupport {
    private GunProjectileSupport() {
    }

    static void shoot(GunItem gun, Level level, Player player, ItemStack gunStack, ItemStack ammo, IBullet bullet, boolean bulletFree, ProjectileConversion conversion, int pierceCount, java.util.List<AmmoConversion> ammoConversions, KubeJSGunEvents callbacks) {
        callbacks.onFire(gun, level, player, gunStack);
        ItemStack firedAmmo = convertAmmo(ammo, ammoConversions);
        if (firedAmmo != ammo && firedAmmo.getItem() instanceof IBullet convertedBullet) bullet = convertedBullet;
        int projectiles = gun.getProjectilesPerShot(gunStack, player);
        for (int i = 0; i < projectiles; i++) {
            BulletEntity shot = bullet.createProjectile(level, firedAmmo, player);
            shot.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F,
                (float) gun.getProjectileSpeed(gunStack, player), (float) gun.getInaccuracy(gunStack, player));
            shot = convert(level, player, shot, conversion, pierceCount);
            shot.setDamage(Math.max(0.0D, shot.getDamage() + gun.getBonusDamage(gunStack, player)) * gun.getDamageMultiplier(gunStack, player));
            shot.setKnockbackStrength(shot.getKnockbackStrength() + gun.getKnockbackBonus(gunStack, player));
            shot.setHeadshotMultiplier(gun.getHeadshotMultiplier(gunStack, player));
            callbacks.markProjectile(gun, shot);
            level.addFreshEntity(shot);
        }
    }

    private static ItemStack convertAmmo(ItemStack ammo, java.util.List<AmmoConversion> conversions) {
        var sourceId = BuiltInRegistries.ITEM.getKey(ammo.getItem());
        for (AmmoConversion conversion : conversions) {
            if (conversion.source().equals(sourceId)) {
                Item target = BuiltInRegistries.ITEM.get(conversion.target());
                if (target instanceof IBullet) return new ItemStack(target);
                GWRexpansions.LOG.warn("KubeJS gun projectile conversion ignored: target {} is not a registered GWR bullet", conversion.target());
            }
        }
        return ammo;
    }

    private static BulletEntity convert(Level level, Player player, BulletEntity original, ProjectileConversion conversion, int pierceCount) {
        if (conversion != ProjectileConversion.PIERCING) return original;
        PiercingBulletEntity piercing = new PiercingBulletEntity(level, player);
        piercing.setPos(original.position());
        piercing.setDeltaMovement(original.getDeltaMovement());
        piercing.setDamage(original.getDamage());
        piercing.setKnockbackStrength(original.getKnockbackStrength());
        piercing.setHeadshotMultiplier(original.getHeadshotMultiplier());
        piercing.setItem(original.getItem());
        piercing.setOwner(player);
        piercing.setPierce(pierceCount);
        piercing.setPierceMultiplier(1.0D);
        return piercing;
    }
}
