package juitar.gwrexpansions.entity.minecraft;

import lykrast.gunswithoutroses.entity.BulletEntity;
import lykrast.gunswithoutroses.registry.GWRDamage;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import java.util.Random;
import java.util.List;



public class SlimeBulletEntity extends BulletEntity {
    // 最大反弹次数
    private int maxBounces = 3;
    private int bounceCount = 0;

    public SlimeBulletEntity(EntityType<? extends BulletEntity> type, Level level) {
        super(type, level);
    }

    public SlimeBulletEntity(Level level, LivingEntity shooter) {
        super(level, shooter);
    }

    @Override
    protected boolean shouldDespawnOnHit(HitResult result) {
        // 只有达到最大反弹次数才消失
        return bounceCount > maxBounces;
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (!level().isClientSide) {
            // 获取原始运动方向和法线
            Vec3 motion = getDeltaMovement();
            Vec3 normal = Vec3.atLowerCornerOf(result.getDirection().getNormal());
            
            // 计算标准反弹方向
            double dot = motion.dot(normal);
            Vec3 standardBounce = motion.subtract(normal.scale(2.0D * dot));
            
            // 添加随机偏移（在45度范围内）
            Random random = new Random();
            double angleX = (random.nextDouble() - 0.5) * Math.PI / 2; // -45° 到 +45°
            double angleY = (random.nextDouble() - 0.5) * Math.PI / 2;
            
            // 创建旋转后的向量
            Vec3 randomBounce = standardBounce
                .xRot((float)angleX)
                .yRot((float)angleY)
                .normalize()
                .scale(standardBounce.length() * 0.65D);
            
            // 设置新的运动方向
            setDeltaMovement(randomBounce);
            
            bounceCount++;
            
        }
        
        // 播放史莱姆跳跃音效
        level().playSound(null, getX(), getY(), getZ(),
                SoundEvents.SLIME_JUMP_SMALL, SoundSource.NEUTRAL,
                1.0F, 1.0F + (level().random.nextFloat() - level().random.nextFloat()) * 0.2F);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (!level().isClientSide) {
            Entity target = result.getEntity();
            Entity shooter = getOwner();
            
            if (target instanceof LivingEntity) {
                int lastHurtResistant = target.invulnerableTime;
                target.invulnerableTime = 0;
                
                float damage = (float) getDamage();
                boolean headshot = hasHeadshot(target);
                if (headshot) {
                    damage *= getHeadshotMultiplier();
                }
                
                boolean damaged = shooter == null
                        ? target.hurt(GWRDamage.gunDamage(level().registryAccess(), this), damage)
                        : target.hurt(GWRDamage.gunDamage(level().registryAccess(), this, shooter), damage);
                
                if (!damaged) {
                    target.invulnerableTime = lastHurtResistant;
                }
            }
            
            // 寻找新目标并反弹
            double searchRadius = 10.0D; // 搜索范围
            List<LivingEntity> nearbyEntities = level().getEntitiesOfClass(
                LivingEntity.class,
                getBoundingBox().inflate(searchRadius),
                entity -> entity != target && entity != shooter && 
                         entity.isAlive() && !entity.isSpectator()
            );
            
            if (!nearbyEntities.isEmpty()) {
                // 找到最近的实体
                LivingEntity nearestEntity = nearbyEntities.get(0);
                double nearestDistance = position().distanceToSqr(nearestEntity.position());
                
                for (LivingEntity entity : nearbyEntities) {
                    double distance = position().distanceToSqr(entity.position());
                    if (distance < nearestDistance) {
                        nearestEntity = entity;
                        nearestDistance = distance;
                    }
                }
                
                // 计算到目标的向量
                Vec3 toTarget = nearestEntity.position()
                    .add(0, nearestEntity.getBbHeight() * 0.5D, 0)
                    .subtract(position())
                    .normalize()
                    .scale(getDeltaMovement().length() * 0.65D);
                
                setDeltaMovement(toTarget);
            } else {
                // 如果没有找到新目标，给一个随机方向
                Random random = new Random();
                double angleXZ = random.nextDouble() * Math.PI * 2; // 水平角度 0-360度
                double angleY = (random.nextDouble() - 0.5) * Math.PI; // 垂直角度 -90到90度
                
                // 计算随机方向向量
                double vx = Math.cos(angleXZ) * Math.cos(angleY);
                double vy = Math.sin(angleY);
                double vz = Math.sin(angleXZ) * Math.cos(angleY);
                
                Vec3 randomDirection = new Vec3(vx, vy, vz)
                    .normalize()
                    .scale(getDeltaMovement().length() * 0.65D);
                
                setDeltaMovement(randomDirection);
            }
            
            bounceCount++;
        }
        // 播放史莱姆跳跃音效
        level().playSound(null, getX(), getY(), getZ(),
                SoundEvents.SLIME_JUMP_SMALL, SoundSource.NEUTRAL,
                1.0F, 1.0F + (level().random.nextFloat() - level().random.nextFloat()) * 0.2F);
    }

    @Override
    protected ParticleOptions getTrailParticle() {
        return ParticleTypes.ITEM_SLIME;
    }

    @Override
    protected double waterInertia() {
        return 0.7; // 在水中的减速较小
    }

    public void setMaxBounces(int maxBounces) {
        this.maxBounces = maxBounces;
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
