package juitar.gwrexpansions.event;

import juitar.gwrexpansions.config.GWREConfig;
import juitar.gwrexpansions.entity.BOMD.CoinEntity;
import juitar.gwrexpansions.entity.meetyourfight.DuskRoseSpiritEntity;
import juitar.gwrexpansions.item.BOMD.Hellforge;
import juitar.gwrexpansions.item.meetyourfight.DuskfallEclipseBlasterItem;
import juitar.gwrexpansions.registry.GWREEffects;
import juitar.gwrexpansions.util.CoinTargetUtils;
import lykrast.gunswithoutroses.entity.BulletEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 处理子弹击中事件的事件处理器
 */
@Mod.EventBusSubscriber
public class BulletHitEventHandler {

    // 防止递归调用的标记
    private static final ThreadLocal<Boolean> PROCESSING = ThreadLocal.withInitial(() -> false);
    public static final String ALLOW_SHOOTER_HIT = "AllowShooterHit";

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        if (!(event.getProjectile() instanceof BulletEntity bullet)
                || !(event.getRayTraceResult() instanceof EntityHitResult entityHit)) {
            return;
        }

        Entity owner = bullet.getOwner();
        Entity target = entityHit.getEntity();
        if (owner == null || target == null || bullet.getPersistentData().getBoolean(ALLOW_SHOOTER_HIT)) {
            return;
        }

