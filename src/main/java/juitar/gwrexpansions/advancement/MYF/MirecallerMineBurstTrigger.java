package juitar.gwrexpansions.advancement.MYF;

import com.google.gson.JsonObject;
import juitar.gwrexpansions.GWRexpansions;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MirecallerMineBurstTrigger extends SimpleCriterionTrigger<MirecallerMineBurstTrigger.TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation(GWRexpansions.MODID, "mirecaller_mine_burst");
    private static final int REQUIRED_KILLS = 3;
    private static final Map<UUID, MineExplosion> ACTIVE_EXPLOSIONS = new HashMap<>();

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

    public static void beginMineExplosion(ServerPlayer player) {
        ACTIVE_EXPLOSIONS.put(player.getUUID(), new MineExplosion());
    }

    public static void endMineExplosion(ServerPlayer player) {
        ACTIVE_EXPLOSIONS.remove(player.getUUID());
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!event.getSource().is(DamageTypeTags.IS_EXPLOSION)) {
            return;
        }

        Entity attacker = event.getSource().getEntity();
        if (!(attacker instanceof ServerPlayer player)) {
            return;
        }

        MineExplosion explosion = ACTIVE_EXPLOSIONS.get(player.getUUID());
        if (explosion == null) {
            return;
        }

        explosion.kills++;
        if (explosion.kills >= REQUIRED_KILLS) {
            MYFCriteria.MIRECALLER_MINE_BURST.trigger(player);
            ACTIVE_EXPLOSIONS.remove(player.getUUID());
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

    private static class MineExplosion {
        private int kills;
    }
}
