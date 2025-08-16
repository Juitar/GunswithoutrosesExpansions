package juitar.gwrexpansions.util;

import juitar.gwrexpansions.entity.BOMD.CoinEntity;
import juitar.gwrexpansions.registry.GWREEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * 硬币反弹系统的目标查找工具类
 * 包含寻找硬币、aimed目标和普通敌人的方法
 */
public class CoinTargetUtils {
    
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
