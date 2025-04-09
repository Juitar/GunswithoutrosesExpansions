package juitar.gwrexpansions.item.meetyourfight;

import juitar.gwrexpansions.config.GWREConfig;
import juitar.gwrexpansions.item.ConfigurableGunItem;
import lykrast.gunswithoutroses.entity.BulletEntity;
import lykrast.gunswithoutroses.item.IBullet;
import lykrast.gunswithoutroses.registry.GWRAttributes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.function.Supplier;

public class DestinyGunItem extends ConfigurableGunItem {
    private int SHOT_TIMES = 0;
    public DestinyGunItem(Properties properties, int bonusDamage, double damageMultiplier, int fireDelay, double inaccuracy, int enchantability, Supplier<GWREConfig.GunConfig> configSupplier) {
        super(properties, bonusDamage, damageMultiplier, fireDelay, inaccuracy, enchantability, configSupplier);
    }
    @Override
    protected void shoot(Level world, Player player, ItemStack gun, ItemStack ammo, IBullet bulletItem, boolean bulletFree) {
        ItemStack override = this.overrideFiredStack(player, gun, ammo, bulletItem, bulletFree);
        if (override != ammo) {
            ammo = override;
            bulletItem = (IBullet)override.getItem();
        }
        int shots = this.getProjectilesPerShot(gun, player);

        double lucky = world.random.nextDouble()+SHOT_TIMES*0.05;
        if (lucky < 0.4) {
            player.displayClientMessage(Component.translatable("msg.gwrexpansions.bust"), true);
        }
        else if (lucky >= 0.4 && lucky < 0.6) {
            player.displayClientMessage(Component.translatable("msg.gwrexpansions.double"), true);
            shots = 2;
        }
        else if (lucky >= 0.6 && lucky <= 0.8) {
            player.displayClientMessage(Component.translatable("msg.gwrexpansions.triple"), true);
            shots = 3;
        }
        else{
            player.displayClientMessage(Component.translatable("msg.gwrexpansions.jackpot"), true);
            shots = 9;
            SHOT_TIMES = 0;
        }
        SHOT_TIMES++;
        int fix_inaccuracy = shots!=1?3:1;
            for (int i = 0; i < shots; ++i) {
                BulletEntity shot = bulletItem.createProjectile(world, ammo, player);
                shot.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, (float) this.getProjectileSpeed(gun, player), (float) this.getInaccuracy(gun, player)+fix_inaccuracy);
                shot.setDamage(Math.max((double) 0.0F, shot.getDamage() + this.getBonusDamage(gun, player)) * this.getDamageMultiplier(gun, player));
                if (player.getAttribute((Attribute) GWRAttributes.knockback.get()) != null) {
                    shot.setKnockbackStrength(shot.getKnockbackStrength() + player.getAttributeValue((Attribute) GWRAttributes.knockback.get()));
                }
                shot.setHeadshotMultiplier(this.getHeadshotMultiplier(gun, player));
                this.affectBulletEntity(player, gun, shot, bulletFree);
                world.addFreshEntity(shot);
            }
    }
}
