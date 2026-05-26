package juitar.gwrexpansions.advancement;

import com.google.gson.JsonObject;
import juitar.gwrexpansions.GWRexpansions;
import juitar.gwrexpansions.item.cataclysm.HarbingerRaycasterItem;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = GWRexpansions.MODID)
public class HarbingerOverloadProtocolTrigger extends SimpleCriterionTrigger<HarbingerOverloadProtocolTrigger.TriggerInstance> {

    private static final ResourceLocation ID = new ResourceLocation(GWRexpansions.MODID, "harbinger_overload_protocol");

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

    public static void onHarbingerOverloadKill(ServerPlayer player) {
        GWRECriteria.HARBINGER_OVERLOAD_PROTOCOL.trigger(player);
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Enemy)) {
            return;
        }

        Entity direct = event.getSource().getDirectEntity();
        Entity source = event.getSource().getEntity();
        ServerPlayer player = getHarbingerOwner(direct);
        if (player == null) {
            player = getHarbingerOwner(source);
        }
        if (player != null) {
            onHarbingerOverloadKill(player);
        }
    }

    private static ServerPlayer getHarbingerOwner(Entity entity) {
        if (entity == null || !(entity.level() instanceof ServerLevel level)) {
            return null;
        }

        CompoundTag data = entity.getPersistentData();
        if (!data.getBoolean(HarbingerRaycasterItem.HARBINGER_OVERLOAD_WEAPON_TAG)
                || !data.hasUUID(HarbingerRaycasterItem.HARBINGER_OVERLOAD_OWNER_TAG)) {
            return null;
        }

        UUID ownerId = data.getUUID(HarbingerRaycasterItem.HARBINGER_OVERLOAD_OWNER_TAG);
        Player player = level.getPlayerByUUID(ownerId);
        return player instanceof ServerPlayer serverPlayer ? serverPlayer : null;
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
