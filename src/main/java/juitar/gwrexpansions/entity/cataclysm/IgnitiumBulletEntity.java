package juitar.gwrexpansions.entity.cataclysm;


import com.github.L_Ender.cataclysm.init.ModEffect;
import juitar.gwrexpansions.config.GWREConfig;
import juitar.gwrexpansions.entity.meetyourfight.DuskfallBulletDelegate;
import juitar.gwrexpansions.registry.GWREEntities;
import lykrast.gunswithoutroses.entity.BulletEntity;
import lykrast.gunswithoutroses.registry.GWRDamage;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.nbt.CompoundTag;

public class IgnitiumBulletEntity extends BulletEntity implements DuskfallBulletDelegate {
    private boolean Healing = false;
    private boolean BlueFire = false;

    public IgnitiumBulletEntity(EntityType<? extends BulletEntity> type, Level world) {
        super(type, world);
    }

    public IgnitiumBulletEntity(Level level, LivingEntity shooter) {
        super(GWREEntities.IGNITIUM_BULLET.get(), shooter, level);
    }


    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity target = result.getEntity();
        Entity shooter = getOwner();


        if (target instanceof LivingEntity livingTarget) {
            int lastHurtResistant = target.invulnerableTime;
            target.invulnerableTime = 0;
            MobEffectInstance currentEffect = livingTarget.getEffect(ModEffect.EFFECTBLAZING_BRAND.get());
            int newAmplifier = Math.min(2, currentEffect != null ? currentEffect.getAmplifier() + 1 : 0);

           // 添加新效果，重置持续时间
            livingTarget.addEffect(new MobEffectInstance(ModEffect.EFFECTBLAZING_BRAND.get(), 200, newAmplifier));

            float damage = (float) this.getDamage();
            boolean headshot = hasHeadshot(target);
            if (headshot) {
                damage *= (float) getHeadshotMultiplier();
            }
            boolean damaged = shooter == null
                    ? target.hurt(GWRDamage.gunDamage(level().registryAccess(), this), damage)
                    : target.hurt(GWRDamage.gunDamage(level().registryAccess(), this, shooter), damage);

            if (!damaged) {
                target.invulnerableTime = lastHurtResistant;
            }

            if (Healing && shooter instanceof LivingEntity) {
                float healRatio = 0.05f + (newAmplifier * 0.05f);
                if (BlueFire) {
                    healRatio *= 1.0F + GWREConfig.GATLING.Ignitium.blueFireHealingBonus.get().floatValue();
                }
                float healAmount = damage * healRatio;
                ((LivingEntity) shooter).heal(healAmount);
            }
        }

    }

    public void setHealing(boolean healing) {
        this.Healing = healing;
    }

    public void setBlueFire(boolean blueFire) {
        this.BlueFire = blueFire;
    }

    @Override
    public boolean gwrexpansions$onDuskfallHitEntity(EntityHitResult result) {
        onHitEntity(result);
        return true;
    }

    @Override
    protected ParticleOptions getTrailParticle() {
        return BlueFire ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.FLAME;
    }

    @Override
    public ParticleOptions gwrexpansions$getDuskfallTrailParticle() {
        return getTrailParticle();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("Healing", Healing);
        compound.putBoolean("BlueFire", BlueFire);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        Healing = compound.getBoolean("Healing");
        BlueFire = compound.getBoolean("BlueFire");
    }
}
