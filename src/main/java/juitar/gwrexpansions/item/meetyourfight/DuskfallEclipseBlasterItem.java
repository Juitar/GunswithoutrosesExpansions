package juitar.gwrexpansions.item.meetyourfight;

import juitar.gwrexpansions.config.GWREConfig;
import juitar.gwrexpansions.entity.meetyourfight.DuskRoseSpiritEntity;
import juitar.gwrexpansions.item.ConfigurableBurstGunItem;
import juitar.gwrexpansions.registry.GWREEntities;
import lykrast.gunswithoutroses.entity.BulletEntity;
import lykrast.gunswithoutroses.entity.PiercingBulletEntity;
import lykrast.gunswithoutroses.item.IBullet;
import lykrast.gunswithoutroses.registry.GWRAttributes;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

public class DuskfallEclipseBlasterItem extends ConfigurableBurstGunItem {
    public static final String SHOT_TAG = "DuskfallEclipseShot";
    public static final String LAST_TARGET_TAG = "DuskfallLastTarget";
    public static final String LAST_TARGET_TIME_TAG = "DuskfallLastTargetTime";
    private static final String SPIRIT_TIMER_TAG = "DuskfallSpiritTimer";

    public DuskfallEclipseBlasterItem(Properties properties, int bonusDamage, double damageMultiplier, int fireDelay,
                                     double inaccuracy, int enchantability, int burstSize, int burstFireDelay,
                                     Supplier<GWREConfig.BurstgunConfig> configSupplier) {
        super(properties, bonusDamage, damageMultiplier, fireDelay, inaccuracy, enchantability, burstSize, burstFireDelay,
                configSupplier);
    }

    @Override
    protected void shoot(Level level, Player player, ItemStack gun, ItemStack ammo, IBullet bulletItem, boolean bulletFree) {
        ItemStack firedAmmo = snapshotAmmo(ammo, bulletItem);
        int shots = getProjectilesPerShot(gun, player);
        for (int i = 0; i < shots; ++i) {
            BulletEntity shot = createPiercingShot(level, player, firedAmmo, bulletItem);
            shot.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F,
                    (float) getProjectileSpeed(gun, player), (float) getInaccuracy(gun, player));
            shot.setDamage(Math.max(0.0D, shot.getDamage() + getBonusDamage(gun, player)) * getDamageMultiplier(gun, player));
            if (player.getAttribute((Attribute) GWRAttributes.knockback.get()) != null) {
                shot.setKnockbackStrength(shot.getKnockbackStrength() + player.getAttributeValue((Attribute) GWRAttributes.knockback.get()));
            }
            shot.setHeadshotMultiplier(getHeadshotMultiplier(gun, player));
            affectBulletEntity(player, gun, shot, bulletFree);
            level.addFreshEntity(shot);
        }
    }

    private BulletEntity createPiercingShot(Level level, LivingEntity shooter, ItemStack ammo, IBullet bulletItem) {
        BulletEntity original = bulletItem.createProjectile(level, ammo, shooter);
        PiercingBulletEntity piercing = new PiercingBulletEntity(level, shooter);
        piercing.setItem(ammo.copyWithCount(1));
        piercing.setDamage(original.getDamage());
        piercing.setWaterInertia(original.getWaterInertia());
        piercing.setOwner(shooter);
        piercing.setPierce(GWREConfig.BURSTGUN.duskfallEclipse.pierceCount.get());
        piercing.setPierceMultiplier(GWREConfig.BURSTGUN.duskfallEclipse.pierceDamageMultiplier.get());
        return piercing;
    }

    private ItemStack snapshotAmmo(ItemStack ammo, IBullet bulletItem) {
        if (!ammo.isEmpty()) {
            return ammo.copyWithCount(1);
        }
        return bulletItem instanceof Item item ? new ItemStack(item) : ammo;
    }

    @Override
    protected void affectBulletEntity(LivingEntity shooter, ItemStack gun, BulletEntity shot, boolean bulletFree) {
        super.affectBulletEntity(shooter, gun, shot, bulletFree);
        shot.getPersistentData().putBoolean(SHOT_TAG, true);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slot, isSelected);
        if (level.isClientSide || !(entity instanceof Player player) || !isHeldStack(player, stack)) {
            return;
        }

        int maxSpirits = GWREConfig.BURSTGUN.duskfallEclipse.maxSpirits.get();
        if (maxSpirits <= 0 || GWREEntities.DUSK_ROSE_SPIRIT == null || !GWREEntities.DUSK_ROSE_SPIRIT.isPresent()) {
            stack.getOrCreateTag().putInt(SPIRIT_TIMER_TAG, 0);
            return;
        }

        int active = DuskRoseSpiritEntity.countActiveFor(player);
        if (active >= maxSpirits) {
            stack.getOrCreateTag().putInt(SPIRIT_TIMER_TAG, 0);
            return;
        }

        int timer = stack.getOrCreateTag().getInt(SPIRIT_TIMER_TAG) + 1;
        int interval = Math.max(1, GWREConfig.BURSTGUN.duskfallEclipse.spiritSummonIntervalTicks.get());
        if (timer >= interval) {
            spawnSpirit(level, player, active);
            timer = 0;
        }
        stack.getOrCreateTag().putInt(SPIRIT_TIMER_TAG, timer);
    }

    private void spawnSpirit(Level level, Player player, int orbitIndex) {
        DuskRoseSpiritEntity spirit = new DuskRoseSpiritEntity(GWREEntities.DUSK_ROSE_SPIRIT.get(), level, player, orbitIndex);
        level.addFreshEntity(spirit);
    }

    private boolean isHeldStack(Player player, ItemStack stack) {
        return player.getMainHandItem() == stack || player.getOffhandItem() == stack;
    }

    public static boolean isHeldBy(Player player) {
        return player.getMainHandItem().getItem() instanceof DuskfallEclipseBlasterItem
                || player.getOffhandItem().getItem() instanceof DuskfallEclipseBlasterItem;
    }

    @Override
    protected void addExtraStatsTooltip(ItemStack stack, @Nullable Level level, List<Component> tooltip) {
        super.addExtraStatsTooltip(stack, level, tooltip);
        tooltip.add(Component.translatable("tooltip.gwrexpansions.duskfall_eclipse_blaster.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.gwrexpansions.duskfall_eclipse_blaster.spirits",
                GWREConfig.BURSTGUN.duskfallEclipse.maxSpirits.get(),
                (int) Math.round(GWREConfig.BURSTGUN.duskfallEclipse.damageBonusPerSpirit.get() * 100.0D),
                (int) Math.round(GWREConfig.BURSTGUN.duskfallEclipse.damageReductionPerSpirit.get() * 100.0D))
                .withStyle(ChatFormatting.DARK_PURPLE));
    }
}
