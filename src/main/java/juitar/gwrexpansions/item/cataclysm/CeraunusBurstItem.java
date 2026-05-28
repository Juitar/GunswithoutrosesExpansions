package juitar.gwrexpansions.item.cataclysm;

import com.github.L_Ender.cataclysm.entity.effect.Lightning_Area_Effect_Entity;
import com.github.L_Ender.cataclysm.entity.effect.Lightning_Storm_Entity;
import com.github.L_Ender.cataclysm.entity.effect.Wave_Entity;
import com.github.L_Ender.cataclysm.entity.projectile.Lightning_Spear_Entity;
import com.github.L_Ender.cataclysm.entity.projectile.Storm_Serpent_Entity;
import com.github.L_Ender.cataclysm.init.ModEffect;
import com.github.L_Ender.cataclysm.init.ModEntities;
import com.github.L_Ender.cataclysm.init.ModParticle;
import com.github.L_Ender.cataclysm.init.ModSounds;
import juitar.gwrexpansions.config.GWREConfig;
import juitar.gwrexpansions.entity.cataclysm.CeraunusLightningBulletEntity;
import juitar.gwrexpansions.entity.cataclysm.CeraunusStormBulletEntity;
import juitar.gwrexpansions.entity.cataclysm.CeraunusWaterBulletEntity;
import juitar.gwrexpansions.item.ConfigurableBurstGunItem;
import juitar.gwrexpansions.registry.GWRECataclysmEnchantments;
import lykrast.gunswithoutroses.entity.BulletEntity;
import lykrast.gunswithoutroses.item.IBullet;
import lykrast.gunswithoutroses.registry.GWRAttributes;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
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
    public static final String SERPENT_TAG = "CeraunusBurstSerpent";
    public static final String SERPENT_SECONDARY_DAMAGE_TAG = "CeraunusBurstSerpentSecondaryDamage";
    public static final String SERPENT_MODE_TAG = "CeraunusBurstSerpentMode";

    private static final String COUNT_TAG = "CeraunusBurstComboCount";
    private static final String WATER_TAG = "CeraunusBurstWater";
    private static final String STORM_TAG = "CeraunusBurstStorm";
    private static final String LIGHTNING_TAG = "CeraunusBurstLightning";
    private static final String EXPIRE_TAG = "CeraunusBurstExpireGameTime";
    private static final String ELEMENT_SLOT_PREFIX = "CeraunusBurstElement";
    private static final String LAST_COUNT_TAG = "CeraunusBurstLastComboCount";
    private static final String LAST_WATER_TAG = "CeraunusBurstLastWater";
    private static final String LAST_STORM_TAG = "CeraunusBurstLastStorm";
    private static final String LAST_LIGHTNING_TAG = "CeraunusBurstLastLightning";
    private static final String LAST_EXPIRE_TAG = "CeraunusBurstLastExpireGameTime";
    private static final String LAST_ELEMENT_SLOT_PREFIX = "CeraunusBurstLastElement";

    public static final int ELEMENT_NONE = 0;
    public static final int ELEMENT_WATER = 1;
    public static final int ELEMENT_STORM = 2;
    public static final int ELEMENT_LIGHTNING = 3;
    private static final int SERPENT_MODE_NONE = 0;
    private static final int SERPENT_MODE_RANDOM_ELEMENT = 1;
    private static final int SERPENT_MODE_WATER_PULSE = 2;
    private static final int SERPENT_MODE_LIGHTNING_STRIKE = 3;
    private static final double RAY_RANGE = 24.0D;
    private static final int LIGHTNING_SPEAR_INTERVAL_TICKS = 15;
    private static final int LIGHTNING_BULLET_RETARGET_DELAY_TICKS = 8;
    private static final int LIGHTNING_BULLET_STORM_DELAY_TICKS = 0;
    private static final double ELEMENTAL_RESONANCE_BULLET_MULTIPLIER = 1.2D;
    private static final double ELEMENTAL_RESONANCE_COMBO_MULTIPLIER = 1.15D;

    private static final ResourceLocation IRON_BULLET_ID = new ResourceLocation("gunswithoutroses", "iron_bullet");
    private static final ResourceLocation GOLD_BULLET_ID = new ResourceLocation("gwrexpansions", "golden_bullet");
    private static final ResourceLocation DIAMOND_BULLET_ID = new ResourceLocation("gwrexpansions", "diamond_bullet");
    private static final List<PendingCombo> PENDING_COMBOS = new ArrayList<>();
    private static final List<PendingLightningSpear> PENDING_LIGHTNING_SPEARS = new ArrayList<>();
    private static final List<PendingLightningBulletStorm> PENDING_LIGHTNING_BULLET_STORMS = new ArrayList<>();

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
        fireElementBullet(level, player, gun, firedAmmo, bulletItem, bulletFree, element);
        updateCombo(level, player, gun, element, damage);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slot, isSelected);
        if (!level.isClientSide && entity instanceof Player) {
            CompoundTag tag = stack.getOrCreateTag();
            if (tag.getInt(COUNT_TAG) > 0 && level.getGameTime() > tag.getLong(EXPIRE_TAG)) {
                resetCombo(tag);
            }
            if (tag.getInt(LAST_COUNT_TAG) > 0 && level.getGameTime() > tag.getLong(LAST_EXPIRE_TAG)) {
                resetLastCombo(tag);
            }
        }
    }

    @Override
    protected void addExtraStatsTooltip(ItemStack stack, @Nullable Level level, List<Component> tooltip) {
        super.addExtraStatsTooltip(stack, level, tooltip);
        tooltip.add(Component.translatable("tooltip.gwrexpansions.ceraunus_burst.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.gwrexpansions.ceraunus_burst.desc2").withStyle(ChatFormatting.GRAY));
    }

    public static void applyWaterBulletHit(BulletEntity bullet, LivingEntity target, LivingEntity owner, double damage) {
        if (!(bullet.level() instanceof ServerLevel level)) {
            return;
        }

        applyWetness(target, 120, 1);
        level.sendParticles(ParticleTypes.SPLASH, target.getX(), target.getY() + target.getBbHeight() * 0.55D, target.getZ(),
                18, 0.45D, 0.25D, 0.45D, 0.08D);
    }

    public static void applyWaterBulletBlockHit(Level level, Vec3 pos) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        serverLevel.sendParticles(ParticleTypes.SPLASH, pos.x, pos.y + 0.05D, pos.z, 14, 0.45D, 0.1D, 0.45D, 0.07D);
    }

    public static void applyLightningBulletHit(LivingEntity target, LivingEntity owner, double damage) {
        if (!(target.level() instanceof ServerLevel level)) {
            return;
        }

        scheduleLightningBulletStorm(level, target, owner, (float) Math.max(1.0D, damage * 0.72D));
    }

    public static void applyLightningBulletBlockHit(Level level, Vec3 pos, LivingEntity owner, double damage) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        spawnSmallLightningField(serverLevel, groundPoint(serverLevel, pos), owner, (float) Math.max(1.0D, damage * 0.62D));
    }

    public static void applyStormBulletHit(LivingEntity target, LivingEntity owner, double damage) {
        if (!(target.level() instanceof ServerLevel level)) {
            return;
        }

        Vec3 center = target.position().add(0.0D, target.getBbHeight() * 0.45D, 0.0D);
        spawnStormBurst(level, center, owner, (float) Math.max(1.0D, damage * 0.35D), 2.25D);
        if (target.hasEffect(ModEffect.EFFECTWETNESS.get())) {
            spawnWaterPulseAtTarget(level, owner, target, (float) Math.max(1.0D, damage * 0.55D));
        }
        spreadNearbyLightningFields(level, owner, target, (float) Math.max(1.0D, damage * 0.35D));
    }

    public static void applyStormBulletBlockHit(Level level, Vec3 pos, LivingEntity owner, double damage) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        spawnStormBurst(serverLevel, pos, owner, (float) Math.max(1.0D, damage * 0.28D), 1.85D);
    }

    public static void onStormSerpentHit(Storm_Serpent_Entity serpent, LivingEntity target) {
        if (!(serpent.level() instanceof ServerLevel level)
                || !serpent.getPersistentData().getBoolean(SERPENT_TAG)) {
            return;
        }

        int mode = serpent.getPersistentData().getInt(SERPENT_MODE_TAG);
        if (mode == SERPENT_MODE_NONE) {
            return;
        }

        CompoundTag hitTargets = serpent.getPersistentData().getCompound("CeraunusBurstSerpentHits");
        String targetKey = Integer.toString(target.getId());
        if (hitTargets.getBoolean(targetKey)) {
            return;
        }
        hitTargets.putBoolean(targetKey, true);
        serpent.getPersistentData().put("CeraunusBurstSerpentHits", hitTargets);

        LivingEntity owner = serpent.getCaster();
        if (owner == null || owner == target || target.isAlliedTo(owner)) {
            return;
        }

        float damage = Math.max(1.0F, serpent.getPersistentData().getFloat(SERPENT_SECONDARY_DAMAGE_TAG));
        if (mode == SERPENT_MODE_RANDOM_ELEMENT && level.random.nextBoolean()) {
            spawnWaterPulseAtTarget(level, owner, target, damage);
        } else if (mode == SERPENT_MODE_RANDOM_ELEMENT) {
            spawnLightningStorm(level, groundPoint(level, target.position()), owner, damage, 3, 2.1F);
        } else if (mode == SERPENT_MODE_WATER_PULSE) {
            spawnWaterPulseAtTarget(level, owner, target, damage);
        } else if (mode == SERPENT_MODE_LIGHTNING_STRIKE) {
            spawnLightningStorm(level, groundPoint(level, target.position()), owner, damage, 3, 2.1F);
        }
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
                spawnComboWarning(level, owner != null ? owner.position() : combo.center, combo.radius);
                continue;
            }

            triggerCombo(level, owner, combo);
            iterator.remove();
        }
        tickScheduledLightningSpears(level);
        tickScheduledLightningBulletStorms(level);
    }

    private void fireElementBullet(Level level, Player player, ItemStack gun, ItemStack ammo,
                                   IBullet bulletItem, boolean bulletFree, int element) {
        int shots = getProjectilesPerShot(gun, player);
        for (int i = 0; i < shots; ++i) {
            BulletEntity shot = createConvertedBullet(level, player, ammo, bulletItem, element);
            shot.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F,
                    (float) getProjectileSpeed(gun, player), (float) getInaccuracy(gun, player));
            shot.setDamage(Math.max(0.0D, shot.getDamage() + getBonusDamage(gun, player))
                    * getDamageMultiplier(gun, player)
                    * GWREConfig.BURSTGUN.ceraunusBurst.baseElementDamageMultiplier.get()
                    * elementalResonanceBulletMultiplier(gun));
            if (player.getAttribute((Attribute) GWRAttributes.knockback.get()) != null) {
                shot.setKnockbackStrength(shot.getKnockbackStrength()
                        + player.getAttributeValue((Attribute) GWRAttributes.knockback.get()));
            }
            shot.setHeadshotMultiplier(getHeadshotMultiplier(gun, player));
            affectBulletEntity(player, gun, shot, bulletFree);
            level.addFreshEntity(shot);
        }
    }

    private BulletEntity createConvertedBullet(Level level, LivingEntity shooter, ItemStack ammo, IBullet bulletItem, int element) {
        BulletEntity original = bulletItem.createProjectile(level, ammo, shooter);
        BulletEntity converted;
        if (element == ELEMENT_WATER) {
            converted = new CeraunusWaterBulletEntity(level, shooter);
        } else if (element == ELEMENT_STORM) {
            converted = new CeraunusStormBulletEntity(level, shooter);
        } else {
            converted = new CeraunusLightningBulletEntity(level, shooter);
        }

        converted.setItem(ammo.copyWithCount(1));
        converted.setDamage(original.getDamage());
        converted.setWaterInertia(element == ELEMENT_WATER ? Math.max(1.0D, original.getWaterInertia()) : original.getWaterInertia());
        converted.setKnockbackStrength(original.getKnockbackStrength());
        converted.setOwner(shooter);
        original.discard();
        return converted;
    }

    private void updateCombo(Level level, Player player, ItemStack gun, int element, double damage) {
        if (level.isClientSide) {
            return;
        }

        CompoundTag tag = gun.getOrCreateTag();
        if (tag.getInt(COUNT_TAG) > 0 && level.getGameTime() > tag.getLong(EXPIRE_TAG)) {
            resetCombo(tag);
        }

        int slot = tag.getInt(COUNT_TAG);
        tag.putInt(ELEMENT_SLOT_PREFIX + slot, element);
        tag.putInt(COUNT_TAG, slot + 1);
        tag.putLong(EXPIRE_TAG, level.getGameTime() + ceraunusConfig().comboWindowTicks.get());
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
            storeLastCombo(tag, level.getGameTime(), water, storm, lightning);
            resetCombo(tag);
            scheduleCombo((ServerLevel) level, player, findComboCenter(player), water, storm, lightning, damage,
                    elementalResonanceComboMultiplier(gun));
        }
    }

    public static boolean hasComboHud(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && (tag.getInt(COUNT_TAG) > 0 || tag.getInt(LAST_COUNT_TAG) > 0);
    }

    public static int getHudComboCount(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null) {
            return 0;
        }
        int count = tag.getInt(COUNT_TAG);
        return count > 0 ? count : tag.getInt(LAST_COUNT_TAG);
    }

    public static int getHudElement(ItemStack stack, int slot) {
        CompoundTag tag = stack.getTag();
        if (tag == null || slot < 0 || slot >= 3) {
            return ELEMENT_NONE;
        }
        int count = tag.getInt(COUNT_TAG);
        return count > 0 ? tag.getInt(ELEMENT_SLOT_PREFIX + slot) : tag.getInt(LAST_ELEMENT_SLOT_PREFIX + slot);
    }

    public static boolean isHudShowingLastCombo(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getInt(COUNT_TAG) <= 0 && tag.getInt(LAST_COUNT_TAG) > 0;
    }

    private static void scheduleCombo(ServerLevel level, Player player, Vec3 center, int water, int storm, int lightning,
                                      double baseDamage, double enchantmentMultiplier) {
        GWREConfig.CeraunusBurstConfig config = GWREConfig.BURSTGUN.ceraunusBurst;
        PENDING_COMBOS.add(new PendingCombo(level.dimension(), player.getUUID(), center, water, storm, lightning,
                Math.max(0, config.comboDelayTicks.get()), config.comboRadius.get(),
                baseDamage * config.comboDamageMultiplier.get() * enchantmentMultiplier));
        level.playSound(null, center.x, center.y, center.z, ModSounds.SCYLLA_ROAR.get(), SoundSource.PLAYERS, 0.55F, 1.45F);
    }

    private static void triggerCombo(ServerLevel level, @Nullable LivingEntity owner, PendingCombo combo) {
        Vec3 ownerCenter = owner != null ? owner.position() : combo.center;

        int water = combo.water;
        int storm = combo.storm;
        int lightning = combo.lightning;
        if (water == 3) {
            spawnRadialWaves(level, ownerCenter, owner, (float) (combo.damage * 0.5D), 8, pureWaterWaveTicks());
            radialKnock(level, ownerCenter, owner, combo.radius + 1.5D, combo.damage * 0.45D, false);
        } else if (storm == 3) {
            spawnSerpentsAround(level, ownerCenter, owner, 4, stormSerpentBiteDamage(), SERPENT_MODE_RANDOM_ELEMENT);
        } else if (lightning == 3) {
            spawnLightningSpearsAround(level, combo.center, owner, (float) (combo.damage * 1.35D), combo.radius, lightningSpearMax());
        } else if (water == 2 && storm == 1) {
            double waveRadius = combo.radius * 0.82D;
            spawnInwardWaves(level, combo.center, owner, (float) (combo.damage * 0.42D), 8, normalWaveTicks(), waveRadius);
            pullTargets(level, combo.center, owner, combo.radius * 0.9D, 1.05D);
            spawnSerpentsAtInwardWaveStarts(level, combo.center, owner, 2, mixedSerpentBiteDamage(), SERPENT_MODE_NONE, 8, waveRadius * 1.5D);
        } else if (water == 2 && lightning == 1) {
            spawnInwardWaves(level, combo.center, owner, (float) (combo.damage * 0.5D), 8, normalWaveTicks(), combo.radius * 0.82D);
            pullTargets(level, combo.center, owner, combo.radius * 0.9D, 1.0D);
            spawnLightningStorm(level, groundPoint(level, combo.center), owner, (float) (combo.damage * 0.9D), 12, 3.25F);
        } else if (storm == 2 && water == 1) {
            spawnSerpentsAround(level, ownerCenter, owner, 2, stormSerpentBiteDamage(), SERPENT_MODE_WATER_PULSE);
        } else if (storm == 2 && lightning == 1) {
            spawnSerpentsAround(level, ownerCenter, owner, 2, stormSerpentBiteDamage(), SERPENT_MODE_LIGHTNING_STRIKE);
        } else if (lightning == 2 && water == 1) {
            spawnRadialWaves(level, ownerCenter, owner, (float) (combo.damage * 0.25D), 8, normalWaveTicks());
            spawnRandomLightningAround(level, ownerCenter, owner, (float) (combo.damage * 1.12D), combo.radius, 6, 12);
        } else if (lightning == 2 && storm == 1) {
            spawnRandomLightningAround(level, ownerCenter, owner, (float) (combo.damage * 0.95D), combo.radius, 5, 3);
            spawnSerpentsAround(level, ownerCenter, owner, 2, stormSerpentBiteDamage(), SERPENT_MODE_NONE);
        } else {
            spawnCrossOrXWaves(level, ownerCenter, owner, (float) (combo.damage * 0.28D), normalWaveTicks(), level.random.nextBoolean());
            spawnRandomLightningAround(level, ownerCenter, owner, (float) (combo.damage * 0.82D), combo.radius, 4, 6);
            spawnSerpentsAround(level, ownerCenter, owner, 2, mixedSerpentBiteDamage(), SERPENT_MODE_NONE);
        }
    }

    private static void spawnComboWarning(ServerLevel level, Vec3 center, double radius) {
        level.sendParticles(ModParticle.SPARK.get(), center.x, center.y + 0.6D, center.z,
                4, radius * 0.15D, 0.25D, radius * 0.15D, 0.03D);
    }

    private static void spawnRadialWaves(ServerLevel level, Vec3 center, @Nullable LivingEntity owner,
                                         float damage, int count, int maxTicks) {
        Vec3 ground = groundPoint(level, center);
        for (int i = 0; i < count; i++) {
            float yaw = (float) (i * 360.0D / count);
            Wave_Entity wave = new Wave_Entity(level, owner, maxTicks, damage);
            wave.setPos(ground.x, ground.y + 0.05D, ground.z);
            wave.setYRot(yaw);
            level.addFreshEntity(wave);
        }
        level.playSound(null, ground.x, ground.y, ground.z, ModSounds.TIDAL_TENTACLE.get(), SoundSource.PLAYERS, 0.65F, 1.35F);
        level.sendParticles(ParticleTypes.SPLASH, ground.x, ground.y + 0.25D, ground.z, 56, 1.4D, 0.12D, 1.4D, 0.12D);
    }

    private static void spawnInwardWaves(ServerLevel level, Vec3 center, @Nullable LivingEntity owner,
                                         float damage, int count, int maxTicks, double radius) {
        Vec3 groundCenter = groundPoint(level, center);
        double distance = Math.max(2.5D, radius);
        for (int i = 0; i < count; i++) {
            double angle = Math.PI * 2.0D * i / count;
            Vec3 spawn = groundPoint(level, groundCenter.add(Math.cos(angle) * distance, 0.0D, Math.sin(angle) * distance));
            Vec3 inward = groundCenter.subtract(spawn);
            float yaw = (float) (Math.atan2(inward.z, inward.x) * 180.0D / Math.PI) - 90.0F;
            Wave_Entity wave = new Wave_Entity(level, owner, maxTicks, damage);
            wave.setPos(spawn.x, spawn.y + 0.05D, spawn.z);
            wave.setYRot(yaw);
            level.addFreshEntity(wave);
            level.sendParticles(ParticleTypes.SPLASH, spawn.x, spawn.y + 0.25D, spawn.z, 12, 0.45D, 0.06D, 0.45D, 0.06D);
        }
        level.playSound(null, groundCenter.x, groundCenter.y, groundCenter.z, ModSounds.TIDAL_TENTACLE.get(), SoundSource.PLAYERS, 0.7F, 1.15F);
        level.sendParticles(ParticleTypes.SPLASH, groundCenter.x, groundCenter.y + 0.25D, groundCenter.z, 42, distance * 0.25D, 0.1D, distance * 0.25D, 0.08D);
    }

    private static void spawnCrossOrXWaves(ServerLevel level, Vec3 center, @Nullable LivingEntity owner,
                                           float damage, int maxTicks, boolean diagonal) {
        Vec3 ground = groundPoint(level, center);
        float offset = diagonal ? 45.0F : 0.0F;
        for (int i = 0; i < 4; i++) {
            float yaw = offset + i * 90.0F;
            Wave_Entity wave = new Wave_Entity(level, owner, maxTicks, damage);
            wave.setPos(ground.x, ground.y + 0.05D, ground.z);
            wave.setYRot(yaw);
            level.addFreshEntity(wave);
        }
        level.playSound(null, ground.x, ground.y, ground.z, ModSounds.TIDAL_TENTACLE.get(), SoundSource.PLAYERS, 0.58F, 1.35F);
        level.sendParticles(ParticleTypes.SPLASH, ground.x, ground.y + 0.25D, ground.z, 30, 1.0D, 0.1D, 1.0D, 0.08D);
    }

    private static void spawnRandomLightningAround(ServerLevel level, Vec3 center, @Nullable LivingEntity owner,
                                                   float damage, double radius, int count, int baseDelay) {
        for (int i = 0; i < count; i++) {
            double angle = level.random.nextDouble() * Math.PI * 2.0D;
            double distance = 1.0D + level.random.nextDouble() * radius;
            Vec3 pos = center.add(Math.cos(angle) * distance, 0.0D, Math.sin(angle) * distance);
            spawnLightningStorm(level, groundPoint(level, pos), owner, damage * 0.72F, baseDelay + i % 5, 2.35F);
        }

        for (LivingEntity target : findTargets(level, center, owner, radius + 2.0D)) {
            if (level.random.nextFloat() < 0.65F) {
                spawnLightningStorm(level, groundPoint(level, target.position()), owner, damage * 0.58F, baseDelay + 2, 2.0F);
            }
        }
    }

    private static void spawnLightningSpearsAround(ServerLevel level, Vec3 center, @Nullable LivingEntity owner,
                                                   float damage, double radius, int count) {
        if (owner == null) {
            return;
        }

        List<LivingEntity> targets = findTargets(level, center, owner, radius + 7.0D);
        int actual = Math.min(lightningSpearMax(), Math.max(0, count));
        for (int i = 0; i < actual; i++) {
            int targetCount = targets.size();
            @Nullable UUID targetId = null;
            Vec3 fallback = randomLightningSpearFallback(level, center, radius);
            int delay = i * LIGHTNING_SPEAR_INTERVAL_TICKS;
            if (targetCount > 0) {
                LivingEntity target = targets.get(i % targetCount);
                targetId = target.getUUID();
                fallback = target.position();
                delay = i / Math.min(targetCount, actual) * LIGHTNING_SPEAR_INTERVAL_TICKS;
            }
            PENDING_LIGHTNING_SPEARS.add(new PendingLightningSpear(level.dimension(), owner.getUUID(), targetId,
                    center, fallback, damage, radius + 7.0D, delay));
        }
        level.playSound(null, center.x, center.y, center.z, ModSounds.EMP_ACTIVATED.get(), SoundSource.PLAYERS, 0.75F, 1.1F);
    }

    private static void tickScheduledLightningSpears(ServerLevel level) {
        Iterator<PendingLightningSpear> iterator = PENDING_LIGHTNING_SPEARS.iterator();
        while (iterator.hasNext()) {
            PendingLightningSpear pending = iterator.next();
            if (!pending.dimension.equals(level.dimension())) {
                continue;
            }
            if (pending.ticksLeft > 0) {
                pending.ticksLeft--;
                continue;
            }

            LivingEntity owner = getLivingOwner(level, pending.ownerId);
            if (owner != null) {
                spawnScheduledLightningSpear(level, owner, pending);
            }
            iterator.remove();
        }
    }

    private static void spawnScheduledLightningSpear(ServerLevel level, LivingEntity owner, PendingLightningSpear pending) {
        LivingEntity target = findScheduledSpearTarget(level, pending, owner);
        Vec3 spawnPos;
        if (target != null) {
            spawnPos = target.position().add(0.0D, target.getBbHeight() + 4.0D, 0.0D);
        } else {
            Vec3 fallback = groundPoint(level, pending.fallback);
            spawnPos = fallback.add(0.0D, 6.0D, 0.0D);
        }
        spawnLightningSpear(level, owner, spawnPos, pending.damage);
    }

    @Nullable
    private static LivingEntity findScheduledSpearTarget(ServerLevel level, PendingLightningSpear pending, LivingEntity owner) {
        if (pending.targetId != null) {
            Entity entity = level.getEntity(pending.targetId);
            if (entity instanceof LivingEntity target
                    && target.isAlive()
                    && target != owner
                    && !target.isAlliedTo(owner)
                    && target.distanceToSqr(pending.center) <= pending.searchRadius * pending.searchRadius) {
                return target;
            }
        }

        List<LivingEntity> freshTargets = findTargets(level, pending.center, owner, pending.searchRadius);
        return freshTargets.isEmpty() ? null : freshTargets.get(level.random.nextInt(freshTargets.size()));
    }

    private static void spawnLightningSpear(ServerLevel level, LivingEntity owner, Vec3 spawnPos, float damage) {
        try {
            Lightning_Spear_Entity spear = new Lightning_Spear_Entity(ModEntities.LIGHTNING_SPEAR.get(), level);
            spear.setOwner(owner);
            spear.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
            spear.setState(1);
            spear.setDamage(damage);
            spear.setAreaDamage(damage * 0.72F);
            spear.setHpDamage(0.0F);
            spear.setAreaRadius(2.2F);
            spear.accelerationPower = 0.22D;
            spear.setDeltaMovement(0.0D, -spear.accelerationPower, 0.0D);
            level.addFreshEntity(spear);
        } catch (LinkageError error) {
            spawnLightningSpearFallback(level, owner, spawnPos, damage);
            return;
        }
        level.sendParticles(ModParticle.SPARK.get(), spawnPos.x, spawnPos.y, spawnPos.z, 12, 0.4D, 0.25D, 0.4D, 0.08D);
        level.playSound(null, spawnPos.x, spawnPos.y, spawnPos.z, ModSounds.EMP_ACTIVATED.get(), SoundSource.PLAYERS, 0.35F, 1.25F);
    }

    private static void spawnLightningSpearFallback(ServerLevel level, LivingEntity owner, Vec3 spawnPos, float damage) {
        Vec3 ground = groundPoint(level, spawnPos);
        radialKnock(level, ground, owner, 2.2D, damage * 0.72F, true);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, ground.x, ground.y + 0.25D, ground.z,
                24, 0.65D, 0.18D, 0.65D, 0.1D);
        level.playSound(null, ground.x, ground.y, ground.z, ModSounds.EMP_ACTIVATED.get(), SoundSource.PLAYERS, 0.35F, 1.25F);
    }

    private static Vec3 randomLightningSpearFallback(ServerLevel level, Vec3 center, double radius) {
        double angle = level.random.nextDouble() * Math.PI * 2.0D;
        double distance = level.random.nextDouble() * Math.max(1.0D, radius);
        return groundPoint(level, center.add(Math.cos(angle) * distance, 0.0D, Math.sin(angle) * distance));
    }

    private static void scheduleLightningBulletStorm(ServerLevel level, LivingEntity target, LivingEntity owner, float damage) {
        PENDING_LIGHTNING_BULLET_STORMS.add(new PendingLightningBulletStorm(level.dimension(), owner.getUUID(), target.getUUID(),
                target.position(), damage, LIGHTNING_BULLET_RETARGET_DELAY_TICKS));
        level.sendParticles(ModParticle.SPARK.get(), target.getX(), target.getY() + target.getBbHeight() * 0.55D, target.getZ(),
                12, 0.45D, 0.2D, 0.45D, 0.07D);
    }

    private static void tickScheduledLightningBulletStorms(ServerLevel level) {
        Iterator<PendingLightningBulletStorm> iterator = PENDING_LIGHTNING_BULLET_STORMS.iterator();
        while (iterator.hasNext()) {
            PendingLightningBulletStorm pending = iterator.next();
            if (!pending.dimension.equals(level.dimension())) {
                continue;
            }
            if (pending.ticksLeft > 0) {
                pending.ticksLeft--;
                continue;
            }

            LivingEntity owner = getLivingOwner(level, pending.ownerId);
            if (owner != null) {
                Vec3 stormPos = pending.fallback;
                Entity entity = level.getEntity(pending.targetId);
                if (entity instanceof LivingEntity target
                        && target.isAlive()
                        && target != owner
                        && !target.isAlliedTo(owner)) {
                    stormPos = target.position();
                }
                spawnSmallLightningField(level, groundPoint(level, stormPos), owner, pending.damage);
            }
            iterator.remove();
        }
    }

    private static void spawnSerpentsAround(ServerLevel level, Vec3 center, @Nullable LivingEntity owner,
                                            int count, float damage, int mode) {
        int max = Math.max(0, GWREConfig.BURSTGUN.ceraunusBurst.stormSerpentMax.get());
        int actual = Math.min(count, max);
        List<LivingEntity> targets = findTargets(level, center, owner, GWREConfig.BURSTGUN.ceraunusBurst.comboRadius.get() + 8.0D);
        for (int i = 0; i < actual; i++) {
            Vec3 pos = randomSerpentSpawnPoint(level, center);
            LivingEntity target = targets.isEmpty() ? null : targets.get(i % targets.size());
            spawnSerpent(level, groundPoint(level, pos), owner, target, damage, i % 2 == 0, mode);
        }
    }

    private static void spawnSerpentsAtInwardWaveStarts(ServerLevel level, Vec3 center, @Nullable LivingEntity owner,
                                                        int count, float damage, int mode, int waveCount, double radius) {
        int max = Math.max(0, GWREConfig.BURSTGUN.ceraunusBurst.stormSerpentMax.get());
        int actual = Math.min(count, max);
        if (actual <= 0) {
            return;
        }

        Vec3 groundCenter = groundPoint(level, center);
        double distance = Math.max(2.5D, radius);
        List<Integer> unusedStarts = new ArrayList<>();
        for (int i = 0; i < waveCount; i++) {
            unusedStarts.add(i);
        }

        List<LivingEntity> targets = findTargets(level, center, owner, GWREConfig.BURSTGUN.ceraunusBurst.comboRadius.get() + 8.0D);
        for (int i = 0; i < actual; i++) {
            int listIndex = unusedStarts.isEmpty() ? level.random.nextInt(Math.max(1, waveCount)) : level.random.nextInt(unusedStarts.size());
            int waveIndex = unusedStarts.isEmpty() ? listIndex : unusedStarts.remove(listIndex);
            double angle = Math.PI * 2.0D * waveIndex / waveCount;
            Vec3 pos = groundPoint(level, groundCenter.add(Math.cos(angle) * distance, 0.1D, Math.sin(angle) * distance));
            LivingEntity target = targets.isEmpty() ? null : targets.get(i % targets.size());
            spawnSerpent(level, pos, owner, target, damage, i % 2 == 0, mode);
        }
    }

    private static void spawnSerpent(ServerLevel level, Vec3 pos, @Nullable LivingEntity owner,
                                     @Nullable LivingEntity target, float damage, boolean right, int mode) {
        if (owner == null || GWREConfig.BURSTGUN.ceraunusBurst.stormSerpentMax.get() <= 0) {
            spawnStormBurst(level, pos, owner, damage, 2.25D);
            return;
        }

        float yaw = target == null ? 0.0F : (float) Math.atan2(target.getZ() - pos.z, target.getX() - pos.x);
        Storm_Serpent_Entity serpent = new Storm_Serpent_Entity(level, pos.x, pos.y + 0.05D, pos.z, yaw,
                4, owner, damage, target, right);
        serpent.getPersistentData().putBoolean(SERPENT_TAG, true);
        serpent.getPersistentData().putFloat(SERPENT_SECONDARY_DAMAGE_TAG, damage * serpentSecondaryDamageMultiplier());
        serpent.getPersistentData().putInt(SERPENT_MODE_TAG, mode);
        level.addFreshEntity(serpent);
        level.sendParticles(ParticleTypes.CLOUD, pos.x, pos.y + 0.25D, pos.z, 12, 0.55D, 0.12D, 0.55D, 0.04D);
    }

    private static Vec3 randomSerpentSpawnPoint(ServerLevel level, Vec3 center) {
        double angle = level.random.nextDouble() * Math.PI * 2.0D;
        double radius = 1.8D + level.random.nextDouble() * 2.0D;
        Vec3 pos = center.add(Math.cos(angle) * radius, 0.1D, Math.sin(angle) * radius);
        return groundPoint(level, pos);
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

    private static void spawnSmallLightningField(ServerLevel level, Vec3 pos, LivingEntity owner, float damage) {
        Lightning_Storm_Entity strike = new Lightning_Storm_Entity(level, pos.x, pos.y + 0.05D, pos.z,
                0.0F, LIGHTNING_BULLET_STORM_DELAY_TICKS, damage, 0.0F, owner, 1.55F);
        level.addFreshEntity(strike);

        level.sendParticles(ModParticle.SPARK.get(), pos.x, pos.y + 0.2D, pos.z, 14, 0.45D, 0.08D, 0.45D, 0.07D);
        level.playSound(null, pos.x, pos.y, pos.z, ModSounds.EMP_ACTIVATED.get(), SoundSource.PLAYERS, 0.35F, 1.55F);
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

    private static void spreadNearbyLightningFields(ServerLevel level, LivingEntity owner, LivingEntity target, float damage) {
        List<Lightning_Area_Effect_Entity> fields = level.getEntitiesOfClass(Lightning_Area_Effect_Entity.class,
                target.getBoundingBox().inflate(5.0D), Entity::isAlive);
        if (fields.isEmpty()) {
            return;
        }

        Vec3 targetGround = groundPoint(level, target.position());
        for (Lightning_Area_Effect_Entity field : fields) {
            field.setRadius(Math.min(6.0F, field.getRadius() + 1.25F));
            field.setDuration(Math.max(field.getDuration(), 40));
            field.setDamage(Math.max(field.getDamage(), damage * 0.45F));
            level.sendParticles(ModParticle.SPARK.get(), field.getX(), field.getY() + 0.15D, field.getZ(),
                    14, field.getRadius() * 0.18D, 0.08D, field.getRadius() * 0.18D, 0.05D);
        }

        Lightning_Area_Effect_Entity spread = new Lightning_Area_Effect_Entity(level, targetGround.x, targetGround.y + 0.05D, targetGround.z);
        spread.setOwner(owner);
        spread.setRadius(2.2F);
        spread.setDuration(35);
        spread.setDamage(damage * 0.55F);
        level.addFreshEntity(spread);
    }

    private static void spawnWaterPulseAtTarget(ServerLevel level, LivingEntity owner, LivingEntity target, float damage) {
        applyWetness(target, 100, 0);
        Vec3 center = groundPoint(level, target.position());
        for (int i = 0; i < 4; i++) {
            Wave_Entity wave = new Wave_Entity(level, owner, 14, damage);
            wave.setPos(center.x, center.y + 0.05D, center.z);
            wave.setYRot((float) (i * 90.0D));
            level.addFreshEntity(wave);
        }
        radialKnock(level, center, owner, 2.75D, damage * 0.35D, false);
        level.sendParticles(ParticleTypes.SPLASH, center.x, center.y + 0.25D, center.z, 18, 0.75D, 0.08D, 0.75D, 0.08D);
    }

    private static void applyWetness(LivingEntity target, int ticks, int amplifier) {
        target.addEffect(new MobEffectInstance(ModEffect.EFFECTWETNESS.get(), ticks, amplifier, false, true, true));
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

    private static double elementalResonanceBulletMultiplier(ItemStack stack) {
        return GWRECataclysmEnchantments.has(stack, GWRECataclysmEnchantments.ELEMENTAL_RESONANCE)
                ? ELEMENTAL_RESONANCE_BULLET_MULTIPLIER
                : 1.0D;
    }

    private static double elementalResonanceComboMultiplier(ItemStack stack) {
        return GWRECataclysmEnchantments.has(stack, GWRECataclysmEnchantments.ELEMENTAL_RESONANCE)
                ? ELEMENTAL_RESONANCE_COMBO_MULTIPLIER
                : 1.0D;
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

    private static LivingEntity getLivingOwner(ServerLevel level, UUID ownerId) {
        Entity owner = level.getEntity(ownerId);
        return owner instanceof LivingEntity living ? living : null;
    }

    public static double lightningBulletWaterDamageMultiplier() {
        return ceraunusConfig().lightningBulletWaterDamageMultiplier.get();
    }

    private static int normalWaveTicks() {
        return Math.max(1, ceraunusConfig().normalWaveTicks.get());
    }

    private static int pureWaterWaveTicks() {
        return Math.max(1, ceraunusConfig().pureWaterWaveTicks.get());
    }

    private static int lightningSpearMax() {
        return Math.max(0, ceraunusConfig().lightningSpearMax.get());
    }

    private static float stormSerpentBiteDamage() {
        return ceraunusConfig().stormSerpentBiteDamage.get().floatValue();
    }

    private static float mixedSerpentBiteDamage() {
        return ceraunusConfig().mixedSerpentBiteDamage.get().floatValue();
    }

    private static float serpentSecondaryDamageMultiplier() {
        return ceraunusConfig().stormSerpentSecondaryDamageMultiplier.get().floatValue();
    }

    private static GWREConfig.CeraunusBurstConfig ceraunusConfig() {
        return GWREConfig.BURSTGUN.ceraunusBurst;
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
        for (int i = 0; i < 3; i++) {
            tag.remove(ELEMENT_SLOT_PREFIX + i);
        }
    }

    private static void storeLastCombo(CompoundTag tag, long gameTime, int water, int storm, int lightning) {
        tag.putInt(LAST_COUNT_TAG, 3);
        tag.putInt(LAST_WATER_TAG, water);
        tag.putInt(LAST_STORM_TAG, storm);
        tag.putInt(LAST_LIGHTNING_TAG, lightning);
        tag.putLong(LAST_EXPIRE_TAG, gameTime + ceraunusConfig().comboDisplayTicks.get());
        for (int i = 0; i < 3; i++) {
            tag.putInt(LAST_ELEMENT_SLOT_PREFIX + i, tag.getInt(ELEMENT_SLOT_PREFIX + i));
        }
    }

    private static void resetLastCombo(CompoundTag tag) {
        tag.remove(LAST_COUNT_TAG);
        tag.remove(LAST_WATER_TAG);
        tag.remove(LAST_STORM_TAG);
        tag.remove(LAST_LIGHTNING_TAG);
        tag.remove(LAST_EXPIRE_TAG);
        for (int i = 0; i < 3; i++) {
            tag.remove(LAST_ELEMENT_SLOT_PREFIX + i);
        }
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

    private static class PendingLightningSpear {
        private final ResourceKey<Level> dimension;
        private final UUID ownerId;
        @Nullable
        private final UUID targetId;
        private final Vec3 center;
        private final Vec3 fallback;
        private final float damage;
        private final double searchRadius;
        private int ticksLeft;

        private PendingLightningSpear(ResourceKey<Level> dimension, UUID ownerId, @Nullable UUID targetId,
                                      Vec3 center, Vec3 fallback, float damage, double searchRadius, int ticksLeft) {
            this.dimension = dimension;
            this.ownerId = ownerId;
            this.targetId = targetId;
            this.center = center;
            this.fallback = fallback;
            this.damage = damage;
            this.searchRadius = searchRadius;
            this.ticksLeft = ticksLeft;
        }
    }

    private static class PendingLightningBulletStorm {
        private final ResourceKey<Level> dimension;
        private final UUID ownerId;
        private final UUID targetId;
        private final Vec3 fallback;
        private final float damage;
        private int ticksLeft;

        private PendingLightningBulletStorm(ResourceKey<Level> dimension, UUID ownerId, UUID targetId,
                                            Vec3 fallback, float damage, int ticksLeft) {
            this.dimension = dimension;
            this.ownerId = ownerId;
            this.targetId = targetId;
            this.fallback = fallback;
            this.damage = damage;
            this.ticksLeft = ticksLeft;
        }
    }
}
