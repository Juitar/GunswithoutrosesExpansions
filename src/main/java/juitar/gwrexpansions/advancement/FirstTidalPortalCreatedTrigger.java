package juitar.gwrexpansions.advancement;

import com.google.gson.JsonObject;
import juitar.gwrexpansions.GWRexpansions;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.common.Mod;

/**
 * 第一次潮汐传送门创建成就触发器
 */
@Mod.EventBusSubscriber(modid = GWRexpansions.MODID)
public class FirstTidalPortalCreatedTrigger extends SimpleCriterionTrigger<FirstTidalPortalCreatedTrigger.TriggerInstance> {

    private static final ResourceLocation ID = new ResourceLocation(GWRexpansions.MODID, "first_tidal_portal_created");

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
     * 供潮汐弹丸代码调用的静态方法
     * 当潮汐弹丸第一次创建传送门时调用此方法
     */
    public static void onFirstTidalPortalCreated(ServerPlayer player) {
        GWRECriteria.FIRST_TIDAL_PORTAL_CREATED.trigger(player);
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