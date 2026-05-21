package juitar.gwrexpansions.item.cataclysm;

import com.github.L_Ender.cataclysm.entity.effect.Lightning_Storm_Entity;
import com.github.L_Ender.cataclysm.entity.effect.Wave_Entity;
import com.github.L_Ender.cataclysm.entity.projectile.Lightning_Spear_Entity;
import com.github.L_Ender.cataclysm.entity.projectile.Storm_Serpent_Entity;
import com.github.L_Ender.cataclysm.entity.projectile.Water_Spear_Entity;
import com.github.L_Ender.cataclysm.init.ModParticle;
import com.github.L_Ender.cataclysm.init.ModSounds;
import juitar.gwrexpansions.config.GWREConfig;
import juitar.gwrexpansions.item.ConfigurableBurstGunItem;
import lykrast.gunswithoutroses.entity.BulletEntity;
import lykrast.gunswithoutroses.item.IBullet;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class CeraunusBurstItem extends ConfigurableBurstGunItem {
    public static final String STORM_SHOT_TAG = "CeraunusBurstStormShot";
    public static final String STORM_DAMAGE_TAG = "CeraunusBurstStormDamage";

    private static final String COUNT_TAG = "CeraunusBurstComboCount";
    private static final String WATER_TAG = "CeraunusBurstWater";
    private static final String STORM_TAG = "CeraunusBurstStorm";
    private static final String LIGHTNING_TAG = "CeraunusBurstLightning";
    private static final String EXPIRE_TAG = "CeraunusBurstExpireGameTime";

    private static final int ELEMENT_NONE = 0;
    private static final int ELEMENT_WATER = 1;
    private static final int ELEMENT_STORM = 2;
    private static final int ELEMENT_LIGHTNING = 3;
    private static final int COMBO_WINDOW_TICKS = 80;
    private static final double RAY_RANGE = 24.0D;

    private static final ResourceLocation IRON_BULLET_ID = new ResourceLocation("gunswithoutroses", "iron_bullet");
    private static final ResourceLocation GOLD_BULLET_ID = new ResourceLocation("gwrexpansions", "golden_bullet");
    private static final ResourceLocation DIAMOND_BULLET_ID = new ResourceLocation("gwrexpansions", "diamond_bullet");
    private static final ThreadLocal<Double> FIRING_STORM_DAMAGE = ThreadLocal.withInitial(() -> 0.0D);
    private static final List<PendingCombo> PENDING_COMBOS = new ArrayList<>();

    public CeraunusBurstItem(Properties properties, int bonusDamage, double damageMultiplier, int fireDelay,
                             double inaccuracy, int enchantability, int burstSize, int burstFireDelay,
                             Supplier<GWREConfig.BurstgunConfig> configSupplier) {
        super(properties, bonusDamage, damageMultiplier, fireDelay, inaccuracy, enchantability, burstSize, burstFireDelay,
                configSupplier);
    }

    @Override
    protected void shoot(Level level, Player player, ItemStack gun, ItemStack ammo, IBullet bulletItem, boolean bulletFree) {
        ItemStack override = overrideFiredStack(player, gun, ammo, bulletItem, bulletFree);
        if (override != ammo && override.getItem() instanceof IBullet overrideBullet) {
            ammo = override;
            bulletItem = overrideBullet;
        }

        ItemStack firedAmmo = snapshotAmmo(ammo, bulletItem);
        int element = resolveElement(firedAmmo);
        if (element == ELEMENT_NONE) {
            resetCombo(gun.getOrCreateTag());
            super.shoot(level, player, gun, ammo, bulletItem, bulletFree);
            return;
        }

        double damage = getElementDamage(level, player, gun, firedAmmo, bulletItem);
        if (element == ELEMENT_WATER) {
            fireWaterSpears(level, player, gun, damage);
        } else if (element == ELEMENT_LIGHTNING) {
            fireLightningSpears(level, player, gun, damage);
        } else {
            fireStormBullets(level, player, gun, ammo, bulletItem, bulletFree, damage);
        }

        updateCombo(level, player, gun, element, damage);
    }

    @Override
    protected void affectBulletEntity(LivingEntity shooter, ItemStack gun, BulletEntity shot, boolean bulletFree) {
        super.affectBulletEntity(shooter, gun, shot, bulletFree);
        double stormDamage = FIRING_STORM_DAMAGE.get();
        if (stormDamage > 0.0D) {
            shot.getPersistentData().putBoolean(STORM_SHOT_TAG, true);
            shot.getPersistentData().putDouble(STORM_DAMAGE_TAG, stormDamage);
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slot, isSelected);
        if (!level.isClientSide && entity instanceof Player) {
            CompoundTag tag = stack.getOrCreateTag();
            if (tag.getInt(COUNT_TAG) > 0 && level.getGameTime() > tag.getLong(EXPIRE_TAG)) {
                resetCombo(tag);
            }
        }
    }

    @Override
    protected void addExtraStatsTooltip(ItemStack stack, @Nullable Level level, List<Component> tooltip) {
        super.addExtraStatsTooltip(stack, level, tooltip);
        tooltip.add(Component.translatable("tooltip.gwrexpansions.ceraunus_burst.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.gwrexpansions.ceraunus_burst.desc2").withStyle(ChatFormatting.GRAY));
    }

    public static void onStormShotHit(BulletEntity bullet, LivingEntity target, Entity shooter) {
        if (!(bullet.level() instanceof ServerLevel level) || !(shooter instanceof LivingEntity owner)) {
            return;
        }

        double damage = bullet.getPersistentData().getDouble(STORM_DAMAGE_TAG);
        Vec3 center = target.position().add(0.0D, target.getBbHeight() * 0.45D, 0.0D);
        spawnStormBurst(level, center, owner, (float) Math.max(1.0D, damage * 0.45D), 2.5D);
        spawnSerpent(level, center.add(0.0D, 0.15D, 0.0D), owner, target, (float) Math.max(1.0D, damage * 0.6D), false);
    }

    public static void tickScheduledCombos(ServerLevel level) {
        Iterator<PendingCombo> iterator = PENDING_COMBOS.iterator();
        while (iterator.hasNext()) {
            PendingCombo combo = iterator.next();
            if (!combo.dimension.equals(level.dimension())) {
                continue;
            }

            LivingEntity owner = getLivingOwner(level, combo.ownerId);
            if (combo.ticksLeft > 0) {
                combo.ticksLeft--;
                spawnAnchorWarning(level, combo.center, combo.radius);
                continue;
            }

            triggerCombo(level, owner, combo);
            iterator.remove();
        }
    }

    private void fireWaterSpears(Level level, Player player, ItemStack gun, double damage) {
        int shots = getProjectilesPerShot(gun, player);
        RandomSource random = level.getRandom();
        for (int i = 0; i < shots; i++) {
            Vec3 direction = addSpread(player.getLookAngle().x, player.getLookAngle().y, player.getLookAngle().z,
                    getInaccuracy(gun, player), random).normalize();
            Water_Spear_Entity spear = new Water_Spear_Entity(player, direction, level, (float) damage);
            Vec3 start = player.getEyePosition().add(direction.scale(0.55D)).subtract(0.0D, 0.12D, 0.0D);
            spear.setPos(start.x, start.y, start.z);
            spear.accelerationPower = Math.max(0.08D, getProjectileSpeed(gun, player) * 0.06D);
            spear.setTotalBounces(1);
            level.addFreshEntity(spear);
        }
    }

    private void fireLightningSpears(Level level, Player player, ItemStack gun, double damage) {
        int shots = getProjectilesPerShot(gun, player);
        RandomSource random = level.getRandom();
        for (int i = 0; i < shots; i++) {
            Vec3 direction = addSpread(player.getLookAngle().x, player.getLookAngle().y, player.getLookAngle().z,
                    getInaccuracy(gun, player), random).normalize();
            Lightning_Spear_Entity spear = new Lightning_Spear_Entity(player, direction, level, (float) damage);
            Vec3 start = player.getEyePosition().add(direction.scale(0.55D)).subtract(0.0D, 0.12D, 0.0D);
            spear.setPos(start.x, start.y, start.z);
            spear.accelerationPower = Math.max(0.08D, getProjectileSpeed(gun, player) * 0.06D);
            spear.setAreaDamage((float) (damage * 0.45D));
            spear.setAreaRadius(2.75F);
            spear.setHpDamage(0.0F);
            level.addFreshEntity(spear);
        }
    }

    private void fireStormBullets(Level level, Player player, ItemStack gun, ItemStack ammo,
                                  IBullet bulletItem, boolean bulletFree, double damage) {
        FIRING_STORM_DAMAGE.set(damage);
        try {
            super.shoot(level, player, gun, ammo, bulletItem, bulletFree);
        } finally {
            FIRING_STORM_DAMAGE.set(0.0D);
        }
    }

    private void updateCombo(Level level, Player player, ItemStack gun, int element, double damage) {
        if (level.isClientSide) {
            return;
        }

        CompoundTag tag = gun.getOrCreateTag();
        if (tag.getInt(COUNT_TAG) > 0 && level.getGameTime() > tag.getLong(EXPIRE_TAG)) {
            resetCombo(tag);
        }

        tag.putInt(COUNT_TAG, tag.getInt(COUNT_TAG) + 1);
        tag.putLong(EXPIRE_TAG, level.getGameTime() + COMBO_WINDOW_TICKS);
        if (element == ELEMENT_WATER) {
            tag.putInt(WATER_TAG, tag.getInt(WATER_TAG) + 1);
        } else if (element == ELEMENT_STORM) {
            tag.putInt(STORM_TAG, tag.getInt(STORM_TAG) + 1);
        } else if (element == ELEMENT_LIGHTNING) {
            tag.putInt(LIGHTNING_TAG, tag.getInt(LIGHTNING_TAG) + 1);
        }

        if (tag.getInt(COUNT_TAG) >= 3) {
            int water = tag.getInt(WATER_TAG);
            int storm = tag.getInt(STORM_TAG);
            int lightning = tag.getInt(LIGHTNING_TAG);
            resetCombo(tag);
            scheduleCombo((ServerLevel) level, player, findComboCenter(player), water, storm, lightning, damage);
        }
    }

    private static void scheduleCombo(ServerLevel level, Player player, Vec3 center, int water, int storm, int lightning, double baseDamage) {
        GWREConfig.CeraunusBurstConfig config = GWREConfig.BURSTGUN.ceraunusBurst;
        PENDING_COMBOS.add(new PendingCombo(level.dimension(), player.getUUID(), center, water, storm, lightning,
                Math.max(1, config.anchorDelay.get()), config.comboRadius.get(), baseDamage * config.comboDamageMultiplier.get()));
        level.playSound(null, center.x, center.y, center.z, ModSounds.SCYLLA_ROAR.get(), SoundSource.PLAYERS, 0.55F, 1.45F);
    }

    private static void triggerCombo(ServerLevel level, @Nullable LivingEntity owner, PendingCombo combo) {
        Vec3[] anchors = anchorPositions(combo.center, combo.radius);
        for (Vec3 anchor : anchors) {
            Vec3 ground = groundPoint(level, anchor);
            anchorImpact(level, ground, owner, (float) (combo.damage * 0.55D), combo.radius * 0.34D);
        }

        int water = combo.water;
        int storm = combo.storm;
        int lightning = combo.lightning;
        if (water == 3) {
            spawnAnchorWaves(level, combo.center, anchors, owner, (float) (combo.damage * 0.55D));
            radialKnock(level, combo.center, owner, combo.radius, combo.damage * 0.35D, false);
        } else if (storm == 3) {
            spawnSerpentsAround(level, combo.center, owner, 4, (float) (combo.damage * 0.65D));
        } else if (lightning == 3) {
            spawnLightningAtAnchors(level, anchors, combo.center, owner, (float) combo.damage, 5);
        } else if (water == 2 && storm == 1) {
            pullTargets(level, combo.center, owner, combo.radius * 0.85D, 0.75D);
            spawnAnchorWaves(level, combo.center, anchors, owner, (float) (combo.damage * 0.42D));
            spawnSerpentsAround(level, combo.center, owner, 1, (float) (combo.damage * 0.62D));
        } else if (water == 2 && lightning == 1) {
            spawnAnchorWaves(level, combo.center, anchors, owner, (float) (combo.damage * 0.48D));
            spawnLightningAtAnchors(level, anchors, combo.center, owner, (float) (combo.damage * 0.62D), 7);
        } else if (storm == 2 && water == 1) {
            spawnSerpentsAround(level, combo.center, owner, 2, (float) (combo.damage * 0.58D));
            spawnAnchorWaves(level, combo.center, anchors, owner, (float) (combo.damage * 0.35D));
        } else if (storm == 2 && lightning == 1) {
            spawnSerpentsAround(level, combo.center, owner, 2, (float) (combo.damage * 0.72D));
            spawnLightningStorm(level, combo.center, owner, (float) (combo.damage * 0.65D), 10, 2.75F);
        } else if (lightning == 2 && water == 1) {
            spawnLightningStorm(level, combo.center, owner, (float) (combo.damage * 0.85D), 4, 3.25F);
            spawnAnchorWaves(level, combo.center, anchors, owner, (float) (combo.damage * 0.38D));
        } else if (lightning == 2 && storm == 1) {
            spawnLightningAtAnchors(level, anchors, combo.center, owner, (float) (combo.damage * 0.45D), 4);
            spawnSerpentsAround(level, combo.center, owner, 2, (float) (combo.damage * 0.62D));
        } else {
            spawnAnchorWaves(level, combo.center, anchors, owner, (float) (combo.damage * 0.36D));
            spawnSerpentsAround(level, combo.center, owner, 2, (float) (combo.damage * 0.48D));
            spawnLightningStorm(level, combo.center, owner, (float) (combo.damage * 0.72D), 8, 3.0F);
        }
    }

    private static void anchorImpact(ServerLevel level, Vec3 pos, @Nullable LivingEntity owner, float damage, double range) {
        level.playSound(null, pos.x, pos.y, pos.z, ModSounds.HEAVY_SMASH.get(), SoundSource.PLAYERS, 0.75F, 1.05F);
        level.sendParticles(ModParticle.SPARK.get(), pos.x, pos.y + 0.25D, pos.z, 22, 0.65D, 0.18D, 0.65D, 0.08D);
        level.sendParticles(ModParticle.SHOCK_WAVE.get(), pos.x, pos.y + 0.08D, pos.z, 1, 0.0D, 0.0D, 0.0D, 0.0D);
        radialKnock(level, pos, owner, range, damage, true);
    }

    private static void spawnAnchorWarning(ServerLevel level, Vec3 center, double radius) {
        for (Vec3 anchor : anchorPositions(center, radius)) {
            Vec3 ground = groundPoint(level, anchor);
            level.sendParticles(ModParticle.SPARK.get(), ground.x, ground.y + 0.1D, ground.z,
                    3, 0.22D, 0.03D, 0.22D, 0.02D);
            for (int i = 0; i < 4; i++) {
                level.sendParticles(ParticleTypes.CRIT, ground.x, ground.y + 0.8D + i * 0.75D, ground.z,
                        1, 0.04D, -0.35D, 0.04D, 0.02D);
            }
        }
    }

    private static void spawnAnchorWaves(ServerLevel level, Vec3 center, Vec3[] anchors, @Nullable LivingEntity owner, float damage) {
        for (Vec3 anchor : anchors) {
            Vec3 ground = groundPoint(level, anchor);
            Vec3 outward = anchor.subtract(center);
            float yaw = (float) (Math.atan2(outward.z, outward.x) * 180.0D / Math.PI) - 90.0F;
            Wave_Entity wave = new Wave_Entity(level, owner, 18, damage);
            wave.setPos(ground.x, ground.y + 0.05D, ground.z);
            wave.setYRot(yaw);
            level.addFreshEntity(wave);
            level.sendParticles(ParticleTypes.SPLASH, ground.x, ground.y + 0.25D, ground.z, 18, 0.8D, 0.08D, 0.8D, 0.08D);
        }
    }

    private static void spawnLightningAtAnchors(ServerLevel level, Vec3[] anchors, Vec3 center,
                                                @Nullable LivingEntity owner, float damage, int delay) {
        for (Vec3 anchor : anchors) {
            spawnLightningStorm(level, groundPoint(level, anchor), owner, damage * 0.65F, delay, 2.25F);
        }
        spawnLightningStorm(level, groundPoint(level, center), owner, damage, delay + 5, 3.25F);
    }

    private static void spawnSerpentsAround(ServerLevel level, Vec3 center, @Nullable LivingEntity owner, int count, float damage) {
        int max = Math.max(0, GWREConfig.BURSTGUN.ceraunusBurst.stormSerpentMax.get());
        int actual = Math.min(count, max);
        List<LivingEntity> targets = findTargets(level, center, owner, GWREConfig.BURSTGUN.ceraunusBurst.comboRadius.get() + 8.0D);
        for (int i = 0; i < actual; i++) {
            double angle = (Math.PI * 2.0D * i) / Math.max(1, actual);
            Vec3 pos = center.add(Math.cos(angle) * 2.5D, 0.1D, Math.sin(angle) * 2.5D);
            LivingEntity target = targets.isEmpty() ? null : targets.get(i % targets.size());
            spawnSerpent(level, groundPoint(level, pos), owner, target, damage, i % 2 == 0);
        }
    }

    private static void spawnSerpent(ServerLevel level, Vec3 pos, @Nullable LivingEntity owner,
                                     @Nullable LivingEntity target, float damage, boolean right) {
        if (owner == null || GWREConfig.BURSTGUN.ceraunusBurst.stormSerpentMax.get() <= 0) {
            spawnStormBurst(level, pos, owner, damage, 2.25D);
            return;
        }

        float yaw = target == null ? 0.0F : (float) (Math.atan2(target.getZ() - pos.z, target.getX() - pos.x) * 180.0D / Math.PI) - 90.0F;
        Storm_Serpent_Entity serpent = new Storm_Serpent_Entity(level, pos.x, pos.y + 0.05D, pos.z, yaw,
                4, owner, damage, target, right);
        level.addFreshEntity(serpent);
        level.sendParticles(ParticleTypes.CLOUD, pos.x, pos.y + 0.25D, pos.z, 12, 0.55D, 0.12D, 0.55D, 0.04D);
    }

    private static void spawnStormBurst(ServerLevel level, Vec3 center, @Nullable LivingEntity owner, float damage, double range) {
        level.playSound(null, center.x, center.y, center.z, ModSounds.SCYLLA_ROAR.get(), SoundSource.PLAYERS, 0.35F, 1.65F);
        level.sendParticles(ParticleTypes.CLOUD, center.x, center.y + 0.2D, center.z, 22, range * 0.35D, 0.2D, range * 0.35D, 0.08D);
        level.sendParticles(ModParticle.SPARK.get(), center.x, center.y + 0.45D, center.z, 14, range * 0.25D, 0.18D, range * 0.25D, 0.06D);
        radialKnock(level, center, owner, range, damage, false);
    }

    private static void spawnLightningStorm(ServerLevel level, Vec3 pos, @Nullable LivingEntity owner,
                                            float damage, int delay, float size) {
        Lightning_Storm_Entity storm = new Lightning_Storm_Entity(level, pos.x, pos.y + 0.05D, pos.z,
                0.0F, delay, damage, 0.0F, owner, size);
        level.addFreshEntity(storm);
        level.sendParticles(ModParticle.SPARK.get(), pos.x, pos.y + 0.25D, pos.z, 16, 0.55D, 0.1D, 0.55D, 0.08D);
        level.playSound(null, pos.x, pos.y, pos.z, ModSounds.EMP_ACTIVATED.get(), SoundSource.PLAYERS, 0.45F, 1.35F);
    }

    private static void radialKnock(ServerLevel level, Vec3 center, @Nullable LivingEntity owner,
                                    double range, double damage, boolean upward) {
        DamageSource source = comboDamageSource(level, owner);
        for (LivingEntity target : findTargets(level, center, owner, range)) {
            int invulnerableTime = target.invulnerableTime;
            target.invulnerableTime = 0;
            boolean damaged = target.hurt(source, (float) damage);
            if (!damaged) {
                target.invulnerableTime = invulnerableTime;
            }
            Vec3 push = target.position().subtract(center);
            if (push.lengthSqr() > 1.0E-4D) {
                Vec3 normalized = push.normalize().scale(0.45D);
                target.push(normalized.x, upward ? 0.25D : 0.08D, normalized.z);
                target.hurtMarked = true;
            }
        }
    }

    private static void pullTargets(ServerLevel level, Vec3 center, @Nullable LivingEntity owner, double range, double strength) {
        for (LivingEntity target : findTargets(level, center, owner, range)) {
            Vec3 pull = center.subtract(target.position());
            if (pull.lengthSqr() > 1.0E-4D) {
                Vec3 motion = pull.normalize().scale(strength);
                target.push(motion.x, 0.08D, motion.z);
                target.hurtMarked = true;
            }
        }
        level.sendParticles(ParticleTypes.SPLASH, center.x, center.y + 0.25D, center.z, 42, range * 0.35D, 0.1D, range * 0.35D, 0.08D);
    }

    private static List<LivingEntity> findTargets(ServerLevel level, Vec3 center, @Nullable LivingEntity owner, double range) {
        AABB area = new AABB(center, center).inflate(range, 2.0D, range);
        return level.getEntitiesOfClass(LivingEntity.class, area,
                target -> target.isAlive()
                        && target != owner
                        && (owner == null || !target.isAlliedTo(owner))
                        && target.distanceToSqr(center) <= range * range);
    }

    private double getElementDamage(Level level, Player player, ItemStack gun, ItemStack ammo, IBullet bulletItem) {
        BulletEntity reference = bulletItem.createProjectile(level, ammo, player);
        double damage = Math.max(0.0D, reference.getDamage() + getBonusDamage(gun, player))
                * getDamageMultiplier(gun, player)
                * GWREConfig.BURSTGUN.ceraunusBurst.baseElementDamageMultiplier.get();
        reference.discard();
        return damage;
    }

    private static DamageSource comboDamageSource(ServerLevel level, @Nullable LivingEntity owner) {
        if (owner instanceof Player player) {
            return level.damageSources().playerAttack(player);
        }
        if (owner != null) {
            return level.damageSources().mobAttack(owner);
        }
        return level.damageSources().magic();
    }

    private static Vec3 findComboCenter(Player player) {
        Level level = player.level();
        Vec3 eye = player.getEyePosition(1.0F);
        Vec3 look = player.getLookAngle();
        Vec3 end = eye.add(look.scale(RAY_RANGE));
        BlockHitResult blockHit = level.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        double blockDistance = blockHit.getType() == HitResult.Type.BLOCK ? eye.distanceToSqr(blockHit.getLocation()) : RAY_RANGE * RAY_RANGE;
        AABB area = player.getBoundingBox().expandTowards(look.scale(RAY_RANGE)).inflate(1.0D);
        EntityHitResult entityHit = net.minecraft.world.entity.projectile.ProjectileUtil.getEntityHitResult(level, player, eye, end, area,
                entity -> entity.isPickable() && entity != player && !entity.isSpectator());
        if (entityHit != null && eye.distanceToSqr(entityHit.getLocation()) <= blockDistance) {
            return entityHit.getLocation();
        }
        if (blockHit.getType() == HitResult.Type.BLOCK) {
            return blockHit.getLocation();
        }
        return end;
    }

    private static Vec3 groundPoint(ServerLevel level, Vec3 pos) {
        Vec3 top = pos.add(0.0D, 8.0D, 0.0D);
        Vec3 bottom = pos.subtract(0.0D, 14.0D, 0.0D);
        BlockHitResult hit = level.clip(new ClipContext(top, bottom, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, null));
        if (hit.getType() == HitResult.Type.BLOCK) {
            return hit.getLocation();
        }

        BlockPos blockPos = BlockPos.containing(pos);
        BlockState state = level.getBlockState(blockPos);
        if (!state.isAir()) {
            return Vec3.atCenterOf(blockPos.above());
        }
        return pos;
    }

    private static Vec3[] anchorPositions(Vec3 center, double radius) {
        double offset = Math.max(1.5D, radius * 0.45D);
        return new Vec3[] {
                center.add(offset, 0.0D, 0.0D),
                center.add(-offset, 0.0D, 0.0D),
                center.add(0.0D, 0.0D, offset),
                center.add(0.0D, 0.0D, -offset)
        };
    }

    private static LivingEntity getLivingOwner(ServerLevel level, UUID ownerId) {
        Entity owner = level.getEntity(ownerId);
        return owner instanceof LivingEntity living ? living : null;
    }

    private static ItemStack snapshotAmmo(ItemStack ammo, IBullet bulletItem) {
        if (!ammo.isEmpty()) {
            return ammo.copyWithCount(1);
        }
        return bulletItem instanceof Item item ? new ItemStack(item) : ammo;
    }

    private static int resolveElement(ItemStack ammo) {
        if (ammo.isEmpty()) {
            return ELEMENT_NONE;
        }
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(ammo.getItem());
        if (IRON_BULLET_ID.equals(id)) {
            return ELEMENT_WATER;
        }
        if (GOLD_BULLET_ID.equals(id)) {
            return ELEMENT_STORM;
        }
        if (DIAMOND_BULLET_ID.equals(id)) {
            return ELEMENT_LIGHTNING;
        }
        return ELEMENT_NONE;
    }

    private static void resetCombo(CompoundTag tag) {
        tag.remove(COUNT_TAG);
        tag.remove(WATER_TAG);
        tag.remove(STORM_TAG);
        tag.remove(LIGHTNING_TAG);
        tag.remove(EXPIRE_TAG);
    }

    private static class PendingCombo {
        private final ResourceKey<Level> dimension;
        private final UUID ownerId;
        private final Vec3 center;
        private final int water;
        private final int storm;
        private final int lightning;
        private final double radius;
        private final double damage;
        private int ticksLeft;

        private PendingCombo(ResourceKey<Level> dimension, UUID ownerId, Vec3 center,
                             int water, int storm, int lightning, int ticksLeft, double radius, double damage) {
            this.dimension = dimension;
            this.ownerId = ownerId;
            this.center = center;
            this.water = water;
            this.storm = storm;
            this.lightning = lightning;
            this.ticksLeft = ticksLeft;
            this.radius = radius;
            this.damage = damage;
        }
    }
}
