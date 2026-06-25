package juitar.gwrexpansions.item.BOMD;

import juitar.gwrexpansions.advancement.BOMD.AvadaKedavraTrigger;
import juitar.gwrexpansions.config.GWREConfig;
import juitar.gwrexpansions.entity.BOMD.ObsidianCoreEntity;
import juitar.gwrexpansions.item.ConfigurableLauncherItem;
import juitar.gwrexpansions.item.GunSkillItem;
import juitar.gwrexpansions.item.GunSkillTooltip;
import juitar.gwrexpansions.registry.CompatBOMD;
import juitar.gwrexpansions.registry.GWREEntities;
import juitar.gwrexpansions.registry.GWRESounds;
import lykrast.gunswithoutroses.item.IBullet;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class ObsidianLauncher extends ConfigurableLauncherItem implements GunSkillItem {
    private static final int BASE_MIN_RANGE = 15;
    private static final int BASE_MAX_RANGE = 30;
    private static final int MAX_RANGE_WITH_FULL_CHARGE = 45;
    private static final int MAX_CHARGE_TIME = 40;
    private static final int HALF_CHARGE_TICKS = 20;
    private static final int NORMAL_MAX_ACTIVE_CORES = 1;

    public static final String NBT_LAUNCHER_ID = "ObsidianLauncherId";
    public static final String NBT_ACTIVE_CORES = "ActiveObsidianCores";
    public static final String NBT_FRENZY_TICKS = "ObsidianFrenzyTicks";
    private static final String NBT_LEGACY_FRENZY_SHOTS = "ObsidianFrenzyShots";
    public static final String NBT_FIRE_SPELL_CAST = "FireSpellCast";
    public static final String NBT_FROST_SPELL_CAST = "FrostSpellCast";
    public static final String NBT_HOLY_SPELL_CAST = "HolySpellCast";
    private static final String NBT_FIRE_SPELL_DISCOVERED = "FireSpellDiscovered";
    private static final String NBT_FROST_SPELL_DISCOVERED = "FrostSpellDiscovered";
    private static final String NBT_HOLY_SPELL_DISCOVERED = "HolySpellDiscovered";

    public ObsidianLauncher(Properties properties, int bonusDamage, double damageMultiplier, int fireDelay,
                            double inaccuracy, int enchantability, Supplier<GWREConfig.GunConfig> configSupplier) {
        super(properties, bonusDamage, damageMultiplier, fireDelay, inaccuracy, enchantability, configSupplier);
    }

    @Override
    protected ItemStack findAmmo(Player player) {
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty() && stack.getItem().equals(CompatBOMD.ObsidianCore.get())) {
                return stack;
            }
        }

        if (!player.getOffhandItem().isEmpty() && player.getOffhandItem().getItem().equals(CompatBOMD.ObsidianCore.get())) {
            return player.getOffhandItem();
        }

        return ItemStack.EMPTY;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        ensureLauncherId(stack);
        boolean frenzied = isFrenzied(stack);

        if (frenzied) {
            player.getCooldowns().removeCooldown(this);
        }

        if ((!frenzied && player.getCooldowns().isOnCooldown(this)) || !canFire(stack)) {
            return InteractionResultHolder.fail(stack);
        }

        ItemStack ammo = findAmmo(player);
        boolean bulletFree = player.getAbilities().instabuild || !shouldConsumeAmmo(stack, player);
        if (ammo.isEmpty() && !bulletFree) {
            return InteractionResultHolder.fail(stack);
        }

        if (!level.isClientSide) {
            if (!bulletFree) {
                ammo.shrink(1);
            }

            CompoundTag tag = stack.getOrCreateTag();
            tag.putInt(NBT_ACTIVE_CORES, Math.min(NORMAL_MAX_ACTIVE_CORES, getActiveCores(stack) + 1));
            tag.putInt("UseTicks", HALF_CHARGE_TICKS);

            shoot(level, player, stack, ammo, null, bulletFree, frenzied);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    getFireSound(), SoundSource.PLAYERS,
                    1.0F, 1.0F / (level.getRandom().nextFloat() * 0.4F + 1.2F));
            player.awardStat(Stats.ITEM_USED.get(this));
        }

        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (!stack.hasTag()) {
            return;
        }

        CompoundTag tag = stack.getOrCreateTag();
        int frenzyTicks = tag.getInt(NBT_FRENZY_TICKS);
        if (frenzyTicks > 0 && entity instanceof Player player) {
            player.getCooldowns().removeCooldown(this);
        }

        if (level.isClientSide) {
            return;
        }

        if (tag.contains(NBT_LEGACY_FRENZY_SHOTS)) {
            tag.remove(NBT_LEGACY_FRENZY_SHOTS);
        }

        if (frenzyTicks > 0) {
            tag.putInt(NBT_FRENZY_TICKS, frenzyTicks - 1);
        }
    }

    @Override
    protected void shoot(Level level, Player player, ItemStack gun, ItemStack ammo, IBullet bulletItem, boolean bulletFree) {
        shoot(level, player, gun, ammo, bulletItem, bulletFree, false);
    }

    private void shoot(Level level, Player player, ItemStack gun, ItemStack ammo, IBullet bulletItem,
                       boolean bulletFree, boolean frenzyShot) {
        ObsidianCoreEntity.SpellType spellType = selectSpellType(gun, level);
        ObsidianCoreEntity coreEntity = new ObsidianCoreEntity(GWREEntities.OBSIDIAN_CORE.get(), level, player, spellType);

        float chargeBonus = calculateChargeBonus(HALF_CHARGE_TICKS);
        double damage = getBonusDamage(gun, player) * getDamageMultiplier(gun, player) * chargeBonus;
        coreEntity.setBaseDamage(damage);
        coreEntity.setAOERadiusMultiplier(1.0f + chargeBonus * 0.5f);
        coreEntity.setMaxRange(getRangeForCharge(HALF_CHARGE_TICKS));
        coreEntity.setLauncherId(ensureLauncherId(gun));
        coreEntity.setFrenzyShot(frenzyShot);

        double pitch = Math.toRadians(player.getXRot());
        double yaw = Math.toRadians(player.getYRot());
        double cosYaw = Math.cos(-yaw - Math.PI);
        double sinYaw = Math.sin(-yaw - Math.PI);
        double cosPitch = -Math.cos(-pitch);
        double sinPitch = Math.sin(-pitch);
        double offsetX = sinYaw * cosPitch * 0.5;
        double offsetY = sinPitch * 0.5;
        double offsetZ = cosYaw * cosPitch * 0.5;

        coreEntity.setPos(player.getX() + offsetX, player.getEyeY() - 0.1 + offsetY, player.getZ() + offsetZ);
        coreEntity.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F,
                (float) getProjectileSpeed(gun, player), 0.0F);

        level.addFreshEntity(coreEntity);
        gun.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(player.getUsedItemHand()));
    }

    private static ObsidianCoreEntity.SpellType selectSpellType(ItemStack gun, Level level) {
        int fireWeight = getSpellWeight(gun, ObsidianCoreEntity.SpellType.FIRE);
        int frostWeight = getSpellWeight(gun, ObsidianCoreEntity.SpellType.FROST);
        int holyWeight = getSpellWeight(gun, ObsidianCoreEntity.SpellType.HOLY);
        if (fireWeight + frostWeight + holyWeight <= 0) {
            fireWeight = frostWeight = holyWeight = 1;
        }
        int roll = level.getRandom().nextInt(fireWeight + frostWeight + holyWeight);

        if (roll < fireWeight) {
            return ObsidianCoreEntity.SpellType.FIRE;
        }
        roll -= fireWeight;
        if (roll < frostWeight) {
            return ObsidianCoreEntity.SpellType.FROST;
        }
        return ObsidianCoreEntity.SpellType.HOLY;
    }

    private static int getSpellWeight(ItemStack gun, ObsidianCoreEntity.SpellType spellType) {
        boolean stored = switch (spellType) {
            case FIRE -> hasStoredFireSpell(gun);
            case FROST -> hasStoredFrostSpell(gun);
            case HOLY -> hasStoredHolySpell(gun);
        };
        return Math.max(0, stored
                ? GWREConfig.LAUNCHER.Obisidian.storedSpellWeight.get()
                : GWREConfig.LAUNCHER.Obisidian.missingSpellWeight.get());
    }

    public int getRangeForCharge(int chargeTime) {
        if (chargeTime <= 0) {
            return BASE_MIN_RANGE;
        }

        float chargePercent = Math.min(1.0f, (float) chargeTime / MAX_CHARGE_TIME);
        int rangeBonus = (int) (chargePercent * (MAX_RANGE_WITH_FULL_CHARGE - BASE_MIN_RANGE));
        return BASE_MIN_RANGE + rangeBonus;
    }

    @Override
    protected void addExtraStatsTooltip(ItemStack stack, @Nullable Level world, List<Component> tooltip) {
        MutableComponent range = Component.literal(Integer.toString(BASE_MAX_RANGE)).withStyle(ChatFormatting.WHITE);
        tooltip.add(Component.translatable("item.gwrexpansions.obsidian_launcher.tooltip.range").withStyle(ChatFormatting.DARK_GREEN));
        tooltip.add(Component.translatable("item.gwrexpansions.obsidian_launcher.tooltip.range.values", range)
                .withStyle(ChatFormatting.DARK_GREEN));
        tooltip.add(Component.translatable("item.gwrexpansions.obsidian_launcher.tooltip.instant_info")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.gwrexpansions.obsidian_launcher.tooltip.frenzy_info")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.gwrexpansions.obsidian_launcher.tooltip.skill",
                GunSkillTooltip.keyName()).withStyle(ChatFormatting.GRAY));

        CompoundTag tag = stack.getOrCreateTag();
        boolean fireCast = tag.getBoolean(NBT_FIRE_SPELL_CAST);
        boolean frostCast = tag.getBoolean(NBT_FROST_SPELL_CAST);
        boolean holyCast = tag.getBoolean(NBT_HOLY_SPELL_CAST);

        int spellsCast = 0;
        if (fireCast) spellsCast++;
        if (frostCast) spellsCast++;
        if (holyCast) spellsCast++;

        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("item.gwrexpansions.obsidian_launcher.tooltip.spells_stored", spellsCast, 3)
                .withStyle(ChatFormatting.YELLOW));
        int frenzyTicks = getFrenzyTicks(stack);
        if (frenzyTicks > 0) {
            tooltip.add(Component.translatable("item.gwrexpansions.obsidian_launcher.tooltip.frenzy_time", frenzyTicks / 20.0F)
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
        }

        if (fireCast) {
            tooltip.add(Component.translatable("item.gwrexpansions.obsidian_launcher.tooltip.fire_spell").withStyle(ChatFormatting.RED));
        }
        if (frostCast) {
            tooltip.add(Component.translatable("item.gwrexpansions.obsidian_launcher.tooltip.frost_spell").withStyle(ChatFormatting.AQUA));
        }
        if (holyCast) {
            tooltip.add(Component.translatable("item.gwrexpansions.obsidian_launcher.tooltip.holy_spell").withStyle(ChatFormatting.YELLOW));
        }
    }

    public static void onCoreReturned(Player player, UUID launcherId, ObsidianCoreEntity.SpellType spellType,
                                      boolean spellReleased, boolean frenzyShot) {
        ItemStack launcher = findLauncherById(player, launcherId);
        if (launcher.isEmpty() || !(launcher.getItem() instanceof ObsidianLauncher item)) {
            return;
        }

        decrementActiveCore(launcher);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                GWRESounds.OBSIDIAN_CORE_RELOAD.get(), SoundSource.PLAYERS, 0.6F, 1.15F);
        if (spellReleased) {
            recordDiscoveredSpell(launcher, spellType);
            if (!frenzyShot) {
                recordStoredSpell(launcher, spellType);
            }
            checkAndTriggerAvadaKedavra(player, launcher);
        }

        if (!frenzyShot) {
            player.getCooldowns().addCooldown(item, item.getFireDelay(launcher, player));
        }
    }

    public static void onCoreLost(Player player, UUID launcherId) {
        ItemStack launcher = findLauncherById(player, launcherId);
        if (!launcher.isEmpty()) {
            decrementActiveCore(launcher);
        }
    }

    public static int getActiveCores(ItemStack stack) {
        return Math.max(0, stack.getOrCreateTag().getInt(NBT_ACTIVE_CORES));
    }

    public static int getFrenzyTicks(ItemStack stack) {
        return Math.max(0, stack.getOrCreateTag().getInt(NBT_FRENZY_TICKS));
    }

    public static float getFrenzyProgress(ItemStack stack) {
        return Math.min(1.0F, (float)getFrenzyTicks(stack) / getFrenzyDurationTicks());
    }

    public static boolean isFrenzied(ItemStack stack) {
        return getFrenzyTicks(stack) > 0;
    }

    public static int getMaxActiveCores(ItemStack stack) {
        return NORMAL_MAX_ACTIVE_CORES;
    }

    public static boolean hasStoredFireSpell(ItemStack stack) {
        return stack.getOrCreateTag().getBoolean(NBT_FIRE_SPELL_CAST);
    }

    public static boolean hasStoredFrostSpell(ItemStack stack) {
        return stack.getOrCreateTag().getBoolean(NBT_FROST_SPELL_CAST);
    }

    public static boolean hasStoredHolySpell(ItemStack stack) {
        return stack.getOrCreateTag().getBoolean(NBT_HOLY_SPELL_CAST);
    }

    public static boolean hasAllStoredSpells(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        return tag.getBoolean(NBT_FIRE_SPELL_CAST)
                && tag.getBoolean(NBT_FROST_SPELL_CAST)
                && tag.getBoolean(NBT_HOLY_SPELL_CAST);
    }

    public static UUID ensureLauncherId(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.hasUUID(NBT_LAUNCHER_ID)) {
            tag.putUUID(NBT_LAUNCHER_ID, UUID.randomUUID());
        }
        return tag.getUUID(NBT_LAUNCHER_ID);
    }

    private static boolean canFire(ItemStack stack) {
        return getActiveCores(stack) < getMaxActiveCores(stack);
    }

    private static void decrementActiveCore(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(NBT_ACTIVE_CORES, Math.max(0, tag.getInt(NBT_ACTIVE_CORES) - 1));
    }

    private static ItemStack findLauncherById(Player player, UUID launcherId) {
        ItemStack mainHand = player.getMainHandItem();
        if (matchesLauncherId(mainHand, launcherId)) return mainHand;

        ItemStack offHand = player.getOffhandItem();
        if (matchesLauncherId(offHand, launcherId)) return offHand;

        for (ItemStack stack : player.getInventory().items) {
            if (matchesLauncherId(stack, launcherId)) {
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }

    private static boolean matchesLauncherId(ItemStack stack, UUID launcherId) {
        return !stack.isEmpty()
                && stack.getItem() instanceof ObsidianLauncher
                && stack.hasTag()
                && stack.getTag().hasUUID(NBT_LAUNCHER_ID)
                && stack.getTag().getUUID(NBT_LAUNCHER_ID).equals(launcherId);
    }

    private static void recordStoredSpell(ItemStack gun, ObsidianCoreEntity.SpellType spellType) {
        CompoundTag tag = gun.getOrCreateTag();

        switch (spellType) {
            case FIRE:
                tag.putBoolean(NBT_FIRE_SPELL_CAST, true);
                break;
            case FROST:
                tag.putBoolean(NBT_FROST_SPELL_CAST, true);
                break;
            case HOLY:
                tag.putBoolean(NBT_HOLY_SPELL_CAST, true);
                break;
        }
    }

    private static void recordDiscoveredSpell(ItemStack gun, ObsidianCoreEntity.SpellType spellType) {
        CompoundTag tag = gun.getOrCreateTag();

        switch (spellType) {
            case FIRE:
                tag.putBoolean(NBT_FIRE_SPELL_DISCOVERED, true);
                break;
            case FROST:
                tag.putBoolean(NBT_FROST_SPELL_DISCOVERED, true);
                break;
            case HOLY:
                tag.putBoolean(NBT_HOLY_SPELL_DISCOVERED, true);
                break;
        }
    }

    private static boolean consumeStoredSpells(ItemStack gun) {
        CompoundTag tag = gun.getOrCreateTag();
        boolean hasStoredSpell = hasAllStoredSpells(gun);
        if (hasStoredSpell) {
            tag.putBoolean(NBT_FIRE_SPELL_CAST, false);
            tag.putBoolean(NBT_FROST_SPELL_CAST, false);
            tag.putBoolean(NBT_HOLY_SPELL_CAST, false);
        }
        return hasStoredSpell;
    }

    @Override
    public boolean canUseGunSkill(ServerPlayer player, InteractionHand hand, ItemStack stack) {
        return stack.getItem() instanceof ObsidianLauncher
                && !isFrenzied(stack)
                && getActiveCores(stack) <= 0
                && hasAllStoredSpells(stack);
    }

    @Override
    public void useGunSkill(ServerPlayer player, InteractionHand hand, ItemStack stack) {
        if (!canUseGunSkill(player, hand, stack) || !consumeStoredSpells(stack)) {
            return;
        }

        stack.getOrCreateTag().putInt(NBT_FRENZY_TICKS, getFrenzyDurationTicks());
        player.getCooldowns().removeCooldown(stack.getItem());
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                GWRESounds.OBSIDIAN_CORE_RELOAD.get(), SoundSource.PLAYERS, 0.75F, 0.8F);
    }

    private static int getFrenzyDurationTicks() {
        return Math.max(1, GWREConfig.LAUNCHER.Obisidian.frenzyDurationTicks.get());
    }

    private static void checkAndTriggerAvadaKedavra(Player player, ItemStack gun) {
        if (!(player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)) {
            return;
        }

        CompoundTag tag = gun.getOrCreateTag();
        if (tag.getBoolean(NBT_FIRE_SPELL_DISCOVERED)
                && tag.getBoolean(NBT_FROST_SPELL_DISCOVERED)
                && tag.getBoolean(NBT_HOLY_SPELL_DISCOVERED)) {
            AvadaKedavraTrigger.onThreeSpellsCast(serverPlayer);
        }
    }

    @Override
    protected float calculateChargeBonus(int chargeTicks) {
        return 0.5F + Math.min(1.0F, (float) chargeTicks / MAX_CHARGE_TIME);
    }
}
