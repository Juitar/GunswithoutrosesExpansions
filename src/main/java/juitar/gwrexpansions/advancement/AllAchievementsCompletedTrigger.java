package juitar.gwrexpansions.advancement;

import com.google.gson.JsonObject;
import juitar.gwrexpansions.GWRexpansions;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.AdvancementEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 所有成就完成触发器
 */
@Mod.EventBusSubscriber(modid = GWRexpansions.MODID)
public class AllAchievementsCompletedTrigger extends SimpleCriterionTrigger<AllAchievementsCompletedTrigger.TriggerInstance> {

    private static final ResourceLocation ID = new ResourceLocation(GWRexpansions.MODID, "all_achievements_completed");

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

    @SubscribeEvent
    public static void onAdvancementGranted(AdvancementEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            ResourceLocation advancementId = event.getAdvancement().getId();
            
            // 避免在RIP AND TEAR成就本身触发时造成递归检查
            if (advancementId.toString().equals("gwrexpansions:rip_and_tear")) {
                return;
            }
            
            // 检查是否完成所有成就
            if (AdvancementManager.hasCompletedAllAchievements(player)) {
                GWRECriteria.ALL_ACHIEVEMENTS_COMPLETED.trigger(player);
            }
        }
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {

        public TriggerInstance(ContextAwarePredicate entityPredicate) {
            super(ID, entityPredicate);
        }

        public boolean matches(ServerPlayer player) {
            return AdvancementManager.hasCompletedAllAchievements(player);
        }

        @Override
        public JsonObject serializeToJson(SerializationContext serializationContext) {
            return super.serializeToJson(serializationContext);
        }
    }
} 