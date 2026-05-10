package juitar.gwrexpansions.entity.meetyourfight;

import juitar.gwrexpansions.config.GWREConfig;
import juitar.gwrexpansions.item.meetyourfight.DuskfallEclipseBlasterItem;
import lykrast.meetyourfight.entity.ProjectileLineEntity;
import lykrast.meetyourfight.entity.RoseSpiritEntity;
import lykrast.meetyourfight.entity.ai.MoveAroundTarget;
import lykrast.meetyourfight.entity.ai.VexMoveRandomGoal;
import lykrast.meetyourfight.registry.MYFSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.UUID;

public class DuskRoseSpiritEntity extends RoseSpiritEntity {
    private static final EntityDataAccessor<Integer> OWNER_ENTITY_ID = SynchedEntityData.defineId(DuskRoseSpiritEntity.class, EntityDataSerializers.INT);
    private static final String OWNER_TAG = "PlayerOwner";
    private static final String GRACE_TAG = "GraceTicks";
    private static final String ORBIT_INDEX_TAG = "OrbitIndex";

    @Nullable
    private UUID ownerUuid;
    private int graceTicks;
    private int orbitIndex;

    public DuskRoseSpiritEntity(EntityType<? extends DuskRoseSpiritEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
        this.graceTicks = GWREConfig.BURSTGUN.duskfallEclipse.unequippedGraceTicks.get();
        this.attackCooldown = 20;
    }

    public DuskRoseSpiritEntity(EntityType<? extends DuskRoseSpiritEntity> type, Level level, Player owner, int orbitIndex) {
        this(type, level);
        this.ownerUuid = owner.getUUID();
        this.entityData.set(OWNER_ENTITY_ID, owner.getId());
        this.orbitIndex = orbitIndex;
        this.applyConfiguredAttributes();
        this.setPos(owner.getX(), owner.getY() + 2.4D, owner.getZ());
        this.setHealth(this.getMaxHealth());
    }

