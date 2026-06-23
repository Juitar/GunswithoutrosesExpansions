package juitar.gwrexpansions.item.BOMD;

import juitar.gwrexpansions.config.GWREConfig;
import juitar.gwrexpansions.entity.BOMD.CoinEntity;
import juitar.gwrexpansions.item.ConfigurableGunItem;
import juitar.gwrexpansions.item.GunSkillItem;
import juitar.gwrexpansions.network.CoinHitFeedbackPacket;
import juitar.gwrexpansions.network.GWRENetwork;
import juitar.gwrexpansions.registry.GWRESounds;
import juitar.gwrexpansions.util.CoinTargetUtils;
import net.minecraft.server.level.ServerPlayer;
import lykrast.gunswithoutroses.entity.BulletEntity;
import lykrast.gunswithoutroses.item.IBullet;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;

import java.util.List;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class Hellforge extends ConfigurableGunItem implements GunSkillItem {
    public static final String NBT_COINS = "Coins";
    public static final String NBT_COIN_RECHARGE_TIMER = "CoinRechargeTimer";
    public static final String NBT_COIN_CHAIN_HITS = "CoinChainHits";
    public static final String NBT_COIN_CHAIN_TIMER = "CoinChainTimer";
    public static final String NBT_COIN_THROW_QUEUE = "CoinThrowQueue";
    public static final String NBT_COIN_THROW_COOLDOWN = "CoinThrowCooldown";
    public static final String NBT_PRIORITY_TARGET_ID = "PriorityTargetId";
    public static final String NBT_PRIORITY_TARGET_TIMER = "PriorityTargetTimer";
    public static final String NBT_COIN_OVERHEAT_TIMER = "CoinOverheatTimer";
    public static final String COIN_INTENT_TARGET_ID = "HellforgeIntentTargetId";
    public static final String BULLET_COIN_MISS_GRACE = "HellforgeCoinMissGrace";
    public static final String BULLET_SHOOTER_UUID = "HellforgeShooterUuid";
    public static final int MAX_COINS = 4;
    public static final int COIN_RECHARGE_TICKS = 60;
    public static final int COIN_CHAIN_WINDOW_TICKS = 40;
    public static final int PRIORITY_TARGET_TICKS = 100;
    public static final int COIN_HIT_RECHARGE_ADVANCE_TICKS = 12;
    public static final int COIN_LINK_RECHARGE_ADVANCE_TICKS = 8;
    public static final int COIN_OVERHEAT_TICKS = 100;
    public static final int COIN_STRONG_OVERHEAT_TICKS = 100;
    public static final int COIN_STRONG_OVERHEAT_RECHARGE_ADVANCE_TICKS = 60;
    private static final int BASE_THROW_COOLDOWN_TICKS = 4;
    private static final int CHAIN_THROW_COOLDOWN_TICKS = 3;
    private static final int MAX_THROW_QUEUE = 2;

    /**
      *
     *
      *
      *
      *
      *
      *
      *
      *
      *
     */
    public Hellforge(Properties properties, int bonusDamage, double damageMultiplier, int fireDelay, double inaccuracy, int enchantability, int conintime, Supplier<GWREConfig.GunConfig> configSupplier) {
        super(properties, bonusDamage, damageMultiplier, fireDelay, inaccuracy, enchantability, configSupplier);
    }

    @Override
    protected void shoot(Level level, Player player, ItemStack gun, ItemStack ammo, IBullet bulletItem, boolean bulletFree) {
        super.shoot(level, player, gun, ammo, bulletItem, bulletFree);
    }

    @Override
    public double getHeadshotMultiplier(ItemStack stack, @Nullable LivingEntity shooter) {
        return stack.getOrCreateTag().getInt(NBT_COIN_OVERHEAT_TIMER) > 0 ? 1.5D : 1.0D;
    }


    @Override
    public double getDamageMultiplier(ItemStack stack, @Nullable LivingEntity shooter) {
        double multiplier = super.getDamageMultiplier(stack, shooter);
        return stack.getOrCreateTag().getInt(NBT_COIN_OVERHEAT_TIMER) > 0 ? multiplier * 1.2D : multiplier;
    }

    @Override
    public int getFireDelay(ItemStack stack, @Nullable LivingEntity shooter) {
        int delay = super.getFireDelay(stack, shooter);
        return stack.getOrCreateTag().getInt(NBT_COIN_OVERHEAT_TIMER) > 0
            ? Math.max(1, (int)Math.ceil(delay * 0.7D))
            : delay;
    }
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return super.use(level, player, hand);
    }


    @Override
    public void inventoryTick(ItemStack stack, Level level, net.minecraft.world.entity.Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);

        if (level.isClientSide || !(entity instanceof ServerPlayer player)) {
            return;
        }

        CompoundTag tag = stack.getOrCreateTag();
        int overheatTimer = tag.getInt(NBT_COIN_OVERHEAT_TIMER);
        int coins = tag.getInt(NBT_COINS);
        if (coins < MAX_COINS) {
            int rechargeTimer = tag.getInt(NBT_COIN_RECHARGE_TIMER) + (overheatTimer > 0 ? 2 : 1);
            if (rechargeTimer >= COIN_RECHARGE_TICKS) {
                tag.putInt(NBT_COINS, coins + 1);
                tag.putInt(NBT_COIN_RECHARGE_TIMER, 0);
            } else {
                tag.putInt(NBT_COIN_RECHARGE_TIMER, rechargeTimer);
            }
        } else if (tag.getInt(NBT_COIN_RECHARGE_TIMER) != 0) {
            tag.putInt(NBT_COIN_RECHARGE_TIMER, 0);
        }

        if (overheatTimer > 0) {
            tag.putInt(NBT_COIN_OVERHEAT_TIMER, overheatTimer - 1);
        }

        int chainTimer = tag.getInt(NBT_COIN_CHAIN_TIMER);
        if (chainTimer > 0) {
            tag.putInt(NBT_COIN_CHAIN_TIMER, chainTimer - 1);
        } else if (tag.getInt(NBT_COIN_CHAIN_HITS) > 0) {
            tag.putInt(NBT_COIN_CHAIN_HITS, 0);
        }

        int cooldown = tag.getInt(NBT_COIN_THROW_COOLDOWN);
        if (cooldown > 0) {
            tag.putInt(NBT_COIN_THROW_COOLDOWN, cooldown - 1);
        }

        int priorityTargetTimer = tag.getInt(NBT_PRIORITY_TARGET_TIMER);
        if (priorityTargetTimer > 0) {
            tag.putInt(NBT_PRIORITY_TARGET_TIMER, priorityTargetTimer - 1);
        } else if (tag.contains(NBT_PRIORITY_TARGET_ID)) {
            tag.remove(NBT_PRIORITY_TARGET_ID);
        }

        if (tag.getInt(NBT_COIN_THROW_QUEUE) > 0 && tag.getInt(NBT_COIN_THROW_COOLDOWN) <= 0) {
            tag.putInt(NBT_COIN_THROW_QUEUE, tag.getInt(NBT_COIN_THROW_QUEUE) - 1);
            tryThrowCoin(level, player, stack, false);
        }
    }

    @Override
    public boolean canUseGunSkill(ServerPlayer player, InteractionHand hand, ItemStack stack) {
        return stack.getItem() instanceof Hellforge;
    }

    @Override
    public void useGunSkill(ServerPlayer player, InteractionHand hand, ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (tag.getInt(NBT_COIN_THROW_COOLDOWN) > 0) {
            int queue = tag.getInt(NBT_COIN_THROW_QUEUE);
            if (queue < MAX_THROW_QUEUE && tag.getInt(NBT_COINS) > queue) {
                tag.putInt(NBT_COIN_THROW_QUEUE, queue + 1);
            }
            return;
        }
        tryThrowCoin(player.level(), player, stack, true);
    }

    private static boolean tryThrowCoin(Level level, ServerPlayer player, ItemStack stack, boolean showFailure) {
        CompoundTag tag = stack.getOrCreateTag();
        int coins = tag.getInt(NBT_COINS);
        if (coins <= 0) {
            if (showFailure) {
                player.displayClientMessage(Component.translatable("message.gwrexpansions.no_coins"), true);
            }
            return false;
        }

        tag.putInt(NBT_COINS, coins - 1);
        if (coins >= MAX_COINS) {
            tag.putInt(NBT_COIN_RECHARGE_TIMER, 0);
        }
        int cooldown = tag.getInt(NBT_COIN_CHAIN_HITS) >= 5 ? CHAIN_THROW_COOLDOWN_TICKS : BASE_THROW_COOLDOWN_TICKS;
        tag.putInt(NBT_COIN_THROW_COOLDOWN, cooldown);
        throwCoin(level, player);
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
            GWRESounds.HELLFORGE_REVOLVER_COIN_FLIP.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        return true;
    }



    /**
      *
     */
    public static void throwCoin(Level level, Player player) {
        if (!level.isClientSide) {
            CoinEntity coin = new CoinEntity(level, player);
            LivingEntity intentTarget = CoinTargetUtils.findPlayerLookIntentTarget(level, player, 32.0D);
            if (intentTarget != null) {
                coin.getPersistentData().putInt(COIN_INTENT_TARGET_ID, intentTarget.getId());
                recordPriorityTarget(player, intentTarget);
            }

            Vec3 eyePos = player.getEyePosition();
            Vec3 lookDirection = player.getLookAngle();
            Vec3 right = lookDirection.cross(new Vec3(0, 1, 0)).normalize();
            if (Double.isNaN(right.x) || Double.isNaN(right.y) || Double.isNaN(right.z)) {
                right = new Vec3(1, 0, 0);
            }
            Vec3 spawnPos = eyePos.add(lookDirection.scale(0.8)).add(right.scale(0.12)).add(0, 0.08, 0);
            coin.setPos(spawnPos.x, spawnPos.y, spawnPos.z);

            Vec3 throwDirection = lookDirection.add(0, 0.28, 0).add(right.scale(0.08)).normalize();
            Vec3 playerVelocity = player.getDeltaMovement();
            double horizontalFactor = player.onGround() ? 0.6 : 0.35;
            Vec3 inheritedVelocity = new Vec3(playerVelocity.x * horizontalFactor, playerVelocity.y * 0.15, playerVelocity.z * horizontalFactor);
            double throwSpeed = 0.55;
            coin.setDeltaMovement(throwDirection.scale(throwSpeed).add(inheritedVelocity));

            level.addFreshEntity(coin);

            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 0.8F);
        }
    }

    public static ItemStack findHellforgeStack(LivingEntity entity) {
        ItemStack mainHand = entity.getMainHandItem();
        if (mainHand.getItem() instanceof Hellforge) {
            return mainHand;
        }
        ItemStack offHand = entity.getOffhandItem();
        if (offHand.getItem() instanceof Hellforge) {
            return offHand;
        }
        return ItemStack.EMPTY;
    }

    public static int recordCoinHit(LivingEntity owner) {
        return recordCoinHit(owner, true, true);
    }

    public static int recordCoinHit(LivingEntity owner, boolean awardCoin, boolean resetFireCooldown) {
        ItemStack stack = findHellforgeStack(owner);
        if (stack.isEmpty()) {
            return 1;
        }

        CompoundTag tag = stack.getOrCreateTag();
        int hits = tag.getInt(NBT_COIN_CHAIN_TIMER) > 0 ? tag.getInt(NBT_COIN_CHAIN_HITS) + 1 : 1;
        tag.putInt(NBT_COIN_CHAIN_HITS, hits);
        tag.putInt(NBT_COIN_CHAIN_TIMER, COIN_CHAIN_WINDOW_TICKS);
        if (awardCoin) {
            addCoins(stack, 1);
        }
        if (resetFireCooldown && owner instanceof Player player) {
            player.getCooldowns().removeCooldown(stack.getItem());
        }
        return hits;
    }

    public static void breakCoinChain(LivingEntity owner) {
        ItemStack stack = findHellforgeStack(owner);
        if (stack.isEmpty()) {
            return;
        }
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(NBT_COIN_CHAIN_HITS, 0);
        tag.putInt(NBT_COIN_CHAIN_TIMER, 0);
        if (owner instanceof ServerPlayer player) {
            GWRENetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new CoinHitFeedbackPacket(0, 0, tag.getInt(NBT_COIN_OVERHEAT_TIMER)));
        }
    }

    public static void addCoins(ItemStack stack, int amount) {
        if (stack.isEmpty()) {
            return;
        }
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(NBT_COINS, Math.min(MAX_COINS, tag.getInt(NBT_COINS) + amount));
        if (tag.getInt(NBT_COINS) >= MAX_COINS) {
            tag.putInt(NBT_COIN_RECHARGE_TIMER, 0);
        }
    }

    public static void advanceCoinRecharge(ItemStack stack, int ticks) {
        if (stack.isEmpty() || ticks <= 0) {
            return;
        }
        CompoundTag tag = stack.getOrCreateTag();
        int coins = tag.getInt(NBT_COINS);
        if (coins >= MAX_COINS) {
            tag.putInt(NBT_COIN_RECHARGE_TIMER, 0);
            return;
        }

        int rechargeTimer = tag.getInt(NBT_COIN_RECHARGE_TIMER) + ticks;
        while (coins < MAX_COINS && rechargeTimer >= COIN_RECHARGE_TICKS) {
            coins++;
            rechargeTimer -= COIN_RECHARGE_TICKS;
        }
        tag.putInt(NBT_COINS, coins);
        tag.putInt(NBT_COIN_RECHARGE_TIMER, coins >= MAX_COINS ? 0 : rechargeTimer);
    }

    public static void triggerCoinOverheat(LivingEntity owner, int coinLinkHits) {
        ItemStack stack = findHellforgeStack(owner);
        if (stack.isEmpty() || coinLinkHits < 3) {
            return;
        }
        CompoundTag tag = stack.getOrCreateTag();
        int previousOverheatTimer = tag.getInt(NBT_COIN_OVERHEAT_TIMER);
        int duration = coinLinkHits >= 4 ? COIN_STRONG_OVERHEAT_TICKS : COIN_OVERHEAT_TICKS;
        tag.putInt(NBT_COIN_OVERHEAT_TIMER, Math.max(previousOverheatTimer, duration));
        if (coinLinkHits >= 4) {
            advanceCoinRecharge(stack, COIN_STRONG_OVERHEAT_RECHARGE_ADVANCE_TICKS);
        }
    }
    public static String getCoinChainGrade(int hits) {
        if (hits >= 5) return "S";
        if (hits == 4) return "A";
        if (hits == 3) return "B";
        if (hits == 2) return "C";
        return "D";
    }

    public static double getCoinDamageMultiplier(int hits) {
        if (hits >= 5) return 2.3;
        if (hits == 4) return 2.05;
        if (hits == 3) return 1.8;
        if (hits == 2) return 1.55;
        return 1.35;
    }

    public static double getCoinLinkDamageMultiplier(int chainHits, int coinLinkHits) {
        return getCoinDamageMultiplier(chainHits) * getCoinLinkMultiplier(coinLinkHits);
    }

    public static double getCoinLinkMultiplier(int coinLinkHits) {
        int cappedLinkHits = Math.max(1, Math.min(coinLinkHits, 4));
        return switch (cappedLinkHits) {
            case 4 -> 3.0D;
            case 3 -> 2.1D;
            case 2 -> 1.5D;
            default -> 1.0D;
        };
    }

    public static int getCoinReturnAmount(int chainHits, int coinLinkHits) {
        int cappedLinkHits = Math.max(1, Math.min(coinLinkHits, 4));
        int linkReturn = switch (cappedLinkHits) {
            case 4 -> 3;
            case 3 -> 2;
            case 2 -> 1;
            default -> 0;
        };
        int gradeReturn = chainHits >= 3 ? 1 : 0;
        return Math.max(gradeReturn, linkReturn);
    }

    public static int getCoinRechargeAdvanceForLink(int coinLinkHits) {
        int cappedLinkHits = Math.max(1, Math.min(coinLinkHits, 4));
        return cappedLinkHits <= 1 ? COIN_HIT_RECHARGE_ADVANCE_TICKS : COIN_LINK_RECHARGE_ADVANCE_TICKS;
    }

    public static double getCoinCopyDamageRatio(int coinLinkHits) {
        int cappedLinkHits = Math.max(1, Math.min(coinLinkHits, 4));
        if (cappedLinkHits >= 4) {
            return 0.75D;
        }
        if (cappedLinkHits >= 3) {
            return 0.6D;
        }
        return 0.5D;
    }

    public static int getPriorityTargetId(LivingEntity owner) {
        ItemStack stack = findHellforgeStack(owner);
        if (stack.isEmpty()) {
            return -1;
        }
        CompoundTag tag = stack.getOrCreateTag();
        return tag.getInt(NBT_PRIORITY_TARGET_TIMER) > 0 ? tag.getInt(NBT_PRIORITY_TARGET_ID) : -1;
    }

    public static void recordPriorityTarget(LivingEntity owner, LivingEntity target) {
        ItemStack stack = findHellforgeStack(owner);
        if (stack.isEmpty() || target == owner || !target.isAlive()) {
            return;
        }
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(NBT_PRIORITY_TARGET_ID, target.getId());
        tag.putInt(NBT_PRIORITY_TARGET_TIMER, PRIORITY_TARGET_TICKS);
    }

    @Override
    protected void affectBulletEntity(LivingEntity shooter, ItemStack gun, BulletEntity bullet, boolean bulletFree) {
        super.affectBulletEntity(shooter, gun, bullet, bulletFree);

        CompoundTag bulletData = bullet.getPersistentData();
        bulletData.putBoolean("HellforgeShot", true);
        bulletData.putUUID(BULLET_SHOOTER_UUID, shooter.getUUID());
        if (gun.getOrCreateTag().getInt(NBT_COIN_CHAIN_HITS) > 0) {
            bulletData.putInt(BULLET_COIN_MISS_GRACE, 10);
        }
        bullet.setHeadshotMultiplier(getHeadshotMultiplier(gun, shooter));
    }

    /**
      *
      *
     */
    public static void onBulletHitLivingEntity(BulletEntity bullet, LivingEntity target, @Nullable Entity shooter) {
        if (bullet.getPersistentData().getBoolean("HellforgeShot") && shooter instanceof LivingEntity livingShooter) {
            recordPriorityTarget(livingShooter, target);
        }
        // Hellforge coin damage no longer depends on applying target-side mob effects.
    }


    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.gwrexpansions.hellforge_revolver.line1").withStyle(ChatFormatting.RED));
        tooltip.add(Component.translatable("tooltip.gwrexpansions.hellforge_revolver.line2").withStyle(ChatFormatting.RED));
        tooltip.add(Component.translatable("tooltip.gwrexpansions.hellforge_revolver.line3").withStyle(ChatFormatting.RED));
    }



    @Override
    protected void addExtraStatsTooltip(ItemStack stack, @Nullable Level world, List<Component> tooltip) {
        tooltip.add(Component.translatable("tooltip.gwrexpansions.hellforge_revolver.desc1").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.gwrexpansions.hellforge_revolver.desc2").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.gwrexpansions.hellforge_revolver.desc3").withStyle(ChatFormatting.GRAY));

        CompoundTag tag = stack.getOrCreateTag();
        int coins = tag.getInt(NBT_COINS);
        int recharge = tag.getInt(NBT_COIN_RECHARGE_TIMER);

        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("tooltip.gwrexpansions.hellforge_revolver.coins", coins, MAX_COINS)
            .withStyle(coins > 0 ? (coins >= MAX_COINS ? ChatFormatting.GOLD : ChatFormatting.YELLOW) : ChatFormatting.RED));
        if (coins < MAX_COINS) {
            tooltip.add(Component.translatable("tooltip.gwrexpansions.hellforge_revolver.coin_recharge", recharge, COIN_RECHARGE_TICKS)
                .withStyle(ChatFormatting.GRAY));
        }

        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("tooltip.gwrexpansions.hellforge_revolver.usage").withStyle(ChatFormatting.YELLOW));
    }
}
