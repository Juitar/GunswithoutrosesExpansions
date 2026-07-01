package juitar.gwrexpansions.item.BOMD;

import juitar.gwrexpansions.config.GWREConfig;
import juitar.gwrexpansions.client.render.HellforgeGeoRenderer;
import juitar.gwrexpansions.entity.BOMD.CoinEntity;
import juitar.gwrexpansions.item.ConfigurableGunItem;
import juitar.gwrexpansions.item.GunSkillItem;
import juitar.gwrexpansions.item.GunSkillTooltip;
import juitar.gwrexpansions.network.CoinHitFeedbackPacket;
import juitar.gwrexpansions.network.GWRENetwork;
import juitar.gwrexpansions.registry.GWRECataclysmEnchantments;
import juitar.gwrexpansions.registry.GWRESounds;
import juitar.gwrexpansions.util.CoinTargetUtils;
import net.minecraft.server.level.ServerPlayer;
import lykrast.gunswithoutroses.entity.BulletEntity;
import lykrast.gunswithoutroses.item.IBullet;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.network.PacketDistributor;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Hellforge extends ConfigurableGunItem implements GunSkillItem, GeoItem {
    public static final String NBT_COINS = "Coins";
    public static final String NBT_COIN_RECHARGE_TIMER = "CoinRechargeTimer";
    public static final String NBT_COIN_CHAIN_HITS = "CoinChainHits";
    public static final String NBT_COIN_CHAIN_TIMER = "CoinChainTimer";
    public static final String NBT_COIN_THROW_QUEUE = "CoinThrowQueue";
    public static final String NBT_COIN_THROW_COOLDOWN = "CoinThrowCooldown";
    public static final String NBT_PRIORITY_TARGET_ID = "PriorityTargetId";
    public static final String NBT_PRIORITY_TARGET_TIMER = "PriorityTargetTimer";
    public static final String NBT_COIN_OVERHEAT_TIMER = "CoinOverheatTimer";
    public static final String NBT_STYLE_SCORE = "StyleScore";
    public static final String NBT_STYLE_TIMER = "StyleTimer";
    public static final String NBT_STYLE_HEAT = "StyleHeat";
    public static final String NBT_HEAT_KEEP_TIMER = "HeatKeepTimer";
    public static final String NBT_OVERHEAT_LOCKOUT_TIMER = "OverheatLockoutTimer";
    public static final String NBT_GECKO_ANIMATION = "HellforgeGeckoAnimation";
    public static final String NBT_GECKO_ANIMATION_TIMER = "HellforgeGeckoAnimationTimer";
    public static final String NBT_GECKO_ANIMATION_SEQUENCE = "HellforgeGeckoAnimationSequence";
    public static final String NBT_LAST_STYLE_EVENT = "LastStyleEvent";
    public static final String NBT_LAST_STYLE_EVENT_COUNT = "LastStyleEventCount";
    public static final String NBT_RECENT_KILL_TIMER = "RecentKillTimer";
    public static final String NBT_RECENT_KILL_COUNT = "RecentKillCount";
    public static final String COIN_INTENT_TARGET_ID = "HellforgeIntentTargetId";
    public static final String BULLET_COIN_MISS_GRACE = "HellforgeCoinMissGrace";
    public static final String BULLET_SHOOTER_UUID = "HellforgeShooterUuid";
    public static final String BULLET_HEADSHOT = "HellforgeHeadshot";
    public static final String BULLET_LAST_TARGET_HEALTH = "HellforgeLastTargetHealth";
    public static final String BULLET_LAST_TARGET_MAX_HEALTH = "HellforgeLastTargetMaxHealth";
    public static final int PRIORITY_TARGET_TICKS = 100;
    private static final int STYLE_HEAT_TRIGGER = 100;
    private static final int STYLE_STRONG_HEAT_TRIGGER = 140;
    private static final int STYLE_MAX_WINDOW_TICKS = 110;
    private static final int OVERHEAT_LOCKOUT_TICKS = 40;
    private static final int STRONG_OVERHEAT_LOCKOUT_TICKS = 60;
    private static final String GECKO_CONTROLLER = "controller";
    private static final String GECKO_ANIM_FIRE = "fire";
    private static final String GECKO_ANIM_COIN = "coin";
    private static final String GECKO_ANIM_ROTATE_FIRE = "rotate_fire";
    private static final int GECKO_FIRE_TICKS = 8;
    private static final int GECKO_COIN_TICKS = 11;
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation FIRE_ANIM = RawAnimation.begin().thenPlay("fire");
    private static final RawAnimation COIN_ANIM = RawAnimation.begin().thenPlay("coin");
    private static final RawAnimation ROTATE_ANIM = RawAnimation.begin().thenLoop("rotate");
    private static final RawAnimation ROTATE_FIRE_ANIM = RawAnimation.begin().thenPlay("rotate+fire");
    private static final Map<Long, Integer> LAST_SEEN_GECKO_SEQUENCE = new ConcurrentHashMap<>();
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

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
        GeoItem.registerSyncedAnimatable(this);
    }

    public enum StyleEvent {
        COIN_HIT("COIN_HIT"),
        COIN_CHAIN("COIN_CHAIN"),
        HEADSHOT("HEADSHOT"),
        KILL(""),
        HEADSHOT_KILL("HEADSHOT_KILL"),
        ONE_SHOT("ONE_SHOT"),
        RICOSHOT_KILL("RICOSHOT_KILL"),
        DOUBLE_KILL("DOUBLE_KILL"),
        TRIPLE_KILL("TRIPLE_KILL"),
        OVERHEAT("OVERHEAT"),
        MAX_OVERHEAT("MAX_OVERHEAT");

        private final String feedbackKey;

        StyleEvent(String feedbackKey) {
            this.feedbackKey = feedbackKey;
        }
    }

    @Override
    protected void shoot(Level level, Player player, ItemStack gun, ItemStack ammo, IBullet bulletItem, boolean bulletFree) {
        super.shoot(level, player, gun, ammo, bulletItem, bulletFree);
        ensureGeckoId(gun, level);
        setGeckoAnimation(gun, isOverheated(gun) ? GECKO_ANIM_ROTATE_FIRE : GECKO_ANIM_FIRE, GECKO_FIRE_TICKS);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private HellforgeGeoRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new HellforgeGeoRenderer();
                }
                return this.renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, GECKO_CONTROLLER, 0, state -> {
            ItemStack stack = state.getData(software.bernie.geckolib.constant.DataTickets.ITEMSTACK);
            restartAnimationIfSequenceChanged(state, stack);
            state.setAnimation(getGeckoAnimation(stack));
            return PlayState.CONTINUE;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public double getHeadshotMultiplier(ItemStack stack, @Nullable LivingEntity shooter) {
        return stack.getOrCreateTag().getInt(NBT_COIN_OVERHEAT_TIMER) > 0 ? hellforgeConfig().overheatHeadshotMultiplier.get() : 1.0D;
    }


    @Override
    public double getDamageMultiplier(ItemStack stack, @Nullable LivingEntity shooter) {
        double multiplier = super.getDamageMultiplier(stack, shooter);
        return stack.getOrCreateTag().getInt(NBT_COIN_OVERHEAT_TIMER) > 0 ? multiplier * hellforgeConfig().overheatDamageMultiplier.get() : multiplier;
    }

    @Override
    public int getFireDelay(ItemStack stack, @Nullable LivingEntity shooter) {
        int delay = super.getFireDelay(stack, shooter);
        CompoundTag tag = stack.getOrCreateTag();
        double multiplier = tag.getInt(NBT_COIN_CHAIN_TIMER) > 0
            ? getCoinFireDelayMultiplier(tag.getInt(NBT_COIN_CHAIN_HITS))
            : 1.0D;
        if (tag.getInt(NBT_COIN_OVERHEAT_TIMER) > 0) {
            multiplier *= hellforgeConfig().overheatFireDelayMultiplier.get();
        }
        return Math.max(1, (int)Math.ceil(delay * multiplier));
    }
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return super.use(level, player, hand);
    }


    @Override
    public void inventoryTick(ItemStack stack, Level level, net.minecraft.world.entity.Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        tickGeckoAnimation(stack);

        if (level.isClientSide || !(entity instanceof ServerPlayer player)) {
            return;
        }
        ensureGeckoId(stack, level);

        CompoundTag tag = stack.getOrCreateTag();
        int overheatTimer = tag.getInt(NBT_COIN_OVERHEAT_TIMER);
        int coins = tag.getInt(NBT_COINS);
        if (coins < getMaxCoins(stack)) {
            int rechargeTimer = tag.getInt(NBT_COIN_RECHARGE_TIMER) + (overheatTimer > 0 ? 2 : 1);
            if (rechargeTimer >= getCoinRechargeTicks()) {
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

        int styleTimer = tag.getInt(NBT_STYLE_TIMER);
        if (styleTimer > 0) {
            tag.putInt(NBT_STYLE_TIMER, styleTimer - 1);
        } else if (tag.getInt(NBT_STYLE_SCORE) > 0) {
            tag.putInt(NBT_STYLE_SCORE, 0);
            tag.putString(NBT_LAST_STYLE_EVENT, "");
            tag.putInt(NBT_LAST_STYLE_EVENT_COUNT, 0);
        }

        int heatKeepTimer = tag.getInt(NBT_HEAT_KEEP_TIMER);
        if (heatKeepTimer > 0) {
            tag.putInt(NBT_HEAT_KEEP_TIMER, heatKeepTimer - 1);
        } else if (tag.getInt(NBT_STYLE_HEAT) > 0) {
            tag.putInt(NBT_STYLE_HEAT, Math.max(0, tag.getInt(NBT_STYLE_HEAT) - 2));
        }

        int lockoutTimer = tag.getInt(NBT_OVERHEAT_LOCKOUT_TIMER);
        if (lockoutTimer > 0) {
            tag.putInt(NBT_OVERHEAT_LOCKOUT_TIMER, lockoutTimer - 1);
        }

        int recentKillTimer = tag.getInt(NBT_RECENT_KILL_TIMER);
        if (recentKillTimer > 0) {
            tag.putInt(NBT_RECENT_KILL_TIMER, recentKillTimer - 1);
        } else if (tag.getInt(NBT_RECENT_KILL_COUNT) > 0) {
            tag.putInt(NBT_RECENT_KILL_COUNT, 0);
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
            if (queue < hellforgeConfig().maxThrowQueue.get() && tag.getInt(NBT_COINS) > queue) {
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
        if (coins >= getMaxCoins(stack)) {
            tag.putInt(NBT_COIN_RECHARGE_TIMER, 0);
        }
        int cooldown = tag.getInt(NBT_COIN_CHAIN_HITS) >= 5 ? hellforgeConfig().chainThrowCooldownTicks.get() : hellforgeConfig().baseThrowCooldownTicks.get();
        tag.putInt(NBT_COIN_THROW_COOLDOWN, cooldown);
        throwCoin(level, player);
        ensureGeckoId(stack, level);
        setGeckoAnimation(stack, GECKO_ANIM_COIN, GECKO_COIN_TICKS);
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


    public static GWREConfig.HellforgeConfig hellforgeConfig() {
        return GWREConfig.PISTOL.hellforge;
    }

    private static boolean isOverheated(@Nullable ItemStack stack) {
        return stack != null && stack.hasTag() && stack.getOrCreateTag().getInt(NBT_COIN_OVERHEAT_TIMER) > 0;
    }

    private static void ensureGeckoId(ItemStack stack, Level level) {
        if (stack.isEmpty() || !(stack.getItem() instanceof Hellforge) || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        GeoItem.getOrAssignId(stack, serverLevel);
    }

    private static void setGeckoAnimation(ItemStack stack, String animation, int ticks) {
        if (stack.isEmpty()) {
            return;
        }

        CompoundTag tag = stack.getOrCreateTag();
        tag.putString(NBT_GECKO_ANIMATION, animation);
        tag.putInt(NBT_GECKO_ANIMATION_TIMER, ticks);
        tag.putInt(NBT_GECKO_ANIMATION_SEQUENCE, tag.getInt(NBT_GECKO_ANIMATION_SEQUENCE) + 1);
    }

    private static void tickGeckoAnimation(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTag()) {
            return;
        }

        CompoundTag tag = stack.getOrCreateTag();
        int timer = tag.getInt(NBT_GECKO_ANIMATION_TIMER);
        if (timer > 1) {
            tag.putInt(NBT_GECKO_ANIMATION_TIMER, timer - 1);
        } else if (timer == 1) {
            tag.putInt(NBT_GECKO_ANIMATION_TIMER, 0);
            tag.remove(NBT_GECKO_ANIMATION);
        }
    }

    private static RawAnimation getGeckoAnimation(@Nullable ItemStack stack) {
        if (stack != null && stack.hasTag() && stack.getOrCreateTag().getInt(NBT_GECKO_ANIMATION_TIMER) > 0) {
            String animation = stack.getOrCreateTag().getString(NBT_GECKO_ANIMATION);
            if (GECKO_ANIM_COIN.equals(animation)) {
                return COIN_ANIM;
            }
            if (GECKO_ANIM_ROTATE_FIRE.equals(animation)) {
                return ROTATE_FIRE_ANIM;
            }
            if (GECKO_ANIM_FIRE.equals(animation)) {
                return FIRE_ANIM;
            }
        }

        return isOverheated(stack) ? ROTATE_ANIM : IDLE_ANIM;
    }

    private static void restartAnimationIfSequenceChanged(AnimationState<Hellforge> state, @Nullable ItemStack stack) {
        if (stack == null || stack.isEmpty() || !stack.hasTag()) {
            return;
        }

        CompoundTag tag = stack.getOrCreateTag();
        int sequence = tag.getInt(NBT_GECKO_ANIMATION_SEQUENCE);
        long id = GeoItem.getId(stack);
        long key = id != 0L ? id : System.identityHashCode(stack);
        Integer previous = LAST_SEEN_GECKO_SEQUENCE.put(key, sequence);
        if (previous != null && previous != sequence) {
            state.getController().forceAnimationReset();
        }
    }

    public static int getMaxCoins() {
        return hellforgeConfig().maxCoins.get();
    }

    public static int getMaxCoins(ItemStack stack) {
        return getMaxCoins()
            + (GWRECataclysmEnchantments.has(stack, GWRECataclysmEnchantments.COIN_RESERVE) ? 2 : 0);
    }

    public static int getCoinRechargeTicks() {
        return hellforgeConfig().coinRechargeTicks.get();
    }

    public static int getCoinChainWindowTicks() {
        return hellforgeConfig().coinChainWindowTicks.get();
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
        tag.putInt(NBT_COIN_CHAIN_TIMER, getCoinChainWindowTicks());
        if (awardCoin) {
            addCoins(stack, 1);
        }
        if (hits >= 4 && resetFireCooldown) {
            clearFireCooldown(owner);
        }
        return hits;
    }

    public static void recordStyleEvent(LivingEntity owner, StyleEvent event, int value, @Nullable LivingEntity target) {
        ItemStack stack = findHellforgeStack(owner);
        if (stack.isEmpty() || event == null) {
            return;
        }

        CompoundTag tag = stack.getOrCreateTag();
        String eventName = event.name();
        int repeatCount = eventName.equals(tag.getString(NBT_LAST_STYLE_EVENT))
            ? tag.getInt(NBT_LAST_STYLE_EVENT_COUNT) + 1
            : 1;
        tag.putString(NBT_LAST_STYLE_EVENT, eventName);
        tag.putInt(NBT_LAST_STYLE_EVENT_COUNT, repeatCount);

        double multiplier = repeatCount > 3 ? 0.7D : 1.0D;
        if (isKillEvent(event) && target != null && target.getMaxHealth() < 10.0F) {
            multiplier *= 0.5D;
        }

        int previousHeatKeepTimer = tag.getInt(NBT_HEAT_KEEP_TIMER);
        int scoreGain = Math.max(1, (int)Math.round(getStyleScoreGain(event, value) * multiplier));
        int heatGain = Math.max(0, (int)Math.round(getStyleHeatGain(event, value) * multiplier));
        int previousScore = tag.getInt(NBT_STYLE_TIMER) > 0 ? tag.getInt(NBT_STYLE_SCORE) : 0;
        int styleScore = Math.max(0, previousScore + scoreGain);
        tag.putInt(NBT_STYLE_SCORE, styleScore);

        String grade = getCoinChainGrade(styleScore);
        int window = Math.min(STYLE_MAX_WINDOW_TICKS, getStyleWindowTicks(grade) + getStyleEventWindowBonus(event, value));
        tag.putInt(NBT_STYLE_TIMER, window);
        tag.putInt(NBT_HEAT_KEEP_TIMER, window);

        int heat = previousHeatKeepTimer > 0 ? tag.getInt(NBT_STYLE_HEAT) + heatGain : heatGain;
        tag.putInt(NBT_STYLE_HEAT, Math.max(0, heat));

        StyleEvent feedbackEvent = event;
        int overheatTimer = tag.getInt(NBT_COIN_OVERHEAT_TIMER);
        if (heat >= STYLE_STRONG_HEAT_TRIGGER) {
            overheatTimer = triggerStyleOverheat(stack, true);
            feedbackEvent = StyleEvent.MAX_OVERHEAT;
        } else if (heat >= STYLE_HEAT_TRIGGER) {
            overheatTimer = triggerStyleOverheat(stack, false);
            feedbackEvent = StyleEvent.OVERHEAT;
        }

        sendStyleFeedback(owner, tag, feedbackEvent, value);
    }

    private static int triggerStyleOverheat(ItemStack stack, boolean strong) {
        CompoundTag tag = stack.getOrCreateTag();
        int duration = strong ? hellforgeConfig().coinStrongOverheatTicks.get() : hellforgeConfig().coinOverheatTicks.get();
        tag.putInt(NBT_COIN_OVERHEAT_TIMER, Math.max(tag.getInt(NBT_COIN_OVERHEAT_TIMER), duration));
        tag.putInt(NBT_STYLE_HEAT, strong ? 0 : Math.max(0, tag.getInt(NBT_STYLE_HEAT) - STYLE_HEAT_TRIGGER));
        tag.putInt(NBT_OVERHEAT_LOCKOUT_TIMER, strong ? STRONG_OVERHEAT_LOCKOUT_TICKS : OVERHEAT_LOCKOUT_TICKS);
        if (strong) {
            advanceCoinRecharge(stack, hellforgeConfig().coinStrongOverheatRechargeAdvanceTicks.get());
        }
        return tag.getInt(NBT_COIN_OVERHEAT_TIMER);
    }

    private static void sendStyleFeedback(LivingEntity owner, CompoundTag tag, StyleEvent event, int eventValue) {
        if (!(owner instanceof ServerPlayer player)) {
            return;
        }
        int score = tag.getInt(NBT_STYLE_SCORE);
        int timer = tag.getInt(NBT_STYLE_TIMER);
        GWRENetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
            new CoinHitFeedbackPacket(score, timer, tag.getInt(NBT_COIN_OVERHEAT_TIMER),
                score, tag.getInt(NBT_STYLE_HEAT), tag.getInt(NBT_HEAT_KEEP_TIMER), event.feedbackKey, eventValue));
    }

    private static boolean isKillEvent(StyleEvent event) {
        return event == StyleEvent.KILL || event == StyleEvent.HEADSHOT_KILL || event == StyleEvent.ONE_SHOT
            || event == StyleEvent.RICOSHOT_KILL || event == StyleEvent.DOUBLE_KILL || event == StyleEvent.TRIPLE_KILL;
    }

    private static int getStyleScoreGain(StyleEvent event, int value) {
        return switch (event) {
            case COIN_HIT -> 8;
            case COIN_CHAIN -> Math.min(60, 8 + Math.max(2, value) * 10);
            case HEADSHOT -> 10;
            case KILL -> 14;
            case HEADSHOT_KILL, ONE_SHOT -> 24;
            case RICOSHOT_KILL -> 28;
            case DOUBLE_KILL -> 20;
            case TRIPLE_KILL -> 32;
            case OVERHEAT -> 0;
            case MAX_OVERHEAT -> 0;
        };
    }

    private static int getStyleHeatGain(StyleEvent event, int value) {
        return switch (event) {
            case COIN_HIT -> 8;
            case COIN_CHAIN -> {
                int links = Math.max(2, value);
                yield links >= 4 ? 55 : links == 3 ? 35 : 18;
            }
            case HEADSHOT -> 8;
            case KILL -> 10;
            case HEADSHOT_KILL, ONE_SHOT -> 18;
            case RICOSHOT_KILL -> 24;
            case DOUBLE_KILL -> 20;
            case TRIPLE_KILL -> 28;
            case OVERHEAT -> 0;
            case MAX_OVERHEAT -> 0;
        };
    }

    private static int getStyleEventWindowBonus(StyleEvent event, int value) {
        return switch (event) {
            case HEADSHOT -> 8;
            case KILL -> 10;
            case HEADSHOT_KILL, ONE_SHOT, RICOSHOT_KILL -> 16;
            case DOUBLE_KILL -> 14;
            case TRIPLE_KILL -> 18;
            case COIN_CHAIN -> {
                int links = Math.max(2, value);
                yield links >= 4 ? 20 : links == 3 ? 14 : 8;
            }
            default -> 0;
        };
    }

    public static void clearFireCooldown(LivingEntity owner) {
        ItemStack stack = findHellforgeStack(owner);
        if (!stack.isEmpty() && owner instanceof Player player) {
            player.getCooldowns().removeCooldown(stack.getItem());
        }
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
                new CoinHitFeedbackPacket(0, 0, tag.getInt(NBT_COIN_OVERHEAT_TIMER),
                    tag.getInt(NBT_STYLE_SCORE), tag.getInt(NBT_STYLE_HEAT),
                    tag.getInt(NBT_HEAT_KEEP_TIMER), "", 0));
        }
    }

    public static void addCoins(ItemStack stack, int amount) {
        if (stack.isEmpty()) {
            return;
        }
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(NBT_COINS, Math.min(getMaxCoins(stack), tag.getInt(NBT_COINS) + amount));
        if (tag.getInt(NBT_COINS) >= getMaxCoins(stack)) {
            tag.putInt(NBT_COIN_RECHARGE_TIMER, 0);
        }
    }

    public static void advanceCoinRecharge(ItemStack stack, int ticks) {
        if (stack.isEmpty() || ticks <= 0) {
            return;
        }
        CompoundTag tag = stack.getOrCreateTag();
        int coins = tag.getInt(NBT_COINS);
        if (coins >= getMaxCoins(stack)) {
            tag.putInt(NBT_COIN_RECHARGE_TIMER, 0);
            return;
        }

        int rechargeTimer = tag.getInt(NBT_COIN_RECHARGE_TIMER) + ticks;
        while (coins < getMaxCoins(stack) && rechargeTimer >= getCoinRechargeTicks()) {
            coins++;
            rechargeTimer -= getCoinRechargeTicks();
        }
        tag.putInt(NBT_COINS, coins);
        tag.putInt(NBT_COIN_RECHARGE_TIMER, coins >= getMaxCoins(stack) ? 0 : rechargeTimer);
    }

    public static void triggerCoinOverheat(LivingEntity owner, int coinLinkHits) {
        ItemStack stack = findHellforgeStack(owner);
        if (stack.isEmpty() || coinLinkHits < 3) {
            return;
        }
        CompoundTag tag = stack.getOrCreateTag();
        int previousOverheatTimer = tag.getInt(NBT_COIN_OVERHEAT_TIMER);
        int duration = coinLinkHits >= 4 ? hellforgeConfig().coinStrongOverheatTicks.get() : hellforgeConfig().coinOverheatTicks.get();
        tag.putInt(NBT_COIN_OVERHEAT_TIMER, Math.max(previousOverheatTimer, duration));
        if (coinLinkHits >= 4) {
            advanceCoinRecharge(stack, hellforgeConfig().coinStrongOverheatRechargeAdvanceTicks.get());
        }
    }
    public static String getCoinChainGrade(int score) {
        if (score >= 100) return "S";
        if (score >= 65) return "A";
        if (score >= 35) return "B";
        if (score >= 15) return "C";
        return "D";
    }

    public static int getStyleWindowTicks(String grade) {
        return switch (grade) {
            case "S" -> 50;
            case "A" -> 60;
            case "B" -> 70;
            case "C" -> 80;
            default -> 90;
        };
    }

    public static double getCoinDamageMultiplier(int hits) {
        if (hits >= 5) return hellforgeConfig().coinDamageS.get();
        if (hits == 4) return hellforgeConfig().coinDamageA.get();
        if (hits == 3) return hellforgeConfig().coinDamageB.get();
        if (hits == 2) return hellforgeConfig().coinDamageC.get();
        return hellforgeConfig().coinDamageD.get();
    }

    public static double getCoinFireDelayMultiplier(int hits) {
        if (hits >= 5) return hellforgeConfig().coinFireDelayS.get();
        if (hits == 4) return hellforgeConfig().coinFireDelayA.get();
        if (hits == 3) return hellforgeConfig().coinFireDelayB.get();
        if (hits == 2) return hellforgeConfig().coinFireDelayC.get();
        if (hits == 1) return hellforgeConfig().coinFireDelayD.get();
        return 1.0D;
    }

    public static double getCoinLinkDamageMultiplier(int chainHits, int coinLinkHits) {
        return getCoinDamageMultiplier(chainHits) * getCoinLinkMultiplier(coinLinkHits);
    }

    public static double getCoinLinkMultiplier(int coinLinkHits) {
        int cappedLinkHits = Math.max(1, Math.min(coinLinkHits, 4));
        return switch (cappedLinkHits) {
            case 4 -> hellforgeConfig().coinLinkMultiplier4.get();
            case 3 -> hellforgeConfig().coinLinkMultiplier3.get();
            case 2 -> hellforgeConfig().coinLinkMultiplier2.get();
            default -> 1.0D;
        };
    }

    public static int getCoinReturnAmount(int chainHits, int coinLinkHits) {
        int cappedLinkHits = Math.max(1, Math.min(coinLinkHits, 4));
        int linkReturn = switch (cappedLinkHits) {
            case 4 -> hellforgeConfig().coinReturnLink4.get();
            case 3 -> hellforgeConfig().coinReturnLink3.get();
            case 2 -> hellforgeConfig().coinReturnLink2.get();
            default -> 0;
        };
        int gradeReturn = chainHits >= hellforgeConfig().coinReturnGradeHits.get() ? 1 : 0;
        return Math.max(gradeReturn, linkReturn);
    }

    public static int getCoinRechargeAdvanceForLink(int coinLinkHits) {
        int cappedLinkHits = Math.max(1, Math.min(coinLinkHits, 4));
        return cappedLinkHits <= 1 ? hellforgeConfig().coinHitRechargeAdvanceTicks.get() : hellforgeConfig().coinLinkRechargeAdvanceTicks.get();
    }

    public static double getCoinCopyDamageRatio(int coinLinkHits) {
        int cappedLinkHits = Math.max(1, Math.min(coinLinkHits, 4));
        if (cappedLinkHits >= 4) {
            return hellforgeConfig().coinCopyDamageRatio4.get();
        }
        if (cappedLinkHits >= 3) {
            return hellforgeConfig().coinCopyDamageRatio3.get();
        }
        return hellforgeConfig().coinCopyDamageRatioDefault.get();
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
    public static void onBulletHeadshot(BulletEntity bullet, LivingEntity target, @Nullable Entity shooter, boolean headshot) {
        if (!headshot || !bullet.getPersistentData().getBoolean("HellforgeShot")
                || !(shooter instanceof LivingEntity livingShooter)) {
            return;
        }
        bullet.getPersistentData().putBoolean(BULLET_HEADSHOT, true);
        recordStyleEvent(livingShooter, StyleEvent.HEADSHOT, 1, target);
    }

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
        tooltip.add(Component.translatable("tooltip.gwrexpansions.hellforge_revolver.desc1",
            GunSkillTooltip.keyName()).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.gwrexpansions.hellforge_revolver.desc2").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.gwrexpansions.hellforge_revolver.desc3").withStyle(ChatFormatting.GRAY));

        CompoundTag tag = stack.getOrCreateTag();
        int coins = tag.getInt(NBT_COINS);

        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("tooltip.gwrexpansions.hellforge_revolver.coins", coins, getMaxCoins(stack))
            .withStyle(coins > 0 ? (coins >= getMaxCoins(stack) ? ChatFormatting.GOLD : ChatFormatting.YELLOW) : ChatFormatting.RED));
    }
}
