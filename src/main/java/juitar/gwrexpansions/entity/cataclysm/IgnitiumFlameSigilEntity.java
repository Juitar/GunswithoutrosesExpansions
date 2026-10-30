package juitar.gwrexpansions.entity.cataclysm;

import juitar.gwrexpansions.config.GWREConfig;
import juitar.gwrexpansions.registry.GWREEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.minecraftforge.network.NetworkHooks;
import javax.annotation.Nullable;
import java.util.UUID;

/** Ground warning followed by a single delayed blast. No terrain modification. */
public class IgnitiumFlameSigilEntity extends Entity {
    public static final int DELAY = 30;
    public static final float RADIUS = 4.5F;
    private static final EntityDataAccessor<Boolean> SOUL = SynchedEntityData.defineId(IgnitiumFlameSigilEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> REMAINING = SynchedEntityData.defineId(IgnitiumFlameSigilEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> BLAST_RADIUS = SynchedEntityData.defineId(IgnitiumFlameSigilEntity.class, EntityDataSerializers.FLOAT);
    private UUID ownerId;
    @Nullable private LivingEntity owner;
    private float damage = 18;

    public IgnitiumFlameSigilEntity(EntityType<? extends IgnitiumFlameSigilEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
    }

    public IgnitiumFlameSigilEntity(ServerLevel level, Vec3 position, @Nullable LivingEntity owner, boolean soul) {
        this(GWREEntities.IGNITIUM_FLAME_SIGIL.get(), level);
        setPos(position);
        ownerId = owner == null ? null : owner.getUUID();
        this.owner = owner;
        entityData.set(SOUL, soul);
        var config = GWREConfig.GATLING.Ignitium;
        entityData.set(REMAINING, config.sigilDelayTicks.get());
        entityData.set(BLAST_RADIUS, config.sigilRadius.get().floatValue());
        damage = (soul ? config.soulSigilDamage : config.sigilDamage).get().floatValue();
    }

    @Override protected void defineSynchedData() {
        entityData.define(SOUL, false);
        entityData.define(REMAINING, DELAY);
        entityData.define(BLAST_RADIUS, RADIUS);
    }

    public boolean isSoul() { return entityData.get(SOUL); }
    public int remainingTicks() { return entityData.get(REMAINING); }
    public float blastRadius() { return entityData.get(BLAST_RADIUS); }

    @Nullable
    public LivingEntity getOwner() {
        if (owner != null && !owner.isRemoved()) return owner;
        if (ownerId != null && level() instanceof ServerLevel server && server.getEntity(ownerId) instanceof LivingEntity living)
            owner = living;
        else owner = null;
        return owner;
    }

    @Override public void tick() {
        super.tick();
        if (level().isClientSide) {
            if (tickCount % 2 == 0) {
                double angle = random.nextDouble() * Math.PI * 2;
                level().addParticle(isSoul() ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.FLAME,
                        getX() + Math.cos(angle) * blastRadius(), getY() + 0.08, getZ() + Math.sin(angle) * blastRadius(), 0, 0.04, 0);
            }
            return;
        }
        int ticks = remainingTicks() - 1;
        entityData.set(REMAINING, ticks);
        if (ticks <= 0 && level() instanceof ServerLevel server) {
            blast(server, this, getOwner(), position(), blastRadius(), damage, isSoul(), server.damageSources().explosion(this, getOwner()));
            discard();
        }
    }

    public static void blast(ServerLevel level, Entity source, @Nullable LivingEntity owner,
                             Vec3 position, float radius, float damage, boolean soul) {
        // Ignis's projectile hit uses mobProjectile. Keep normal armor mitigation,
        // instead of turning the weapon's entire area damage into armor-bypassing magic.
        float healthLost = blast(level, source, owner, position, radius, damage, soul, level.damageSources().mobProjectile(source, owner));
        // Only the projectile's initial blast steals health. The delayed sigil calls
        // the damage-only overload directly.
        if (owner != null && owner.isAlive() && healthLost > 0)
            owner.heal(healthLost * GWREConfig.GATLING.Ignitium.skillLifestealPercent.get().floatValue() / 100F);
    }

    private static float blast(ServerLevel level, Entity source, @Nullable LivingEntity owner,
                              Vec3 position, float radius, float damage, boolean soul, DamageSource damageSource) {
        var bounds = new AABB(position, position).inflate(radius);
        float healthLost = 0;
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, bounds)) {
            if (target == owner || !target.isAlive() || target.isSpectator()
                    || (owner != null && (owner.isAlliedTo(target) || target.isPassengerOfSameVehicle(owner)))
                    || target.distanceToSqr(position) > radius * radius) continue;
            float attenuation = 1.0F - 0.35F * (float) (Math.sqrt(target.distanceToSqr(position)) / radius);
            float healthBefore = target.getHealth();
            if (target.hurt(damageSource, damage * attenuation)) {
                // Measure after armor, resistance, immunity and death clamping. Misses,
                // absorption and overkill must not generate health for the shooter.
                healthLost += Math.max(0, healthBefore - target.getHealth());
                Vec3 push = target.position().subtract(position);
                target.knockback(0.8D, -push.x, -push.z);
                target.setSecondsOnFire(3);
            }
        }
        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, position.x, position.y + 0.3, position.z, 1, 0, 0, 0, 0);
        level.sendParticles(soul ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.FLAME, position.x, position.y + 0.4, position.z, 45, radius * 0.5, 0.5, radius * 0.5, 0.08);
        // IgnisExplosion.finalizeExplosion uses GENERIC_EXPLODE, not the boss's melee impact.
        level.playSound(null, position.x, position.y, position.z, SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS,
                1.0F, (1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.2F) * 0.7F);
        return healthLost;
    }

    @Override protected void readAdditionalSaveData(CompoundTag tag) {
        owner = null;
        ownerId = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
        entityData.set(SOUL, tag.getBoolean("Soul"));
        entityData.set(REMAINING, tag.contains("Remaining") ? Math.max(1, tag.getInt("Remaining")) : DELAY);
        if (tag.contains("Radius")) entityData.set(BLAST_RADIUS, Math.max(0.1F, tag.getFloat("Radius")));
        damage = tag.contains("Damage") ? Math.max(0, tag.getFloat("Damage"))
                : (isSoul() ? GWREConfig.GATLING.Ignitium.soulSigilDamage : GWREConfig.GATLING.Ignitium.sigilDamage).get().floatValue();
    }
    @Override protected void addAdditionalSaveData(CompoundTag tag) {
        if (ownerId != null) tag.putUUID("Owner", ownerId);
        tag.putBoolean("Soul", isSoul());
        tag.putInt("Remaining", remainingTicks());
        tag.putFloat("Radius", blastRadius());
        tag.putFloat("Damage", damage);
    }
    @Override public Packet<ClientGamePacketListener> getAddEntityPacket() { return NetworkHooks.getEntitySpawningPacket(this); }
}
