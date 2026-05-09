package juitar.gwrexpansions.entity.meetyourfight;

import juitar.gwrexpansions.config.GWREConfig;
import juitar.gwrexpansions.item.meetyourfight.DuskfallEclipseBlasterItem;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.UUID;

public class DuskRoseSpiritEntity extends Entity implements ItemSupplier {
    private static final String OWNER_TAG = "Owner";
    private static final String TARGET_TAG = "Target";
    private static final String GRACE_TAG = "GraceTicks";
    private static final String ATTACK_COOLDOWN_TAG = "AttackCooldown";
    private static final String WARN_TICKS_TAG = "WarnTicks";
    private static final String WARN_TARGET_TAG = "WarnTarget";
    private static final String ORBIT_INDEX_TAG = "OrbitIndex";

    @Nullable
    private UUID ownerUuid;
    @Nullable
    private UUID targetUuid;
    @Nullable
    private UUID warnedTargetUuid;
    private int graceTicks;
    private int attackCooldown;
    private int warnTicks;
    private int orbitIndex;

    public DuskRoseSpiritEntity(EntityType<? extends DuskRoseSpiritEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
        setNoGravity(true);
        graceTicks = GWREConfig.BURSTGUN.duskfallEclipse.unequippedGraceTicks.get();
    }

    public DuskRoseSpiritEntity(EntityType<? extends DuskRoseSpiritEntity> type, Level level, Player owner, int orbitIndex) {
        this(type, level);
        this.ownerUuid = owner.getUUID();
        this.orbitIndex = orbitIndex;
        this.graceTicks = GWREConfig.BURSTGUN.duskfallEclipse.unequippedGraceTicks.get();
        setPos(owner.getX(), owner.getEyeY() + 0.4D, owner.getZ());
    }

    @Override
    public void tick() {
        super.tick();
        setNoGravity(true);

        if (level().isClientSide) {
            return;
        }

        Player owner = getOwnerPlayer();
        if (owner == null || !owner.isAlive()) {
            discard();
            return;
        }

        if (DuskfallEclipseBlasterItem.isHeldBy(owner)) {
            graceTicks = GWREConfig.BURSTGUN.duskfallEclipse.unequippedGraceTicks.get();
        } else if (graceTicks-- <= 0) {
            discard();
            return;
        }

        followOwner(owner);
        LivingEntity target = selectTarget(owner);
        handleAttack(owner, target);
    }

    private void followOwner(Player owner) {
        double angle = (owner.tickCount * 0.09D) + (orbitIndex * Math.PI * 2.0D / Math.max(1, GWREConfig.BURSTGUN.duskfallEclipse.maxSpirits.get()));
        double radius = 1.15D + (orbitIndex % 2) * 0.25D;
        Vec3 desired = owner.position().add(Math.cos(angle) * radius, 1.35D + Math.sin(angle * 0.7D) * 0.25D,
                Math.sin(angle) * radius);
        Vec3 delta = desired.subtract(position());
        if (delta.lengthSqr() > 64.0D) {
            setPos(desired);
            setDeltaMovement(Vec3.ZERO);
            return;
        }

        setDeltaMovement(delta.scale(0.22D));
        move(MoverType.SELF, getDeltaMovement());
    }

