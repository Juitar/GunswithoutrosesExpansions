package juitar.gwrexpansions.entity.cataclysm;

import com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.The_Leviathan.Portal_Abyss_Blast_Entity;
import com.github.L_Ender.cataclysm.init.ModEffect;
import com.github.L_Ender.cataclysm.init.ModSounds;
import com.github.L_Ender.cataclysm.util.CMDamageTypes;
import juitar.gwrexpansions.advancement.TidalPortalKilledEndermanTrigger;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.sounds.SoundSource;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TidalPortalBeamEntity extends Portal_Abyss_Blast_Entity {
    private static final double RANGE = 50.0D;
    private static final double RANGE_SQR = RANGE * RANGE;
    private static final String PRIORITY_TARGET_TAG = "PriorityTarget";
    private static final EntityDataAccessor<Integer> PRIORITY_TARGET_ID = SynchedEntityData
            .defineId(TidalPortalBeamEntity.class, EntityDataSerializers.INT);

    @Nullable
    private UUID priorityTargetUuid;

    public TidalPortalBeamEntity(EntityType<? extends Portal_Abyss_Blast_Entity> type, Level level) {
        super(type, level);
        this.noCulling = true;
    }

    public void configure(LivingEntity owner, Vec3 direction, int duration, float damage, float hpDamage) {
        configure(owner, direction, duration, damage, hpDamage, null);
    }

    public void configure(LivingEntity owner, Vec3 direction, int duration, float damage, float hpDamage,
            @Nullable LivingEntity priorityTarget) {
        this.caster = owner;
        setPriorityTarget(priorityTarget);
        Vec3 beamDirection = getAutoTargetPoint().subtract(position());
        if (beamDirection.lengthSqr() < 0.001D) {
            Vec3 aimedPoint = getCasterAimedPoint(owner);
            beamDirection = aimedPoint.subtract(position());
        }
        if (beamDirection.lengthSqr() < 0.001D) {
            beamDirection = direction.lengthSqr() < 0.001D ? owner.getViewVector(1.0F) : direction;
        }
        configureFromPortal(beamDirection, duration, damage, hpDamage);
        if (!level().isClientSide) {
            setCasterID(owner.getId());
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(PRIORITY_TARGET_ID, -1);
    }

    public void configureFromPortal(Vec3 direction, int duration, float damage, float hpDamage) {
        Vec3 beamDirection = direction.lengthSqr() < 0.001D ? new Vec3(1.0D, 0.0D, 0.0D) : direction.normalize();
        setYaw((float) Math.atan2(beamDirection.z, beamDirection.x));
        setPitch((float) Math.asin(Mth.clamp(beamDirection.y, -1.0D, 1.0D)));
        setDuration(Math.max(1, duration));
        setBeamDirection(90.0F);
        setDamage(Math.max(0.0F, damage));
        setHpDamage(Math.max(0.0F, hpDamage));
        calculateEndPosSafe();
    }

    @Override
    public void tick() {
        this.tickCount++;
        if (this.tickCount == 1 && level().isClientSide) {
            Entity entity = level().getEntity(getCasterID());
            if (entity instanceof LivingEntity living) {
                this.caster = living;
            }
        }

        updateDirectionFromAutoTarget();
        this.prevCollidePosX = this.collidePosX;
        this.prevCollidePosY = this.collidePosY;
        this.prevCollidePosZ = this.collidePosZ;
        this.prevYaw = this.renderYaw;
        this.prevPitch = this.renderPitch;
        this.renderYaw = getYaw();
        this.renderPitch = getPitch();
        this.xo = getX();
        this.yo = getY();
        this.zo = getZ();

        if (!this.on && this.appear.getTimer() == 0) {
            discard();
            return;
        }

        if (this.on && this.tickCount > 20) {
            this.appear.increaseTimer();
        } else {
            this.appear.decreaseTimer();
        }

        if (this.tickCount == 20) {
            level().playSound(null, this, ModSounds.PORTAL_ABYSS_BLAST.get(), SoundSource.HOSTILE, 0.5F, 1.0F);
        }

        if (this.caster != null && !this.caster.isAlive()) {
            discard();
            return;
        }

        if (this.tickCount > 20) {
            calculateEndPosSafe();
            Vec3 start = position();
            Vec3 end = new Vec3(this.endPosX, this.endPosY, this.endPosZ);
            List<LivingEntity> targets = collectBeamTargets(start, end);

            if (level().isClientSide && this.blockSide != null) {
                spawnOriginalImpactParticles(3);
            }

            if (!level().isClientSide) {
                for (LivingEntity target : targets) {
                    hurtTarget(target);
                }
            }
        }

        if (this.tickCount - 20 > getDuration()) {
            this.on = false;
        }
    }

    private void updateDirectionFromAutoTarget() {
        if (this.caster == null || !this.caster.isAlive()) {
            return;
        }

        Vec3 direction = getAutoTargetPoint().subtract(position());
        if (direction.lengthSqr() < 0.001D) {
            Vec3 aimedPoint = getCasterAimedPoint(this.caster);
            direction = aimedPoint.subtract(position());
        }
        if (direction.lengthSqr() < 0.001D) {
            direction = this.caster.getViewVector(1.0F);
        }
        setDirection(direction);
    }

    private Vec3 getAutoTargetPoint() {
        LivingEntity target = findPriorityTarget();
        if (target == null) {
            target = findNearestEnemy();
        }
        if (target == null) {
            return position();
        }
        return target.position().add(0.0D, target.getBbHeight() * 0.55D, 0.0D);
    }

    @Nullable
    private LivingEntity findPriorityTarget() {
        int targetId = this.entityData.get(PRIORITY_TARGET_ID);
        if (targetId >= 0) {
            Entity entity = level().getEntity(targetId);
            if (entity instanceof LivingEntity target && isPriorityTarget(target)) {
                return target;
            }
        }

        if (this.priorityTargetUuid != null && level() instanceof ServerLevel serverLevel) {
            Entity entity = serverLevel.getEntity(this.priorityTargetUuid);
            if (entity instanceof LivingEntity target && isPriorityTarget(target)) {
                setPriorityTarget(target);
                return target;
            }
        }
        return null;
    }

    private void setPriorityTarget(@Nullable LivingEntity target) {
        this.priorityTargetUuid = target == null ? null : target.getUUID();
        this.entityData.set(PRIORITY_TARGET_ID, target == null ? -1 : target.getId());
    }

    @Nullable
    private LivingEntity findNearestEnemy() {
        AABB searchBox = getBoundingBox().inflate(RANGE);
        LivingEntity nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (LivingEntity target : level().getEntitiesOfClass(LivingEntity.class, searchBox)) {
            if (!isEnemyTarget(target)) {
                continue;
            }

            double distance = target.distanceToSqr(this);
            if (distance < nearestDistance) {
                nearest = target;
                nearestDistance = distance;
            }
        }
        return nearest;
    }

    private Vec3 getCasterAimedPoint(LivingEntity caster) {
        HitResult cameraHit = caster.pick(RANGE, 1.0F, true);
        if (cameraHit != null && cameraHit.getType() != HitResult.Type.MISS) {
            return cameraHit.getLocation();
        }

        Vec3 eye = caster.getEyePosition(1.0F);
        return eye.add(caster.getViewVector(1.0F).scale(RANGE));
    }

    private void setDirection(Vec3 direction) {
        Vec3 beamDirection = direction.lengthSqr() < 0.001D ? new Vec3(1.0D, 0.0D, 0.0D) : direction.normalize();
        setYaw((float) Math.atan2(beamDirection.z, beamDirection.x));
        setPitch((float) Math.asin(Mth.clamp(beamDirection.y, -1.0D, 1.0D)));
    }

    private void calculateEndPosSafe() {
        float yaw = level().isClientSide ? this.renderYaw : getYaw();
        float pitch = level().isClientSide ? this.renderPitch : getPitch();
        this.endPosX = getX() + RANGE * Math.cos(yaw) * Math.cos(pitch);
        this.endPosZ = getZ() + RANGE * Math.sin(yaw) * Math.cos(pitch);
        this.endPosY = getY() + RANGE * Math.sin(pitch);
    }

    private List<LivingEntity> collectBeamTargets(Vec3 start, Vec3 end) {
        BlockHitResult blockHit = level().clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        if (blockHit.getType() == HitResult.Type.MISS) {
            this.collidePosX = this.endPosX;
            this.collidePosY = this.endPosY;
            this.collidePosZ = this.endPosZ;
            this.blockSide = null;
        } else {
            Vec3 hitLocation = blockHit.getLocation();
            this.collidePosX = hitLocation.x;
            this.collidePosY = hitLocation.y;
            this.collidePosZ = hitLocation.z;
            this.blockSide = blockHit.getDirection();
            end = hitLocation;
        }

        AABB searchBox = new AABB(
                Math.min(getX(), this.collidePosX), Math.min(getY(), this.collidePosY), Math.min(getZ(), this.collidePosZ),
                Math.max(getX(), this.collidePosX), Math.max(getY(), this.collidePosY), Math.max(getZ(), this.collidePosZ))
                .inflate(1.0D, 1.0D, 1.0D);
        List<LivingEntity> hits = new ArrayList<>();
        for (LivingEntity target : level().getEntitiesOfClass(LivingEntity.class, searchBox)) {
            if (shouldIgnore(target)) {
                continue;
            }

            float radius = target.getPickRadius() + 1.3F;
            AABB targetBox = target.getBoundingBox().inflate(radius, radius, radius);
            if (targetBox.contains(start) || targetBox.clip(start, end).isPresent()) {
                hits.add(target);
            }
        }
        return hits;
    }

    private boolean shouldIgnore(LivingEntity target) {
        return !isEnemyTarget(target) && !isPriorityTarget(target);
    }

    private boolean isPriorityTarget(LivingEntity target) {
        return isValidOwnedTarget(target) && target.distanceToSqr(this) <= RANGE_SQR;
    }

    private boolean isEnemyTarget(LivingEntity target) {
        return target instanceof Enemy && isValidOwnedTarget(target);
    }

    private boolean isValidOwnedTarget(LivingEntity target) {
        if (!target.isAlive() || target.isSpectator() || target == this.caster) {
            return false;
        }
        if (this.caster == null) {
            return false;
        }
        return !this.caster.isAlliedTo(target) && !target.isAlliedTo(this.caster)
                && target.getVehicle() != this.caster && this.caster.getVehicle() != target
                && (target.getVehicle() == null || target.getVehicle() != this.caster.getVehicle());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID(PRIORITY_TARGET_TAG)) {
            this.priorityTargetUuid = tag.getUUID(PRIORITY_TARGET_TAG);
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.priorityTargetUuid != null) {
            tag.putUUID(PRIORITY_TARGET_TAG, this.priorityTargetUuid);
        }
    }

    private void hurtTarget(LivingEntity target) {
        if (this.caster == null) {
            return;
        }

        float healthDamage = (float) Math.min(getDamage(), target.getMaxHealth() * getHpDamage() * 0.01D);
        boolean hurt = target.hurt(CMDamageTypes.causeDeathLaserDamage(this, this.caster), getDamage() + healthDamage);
        if (!hurt) {
            return;
        }

        if (!target.isAlive() && target instanceof EnderMan && this.caster instanceof ServerPlayer player) {
            TidalPortalKilledEndermanTrigger.onTidalPortalKilledEnderman(player);
        }

        MobEffect abyssalBurn = ModEffect.EFFECTABYSSAL_BURN.get();
        MobEffectInstance current = target.getEffect(abyssalBurn);
        int amplifier = 1;
        if (current != null) {
            amplifier += current.getAmplifier();
            target.removeEffectNoUpdate(abyssalBurn);
        } else {
            amplifier--;
        }
        amplifier = Mth.clamp(amplifier, 0, 3);
        target.addEffect(new MobEffectInstance(abyssalBurn, 160, amplifier, false, true, true));
    }

    private void spawnOriginalImpactParticles(int count) {
        for (int i = 0; i < count; i++) {
            float angle = random.nextFloat() * 2.0F * (float) Math.PI;
            float speedY = random.nextFloat() * 0.08F;
            float xSpeed = Mth.cos(angle);
            float zSpeed = Mth.sin(angle);
            level().addParticle(ParticleTypes.ELECTRIC_SPARK,
                    this.collidePosX, this.collidePosY + 0.1D, this.collidePosZ,
                    xSpeed, speedY, zSpeed);
        }
    }

    @Override
    public PushReaction getPistonPushReaction() {
        return PushReaction.IGNORE;
    }
}
