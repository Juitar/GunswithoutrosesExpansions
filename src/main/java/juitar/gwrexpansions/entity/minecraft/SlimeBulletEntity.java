package juitar.gwrexpansions.entity.minecraft;

import lykrast.gunswithoutroses.entity.BulletEntity;
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
                .scale(standardBounce.length() * 0.8D); // 保持20%的速度损失
            
            // 设置新的运动方向
            setDeltaMovement(randomBounce);
            
            bounceCount++;
            
            // 生成更多粒子效果来展示随机性
            for (int i = 0; i < 5; i++) {
                level().addParticle(ParticleTypes.ITEM_SLIME, 
                    getX(), getY(), getZ(),
                    (random.nextDouble() - 0.5) * 0.2,
                    (random.nextDouble() - 0.5) * 0.2,
                    (random.nextDouble() - 0.5) * 0.2);
            }
        }

        // 播放史莱姆跳跃音效
        level().playSound(null, getX(), getY(), getZ(),
                SoundEvents.SLIME_JUMP_SMALL, SoundSource.NEUTRAL,
                1.0F, 1.0F + (level().random.nextFloat() - level().random.nextFloat()) * 0.2F);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        // 对实体造成伤害
        if (!level().isClientSide) {
            Entity target = result.getEntity();
            Entity shooter = getOwner();
            
            // 造成伤害
            target.hurt(damageSources().thrown(this, shooter), (float) getDamage());
            
            // 计算反弹方向
            Vec3 motion = getDeltaMovement();
            // 反向运动
            setDeltaMovement(motion.scale(-0.8D));
            
            bounceCount++;
            
            // 生成粒子效果
            level().addParticle(ParticleTypes.ITEM_SLIME, 
                getX(), getY(), getZ(),
                0.0D, 0.0D, 0.0D);
        }
        level().playSound(null, result.getLocation().x, result.getLocation().y , result.getLocation().z,
                SoundEvents.SLIME_JUMP_SMALL, SoundSource.NEUTRAL,
                1.0F, 1.0F + (level().random.nextFloat() - level().random.nextFloat()) * 0.2F);
    }

    @Override
    protected ParticleOptions getTrailParticle() {
        return ParticleTypes.ITEM_SLIME;
    }

    @Override
    protected double waterInertia() {
        return 0.9; // 在水中的减速较小
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
