package juitar.gwrexpansions.item.cataclysm;

import com.github.L_Ender.cataclysm.entity.effect.Sandstorm_Entity;
import com.github.L_Ender.cataclysm.entity.projectile.Sandstorm_Projectile;
import com.github.L_Ender.cataclysm.init.ModParticle;
import com.github.L_Ender.cataclysm.init.ModSounds;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import juitar.gwrexpansions.config.GWREConfig;
import juitar.gwrexpansions.item.ConfigurableGunItem;
import lykrast.gunswithoutroses.entity.BulletEntity;
import lykrast.gunswithoutroses.item.IBullet;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

public class RemnantFangshotItem extends ConfigurableGunItem {
    public static final String BULLET_TAG = "RemnantFangshotShot";
    public static final String BULLET_SHOT_ID_TAG = "RemnantFangshotShotId";
    public static final String SANDSTORM_SHOT_TAG = "RemnantFangshotPowerSandstorm";
    public static final String SANDSTORM_DAMAGE_TAG = "RemnantFangshotPowerSandstormDamage";
    public static final String SANDSTORM_SHOT_ID_TAG = "RemnantFangshotPowerSandstormShotId";
    private static final String SHOT_ID_TAG = "RemnantFangshotShotId";
    private static final String LAST_HIT_SHOT_ID_TAG = "RemnantFangshotLastHitShotId";
    private static final String EXPECTED_TAG = "RemnantFangshotExpected";
    private static final String CYCLE_TAG = "RemnantFangshotCycle";
    private static final String COMBO_EXPIRE_TAG = "RemnantFangshotComboExpire";
    private static final String BLADE_AMP_TAG = "RemnantFangshotBladeAmp";
    private static final String POWER_TICKS_TAG = "RemnantFangshotPowerTicks";
    private static final String POWER_DASH_USED_TAG = "RemnantFangshotPowerDashUsed";
    private static final String DASH_TICKS_TAG = "RemnantFangshotDashTicks";
    private static final String DASH_DAMAGE_TAG = "RemnantFangshotDashDamage";
    private static final String DASH_HITS_TAG = "RemnantFangshotDashHits";
    private static final String DASH_STORM_ID_TAG = "RemnantFangshotDashStormId";
    public static final String DASH_STORM_TAG = "RemnantFangshotDashStorm";
    public static final String PLAYER_DASH_TICKS_TAG = "RemnantFangshotDashTicks";

    private static final int EXPECT_SHOT = 0;
    private static final int EXPECT_MELEE = 1;
    private static final int DASH_STORM_EXTRA_TICKS = 6;
    private static final int SANDSTORM_PARTICLES_PER_TICK = 36;
    private static final int SAND_GUST_PARTICLES_PER_TICK = 16;
    private static final ResourceLocation IRON_BULLET_ID = new ResourceLocation("gunswithoutroses", "iron_bullet");

    private static final ThreadLocal<Integer> FIRING_SHOT_ID = ThreadLocal.withInitial(() -> 0);
    private static final ThreadLocal<Boolean> FIRING_POWER_SHOT = ThreadLocal.withInitial(() -> false);

