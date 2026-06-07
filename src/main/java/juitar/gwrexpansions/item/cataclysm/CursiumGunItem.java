package juitar.gwrexpansions.item.cataclysm;

import com.github.L_Ender.cataclysm.entity.projectile.Phantom_Halberd_Entity;
import com.github.L_Ender.cataclysm.init.ModParticle;
import com.github.L_Ender.cataclysm.init.ModSounds;
import juitar.gwrexpansions.config.GWREConfig;
import juitar.gwrexpansions.entity.cataclysm.CursiumBulletEntity;
import juitar.gwrexpansions.item.ConfigurableGunItem;
import juitar.gwrexpansions.registry.CompatCataclysm;
import juitar.gwrexpansions.registry.GWRECataclysmEnchantments;
import lykrast.gunswithoutroses.entity.BulletEntity;
import lykrast.gunswithoutroses.item.IBullet;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

public class CursiumGunItem extends ConfigurableGunItem {
    private static final String RAGE_TAG = "CursiumRage";
    private static final String TRIPLE_SHOT_READY_TAG = "CursiumTripleShotReady";
    public static final String CURSIUM_SNIPER_SHOT_TAG = "CursiumSniperShot";
    private static final double CURSIUM_DRAIN_RAGE_PER_HEADSHOT = 1.5D;
    private static final double TRIPLE_SHOT_SIDE_OFFSET = 0.45D;

    public CursiumGunItem(Properties properties, int bonusDamage, double damageMultiplier, int fireDelay, double inaccuracy, int enchantability, Supplier<GWREConfig.GunConfig> configSupplier) {
        super(properties, bonusDamage, damageMultiplier, fireDelay, inaccuracy, enchantability,configSupplier);
    }

    public static double getRage(ItemStack stack) {
        return stack.hasTag() ? Mth.clamp(stack.getOrCreateTag().getDouble(RAGE_TAG), 0.0D, getMaxRage()) : 0.0D;
    }

    public static int getMaxRage() {
        return GWREConfig.SNIPER.cursium.maxRage.get();
    }

    public static void setRage(ItemStack stack, double rage) {
        double clamped = Mth.clamp(rage, 0.0D, getMaxRage());
        if (clamped <= 0.0D) {
            stack.getOrCreateTag().remove(RAGE_TAG);
        } else {
            stack.getOrCreateTag().putDouble(RAGE_TAG, clamped);
        }
    }

    public static void addRage(Player player) {
        ItemStack stack = findHeldCursiumSniper(player);
        if (!stack.isEmpty()) {
            addRage(stack);
        }
    }

    private static void addRage(ItemStack stack) {
        if (!GWRECataclysmEnchantments.has(stack, GWRECataclysmEnchantments.CURSIUM_DRAIN)) {
            setRage(stack, getRage(stack) + 1.0D);
            return;
        }

        setRage(stack, getRage(stack) + CURSIUM_DRAIN_RAGE_PER_HEADSHOT);
    }

    public static void onBulletHeadshot(BulletEntity bullet, Entity shooter, boolean headshot) {
        if (headshot
                && bullet.getPersistentData().getBoolean(CURSIUM_SNIPER_SHOT_TAG)
                && shooter instanceof Player player) {
            addRage(player);
        }
    }

    public static boolean isTripleShotReady(ItemStack stack) {
        return stack.hasTag() && stack.getOrCreateTag().getBoolean(TRIPLE_SHOT_READY_TAG);
    }

    private static void setTripleShotReady(ItemStack stack, boolean ready) {
        if (ready) {
            stack.getOrCreateTag().putBoolean(TRIPLE_SHOT_READY_TAG, true);
        } else {
            stack.getOrCreateTag().remove(TRIPLE_SHOT_READY_TAG);
        }
    }

    public static ItemStack findHeldCursiumSniper(Player player) {
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.getItem() instanceof CursiumGunItem) {
            return mainHand;
        }

