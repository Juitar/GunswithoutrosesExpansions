package juitar.gwrexpansions.entity.iceandfire;

import com.github.alexthe666.iceandfire.entity.*;
import lykrast.gunswithoutroses.entity.BulletEntity;

import lykrast.gunswithoutroses.registry.GWRDamage;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.level.Level;

public class FireDragonSteelBulletEntity extends BulletEntity {
    public FireDragonSteelBulletEntity(EntityType<? extends BulletEntity> type, Level level) {
        super(type, level);
    }

    public FireDragonSteelBulletEntity(Level level, LivingEntity shooter) {
        super(level, shooter);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity target = result.getEntity();
        Entity shooter = getOwner();

        if (target instanceof EntityMutlipartPart multiPart) {
            target = multiPart.getParent();
        }

        if (target instanceof LivingEntity livingTarget) {
            // 保存并重置无敌时间
            int lastHurtResistant = target.invulnerableTime;
            target.invulnerableTime = 0;
            // 设置目标着火
            target.setSecondsOnFire(10);

            // 击退效果
            livingTarget.knockback(1F, getX() - livingTarget.getX(), getZ() - livingTarget.getZ());

            // 计算伤害，包括爆头检测
            float damage = (float) this.getDamage();
            boolean headshot = hasHeadshot(target);
            if (headshot) {
                damage *= getHeadshotMultiplier();
            }
            // 对冰龙造成额外伤害
            if (target instanceof EntityIceDragon) {
                damage += 4.0F;
            }

            boolean damaged = shooter == null
                    ? target.hurt(GWRDamage.gunDamage(level().registryAccess(), this), damage)
                    : target.hurt(GWRDamage.gunDamage(level().registryAccess(), this, shooter), damage);
            // 如果伤害未生效,恢复无敌时间
            if (!damaged) {
                target.invulnerableTime = lastHurtResistant;
            }
        }
    }
} 