    public RemnantFangshotItem(Properties properties, int bonusDamage, double damageMultiplier, int fireDelay,
                               double inaccuracy, int enchantability,
                               Supplier<GWREConfig.GunConfig> configSupplier) {
        super(properties, bonusDamage, damageMultiplier, fireDelay, inaccuracy, enchantability, configSupplier);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            if (!canPowerDash(stack) || getDashTicks(stack) > 0) {
                return InteractionResultHolder.fail(stack);
            }

            if (!level.isClientSide) {
                startDash(level, player, stack);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        return super.use(level, player, hand);
    }

    @Override
    protected void shoot(Level level, Player player, ItemStack gun, ItemStack ammo, IBullet bulletItem, boolean bulletFree) {
        CompoundTag tag = gun.getOrCreateTag();
        int shotId = tag.getInt(SHOT_ID_TAG) + 1;
        tag.putInt(SHOT_ID_TAG, shotId);
        if (isPowered(gun)) {
            shootPowerSandstorms(level, player, gun, ammo, bulletItem, bulletFree, shotId);
            return;
        }

        FIRING_SHOT_ID.set(shotId);
        try {
            super.shoot(level, player, gun, ammo, bulletItem, bulletFree);
        } finally {
            FIRING_SHOT_ID.set(0);
        }
    }

    @Override
    protected void affectBulletEntity(LivingEntity shooter, ItemStack gun, BulletEntity bullet, boolean bulletFree) {
        super.affectBulletEntity(shooter, gun, bullet, bulletFree);
        CompoundTag data = bullet.getPersistentData();
        data.putBoolean(BULLET_TAG, true);
        data.putInt(BULLET_SHOT_ID_TAG, FIRING_SHOT_ID.get());
        if (FIRING_POWER_SHOT.get()) {
            bullet.setDamage(bullet.getDamage() * powerProjectileDamageMultiplier());
        }
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof Player player && !attacker.level().isClientSide) {
            handleBladeHit(stack, target, player);
        }
        stack.hurtAndBreak(1, attacker, entity -> entity.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        return true;
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, net.minecraft.world.level.block.state.BlockState state,
                             net.minecraft.core.BlockPos pos, LivingEntity miner) {
        if (!level.isClientSide && state.getDestroySpeed(level, pos) != 0.0F) {
            stack.hurtAndBreak(2, miner, entity -> entity.broadcastBreakEvent(EquipmentSlot.MAINHAND));
        }
        return true;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        if (slot != EquipmentSlot.MAINHAND) {
            return super.getAttributeModifiers(slot, stack);
        }

        double damage = getCurrentMeleeDamage(stack) - 1.0D;
        double speed = hasBladeAmp(stack) ? ampedAttackSpeedModifier() : baseAttackSpeedModifier();
        ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
        builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID,
                "Remnant Fangshot blade damage", damage, AttributeModifier.Operation.ADDITION));
        builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID,
                "Remnant Fangshot blade speed", speed, AttributeModifier.Operation.ADDITION));
        return builder.build();
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
        return toolAction == ToolActions.SWORD_DIG || toolAction == ToolActions.SWORD_SWEEP
                || super.canPerformAction(stack, toolAction);
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return super.canApplyAtEnchantingTable(stack, enchantment)
                || enchantment == Enchantments.SHARPNESS
                || enchantment == Enchantments.SMITE
                || enchantment == Enchantments.BANE_OF_ARTHROPODS
                || enchantment == Enchantments.KNOCKBACK
                || enchantment == Enchantments.FIRE_ASPECT
                || enchantment == Enchantments.MOB_LOOTING
                || enchantment == Enchantments.SWEEPING_EDGE;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slot, isSelected);
        CompoundTag tag = stack.getOrCreateTag();
        tickDown(tag, BLADE_AMP_TAG);
        int powerTicks = tickDown(tag, POWER_TICKS_TAG);
        if (powerTicks <= 0) {
            tag.remove(POWER_DASH_USED_TAG);
        }
        int comboExpire = tickDown(tag, COMBO_EXPIRE_TAG);
        if (comboExpire <= 0 && (tag.getInt(CYCLE_TAG) > 0 || tag.getInt(EXPECTED_TAG) != EXPECT_SHOT)) {
            resetCombo(tag);
        }

        if (entity instanceof Player player) {
            handleDashTick(stack, level, player);
        }
    }

    @Override
    protected void addExtraStatsTooltip(ItemStack stack, @Nullable Level world, List<Component> tooltip) {
        super.addExtraStatsTooltip(stack, world, tooltip);
        tooltip.add(Component.translatable("tooltip.gwrexpansions.remnant_fangshot.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.gwrexpansions.remnant_fangshot.desc2").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.gwrexpansions.remnant_fangshot.desc3").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.gwrexpansions.remnant_fangshot.power",
                getPowerSeconds(stack)).withStyle(isPowered(stack) ? ChatFormatting.GOLD : ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("tooltip.gwrexpansions.remnant_fangshot.rage",
                getCycle(stack), getMaxRage()).withStyle(ChatFormatting.DARK_GREEN));
        tooltip.add(Component.translatable("tooltip.gwrexpansions.remnant_fangshot.power_charge",
                getPowerChargeState(stack)).withStyle(canPowerDash(stack) ? ChatFormatting.GOLD : ChatFormatting.DARK_GRAY));
    }

    public static void onBulletHit(ItemStack stack, Player player, LivingEntity target, int shotId) {
        CompoundTag tag = stack.getOrCreateTag();
        if (shotId <= 0) {
            return;
        }

        if (tag.getInt(LAST_HIT_SHOT_ID_TAG) == shotId) {
            return;
        }

        tag.putInt(LAST_HIT_SHOT_ID_TAG, shotId);
        if (tag.getInt(EXPECTED_TAG) != EXPECT_SHOT) {
            resetCombo(tag);
        }

        tag.putInt(BLADE_AMP_TAG, bladeAmpTicks());
        tag.putInt(COMBO_EXPIRE_TAG, comboWindowTicks());
        tag.putInt(EXPECTED_TAG, EXPECT_MELEE);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                ModSounds.REMNANT_BITE.get(), SoundSource.PLAYERS, 0.45F, 1.35F);
    }

    public static boolean isDashing(Player player) {
        return player.getPersistentData().getInt(PLAYER_DASH_TICKS_TAG) > 0;
    }

    public static float getDashIncomingDamageMultiplier() {
        return (float) (1.0D - remnantConfig().dashDamageReduction.get());
    }

    public static ItemStack findHeldFangshot(Player player) {
        ItemStack main = player.getMainHandItem();
        if (main.getItem() instanceof RemnantFangshotItem) {
            return main;
        }
        ItemStack off = player.getOffhandItem();
        if (off.getItem() instanceof RemnantFangshotItem) {
            return off;
        }
        return ItemStack.EMPTY;
    }

    private static void handleBladeHit(ItemStack stack, LivingEntity target, Player player) {
        CompoundTag tag = stack.getOrCreateTag();
        boolean charged = player.getAttackStrengthScale(0.5F) >= minFullAttackScale();
        if (isPowered(stack) && charged) {
            triggerPowerStomp(stack, target, player);
        }

        if (tag.getInt(EXPECTED_TAG) != EXPECT_MELEE || !hasBladeAmp(stack) || !charged) {
            resetCombo(tag);
            return;
        }

        tag.putInt(COMBO_EXPIRE_TAG, comboWindowTicks());
        tag.putInt(EXPECTED_TAG, EXPECT_SHOT);
        halveFiringCooldown(stack, player);
        int cycle = tag.getInt(CYCLE_TAG) + 1;
        if (cycle >= getMaxRage()) {
            enterPower(stack, player);
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    ModSounds.REMNANT_ROAR.get(), SoundSource.PLAYERS, 0.8F, 1.1F);
        } else {
            tag.putInt(CYCLE_TAG, cycle);
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    ModSounds.REMNANT_STOMP.get(), SoundSource.PLAYERS, 0.45F, 1.25F);
        }
    }

    private static void startDash(Level level, Player player, ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        int dashTicks = dashTicks();
        tag.putBoolean(POWER_DASH_USED_TAG, true);
        tag.putInt(DASH_TICKS_TAG, dashTicks);
        tag.putDouble(DASH_DAMAGE_TAG, getCurrentMeleeDamage(stack) * dashDamageMultiplier());
        tag.put(DASH_HITS_TAG, new CompoundTag());
        player.getPersistentData().putInt(PLAYER_DASH_TICKS_TAG, dashTicks);
        if (level instanceof ServerLevel serverLevel) {
            getOrCreateDashStorm(stack, serverLevel, player, dashTicks);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                ModSounds.REMNANT_CHARGE_ROAR.get(), SoundSource.PLAYERS, 0.85F, 1.2F);
    }

    private static void handleDashTick(ItemStack stack, Level level, Player player) {
        CompoundTag tag = stack.getOrCreateTag();
        int dashTicks = tag.getInt(DASH_TICKS_TAG);
        if (dashTicks <= 0) {
            if (player.getPersistentData().getInt(PLAYER_DASH_TICKS_TAG) > 0) {
                player.getPersistentData().remove(PLAYER_DASH_TICKS_TAG);
            }
            if (!level.isClientSide) {
                discardDashStorm(tag, level);
            }
            return;
        }

        Vec3 direction = horizontalLook(player);
        player.setDeltaMovement(direction.scale(dashSpeed()).add(0.0D, Math.max(0.0D, player.getDeltaMovement().y), 0.0D));
        player.hurtMarked = true;
        player.fallDistance = 0.0F;

        if (!level.isClientSide) {
            ServerLevel serverLevel = (ServerLevel) level;
            Sandstorm_Entity storm = getOrCreateDashStorm(stack, serverLevel, player, dashTicks);
            damageDashTargets(stack, serverLevel, player, storm);
            spawnDashSandstorm(serverLevel, player, dashTicks);
            if (dashTicks % 3 == 0) {
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        ModSounds.REMNANT_CHARGE_STEP.get(), SoundSource.PLAYERS, 0.55F, 1.25F);
            }
        }

        dashTicks--;
        if (dashTicks <= 0) {
            tag.remove(DASH_TICKS_TAG);
            tag.remove(DASH_DAMAGE_TAG);
            tag.remove(DASH_HITS_TAG);
            player.getPersistentData().remove(PLAYER_DASH_TICKS_TAG);
            if (!level.isClientSide) {
                discardDashStorm(tag, level);
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        ModSounds.REMNANT_SHOCKWAVE.get(), SoundSource.PLAYERS, 0.65F, 1.25F);
            }
        } else {
            tag.putInt(DASH_TICKS_TAG, dashTicks);
            player.getPersistentData().putInt(PLAYER_DASH_TICKS_TAG, dashTicks);
        }
    }

    private static void damageDashTargets(ItemStack stack, ServerLevel level, Player player, Entity damageDealer) {
        CompoundTag tag = stack.getOrCreateTag();
        CompoundTag hitIds = tag.getCompound(DASH_HITS_TAG);
        double damage = tag.getDouble(DASH_DAMAGE_TAG);
        double hitRange = dashHitRange();
        AABB area = damageDealer.getBoundingBox().inflate(hitRange, 0.75D, hitRange);
        DamageSource source = level.damageSources().indirectMagic(damageDealer, player);

        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area,
                target -> target != player && target.isAlive() && !target.isAlliedTo(player))) {
            String key = Integer.toString(target.getId());
            if (hitIds.getBoolean(key)) {
                continue;
            }
            hitIds.putBoolean(key, true);
            int invulnerableTime = target.invulnerableTime;
            target.invulnerableTime = 0;
            boolean damaged = target.hurt(source, (float) damage);
            if (!damaged) {
                target.invulnerableTime = invulnerableTime;
            }
        }
        tag.put(DASH_HITS_TAG, hitIds);
    }

    private static Sandstorm_Entity getOrCreateDashStorm(ItemStack stack, ServerLevel level, Player player, int dashTicks) {
        CompoundTag tag = stack.getOrCreateTag();
        Entity existing = level.getEntity(tag.getInt(DASH_STORM_ID_TAG));
        Sandstorm_Entity storm;
        if (existing instanceof Sandstorm_Entity existingStorm && existingStorm.isAlive()) {
            storm = existingStorm;
        } else {
            storm = new Sandstorm_Entity(level, player.getX(), player.getY(), player.getZ(),
                    dashTicks + DASH_STORM_EXTRA_TICKS, 0.0F, null);
            storm.getPersistentData().putBoolean(DASH_STORM_TAG, true);
            storm.setSilent(true);
            storm.setNoGravity(true);
            level.addFreshEntity(storm);
            tag.putInt(DASH_STORM_ID_TAG, storm.getId());
        }

        storm.setPos(player.getX(), player.getY(), player.getZ());
        storm.setDeltaMovement(Vec3.ZERO);
        storm.setLifespan(Math.max(storm.getLifespan(), dashTicks + DASH_STORM_EXTRA_TICKS));
        storm.getPersistentData().putBoolean(DASH_STORM_TAG, true);
        storm.setSilent(true);
        storm.setNoGravity(true);
        return storm;
    }

    private static void discardDashStorm(CompoundTag tag, Level level) {
        Entity existing = level.getEntity(tag.getInt(DASH_STORM_ID_TAG));
        if (existing instanceof Sandstorm_Entity storm
                && storm.getPersistentData().getBoolean(DASH_STORM_TAG)) {
            storm.discard();
        }
        tag.remove(DASH_STORM_ID_TAG);
    }

    private static void spawnDashSandstorm(ServerLevel level, Player player, int dashTicks) {
        double phase = (dashTicks() - dashTicks) * 0.85D;
        Vec3 base = player.position();
        Vec3 direction = horizontalLook(player);
        Vec3 right = new Vec3(-direction.z, 0.0D, direction.x).normalize();
        BlockParticleOption sand = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SAND.defaultBlockState());

        for (int i = 0; i < SANDSTORM_PARTICLES_PER_TICK; i++) {
            double progress = i / (double) SANDSTORM_PARTICLES_PER_TICK;
            double height = 0.15D + progress * 2.05D;
            double radius = 0.55D + progress * 0.85D;
            double angle = phase + i * 1.35D;
            double x = base.x + Math.cos(angle) * radius;
            double y = base.y + height;
            double z = base.z + Math.sin(angle) * radius;
            double vx = -Math.sin(angle) * 0.055D;
            double vz = Math.cos(angle) * 0.055D;
            level.sendParticles(ModParticle.SANDSTORM.get(), x, y, z, 1, vx, 0.035D, vz, 0.02D);
        }

        for (int i = 0; i < SAND_GUST_PARTICLES_PER_TICK; i++) {
            double progress = i / (double) SAND_GUST_PARTICLES_PER_TICK;
            double height = 0.05D + (i % 4) * 0.28D;
            double side = (i % 2 == 0 ? -1.0D : 1.0D) * (0.45D + progress * 1.1D);
            double back = 0.25D + progress * 1.35D;
            Vec3 pos = base.subtract(direction.scale(back)).add(right.scale(side)).add(0.0D, height, 0.0D);
            level.sendParticles(sand, pos.x, pos.y, pos.z, 3,
                    right.x * side * 0.025D - direction.x * 0.08D,
                    0.06D,
                    right.z * side * 0.025D - direction.z * 0.08D,
                    0.12D);
        }

        Vec3 chest = base.add(0.0D, 1.05D, 0.0D);
        level.sendParticles(ParticleTypes.CLOUD, chest.x, chest.y, chest.z, 10,
                0.42D, 0.42D, 0.42D, 0.02D);
        level.sendParticles(ParticleTypes.POOF, chest.x, base.y + 0.35D, chest.z, 4,
                0.36D, 0.18D, 0.36D, 0.01D);
    }

    private static Vec3 horizontalLook(Player player) {
        Vec3 look = player.getLookAngle();
        Vec3 horizontal = new Vec3(look.x, 0.0D, look.z);
        if (horizontal.lengthSqr() < 1.0E-4D) {
            return Vec3.directionFromRotation(0.0F, player.getYRot()).multiply(1.0D, 0.0D, 1.0D).normalize();
        }
        return horizontal.normalize();
    }

    private static int tickDown(CompoundTag tag, String key) {
        int value = tag.getInt(key);
        if (value <= 0) {
            return 0;
        }
        value--;
        if (value <= 0) {
            tag.remove(key);
        } else {
            tag.putInt(key, value);
        }
        return value;
    }

    private static void resetCombo(CompoundTag tag) {
        tag.putInt(EXPECTED_TAG, EXPECT_SHOT);
        tag.putInt(CYCLE_TAG, 0);
        tag.remove(COMBO_EXPIRE_TAG);
        tag.remove(BLADE_AMP_TAG);
    }

    private static void halveFiringCooldown(ItemStack stack, Player player) {
        if (!(stack.getItem() instanceof RemnantFangshotItem fangshot)) {
            return;
        }

        float cooldownPercent = player.getCooldowns().getCooldownPercent(stack.getItem(), 0.0F);
        if (cooldownPercent <= 0.0F) {
            return;
        }

        int fullDelay = fangshot.getFireDelay(stack, player);
        int remaining = (int) Math.ceil(fullDelay * cooldownPercent);
        double multiplier = cooldownRemainingMultiplier();
        if (multiplier <= 0.0D) {
            player.getCooldowns().removeCooldown(stack.getItem());
            return;
        }

        int reduced = Math.max(1, (int) Math.ceil(remaining * multiplier));
        player.getCooldowns().addCooldown(stack.getItem(), reduced);
    }

    private static GWREConfig.RemnantFangshotConfig remnantConfig() {
        return GWREConfig.SHOTGUN.RemnantFangshot;
    }

    private static int rageRequired() {
        return remnantConfig().rageRequired.get();
    }

    private static int awakenedTicks() {
        return remnantConfig().awakenedTicks.get();
    }

    private static int bladeAmpTicks() {
        return remnantConfig().bladeAmpTicks.get();
    }

    private static int comboWindowTicks() {
        return remnantConfig().comboWindowTicks.get();
    }

    private static double baseMeleeDamage() {
        return remnantConfig().baseMeleeDamage.get();
    }

    private static double bladeDamageBonus() {
        return remnantConfig().bladeDamageBonus.get();
    }

    private static double baseAttackSpeedModifier() {
        return remnantConfig().baseAttackSpeedModifier.get();
    }

    private static double ampedAttackSpeedModifier() {
        return remnantConfig().ampedAttackSpeedModifier.get();
    }

    private static double minFullAttackScale() {
        return remnantConfig().minFullAttackScale.get();
    }

    private static double cooldownRemainingMultiplier() {
        return remnantConfig().cooldownRemainingMultiplier.get();
    }

    private static double powerProjectileDamageMultiplier() {
        return remnantConfig().powerProjectileDamageMultiplier.get();
    }

    private static double powerStompDamageMultiplier() {
        return remnantConfig().powerStompDamageMultiplier.get();
    }

    private static double powerStompRange() {
        return remnantConfig().powerStompRange.get();
    }

    private static int dashTicks() {
        return remnantConfig().dashTicks.get();
    }

    private static double dashDamageMultiplier() {
        return remnantConfig().dashDamageMultiplier.get();
    }

    private static double dashSpeed() {
        return remnantConfig().dashSpeed.get();
    }

    private static double dashHitRange() {
        return remnantConfig().dashHitRange.get();
    }

    private static boolean hasBladeAmp(ItemStack stack) {
        return stack.getOrCreateTag().getInt(BLADE_AMP_TAG) > 0;
    }

    private static int getCycle(ItemStack stack) {
        return stack.getOrCreateTag().getInt(CYCLE_TAG);
    }

    public static int getRage(ItemStack stack) {
        return getCycle(stack);
    }

    public static int getMaxRage() {
        return rageRequired();
    }

    public static int getMaxAwakenedTicks() {
        return awakenedTicks();
    }

    public static int getAwakenedTicks(ItemStack stack) {
        return stack.getOrCreateTag().getInt(POWER_TICKS_TAG);
    }

    public static boolean isAwakened(ItemStack stack) {
        return isPowered(stack);
    }

    public static boolean canUseSandstormCharge(ItemStack stack) {
        return canPowerDash(stack);
    }

    private static int getDashTicks(ItemStack stack) {
        return stack.getOrCreateTag().getInt(DASH_TICKS_TAG);
    }

    private static double getCurrentMeleeDamage(ItemStack stack) {
        return baseMeleeDamage()
                + (hasBladeAmp(stack) ? bladeDamageBonus() : 0.0D);
    }

    private static void enterPower(ItemStack stack, Player player) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(POWER_TICKS_TAG, awakenedTicks());
        tag.putInt(CYCLE_TAG, 0);
        tag.putInt(EXPECTED_TAG, EXPECT_SHOT);
        tag.remove(COMBO_EXPIRE_TAG);
        tag.remove(POWER_DASH_USED_TAG);
        if (player.level() instanceof ServerLevel serverLevel) {
            spawnPowerAwakenParticles(serverLevel, player);
        }
    }

    private static boolean isPowered(ItemStack stack) {
        return stack.getOrCreateTag().getInt(POWER_TICKS_TAG) > 0;
    }

    private static boolean canPowerDash(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        return tag.getInt(POWER_TICKS_TAG) > 0 && !tag.getBoolean(POWER_DASH_USED_TAG);
    }

    private static int getPowerSeconds(ItemStack stack) {
        return (int) Math.ceil(stack.getOrCreateTag().getInt(POWER_TICKS_TAG) / 20.0D);
    }

    private static Component getPowerChargeState(ItemStack stack) {
        String key;
        if (!isPowered(stack)) {
            key = "tooltip.gwrexpansions.remnant_fangshot.power_charge.unavailable";
        } else if (canPowerDash(stack)) {
            key = "tooltip.gwrexpansions.remnant_fangshot.power_charge.ready";
        } else {
            key = "tooltip.gwrexpansions.remnant_fangshot.power_charge.used";
        }
        return Component.translatable(key);
    }

    private void shootPowerSandstorms(Level level, Player player, ItemStack gun, ItemStack ammo,
                                      IBullet bulletItem, boolean bulletFree, int shotId) {
        ItemStack override = overrideFiredStack(player, gun, ammo, bulletItem, bulletFree);
        if (override != ammo && override.getItem() instanceof IBullet overrideBullet) {
            ammo = override;
            bulletItem = overrideBullet;
        }

        ItemStack firedAmmo = snapshotAmmo(ammo, bulletItem);
        if (!isIronBullet(firedAmmo)) {
            fireNormalShotWithId(level, player, gun, ammo, bulletItem, bulletFree, shotId);
            return;
        }

        double damage = getPowerSandstormDamage(level, player, gun, firedAmmo, bulletItem);
        int shots = getProjectilesPerShot(gun, player);
        RandomSource random = level.getRandom();
        for (int i = 0; i < shots; i++) {
            Vec3 direction = addSpread(player.getLookAngle().x, player.getLookAngle().y, player.getLookAngle().z,
                    getInaccuracy(gun, player), random).normalize();
            Sandstorm_Projectile sandstorm = new Sandstorm_Projectile(player,
                    direction.x, direction.y, direction.z, level, 0.0F);
            Vec3 start = player.getEyePosition().add(direction.scale(0.55D)).subtract(0.0D, 0.18D, 0.0D);
            sandstorm.setPos(start.x, start.y, start.z);
            sandstorm.setDeltaMovement(direction.scale(getProjectileSpeed(gun, player)));
            sandstorm.setState(1);
            sandstorm.setSilent(true);
            CompoundTag data = sandstorm.getPersistentData();
            data.putBoolean(SANDSTORM_SHOT_TAG, true);
            data.putDouble(SANDSTORM_DAMAGE_TAG, damage);
            data.putInt(SANDSTORM_SHOT_ID_TAG, shotId);
            level.addFreshEntity(sandstorm);
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                ModSounds.REMNANT_ROAR.get(), SoundSource.PLAYERS, 0.55F, 1.45F);
    }

    private void fireNormalShotWithId(Level level, Player player, ItemStack gun, ItemStack ammo,
                                      IBullet bulletItem, boolean bulletFree, int shotId) {
        FIRING_SHOT_ID.set(shotId);
        FIRING_POWER_SHOT.set(true);
        try {
            super.shoot(level, player, gun, ammo, bulletItem, bulletFree);
        } finally {
            FIRING_SHOT_ID.set(0);
            FIRING_POWER_SHOT.set(false);
        }
    }

    private double getPowerSandstormDamage(Level level, Player player, ItemStack gun, ItemStack ammo, IBullet bulletItem) {
        BulletEntity reference = bulletItem.createProjectile(level, ammo, player);
        double damage = Math.max(0.0D, reference.getDamage() + getBonusDamage(gun, player))
                * getDamageMultiplier(gun, player)
                * powerProjectileDamageMultiplier();
        reference.discard();
        return damage;
    }

    private static ItemStack snapshotAmmo(ItemStack ammo, IBullet bulletItem) {
        if (!ammo.isEmpty()) {
            return ammo.copyWithCount(1);
        }
        return bulletItem instanceof net.minecraft.world.item.Item item ? new ItemStack(item) : ammo;
    }

    private static boolean isIronBullet(ItemStack ammo) {
        net.minecraft.world.item.Item ironBullet = ForgeRegistries.ITEMS.getValue(IRON_BULLET_ID);
        return ironBullet != null && ammo.is(ironBullet);
    }

    private static void triggerPowerStomp(ItemStack stack, LivingEntity primaryTarget, Player player) {
        if (!(player.level() instanceof ServerLevel level)) {
            return;
        }

        double damage = getCurrentMeleeDamage(stack) * powerStompDamageMultiplier();
        DamageSource source = level.damageSources().playerAttack(player);
        double range = powerStompRange();
        AABB area = primaryTarget.getBoundingBox().inflate(range, 0.75D, range);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, area,
                target -> target != player && target.isAlive() && !target.isAlliedTo(player))) {
            int invulnerableTime = target.invulnerableTime;
            target.invulnerableTime = 0;
            boolean damaged = target.hurt(source, (float) damage);
            if (!damaged) {
                target.invulnerableTime = invulnerableTime;
            }
        }

        spawnPowerStompParticles(level, primaryTarget.position());
        level.playSound(null, primaryTarget.getX(), primaryTarget.getY(), primaryTarget.getZ(),
                ModSounds.REMNANT_STOMP.get(), SoundSource.PLAYERS, 0.65F, 0.95F);
        level.playSound(null, primaryTarget.getX(), primaryTarget.getY(), primaryTarget.getZ(),
                ModSounds.REMNANT_SHOCKWAVE.get(), SoundSource.PLAYERS, 0.45F, 1.3F);
    }

    private static void spawnPowerAwakenParticles(ServerLevel level, Player player) {
        Vec3 base = player.position();
        for (int i = 0; i < 54; i++) {
            double angle = i * (Math.PI * 2.0D / 54.0D);
            double radius = 0.55D + (i % 6) * 0.17D;
            level.sendParticles(ModParticle.SANDSTORM.get(),
                    base.x + Math.cos(angle) * radius,
                    base.y + 0.25D + (i % 8) * 0.12D,
                    base.z + Math.sin(angle) * radius,
                    1, -Math.sin(angle) * 0.05D, 0.06D, Math.cos(angle) * 0.05D, 0.02D);
        }
    }

    private static void spawnPowerStompParticles(ServerLevel level, Vec3 center) {
        BlockParticleOption sand = new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SAND.defaultBlockState());
        for (int i = 0; i < 40; i++) {
            double angle = i * (Math.PI * 2.0D / 40.0D);
            double radius = 0.5D + (i % 5) * 0.45D;
            double x = center.x + Math.cos(angle) * radius;
            double z = center.z + Math.sin(angle) * radius;
            level.sendParticles(sand, x, center.y + 0.08D, z, 3,
                    Math.cos(angle) * 0.12D, 0.12D, Math.sin(angle) * 0.12D, 0.16D);
        }
        level.sendParticles(ParticleTypes.POOF, center.x, center.y + 0.35D, center.z, 12,
                1.1D, 0.18D, 1.1D, 0.03D);
    }
}