    @Nullable
    private LivingEntity selectTarget(Player owner) {
        double range = GWREConfig.BURSTGUN.duskfallEclipse.spiritAutoTargetRange.get();
        LivingEntity remembered = getRememberedTarget(owner, range);
        if (remembered != null) {
            targetUuid = remembered.getUUID();
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
                && !target.isAlliedTo(owner)
                && target.distanceToSqr(owner) <= range * range;
    }

    private void handleAttack(Player owner, @Nullable LivingEntity target) {
        if (attackCooldown > 0) {
            attackCooldown--;
        }

        if (warnTicks > 0) {
            warnTicks--;
            if (warnTicks <= 0) {
                LivingEntity warnedTarget = resolveWarnedTarget();
                if (warnedTarget != null && isValidTarget(owner, warnedTarget, GWREConfig.BURSTGUN.duskfallEclipse.spiritAutoTargetRange.get() + 4.0D)) {
                    fireAt(owner, warnedTarget);
                }
                attackCooldown = GWREConfig.BURSTGUN.duskfallEclipse.spiritAttackCooldownTicks.get();
                warnedTargetUuid = null;
            }
            return;
        }

        if (target != null && attackCooldown <= 0) {
            warnedTargetUuid = target.getUUID();
            warnTicks = GWREConfig.BURSTGUN.duskfallEclipse.spiritWarnTicks.get();
            if (warnTicks <= 0) {
                fireAt(owner, target);
                attackCooldown = GWREConfig.BURSTGUN.duskfallEclipse.spiritAttackCooldownTicks.get();
                warnedTargetUuid = null;
            } else {
                level().playSound(null, getX(), getY(), getZ(), myfSound("entity.rosespirit.warn", SoundEvents.AMETHYST_BLOCK_CHIME),
                        SoundSource.PLAYERS, 0.65F, 1.2F);
            }
        }
    }

    @Nullable
    private LivingEntity resolveWarnedTarget() {
        if (warnedTargetUuid == null || !(level() instanceof ServerLevel serverLevel)) {
            return null;
        }

        Entity entity = serverLevel.getEntity(warnedTargetUuid);
        return entity instanceof LivingEntity living ? living : null;
    }

    private void fireAt(Player owner, LivingEntity target) {
        double damage = GWREConfig.BURSTGUN.duskfallEclipse.spiritAttackDamage.get().floatValue();
        if (damage > 0.0D) {
            int oldInvulnerableTime = target.invulnerableTime;
            target.invulnerableTime = 0;
            target.hurt(level().damageSources().indirectMagic(this, owner), (float) damage);
            target.invulnerableTime = oldInvulnerableTime;
        }

        Vec3 direction = target.getEyePosition().subtract(position()).normalize();
        target.push(direction.x * 0.18D, 0.06D, direction.z * 0.18D);
        level().playSound(null, target.getX(), target.getY(), target.getZ(), myfSound("entity.rosespirit.shoot", SoundEvents.AMETHYST_BLOCK_RESONATE),
                SoundSource.PLAYERS, 0.75F, 1.1F);
        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SWEEP_ATTACK, target.getX(), target.getY(0.55D), target.getZ(),
                    1, 0.1D, 0.05D, 0.1D, 0.0D);
            serverLevel.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 0.2D, getZ(),
                    4, 0.1D, 0.1D, 0.1D, 0.02D);
        }
    }

    @Nullable
    private Player getOwnerPlayer() {
        if (ownerUuid == null || !(level() instanceof ServerLevel serverLevel)) {
            return null;
        }

        Entity entity = serverLevel.getEntity(ownerUuid);
        return entity instanceof Player player ? player : null;
    }

    public boolean isOwnedBy(Player player) {
        return ownerUuid != null && ownerUuid.equals(player.getUUID());
    }

    public static int countActiveFor(Player player) {
        return player.level().getEntitiesOfClass(DuskRoseSpiritEntity.class, player.getBoundingBox().inflate(96.0D),
                spirit -> spirit.isAlive() && spirit.isOwnedBy(player)).size();
    }

    @Override
    public ItemStack getItem() {
        Item bloom = ForgeRegistries.ITEMS.getValue(new ResourceLocation("meetyourfight", "violet_bloom"));
        return new ItemStack(bloom == null ? Items.POPPY : bloom);
    }

    private static SoundEvent myfSound(String name, SoundEvent fallback) {
        SoundEvent sound = ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("meetyourfight", name));
        return sound == null ? fallback : sound;
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        ownerUuid = tag.hasUUID(OWNER_TAG) ? tag.getUUID(OWNER_TAG) : null;
        targetUuid = tag.hasUUID(TARGET_TAG) ? tag.getUUID(TARGET_TAG) : null;
        warnedTargetUuid = tag.hasUUID(WARN_TARGET_TAG) ? tag.getUUID(WARN_TARGET_TAG) : null;
        graceTicks = tag.getInt(GRACE_TAG);
        attackCooldown = tag.getInt(ATTACK_COOLDOWN_TAG);
        warnTicks = tag.getInt(WARN_TICKS_TAG);
        orbitIndex = tag.getInt(ORBIT_INDEX_TAG);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (ownerUuid != null) {
            tag.putUUID(OWNER_TAG, ownerUuid);
        }
        if (targetUuid != null) {
            tag.putUUID(TARGET_TAG, targetUuid);
        }
        if (warnedTargetUuid != null) {
            tag.putUUID(WARN_TARGET_TAG, warnedTargetUuid);
        }
        tag.putInt(GRACE_TAG, graceTicks);
        tag.putInt(ATTACK_COOLDOWN_TAG, attackCooldown);
        tag.putInt(WARN_TICKS_TAG, warnTicks);
        tag.putInt(ORBIT_INDEX_TAG, orbitIndex);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
