package juitar.gwrexpansions.entity.cataclysm;

import juitar.gwrexpansions.item.cataclysm.TidalGunItem;
import com.github.L_Ender.cataclysm.init.ModParticle;
import com.github.L_Ender.cataclysm.init.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.UUID;

public class TidalRiftEntity extends Entity {
    protected static final EntityDataAccessor<Integer> LIFESPAN = SynchedEntityData.defineId(TidalRiftEntity.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Integer> STAGE = SynchedEntityData.defineId(TidalRiftEntity.class, EntityDataSerializers.INT);

    private static final String OWNER_TAG = "Owner";
    private static final String RADIUS_TAG = "Radius";
    private static final String DAMAGE_TAG = "Damage";
    private static final String PULL_TAG = "Pull";
    private static final String INTERVAL_TAG = "Interval";

    @Nullable
    private LivingEntity owner;
    @Nullable
    private UUID ownerUuid;
    private double radius = TidalGunItem.tidalConfig().riftRadius.get();
    private float damage = TidalGunItem.tidalConfig().riftDamage.get().floatValue();
    private double pullStrength = TidalGunItem.tidalConfig().riftPullStrength.get();
    private int damageInterval = TidalGunItem.tidalConfig().riftDamageIntervalTicks.get();
    private boolean opened;
    private boolean closeSound;
    private boolean finalPulse;
    private int ambientSoundTime;

    public TidalRiftEntity(EntityType<? extends TidalRiftEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public void configure(int duration, double radius, float damage, double pullStrength, int damageInterval) {
        setLifespan(Math.max(1, duration));
        setStage(0);
        this.radius = Math.max(0.0D, radius);
        this.damage = Math.max(0.0F, damage);
        this.pullStrength = Math.max(0.0D, pullStrength);
        this.damageInterval = Math.max(1, damageInterval);
    }

    public void setOwner(LivingEntity owner) {
        this.owner = owner;
        this.ownerUuid = owner.getUUID();
    }

    @Override
    public void tick() {
        super.tick();

        if (!opened) {
            playSound(ModSounds.BLACK_HOLE_OPENING.get(), 0.7F, 1.0F);
            opened = true;
        }

        growStage();
        tickAmbientSound();
        spawnInwardParticles();
        if (!level().isClientSide) {
            pullAndDamageEntities();
            damageCenterTargets();
            if (getLifespan() <= 100 && !closeSound) {
                playSound(ModSounds.BLACK_HOLE_CLOSING.get(), 0.7F, 1.0F);
                closeSound = true;
            }
        }

        setLifespan(getLifespan() - 1);
        if (getLifespan() <= 100 && tickCount % 40 == 0) {
            setStage(getStage() - 1);
        }
        if (getLifespan() <= 0 || getStage() <= 0 && getLifespan() <= 100) {
            doFinalPulse();
            discard();
        }
    }

    private void growStage() {
        if (getLifespan() > 100 && getStage() < 4 && tickCount % 20 == 0) {
            setStage(getStage() + 1);
        }
    }

    private void tickAmbientSound() {
        if (random.nextInt(3000) < ambientSoundTime++) {
            ambientSoundTime = -80;
            playSound(ModSounds.BLACK_HOLE_LOOP.get(), 0.7F, 1.0F + random.nextFloat() * 0.2F);
        }
    }

    private void spawnInwardParticles() {
        if (!level().isClientSide || getStage() <= 0 || tickCount % 2 != 0) {
            return;
        }

        int count = 2 + getStage();
        double activeRadius = Math.max(2.5D, radius * (0.35D + getStage() * 0.12D));
        for (int i = 0; i < count; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double distance = activeRadius * (0.55D + random.nextDouble() * 0.45D);
            double yOffset = (random.nextDouble() - 0.5D) * Math.max(2.0D, activeRadius * 0.35D);
            Vec3 spawn = position().add(Math.cos(angle) * distance, yOffset, Math.sin(angle) * distance);
            Vec3 velocity = position().subtract(spawn).normalize().scale(0.08D + random.nextDouble() * 0.05D);
            level().addParticle(ParticleTypes.REVERSE_PORTAL, spawn.x, spawn.y, spawn.z, velocity.x, velocity.y, velocity.z);
        }
    }

    private void pullAndDamageEntities() {
        Vec3 center = position();
        double activeRadius = Math.max(1.0D, radius * getVisualScale());
        AABB area = new AABB(center, center).inflate(Math.max(activeRadius, 30.0D * getVisualScale()));
        Entity owner = getOwnerEntity();
        for (LivingEntity target : level().getEntitiesOfClass(LivingEntity.class, area)) {
            if (shouldIgnore(target, owner)) {
                continue;
            }

            Vec3 targetCenter = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D);
            Vec3 toCenter = center.subtract(targetCenter);
            double distance = toCenter.length();
            if (distance <= 0.05D) {
                continue;
            }

            double stage = getRiftStage();
            double factor = distance <= activeRadius ? 0.045D : 0.015D;
            Vec3 pull = toCenter.normalize().scale(stage * factor * pullStrength);
            target.setDeltaMovement(target.getDeltaMovement().add(pull));
            target.hurtMarked = true;
        }
    }

    private void damageCenterTargets() {
        if (damage <= 0.0F || tickCount % damageInterval != 0) {
            return;
        }
        Entity owner = getOwnerEntity();
        AABB area = getBoundingBox().inflate(1.0D + getVisualScale() * 1.75D);
        for (LivingEntity target : level().getEntitiesOfClass(LivingEntity.class, area)) {
            if (shouldIgnore(target, owner)) {
                continue;
            }
            DamageSource source = owner == null
                    ? level().damageSources().magic()
                    : level().damageSources().indirectMagic(this, owner);
            float stageDamage = (float) (damage * (0.75D + getRiftStage() * 0.15D));
            target.hurt(source, stageDamage);
        }
    }

    private void doFinalPulse() {
        if (finalPulse) {
            return;
        }
        finalPulse = true;
        if (level().isClientSide) {
            level().addParticle(ModParticle.SHOCK_WAVE.get(), getX(), getY(), getZ(), 0.0D, 0.0D, 0.0D);
            return;
        }
        Entity owner = getOwnerEntity();
        level().explode(owner == null ? this : owner, getX(), getY(), getZ(), 4.0F, false, Level.ExplosionInteraction.NONE);
    }

    private boolean shouldIgnore(LivingEntity target, @Nullable Entity owner) {
        if (target.isSpectator() || !target.isAlive()) {
            return true;
        }
        if (owner == null) {
            return false;
        }
        if (target == owner || target.isAlliedTo(owner) || owner.isAlliedTo(target)) {
            return true;
        }
        return target.getVehicle() == owner || owner.getVehicle() == target
                || (target.getVehicle() != null && target.getVehicle() == owner.getVehicle());
    }

    public float getVisualScale() {
        return getStage() >= 4 ? 1.0F : Math.max(0.2F, getStage() / 4.0F);
    }

    public int getRiftStage() {
        return Math.max(1, getStage());
    }

    public int getLifespan() {
        return entityData.get(LIFESPAN);
    }

    public void setLifespan(int lifespan) {
        entityData.set(LIFESPAN, lifespan);
    }

    public int getStage() {
        return entityData.get(STAGE);
    }

    public void setStage(int stage) {
        entityData.set(STAGE, stage);
    }

    @Nullable
    private Entity getOwnerEntity() {
        if (owner != null && owner.isAlive()) {
            return owner;
        }
        if (ownerUuid != null && level() instanceof ServerLevel serverLevel) {
            Entity entity = serverLevel.getEntity(ownerUuid);
            if (entity instanceof LivingEntity living) {
                owner = living;
                return living;
            }
        }
        return null;
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(LIFESPAN, 300);
        entityData.define(STAGE, 0);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID(OWNER_TAG)) {
            ownerUuid = tag.getUUID(OWNER_TAG);
        }
        setLifespan(tag.getInt("Lifespan"));
        setStage(tag.getInt("Stage"));
        radius = tag.getDouble(RADIUS_TAG);
        damage = tag.getFloat(DAMAGE_TAG);
        pullStrength = tag.getDouble(PULL_TAG);
        damageInterval = Math.max(1, tag.getInt(INTERVAL_TAG));
        opened = tag.getBoolean("Opened");
        closeSound = tag.getBoolean("CloseSound");
        finalPulse = tag.getBoolean("FinalPulse");
        ambientSoundTime = tag.getInt("AmbientSoundTime");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (ownerUuid != null) {
            tag.putUUID(OWNER_TAG, ownerUuid);
        }
        tag.putInt("Lifespan", getLifespan());
        tag.putInt("Stage", getStage());
        tag.putDouble(RADIUS_TAG, radius);
        tag.putFloat(DAMAGE_TAG, damage);
        tag.putDouble(PULL_TAG, pullStrength);
        tag.putInt(INTERVAL_TAG, damageInterval);
        tag.putBoolean("Opened", opened);
        tag.putBoolean("CloseSound", closeSound);
        tag.putBoolean("FinalPulse", finalPulse);
        tag.putInt("AmbientSoundTime", ambientSoundTime);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
