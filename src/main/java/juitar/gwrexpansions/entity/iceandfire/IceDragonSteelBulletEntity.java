package juitar.gwrexpansions.entity.iceandfire;

import com.github.alexthe666.iceandfire.entity.*;
import com.github.alexthe666.iceandfire.entity.props.EntityDataProvider;
import lykrast.gunswithoutroses.entity.BulletEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public class IceDragonSteelBulletEntity extends BulletEntity {
    public IceDragonSteelBulletEntity(EntityType<? extends BulletEntity> type, Level level) {
        super(type, level);
    }

    public IceDragonSteelBulletEntity(Level level, LivingEntity shooter) {
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
            // 添加缓慢效果
            livingTarget.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1));
            EntityDataProvider.getCapability(target).ifPresent(data -> data.frozenData.setFrozen(livingTarget, 300));
            // 击退效果
            livingTarget.knockback(1F, getX() - livingTarget.getX(), getZ() - livingTarget.getZ());

            // 对火龙造成额外伤害
            float damage = (float) this.getDamage();
            if (target instanceof EntityFireDragon) {
                damage += 4.0F;
            }

            livingTarget.hurt(damageSources().thrown(this, shooter), damage);
        }
    }
} 