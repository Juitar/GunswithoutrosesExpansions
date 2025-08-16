package juitar.gwrexpansions.advancement;

import com.google.gson.JsonObject;
import juitar.gwrexpansions.GWRexpansions;
import juitar.gwrexpansions.item.vanilla.HungerBulletItem;
import juitar.gwrexpansions.registry.VanillaItem;
import lykrast.gunswithoutroses.entity.BulletEntity;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 饥饿弹丸消耗光饱食度成就触发器
 */
@Mod.EventBusSubscriber(modid = GWRexpansions.MODID)
public class HungerBulletDepleteFoodTrigger extends SimpleCriterionTrigger<HungerBulletDepleteFoodTrigger.TriggerInstance> {

    private static final ResourceLocation ID = new ResourceLocation(GWRexpansions.MODID, "hunger_bullet_deplete_food");
    
    // 用于追踪玩家使用饥饿弹丸的状态
    private static final Map<UUID, Boolean> hungerBulletUsers = new HashMap<>();

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public TriggerInstance createInstance(JsonObject json, ContextAwarePredicate entityPredicate, DeserializationContext deserializationContext) {
        return new TriggerInstance(entityPredicate);
    }

    public void trigger(ServerPlayer player) {
        this.trigger(player, instance -> instance.matches(player));
    }

    /**
     * 供饥饿弹丸代码调用的静态方法
     * 当玩家使用饥饿弹丸时调用此方法
     */
    public static void onHungerBulletUsed(ServerPlayer player) {
        hungerBulletUsers.put(player.getUUID(), true);
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        // 检查是否是饥饿弹丸造成的伤害
        if (event.getSource().getDirectEntity() instanceof BulletEntity bullet) {
            ItemStack bulletItem = bullet.getItem();
            if (!bulletItem.isEmpty() && bulletItem.is(VanillaItem.hunger_bullet.get())) {
                Entity target = event.getEntity();
                if (target instanceof ServerPlayer player) {
                    // 标记该玩家使用了饥饿弹丸
                    hungerBulletUsers.put(player.getUUID(), true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            UUID playerUUID = player.getUUID();
            
            // 检查玩家是否使用了饥饿弹丸且饱食度为0
            if (hungerBulletUsers.containsKey(playerUUID) && 
                hungerBulletUsers.get(playerUUID) && 
                player.getFoodData().getFoodLevel() <= 0) {
                
                // 触发成就
                GWRECriteria.HUNGER_BULLET_DEPLETE_FOOD.trigger(player);
                
                // 清除标记，避免重复触发
                hungerBulletUsers.remove(playerUUID);
            }
        }
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {

        public TriggerInstance(ContextAwarePredicate entityPredicate) {
            super(ID, entityPredicate);
        }

        public boolean matches(ServerPlayer player) {
            return true;
        }

        @Override
        public JsonObject serializeToJson(SerializationContext serializationContext) {
            return super.serializeToJson(serializationContext);
        }
    }
} 