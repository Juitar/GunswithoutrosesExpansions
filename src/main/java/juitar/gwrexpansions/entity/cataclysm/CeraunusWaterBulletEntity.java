package juitar.gwrexpansions.entity.cataclysm;

import juitar.gwrexpansions.event.BulletHitEventHandler;
import juitar.gwrexpansions.entity.meetyourfight.DuskfallBulletDelegate;
import juitar.gwrexpansions.item.cataclysm.CeraunusBurstItem;
import juitar.gwrexpansions.registry.GWREEntities;
import lykrast.gunswithoutroses.entity.BulletEntity;
import lykrast.gunswithoutroses.registry.GWRDamage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class CeraunusWaterBulletEntity extends BulletEntity implements DuskfallBulletDelegate {
    private int maxBounces = 2;
    private int bounceCount = 0;

    public CeraunusWaterBulletEntity(EntityType<? extends BulletEntity> type, Level level) {
        super(type, level);
    }

    public CeraunusWaterBulletEntity(Level level, LivingEntity shooter) {
        super(GWREEntities.CERAUNUS_WATER_BULLET.get(), shooter, level);
        setOwner(shooter);
        setWaterInertia(1.0D);
    }

    @Override
    protected boolean shouldDespawnOnHit(HitResult result) {
        return bounceCount > maxBounces;
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

            boolean damaged = shooter == null
                    ? target.hurt(GWRDamage.gunDamage(level().registryAccess(), this), damage)
                    : target.hurt(GWRDamage.gunDamage(level().registryAccess(), this, shooter), damage);
            if (!damaged) {
                target.invulnerableTime = lastHurtResistant;
            }

            if (target instanceof LivingEntity livingTarget && shooter instanceof LivingEntity livingShooter) {
                CeraunusBurstItem.applyWaterBulletHit(this, livingTarget, livingShooter, getDamage());
            }

            bounceFromEntity(target, shooter);
        }
        playBounceSound();
    }

    @Override
    protected void onHitBlock(BlockHitResult hit) {
        if (!level().isClientSide) {
            CeraunusBurstItem.applyWaterBulletBlockHit(level(), hit.getLocation());
            Vec3 motion = getDeltaMovement();
            Vec3 normal = Vec3.atLowerCornerOf(hit.getDirection().getNormal());
            double dot = motion.dot(normal);
            Vec3 standardBounce = motion.subtract(normal.scale(2.0D * dot));
            Vec3 randomBounce = standardBounce
                    .xRot((float) ((level().random.nextDouble() - 0.5D) * Math.PI / 2.0D))
                    .yRot((float) ((level().random.nextDouble() - 0.5D) * Math.PI / 2.0D))
                    .normalize()
                    .scale(standardBounce.length() * 0.65D);

            setDeltaMovement(randomBounce);
            registerBounce();
        }
        playBounceSound();
    }

    private void bounceFromEntity(Entity target, Entity shooter) {
        double searchRadius = 10.0D;
        List<LivingEntity> nearbyEntities = level().getEntitiesOfClass(
                LivingEntity.class,
                getBoundingBox().inflate(searchRadius),
                entity -> entity != target
                        && entity != shooter
                        && entity.isAlive()
                        && !entity.isSpectator()
        );

        if (!nearbyEntities.isEmpty()) {
            LivingEntity nearestEntity = nearbyEntities.get(0);
            double nearestDistance = position().distanceToSqr(nearestEntity.position());
            for (LivingEntity entity : nearbyEntities) {
                double distance = position().distanceToSqr(entity.position());
                if (distance < nearestDistance) {
                    nearestEntity = entity;
                    nearestDistance = distance;
                }
            }

            Vec3 toTarget = nearestEntity.position()
                    .add(0.0D, nearestEntity.getBbHeight() * 0.5D, 0.0D)
                    .subtract(position())
                    .normalize()
                    .scale(getDeltaMovement().length() * 0.65D);
            setDeltaMovement(toTarget);
        } else {
            double angleXZ = level().random.nextDouble() * Math.PI * 2.0D;
            double angleY = (level().random.nextDouble() - 0.5D) * Math.PI;
            Vec3 randomDirection = new Vec3(
                    Math.cos(angleXZ) * Math.cos(angleY),
                    Math.sin(angleY),
                    Math.sin(angleXZ) * Math.cos(angleY)
            ).normalize().scale(getDeltaMovement().length() * 0.65D);
            setDeltaMovement(randomDirection);
        }

        registerBounce();
    }

    private void registerBounce() {
        bounceCount++;
        getPersistentData().putBoolean(BulletHitEventHandler.ALLOW_SHOOTER_HIT, true);
    }

    private void playBounceSound() {
        level().playSound(null, getX(), getY(), getZ(), SoundEvents.BUBBLE_COLUMN_BUBBLE_POP,
                SoundSource.NEUTRAL, 0.75F, 1.15F + (level().random.nextFloat() - level().random.nextFloat()) * 0.2F);
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
        return ParticleTypes.SPLASH;
    }

    @Override
    public ParticleOptions gwrexpansions$getDuskfallTrailParticle() {
        return getTrailParticle();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("BounceCount", bounceCount);
        compound.putInt("MaxBounces", maxBounces);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        bounceCount = compound.getInt("BounceCount");
        maxBounces = compound.getInt("MaxBounces");
    }
}
