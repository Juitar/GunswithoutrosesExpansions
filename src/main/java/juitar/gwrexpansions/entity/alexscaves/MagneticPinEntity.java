package juitar.gwrexpansions.entity.alexscaves;

import juitar.gwrexpansions.registry.GWREEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import lykrast.gunswithoutroses.entity.BulletEntity;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MagneticPinEntity extends BulletEntity {
    public static final double MAX_OWNER_DISTANCE = 32.0D;
    public static final int MAX_ORBITING_BULLETS = 24;
    public static final double PIN_HALF_LENGTH = 0.48D;
    private static final double ENTITY_EMBED_DEPTH = 0.16D;
    private static final double BLOCK_EMBED_DEPTH = 0.08D;
    private static final EntityDataAccessor<Boolean> ATTACHED = SynchedEntityData.defineId(MagneticPinEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> ATTACHED_TO_BLOCK = SynchedEntityData.defineId(MagneticPinEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> AXIS_X = SynchedEntityData.defineId(MagneticPinEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> AXIS_Y = SynchedEntityData.defineId(MagneticPinEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> AXIS_Z = SynchedEntityData.defineId(MagneticPinEntity.class, EntityDataSerializers.FLOAT);

    private final List<MagneticBulletEntity> orbitingBullets = new ArrayList<>();
    @Nullable
    private UUID attachedEntityUuid;
    private int attachedEntityId = -1;
    @Nullable
    private BlockPos attachedBlock;
    private Direction attachedFace = Direction.UP;
    private Vec3 attachedOffset = Vec3.ZERO;
    private boolean releasingBullets;

    public MagneticPinEntity(EntityType<? extends BulletEntity> type, Level level) {
        super(type, level);
    }

    public MagneticPinEntity(Level level, LivingEntity shooter) {
        super(GWREEntities.MAGNETIC_PIN.get(), shooter, level);
        setOwner(shooter);
        setDamage(0.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(ATTACHED, false);
        entityData.define(ATTACHED_TO_BLOCK, false);
        entityData.define(AXIS_X, 0.0F);
        entityData.define(AXIS_Y, 0.0F);
        entityData.define(AXIS_Z, 1.0F);
    }

    @Override
    public void tick() {
        if (!isAttached()) {
            super.tick();
            return;
        }

        baseTick();
        setDeltaMovement(Vec3.ZERO);
        noPhysics = true;

        if (!level().isClientSide && shouldDiscardAttachedPin()) {
            discard();
            return;
        }

        if (!level().isClientSide && isAttachedToBlock() && attachedBlock != null) {
            setPos(Vec3.atCenterOf(attachedBlock).add(attachedOffset));
        } else {
            Entity attachedEntity = getAttachedEntity();
            if (attachedEntity != null) {
                setPos(attachedEntity.position().add(attachedOffset));
            }
        }

        spawnAttachedTrail();
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

        if (level().isClientSide) {
            spawnFlightTrail(new Vec3(nextX, nextY, nextZ), movement);
        }

        setPos(nextX, nextY, nextZ);
    }

    private boolean shouldDiscardAttachedPin() {
        Entity owner = getOwner();
        if (!(owner instanceof Player player) || !player.isAlive() || owner.level() != level()) {
            return true;
        }
        if (distanceToSqr(owner) > MAX_OWNER_DISTANCE * MAX_OWNER_DISTANCE) {
            return true;
        }
        if (isAttachedToBlock()) {
            return attachedBlock == null || level().getBlockState(attachedBlock).isAir();
        }
        Entity attachedEntity = getAttachedEntity();
        return !(attachedEntity instanceof LivingEntity livingEntity) || !livingEntity.isAlive();
    }

    @Override
    protected HitResult getHitResult() {
        if (isAttached()) {
            return BlockHitResult.miss(position(), attachedFace, blockPosition());
        }
        return super.getHitResult();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity target = result.getEntity();
        if (target == getOwner()) {
            return;
        }
        setAttached(false);
        attachedEntityId = target.getId();
        attachedEntityUuid = target.getUUID();
        Vec3 axis = getMagneticAxis();
        setMagneticAxis(axis);
        Vec3 hitLocation = getEntityImpactLocation(target, result);
        Vec3 anchor = hitLocation.subtract(axis.scale(PIN_HALF_LENGTH - ENTITY_EMBED_DEPTH));
        attachedOffset = anchor.subtract(target.position());
        noPhysics = true;
        setDeltaMovement(Vec3.ZERO);
        setPos(target.position().add(attachedOffset));
    }

    private Vec3 getEntityImpactLocation(Entity target, EntityHitResult result) {
        Vec3 start = position();
        Vec3 movement = getDeltaMovement();
        Vec3 end = movement.lengthSqr() > 1.0E-4D ? start.add(movement) : result.getLocation();
        return target.getBoundingBox().inflate(0.05D)
                .clip(start, end)
                .orElse(result.getLocation());
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        setAttached(true);
        attachedBlock = result.getBlockPos();
        attachedFace = result.getDirection();
        Vec3 axis = getMagneticAxis();
        setMagneticAxis(axis);
        Vec3 anchor = result.getLocation().subtract(axis.scale(PIN_HALF_LENGTH - BLOCK_EMBED_DEPTH));
        attachedOffset = anchor.subtract(Vec3.atCenterOf(attachedBlock));
        noPhysics = true;
        setDeltaMovement(Vec3.ZERO);
        setPos(anchor);
    }

    @Override
    protected boolean shouldDespawnOnHit(HitResult result) {
        return false;
    }

    public boolean tryAddOrbitingBullet(MagneticBulletEntity bullet) {
        orbitingBullets.removeIf(entity -> entity == null || entity.isRemoved());
        if (orbitingBullets.size() >= MAX_ORBITING_BULLETS) {
            return false;
        }
        orbitingBullets.add(bullet);
        return true;
    }

    public void removeOrbitingBullet(MagneticBulletEntity bullet) {
        orbitingBullets.remove(bullet);
    }

    public boolean isAttachedToBlock() {
        return isAttached() && entityData.get(ATTACHED_TO_BLOCK);
    }

    public boolean isAttachedToEntity() {
        return isAttached() && !entityData.get(ATTACHED_TO_BLOCK);
    }

    public boolean canAttract(Entity owner) {
        return isAttached() && !isRemoved() && owner != null && owner.equals(getOwner());
    }

    private boolean isAttached() {
        return entityData.get(ATTACHED);
    }

    private void setAttached(boolean toBlock) {
        entityData.set(ATTACHED, true);
        entityData.set(ATTACHED_TO_BLOCK, toBlock);
    }

    @Nullable
    public Entity getAttachedEntity() {
        if (attachedEntityId != -1) {
            Entity entity = level().getEntity(attachedEntityId);
            if (entity != null) {
                return entity;
            }
        }
        if (attachedEntityUuid != null) {
            for (Entity entity : level().getEntities(this, getBoundingBox().inflate(64.0D), entity -> entity.getUUID().equals(attachedEntityUuid))) {
                attachedEntityId = entity.getId();
                return entity;
            }
        }
        return null;
    }

    public Vec3 getTargetPoint() {
        Entity entity = getAttachedEntity();
        if (entity instanceof LivingEntity livingEntity) {
            return livingEntity.position().add(0.0D, livingEntity.getBbHeight() * 0.65D, 0.0D);
        }
        return position();
    }

    public int getOrbitIndex(MagneticBulletEntity bullet) {
        int index = orbitingBullets.indexOf(bullet);
        return index < 0 ? 0 : index;
    }

    public int getOrbitCount() {
        return Math.max(1, orbitingBullets.size());
    }

    public Vec3 getMagneticAxis() {
        Vec3 movement = getDeltaMovement();
        if (movement.lengthSqr() > 1.0E-4D) {
            return movement.normalize();
        }
        Vec3 syncedAxis = new Vec3(entityData.get(AXIS_X), entityData.get(AXIS_Y), entityData.get(AXIS_Z));
        if (syncedAxis.lengthSqr() > 1.0E-4D) {
            return syncedAxis.normalize();
        }
        Vec3 look = getLookAngle();
        return look.lengthSqr() > 1.0E-4D ? look.normalize() : new Vec3(0.0D, 0.0D, 1.0D);
    }

    private void setMagneticAxis(Vec3 axis) {
        Vec3 normalized = axis.lengthSqr() > 1.0E-4D ? axis.normalize() : new Vec3(0.0D, 0.0D, 1.0D);
        entityData.set(AXIS_X, (float) normalized.x);
        entityData.set(AXIS_Y, (float) normalized.y);
        entityData.set(AXIS_Z, (float) normalized.z);
    }

    @Override
    public void remove(RemovalReason reason) {
        if (!level().isClientSide && !releasingBullets && reason != RemovalReason.UNLOADED_TO_CHUNK) {
            releaseOrbitingBullets();
        }
        super.remove(reason);
    }

    private void releaseOrbitingBullets() {
        releasingBullets = true;
        List<MagneticBulletEntity> bullets = new ArrayList<>(orbitingBullets);
        orbitingBullets.clear();
        int total = Math.max(1, bullets.size());
        for (int i = 0; i < bullets.size(); i++) {
            MagneticBulletEntity bullet = bullets.get(i);
            if (bullet != null && !bullet.isRemoved()) {
                bullet.releaseFromPin(position(), i, total);
            }
        }
        releasingBullets = false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Attached", isAttached());
        tag.putBoolean("AttachedToBlock", isAttachedToBlock());
        tag.putInt("AttachedEntityId", attachedEntityId);
        if (attachedEntityUuid != null) {
            tag.putUUID("AttachedEntityUuid", attachedEntityUuid);
        }
        if (attachedBlock != null) {
            tag.putInt("AttachedBlockX", attachedBlock.getX());
            tag.putInt("AttachedBlockY", attachedBlock.getY());
            tag.putInt("AttachedBlockZ", attachedBlock.getZ());
        }
        tag.putInt("AttachedFace", attachedFace.get3DDataValue());
        tag.putDouble("AttachedOffsetX", attachedOffset.x);
        tag.putDouble("AttachedOffsetY", attachedOffset.y);
        tag.putDouble("AttachedOffsetZ", attachedOffset.z);
        Vec3 axis = getMagneticAxis();
        tag.putDouble("AxisX", axis.x);
        tag.putDouble("AxisY", axis.y);
        tag.putDouble("AxisZ", axis.z);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.getBoolean("Attached")) {
            setAttached(tag.getBoolean("AttachedToBlock"));
        }
        attachedEntityId = tag.getInt("AttachedEntityId");
        if (tag.hasUUID("AttachedEntityUuid")) {
            attachedEntityUuid = tag.getUUID("AttachedEntityUuid");
        }
        if (tag.contains("AttachedBlockX")) {
            attachedBlock = new BlockPos(tag.getInt("AttachedBlockX"), tag.getInt("AttachedBlockY"), tag.getInt("AttachedBlockZ"));
        }
        attachedFace = Direction.from3DDataValue(tag.getInt("AttachedFace"));
        attachedOffset = new Vec3(tag.getDouble("AttachedOffsetX"), tag.getDouble("AttachedOffsetY"), tag.getDouble("AttachedOffsetZ"));
        if (tag.contains("AxisX")) {
            setMagneticAxis(new Vec3(tag.getDouble("AxisX"), tag.getDouble("AxisY"), tag.getDouble("AxisZ")));
        }
        noPhysics = isAttached();
    }

    @Override
    protected ParticleOptions getTrailParticle() {
        return getFlowParticle();
    }

    @Override
    public ItemStack getItemRaw() {
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation("alexscaves", "azure_magnet"));
        return new ItemStack(item == null ? Items.IRON_NUGGET : item);
    }

    private void spawnFlightTrail(Vec3 currentPos, Vec3 movement) {
        if (movement.lengthSqr() < 1.0E-4D) {
            return;
        }
        Vec3 start = currentPos.add(0.0D, 0.03D, 0.0D);
        Vec3 end = start.add(movement.scale(0.75D)).add(randomJitter(0.14D, 0.14D));
        level().addParticle(getFlightTrailParticle(), start.x, start.y, start.z, end.x, end.y, end.z);
    }

    private void spawnAttachedTrail() {
        if (!level().isClientSide) {
            return;
        }
        Vec3 start = position();
        Vec3 face = Vec3.atLowerCornerOf(attachedFace.getNormal()).scale(0.45D);
        Vec3 end = start.add(face).add(randomJitter(0.18D, 0.18D));
        level().addParticle(getFlightTrailParticle(), start.x, start.y, start.z, end.x, end.y, end.z);
    }

    private Vec3 randomJitter(double horizontal, double vertical) {
        return new Vec3(
                (random.nextDouble() - 0.5D) * horizontal,
                (random.nextDouble() - 0.5D) * vertical,
                (random.nextDouble() - 0.5D) * horizontal
        );
    }

    private static ParticleOptions getFlowParticle() {
        SimpleParticleType particle = (SimpleParticleType) ForgeRegistries.PARTICLE_TYPES.getValue(new ResourceLocation("alexscaves", "azure_magnetic_flow"));
        return particle == null ? ParticleTypes.ELECTRIC_SPARK : particle;
    }

    private static ParticleOptions getFlightTrailParticle() {
        SimpleParticleType particle = (SimpleParticleType) ForgeRegistries.PARTICLE_TYPES.getValue(new ResourceLocation("alexscaves", "azure_shield_lightning"));
        return particle == null ? ParticleTypes.ELECTRIC_SPARK : particle;
    }

    private static ParticleOptions getOrbitParticle() {
        SimpleParticleType particle = (SimpleParticleType) ForgeRegistries.PARTICLE_TYPES.getValue(new ResourceLocation("alexscaves", "azure_magnetic_orbit"));
        return particle == null ? ParticleTypes.ELECTRIC_SPARK : particle;
    }
}
