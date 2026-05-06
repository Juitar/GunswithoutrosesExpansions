package juitar.gwrexpansions.advancement;

import com.google.gson.JsonObject;
import juitar.gwrexpansions.CompatModids;
import juitar.gwrexpansions.GWRexpansions;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

/**
 * 鸡被火焰喷射烧死成就触发器
 */
@Mod.EventBusSubscriber(modid = GWRexpansions.MODID)
public class ChickenBurnedByFlameJetTrigger extends SimpleCriterionTrigger<ChickenBurnedByFlameJetTrigger.TriggerInstance> {

    private static final ResourceLocation ID = new ResourceLocation(GWRexpansions.MODID, "chicken_burned_by_flame_jet");

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
     * 供熔岩电池弹丸代码调用的静态方法
     * 当熔岩电池弹丸的火焰喷射烧死鸡时调用此方法
     */
    public static void onChickenBurnedByFlameJet(ServerPlayer player) {
        GWRECriteria.CHICKEN_BURNED_BY_FLAME_JET.trigger(player);
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        // 如果Cataclysm模组未加载,直接返回
        if (!ModList.get().isLoaded(CompatModids.CATACLYSM)) {
            return;
        }
        
        LivingEntity deadEntity = event.getEntity();
        Entity killer = event.getSource().getDirectEntity();
        
        // 检查死亡的实体是否是鸡
        if (!(deadEntity instanceof Chicken)) {
            return;
        }
        
        // 检查杀手是否是火焰喷射实体(使用类名字符串比较,避免直接引用Cataclysm类)
        if (killer == null) {
            return;
        }
        
        String killerClassName = killer.getClass().getName();
        if (!killerClassName.equals("com.github.L_Ender.cataclysm.entity.projectile.Flame_Jet_Entity")) {
            return;
        }
        
        // 使用反射获取火焰喷射实体的拥有者
        try {
            // 调用 getCaster() 方法
            Entity flameJetOwner = (Entity) killer.getClass().getMethod("getCaster").invoke(killer);
            
            if (flameJetOwner instanceof ServerPlayer player) {
                // 触发成就
                GWRECriteria.CHICKEN_BURNED_BY_FLAME_JET.trigger(player);
            }
        } catch (Exception e) {
            GWRexpansions.LOG.error("Failed to get flame jet owner for chicken burn achievement", e);
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