package juitar.gwrexpansions.entity.meetyourfight;

import juitar.gwrexpansions.advancement.MYF.MirecallerMineBurstTrigger;
import juitar.gwrexpansions.config.GWREConfig;
import juitar.gwrexpansions.registry.GWREEntities;
import lykrast.gunswithoutroses.entity.BulletEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class MirecallerBombBulletEntity extends BulletEntity {
    private static final float DIRECT_HIT_DAMAGE_SCALE = 0.75F;
    private static final float SPLASH_MIN_DAMAGE_SCALE = 0.25F;
    private static final double DEFAULT_DAMAGE_RADIUS_SCALE = 2.5D;

    public MirecallerBombBulletEntity(EntityType<? extends BulletEntity> type, Level level) {
        super(type, level);
    }

    public MirecallerBombBulletEntity(Level level, LivingEntity shooter) {
        super(GWREEntities.MIRECALLER_BOMB_BULLET.get(), shooter, level);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity target = result.getEntity();
        LivingEntity directTarget = target instanceof LivingEntity living ? living : null;
        boolean headshot = directTarget != null && hasHeadshot(directTarget);
        Vec3 impact = target.getBoundingBox().inflate(0.3D)
                .clip(position(), position().add(getDeltaMovement()))
                .orElse(position());

        Entity shooter = getOwner();
        if (!level().isClientSide && shooter instanceof ServerPlayer player) {
            MirecallerMineBurstTrigger.beginMineExplosion(player);
            try {
                super.onHitEntity(result);
                if (directTarget != null && !directTarget.isAlive()) {
                    MirecallerMineBurstTrigger.recordMineKill(player);
                }
                explode(impact, directTarget, headshot);
            } finally {
                MirecallerMineBurstTrigger.endMineExplosion(player);
            }
            return;
        }

        super.onHitEntity(result);
        if (!level().isClientSide) {
            explode(impact, directTarget, headshot);
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult hit) {
        Entity shooter = getOwner();
        if (!level().isClientSide && shooter instanceof ServerPlayer player) {
            MirecallerMineBurstTrigger.beginMineExplosion(player);
            try {
                super.onHitBlock(hit);
                explode(hit.getLocation(), null, false);
            } finally {
                MirecallerMineBurstTrigger.endMineExplosion(player);
            }
            return;
        }

        super.onHitBlock(hit);
        if (!level().isClientSide) {
            explode(hit.getLocation(), null, false);
        }
    }

    private void explode(Vec3 center, @Nullable LivingEntity directTarget, boolean headshot) {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }

        Entity shooter = getOwner();
        float damage = (float) getDamage();
        double radius = Math.sqrt(Math.max(0.0D, damage)) * getConfiguredRadiusScale();
        if (headshot) {
            radius *= 1.5D;
            damage *= (float) getHeadshotMultiplier();
        }

        double radiusSqr = radius * radius;
        AABB blastBounds = new AABB(center, center).inflate(radius + 1.0D);
        for (LivingEntity target : serverLevel.getEntitiesOfClass(LivingEntity.class,
                blastBounds)) {
            if (!canSplashDamage(target, shooter, directTarget, center, radiusSqr)) {
                continue;
            }

            float distanceScale = getDistanceDamageScale(target, center, radiusSqr);
            if (isOnFire()) {
                target.setSecondsOnFire(5);
            }

            int originalInvulnerableTime = target.invulnerableTime;
            target.invulnerableTime = 0;
            boolean damaged = target.hurt(damageSources().explosion(this, shooter), damage * distanceScale);
            if (damaged) {
                applySplashKnockback(target, center, distanceScale);
                if (shooter instanceof LivingEntity livingShooter) {
                    doEnchantDamageEffects(livingShooter, target);
                }
            } else {
                target.invulnerableTime = originalInvulnerableTime;
            }
        }

        serverLevel.playSound(null, center.x, center.y, center.z,
                SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.0F, 1.0F);
        double particleSpeed = Math.max(0.1D, radius - 2.0D);
        serverLevel.sendParticles(ParticleTypes.EXPLOSION, center.x, center.y, center.z,
                Math.max(1, (int) (radiusSqr * Math.max(0.0D, radius - 1.0D))),
                particleSpeed, particleSpeed, particleSpeed, 0.0D);
    }

    private boolean canSplashDamage(LivingEntity target, @Nullable Entity shooter,
                                    @Nullable LivingEntity directTarget, Vec3 center, double radiusSqr) {
        return target.isAlive()
                && !target.isInvulnerable()
                && target != shooter
                && target != directTarget
                && target.getBoundingBox().distanceToSqr(center) < radiusSqr;
    }

    private float getDistanceDamageScale(LivingEntity target, Vec3 center, double radiusSqr) {
        double distanceSqr = target.getBoundingBox().distanceToSqr(center);
        if (distanceSqr <= 1.0D) {
            return DIRECT_HIT_DAMAGE_SCALE;
        }
        return (float) (DIRECT_HIT_DAMAGE_SCALE
                - (DIRECT_HIT_DAMAGE_SCALE - SPLASH_MIN_DAMAGE_SCALE) * (distanceSqr - 1.0D) / (radiusSqr - 1.0D));
    }

    private double getConfiguredRadiusScale() {
        return GWREConfig.SHOTGUN.Mirecaller.mineExplosionPower.get() / DEFAULT_DAMAGE_RADIUS_SCALE;
    }

    private void applySplashKnockback(LivingEntity target, Vec3 center, float distanceScale) {
        double knockback = getKnockbackStrength();
        if (knockback <= 0.0D) {
            return;
        }

        Vec3 push = target.position().subtract(center)
                .multiply(1.0D, 0.0D, 1.0D)
                .normalize()
                .scale(knockback * 0.6D * distanceScale);
        if (push.lengthSqr() > 0.0D) {
            target.push(push.x, 0.1D, push.z);
        }
    }

}
