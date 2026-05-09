package juitar.gwrexpansions.item.alexscaves;

import juitar.gwrexpansions.GWRexpansions;
import juitar.gwrexpansions.config.GWREConfig;
import juitar.gwrexpansions.entity.alexscaves.MagneticBulletEntity;
import juitar.gwrexpansions.entity.alexscaves.MagneticPinEntity;
import juitar.gwrexpansions.item.ConfigurableGatlingItem;
import lykrast.gunswithoutroses.entity.BulletEntity;
import lykrast.gunswithoutroses.item.IBullet;
import lykrast.gunswithoutroses.registry.GWRAttributes;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

public class MagneticGatlingItem extends ConfigurableGatlingItem {
    private static final int PIN_DURABILITY_COST = 4;
    private static final int PIN_COOLDOWN = 40;
    public static final TagKey<Item> MAGNETIZABLE_BULLETS = ItemTags.create(GWRexpansions.resource("magnetizable_bullets"));

    public MagneticGatlingItem(Properties properties, int bonusDamage, double damageMultiplier, int fireDelay, double inaccuracy, int enchantability, Supplier<GWREConfig.GunConfig> config) {
        super(properties, bonusDamage, damageMultiplier, fireDelay, inaccuracy, enchantability, config);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isCrouching()) {
            if (player.getCooldowns().isOnCooldown(this)) {
                return InteractionResultHolder.fail(stack);
            }
            player.startUsingItem(hand);
            if (!level.isClientSide) {
                fireMagneticPin(level, player, stack);
                player.getCooldowns().addCooldown(this, PIN_COOLDOWN);
                player.awardStat(Stats.ITEM_USED.get(this));
            }
            return InteractionResultHolder.consume(stack);
        }
        return super.use(level, player, hand);
    }

    private void fireMagneticPin(Level level, Player player, ItemStack gun) {
        discardExistingPin(level, player);

        MagneticPinEntity pin = new MagneticPinEntity(level, player);
        pin.setItem(new ItemStack(this));
        pin.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 3.0F, 0.0F);
        level.addFreshEntity(pin);

        if (!player.getAbilities().instabuild) {
            gun.hurtAndBreak(PIN_DURABILITY_COST, player, brokenPlayer -> brokenPlayer.broadcastBreakEvent(player.getUsedItemHand()));
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(), getFireSound(), SoundSource.PLAYERS, 0.9F, 1.35F);
    }

    private void discardExistingPin(Level level, Player player) {
        level.getEntitiesOfClass(MagneticPinEntity.class, player.getBoundingBox().inflate(96.0D),
                pin -> pin.canAttract(player)).forEach(MagneticPinEntity::discard);
    }

    @Override
    protected void shoot(Level level, Player player, ItemStack gun, ItemStack ammo, IBullet bulletItem, boolean bulletFree) {
        ItemStack override = overrideFiredStack(player, gun, ammo, bulletItem, bulletFree);
        if (override != ammo && override.getItem() instanceof IBullet overrideBullet) {
            ammo = override;
            bulletItem = overrideBullet;
        }

        ItemStack firedAmmo = snapshotAmmo(ammo, bulletItem);
        int shots = getProjectilesPerShot(gun, player);
        for (int i = 0; i < shots; ++i) {
            BulletEntity shot = shouldMagnetize(firedAmmo) ? createMagneticBullet(level, player, firedAmmo, bulletItem) : bulletItem.createProjectile(level, firedAmmo, player);
            shot.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, (float)getProjectileSpeed(gun, player), (float)getInaccuracy(gun, player));
            shot.setDamage(Math.max(0.0D, shot.getDamage() + getBonusDamage(gun, player)) * getDamageMultiplier(gun, player));
            if (player.getAttribute((Attribute) GWRAttributes.knockback.get()) != null) {
                shot.setKnockbackStrength(shot.getKnockbackStrength() + player.getAttributeValue((Attribute)GWRAttributes.knockback.get()));
            }
            shot.setHeadshotMultiplier(getHeadshotMultiplier(gun, player));
            affectBulletEntity(player, gun, shot, bulletFree);
            level.addFreshEntity(shot);
        }
    }

    private boolean shouldMagnetize(ItemStack ammo) {
        return ammo.is(MAGNETIZABLE_BULLETS);
    }

    private ItemStack snapshotAmmo(ItemStack ammo, IBullet bulletItem) {
        if (!ammo.isEmpty()) {
            return ammo.copyWithCount(1);
        }
        return bulletItem instanceof Item item ? new ItemStack(item) : ammo;
    }

    private BulletEntity createMagneticBullet(Level level, LivingEntity shooter, ItemStack ammo, IBullet bulletItem) {
        BulletEntity original = bulletItem.createProjectile(level, ammo, shooter);
        MagneticBulletEntity magnetic = new MagneticBulletEntity(level, shooter);
        magnetic.setItem(ammo.copyWithCount(1));
        magnetic.setDamage(original.getDamage());
        magnetic.setWaterInertia(original.getWaterInertia());
        magnetic.setOwner(shooter);
        magnetic.setEffectDelegate(original);
        return magnetic;
    }

    @Override
    protected void addExtraStatsTooltip(ItemStack stack, @Nullable Level level, List<Component> tooltip) {
        tooltip.add(Component.translatable("tooltip.gwrexpansions.magnetic_gatling.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.gwrexpansions.magnetic_gatling.desc2").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.gwrexpansions.magnetic_gatling.desc3").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
