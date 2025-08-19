package juitar.gwrexpansions.advancement.BOMD;

import com.google.gson.JsonObject;
import juitar.gwrexpansions.GWRexpansions;
import juitar.gwrexpansions.advancement.GWRECriteria;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.common.Mod;

/**
 * 用黑曜石发射器释放三个不同魔咒成就触发器
 */
@Mod.EventBusSubscriber(modid = GWRexpansions.MODID)
public class AvadaKedavraTrigger extends SimpleCriterionTrigger<AvadaKedavraTrigger.TriggerInstance> {

    private static final ResourceLocation ID = new ResourceLocation(GWRexpansions.MODID, "avada_kedavra");

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
     * 供BOMD模组代码调用的静态方法
     * 当玩家用黑曜石发射器释放三个不同魔咒时调用此方法
     */
    public static void onThreeSpellsCast(ServerPlayer player) {
        GWRECriteria.AVADA_KEDAVRA.trigger(player);
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