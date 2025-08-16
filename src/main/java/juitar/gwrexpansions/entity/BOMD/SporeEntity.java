package juitar.gwrexpansions.entity.BOMD;

import lykrast.gunswithoutroses.entity.BulletEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * 孢子子弹实体 - 速度慢且持续减速，速度到零后60tick消失，造成伤害可给射手治疗
 */
public class SporeEntity extends BulletEntity {
    private static final int MAX_LIFETIME_AFTER_STOP = 60; // 停止后60tick消失
    private static final double MIN_SPEED = 0.01; // 最小速度阈值
    private static final double DECELERATION_RATE = 0.98; // 减速率（每tick乘以此值）
    private static final float HEAL_RATIO = 0.5f; // 治疗比例（伤害的50%）
    private static final double SPIKE_DAMAGE = 6.0; // 尖刺伤害

    private int stoppedTicks = 0; // 停止后的tick计数
    private boolean hasStopped = false; // 是否已经停止

    public SporeEntity(EntityType<? extends BulletEntity> type, Level level) {
        super(type, level);
    }

    public SporeEntity(Level level, LivingEntity shooter) {
        super(level, shooter);
    }

    public SporeEntity(EntityType<? extends BulletEntity> type, Level level, LivingEntity shooter) {
        super(type, level);
        this.setOwner(shooter);
    }

    @Override
    public void tick() {
        super.tick();
        
        if (!this.level().isClientSide) {
            // 应用减速效果
            Vec3 motion = this.getDeltaMovement();
            double currentSpeed = motion.length();
            
            // 如果速度还没有降到最低阈值，继续减速
            if (currentSpeed > MIN_SPEED && !hasStopped) {
                Vec3 newMotion = motion.scale(DECELERATION_RATE);
                this.setDeltaMovement(newMotion);
                
                // 检查是否已经基本停止
                if (newMotion.length() <= MIN_SPEED) {
                    this.setDeltaMovement(Vec3.ZERO);
                    hasStopped = true;
                }
            } else if (!hasStopped) {
                // 速度已经很低，设为停止状态
                this.setDeltaMovement(Vec3.ZERO);
                hasStopped = true;
            }
            
            // 如果已经停止，开始计数
            if (hasStopped) {
                stoppedTicks++;
                
                // 停止后60tick消失
                if (stoppedTicks >= MAX_LIFETIME_AFTER_STOP) {
                    // 生成消失粒子效果
                    for (int i = 0; i < 8; i++) {
                        this.level().addParticle(ParticleTypes.SPORE_BLOSSOM_AIR,
                            this.getX() + (this.random.nextDouble() - 0.5) * 0.5,
                            this.getY() + (this.random.nextDouble() - 0.5) * 0.5,
                            this.getZ() + (this.random.nextDouble() - 0.5) * 0.5,
                            0, 0, 0);
                    }
                    this.discard();
                    return;
                }
            }
        }
        
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (!this.level().isClientSide) {
            Entity target = result.getEntity();
            Entity shooter = this.getOwner();
            
            if(target != shooter) super.onHitEntity(result);
            else{
                if (target instanceof LivingEntity) {
                    ((LivingEntity) target).heal(1);
                }
            }
            // 如果射手是生物实体，给予治疗
            if (shooter instanceof LivingEntity livingShooter && target instanceof LivingEntity) {
                float damage = (float) this.getDamage();
                float healAmount = damage * HEAL_RATIO;
                livingShooter.heal(healAmount);

                // 在射手身上生成治疗粒子效果
                for (int i = 0; i < 8; i++) {
                    this.level().addParticle(ParticleTypes.HEART,
                        livingShooter.getX() + (this.random.nextDouble() - 0.5) * 0.8,
                        livingShooter.getY() + livingShooter.getEyeHeight() + (this.random.nextDouble() - 0.5) * 0.5,
                        livingShooter.getZ() + (this.random.nextDouble() - 0.5) * 0.8,
                        0, 0.1, 0);
                }
            }
            
            // 播放孢子击中音效
            this.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.MOSS_BREAK, SoundSource.NEUTRAL, 0.8F, 1.2F);
            
            // 生成击中粒子效果
            for (int i = 0; i < 10; i++) {
                this.level().addParticle(ParticleTypes.SPORE_BLOSSOM_AIR,
                    target.getX() + (this.random.nextDouble() - 0.5) * 0.8,
                    target.getY() + (this.random.nextDouble() - 0.5) * 0.8,
                    target.getZ() + (this.random.nextDouble() - 0.5) * 0.8,
                    (this.random.nextDouble() - 0.5) * 0.2,
                    (this.random.nextDouble() - 0.5) * 0.2,
                    (this.random.nextDouble() - 0.5) * 0.2);
            }
            
            // 孢子击中后立即消失
            this.discard();
        }
    }

}
