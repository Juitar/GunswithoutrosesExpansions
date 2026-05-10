package juitar.gwrexpansions.advancement.MYF;

import com.google.gson.JsonObject;
import juitar.gwrexpansions.GWRexpansions;
import juitar.gwrexpansions.entity.meetyourfight.DuskRoseSpiritEntity;
import lykrast.meetyourfight.entity.ProjectileLineEntity;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DuskRoseStandAttackTrigger extends SimpleCriterionTrigger<DuskRoseStandAttackTrigger.TriggerInstance> {
    private static final ResourceLocation ID = new ResourceLocation(GWRexpansions.MODID, "dusk_rose_stand_attack");
    private static final float FULL_HEALTH_EPSILON = 0.01F;
    private static final long RECORD_TTL_TICKS = 6000L;
    private static final Map<UUID, DamageRecord> DAMAGE_RECORDS = new HashMap<>();

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

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide || event.isCanceled()) {
            return;
        }

        RoseHit roseHit = getRoseHit(event.getSource().getDirectEntity());
        UUID targetId = target.getUUID();

        if (roseHit == null) {
            DamageRecord record = DAMAGE_RECORDS.get(targetId);
            if (record != null) {
                DAMAGE_RECORDS.remove(targetId);
            }
            return;
        }

        DamageRecord record = DAMAGE_RECORDS.computeIfAbsent(targetId, ignored -> new DamageRecord());
        record.expiresAt = target.level().getGameTime() + RECORD_TTL_TICKS;
        UUID ownerId = roseHit.owner.getUUID();
        if (record.ownerId == null) {
            record.ownerId = ownerId;
        } else if (!record.ownerId.equals(ownerId)) {
            record.damagedByOther = true;
        }

        if (!record.hasRoseDamage && target.getHealth() < target.getMaxHealth() - FULL_HEALTH_EPSILON) {
            record.damagedByOther = true;
        }
        record.hasRoseDamage = true;
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity target = event.getEntity();
        RoseHit roseHit = getRoseHit(event.getSource().getDirectEntity());
        DamageRecord record = DAMAGE_RECORDS.remove(target.getUUID());

        if (roseHit == null || record == null || record.damagedByOther || !record.hasRoseDamage) {
            return;
        }

        if (record.ownerId != null && record.ownerId.equals(roseHit.owner.getUUID())) {
            MYFCriteria.DUSK_ROSE_STAND_ATTACK.trigger(roseHit.owner);
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.getServer().getTickCount() % 200 != 0) {
            return;
        }
        long now = event.getServer().overworld().getGameTime();
        DAMAGE_RECORDS.entrySet().removeIf(entry -> entry.getValue().expiresAt > 0 && entry.getValue().expiresAt < now);
    }

    private static RoseHit getRoseHit(Entity direct) {
        if (direct instanceof ProjectileLineEntity projectile
                && projectile.getOwner() instanceof DuskRoseSpiritEntity spirit
                && spirit.getOwnerPlayer() instanceof ServerPlayer owner) {
            return new RoseHit(owner);
        }
        return null;
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

    private record RoseHit(ServerPlayer owner) {
    }

    private static class DamageRecord {
        private UUID ownerId;
        private boolean hasRoseDamage;
        private boolean damagedByOther;
        private long expiresAt;
    }
}
