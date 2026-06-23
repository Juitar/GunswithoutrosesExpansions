package juitar.gwrexpansions.item.cataclysm;

import juitar.gwrexpansions.advancement.FirstTidalPortalCreatedTrigger;
import juitar.gwrexpansions.config.GWREConfig;
import juitar.gwrexpansions.entity.cataclysm.TidalAbyssBlastPortalEntity;
import juitar.gwrexpansions.entity.cataclysm.TidalRiftEntity;
import juitar.gwrexpansions.item.ConfigurableGunItem;
import juitar.gwrexpansions.item.GunSkillItem;
import juitar.gwrexpansions.item.GunSkillTooltip;
import juitar.gwrexpansions.registry.CompatCataclysm;
import juitar.gwrexpansions.registry.GWRECataclysmEnchantments;
import juitar.gwrexpansions.registry.GWREEntities;
import lykrast.gunswithoutroses.entity.BulletEntity;
import lykrast.gunswithoutroses.item.IBullet;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;


import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class TidalGunItem extends ConfigurableGunItem implements GunSkillItem {
    private static final String ENERGY_TAG = "TidalEnergy";
    private static final String CHARGE_TICKS_TAG = "TidalChargeTicks";
    private static final String SKILL_CHARGING_TAG = "TidalSkillCharging";
    private static final String HUD_TICKS_TAG = "TidalHudTicks";
    private static final String LAND_ORB_COOLDOWN_TAG = "TidalLandOrbCooldown";
    private static final String LAND_MINE_COOLDOWN_TAG = "TidalLandMineCooldown";
    private static final String FULL_ORB_COOLDOWN_TAG = "TidalFullOrbCooldown";
    private static final String FULL_MINE_COOLDOWN_TAG = "TidalFullMineCooldown";
    private static final String TENTACLE_COOLDOWN_TAG = "TidalTentacleCooldown";
    private static final String ENTITY_LAND_ORB_COOLDOWN_TAG = "GWRETidalLandOrbCooldownUntil";
    private static final String ENTITY_LAND_MINE_COOLDOWN_TAG = "GWRETidalLandMineCooldownUntil";
    private static final String ENTITY_FULL_ORB_COOLDOWN_TAG = "GWRETidalFullOrbCooldownUntil";
    private static final String ENTITY_FULL_MINE_COOLDOWN_TAG = "GWRETidalFullMineCooldownUntil";
    private static final String ENTITY_TENTACLE_COOLDOWN_TAG = "GWRETidalTentacleCooldownUntil";
    private static final String PORTAL_PRIORITY_TARGET_TAG = "GWRETidalPortalPriorityTarget";
    private static final String PORTAL_PRIORITY_TARGET_TIME_TAG = "GWRETidalPortalPriorityTargetTime";
    private static final double AIM_RANGE = 48.0D;
    private static final double ABYSSAL_CHARGE_HIT_ENERGY_MULTIPLIER = 1.5D;
    private static final int PORTAL_PRIORITY_TARGET_WINDOW_TICKS = 60;

    public TidalGunItem(Properties properties, int bonusDamage, double damageMultiplier, int fireDelay, double inaccuracy, int enchantability, Supplier<GWREConfig.GunConfig> configSupplier) {
        super(properties, bonusDamage, damageMultiplier, fireDelay, inaccuracy, enchantability, configSupplier);
    }

    @Override
    protected ItemStack overrideFiredStack(LivingEntity shooter, ItemStack gun, ItemStack ammo, IBullet bulletItem, boolean bulletFree) {
        if (ammo.is(CompatCataclysm.tagBaseBullets)) return new ItemStack(CompatCataclysm.tidal_bullet.get());
        else return ammo;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return super.use(level, player, hand);
    }

    @Override
    public boolean canUseGunSkill(ServerPlayer player, InteractionHand hand, ItemStack stack) {
        return stack.getItem() instanceof TidalGunItem
                && (isSkillCharging(stack)
                || isFullForm(player.level(), player) && !player.getCooldowns().isOnCooldown(this));
    }

    @Override
    public void useGunSkill(ServerPlayer player, InteractionHand hand, ItemStack stack) {
        Level level = player.level();
        if (!isFullForm(level, player)) {
            return;
        }

        CompoundTag tag = stack.getOrCreateTag();
        tag.putBoolean(SKILL_CHARGING_TAG, true);
        tag.putInt(CHARGE_TICKS_TAG, 0);
        tag.putInt(HUD_TICKS_TAG, 10);
    }

    @Override
    public void releaseGunSkill(ServerPlayer player, InteractionHand hand, ItemStack stack) {
        if (!isSkillCharging(stack)) {
            clearCharge(stack);
            return;
        }

        releaseChargedSkill(stack, player.level(), player, getHudChargeTicks(stack));
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int remainingUseDuration) {
        if (!(entity instanceof Player player)) {
            clearCharge(stack);
            return;
        }

        int usedTicks = getUseDuration(stack) - remainingUseDuration;
        releaseChargedSkill(stack, level, player, usedTicks);
    }

    private void releaseChargedSkill(ItemStack stack, Level level, Player player, int usedTicks) {
        clearCharge(stack);
        GWREConfig.TidalPistolConfig tidal = tidalConfig();
        boolean fullForm = isFullForm(level, player);

        if (usedTicks < tidal.portalChargeTicks.get()) {
            level.playSound(null, player.blockPosition(), SoundEvents.TRIDENT_HIT_GROUND, SoundSource.PLAYERS, 0.25F, 1.45F);
            return;
        }

        if (!fullForm) {
            level.playSound(null, player.blockPosition(), SoundEvents.TRIDENT_HIT_GROUND, SoundSource.PLAYERS, 0.45F, 0.7F);
            return;
        }

        Vec3 target = getAimedPoint(level, player);
        if (usedTicks >= tidal.riftChargeTicks.get() && getEnergy(stack) >= tidal.maxEnergy.get()
                && consumeEnergy(stack, tidal.riftCost.get())) {
            spawnRift(level, player, target);
            player.getCooldowns().addCooldown(this, getFireDelay(stack, player) + 20);
            return;
        }

        if (consumeEnergy(stack, tidal.portalCost.get())) {
            spawnPortal(level, player, target);
            player.getCooldowns().addCooldown(this, getFireDelay(stack, player) + 10);
        } else {
            level.playSound(null, player.blockPosition(), SoundEvents.TRIDENT_HIT_GROUND, SoundSource.PLAYERS, 0.45F, 0.8F);
        }
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        super.onUseTick(level, entity, stack, remainingUseDuration);
        if (entity instanceof Player) {
            int usedTicks = getUseDuration(stack) - remainingUseDuration;
            CompoundTag tag = stack.getOrCreateTag();
            tag.putInt(CHARGE_TICKS_TAG, usedTicks);
            tag.putInt(HUD_TICKS_TAG, 10);
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slot, isSelected);
        if (!(entity instanceof Player player)) {
            return;
        }

        CompoundTag tag = stack.getOrCreateTag();
        tickCooldown(tag, LAND_ORB_COOLDOWN_TAG);
        tickCooldown(tag, LAND_MINE_COOLDOWN_TAG);
        tickCooldown(tag, FULL_ORB_COOLDOWN_TAG);
        tickCooldown(tag, FULL_MINE_COOLDOWN_TAG);
        tickCooldown(tag, TENTACLE_COOLDOWN_TAG);
        tickCooldown(tag, HUD_TICKS_TAG);

        boolean held = isSelected || player.getMainHandItem() == stack || player.getOffhandItem() == stack;
        if (!held && isSkillCharging(stack)) {
            clearCharge(stack);
        }

        if (!level.isClientSide && level.getGameTime() % 20L == 0L) {
            int regen = tidalConfig().inventoryRegenPerSecond.get();
            if (held) {
                regen = player.isInWaterOrBubble()
                        ? tidalConfig().heldWaterRegenPerSecond.get()
                        : tidalConfig().heldLandRegenPerSecond.get();
            }
            addEnergy(stack, regen);
        }

        if (!level.isClientSide && held && isSkillCharging(stack)) {
            if (isFullForm(level, player)) {
                tag.putInt(CHARGE_TICKS_TAG, tag.getInt(CHARGE_TICKS_TAG) + 1);
                tag.putInt(HUD_TICKS_TAG, 10);
            } else {
                clearCharge(stack);
                level.playSound(null, player.blockPosition(), SoundEvents.TRIDENT_HIT_GROUND, SoundSource.PLAYERS, 0.45F, 0.7F);
            }
        }

        if (!level.isClientSide && held && isFullForm(level, player)) {
            player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 60, 0, true, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 60, 0, true, false, true));
        }
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    protected void addExtraStatsTooltip(ItemStack stack, @Nullable Level world, List<Component> tooltip){
        tooltip.add(Component.translatable("tooltip.gwrexpansions.tidal_pistol.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.gwrexpansions.tidal_pistol.desc2").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.gwrexpansions.tidal_pistol.desc3").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.gwrexpansions.tidal_pistol.desc4",
                GunSkillTooltip.keyName()).withStyle(ChatFormatting.GRAY));
    }

    public static GWREConfig.TidalPistolConfig tidalConfig() {
        return GWREConfig.PISTOL.tidal;
    }

    public static int getMaxEnergy() {
        return tidalConfig().maxEnergy.get();
    }

    public static int getEnergy(ItemStack stack) {
        if (!(stack.getItem() instanceof TidalGunItem)) {
            return 0;
        }
        CompoundTag tag = stack.getOrCreateTag();
        int max = getMaxEnergy();
        if (!tag.contains(ENERGY_TAG)) {
            tag.putInt(ENERGY_TAG, max);
        }
        int energy = Math.min(max, Math.max(0, tag.getInt(ENERGY_TAG)));
        tag.putInt(ENERGY_TAG, energy);
        return energy;
    }

    public static int addEnergy(ItemStack stack, int amount) {
        if (!(stack.getItem() instanceof TidalGunItem) || amount <= 0) {
            return getEnergy(stack);
        }
        CompoundTag tag = stack.getOrCreateTag();
        int energy = Math.min(getMaxEnergy(), getEnergy(stack) + amount);
        tag.putInt(ENERGY_TAG, energy);
        tag.putInt(HUD_TICKS_TAG, 40);
        return energy;
    }

    public static boolean consumeEnergy(ItemStack stack, int amount) {
        if (!(stack.getItem() instanceof TidalGunItem)) {
            return false;
        }
        int energy = getEnergy(stack);
        if (energy < amount) {
            return false;
        }
        stack.getOrCreateTag().putInt(ENERGY_TAG, energy - amount);
        stack.getOrCreateTag().putInt(HUD_TICKS_TAG, 40);
        return true;
    }

    public static ItemStack findHeldTidalPistol(LivingEntity entity) {
        ItemStack mainHand = entity.getMainHandItem();
        if (mainHand.getItem() instanceof TidalGunItem) {
            return mainHand;
        }

        ItemStack offHand = entity.getOffhandItem();
        if (offHand.getItem() instanceof TidalGunItem) {
            return offHand;
        }

        return ItemStack.EMPTY;
    }

    public static void addEnergyToHeld(LivingEntity entity, int amount) {
        ItemStack stack = findHeldTidalPistol(entity);
        if (!stack.isEmpty()) {
            addEnergy(stack, modifiedHitEnergy(stack, amount));
        }
    }

    private static int modifiedHitEnergy(ItemStack stack, int amount) {
        if (!GWRECataclysmEnchantments.has(stack, GWRECataclysmEnchantments.ABYSSAL_CHARGE)) {
            return amount;
        }
        return Math.max(1, (int) Math.ceil(amount * ABYSSAL_CHARGE_HIT_ENERGY_MULTIPLIER));
    }

    public static boolean isFullForm(Level level, LivingEntity entity) {
        return entity.isInWaterOrBubble() || level.isRainingAt(entity.blockPosition());
    }

    public static boolean consumeHeldEnergy(LivingEntity entity, int amount) {
        ItemStack stack = findHeldTidalPistol(entity);
        return !stack.isEmpty() && consumeEnergy(stack, amount);
    }

    public static boolean hasLandCooldown(LivingEntity entity, boolean mine) {
        return hasEchoCooldown(entity, mine, false);
    }

    public static void setLandCooldown(LivingEntity entity, boolean mine, int ticks) {
        setEchoCooldown(entity, mine, false, ticks);
    }

    public static boolean hasEchoCooldown(LivingEntity entity, boolean mine, boolean fullForm) {
        ItemStack stack = findHeldTidalPistol(entity);
        if (stack.isEmpty()) {
            return entity.getPersistentData().getLong(cooldownEntityTag(mine, fullForm))
                    > entity.level().getGameTime();
        }
        return stack.getOrCreateTag().getInt(cooldownItemTag(mine, fullForm)) > 0;
    }

    public static void setEchoCooldown(LivingEntity entity, boolean mine, boolean fullForm, int ticks) {
        ItemStack stack = findHeldTidalPistol(entity);
        if (!stack.isEmpty() && ticks > 0) {
            stack.getOrCreateTag().putInt(cooldownItemTag(mine, fullForm), ticks);
            stack.getOrCreateTag().putInt(HUD_TICKS_TAG, 40);
            return;
        }

        if (ticks > 0) {
            entity.getPersistentData().putLong(cooldownEntityTag(mine, fullForm),
                    entity.level().getGameTime() + ticks);
        }
    }

    private static String cooldownItemTag(boolean mine, boolean fullForm) {
        if (fullForm) {
            return mine ? FULL_MINE_COOLDOWN_TAG : FULL_ORB_COOLDOWN_TAG;
        }
        return mine ? LAND_MINE_COOLDOWN_TAG : LAND_ORB_COOLDOWN_TAG;
    }

    private static String cooldownEntityTag(boolean mine, boolean fullForm) {
        if (fullForm) {
            return mine ? ENTITY_FULL_MINE_COOLDOWN_TAG : ENTITY_FULL_ORB_COOLDOWN_TAG;
        }
        return mine ? ENTITY_LAND_MINE_COOLDOWN_TAG : ENTITY_LAND_ORB_COOLDOWN_TAG;
    }

    public static boolean hasTentacleCooldown(LivingEntity entity) {
        ItemStack stack = findHeldTidalPistol(entity);
        if (stack.isEmpty()) {
            return entity.getPersistentData().getLong(ENTITY_TENTACLE_COOLDOWN_TAG) > entity.level().getGameTime();
        }
        return stack.getOrCreateTag().getInt(TENTACLE_COOLDOWN_TAG) > 0;
    }

    public static void setTentacleCooldown(LivingEntity entity, int ticks) {
        ItemStack stack = findHeldTidalPistol(entity);
        if (!stack.isEmpty() && ticks > 0) {
            stack.getOrCreateTag().putInt(TENTACLE_COOLDOWN_TAG, ticks);
            stack.getOrCreateTag().putInt(HUD_TICKS_TAG, 40);
            return;
        }

        if (ticks > 0) {
            entity.getPersistentData().putLong(ENTITY_TENTACLE_COOLDOWN_TAG, entity.level().getGameTime() + ticks);
        }
    }

    public static void rememberPortalPriorityTarget(Player player, LivingEntity target) {
        if (player.level().isClientSide || target == player || !target.isAlive() || target.isSpectator()) {
            return;
        }
        if (player.isAlliedTo(target) || target.isAlliedTo(player)) {
            return;
        }

        CompoundTag data = player.getPersistentData();
        data.putUUID(PORTAL_PRIORITY_TARGET_TAG, target.getUUID());
        data.putLong(PORTAL_PRIORITY_TARGET_TIME_TAG, player.level().getGameTime());
    }

    @Nullable
    private static LivingEntity getRecentPortalPriorityTarget(Player player) {
        CompoundTag data = player.getPersistentData();
        if (!data.hasUUID(PORTAL_PRIORITY_TARGET_TAG)) {
            return null;
        }
        long recordedAt = data.getLong(PORTAL_PRIORITY_TARGET_TIME_TAG);
        if (player.level().getGameTime() - recordedAt > PORTAL_PRIORITY_TARGET_WINDOW_TICKS) {
            data.remove(PORTAL_PRIORITY_TARGET_TAG);
            data.remove(PORTAL_PRIORITY_TARGET_TIME_TAG);
            return null;
        }
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return null;
        }

        UUID targetId = data.getUUID(PORTAL_PRIORITY_TARGET_TAG);
        Entity entity = serverLevel.getEntity(targetId);
        return entity instanceof LivingEntity target && isValidPortalPriorityTarget(player, target) ? target : null;
    }

    private static boolean isValidPortalPriorityTarget(Player player, LivingEntity target) {
        return target != player && target.isAlive() && !target.isSpectator()
                && !player.isAlliedTo(target) && !target.isAlliedTo(player)
                && target.getVehicle() != player && player.getVehicle() != target
                && (target.getVehicle() == null || target.getVehicle() != player.getVehicle());
    }

    public static int getHudChargeTicks(ItemStack stack) {
        return stack.getOrCreateTag().getInt(CHARGE_TICKS_TAG);
    }

    public static boolean hasEnergyHud(ItemStack stack) {
        return stack.getItem() instanceof TidalGunItem
                && (getEnergy(stack) < getMaxEnergy()
                || stack.getOrCreateTag().getInt(HUD_TICKS_TAG) > 0
                || stack.getOrCreateTag().getInt(CHARGE_TICKS_TAG) > 0);
    }

    private static void tickCooldown(CompoundTag tag, String key) {
        int ticks = tag.getInt(key);
        if (ticks > 0) {
            tag.putInt(key, ticks - 1);
        }
    }

    private static void clearCharge(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(CHARGE_TICKS_TAG, 0);
        tag.remove(SKILL_CHARGING_TAG);
        tag.putInt(HUD_TICKS_TAG, 30);
    }

    private static boolean isSkillCharging(ItemStack stack) {
        return stack.getOrCreateTag().getBoolean(SKILL_CHARGING_TAG);
    }

    private static Vec3 getAimedPoint(Level level, Player player) {
        HitResult result = player.pick(AIM_RANGE, 1.0F, true);
        if (result != null && result.getType() != HitResult.Type.MISS) {
            return result.getLocation();
        }

        Vec3 eye = player.getEyePosition(1.0F);
        return eye.add(player.getViewVector(1.0F).scale(AIM_RANGE));
    }

    private static void spawnPortal(Level level, Player player, Vec3 target) {
        if (level.isClientSide) {
            return;
        }

        TidalAbyssBlastPortalEntity portal = new TidalAbyssBlastPortalEntity(GWREEntities.TIDAL_ABYSS_BLAST_PORTAL.get(), level);
        Vec3 portalPos = target.add(player.getLookAngle().scale(-0.35D));
        portal.setPos(portalPos.x, portalPos.y, portalPos.z);
        portal.configure(player, target, tidalConfig().portalWarmupTicks.get(),
                80, tidalConfig().portalDamage.get().floatValue(), tidalConfig().portalHpDamage.get().floatValue());
        portal.setPriorityTarget(getRecentPortalPriorityTarget(player));
        level.addFreshEntity(portal);
        level.playSound(null, target.x, target.y, target.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.8F, 1.15F);

        if (player instanceof ServerPlayer serverPlayer) {
            FirstTidalPortalCreatedTrigger.onFirstTidalPortalCreated(serverPlayer);
        }
    }

    private static void spawnRift(Level level, Player player, Vec3 target) {
        if (level.isClientSide) {
            return;
        }

        TidalRiftEntity rift = new TidalRiftEntity(GWREEntities.TIDAL_RIFT.get(), level);
        rift.setOwner(player);
        rift.setPos(target.x, target.y, target.z);
        rift.configure(tidalConfig().riftDurationTicks.get(), tidalConfig().riftRadius.get(),
                tidalConfig().riftDamage.get().floatValue(), tidalConfig().riftPullStrength.get(),
                tidalConfig().riftDamageIntervalTicks.get());
        level.addFreshEntity(rift);
        level.playSound(null, target.x, target.y, target.z, SoundEvents.RESPAWN_ANCHOR_DEPLETE.value(), SoundSource.PLAYERS, 0.9F, 0.65F);
    }
}
