package juitar.gwrexpansions.advancement.MYF;

import com.google.gson.JsonObject;
import juitar.gwrexpansions.GWRexpansions;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DestinyAllInTrigger extends SimpleCriterionTrigger<DestinyAllInTrigger.TriggerInstance> {
    public static final String JACKPOT_GROUP_TAG = "DestinyJackpotGroup";
    private static final ResourceLocation ID = new ResourceLocation(GWRexpansions.MODID, "destiny_all_in");
    private static final int REQUIRED_KILLS = 2;
    private static final long JACKPOT_WINDOW_TICKS = 120L;
    private static final Map<UUID, JackpotWindow> ACTIVE_JACKPOTS = new HashMap<>();

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public TriggerInstance createInstance(JsonObject json, ContextAwarePredicate entityPredicate, DeserializationContext deserializationContext) {
        return new TriggerInstance(entityPredicate);
    }

    public void trigger(ServerPlayer player) {
        this.trigger(player, instance -> instance.matches());
    }

    public static UUID onJackpot(ServerPlayer player) {
        UUID group = UUID.randomUUID();
        ACTIVE_JACKPOTS.put(player.getUUID(), new JackpotWindow(group, player.level().getGameTime() + JACKPOT_WINDOW_TICKS));
        return group;
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        Entity attacker = event.getSource().getEntity();
        Entity direct = event.getSource().getDirectEntity();
        if (!(attacker instanceof ServerPlayer player) || direct == null || !direct.getPersistentData().hasUUID(JACKPOT_GROUP_TAG)) {
            return;
        }

        JackpotWindow window = ACTIVE_JACKPOTS.get(player.getUUID());
        if (window == null || !window.group.equals(direct.getPersistentData().getUUID(JACKPOT_GROUP_TAG))) {
            return;
        }

        long now = player.level().getGameTime();
        if (now > window.expiresAt) {
            ACTIVE_JACKPOTS.remove(player.getUUID());
            return;
        }

        window.kills++;
        if (window.kills >= REQUIRED_KILLS) {
            MYFCriteria.DESTINY_ALL_IN.trigger(player);
            ACTIVE_JACKPOTS.remove(player.getUUID());
        }
    }

    public static class TriggerInstance extends AbstractCriterionTriggerInstance {
        public TriggerInstance(ContextAwarePredicate entityPredicate) {
            super(ID, entityPredicate);
        }

        public boolean matches() {
            return true;
        }

        @Override
        public JsonObject serializeToJson(SerializationContext serializationContext) {
            return super.serializeToJson(serializationContext);
        }
    }

    private static class JackpotWindow {
        private final UUID group;
        private final long expiresAt;
        private int kills;

        private JackpotWindow(UUID group, long expiresAt) {
            this.group = group;
            this.expiresAt = expiresAt;
        }
    }
}