        ItemStack offHand = player.getOffhandItem();
        if (offHand.getItem() instanceof CursiumGunItem) {
            return offHand;
        }

        return ItemStack.EMPTY;
    }

    @Override
    public double getDamageMultiplier(ItemStack stack, @Nullable LivingEntity shooter) {
        double rageBonus = getRage(stack) * GWREConfig.SNIPER.cursium.damageMultiplierPerRage.get();
        return Math.max(0.0D, super.getDamageMultiplier(stack, shooter) + rageBonus);
    }

    @Override
    public double getHeadshotMultiplier(ItemStack stack, @Nullable LivingEntity shooter) {
        double multiplier = super.getHeadshotMultiplier(stack, shooter);
        if (getRage(stack) >= getMaxRage()) {
            multiplier += GWREConfig.SNIPER.cursium.fullRageHeadshotMultiplierBonus.get();
        }
        return Math.max(1.0D, multiplier);
    }

    @Override
    protected ItemStack overrideFiredStack(LivingEntity shooter, ItemStack gun, ItemStack ammo, IBullet bulletItem, boolean bulletFree) {
        if (ammo.is(CompatCataclysm.tagBaseBullets)) return new ItemStack(CompatCataclysm.cursium_bullet.get());
        else return ammo;
    }

    @Override
    protected void affectBulletEntity(LivingEntity shooter, ItemStack gun, BulletEntity bullet, boolean bulletFree) {
        super.affectBulletEntity(shooter, gun, bullet, bulletFree);
        bullet.getPersistentData().putBoolean(CURSIUM_SNIPER_SHOT_TAG, true);
        if (bullet instanceof CursiumBulletEntity cursiumBullet) {
            cursiumBullet.setSHOT_FROM_CURSIUM(true);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            double maxRage = getMaxRage();
            if (getRage(stack) < maxRage) {
                return InteractionResultHolder.fail(stack);
            }

            if (!level.isClientSide) {
                setRage(stack, 0.0D);
                setTripleShotReady(stack, true);
                releasePhantomHalberdStorm((ServerLevel) level, player);
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }

        return super.use(level, player, hand);
    }

    @Override
    protected void shoot(Level level, Player player, ItemStack gun, ItemStack ammo, IBullet bulletItem, boolean bulletFree) {
        boolean tripleShotReady = isTripleShotReady(gun);
        super.shoot(level, player, gun, ammo, bulletItem, bulletFree);
        if (tripleShotReady && !level.isClientSide) {
            spawnTripleShotSideBullets(level, player, gun, ammo, bulletItem, bulletFree);
            setTripleShotReady(gun, false);
        }
    }

    @Override
    protected void addExtraStatsTooltip(ItemStack stack, @Nullable Level world, List<Component> tooltip){
        tooltip.add(Component.translatable("tooltip.gwrexpansions.cursium_sniper.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.gwrexpansions.cursium_sniper.desc2") .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.gwrexpansions.cursium_sniper.rage")
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.gwrexpansions.cursium_sniper.triple_shot")
                .withStyle(ChatFormatting.GRAY));
    }

    private void spawnTripleShotSideBullets(Level level, Player player, ItemStack gun, ItemStack ammo,
            IBullet bulletItem, boolean bulletFree) {
        ItemStack firedStack = overrideFiredStack(player, gun, ammo, bulletItem, bulletFree);
        if (!(firedStack.getItem() instanceof IBullet firedBullet)) {
            return;
        }

        Vec3 look = player.getLookAngle();
        Vec3 right = new Vec3(look.z, 0.0D, -look.x);
        if (right.lengthSqr() < 1.0E-5D) {
            float yaw = player.getYRot() * Mth.DEG_TO_RAD;
            right = new Vec3(Mth.cos(yaw), 0.0D, Mth.sin(yaw));
        } else {
            right = right.normalize();
        }

        spawnSideBullet(level, player, gun, firedStack, firedBullet, bulletFree, right.scale(-TRIPLE_SHOT_SIDE_OFFSET));
        spawnSideBullet(level, player, gun, firedStack, firedBullet, bulletFree, right.scale(TRIPLE_SHOT_SIDE_OFFSET));
    }

    private void spawnSideBullet(Level level, Player player, ItemStack gun, ItemStack firedStack, IBullet bulletItem,
            boolean bulletFree, Vec3 offset) {
        BulletEntity shot = bulletItem.createProjectile(level, firedStack, player);
        shot.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F,
                (float) getProjectileSpeed(gun, player), 0.0F);
        shot.setPos(shot.getX() + offset.x, shot.getY(), shot.getZ() + offset.z);
        shot.setDamage(Math.max(0.0D, shot.getDamage() + getBonusDamage(gun, player))
                * getDamageMultiplier(gun, player));
        shot.setKnockbackStrength(shot.getKnockbackStrength() + getKnockbackBonus(gun, player));
        shot.setHeadshotMultiplier(getHeadshotMultiplier(gun, player));
        affectBulletEntity(player, gun, shot, bulletFree);
        level.addFreshEntity(shot);
    }

    private static void releasePhantomHalberdStorm(ServerLevel level, Player player) {
        level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.PHANTOM_SPEAR.get(),
                SoundSource.PLAYERS, 1.0F, 1.0F);

        double[] radii = { 2.0D, 3.6D, 5.2D, 6.8D };
        int[] counts = { 8, 12, 16, 20 };
        float baseAngle = player.getYRot() * Mth.DEG_TO_RAD;
        int baseDelay = GWREConfig.BulletConfig.phantomHalberdDelay.get();
        float damage = GWREConfig.BulletConfig.phantomHalberDamage.get().floatValue();

        for (int wave = 0; wave < radii.length; wave++) {
            double radius = radii[wave];
            int count = counts[wave];
            int warmup = baseDelay + wave * 4;
            for (int index = 0; index < count; index++) {
                double angle = baseAngle + (Math.PI * 2.0D * index / count) + (wave % 2 == 0 ? 0.0D : Math.PI / count);
                double x = player.getX() + Mth.cos((float) angle) * radius;
                double z = player.getZ() + Mth.sin((float) angle) * radius;
                spawnHalberd(level, player, x, z, (float) angle, warmup + index % 3, damage);
            }
        }
    }

    private static void spawnHalberd(ServerLevel level, Player player, double x, double z, float yRot, int warmup, float damage) {
        double maxY = player.getY() + 3.0D;
        double minY = player.getY() - 5.0D;
        BlockPos blockPos = BlockPos.containing(x, maxY, z);
        boolean spawned = false;

        while (blockPos.getY() >= Mth.floor(minY) - 1) {
            BlockPos below = blockPos.below();
            BlockState belowState = level.getBlockState(below);
            if (belowState.isFaceSturdy(level, below, Direction.UP)) {
                double y = blockPos.getY();
                BlockState state = level.getBlockState(blockPos);
                VoxelShape shape = state.getCollisionShape(level, blockPos, CollisionContext.empty());
                if (!shape.isEmpty()) {
                    y += shape.max(Direction.Axis.Y);
                }
                spawnHalberdAt(level, player, x, y, z, yRot, warmup, damage);
                spawned = true;
                break;
            }
            blockPos = blockPos.below();
        }

        if (!spawned) {
            spawnHalberdAt(level, player, x, player.getY(), z, yRot, warmup, damage);
        }
    }

    private static void spawnHalberdAt(ServerLevel level, LivingEntity caster, double x, double y, double z,
            float yRot, int warmup, float damage) {
        Phantom_Halberd_Entity halberd = new Phantom_Halberd_Entity(level, x, y, z, yRot, warmup, caster, damage);
        level.addFreshEntity(halberd);
        level.sendParticles(ModParticle.PHANTOM_WING_FLAME.get(), x, y + 0.2D, z,
                8, 0.18D, 0.05D, 0.18D, 0.01D);
    }
}
