package juitar.gwrexpansions.event;

import juitar.gwrexpansions.CompatModids;
import juitar.gwrexpansions.GWRexpansions;
import juitar.gwrexpansions.config.GWREConfig;
import juitar.gwrexpansions.entity.BOMD.CoinEntity;
import juitar.gwrexpansions.entity.vanilla.SlimeBulletEntity;
import juitar.gwrexpansions.item.BOMD.Hellforge;
import juitar.gwrexpansions.item.vanilla.RedstoneBulletItem;
import juitar.gwrexpansions.registry.VanillaItem;
import juitar.gwrexpansions.util.CoinTargetUtils;
import lykrast.gunswithoutroses.entity.BulletEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
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
        if (!(event.getProjectile() instanceof BulletEntity bullet)) {
            return;
        }

        CompoundTag bulletData = bullet.getPersistentData();
        if (bulletData.getBoolean("HellforgeShot")
                && bulletData.getInt("HellforgeCoinChainHits") <= 0
                && shouldBreakHellforgeCoinChainOnImpact(event.getRayTraceResult())) {
            breakHellforgeCoinChainForBullet(bullet, bulletData);
        }

        if (!(event.getRayTraceResult() instanceof EntityHitResult entityHit)) {
            return;
        }

        Entity owner = bullet.getOwner();
        Entity target = entityHit.getEntity();
        if (owner == null || target == null) {
            return;
        }

        boolean allowShooterHit = bullet.getPersistentData().getBoolean(ALLOW_SHOOTER_HIT);
        if (allowShooterHit
                && (!isConfigurableShooterSelfDamageBullet(bullet)
                        || GWREConfig.GENERAL.allowShooterProjectileSelfDamage.get())) {
            return;
        }

        if (target == owner || target.isPassengerOfSameVehicle(owner)) {
            event.setImpactResult(ProjectileImpactEvent.ImpactResult.SKIP_ENTITY);
            return;
        }

    }

    private static boolean shouldBreakHellforgeCoinChainOnImpact(HitResult result) {
        if (result == null) {
            return false;
        }
        if (isBomdLoaded() && result instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof CoinEntity) {
            return false;
        }
        return result.getType() == HitResult.Type.BLOCK || result.getType() == HitResult.Type.ENTITY;
    }

    private static boolean isConfigurableShooterSelfDamageBullet(BulletEntity bullet) {
        ItemStack bulletItem = bullet.getItem();
        return bullet instanceof SlimeBulletEntity
                || (!bulletItem.isEmpty() && bulletItem.is(VanillaItem.diamond_bullet_shrapnel.get()));
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

                boolean isHellforgeShot = bulletData.getBoolean("HellforgeShot");

                // 检查子弹是否有HellforgeShot标记
                if (isHellforgeShot) {
                    bulletData.putFloat(Hellforge.BULLET_LAST_TARGET_HEALTH, target.getHealth());
                    bulletData.putFloat(Hellforge.BULLET_LAST_TARGET_MAX_HEALTH, target.getMaxHealth());
                    if (bulletData.getInt("HellforgeCoinChainHits") <= 0 && shooter instanceof LivingEntity livingShooter) {
                        Hellforge.breakCoinChain(livingShooter);
                    } else if (bulletData.getInt("HellforgeCoinChainHits") <= 0) {
                        breakHellforgeCoinChainForBullet(bullet, bulletData);
                    }

                    handleHellforgeCoinDamage(bullet, target, shooter);
                    Hellforge.onBulletHitLivingEntity(bullet, target, shooter);

                    // 检查是否需要进行硬币连锁反弹
                    checkCoinChaining(bullet, target, shooter);
                }
            }
        } catch (Exception e) {
            // 记录错误但不崩溃游戏
            GWRexpansions.LOG.error("Error in BulletHitEventHandler.onLivingHurt", e);
            // 不重新抛出异常，避免崩溃
        } finally {
            // 确保标记被重置
            PROCESSING.set(false);
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getSource().getDirectEntity() instanceof BulletEntity bullet)) {
            return;
        }

        CompoundTag bulletData = bullet.getPersistentData();
        if (!bulletData.getBoolean("HellforgeShot") || !(event.getSource().getEntity() instanceof LivingEntity shooter)) {
            return;
        }

        LivingEntity target = event.getEntity();
        boolean headshot = bulletData.getBoolean(Hellforge.BULLET_HEADSHOT);
        boolean ricoshot = bulletData.getInt("CoinBounceCount") > 0;
        float previousHealth = bulletData.getFloat(Hellforge.BULLET_LAST_TARGET_HEALTH);
        float previousMaxHealth = Math.max(1.0F, bulletData.getFloat(Hellforge.BULLET_LAST_TARGET_MAX_HEALTH));
        boolean oneShot = previousHealth >= previousMaxHealth * 0.75F;

        if (headshot) {
            Hellforge.recordStyleEvent(shooter, Hellforge.StyleEvent.HEADSHOT_KILL, 1, target);
        } else if (ricoshot) {
            Hellforge.recordStyleEvent(shooter, Hellforge.StyleEvent.RICOSHOT_KILL, Math.max(1, bulletData.getInt("CoinBounceCount")), target);
        } else if (oneShot) {
            Hellforge.recordStyleEvent(shooter, Hellforge.StyleEvent.ONE_SHOT, 1, target);
        } else {
            Hellforge.recordStyleEvent(shooter, Hellforge.StyleEvent.KILL, 1, target);
        }

        recordHellforgeMultiKill(shooter, target);
    }

    private static void recordHellforgeMultiKill(LivingEntity shooter, LivingEntity target) {
        ItemStack stack = Hellforge.findHellforgeStack(shooter);
        if (stack.isEmpty()) {
            return;
        }

        CompoundTag tag = stack.getOrCreateTag();
        int kills = tag.getInt(Hellforge.NBT_RECENT_KILL_TIMER) > 0 ? tag.getInt(Hellforge.NBT_RECENT_KILL_COUNT) + 1 : 1;
        tag.putInt(Hellforge.NBT_RECENT_KILL_COUNT, kills);
        tag.putInt(Hellforge.NBT_RECENT_KILL_TIMER, 60);

        if (kills == 2) {
            Hellforge.recordStyleEvent(shooter, Hellforge.StyleEvent.DOUBLE_KILL, kills, target);
        } else if (kills >= 3) {
            Hellforge.recordStyleEvent(shooter, Hellforge.StyleEvent.TRIPLE_KILL, kills, target);
        }
    }

    private static void handleHellforgeCoinDamage(BulletEntity bullet, LivingEntity target, Entity shooter) {
        try {
            CompoundTag bulletData = bullet.getPersistentData();
            int chainHits = bulletData.getInt("HellforgeCoinChainHits");
            if (chainHits <= 0) {
                return;
            }

            if (bulletData.getBoolean("HellforgeCoinPierceArmor")) {
                float percent = chainHits >= 5 ? 0.06F : 0.04F;
                float extraDamage = target.getMaxHealth() * percent;
                int originalInvulnerableTime = target.invulnerableTime;
                target.invulnerableTime = 0;
                if (shooter instanceof LivingEntity livingShooter) {
                    target.hurt(bullet.level().damageSources().indirectMagic(bullet, livingShooter), extraDamage);
                } else {
                    target.hurt(bullet.level().damageSources().magic(), extraDamage);
                }
                target.invulnerableTime = originalInvulnerableTime;
            }

            if (bulletData.getBoolean("HellforgeCoinSplash")) {
                double radius = chainHits >= 5 ? 4.0D : 3.0D;
                float splashDamage = (float) Math.max(2.0D, bullet.getDamage() * 0.35D);
                target.level().getEntitiesOfClass(LivingEntity.class,
                    target.getBoundingBox().inflate(radius),
                    entity -> entity != target && entity.isAlive() && entity != shooter
                ).forEach(entity -> {
                    int originalInvulnerableTime = entity.invulnerableTime;
                    entity.invulnerableTime = 0;
                    if (shooter instanceof LivingEntity livingShooter) {
                        entity.hurt(bullet.level().damageSources().indirectMagic(bullet, livingShooter), splashDamage);
                    } else {
                        entity.hurt(bullet.level().damageSources().magic(), splashDamage);
                    }
                    entity.invulnerableTime = originalInvulnerableTime;
                });
                bulletData.putBoolean("HellforgeCoinSplash", false);
            }
        } catch (Exception e) {
            GWRexpansions.LOG.error("Error in handleHellforgeCoinDamage", e);
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
            if (isBomdLoaded() && bounceCount > 0 && bounceCount < 5) { // 最多5次反弹
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
            GWRexpansions.LOG.error("Error in checkCoinChaining", e);
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
                RedstoneBulletItem.tickRedstonePulses(level);

                // 获取所有子弹实体
                level.getAllEntities().forEach(entity -> {
                    if (entity instanceof BulletEntity bullet) {
                        CompoundTag bulletData = bullet.getPersistentData();

                        tickHellforgeCoinMissGrace(bullet, bulletData);

                        if (bulletData.getBoolean("SeekingCoinChain")) {
                            handleCoinChainSeeking(bullet);
                        }
                    }
                });
            });
        }
    }

    private static void tickHellforgeCoinMissGrace(BulletEntity bullet, CompoundTag bulletData) {
        if (!bulletData.getBoolean("HellforgeShot") || !bulletData.contains(Hellforge.BULLET_COIN_MISS_GRACE)) {
            return;
        }
        if (bulletData.getInt("HellforgeCoinChainHits") > 0) {
            bulletData.remove(Hellforge.BULLET_COIN_MISS_GRACE);
            return;
        }

        int grace = bulletData.getInt(Hellforge.BULLET_COIN_MISS_GRACE) - 1;
        if (grace > 0) {
            bulletData.putInt(Hellforge.BULLET_COIN_MISS_GRACE, grace);
            return;
        }

        if (bullet.getOwner() instanceof LivingEntity livingOwner) {
            Hellforge.breakCoinChain(livingOwner);
        } else {
            breakHellforgeCoinChainForBullet(bullet, bulletData);
        }
        bulletData.remove(Hellforge.BULLET_COIN_MISS_GRACE);
    }

    private static void breakHellforgeCoinChainForBullet(BulletEntity bullet, CompoundTag bulletData) {
        if (bullet.getOwner() instanceof LivingEntity livingOwner) {
            Hellforge.breakCoinChain(livingOwner);
            return;
        }
        if (!bulletData.hasUUID(Hellforge.BULLET_SHOOTER_UUID) || !(bullet.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        ServerPlayer player = serverLevel.getServer().getPlayerList().getPlayer(bulletData.getUUID(Hellforge.BULLET_SHOOTER_UUID));
        if (player != null) {
            Hellforge.breakCoinChain(player);
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

    private static boolean isBomdLoaded() {
        return ModList.get().isLoaded(CompatModids.BOMD);
    }
}
