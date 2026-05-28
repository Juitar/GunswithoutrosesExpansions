package juitar.gwrexpansions.entity.cataclysm;

import com.github.L_Ender.cataclysm.entity.projectile.Phantom_Halberd_Entity;
import com.github.L_Ender.cataclysm.init.ModParticle;
import juitar.gwrexpansions.config.GWREConfig;
import juitar.gwrexpansions.entity.meetyourfight.DuskfallBulletDelegate;
import juitar.gwrexpansions.item.cataclysm.CursiumGunItem;
import juitar.gwrexpansions.registry.GWREEntities;
import lykrast.gunswithoutroses.entity.BulletEntity;
import lykrast.gunswithoutroses.item.IBullet;
import lykrast.gunswithoutroses.registry.GWRDamage;
import lykrast.gunswithoutroses.registry.GWRItems;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class CursiumBulletEntity extends BulletEntity implements DuskfallBulletDelegate {
    private LivingEntity finalTarget;
    private boolean stopSeeking = false;
    private boolean SHOT_FROM_CURSIUM = false;
    private static final float SEEKING_SPEED = 0.4775F;
    private static final float VELOCITY_RETAIN = 0.625F;
    private static final float MIN_TRACKING_DISTANCE = 1.0F;
    private static final float MIN_VELOCITY = 1.25F;
    public CursiumBulletEntity(EntityType<? extends BulletEntity> type, Level level) {
        super(type, level);
    }
    public CursiumBulletEntity(Level level, LivingEntity shooter) {
        super(GWREEntities.CURSIUM_BULLET.get(), shooter, level);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity target = result.getEntity();
        Entity shooter = getOwner();


            // 保存并重置无敌时间
            int lastHurtResistant = target.invulnerableTime;
            target.invulnerableTime = 0;

            // 计算伤害，包括爆头检测
            float damage = (float) this.getDamage();
            boolean headshot = hasHeadshot(target);
            if (headshot) {
                CursiumGunItem.onBulletHeadshot(this, shooter, true);
                damage *= (float) getHeadshotMultiplier();
                // 爆头时召唤幻影戟
                if (SHOT_FROM_CURSIUM) {
                    List<LivingEntity> nearbyEntities = level().getEntitiesOfClass(
                            LivingEntity.class,
                            getBoundingBox().inflate(GWREConfig.BulletConfig.phantomHalberdRange.get()),
                            entity -> entity != shooter &&
                                    entity.isAlive() && !entity.isSpectator()
                    );
                    for (LivingEntity nearbyEntity : nearbyEntities) {
                        double spawnX = nearbyEntity.getX();
                        double spawnY = nearbyEntity.getY();
                        double spawnZ = nearbyEntity.getZ();

                        float yRot = (float) Math.atan2(nearbyEntity.getZ() - spawnZ, nearbyEntity.getX() - spawnX);

                        Phantom_Halberd_Entity phantomHalberd = new Phantom_Halberd_Entity(
                                level(),
                                spawnX,
                                spawnY,
                                spawnZ,
                                yRot,
                                GWREConfig.BulletConfig.phantomHalberdDelay.get(),
                                shooter instanceof LivingEntity ? (LivingEntity) shooter : null,
                                GWREConfig.BulletConfig.phantomHalberDamage.get().floatValue()
                        );

                        level().addFreshEntity(phantomHalberd);
                    }
                }
        }
            // 应用伤害
            boolean damaged = shooter == null
            ? target.hurt(GWRDamage.gunDamage(level().registryAccess(), this), damage)
            : target.hurt(GWRDamage.gunDamage(level().registryAccess(), this, shooter), damage);
            // 如果伤害未生效，恢复无敌时间
            if (!damaged) {
                target.invulnerableTime = lastHurtResistant;
            } else if (target instanceof LivingEntity livingTarget) {
                Item item = this.getItemRaw().getItem();
                IBullet bullet = item instanceof IBullet ? (IBullet) item : GWRItems.ironBullet.get();
                bullet.onLivingEntityHit(this, livingTarget, shooter, level(), headshot);
            }
    }

    public void setFinalTarget(LivingEntity target) {
        this.finalTarget = target;
    }

    public void setSHOT_FROM_CURSIUM(boolean SHOT_FROM_CURSIUM) {
        this.SHOT_FROM_CURSIUM = SHOT_FROM_CURSIUM;
    }

    @Override
    public boolean gwrexpansions$onDuskfallHitEntity(EntityHitResult result) {
        onHitEntity(result);
        return true;
    }

    @Override
    protected ParticleOptions getTrailParticle() {
        return ModParticle.PHANTOM_WING_FLAME.get();
    }

    @Override
    public ParticleOptions gwrexpansions$getDuskfallTrailParticle() {
        return getTrailParticle();
    }

    @Override
    public void tick() {
        super.tick();
        
        if (!level().isClientSide && !stopSeeking) {
            if (finalTarget != null && finalTarget.isAlive() && 
                !(finalTarget instanceof Player && finalTarget.isSpectator())) {
                float currentSpeed = (float) getDeltaMovement().length();
                
                if (currentSpeed > MIN_VELOCITY && tickCount > 2) {
                    Vec3 targetVector = finalTarget.position()
                        .add(0, finalTarget.getBbHeight() * 0.65F, 0)
                        .subtract(position());
                    
                    if (targetVector.length() > MIN_TRACKING_DISTANCE) {
                        setDeltaMovement(
                            getDeltaMovement().scale(VELOCITY_RETAIN)
                            .add(targetVector.normalize().scale(SEEKING_SPEED))
                        );
                    }
                }
            }
        }
    }
}