    public static AttributeSupplier.Builder createDuskAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.ARMOR, 5.0D)
                .add(Attributes.FOLLOW_RANGE, 64.0D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(OWNER_ENTITY_ID, 0);
    }

    private void applyConfiguredAttributes() {
        if (getAttribute(Attributes.MAX_HEALTH) != null) {
            getAttribute(Attributes.MAX_HEALTH).setBaseValue(GWREConfig.BURSTGUN.duskfallEclipse.spiritMaxHealth.get());
            if (getHealth() > getMaxHealth()) {
                setHealth(getMaxHealth());
            }
        }
        if (getAttribute(Attributes.ARMOR) != null) {
            getAttribute(Attributes.ARMOR).setBaseValue(GWREConfig.BURSTGUN.duskfallEclipse.spiritArmor.get());
        }
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new DuskHideAfterHitGoal());
        this.goalSelector.addGoal(3, new DuskBurstAttackGoal());
        this.goalSelector.addGoal(6, new DuskMoveAroundOwnerGoal(0.35D));
        this.goalSelector.addGoal(7, new MoveAroundTarget(this, 0.35D));
        this.goalSelector.addGoal(8, new VexMoveRandomGoal(this, 0.25D));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, LivingEntity.class, 8.0F));
        this.targetSelector.addGoal(1, new DuskOwnerTargetGoal());
        this.targetSelector.addGoal(3, new HurtByTargetGoal(this));
    }

    @Override
    public void customServerAiStep() {
        applyConfiguredAttributes();

        Player owner = getOwnerPlayer();
        if (owner == null || !owner.isAlive()) {
            discard();
            return;
        }
        this.entityData.set(OWNER_ENTITY_ID, owner.getId());

        if (DuskfallEclipseBlasterItem.isHeldBy(owner)) {
            graceTicks = GWREConfig.BURSTGUN.duskfallEclipse.unequippedGraceTicks.get();
        } else if (graceTicks-- <= 0) {
            discard();
            return;
        }

        LivingEntity target = selectTarget(owner);
        if (target != null) {
            setTarget(target);
        } else if (getTarget() != null && !isValidTarget(owner, getTarget(), GWREConfig.BURSTGUN.duskfallEclipse.spiritAutoTargetRange.get() + 8.0D)) {
            setTarget(null);
        }

        super.customServerAiStep();
    }

    @Override
    public void move(MoverType type, Vec3 movement) {
        super.move(type, movement);
        checkInsideBlocks();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getEntity() instanceof Player player && isOwnedBy(player)) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    protected ResourceLocation getDefaultLootTable() {
        return new ResourceLocation("minecraft", "empty");
    }

    public boolean isOwnedBy(Player player) {
        return ownerUuid != null && ownerUuid.equals(player.getUUID());
    }

    public boolean hasSameOwner(DuskRoseSpiritEntity other) {
        return ownerUuid != null && ownerUuid.equals(other.ownerUuid);
    }

    @Nullable
    public Entity getSyncedOwnerEntity() {
        int ownerId = this.entityData.get(OWNER_ENTITY_ID);
        return ownerId == 0 ? null : level().getEntity(ownerId);
    }

    public static int countActiveFor(Player player) {
        return player.level().getEntitiesOfClass(DuskRoseSpiritEntity.class, player.getBoundingBox().inflate(96.0D),
                spirit -> spirit.isAlive() && spirit.isOwnedBy(player)).size();
    }

    @Nullable
    public Player getOwnerPlayer() {
        if (ownerUuid == null || !(level() instanceof ServerLevel serverLevel)) {
            return null;
        }

        Entity entity = serverLevel.getEntity(ownerUuid);
        return entity instanceof Player player ? player : null;
    }

    @Nullable
    private LivingEntity selectTarget(Player owner) {
        double range = GWREConfig.BURSTGUN.duskfallEclipse.spiritAutoTargetRange.get();
        LivingEntity remembered = getRememberedTarget(owner, range);
        if (remembered != null) {
            return remembered;
        }

        AABB searchBox = owner.getBoundingBox().inflate(range);
        return level().getEntitiesOfClass(LivingEntity.class, searchBox, target -> isValidAutoTarget(owner, target))
                .stream()
                .min(Comparator.comparingDouble(target -> target.distanceToSqr(owner)))
                .orElse(null);
    }

    @Nullable
    private LivingEntity getRememberedTarget(Player owner, double range) {
        CompoundTag data = owner.getPersistentData();
        if (!data.hasUUID(DuskfallEclipseBlasterItem.LAST_TARGET_TAG)) {
            return null;
        }

        long memoryTicks = GWREConfig.BURSTGUN.duskfallEclipse.lastTargetMemoryTicks.get();
        if (memoryTicks > 0 && level().getGameTime() - data.getLong(DuskfallEclipseBlasterItem.LAST_TARGET_TIME_TAG) > memoryTicks) {
            return null;
        }

        if (level() instanceof ServerLevel serverLevel) {
            Entity entity = serverLevel.getEntity(data.getUUID(DuskfallEclipseBlasterItem.LAST_TARGET_TAG));
            if (entity instanceof LivingEntity living && isValidTarget(owner, living, range)) {
                return living;
            }
        }
        return null;
    }

    private boolean isValidAutoTarget(Player owner, LivingEntity target) {
        return isValidTarget(owner, target, GWREConfig.BURSTGUN.duskfallEclipse.spiritAutoTargetRange.get())
                && target instanceof Enemy;
    }

    private boolean isValidTarget(Player owner, LivingEntity target, double range) {
        return target.isAlive()
                && target != owner
                && !(target instanceof DuskRoseSpiritEntity spirit && spirit.isOwnedBy(owner))
                && !target.isAlliedTo(owner)
                && target.distanceToSqr(owner) <= range * range;
    }

    private ProjectileLineEntity readyAttack() {
        ProjectileLineEntity attack = new DuskRoseProjectileLineEntity(level(), this);
        attack.setOwner(this);
        attack.setPos(getX(), getY() + 0.625D, getZ());
        attack.setVariant(ProjectileLineEntity.VAR_ROSALYNE);
        return attack;
    }

    private void performAttack(LivingEntity target) {
        Vec3 direction = new Vec3(target.getX() - getX(), target.getY() + 1.0D - getY(), target.getZ() - getZ()).normalize();
        ProjectileLineEntity attack = readyAttack();
        attack.setUp(1, direction.x, direction.y, direction.z, getX(), getY(), getZ());
        level().addFreshEntity(attack);
        playSound(MYFSounds.roseSpiritShoot.get(), 1.0F, 1.0F);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        ownerUuid = tag.hasUUID(OWNER_TAG) ? tag.getUUID(OWNER_TAG) : null;
        graceTicks = tag.getInt(GRACE_TAG);
        orbitIndex = tag.getInt(ORBIT_INDEX_TAG);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (ownerUuid != null) {
            tag.putUUID(OWNER_TAG, ownerUuid);
        }
        tag.putInt(GRACE_TAG, graceTicks);
        tag.putInt(ORBIT_INDEX_TAG, orbitIndex);
    }

    private class DuskOwnerTargetGoal extends Goal {
        @Override
        public boolean canUse() {
            Player owner = getOwnerPlayer();
            return owner != null && selectTarget(owner) != null;
        }

        @Override
        public void start() {
            Player owner = getOwnerPlayer();
            if (owner != null) {
                setTarget(selectTarget(owner));
            }
        }
    }

    private class DuskMoveAroundOwnerGoal extends Goal {
        private final double speed;

        private DuskMoveAroundOwnerGoal(double speed) {
            this.speed = speed;
            setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            return getOwnerPlayer() != null && !getMoveControl().hasWanted();
        }

        @Override
        public void start() {
            Player owner = getOwnerPlayer();
            if (owner == null) {
                return;
            }

            Vec3 forward = new Vec3(owner.getLookAngle().x, 0.0D, owner.getLookAngle().z);
            if (forward.lengthSqr() < 1.0E-4D) {
                forward = new Vec3(0.0D, 0.0D, 1.0D).yRot((float) (-owner.getYRot() * Math.PI / 180.0D));
            }
            forward = forward.normalize();
            Vec3 back = forward.scale(-1.0D);
            Vec3 right = new Vec3(-forward.z, 0.0D, forward.x);
            double side = switch (Math.floorMod(orbitIndex, 3)) {
                case 0 -> -1.25D;
                case 1 -> 1.25D;
                default -> 0.0D;
            };
            double distance = getRandom().nextDouble() * 0.5D + 1.7D;
            double hoverHeight = owner.getY() + 2.35D + getRandom().nextDouble() * 0.55D;
            Vec3 target = owner.position().add(back.scale(distance)).add(right.scale(side));
            double moveSpeed = distanceToSqr(owner) > 25.0D ? 2.0D : Math.max(speed, 0.45D);
            getMoveControl().setWantedPosition(target.x,
                    hoverHeight,
                    target.z,
                    moveSpeed);
        }

        @Override
        public boolean canContinueToUse() {
            return false;
        }
    }

    private class DuskBurstAttackGoal extends Goal {
        private LivingEntity target;
        private int attackRemaining;
        private int attackDelay;
        private int phase;

        @Override
        public boolean canUse() {
            return attackCooldown <= 0 && getTarget() != null && getTarget().isAlive();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void start() {
            attackCooldown = 2;
            attackDelay = Math.max(1, GWREConfig.BURSTGUN.duskfallEclipse.spiritWarnTicks.get());
            attackRemaining = 2 + getRandom().nextInt(5);
            target = getTarget();
            phase = 0;
            setStatus(RISING);
        }

        @Override
        public void tick() {
            attackCooldown = 2;
            attackDelay--;
            if (attackDelay > 0) {
                return;
            }

            switch (phase) {
                case 0 -> {
                    setStatus(OUT);
                    phase = 1;
                    attackDelay = 20 + getRandom().nextInt(41);
                }
                case 1 -> {
                    setStatus(ATTACKING);
                    phase = 2;
                    attackDelay = Math.max(1, GWREConfig.BURSTGUN.duskfallEclipse.spiritWarnTicks.get());
                    playSound(MYFSounds.roseSpiritWarn.get(), 1.0F, 1.0F);
                }
                case 2 -> {
                    attackDelay = Math.max(1, GWREConfig.BURSTGUN.duskfallEclipse.spiritAttackCooldownTicks.get());
                    attackRemaining--;
                    if (target != null && target.isAlive()) {
                        performAttack(target);
                    }
                    if (attackRemaining <= 0) {
                        phase = 3;
                        setStatus(OUT);
                        attackDelay = 40 + getRandom().nextInt(41);
                    }
                }
                case 3 -> {
                    phase = 4;
                    attackDelay = 10;
                    setStatus(RETRACTING);
                }
                case 4 -> {
                    phase = 5;
                    setStatus(HIDING);
                }
                default -> {
                }
            }
        }

        @Override
        public void stop() {
            attackCooldown = Math.max(1, GWREConfig.BURSTGUN.duskfallEclipse.spiritAttackCooldownTicks.get());
        }

        @Override
        public boolean canContinueToUse() {
            return phase <= 4 && target != null && target.isAlive() && getStatus() != HURT && getStatus() != RETRACTING_HURT;
        }
    }

    private class DuskHideAfterHitGoal extends Goal {
        private int timer;

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void start() {
            attackCooldown = 100 + getRandom().nextInt(61);
            timer = 60;
            playSound(MYFSounds.roseSpiritHurtBig.get(), 1.0F, 1.0F);
        }

        @Override
        public void tick() {
            timer--;
            if (timer <= 0) {
                setStatus(HIDING);
                return;
            }

            if (timer == 10) {
                setStatus(RETRACTING_HURT);
                LivingEntity target = getTarget();
                Vec3 direction = target == null
                        ? new Vec3(1.0D, -0.25D, 0.0D)
                        : new Vec3(target.getX() - getX(), target.getY() + 1.0D - getY(), target.getZ() - getZ());
                direction = direction.normalize();
                for (int i = 0; i < 8; i++) {
                    ProjectileLineEntity attack = readyAttack();
                    attack.setUp(1, direction.x, direction.y, direction.z, getX(), getY(), getZ());
                    level().addFreshEntity(attack);
                    direction = direction.yRot(0.7853982F);
                }
                playSound(MYFSounds.roseSpiritShoot.get(), 1.0F, 1.0F);
            }
        }

        @Override
        public boolean canUse() {
            return getStatus() == HURT || getStatus() == RETRACTING_HURT;
        }
    }
}
