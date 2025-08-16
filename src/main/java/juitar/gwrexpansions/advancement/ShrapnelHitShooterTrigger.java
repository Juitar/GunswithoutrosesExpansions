package juitar.gwrexpansions.advancement;

import com.google.gson.JsonObject;
import juitar.gwrexpansions.GWRexpansions;
import juitar.gwrexpansions.registry.VanillaItem;
import lykrast.gunswithoutroses.entity.BulletEntity;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 钻石弹丸碎片伤害射手成就触发器
 */
@Mod.EventBusSubscriber(modid = GWRexpansions.MODID)
public class ShrapnelHitShooterTrigger extends SimpleCriterionTrigger<ShrapnelHitShooterTrigger.TriggerInstance> {

    private static final ResourceLocation ID = new ResourceLocation(GWRexpansions.MODID, "shrapnel_hit_shooter");

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
     * 供钻石弹丸代码调用的静态方法
     * 当钻石弹丸碎片伤害到射手时调用此方法
     */
    public static void onShrapnelHitShooter(ServerPlayer player) {
        GWRECriteria.SHRAPNEL_HIT_SHOOTER.trigger(player);
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity hurt = event.getEntity();
        Entity attacker = event.getSource().getDirectEntity();
        
        // 检查受伤者是否是玩家
        if (!(hurt instanceof ServerPlayer player)) {
            return;
        }
        
        // 检查攻击者是否是子弹实体
        if (!(attacker instanceof BulletEntity bullet)) {
            return;
        }
        
        // 检查子弹是否是钻石弹丸碎片
        ItemStack bulletItem = bullet.getItem();
        if (bulletItem.isEmpty() || !bulletItem.is(VanillaItem.diamond_bullet_shrapnel.get())) {
            return;
        }
        
        // 检查子弹的拥有者是否是受伤的玩家（即射手被自己的弹片伤害）
        Entity bulletOwner = bullet.getOwner();
        if (bulletOwner != null && bulletOwner.equals(player)) {
            // 触发成就
            GWRECriteria.SHRAPNEL_HIT_SHOOTER.trigger(player);
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