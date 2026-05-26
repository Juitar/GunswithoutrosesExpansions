package juitar.gwrexpansions.item.cataclysm;

import com.github.L_Ender.cataclysm.init.ModSounds;
import juitar.gwrexpansions.config.GWREConfig;
import juitar.gwrexpansions.entity.cataclysm.IgnitiumBulletEntity;
import juitar.gwrexpansions.item.ConfigurableGatlingItem;
import juitar.gwrexpansions.registry.CompatCataclysm;
import lykrast.gunswithoutroses.entity.BulletEntity;
import lykrast.gunswithoutroses.item.IBullet;
import lykrast.gunswithoutroses.registry.GWRAttributes;
import lykrast.gunswithoutroses.registry.GWREnchantments;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

public class IgnitiumGatlingItem extends ConfigurableGatlingItem {
    private static final String BLUE_FIRE_TICKS_TAG = "IgnitiumBlueFireTicks";
    private static final String WAS_BELOW_HALF_TAG = "IgnitiumWasBelowHalf";

    public IgnitiumGatlingItem(Properties properties, int bonusDamage, double damageMultiplier, int fireDelay, double inaccuracy, int enchantability, Supplier<GWREConfig.GunConfig> config){
        super(properties, bonusDamage, damageMultiplier, fireDelay, inaccuracy, enchantability,config);
    }

    public static boolean isBlueFireActive(ItemStack stack) {
        return getBlueFireTicks(stack) > 0;
    }

    public static int getBlueFireTicks(ItemStack stack) {
        return stack.hasTag() ? stack.getOrCreateTag().getInt(BLUE_FIRE_TICKS_TAG) : 0;
    }

    @Override
    protected ItemStack overrideFiredStack(LivingEntity shooter, ItemStack gun, ItemStack ammo, IBullet bulletItem, boolean bulletFree) {
        if (ammo.is(CompatCataclysm.tagBaseBullets)) return new ItemStack(CompatCataclysm.ignitium_bullet.get());
        else return ammo;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slot, isSelected);
        if (!(entity instanceof Player player) || (!isSelected && player.getOffhandItem() != stack)) {
            return;
        }

        boolean belowHalf = player.getHealth() <= player.getMaxHealth() * 0.5F;
        boolean wasBelowHalf = stack.getOrCreateTag().getBoolean(WAS_BELOW_HALF_TAG);
        int ticks = getBlueFireTicks(stack);

        if (belowHalf && !wasBelowHalf) {
            ticks = getConfig().blueFireDurationTicks.get();
            stack.getOrCreateTag().putInt(BLUE_FIRE_TICKS_TAG, ticks);
            if (!level.isClientSide) {
                spawnBlueFireBurst((ServerLevel) level, player);
            }
        } else if (!belowHalf && ticks > 0) {
            ticks--;
            if (ticks > 0) {
                stack.getOrCreateTag().putInt(BLUE_FIRE_TICKS_TAG, ticks);
            } else {
                stack.getOrCreateTag().remove(BLUE_FIRE_TICKS_TAG);
            }
        }

        stack.getOrCreateTag().putBoolean(WAS_BELOW_HALF_TAG, belowHalf);
    }

    @Override
    public double getBonusDamage(ItemStack stack, @Nullable LivingEntity shooter) {
        if (!isBlueFireActive(stack)) {
            return super.getBonusDamage(stack, shooter);
        }

        int impact = stack.getEnchantmentLevel(GWREnchantments.impact.get());
        double bonus = impact >= 1 ? GWREnchantments.impactBonus(impact) : 0.0D;
        if (shooter != null && shooter.getAttribute(GWRAttributes.dmgBase.get()) != null) {
            bonus += shooter.getAttributeValue(GWRAttributes.dmgBase.get());
        }
        return getConfig().blueFireBonusDamage.get() + bonus;
    }

    @Override
    public double getFireDelayFractional(ItemStack stack, @Nullable LivingEntity shooter) {
        if (!isBlueFireActive(stack)) {
            return super.getFireDelayFractional(stack, shooter);
        }

        double baseDelay = getConfig().blueFireDelay.get().doubleValue();
        int sleight = stack.getEnchantmentLevel(GWREnchantments.sleightOfHand.get());
        double delay = sleight > 0 ? GWREnchantments.sleightModifyFractional(sleight, baseDelay) : baseDelay;
        if (shooter != null && shooter.getAttribute(GWRAttributes.fireDelay.get()) != null) {
            delay *= shooter.getAttributeValue(GWRAttributes.fireDelay.get());
        }
        return Math.max(1.0D, delay);
    }

    @Override
    protected void affectBulletEntity(LivingEntity shooter, ItemStack gun, BulletEntity bullet, boolean bulletFree) {
        super.affectBulletEntity(shooter, gun, bullet, bulletFree);
        if (bullet instanceof IgnitiumBulletEntity ignitiumBullet) {
            ignitiumBullet.setBlueFire(isBlueFireActive(gun));
        }
    }

    @Override
    protected void addExtraStatsTooltip(ItemStack stack, @Nullable Level world, List<Component> tooltip){
        tooltip.add(Component.translatable("tooltip.gwrexpansions.ignitium_gatling.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.gwrexpansions.ignitium_gatling.desc2") .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.gwrexpansions.ignitium_gatling.blue_fire",
                getConfig().blueFireDurationTicks.get() / 20.0D).withStyle(ChatFormatting.AQUA));
    }

    private static GWREConfig.IgnitiumGatlingConfig getConfig() {
        return GWREConfig.GATLING.Ignitium;
    }

    private static void spawnBlueFireBurst(ServerLevel level, Player player) {
        double y = player.getY() + player.getBbHeight() * 0.5D;
        level.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, player.getX(), y, player.getZ(),
                80, player.getBbWidth() * 0.8D, player.getBbHeight() * 0.35D, player.getBbWidth() * 0.8D, 0.12D);
        level.sendParticles(ParticleTypes.SOUL, player.getX(), y, player.getZ(),
                24, player.getBbWidth() * 0.55D, player.getBbHeight() * 0.25D, player.getBbWidth() * 0.55D, 0.05D);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.FLAME_BURST.get(),
                SoundSource.PLAYERS, 0.9F, 1.45F);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.IGNIS_IMPACT.get(),
                SoundSource.PLAYERS, 0.45F, 1.7F);
    }
}