        if (target == owner || target.isPassengerOfSameVehicle(owner)) {
            event.setImpactResult(ProjectileImpactEvent.ImpactResult.SKIP_ENTITY);
        }
    }

    /**
     * 使用高优先级确保我们的处理先于其他模组
     */
    @SubscribeEvent(priority = EventPriority.LOW)  // 改为低优先级，避免干扰其他事件
    public static void onLivingHurt(LivingHurtEvent event) {
        // 防止递归调用
        if (PROCESSING.get()) {
            return;
        }

        try {
            PROCESSING.set(true);

            // 检查事件是否已被取消
            if (event.isCanceled()) {
                return;
            }

            applyDuskfallSpiritModifiers(event);

            // 检查伤害来源是否是子弹
            if (event.getSource().getDirectEntity() instanceof BulletEntity bullet) {
                LivingEntity target = event.getEntity();
                Entity shooter = event.getSource().getEntity();

                // 安全检查
                if (target == null || bullet == null) {
                    return;
                }

                CompoundTag bulletData = bullet.getPersistentData();
                if (bulletData == null) {
                    return;
                }

                if (bulletData.getBoolean(DuskfallEclipseBlasterItem.SHOT_TAG) && shooter instanceof Player player) {
                    player.getPersistentData().putUUID(DuskfallEclipseBlasterItem.LAST_TARGET_TAG, target.getUUID());
                    player.getPersistentData().putLong(DuskfallEclipseBlasterItem.LAST_TARGET_TIME_TAG, target.level().getGameTime());
                }

                boolean isHellforgeShot = bulletData.getBoolean("HellforgeShot");

                // 检查子弹是否有HellforgeShot标记
                if (isHellforgeShot) {
                    // 处理aimed效果
                    handleAimedEffect(bullet, target, shooter);

                    // 检查是否需要进行硬币连锁反弹
                    checkCoinChaining(bullet, target, shooter);
                }
            }
        } catch (Exception e) {
            // 记录错误但不崩溃游戏
            System.err.println("Error in BulletHitEventHandler.onLivingHurt: " + e.getMessage());
            e.printStackTrace();
            // 不重新抛出异常，避免崩溃
        } finally {
            // 确保标记被重置
            PROCESSING.set(false);
        }
    }

    private static void applyDuskfallSpiritModifiers(LivingHurtEvent event) {
        Entity sourceEntity = event.getSource().getEntity();
        Entity directEntity = event.getSource().getDirectEntity();

        if (sourceEntity instanceof Player attacker && !(directEntity instanceof DuskRoseSpiritEntity)) {
            int spirits = DuskRoseSpiritEntity.countActiveFor(attacker);
            if (spirits > 0) {
                float multiplier = (float) (1.0D + spirits * GWREConfig.BURSTGUN.duskfallEclipse.damageBonusPerSpirit.get());
                event.setAmount(event.getAmount() * multiplier);
            }
        }

        if (event.getEntity() instanceof Player defender) {
            int spirits = DuskRoseSpiritEntity.countActiveFor(defender);
            if (spirits > 0) {
                double reduction = Math.min(0.95D, spirits * GWREConfig.BURSTGUN.duskfallEclipse.damageReductionPerSpirit.get());
                event.setAmount((float) (event.getAmount() * (1.0D - reduction)));
            }
        }
    }

    /**
     * 处理aimed效果的添加和移除，以及反弹子弹的额外伤害计算
     */
    private static void handleAimedEffect(BulletEntity bullet, LivingEntity target, Entity shooter) {
        try {
            CompoundTag bulletData = bullet.getPersistentData();

            // 检查是否是反弹子弹击中有aimed效果的目标
            int bounceCount = bulletData.getInt("CoinBounceCount");

            if (bounceCount > 0) { // 只有反弹后的子弹才计算aimed额外伤害
                MobEffectInstance targetAimed = target.getEffect(GWREEffects.AIMED.get());

                if (targetAimed != null) {
                    // 计算aimed额外伤害
                    int aimedLevel = targetAimed.getAmplifier() + 1; // 效果等级（1-5）

                    // 每等级1.5%最大生命值的额外伤害
                    float percentageDamage = aimedLevel * 0.015f; // 1.5% per level
                    float maxHealthDamage = target.getMaxHealth() * percentageDamage;

                    System.out.println("DEBUG: 反弹子弹额外伤害=" + maxHealthDamage + " (aimed等级=" + aimedLevel + ")");

                    // 应用额外伤害
                    if (maxHealthDamage > 0) {
                        try {
                            // 保存当前无敌时间
                            int originalInvulnerableTime = target.invulnerableTime;
                            target.invulnerableTime = 0; // 临时重置无敌时间以确保额外伤害生效

                            boolean extraDamaged = false;

                            // 安全地应用伤害
                            if (shooter instanceof LivingEntity livingShooter) {
                                extraDamaged = target.hurt(bullet.level().damageSources().indirectMagic(bullet, livingShooter), maxHealthDamage);
                            } else {
                                extraDamaged = target.hurt(bullet.level().damageSources().magic(), maxHealthDamage);
                            }

                            // 恢复无敌时间
                            target.invulnerableTime = originalInvulnerableTime;

                            // 如果额外伤害成功，播放特殊音效和粒子效果
                            if (extraDamaged && target.level() != null) {
                                try {
                                    target.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                                        net.minecraft.sounds.SoundEvents.PLAYER_HURT_SWEET_BERRY_BUSH,
                                        net.minecraft.sounds.SoundSource.PLAYERS, 0.8F, 1.5F);

                                    // 添加红色粒子效果表示额外伤害
                                    for (int i = 0; i < aimedLevel * 3; i++) {
                                        target.level().addParticle(net.minecraft.core.particles.ParticleTypes.DAMAGE_INDICATOR,
                                            target.getX() + (target.getRandom().nextDouble() - 0.5) * 1.0,
                                            target.getY() + target.getRandom().nextDouble() * 2.0,
                                            target.getZ() + (target.getRandom().nextDouble() - 0.5) * 1.0,
                                            0, 0, 0);
                                    }
                                } catch (Exception soundEx) {
                                    System.err.println("Error playing sound/particles: " + soundEx.getMessage());
                                }
                            }
                        } catch (Exception damageEx) {
                            System.err.println("Error applying extra damage: " + damageEx.getMessage());
                            damageEx.printStackTrace();
                        }
                    }
                }
            }

            // 检查是否需要移除aimed效果（在计算额外伤害之后）
            boolean shouldRemoveAimed = bulletData.getBoolean("RemoveAimedOnHit");
            int aimedTargetId = bulletData.getInt("AimedTargetId");

            if (shouldRemoveAimed && target.getId() == aimedTargetId) {
                target.removeEffect(GWREEffects.AIMED.get());
                bulletData.putBoolean("RemoveAimedOnHit", false);
                // 不再提前返回，继续执行后续逻辑
            }

            // 添加aimed效果（正常的Hellforge击中逻辑）
            MobEffectInstance currentEffect = target.getEffect(GWREEffects.AIMED.get());
            int newLevel = 0;
            int newDuration = 200; // 基础10秒

            if (currentEffect != null) {
                // 如果已有效果，增加等级和时间
                newLevel = Math.min(currentEffect.getAmplifier() + 1, 4); // 最高5级（0-4）
                newDuration = currentEffect.getDuration() + 100; // 增加5秒
            }
            // 应用新的效果
            target.addEffect(new MobEffectInstance(GWREEffects.AIMED.get(), newDuration, newLevel));

            // 调用Hellforge的击中处理方法
            Hellforge.onBulletHitLivingEntity(bullet, target, shooter);

        } catch (Exception e) {
            System.err.println("Error in handleAimedEffect: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 检查硬币连锁反弹
     */
    private static void checkCoinChaining(BulletEntity bullet, LivingEntity target, Entity shooter) {
        try {
            CompoundTag bulletData = bullet.getPersistentData();
            int bounceCount = bulletData.getInt("CoinBounceCount");

            // 如果子弹已经反弹过，检查是否可以继续连锁到其他硬币
            if (bounceCount > 0 && bounceCount < 5) { // 最多5次反弹
                final double SEARCH_RANGE = 32.0;
                Vec3 bulletPos = bullet.position();

                // 寻找附近的硬币进行连锁反弹
                CoinEntity nearestCoin = CoinTargetUtils.findNearestCoin(bullet.level(), bulletPos, SEARCH_RANGE);

                if (nearestCoin != null) {
                    // 设置子弹朝向硬币
                    Vec3 coinPos = nearestCoin.position();
                    Vec3 direction = coinPos.subtract(bulletPos).normalize();
                    double speed = 3.0;
                    bullet.setDeltaMovement(direction.scale(speed));

                    // 标记子弹正在寻找硬币连锁
                    bulletData.putBoolean("SeekingCoinChain", true);
                    bulletData.putInt("TargetCoinId", nearestCoin.getId());
                }
            }
        } catch (Exception e) {
            System.err.println("Error in checkCoinChaining: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 处理子弹寻找硬币连锁的tick事件
     */
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            // 在服务器tick结束时处理所有寻找硬币连锁的子弹
            event.getServer().getAllLevels().forEach(level -> {
                // 获取所有子弹实体
                level.getAllEntities().forEach(entity -> {
                    if (entity instanceof BulletEntity bullet) {
                        CompoundTag bulletData = bullet.getPersistentData();

                        if (bulletData.getBoolean("SeekingCoinChain")) {
                            handleCoinChainSeeking(bullet);
                        }
                        
                        // 处理花苞弹的EventScheduler
                        if (bullet instanceof juitar.gwrexpansions.entity.BOMD.BudBulletEntity budBullet) {
                            // 这里我们需要访问BudBulletEntity的eventScheduler
                            // 由于eventScheduler是private的，我们需要添加一个公共方法来运行它
                        }
                    }
                });
            });
        }
    }

    /**
     * 处理子弹寻找硬币连锁的逻辑
     */
    private static void handleCoinChainSeeking(BulletEntity bullet) {
        CompoundTag bulletData = bullet.getPersistentData();
        int targetCoinId = bulletData.getInt("TargetCoinId");

        // 查找目标硬币
        Entity targetEntity = bullet.level().getEntity(targetCoinId);
        if (targetEntity instanceof CoinEntity targetCoin && targetCoin.isAlive()) {
            // 检查是否接近硬币
            double distance = bullet.distanceTo(targetCoin);
            if (distance < 1.5) {
                // 触发硬币击中
                targetCoin.handleBulletHit(bullet);
                bulletData.putBoolean("SeekingCoinChain", false);
                bulletData.remove("TargetCoinId");
            } else {
                // 继续朝向硬币飞行
                Vec3 bulletPos = bullet.position();
                Vec3 coinPos = targetCoin.position();
                Vec3 direction = coinPos.subtract(bulletPos).normalize();
                double speed = 3.0;
                bullet.setDeltaMovement(direction.scale(speed));
            }
        } else {
            // 目标硬币不存在，停止寻找
            bulletData.putBoolean("SeekingCoinChain", false);
            bulletData.remove("TargetCoinId");
        }
    }
}
