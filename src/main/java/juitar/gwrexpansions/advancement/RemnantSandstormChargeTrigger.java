package juitar.gwrexpansions.advancement;

import com.google.gson.JsonObject;
import juitar.gwrexpansions.GWRexpansions;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = GWRexpansions.MODID)
public class RemnantSandstormChargeTrigger extends SimpleCriterionTrigger<RemnantSandstormChargeTrigger.TriggerInstance> {

    private static final ResourceLocation ID = new ResourceLocation(GWRexpansions.MODID, "remnant_sandstorm_charge");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public TriggerInstance createInstance(JsonObject json, ContextAwarePredicate entityPredicate,
                                          DeserializationContext deserializationContext) {
        return new TriggerInstance(entityPredicate);
    }

    public void trigger(ServerPlayer player) {
        this.trigger(player, instance -> instance.matches(player));
    }

    public static void onRemnantSandstormCharge(ServerPlayer player) {
        GWRECriteria.REMNANT_SANDSTORM_CHARGE.trigger(player);
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
