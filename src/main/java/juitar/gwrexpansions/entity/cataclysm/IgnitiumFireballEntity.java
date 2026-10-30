package juitar.gwrexpansions.entity.cataclysm;

import juitar.gwrexpansions.config.GWREConfig;
import juitar.gwrexpansions.event.IgnitiumSkillEvents;
import juitar.gwrexpansions.registry.GWREEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.minecraftforge.event.ForgeEventFactory;

/** A ballistic projectile: constant horizontal velocity and downward acceleration, no fireball propulsion. */
public class IgnitiumFireballEntity extends Projectile {
    private static final EntityDataAccessor<Boolean> ABYSS = SynchedEntityData.defineId(IgnitiumFireballEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> SOUL = SynchedEntityData.defineId(IgnitiumFireballEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> GRAVITY = SynchedEntityData.defineId(IgnitiumFireballEntity.class, EntityDataSerializers.FLOAT);

    public IgnitiumFireballEntity(EntityType<? extends IgnitiumFireballEntity> type, Level level) {
        super(type, level);
    }

    public IgnitiumFireballEntity(ServerLevel level, LivingEntity owner, boolean abyss, boolean soul) {
        this(GWREEntities.IGNITIUM_FIREBALL.get(), level);
        setOwner(owner);
        entityData.set(ABYSS, abyss);
        entityData.set(SOUL, soul);
    }

    @Override protected void defineSynchedData() {
        entityData.define(ABYSS, false);
        entityData.define(SOUL, false);
        entityData.define(GRAVITY, 0.04F);
    }

    public boolean isAbyss() { return entityData.get(ABYSS); }
    public boolean isSoul() { return entityData.get(SOUL); }
    public float gravity() { return entityData.get(GRAVITY); }
    public void setGravity(double gravity) { entityData.set(GRAVITY, (float) Mth.clamp(gravity, 0.001, 0.5)); }

    public void launch(LivingEntity shooter, double speed, double gravity) {
        Vec3 direction = Vec3.directionFromRotation(shooter.getXRot(), shooter.getYRot());
        setPos(shooter.getEyePosition().add(direction.scale(0.85)));
        shoot(direction.x, direction.y, direction.z, (float) speed, 0);
        setGravity(gravity);
    }

    @Override public void tick() {
        super.tick();
        if (!level().isClientSide && tickCount > 160) { discard(); return; }
        HitResult hit = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        if (hit.getType() != HitResult.Type.MISS && !ForgeEventFactory.onProjectileImpact(this, hit)) onHit(hit);
        if (isRemoved()) return;
        setPos(position().add(getDeltaMovement()));
        setDeltaMovement(getDeltaMovement().add(0, -gravity(), 0));
        ProjectileUtil.rotateTowardsMovement(this, 0.2F);
        if (level().isClientSide) {
            level().addParticle(isSoul() ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.FLAME,
                    getX(), getY(), getZ(), 0, 0, 0);
            if (tickCount % 2 == 0) level().addParticle(ParticleTypes.SMOKE, getX(), getY(), getZ(), 0, 0.02, 0);
        }
    }

    @Override protected boolean canHitEntity(Entity entity) {
        Entity owner = getOwner();
        return !(entity instanceof IgnitiumFireballEntity) && entity != owner
                && (owner == null || !owner.isAlliedTo(entity)) && super.canHitEntity(entity);
    }

    @Override protected void onHit(HitResult hit) {
        if (!(level() instanceof ServerLevel server) || isRemoved()) return;
        LivingEntity owner = getOwner() instanceof LivingEntity living ? living : null;
        if (isAbyss()) {
            IgnitiumSkillEvents.abyssImpact(server, this, owner, hit.getLocation(), isSoul());
        } else {
            var config = GWREConfig.GATLING.Ignitium;
            IgnitiumFlameSigilEntity.blast(server, this, owner, hit.getLocation(), config.lightRadius.get().floatValue(),
                    (isSoul() ? config.soulLightDamage : config.lightDamage).get().floatValue(), isSoul());
        }
        discard();
    }

    @Override protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Abyss", isAbyss());
        tag.putBoolean("Soul", isSoul());
        tag.putFloat("Gravity", gravity());
        tag.putInt("FlightTicks", tickCount);
    }

    @Override protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(ABYSS, tag.getBoolean("Abyss"));
        entityData.set(SOUL, tag.getBoolean("Soul"));
        if (tag.contains("Gravity")) setGravity(tag.getFloat("Gravity"));
        tickCount = tag.getInt("FlightTicks");
    }
}
