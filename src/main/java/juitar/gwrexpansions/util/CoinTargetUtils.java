package juitar.gwrexpansions.util;

import juitar.gwrexpansions.entity.BOMD.CoinEntity;
import juitar.gwrexpansions.registry.GWREEffects;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * 硬币反弹系统的目标查找工具类
 * 包含寻找硬币、aimed目标和普通敌人的方法
 */
public class CoinTargetUtils {
    private static final double MAX_RICOCHET_TARGET_RANGE = 22.0D;
    private static final double MAX_INTENT_TARGET_RANGE = 24.0D;
    private static final double INTENT_DOT_THRESHOLD = 0.985D;
    
    /**
     * 寻找最近的具有aimed效果的目标
     */
    public static LivingEntity findNearestAimedTarget(Level level, Vec3 position, double range) {
        AABB searchArea = new AABB(position.subtract(range, range, range), position.add(range, range, range));
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, searchArea);

        LivingEntity nearestTarget = null;
        double nearestDistance = Double.MAX_VALUE;

        for (LivingEntity entity : entities) {
            // 检查实体是否有aimed效果
            boolean hasAimedEffect = entity.hasEffect(GWREEffects.AIMED.get());
            if (hasAimedEffect && entity.isAlive()) {
                double distance = entity.position().distanceTo(position);
                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    nearestTarget = entity;
                }
            }
        }

        return nearestTarget;
    }

    /**
     * 寻找最近的敌人（不限制aimed效果）
     */
    public static LivingEntity findNearestEnemy(Level level, Vec3 position, LivingEntity owner, double range) {
        AABB searchArea = new AABB(position.subtract(range, range, range), position.add(range, range, range));
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, searchArea);
        LivingEntity nearestTarget = null;
        double nearestDistance = Double.MAX_VALUE;
        for (LivingEntity entity : entities) {
            // 排除所有者和友方实体
            if (entity == owner || !entity.isAlive() || entity.isSpectator()) {
                continue;
            }

            // 简单的敌对判断：不是玩家的宠物/坐骑
            if (entity instanceof net.minecraft.world.entity.OwnableEntity ownable && ownable.getOwner() == owner) {
                continue;
            }

            double distance = entity.position().distanceTo(position);
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearestTarget = entity;
            }
        }

        return nearestTarget;
    }

    /**
     * 寻找硬币反弹目标。距离仍重要，但优先选择高价值、正在威胁玩家且无方块遮挡的目标。
     */
    public static LivingEntity findBestRicochetTarget(Level level, Vec3 position, LivingEntity owner, double range) {
        return findBestRicochetTarget(level, position, owner, range, -1);
    }

    public static LivingEntity findBestRicochetTarget(Level level, Vec3 position, LivingEntity owner, double range, int priorityTargetId) {
        double cappedRange = Math.min(range, MAX_RICOCHET_TARGET_RANGE);
        double intentRange = Math.min(range, MAX_INTENT_TARGET_RANGE);
        double searchRange = Math.max(cappedRange, intentRange);
        AABB searchArea = new AABB(position.subtract(searchRange, searchRange, searchRange), position.add(searchRange, searchRange, searchRange));
        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, searchArea);
        LivingEntity priorityTarget = findPriorityTarget(level, position, owner, entities, priorityTargetId, cappedRange);
        if (priorityTarget != null) {
            return priorityTarget;
        }

        LivingEntity intentTarget = findIntentTarget(level, position, owner, entities, intentRange);
        if (intentTarget != null) {
            return intentTarget;
        }

        LivingEntity bestTarget = null;
        double bestScore = Double.MAX_VALUE;

        for (LivingEntity entity : entities) {
            if (!isValidEnemy(entity, owner) || !hasLineOfSight(level, position, entity)) {
                continue;
            }

            double distance = entity.position().distanceTo(position);
            if (distance > cappedRange) {
                continue;
            }
            double score = distance * 1.15D;
            boolean boss = isBoss(entity);
            boolean hostile = entity instanceof Enemy;
            boolean attackingOwner = entity instanceof Mob mob && mob.getTarget() == owner;
            boolean hurtOwner = owner.getLastHurtByMob() == entity;
            boolean recentlyHitByOwner = owner.getLastHurtMob() == entity;
            boolean neutral = entity instanceof NeutralMob;

            if (boss) {
                score -= 35.0D;
            }
            if (hostile) {
                score -= 18.0D;
            }
            if (attackingOwner) {
                score -= 16.0D;
            }
            if (hurtOwner) {
                score -= 12.0D;
            }
            if (recentlyHitByOwner) {
                score -= 8.0D;
            }
            if (neutral) {
                score -= 4.0D;
            }

            Vec3 targetDirection = entity.getBoundingBox().getCenter().subtract(position).normalize();
            double aimDot = owner.getLookAngle().normalize().dot(targetDirection);
            if (aimDot > 0.0D) {
                score -= aimDot * 6.0D;
            }
            score -= Math.min(entity.getMaxHealth(), 200.0F) * 0.06D;
            score -= Math.min(entity.getHealth(), 100.0F) * 0.02D;

            if (score < bestScore) {
                bestScore = score;
                bestTarget = entity;
            }
        }

        return bestTarget != null ? bestTarget : findFallbackPassiveTarget(level, position, owner, entities, cappedRange);
    }

    private static LivingEntity findPriorityTarget(Level level, Vec3 position, LivingEntity owner, List<LivingEntity> entities, int priorityTargetId, double range) {
        if (priorityTargetId < 0) {
            return null;
        }
        for (LivingEntity entity : entities) {
            if (entity.getId() != priorityTargetId || !isValidBaseTarget(entity, owner)) {
                continue;
            }
            if (entity.position().distanceTo(position) <= range && hasLineOfSight(level, position, entity)) {
                return entity;
            }
        }
        return null;
    }

    private static LivingEntity findIntentTarget(Level level, Vec3 coinPosition, LivingEntity owner, List<LivingEntity> entities, double range) {
        Vec3 eyePosition = owner.getEyePosition();
        Vec3 look = owner.getLookAngle().normalize();
        LivingEntity bestTarget = null;
        double bestScore = Double.MAX_VALUE;

        for (LivingEntity entity : entities) {
            if (!isValidBaseTarget(entity, owner) || !isIntentEligible(entity, owner)) {
                continue;
            }

            Vec3 targetCenter = entity.getBoundingBox().getCenter();
            Vec3 toTarget = targetCenter.subtract(eyePosition);
            double distance = toTarget.length();
            if (distance > range || distance <= 0.001D) {
                continue;
            }

            Vec3 direction = toTarget.normalize();
            double aimDot = look.dot(direction);
            double allowedMiss = Math.max(1.0D, entity.getBbWidth() * 0.75D);
            double perpendicularMiss = Math.sqrt(Math.max(0.0D, 1.0D - aimDot * aimDot)) * distance;
            if (aimDot < INTENT_DOT_THRESHOLD && perpendicularMiss > allowedMiss) {
                continue;
            }
            if (!hasLineOfSight(level, eyePosition, entity) || !hasLineOfSight(level, coinPosition, entity)) {
                continue;
            }

            double score = perpendicularMiss * 8.0D + distance * 0.12D;
            if (entity instanceof NeutralMob) {
                score -= 4.0D;
            }
            if (owner.getLastHurtMob() == entity) {
                score -= 8.0D;
            }
            if (owner.getLastHurtByMob() == entity) {
                score -= 6.0D;
            }
            if (score < bestScore) {
                bestScore = score;
                bestTarget = entity;
            }
        }

        return bestTarget;
    }

    private static boolean isValidBaseTarget(LivingEntity entity, LivingEntity owner) {
        if (entity == owner || !entity.isAlive() || entity.isSpectator()) {
            return false;
        }
        if (entity instanceof Player) {
            return false;
        }
        if (entity instanceof net.minecraft.world.entity.OwnableEntity ownable && ownable.getOwner() == owner) {
            return false;
        }
        return true;
    }

    private static boolean isValidEnemy(LivingEntity entity, LivingEntity owner) {
        if (!isValidBaseTarget(entity, owner)) {
            return false;
        }
        boolean boss = isBoss(entity);
        boolean hostile = entity instanceof Enemy;
        boolean attackingOwner = entity instanceof Mob mob && mob.getTarget() == owner;
        boolean hurtOwner = owner.getLastHurtByMob() == entity;
        boolean recentlyHitByOwner = owner.getLastHurtMob() == entity;
        boolean neutralInCombat = entity instanceof NeutralMob && (attackingOwner || hurtOwner || recentlyHitByOwner);
        return boss || hostile || attackingOwner || hurtOwner || recentlyHitByOwner || neutralInCombat;
    }

    private static boolean isIntentEligible(LivingEntity entity, LivingEntity owner) {
        if (isValidEnemy(entity, owner)) {
            return true;
        }
        return entity instanceof NeutralMob;
    }

    private static LivingEntity findFallbackPassiveTarget(Level level, Vec3 position, LivingEntity owner, List<LivingEntity> entities, double range) {
        LivingEntity nearestTarget = null;
        double nearestDistance = Double.MAX_VALUE;

        for (LivingEntity entity : entities) {
            if (!isValidBaseTarget(entity, owner) || isValidEnemy(entity, owner)) {
                continue;
            }
            double distance = entity.position().distanceTo(position);
            if (distance > range || !hasLineOfSight(level, position, entity)) {
                continue;
            }
            if (distance < nearestDistance) {
                nearestDistance = distance;
                nearestTarget = entity;
            }
        }

        return nearestTarget;
    }

    private static boolean isBoss(LivingEntity entity) {
        return entity instanceof WitherBoss || entity instanceof EnderDragon;
    }

    private static boolean hasLineOfSight(Level level, Vec3 position, LivingEntity entity) {
        Vec3 target = entity.getBoundingBox().getCenter();
        return level.clip(new ClipContext(position, target, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, entity)).getType() == HitResult.Type.MISS;
    }

    /**
     * 寻找最近的硬币实体
     */
    public static CoinEntity findNearestCoin(Level level, Vec3 position, double range) {
        AABB searchArea = new AABB(position.subtract(range, range, range), position.add(range, range, range));
        List<CoinEntity> coins = level.getEntitiesOfClass(CoinEntity.class, searchArea);

        CoinEntity nearestCoin = null;
        double nearestDistance = Double.MAX_VALUE;

        for (CoinEntity coin : coins) {
            if (coin.isAlive()) {
                double distance = coin.position().distanceTo(position);
                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    nearestCoin = coin;
                }
            }
        }

        return nearestCoin;
    }
}
