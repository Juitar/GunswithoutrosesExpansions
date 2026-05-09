package juitar.gwrexpansions.entity.alexscaves;

import juitar.gwrexpansions.registry.GWREEntities;
import lykrast.gunswithoutroses.entity.BulletEntity;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MagneticBulletEntity extends BulletEntity {
    private static final int ENTITY_ORBIT_TICKS = 30;
    private static final double ORBIT_COLLISION_INFLATE = 0.45D;
    private static final double PIN_SEEK_ACCELERATION = 0.38D;
    private static final double PIN_SEEK_RETAIN = 0.72D;
    private static final double RELEASE_SPEED = 2.0D;
    private static final double TARGET_RELEASE_SPEED = 2.65D;
    private static final EntityDataAccessor<Boolean> ORBITING = SynchedEntityData.defineId(MagneticBulletEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> PIN_ID = SynchedEntityData.defineId(MagneticBulletEntity.class, EntityDataSerializers.INT);

    @Nullable
    private MagneticPinEntity pin;
    @Nullable
    private BulletEntity effectDelegate;
    private boolean released;
    private int orbitTicks;
    private int targetReleaseEntityId = -1;
    private int targetReleaseTicks;

    public MagneticBulletEntity(EntityType<? extends BulletEntity> type, Level level) {
        super(type, level);
    }

    public MagneticBulletEntity(Level level, LivingEntity shooter) {
        super(GWREEntities.MAGNETIC_BULLET.get(), shooter, level);
        setOwner(shooter);
    }

    public void setEffectDelegate(BulletEntity effectDelegate) {
        this.effectDelegate = effectDelegate;
        syncEffectDelegate();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(ORBITING, false);
        entityData.define(PIN_ID, -1);
    }

    @Override
    public void tick() {
        pin = resolvePin();

        if (isOrbitingSynced()) {
            tickOrbiting();
            return;
        }

        if (!level().isClientSide && targetReleaseTicks > 0 && handleTargetReleaseCollision()) {
            return;
        }

        if (!released && !level().isClientSide) {
            MagneticPinEntity activePin = pin != null && pin.canAttract(getOwner()) ? pin : findActivePin();
            if (activePin != null) {
                pin = activePin;
                setPinId(activePin.getId());
                Vec3 toPin = activePin.position().subtract(position());
                if (toPin.lengthSqr() < 2.25D) {
                    if (activePin.tryAddOrbitingBullet(this)) {
                        setOrbitingState(activePin, true);
                        orbitTicks = 0;
                        noPhysics = true;
                        setDeltaMovement(Vec3.ZERO);
                        tickOrbiting();
                        return;
                    }
                    released = true;
                } else {
                    setDeltaMovement(getDeltaMovement().scale(PIN_SEEK_RETAIN).add(toPin.normalize().scale(PIN_SEEK_ACCELERATION)));
                }
            }
        }

        super.tick();
    }

    @Override
    protected void move() {
        Vec3 movement = getDeltaMovement();
        double nextX = getX() + movement.x;
        double nextY = getY() + movement.y;
        double nextZ = getZ() + movement.z;

        ProjectileUtil.rotateTowardsMovement(this, 0.2F);
        if (isInWater()) {
            for (int i = 0; i < 4; i++) {
                level().addParticle(ParticleTypes.BUBBLE,
                        nextX - movement.x * 0.25D,
                        nextY - movement.y * 0.25D,
                        nextZ - movement.z * 0.25D,
                        movement.x, movement.y, movement.z);
            }
            setDeltaMovement(movement.scale(getWaterInertia()));
        }

        if (level().isClientSide && !isOrbitingSynced()) {
            spawnFlightTrail(new Vec3(nextX, nextY, nextZ), movement);
        }

        setPos(nextX, nextY, nextZ);
    }

    private void tickOrbiting() {
        baseTick();
        noPhysics = true;

        if (pin == null || pin.isRemoved()) {
            if (!level().isClientSide) {
                releaseFromPin(position(), 0, 1);
            }
            return;
        }

        orbitTicks++;
        Vec3 center = pin.position();
        Vec3 axis = pin.getMagneticAxis();
        Vec3 orbitBasisA = getOrbitBasisA(axis);
        Vec3 orbitBasisB = axis.cross(orbitBasisA).normalize();
        double angle = (tickCount * 0.35D) + getOrbitPhaseOffset();
        double radius = pin.isAttachedToBlock() ? 1.35D : 0.9D;
        double axisOffset = Math.sin(angle * 1.7D) * 0.22D;
        Vec3 radial = orbitBasisA.scale(Math.cos(angle) * radius).add(orbitBasisB.scale(Math.sin(angle) * radius));
        Vec3 orbitPos = center.add(radial).add(axis.scale(axisOffset));

        setPos(orbitPos.x, orbitPos.y, orbitPos.z);
        setDeltaMovement(Vec3.ZERO);
        spawnOrbitParticle(center, axis);
        if (!level().isClientSide) {
            if (handleOrbitCollision()) {
                return;
            }
        }

        if (!level().isClientSide && pin.isAttachedToEntity() && orbitTicks >= ENTITY_ORBIT_TICKS) {
            Entity target = pin.getAttachedEntity();
            int index = pin.getOrbitIndex(this);
            int total = pin.getOrbitCount();
            pin.removeOrbitingBullet(this);
            if (target instanceof LivingEntity livingTarget && livingTarget.isAlive()) {
                Vec3 targetPoint = pin.getTargetPoint();
                Vec3 direction = targetPoint.subtract(position()).normalize();
                released = true;
                targetReleaseEntityId = target.getId();
                targetReleaseTicks = 8;
                setOrbitingState(null, false);
                noPhysics = false;
                setDeltaMovement(direction.scale(TARGET_RELEASE_SPEED));
            } else {
                releaseFromPin(center, index, total);
            }
        }
    }

    public void releaseFromPin(Vec3 center, int index, int total) {
        if (pin != null) {
            pin.removeOrbitingBullet(this);
        }
        double angle = Math.PI * 2.0D * index / Math.max(1, total);
        Vec3 outward = position().subtract(center);
        if (outward.lengthSqr() < 0.01D) {
            outward = new Vec3(Math.cos(angle), 0.08D, Math.sin(angle));
        }
        Vec3 direction = new Vec3(outward.x, Math.max(-0.15D, outward.y), outward.z).normalize();
        setDeltaMovement(direction.scale(RELEASE_SPEED));
        released = true;
        setOrbitingState(null, false);
        noPhysics = false;
        pin = null;
    }

    @Nullable
    private MagneticPinEntity findActivePin() {
        Entity owner = getOwner();
        if (owner == null) {
            return null;
        }
        return level().getEntitiesOfClass(MagneticPinEntity.class, owner.getBoundingBox().inflate(64.0D),
                candidate -> candidate.canAttract(owner)).stream()
                .min((a, b) -> Double.compare(a.distanceToSqr(this), b.distanceToSqr(this)))
                .orElse(null);
    }

    @Override
    protected ParticleOptions getTrailParticle() {
        return getFlowParticle();
    }

    private void spawnOrbitParticle(Vec3 center, Vec3 axis) {
        if (level().isClientSide) {
            Vec3 radial = position().subtract(center);
            Vec3 tangent = radial.lengthSqr() > 0.0001D
                    ? axis.cross(radial).normalize().scale(0.03D)
                    : Vec3.ZERO;
            Vec3 trailStart = position().add(tangent);
            Vec3 trailEnd = trailStart.subtract(tangent.scale(5.0D)).add(randomJitter(0.08D, 0.04D));
            level().addParticle(getOrbitTailParticle(), trailStart.x, trailStart.y, trailStart.z, trailEnd.x, trailEnd.y, trailEnd.z);
            level().addParticle(getOrbitAxisParticle(), getX(), getY(), getZ(), center.x, center.y, center.z);
        }
    }

    private void spawnFlightTrail(Vec3 currentPos, Vec3 movement) {
        if (movement.lengthSqr() < 1.0E-4D) {
            return;
        }
        Vec3 start = currentPos.add(0.0D, 0.05D, 0.0D);
        Vec3 end = start.add(movement.scale(0.8D)).add(randomJitter(0.18D, 0.18D));
        level().addParticle(getFlightTrailParticle(), start.x, start.y, start.z, end.x, end.y, end.z);
    }

    private Vec3 randomJitter(double horizontal, double vertical) {
        return new Vec3(
                (random.nextDouble() - 0.5D) * horizontal,
                (random.nextDouble() - 0.5D) * vertical,
                (random.nextDouble() - 0.5D) * horizontal
        );
    }

    private Vec3 getOrbitBasisA(Vec3 axis) {
        Vec3 reference = Math.abs(axis.y) < 0.9D ? new Vec3(0.0D, 1.0D, 0.0D) : new Vec3(1.0D, 0.0D, 0.0D);
        Vec3 basis = axis.cross(reference);
        return basis.lengthSqr() > 1.0E-4D ? basis.normalize() : new Vec3(1.0D, 0.0D, 0.0D);
    }

    private boolean isOrbitingSynced() {
        return entityData.get(ORBITING);
    }

    private void setOrbitingState(@Nullable MagneticPinEntity activePin, boolean orbiting) {
        entityData.set(ORBITING, orbiting);
        entityData.set(PIN_ID, activePin == null ? -1 : activePin.getId());
        pin = activePin;
    }

    private void setPinId(int id) {
        entityData.set(PIN_ID, id);
    }

    @Nullable
    private MagneticPinEntity resolvePin() {
        if (pin != null && !pin.isRemoved()) {
            return pin;
        }
        int id = entityData.get(PIN_ID);
        if (id == -1) {
            return null;
        }
        Entity entity = level().getEntity(id);
        return entity instanceof MagneticPinEntity magneticPin ? magneticPin : null;
    }

    private double getOrbitPhaseOffset() {
        return (getId() * 0.7548776662466927D) % (Math.PI * 2.0D);
    }

    private boolean handleOrbitCollision() {
        Entity owner = getOwner();
        Entity pinnedEntity = pin != null && pin.isAttachedToEntity() ? pin.getAttachedEntity() : null;
        Entity hit = level().getEntities(this, getBoundingBox().inflate(ORBIT_COLLISION_INFLATE),
                candidate -> candidate instanceof LivingEntity
                        && candidate.isAlive()
                        && candidate != owner
                        && candidate != pinnedEntity)
                .stream()
                .findFirst()
                .orElse(null);
        if (hit == null) {
            return false;
        }
        if (pin != null) {
            pin.removeOrbitingBullet(this);
        }
        setOrbitingState(null, false);
        noPhysics = false;
        onHit(new EntityHitResult(hit));
        return true;
    }

    private boolean handleTargetReleaseCollision() {
        Entity target = level().getEntity(targetReleaseEntityId);
        if (!(target instanceof LivingEntity livingTarget) || !livingTarget.isAlive()) {
            targetReleaseEntityId = -1;
            targetReleaseTicks = 0;
            return false;
        }

        Vec3 start = position();
        Vec3 targetPoint = target.getBoundingBox().getCenter();
        Vec3 direction = targetPoint.subtract(start);
        if (direction.lengthSqr() > 1.0E-4D) {
            setDeltaMovement(direction.normalize().scale(TARGET_RELEASE_SPEED));
        }

        Vec3 end = start.add(getDeltaMovement());
        if (target.getBoundingBox().inflate(0.75D).contains(start)
                || target.getBoundingBox().inflate(0.35D).clip(start, end).isPresent()) {
            targetReleaseEntityId = -1;
            targetReleaseTicks = 0;
            onHit(new EntityHitResult(target, targetPoint));
            return true;
        }

        targetReleaseTicks--;
        if (targetReleaseTicks <= 0) {
            targetReleaseEntityId = -1;
        }
        return false;
    }

    @Override
    protected void onHitBlock(BlockHitResult hit) {
        if (!invokeDelegateHit("onHitBlock", BlockHitResult.class, hit)) {
            super.onHitBlock(hit);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (!invokeDelegateHit("onHitEntity", EntityHitResult.class, result)) {
            super.onHitEntity(result);
        }
    }

    private boolean invokeDelegateHit(String methodName, Class<?> parameterType, Object hit) {
        if (effectDelegate == null) {
            return false;
        }
        syncEffectDelegate();
        try {
            Method method = findHitMethod(effectDelegate.getClass(), methodName, parameterType);
            method.invoke(effectDelegate, hit);
            return true;
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | LinkageError ignored) {
            return false;
        }
    }

    private void syncEffectDelegate() {
        if (effectDelegate == null) {
            return;
        }
        effectDelegate.setOwner(getOwner());
        effectDelegate.setItem(getItemRaw());
        effectDelegate.setDamage(getDamage());
        effectDelegate.setWaterInertia(getWaterInertia());
        effectDelegate.setKnockbackStrength(getKnockbackStrength());
        effectDelegate.setHeadshotMultiplier(getHeadshotMultiplier());
        effectDelegate.setDeltaMovement(getDeltaMovement());
        effectDelegate.setRemainingFireTicks(getRemainingFireTicks());
        effectDelegate.moveTo(getX(), getY(), getZ(), getYRot(), getXRot());
    }

    private static Method findHitMethod(Class<?> clazz, String methodName, Class<?> parameterType) throws NoSuchMethodException {
        Class<?> current = clazz;
        while (current != null) {
            try {
                Method method = current.getDeclaredMethod(methodName, parameterType);
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException ignored) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchMethodException(methodName);
    }

    private static ParticleOptions getFlowParticle() {
        SimpleParticleType particle = (SimpleParticleType) ForgeRegistries.PARTICLE_TYPES.getValue(new ResourceLocation("alexscaves", "scarlet_magnetic_flow"));
        return particle == null ? ParticleTypes.ELECTRIC_SPARK : particle;
    }

    private static ParticleOptions getFlightTrailParticle() {
        SimpleParticleType particle = (SimpleParticleType) ForgeRegistries.PARTICLE_TYPES.getValue(new ResourceLocation("alexscaves", "scarlet_shield_lightning"));
        return particle == null ? ParticleTypes.ELECTRIC_SPARK : particle;
    }

    private static ParticleOptions getOrbitTailParticle() {
        SimpleParticleType particle = (SimpleParticleType) ForgeRegistries.PARTICLE_TYPES.getValue(new ResourceLocation("alexscaves", "scarlet_shield_lightning"));
        return particle == null ? ParticleTypes.ELECTRIC_SPARK : particle;
    }

    private static ParticleOptions getOrbitAxisParticle() {
        SimpleParticleType particle = (SimpleParticleType) ForgeRegistries.PARTICLE_TYPES.getValue(new ResourceLocation("alexscaves", "azure_shield_lightning"));
        return particle == null ? ParticleTypes.ELECTRIC_SPARK : particle;
    }
}
