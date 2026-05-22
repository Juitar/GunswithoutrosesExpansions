package juitar.gwrexpansions.entity.cataclysm;

import com.github.L_Ender.cataclysm.init.ModParticle;
import juitar.gwrexpansions.entity.meetyourfight.DuskfallBulletDelegate;
import juitar.gwrexpansions.item.cataclysm.CeraunusBurstItem;
import juitar.gwrexpansions.registry.GWREEntities;
import lykrast.gunswithoutroses.entity.BulletEntity;
import lykrast.gunswithoutroses.registry.GWRDamage;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

public class CeraunusLightningBulletEntity extends BulletEntity implements DuskfallBulletDelegate {
    public CeraunusLightningBulletEntity(EntityType<? extends BulletEntity> type, Level level) {
        super(type, level);
    }

    public CeraunusLightningBulletEntity(Level level, LivingEntity shooter) {
        super(GWREEntities.CERAUNUS_LIGHTNING_BULLET.get(), shooter, level);
        setOwner(shooter);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (!level().isClientSide) {
            Entity target = result.getEntity();
            Entity shooter = getOwner();
            int lastHurtResistant = target.invulnerableTime;
            target.invulnerableTime = 0;

            float damage = (float) getDamage();
            if (hasHeadshot(target)) {
                damage *= getHeadshotMultiplier();
            }
            boolean waterBoosted = isInWater() || target.isInWater();
            if (waterBoosted) {
                damage *= (float) CeraunusBurstItem.lightningBulletWaterDamageMultiplier();
            }

            boolean damaged = shooter == null
                    ? target.hurt(GWRDamage.gunDamage(level().registryAccess(), this), damage)
                    : target.hurt(GWRDamage.gunDamage(level().registryAccess(), this, shooter), damage);
            if (!damaged) {
                target.invulnerableTime = lastHurtResistant;
            }

            if (target instanceof LivingEntity livingTarget && shooter instanceof LivingEntity livingShooter) {
                CeraunusBurstItem.applyLightningBulletHit(livingTarget, livingShooter,
                        waterBoosted ? getDamage() * CeraunusBurstItem.lightningBulletWaterDamageMultiplier() : getDamage());
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult hit) {
        if (!level().isClientSide && getOwner() instanceof LivingEntity livingShooter) {
            CeraunusBurstItem.applyLightningBulletBlockHit(level(), hit.getLocation(), livingShooter,
                    isInWater() ? getDamage() * CeraunusBurstItem.lightningBulletWaterDamageMultiplier() : getDamage());
        }
    }

    @Override
    public boolean gwrexpansions$onDuskfallHitEntity(EntityHitResult result) {
        onHitEntity(result);
        return true;
    }

    @Override
    public boolean gwrexpansions$onDuskfallHitBlock(BlockHitResult hit) {
        onHitBlock(hit);
        return true;
    }

    @Override
    protected ParticleOptions getTrailParticle() {
        return ModParticle.SPARK.get();
    }

    @Override
    public ParticleOptions gwrexpansions$getDuskfallTrailParticle() {
        return getTrailParticle();
    }
}
