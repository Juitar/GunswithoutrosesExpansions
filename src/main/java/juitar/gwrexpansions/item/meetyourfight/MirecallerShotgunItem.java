package juitar.gwrexpansions.item.meetyourfight;

import juitar.gwrexpansions.config.GWREConfig;
import juitar.gwrexpansions.entity.meetyourfight.MirecallerSwampMineEntity;
import juitar.gwrexpansions.item.ConfigurableGunItem;
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
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

public class MirecallerShotgunItem extends ConfigurableGunItem {
    private static final String MINE_COUNT_TAG = "MirecallerMineCount";
    private static final String SWOOP_READY_TICKS_TAG = "MirecallerSwoopReadyTicks";
    private static final int MINES_FOR_SWOOP = 3;
    private static final int SWOOP_READY_TICKS = 100;
    private static final double SWOOP_DAMAGE_BONUS = 2.0D;
    private static final double SWOOP_KNOCKBACK_BONUS = 1.25D;
    private static final ThreadLocal<Boolean> FIRING_SWOOP_SHOT = ThreadLocal.withInitial(() -> false);

    public MirecallerShotgunItem(Properties properties, int bonusDamage, double damageMultiplier, int fireDelay,
                                 double inaccuracy, int enchantability,
                                 Supplier<GWREConfig.GunConfig> configSupplier) {
        super(properties, bonusDamage, damageMultiplier, fireDelay, inaccuracy, enchantability, configSupplier);
    }

    @Override
    protected void shoot(Level world, Player player, ItemStack gun, ItemStack ammo, IBullet bulletItem, boolean bulletFree) {
        if (isSwoopReady(gun)) {
            FIRING_SWOOP_SHOT.set(true);
            try {
                super.shoot(world, player, gun, ammo, bulletItem, bulletFree);
            } finally {
                FIRING_SWOOP_SHOT.set(false);
            }
            launchXPatternMines(world, player);
            clearSwoop(gun);
            return;
        }

        super.shoot(world, player, gun, ammo, bulletItem, bulletFree);
        launchMine(world, player);
        addMineCharge(world, player, gun);
    }

    @Override
    protected void affectBulletEntity(LivingEntity shooter, ItemStack gun, BulletEntity shot, boolean bulletFree) {
        super.affectBulletEntity(shooter, gun, shot, bulletFree);
        if (FIRING_SWOOP_SHOT.get()) {
            shot.setDamage(shot.getDamage() + SWOOP_DAMAGE_BONUS);
            shot.setKnockbackStrength(shot.getKnockbackStrength() + SWOOP_KNOCKBACK_BONUS);
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

    private static void launchXPatternMines(Level world, Player player) {
        if (world.isClientSide) {
            return;
        }

        Vec3 forward = horizontalForward(player);
        Vec3 right = new Vec3(-forward.z, 0.0D, forward.x).normalize();
        Vec3[] directions = new Vec3[] {
                forward.add(right).normalize(),
                forward.subtract(right).normalize(),
                forward.scale(-1.0D).add(right).normalize(),
                forward.scale(-1.0D).subtract(right).normalize()
        };

        Vec3 center = player.position().add(0.0D, 0.35D, 0.0D);
        for (Vec3 direction : directions) {
            launchMine(world, player, center.add(direction.scale(0.6D)),
                    direction.scale(0.55D).add(0.0D, 0.08D, 0.0D));
        }

        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                MYFSounds.swampjawBomb.get(), SoundSource.PLAYERS, 0.9F, 1.0F);
    }

    private static void launchMine(Level world, Player player) {
        if (world.isClientSide) {
            return;
        }

        Vec3 look = player.getLookAngle().normalize();
        Vec3 right = look.cross(new Vec3(0.0D, 1.0D, 0.0D));
        if (right.lengthSqr() < 1.0E-4D) {
            right = new Vec3(1.0D, 0.0D, 0.0D);
        } else {
            right = right.normalize();
        }

        double sideOffset = (world.getRandom().nextDouble() - 0.5D) * 0.35D;
        Vec3 spawnPos = player.getEyePosition()
                .add(look.scale(0.65D))
                .add(right.scale(sideOffset))
                .add(0.0D, -0.35D, 0.0D);

        Vec3 velocity = look.scale(0.85D)
                .add(right.scale((world.getRandom().nextDouble() - 0.5D) * 0.2D))
                .add(0.0D, 0.1D, 0.0D);
        launchMine(world, player, spawnPos, velocity);
    }

    private static void launchMine(Level world, Player player, Vec3 spawnPos, Vec3 velocity) {
        MirecallerSwampMineEntity mine = new MirecallerSwampMineEntity(world, spawnPos.x, spawnPos.y, spawnPos.z, player);
        mine.setDeltaMovement(velocity);
        world.addFreshEntity(mine);
    }

    private static Vec3 horizontalForward(Player player) {
        Vec3 look = player.getLookAngle();
        Vec3 forward = new Vec3(look.x, 0.0D, look.z);
        if (forward.lengthSqr() < 1.0E-4D) {
            return new Vec3(0.0D, 0.0D, 1.0D);
        }
        return forward.normalize();
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
