package juitar.gwrexpansions.entity.cataclysm;

import lykrast.gunswithoutroses.entity.BulletEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class CursiumBulletEntity extends BulletEntity {
    private LivingEntity finalTarget;
    private boolean stopSeeking = false;
    private static final float SEEKING_SPEED = 0.4775F;
    private static final float VELOCITY_RETAIN = 0.625F;
    private static final float MIN_TRACKING_DISTANCE = 1.0F;
    private static final float MIN_VELOCITY = 1.25F;

    public CursiumBulletEntity(EntityType<? extends BulletEntity> type, Level level) {
        super(type, level);
    }

    public CursiumBulletEntity(Level level, LivingEntity shooter) {
        super(level, shooter);
    }

    public void setFinalTarget(LivingEntity target) {
        this.finalTarget = target;
    }

    @Override
    public void tick() {
        super.tick();
        
        if (!level().isClientSide  && !stopSeeking) {
            // 检查目标是否有效
            if (finalTarget != null && finalTarget.isAlive() && 
                !(finalTarget instanceof Player && finalTarget.isSpectator())) {
                
                // 获取当前速度
                float currentSpeed = (float) getDeltaMovement().length();
                
                // 只有在速度足够快且飞行超过2tick时才进行追踪
                if (currentSpeed > MIN_VELOCITY && tickCount > 2) {
                    // 计算到目标的向量（瞄准目标躯干中心点）
                    Vec3 targetVector = finalTarget.position()
                        .add(0, finalTarget.getBbHeight() * 0.65F, 0)
                        .subtract(position());
                    
                    // 如果距离目标足够远才进行追踪
                    if (targetVector.length() > MIN_TRACKING_DISTANCE) {
                        // 调整子弹运动方向
                        setDeltaMovement(
                            getDeltaMovement().scale(VELOCITY_RETAIN)  // 保持部分原有速度
                            .add(targetVector.normalize().scale(SEEKING_SPEED))  // 添加向目标的追踪速度
                        );
                    }
                }
            }
        }
    }

    public void setStopSeeking(boolean stop) {
        this.stopSeeking = stop;
    }
}
