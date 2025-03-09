package juitar.gwrexpansions.entity.iceandfire;

import com.github.alexthe666.iceandfire.entity.*;
import lykrast.gunswithoutroses.entity.BulletEntity;
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

            // 对冰龙造成额外伤害
            float damage = (float) this.getDamage();
            if (target instanceof EntityIceDragon) {
                damage += 4.0F;
            }

            boolean damaged = livingTarget.hurt(damageSources().thrown(this, shooter), damage);

            // 如果伤害未生效,恢复无敌时间
            if (!damaged) {
                target.invulnerableTime = lastHurtResistant;
            }
        }
    }
} 