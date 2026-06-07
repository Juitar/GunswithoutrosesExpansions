package juitar.gwrexpansions.item.meetyourfight;

import juitar.gwrexpansions.config.GWREConfig;
import juitar.gwrexpansions.entity.meetyourfight.MirecallerBombBulletEntity;
import juitar.gwrexpansions.item.ConfigurableGunItem;
import juitar.gwrexpansions.registry.VanillaItem;
import lykrast.gunswithoutroses.entity.BulletEntity;
import lykrast.gunswithoutroses.item.IBullet;
import lykrast.meetyourfight.registry.MYFSounds;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

public class MirecallerShotgunItem extends ConfigurableGunItem {
    private static final String MINE_COUNT_TAG = "MirecallerMineCount";
    private static final String SWOOP_READY_TICKS_TAG = "MirecallerSwoopReadyTicks";
    private static final int MINES_FOR_SWOOP = 3;
    private static final int SWOOP_READY_TICKS = 100;
    private static final double COPPER_BOMB_DAMAGE_BONUS = 1.0D;
    private static final double SWOOP_DAMAGE_BONUS = 2.0D;
    private static final ThreadLocal<Boolean> FIRING_SWOOP_SHOT = ThreadLocal.withInitial(() -> false);

    public MirecallerShotgunItem(Properties properties, int bonusDamage, double damageMultiplier, int fireDelay,
                                 double inaccuracy, int enchantability,
                                 Supplier<GWREConfig.GunConfig> configSupplier) {
        super(properties, bonusDamage, damageMultiplier, fireDelay, inaccuracy, enchantability, configSupplier);
    }

    @Override
    protected void shoot(Level world, Player player, ItemStack gun, ItemStack ammo, IBullet bulletItem, boolean bulletFree) {
        boolean swoopReady = isSwoopReady(gun);
        if (swoopReady) {
            FIRING_SWOOP_SHOT.set(true);
            try {
                fireProjectiles(world, player, gun, ammo, bulletItem, bulletFree, 1);
            } finally {
                FIRING_SWOOP_SHOT.set(false);
            }
            clearSwoop(gun);
            return;
        }

        fireProjectiles(world, player, gun, ammo, bulletItem, bulletFree, 0);
        addMineCharge(world, player, gun);
    }

    private void fireProjectiles(Level world, Player player, ItemStack gun, ItemStack ammo, IBullet bulletItem,
                                 boolean bulletFree, int extraProjectiles) {
        int shots = getProjectilesPerShot(gun, player) + extraProjectiles;
        for (int i = 0; i < shots; ++i) {
            BulletEntity shot = createShot(world, player, ammo, bulletItem);
            shot.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F,
                    (float) getProjectileSpeed(gun, player), (float) getInaccuracy(gun, player));
            shot.setDamage(Math.max(0.0D, shot.getDamage() + getBonusDamage(gun, player))
                    * getDamageMultiplier(gun, player));
            shot.setKnockbackStrength(shot.getKnockbackStrength() + getKnockbackBonus(gun, player));
            shot.setHeadshotMultiplier(getHeadshotMultiplier(gun, player));
            affectBulletEntity(player, gun, shot, bulletFree);
            world.addFreshEntity(shot);
        }
    }

    private BulletEntity createShot(Level world, LivingEntity shooter, ItemStack ammo, IBullet bulletItem) {
        if (ammo.is(VanillaItem.copper_bullet.get())) {
            return createBombShot(world, shooter, ammo, bulletItem);
        }
        return bulletItem.createProjectile(world, ammo, shooter);
    }

    private BulletEntity createBombShot(Level world, LivingEntity shooter, ItemStack ammo, IBullet bulletItem) {
        BulletEntity original = bulletItem.createProjectile(world, ammo, shooter);
        MirecallerBombBulletEntity bomb = new MirecallerBombBulletEntity(world, shooter);
        bomb.setItem(ammo.copyWithCount(1));
        bomb.setDamage(original.getDamage() + COPPER_BOMB_DAMAGE_BONUS);
        bomb.setWaterInertia(original.getWaterInertia());
        bomb.setKnockbackStrength(original.getKnockbackStrength());
        bomb.setHeadshotMultiplier(original.getHeadshotMultiplier());
        bomb.getPersistentData().merge(original.getPersistentData());
        bomb.setOwner(shooter);
        return bomb;
    }

    @Override
    protected void affectBulletEntity(LivingEntity shooter, ItemStack gun, BulletEntity shot, boolean bulletFree) {
        super.affectBulletEntity(shooter, gun, shot, bulletFree);
        if (FIRING_SWOOP_SHOT.get()) {
            shot.setDamage(shot.getDamage() + SWOOP_DAMAGE_BONUS);
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean isSelected) {
        super.inventoryTick(stack, world, entity, slot, isSelected);
        if (world.isClientSide) {
            return;
        }

        int readyTicks = getSwoopReadyTicks(stack);
        if (readyTicks > 0) {
            readyTicks--;
            if (readyTicks <= 0) {
                clearSwoop(stack);
            } else {
                stack.getOrCreateTag().putInt(SWOOP_READY_TICKS_TAG, readyTicks);
            }
        }
    }

    @Override
    protected void addExtraStatsTooltip(ItemStack stack, @Nullable Level world, List<Component> tooltip) {
        super.addExtraStatsTooltip(stack, world, tooltip);
        tooltip.add(Component.translatable("tooltip.gwrexpansions.mirecaller_shotgun.desc").withStyle(ChatFormatting.GRAY));
        if (isSwoopReady(stack)) {
            tooltip.add(Component.translatable("tooltip.gwrexpansions.mirecaller_shotgun.ready").withStyle(ChatFormatting.GREEN));
        } else {
            tooltip.add(Component.translatable("tooltip.gwrexpansions.mirecaller_shotgun.charges",
                    getMineCount(stack), MINES_FOR_SWOOP).withStyle(ChatFormatting.DARK_GREEN));
        }
    }

    private static void addMineCharge(Level world, Player player, ItemStack stack) {
        int previousCount = getMineCount(stack);
        int count = Math.min(MINES_FOR_SWOOP, previousCount + 1);
        stack.getOrCreateTag().putInt(MINE_COUNT_TAG, count);
        if (count >= MINES_FOR_SWOOP) {
            stack.getOrCreateTag().putInt(SWOOP_READY_TICKS_TAG, SWOOP_READY_TICKS);
            if (previousCount < MINES_FOR_SWOOP && !world.isClientSide) {
                world.playSound(null, player.getX(), player.getY(), player.getZ(),
                        MYFSounds.swampjawStun.get(), SoundSource.PLAYERS, 0.9F, 1.05F);
            }
        }
    }

    private static boolean isSwoopReady(ItemStack stack) {
        return getSwoopReadyTicks(stack) > 0;
    }

    private static int getMineCount(ItemStack stack) {
        return stack.getOrCreateTag().getInt(MINE_COUNT_TAG);
    }

    private static int getSwoopReadyTicks(ItemStack stack) {
        return stack.getOrCreateTag().getInt(SWOOP_READY_TICKS_TAG);
    }

    private static void clearSwoop(ItemStack stack) {
        stack.getOrCreateTag().putInt(MINE_COUNT_TAG, 0);
        stack.getOrCreateTag().remove(SWOOP_READY_TICKS_TAG);
    }
}
