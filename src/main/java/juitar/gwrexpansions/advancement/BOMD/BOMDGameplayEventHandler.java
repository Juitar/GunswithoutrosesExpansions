package juitar.gwrexpansions.advancement.BOMD;


import juitar.gwrexpansions.CompatModids;
import lykrast.gunswithoutroses.entity.BulletEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.ModList;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * BOMD模组游戏玩法事件处理器
 * 处理BOMD模组相关的游戏玩法成就触发
 */
@Mod.EventBusSubscriber
public class BOMDGameplayEventHandler {

    // 追踪硬币反弹的子弹
    private static final Map<UUID, Integer> coinBounceBullets = new HashMap<>();
    
    // 追踪黑曜石核心击杀
    private static final Map<UUID, Boolean> obsidianCoreKills = new HashMap<>();

    /**
     * 处理生物死亡事件，检查黑曜石核心击杀
     */
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!ModList.get().isLoaded(CompatModids.BOMD)) {
            return;
        }

        Entity killer = event.getSource().getDirectEntity();

        if (!isObsidianCoreEntity(killer) || !isReturningObsidianCore(killer)) {
            return;
        }

        Entity owner = getObsidianCoreOwner(killer);
        if (owner instanceof ServerPlayer player) {
            ObsidianCakeTrigger.onObsidianCoreKill(player);
        }
    }

    private static boolean isObsidianCoreEntity(Entity entity) {
        return entity != null && entity.getClass().getName().equals("juitar.gwrexpansions.entity.BOMD.ObsidianCoreEntity");
    }

    private static boolean isReturningObsidianCore(Entity entity) {
        try {
            Object returning = entity.getClass().getMethod("isReturning").invoke(entity);
            return returning instanceof Boolean value && value;
        } catch (ReflectiveOperationException | RuntimeException e) {
            return false;
        }
    }

    private static Entity getObsidianCoreOwner(Entity entity) {
        try {
            Object owner = entity.getClass().getMethod("getOwner").invoke(entity);
            return owner instanceof Entity ownerEntity ? ownerEntity : null;
        } catch (ReflectiveOperationException | RuntimeException e) {
            return null;
        }
    }

    /**
     * 处理生物受伤事件，检查硬币反弹击杀
     */
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!ModList.get().isLoaded(CompatModids.BOMD)) {
            return;
        }

        LivingEntity target = event.getEntity();
        Entity attacker = event.getSource().getDirectEntity();

        // 检查是否是子弹攻击
        if (attacker instanceof BulletEntity bullet) {
            // 检查子弹是否通过硬币反弹
            if (isCoinBounceBullet(bullet)) {
                // 检查目标是否是满血敌人
                if (target.getHealth() >= target.getMaxHealth()) {
                    Entity owner = bullet.getOwner();
                    if (owner instanceof ServerPlayer player) {
                        // 触发硬币反弹击杀成就
                        HellIsFullTrigger.onCoinRicochetKill(player);
                    }
                }
            }
        }
    }

    /**
     * 检查子弹是否通过硬币反弹
     */
    private static boolean isCoinBounceBullet(BulletEntity bullet) {
        return bullet.getPersistentData().getInt("CoinBounceCount") > 0;
    }

    /**
     * 记录硬币反弹的子弹（供CoinEntity调用）
     */
    public static void recordCoinBounceBullet(BulletEntity bullet) {
        coinBounceBullets.put(bullet.getUUID(), bullet.getPersistentData().getInt("CoinBounceCount"));
    }

    /**
     * 记录黑曜石核心击杀（供ObsidianCoreEntity调用）
     */
    public static void recordObsidianCoreKill(ServerPlayer player) {
        obsidianCoreKills.put(player.getUUID(), true);
    }
} 
