package juitar.gwrexpansions.item.cataclysm;

import com.github.L_Ender.cataclysm.entity.projectile.Death_Laser_Beam_Entity;
import com.github.L_Ender.cataclysm.entity.projectile.Wither_Homing_Missile_Entity;
import com.github.L_Ender.cataclysm.init.ModEntities;
import com.github.L_Ender.cataclysm.init.ModSounds;
import juitar.gwrexpansions.config.GWREConfig;
import juitar.gwrexpansions.entity.vanilla.RedstonePiercingBulletEntity;
import juitar.gwrexpansions.item.ConfigurableGunItem;
import juitar.gwrexpansions.registry.VanillaItem;
import lykrast.gunswithoutroses.entity.BulletEntity;
import lykrast.gunswithoutroses.item.IBullet;
import lykrast.gunswithoutroses.registry.GWRAttributes;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class HarbingerRaycasterItem extends ConfigurableGunItem {
    private static final String OVERLOAD_TAG = "HarbingerRaycasterOverload";
    private static final String OVERLOAD_ACTIVE_TICKS_TAG = "HarbingerRaycasterOverloadActiveTicks";
    public static final String HARBINGER_REDSTONE_SHOT_TAG = "HarbingerRaycasterRedstoneShot";
    public static final String HARBINGER_OVERLOAD_SHOT_TAG = "HarbingerRaycasterOverloadShot";
    private static final double DEATH_LASER_VERTICAL_OFFSET = -0.18D;
    private static final int DEATH_LASER_PREPARE_TICKS = 8;
    private static final double MISSILE_SPAWN_RADIUS = 1.15D;
    private static final List<ActiveOverload> ACTIVE_OVERLOADS = new ArrayList<>();
    private static final List<PendingDeathLaser> PENDING_DEATH_LASERS = new ArrayList<>();
    private static final List<ActiveDeathLaserSegment> ACTIVE_DEATH_LASERS = new ArrayList<>();

    public HarbingerRaycasterItem(Properties properties, int bonusDamage, double damageMultiplier,
                                  int fireDelay, double inaccuracy, int enchantability,
                                  Supplier<GWREConfig.GunConfig> configSupplier) {
        super(properties, bonusDamage, damageMultiplier, fireDelay, inaccuracy, enchantability, configSupplier);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (isOverloadActive(stack) || isOverloadActiveFor(level, player)) {
            return InteractionResultHolder.fail(stack);
        }

        return super.use(level, player, hand);
    }

    @Override
    protected void shoot(Level level, Player player, ItemStack gun, ItemStack ammo, IBullet bulletItem, boolean bulletFree) {
        if (isOverloadActive(gun) || isOverloadActiveFor(level, player)) {
            return;
        }

        if (!isRedstoneBullet(ammo)) {
            super.shoot(level, player, gun, ammo, bulletItem, bulletFree);
            return;
        }

        boolean overloaded = isOverloaded(gun);
        fireEmpoweredRedstoneShot(level, player, gun, ammo, bulletItem, bulletFree, overloaded);

        if (overloaded) {
            setOverload(gun, 0);
            triggerOverload(level, player, gun, ammo, bulletItem);
            return;
        }

        int overload = Math.min(getMaxOverload(), getOverload(gun) + 1);
        setOverload(gun, overload);
        if (overload >= getMaxOverload()) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    ModSounds.HARBINGER_MODE_CHANGE.get(), SoundSource.PLAYERS, 0.75F, 1.15F);
        }
    }

    private void fireEmpoweredRedstoneShot(Level level, Player player, ItemStack gun, ItemStack ammo,
                                           IBullet bulletItem, boolean bulletFree, boolean overloadShot) {
        int shots = getProjectilesPerShot(gun, player);
        ItemStack firedAmmo = snapshotAmmo(ammo, bulletItem);
        double baseDamage = getBulletBaseDamage(level, player, firedAmmo, bulletItem);
        for (int i = 0; i < shots; i++) {
            RedstonePiercingBulletEntity shot = new RedstonePiercingBulletEntity(level, player);
            shot.setItem(firedAmmo.copyWithCount(1));
            shot.setOwner(player);
            shot.setPierce(raycasterConfig().redstonePierce.get());
            shot.setPierceMultiplier(raycasterConfig().redstonePierceDamageMultiplier.get());
            shot.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F,
                    (float) getProjectileSpeed(gun, player), (float) getInaccuracy(gun, player));
            shot.setDamage(Math.max(0.0D, baseDamage + raycasterConfig().redstoneDamageBonus.get() + getBonusDamage(gun, player))
                    * getDamageMultiplier(gun, player));
            if (player.getAttribute((Attribute) GWRAttributes.knockback.get()) != null) {
                shot.setKnockbackStrength(shot.getKnockbackStrength()
                        + player.getAttributeValue((Attribute) GWRAttributes.knockback.get()));
            }
            shot.setHeadshotMultiplier(getHeadshotMultiplier(gun, player));
            affectBulletEntity(player, gun, shot, bulletFree);
            CompoundTag persistentData = shot.getPersistentData();
            persistentData.putBoolean(HARBINGER_REDSTONE_SHOT_TAG, true);
            persistentData.putBoolean(HARBINGER_OVERLOAD_SHOT_TAG, overloadShot || isOverloadActiveFor(level, player));
            level.addFreshEntity(shot);
        }
    }

    private void triggerOverload(Level level, Player player, ItemStack gun, ItemStack ammo, IBullet bulletItem) {
        double damage = getBulletBaseDamage(level, player, snapshotAmmo(ammo, bulletItem), bulletItem);
        setOverloadActiveTicks(gun, overloadDurationTicks());
        if (level instanceof ServerLevel serverLevel) {
            startOverloadMode(serverLevel, player);
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    ModSounds.HARBINGER_DEATHLASER_PREPARE.get(), SoundSource.PLAYERS, 0.55F, 1.0F);
            scheduleDeathLaser(serverLevel, player,
                    (damage + raycasterConfig().redstoneDamageBonus.get() + getBonusDamage(gun, player))
                            * getDamageMultiplier(gun, player) * raycasterConfig().deathLaserDamageMultiplier.get(),
                    Math.max(1, overloadDurationTicks() - DEATH_LASER_PREPARE_TICKS));
        }
    }

    private static void scheduleDeathLaser(ServerLevel level, Player player, double damage, int durationTicks) {
        PENDING_DEATH_LASERS.add(new PendingDeathLaser(level.dimension(), player.getUUID(), damage, durationTicks,
                DEATH_LASER_PREPARE_TICKS));
    }

    private static void fireDeathLaser(Level level, Player player, double damage, int durationTicks) {
        Vec3 eye = player.getEyePosition(1.0F);
        Vec3 look = player.getLookAngle().normalize();
        if (look.lengthSqr() < 1.0E-4D) {
            look = Vec3.directionFromRotation(player.getXRot(), player.getYRot()).normalize();
        }

        int segments = Math.max(1, raycasterConfig().deathLaserSegments.get());
        double segmentLength = raycasterConfig().deathLaserSegmentLength.get();
        for (int i = 0; i < segments; i++) {
            Vec3 start = eye.add(look.scale(segmentLength * i));
            if (i > 0 && isBlockedBeforeSegment(level, player, eye, start)) {
                break;
            }
            spawnDeathLaserSegment(level, player, start, damage, durationTicks, i);
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                ModSounds.DEATH_LASER.get(), SoundSource.PLAYERS, 1.05F, 0.95F);
    }

    private static void spawnDeathLaserSegment(Level level, Player player, Vec3 start, double damage,
                                               int durationTicks, int segmentIndex) {
        Death_Laser_Beam_Entity laser = new Death_Laser_Beam_Entity(ModEntities.DEATH_LASER_BEAM.get(), level,
                player, start.x, start.y + DEATH_LASER_VERTICAL_OFFSET, start.z, getLaserYaw(player), getLaserPitch(player),
                durationTicks, (float) Math.max(1.0D, damage),
                raycasterConfig().deathLaserHpDamage.get().floatValue());
        laser.setFire(false);
        level.addFreshEntity(laser);
        if (level instanceof ServerLevel serverLevel) {
            ACTIVE_DEATH_LASERS.add(new ActiveDeathLaserSegment(serverLevel.dimension(), player.getUUID(),
                    laser.getUUID(), segmentIndex, durationTicks));
        }
    }

    private static float getLaserYaw(LivingEntity owner) {
        return (float) Math.toRadians(owner.getYRot() + 90.0F);
    }

    private static float getLaserPitch(LivingEntity owner) {
        return (float) Math.toRadians(-owner.getXRot());
    }

    private static Vec3 getLaserLook(LivingEntity owner) {
        Vec3 look = owner.getLookAngle().normalize();
        if (look.lengthSqr() < 1.0E-4D) {
            return Vec3.directionFromRotation(owner.getXRot(), owner.getYRot()).normalize();
        }
        return look;
    }

    private static boolean isBlockedBeforeSegment(Level level, LivingEntity owner, Vec3 from, Vec3 to) {
        BlockHitResult hit = level.clip(new ClipContext(from, to,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, owner));
        return hit.getType() != HitResult.Type.MISS;
    }

    private double getBulletBaseDamage(Level level, Player player, ItemStack ammo, IBullet bulletItem) {
        BulletEntity reference = bulletItem.createProjectile(level, ammo, player);
        double damage = reference.getDamage();
        reference.discard();
        return damage;
    }

    public static void tickScheduledMissiles(ServerLevel level) {
        tickActiveOverloads(level);
        tickPendingDeathLasers(level);
        tickActiveDeathLasers(level);
    }

    private static void tickPendingDeathLasers(ServerLevel level) {
        Iterator<PendingDeathLaser> iterator = PENDING_DEATH_LASERS.iterator();
        while (iterator.hasNext()) {
            PendingDeathLaser pending = iterator.next();
            if (!pending.dimension.equals(level.dimension())) {
                continue;
            }

            Entity ownerEntity = level.getEntity(pending.ownerId);
            if (!(ownerEntity instanceof ServerPlayer owner) || !owner.isAlive()) {
                iterator.remove();
                continue;
            }

            if (pending.ticksLeft-- > 0) {
                continue;
            }

            fireDeathLaser(level, owner, pending.damage, pending.durationTicks);
            iterator.remove();
        }
    }

    private static void startOverloadMode(ServerLevel level, Player player) {
        ActiveOverload existing = null;
        for (ActiveOverload overload : ACTIVE_OVERLOADS) {
            if (overload.dimension.equals(level.dimension()) && overload.ownerId.equals(player.getUUID())) {
                existing = overload;
                break;
            }
        }

        if (existing != null) {
            existing.ticksLeft = overloadDurationTicks();
            existing.missileTicksLeft = Math.max(0, raycasterConfig().missileStartDelay.get());
            grantOverloadFlight(player);
            return;
        }

        ACTIVE_OVERLOADS.add(new ActiveOverload(level.dimension(), player.getUUID(),
                overloadDurationTicks(), Math.max(0, raycasterConfig().missileStartDelay.get()),
                player.getAbilities().mayfly, player.getAbilities().flying));
        grantOverloadFlight(player);
    }

    private static void tickActiveOverloads(ServerLevel level) {
        Iterator<ActiveOverload> iterator = ACTIVE_OVERLOADS.iterator();
        while (iterator.hasNext()) {
            ActiveOverload overload = iterator.next();
            if (!overload.dimension.equals(level.dimension())) {
                continue;
            }

            Entity ownerEntity = level.getEntity(overload.ownerId);
            if (!(ownerEntity instanceof ServerPlayer owner) || !owner.isAlive() || overload.ticksLeft-- <= 0) {
                if (ownerEntity instanceof ServerPlayer owner) {
                    restoreOverloadFlight(owner, overload);
                }
                iterator.remove();
                continue;
            }

            grantOverloadFlight(owner);
            if (overload.missileTicksLeft > 0) {
                overload.missileTicksLeft--;
                continue;
            }

            fireMissileWave(level, owner, overload.waveIndex++,
                    Math.max(0, raycasterConfig().missilesPerWave.get()), null);
            overload.missileTicksLeft = Math.max(1, raycasterConfig().missileIntervalTicks.get());
        }
    }

    private static void grantOverloadFlight(Player player) {
        if (!raycasterConfig().overloadFlightEnabled.get()) {
            return;
        }
        if (!player.getAbilities().mayfly || !player.getAbilities().flying) {
            player.getAbilities().mayfly = true;
            player.getAbilities().flying = true;
            player.onUpdateAbilities();
        }
    }

    private static void restoreOverloadFlight(ServerPlayer player, ActiveOverload overload) {
        if (!raycasterConfig().overloadFlightEnabled.get() || player.getAbilities().instabuild) {
            return;
        }
        player.getAbilities().mayfly = overload.hadMayfly;
        player.getAbilities().flying = overload.hadFlying && overload.hadMayfly;
        player.onUpdateAbilities();
    }

    private static void tickActiveDeathLasers(ServerLevel level) {
        Iterator<ActiveDeathLaserSegment> iterator = ACTIVE_DEATH_LASERS.iterator();
        while (iterator.hasNext()) {
            ActiveDeathLaserSegment segment = iterator.next();
            if (!segment.dimension.equals(level.dimension())) {
                continue;
            }

            Entity ownerEntity = level.getEntity(segment.ownerId);
            Entity laserEntity = level.getEntity(segment.laserId);
            if (!(ownerEntity instanceof LivingEntity owner)
                    || !(laserEntity instanceof Death_Laser_Beam_Entity laser)
                    || !owner.isAlive()
                    || segment.ticksLeft-- <= 0) {
                if (laserEntity instanceof Death_Laser_Beam_Entity laser) {
                    laser.discard();
                }
                iterator.remove();
                continue;
            }

            Vec3 eye = owner.getEyePosition(1.0F);
            Vec3 look = getLaserLook(owner);
            Vec3 start = eye.add(look.scale(raycasterConfig().deathLaserSegmentLength.get() * segment.segmentIndex));
            if (segment.segmentIndex > 0 && isBlockedBeforeSegment(level, owner, eye, start)) {
                laser.discard();
                iterator.remove();
                continue;
            }

            laser.setPos(start.x, start.y + DEATH_LASER_VERTICAL_OFFSET, start.z);
            laser.setYaw(getLaserYaw(owner));
            laser.setPitch(getLaserPitch(owner));
        }
    }

    public static void onRedstoneBulletHeadshot(BulletEntity bullet, LivingEntity target,
                                                @Nullable Entity shooter, Level world) {
        if (!(world instanceof ServerLevel level) || !(shooter instanceof LivingEntity owner)) {
            return;
        }

        CompoundTag persistentData = bullet.getPersistentData();
        if (!persistentData.getBoolean(HARBINGER_REDSTONE_SHOT_TAG)
                || persistentData.getBoolean(HARBINGER_OVERLOAD_SHOT_TAG)
                || isOverloadActiveFor(level, owner)) {
            return;
        }

        fireMissileWave(level, owner, Math.abs(bullet.getId()),
                Math.max(0, raycasterConfig().normalHeadshotMissiles.get()), target);
    }

    private static void fireMissileWave(ServerLevel level, LivingEntity owner, int waveIndex,
                                        int missileCount, @Nullable LivingEntity priorityTarget) {
        if (missileCount <= 0) {
            return;
        }

        List<LivingEntity> targets = findMissileTargets(level, owner);
        if (isValidMissileTarget(owner, priorityTarget)) {
            double targetRange = raycasterConfig().missileTargetRange.get();
            if (priorityTarget.distanceToSqr(owner) <= targetRange * targetRange) {
                targets.removeIf(target -> target == priorityTarget);
                targets.add(0, priorityTarget);
            }
        }
        if (targets.isEmpty()) {
            return;
        }

        float missileDamage = raycasterConfig().missileDamage.get().floatValue();
        double baseAngle = Math.toRadians(owner.getYRot()) + waveIndex * 0.72D;

        for (int i = 0; i < missileCount; i++) {
            LivingEntity target = targets.get((waveIndex * missileCount + i) % targets.size());
            double angle = baseAngle + (Math.PI * 2.0D * i / missileCount);
            double radius = MISSILE_SPAWN_RADIUS + (i % 2) * 0.28D;
            double verticalOffset = (i % 3) * 0.22D;
            Vec3 spawn = owner.position()
                    .add(0.0D, owner.getBbHeight() * 0.65D + 0.25D + verticalOffset, 0.0D)
                    .add(Math.sin(angle) * radius, 0.0D, Math.cos(angle) * radius);
            Vec3 targetCenter = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D);
            Vec3 direction = targetCenter.subtract(spawn).normalize();
            Wither_Homing_Missile_Entity missile = new Wither_Homing_Missile_Entity(owner, direction, level,
                    missileDamage, target);
            missile.setPos(spawn.x, spawn.y, spawn.z);
            missile.setDamage(missileDamage);
            level.addFreshEntity(missile);
        }

        level.playSound(null, owner.getX(), owner.getY(), owner.getZ(),
                ModSounds.ROCKET_LAUNCH.get(), SoundSource.PLAYERS, 0.45F, 1.2F);
    }

    private static List<LivingEntity> findMissileTargets(ServerLevel level, LivingEntity owner) {
        double targetRange = raycasterConfig().missileTargetRange.get();
        AABB area = owner.getBoundingBox().inflate(targetRange, 8.0D, targetRange);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, area,
                target -> isValidMissileTarget(owner, target)
                        && target.distanceToSqr(owner) <= targetRange * targetRange);
        targets.sort(Comparator.comparingDouble(target -> target.distanceToSqr(owner)));
        return targets;
    }

    private static boolean isValidMissileTarget(LivingEntity owner, @Nullable LivingEntity target) {
        return target != null
                && target.isAlive()
                && target != owner
                && !target.isSpectator()
                && !target.isAlliedTo(owner);
    }

    private static boolean isOverloadActiveFor(Level level, LivingEntity owner) {
        for (ActiveOverload overload : ACTIVE_OVERLOADS) {
            if (overload.dimension.equals(level.dimension()) && overload.ownerId.equals(owner.getUUID())) {
                return true;
            }
        }
        return false;
    }

    private static ItemStack snapshotAmmo(ItemStack ammo, IBullet bulletItem) {
        if (!ammo.isEmpty()) {
            return ammo.copyWithCount(1);
        }
        return bulletItem instanceof Item item ? new ItemStack(item) : ammo;
    }

    private static boolean isRedstoneBullet(ItemStack stack) {
        return !stack.isEmpty() && stack.is(VanillaItem.redstone_bullet.get());
    }

    public static int getOverload(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : Mth.clamp(tag.getInt(OVERLOAD_TAG), 0, getMaxOverload());
    }

    public static int getHudOverload(ItemStack stack) {
        return isOverloadActive(stack) ? getMaxOverload() : getOverload(stack);
    }

    private static void setOverload(ItemStack stack, int overload) {
        stack.getOrCreateTag().putInt(OVERLOAD_TAG, Mth.clamp(overload, 0, getMaxOverload()));
    }

    public static boolean isOverloadActive(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getInt(OVERLOAD_ACTIVE_TICKS_TAG) > 0;
    }

    public static int getOverloadActiveTicks(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag == null ? 0 : Math.max(0, tag.getInt(OVERLOAD_ACTIVE_TICKS_TAG));
    }

    private static void setOverloadActiveTicks(ItemStack stack, int ticks) {
        stack.getOrCreateTag().putInt(OVERLOAD_ACTIVE_TICKS_TAG, Math.max(0, ticks));
    }

    private static boolean isOverloaded(ItemStack stack) {
        return getOverload(stack) >= getMaxOverload();
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(OVERLOAD_ACTIVE_TICKS_TAG)) {
            return;
        }

        int ticks = tag.getInt(OVERLOAD_ACTIVE_TICKS_TAG);
        if (ticks > 0) {
            tag.putInt(OVERLOAD_ACTIVE_TICKS_TAG, ticks - 1);
            return;
        }

        tag.remove(OVERLOAD_ACTIVE_TICKS_TAG);
    }

    @Override
    protected void addExtraStatsTooltip(ItemStack stack, @Nullable Level level, List<Component> tooltip) {
        tooltip.add(Component.translatable("tooltip.gwrexpansions.harbinger_raycaster.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.gwrexpansions.harbinger_raycaster.desc2").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.gwrexpansions.harbinger_raycaster.desc3").withStyle(ChatFormatting.GRAY));
        ChatFormatting color = isOverloaded(stack) ? ChatFormatting.RED : ChatFormatting.DARK_AQUA;
        tooltip.add(Component.translatable("tooltip.gwrexpansions.harbinger_raycaster.overload",
                getOverload(stack), getMaxOverload()).withStyle(color));
    }

    private static GWREConfig.HarbingerRaycasterConfig raycasterConfig() {
        return GWREConfig.SNIPER.harbingerRaycaster;
    }

    public static int getMaxOverload() {
        return Math.max(1, raycasterConfig().maxOverload.get());
    }

    public static int getOverloadDurationTicks() {
        return Math.max(1, raycasterConfig().overloadModeDurationTicks.get());
    }

    private static int overloadDurationTicks() {
        return getOverloadDurationTicks();
    }

    private static class ActiveOverload {
        private final ResourceKey<Level> dimension;
        private final UUID ownerId;
        private final boolean hadMayfly;
        private final boolean hadFlying;
        private int ticksLeft;
        private int missileTicksLeft;
        private int waveIndex;

        private ActiveOverload(ResourceKey<Level> dimension, UUID ownerId, int ticksLeft, int missileTicksLeft,
                               boolean hadMayfly, boolean hadFlying) {
            this.dimension = dimension;
            this.ownerId = ownerId;
            this.ticksLeft = ticksLeft;
            this.missileTicksLeft = missileTicksLeft;
            this.hadMayfly = hadMayfly;
            this.hadFlying = hadFlying;
        }
    }

    private static class ActiveDeathLaserSegment {
        private final ResourceKey<Level> dimension;
        private final UUID ownerId;
        private final UUID laserId;
        private final int segmentIndex;
        private int ticksLeft;

        private ActiveDeathLaserSegment(ResourceKey<Level> dimension, UUID ownerId, UUID laserId,
                                        int segmentIndex, int ticksLeft) {
            this.dimension = dimension;
            this.ownerId = ownerId;
            this.laserId = laserId;
            this.segmentIndex = segmentIndex;
            this.ticksLeft = ticksLeft;
        }
    }

    private static class PendingDeathLaser {
        private final ResourceKey<Level> dimension;
        private final UUID ownerId;
        private final double damage;
        private final int durationTicks;
        private int ticksLeft;

        private PendingDeathLaser(ResourceKey<Level> dimension, UUID ownerId, double damage,
                                  int durationTicks, int ticksLeft) {
            this.dimension = dimension;
            this.ownerId = ownerId;
            this.damage = damage;
            this.durationTicks = durationTicks;
            this.ticksLeft = ticksLeft;
        }
    }
}
