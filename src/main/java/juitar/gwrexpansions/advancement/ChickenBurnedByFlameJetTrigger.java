package juitar.gwrexpansions.advancement;

import com.google.gson.JsonObject;
import com.github.L_Ender.cataclysm.entity.projectile.Flame_Jet_Entity;
import juitar.gwrexpansions.GWRexpansions;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Chicken;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
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
        LivingEntity deadEntity = event.getEntity();
        Entity killer = event.getSource().getDirectEntity();
        
        // 检查死亡的实体是否是鸡
        if (!(deadEntity instanceof Chicken)) {
            return;
        }
        
        // 检查杀手是否是火焰喷射实体
        if (!(killer instanceof Flame_Jet_Entity)) {
            return;
        }
        
        // 检查火焰喷射实体的拥有者是否是玩家
        Entity flameJetOwner = ((Flame_Jet_Entity) killer).getCaster();
        if (!(flameJetOwner instanceof ServerPlayer player)) {
            return;
        }
        
        // 触发成就
        GWRECriteria.CHICKEN_BURNED_BY_FLAME_JET.trigger(player);
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