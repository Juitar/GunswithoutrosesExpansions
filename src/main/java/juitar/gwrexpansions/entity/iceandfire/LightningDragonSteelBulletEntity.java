package juitar.gwrexpansions.entity.iceandfire;

import com.github.alexthe666.iceandfire.entity.*;
import lykrast.gunswithoutroses.entity.BulletEntity;
import lykrast.gunswithoutroses.registry.GWRDamage;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.level.Level;

public class LightningDragonSteelBulletEntity extends BulletEntity {
    public LightningDragonSteelBulletEntity(EntityType<? extends BulletEntity> type, Level level) {
        super(type, level);
    }

    public LightningDragonSteelBulletEntity(Level level, LivingEntity shooter) {
        super(level, shooter);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity target = result.getEntity();
        Entity shooter = getOwner();

        // 处理多部件实体
        if (target instanceof EntityMutlipartPart multiPart) {
            target = multiPart.getParent();
        }

        if (target instanceof LivingEntity livingTarget) {
            // 保存并重置无敌时间
            int lastHurtResistant = target.invulnerableTime;
            target.invulnerableTime = 0;
            // 生成闪电
            if (!level().isClientSide) {
                LightningBolt lightning = EntityType.LIGHTNING_BOLT.create(level());
                lightning.moveTo(target.position());
                level().addFreshEntity(lightning);
            }

            livingTarget.knockback(1F, getX() - livingTarget.getX(), getZ() - livingTarget.getZ());

            float damage = (float) this.getDamage();
            boolean headshot = hasHeadshot(target);
            if (headshot) {
                damage *= getHeadshotMultiplier();
            }
            if (target instanceof EntityIceDragon || target instanceof EntityFireDragon) {
                damage += 2.0F;
            }
            boolean damaged = shooter == null
                    ? target.hurt(GWRDamage.gunDamage(level().registryAccess(), this), damage)
                    : target.hurt(GWRDamage.gunDamage(level().registryAccess(), this, shooter), damage);

            if (!damaged) {
                target.invulnerableTime = lastHurtResistant;
            }
        }
    }
} 