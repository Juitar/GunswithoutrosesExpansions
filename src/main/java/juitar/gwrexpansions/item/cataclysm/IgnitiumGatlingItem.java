package juitar.gwrexpansions.item.cataclysm;

import com.github.L_Ender.cataclysm.init.ModSounds;
import juitar.gwrexpansions.GWRexpansions;
import juitar.gwrexpansions.client.render.IgnitiumGatlingGeoRenderer;
import juitar.gwrexpansions.config.GWREConfig;
import juitar.gwrexpansions.entity.cataclysm.IgnitiumBulletEntity;
import juitar.gwrexpansions.entity.cataclysm.IgnitiumFireballEntity;
import juitar.gwrexpansions.network.GWRENetwork;
import juitar.gwrexpansions.network.IgnitiumStatePacket;
import juitar.gwrexpansions.item.ConfigurableGatlingItem;
import juitar.gwrexpansions.item.GunSkillItem;
import juitar.gwrexpansions.item.GunSkillTooltip;
import juitar.gwrexpansions.registry.CompatCataclysm;
import juitar.gwrexpansions.registry.GWRECataclysmEnchantments;
import lykrast.gunswithoutroses.entity.BulletEntity;
import lykrast.gunswithoutroses.item.IBullet;
import lykrast.gunswithoutroses.registry.GWRAttributes;
import lykrast.gunswithoutroses.registry.GWREnchantments;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.network.PacketDistributor;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.WeakHashMap;
import java.lang.ref.WeakReference;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** Ignis-inspired Gatling: ordinary fire builds heat, R enters a fireball mode. */
public class IgnitiumGatlingItem extends ConfigurableGatlingItem implements GunSkillItem, GeoItem {
    public enum Part {
        BODY("body"), LAVA("lava"), FLASH("flash"), SIGIL("sigil"), ABYSS_FLASH("abyss_flash"),
        BURST_FRONT("burst_front"), VENT("vent"), FIREBALL("fireball"), EMBER("ember"), SMOKE("smoke");
        Part(String ignoredLegacyName) {}
    }

    private static final String BLUE_FIRE_TICKS_TAG = "IgnitiumBlueFireTicks";
    private static final String WAS_BELOW_HALF_TAG = "IgnitiumWasBelowHalf";
    private static final double BLUE_FLAME_BRINK_DURATION_MULTIPLIER = 1.5D;
    private static final String HEAT_TAG = "IgnitiumHeat";
    private static final String LAST_HEAT_SHOT_TAG = "IgnitiumLastHeatShot";
    private static final String HEAT_COOL_TIMER_TAG = "IgnitiumNextCoolAt";
    private static final String FIREBALL_MODE_TAG = "IgnitiumFireballMode";
    private static final String MODE_DRAIN_TIMER_TAG = "IgnitiumNextDrainAt";
    private static final String ABYSS_COOLDOWN_TAG = "IgnitiumAbyssReadyAt";
    private static final String FIREBALL_READY_TAG = "IgnitiumFireballReadyAt";
    private static final String NBT_GECKO_ANIMATION = "IgnitiumGeckoAnimation";
    private static final String NBT_GECKO_ANIMATION_TIMER = "IgnitiumAnimationEndsAt";
    private static final String NBT_GECKO_ANIMATION_SEQUENCE = "IgnitiumGeckoAnimationSequence";
    private static final String NBT_SOUL_TRANSITION = "IgnitiumSoulTransition";
    private static final String NBT_SOUL_TRANSITION_END = "IgnitiumSoulTransitionEndsAt";
    private static final String NBT_SOUL_TRANSITION_SEQUENCE = "IgnitiumSoulTransitionSequence";
    private static final String NBT_SOUL_TRANSITION_FROM_SOUL = "IgnitiumSoulTransitionFromSoul";
    private static final String NBT_MODE_ENTER_START = "IgnitiumModeEntryStartsAt";
    // soul_enter / soul_exit are authored at 1.6 seconds, played at controller speed 1.
    private static final int SOUL_TRANSITION_TICKS = 32;
    private static final String GECKO_CONTROLLER = "controller";

    private static final String ANIM_IDLE = "animation.ignitium_gatling.idle";
    private static final String ANIM_IDLE_SOUL = "animation.ignitium_gatling.idle_soul";
    private static final String ANIM_FIRE = "animation.ignitium_gatling.fire";
    private static final String ANIM_FIRE_SOUL = "animation.ignitium_gatling.fire_soul";
    private static final String ANIM_ENTER = "animation.ignitium_gatling.fireball_enter";
    private static final String ANIM_ENTER_SOUL = "animation.ignitium_gatling.fireball_enter_soul";
    private static final String ANIM_FIREBALL = "animation.ignitium_gatling.fireball_shot";
    private static final String ANIM_FIREBALL_SOUL = "animation.ignitium_gatling.fireball_shot_soul";
    private static final String ANIM_ABYSS = "animation.ignitium_gatling.abyss_shot";
    private static final String ANIM_ABYSS_SOUL = "animation.ignitium_gatling.abyss_shot_soul";
    private static final String ANIM_EXIT = "animation.ignitium_gatling.fireball_exit";
    private static final String ANIM_EXIT_SOUL = "animation.ignitium_gatling.fireball_exit_soul";
    private static final String ANIM_SOUL_ENTER = "animation.ignitium_gatling.soul_enter";
    private static final String ANIM_SOUL_EXIT = "animation.ignitium_gatling.soul_exit";
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop(ANIM_IDLE);
    private static final RawAnimation IDLE_SOUL_ANIM = RawAnimation.begin().thenLoop(ANIM_IDLE_SOUL);
    private static final RawAnimation FIRE_ANIM = RawAnimation.begin().thenLoop(ANIM_FIRE);
    private static final RawAnimation FIRE_SOUL_ANIM = RawAnimation.begin().thenLoop(ANIM_FIRE_SOUL);
    private static final RawAnimation ENTER_ANIM = RawAnimation.begin().thenPlay(ANIM_ENTER);
    private static final RawAnimation ENTER_SOUL_ANIM = RawAnimation.begin().thenPlay(ANIM_ENTER_SOUL);
    private static final RawAnimation FIREBALL_ANIM = RawAnimation.begin().thenPlay(ANIM_FIREBALL);
    private static final RawAnimation FIREBALL_SOUL_ANIM = RawAnimation.begin().thenPlay(ANIM_FIREBALL_SOUL);
    private static final RawAnimation ABYSS_ANIM = RawAnimation.begin().thenPlay(ANIM_ABYSS);
    private static final RawAnimation ABYSS_SOUL_ANIM = RawAnimation.begin().thenPlay(ANIM_ABYSS_SOUL);
    private static final RawAnimation EXIT_ANIM = RawAnimation.begin().thenPlay(ANIM_EXIT);
    private static final RawAnimation EXIT_SOUL_ANIM = RawAnimation.begin().thenPlay(ANIM_EXIT_SOUL);
    private static final RawAnimation SOUL_ENTER_ANIM = RawAnimation.begin().thenPlay(ANIM_SOUL_ENTER);
    private static final RawAnimation SOUL_EXIT_ANIM = RawAnimation.begin().thenPlay(ANIM_SOUL_EXIT);
    private record AnimationPlayback(String animation, int sequence) {}
    private record AnimationStack(WeakReference<ItemStack> stack, WeakReference<Player> holder) {
        boolean isCarried() {
            ItemStack value = stack.get();
            Player player = holder.get();
            if (value == null || value.isEmpty() || player == null || player.isRemoved()) return false;
            for (int slot = 0; slot < player.getInventory().getContainerSize(); slot++)
                if (player.getInventory().getItem(slot) == value) return true;
            return false;
        }
    }
    private static final Map<MinecraftServer, Map<Long, AnimationStack>> SERVER_ANIMATION_STACKS = new WeakHashMap<>();
    private static final ThreadLocal<ItemStack> RENDER_STACK = new ThreadLocal<>();
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public IgnitiumGatlingItem(Properties properties, int bonusDamage, double damageMultiplier, int fireDelay,
                               double inaccuracy, int enchantability, Supplier<GWREConfig.GunConfig> config) {
        super(properties, bonusDamage, damageMultiplier, fireDelay, inaccuracy, enchantability, config);
        GeoItem.registerSyncedAnimatable(this);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private IgnitiumGatlingGeoRenderer renderer;
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) renderer = new IgnitiumGatlingGeoRenderer();
                return renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        // registerControllers runs for each GeckoLib instance. Playback history belongs
        // to that controller, not a global map shared by copies or render perspectives.
        AnimationPlayback[] lastPlayback = {null};
        controllers.add(new AnimationController<>(this, GECKO_CONTROLLER, 0, state -> {
            ItemStack stack = state.getData(software.bernie.geckolib.constant.DataTickets.ITEMSTACK);
            AnimationPlayback playback = playback(stack);
            if (lastPlayback[0] != null && !lastPlayback[0].equals(playback)) state.getController().forceAnimationReset();
            lastPlayback[0] = playback;
            state.setControllerSpeed(1.0F);
            // Ordinary fire is a state-controlled loop. A triggered loop suppresses
            // this predicate forever and cannot return to idle on mouse release.
            state.setAnimation(getGeckoAnimation(stack));
            return PlayState.CONTINUE;
        }));
    }

    @Override public AnimatableInstanceCache getAnimatableInstanceCache() { return cache; }

    public static boolean isBlueFireActive(ItemStack stack) { return getBlueFireTicks(stack) > 0; }
    public static int getBlueFireTicks(ItemStack stack) { return stack.hasTag() ? stack.getOrCreateTag().getInt(BLUE_FIRE_TICKS_TAG) : 0; }
    public static int getMaxHeat(ItemStack stack) {
        return stack.hasTag() && stack.getTag().contains("IgnitiumMaxHeat")
                ? Math.max(1, stack.getTag().getInt("IgnitiumMaxHeat")) : getConfig().maxHeat.get();
    }
    public static boolean isGunSkillEnabled(ItemStack stack) {
        return stack.hasTag() && stack.getTag().contains("IgnitiumSkillEnabled")
                ? stack.getTag().getBoolean("IgnitiumSkillEnabled") : getConfig().skillEnabled.get();
    }
    public static int getHeat(ItemStack stack) { return stack.hasTag() ? Mth.clamp(stack.getOrCreateTag().getInt(HEAT_TAG), 0, getMaxHeat(stack)) : 0; }
    public static float getHeatFraction(ItemStack stack) { return getHeat(stack) / (float) getMaxHeat(stack); }
    public static boolean isFireballMode(ItemStack stack) { return stack.hasTag() && stack.getOrCreateTag().getBoolean(FIREBALL_MODE_TAG); }
    private static void setHeat(ItemStack stack, int heat) {
        int value = Mth.clamp(heat, 0, getConfig().maxHeat.get());
        if (value == 0) stack.getOrCreateTag().remove(HEAT_TAG); else stack.getOrCreateTag().putInt(HEAT_TAG, value);
    }
    private static void addHeat(ItemStack stack, int amount) { setHeat(stack, getHeat(stack) + amount); }

    @Override
    public boolean canUseGunSkill(ServerPlayer player, InteractionHand hand, ItemStack stack) {
        return getConfig().skillEnabled.get() && player.isAlive() && !player.isSpectator()
                && player.getItemInHand(hand) == stack && stack.getItem() == this;
    }

    @Override
    public void useGunSkill(ServerPlayer player, InteractionHand hand, ItemStack stack) {
        if (!canUseGunSkill(player, hand, stack)) return;
        ensureGeckoId(stack, player.level(), player);
        long now = player.level().getGameTime();
        if (isFireballMode(stack)) {
            if (isActionLocked(stack, now) || getAbyssCooldown(stack, now) > 0) return;
            int cost = Math.min(getConfig().abyssCost.get(), getConfig().maxHeat.get());
            if (getHeat(stack) < cost) { exitFireballMode(player.level(), player, stack); return; }
            setHeat(stack, getHeat(stack) - cost);
            stack.getOrCreateTag().putLong(ABYSS_COOLDOWN_TAG, now + getConfig().abyssCooldownTicks.get());
            stack.getOrCreateTag().putLong(FIREBALL_READY_TAG, now + 9);
            setGeckoAnimation(stack, isBlueFireActive(stack) ? ANIM_ABYSS_SOUL : ANIM_ABYSS, now, 9);
            spawnAbyssFireball(player.serverLevel(), player, stack);
            syncState(player, stack);
            return;
        }
        if (getHeat(stack) < getConfig().maxHeat.get()) {
            player.displayClientMessage(Component.translatable("message.gwrexpansions.ignitium_heat", getHeat(stack), getConfig().maxHeat.get()), true);
            return;
        }
        if (player.isUsingItem()) player.stopUsingItem();
        setHeat(stack, getHeat(stack) - Math.min(getConfig().entryCost.get(), getConfig().maxHeat.get()));
        stack.getOrCreateTag().putBoolean(FIREBALL_MODE_TAG, true);
        stack.getOrCreateTag().putLong(MODE_DRAIN_TIMER_TAG, now + 20);
        stack.getOrCreateTag().putLong(FIREBALL_READY_TAG, now + 6);
        stack.getOrCreateTag().putLong(NBT_MODE_ENTER_START, now);
        setGeckoAnimation(stack, isBlueFireActive(stack) ? ANIM_ENTER_SOUL : ANIM_ENTER, now, 6);
        syncState(player, stack);
        // Ignis MAGIC_ATTACK uses this cue for its summoned fireball group.
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.EVOKER_PREPARE_SUMMON, SoundSource.PLAYERS, 0.8F, 1.4F);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (isFireballMode(stack)) { player.startUsingItem(hand); return InteractionResultHolder.consume(stack); }
        return super.use(level, player, hand);
    }

    @Override
    public void onUseTick(Level level, LivingEntity livingEntity, ItemStack stack, int remainingUseDuration) {
        if (isFireballMode(stack)) {
            long now = level.getGameTime();
            if (level instanceof ServerLevel server && livingEntity instanceof Player player
                    && now >= stack.getOrCreateTag().getLong(FIREBALL_READY_TAG) && !isActionLocked(stack, now)) {
                int cost = Math.min(getConfig().lightCost.get(), getConfig().maxHeat.get());
                if (!getConfig().skillEnabled.get() || getHeat(stack) < cost) {
                    exitFireballMode(level, player, stack);
                    return;
                }
                setHeat(stack, getHeat(stack) - cost);
                stack.getOrCreateTag().putLong(FIREBALL_READY_TAG, now + getConfig().lightIntervalTicks.get());
                spawnFireball(server, player, stack);
                syncState(player, stack);
            }
            return;
        }
        super.onUseTick(level, livingEntity, stack, remainingUseDuration);
    }

    @Override
    protected void shoot(Level level, Player player, ItemStack gun, ItemStack ammo, IBullet bulletItem, boolean bulletFree) {
        if (isFireballMode(gun)) return;
        super.shoot(level, player, gun, ammo, bulletItem, bulletFree);
        if (!level.isClientSide) {
            if (getConfig().skillEnabled.get()) addHeat(gun, getConfig().heatPerBullet.get());
            gun.getOrCreateTag().putLong(LAST_HEAT_SHOT_TAG, level.getGameTime());
            gun.getOrCreateTag().putLong(HEAT_COOL_TIMER_TAG, level.getGameTime() + getConfig().coolDelayTicks.get());
            ensureGeckoId(gun, level, player);
            setGeckoAnimation(gun, isBlueFireActive(gun) ? ANIM_FIRE_SOUL : ANIM_FIRE,
                    level.getGameTime(), getFireDelay(gun, player) + 2);
            syncState(player, gun);
        }
    }

    private static void spawnFireball(ServerLevel level, Player player, ItemStack stack) {
        var fireball = new IgnitiumFireballEntity(level, player, false, isBlueFireActive(stack));
        fireball.launch(player, getConfig().lightSpeed.get(), getConfig().projectileGravity.get());
        level.addFreshEntity(fireball);
        setGeckoAnimation(stack, isBlueFireActive(stack) ? ANIM_FIREBALL_SOUL : ANIM_FIREBALL, level.getGameTime(), 5);
    }

    private static void spawnAbyssFireball(ServerLevel level, Player player, ItemStack stack) {
        var fireball = new IgnitiumFireballEntity(level, player, true, isBlueFireActive(stack));
        fireball.launch(player, getConfig().abyssSpeed.get(), getConfig().projectileGravity.get());
        level.addFreshEntity(fireball);
    }

    public static ItemStack findHeldStack(Player player) {
        if (player.getMainHandItem().getItem() instanceof IgnitiumGatlingItem) return player.getMainHandItem();
        return player.getOffhandItem().getItem() instanceof IgnitiumGatlingItem ? player.getOffhandItem() : ItemStack.EMPTY;
    }
    public static int getAbyssCooldown(ItemStack stack, long now) {
        return stack.hasTag() ? (int) Math.max(0, stack.getOrCreateTag().getLong(ABYSS_COOLDOWN_TAG) - now) : 0;
    }
    public static boolean isAbyssReady(ItemStack stack, long now) {
        int cost = stack.hasTag() && stack.getTag().contains("IgnitiumAbyssCost")
                ? stack.getTag().getInt("IgnitiumAbyssCost") : Math.min(getConfig().abyssCost.get(), getMaxHeat(stack));
        return isFireballMode(stack) && getHeat(stack) >= cost
                && getAbyssCooldown(stack, now) == 0 && !isActionLocked(stack, now);
    }
    private static void exitFireballMode(Level level, Player player, ItemStack stack) {
        if (player.getUseItem() == stack) player.stopUsingItem();
        stack.getOrCreateTag().putBoolean(FIREBALL_MODE_TAG, false);
        setGeckoAnimation(stack, isBlueFireActive(stack) ? ANIM_EXIT_SOUL : ANIM_EXIT, level.getGameTime(), 4);
        syncState(player, stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        super.inventoryTick(stack, level, entity, slot, selected);
        if (level.isClientSide) {
            // Visual expiry is also checked locally, so even a delayed equipment packet cannot latch fire.
            tickGeckoAnimation(stack, level.getGameTime());
            return;
        }
        if (!(entity instanceof Player player)) return;
        stack.getOrCreateTag().putInt("IgnitiumMaxHeat", getConfig().maxHeat.get());
        stack.getOrCreateTag().putBoolean("IgnitiumSkillEnabled", getConfig().skillEnabled.get());
        stack.getOrCreateTag().putInt("IgnitiumAbyssCost", Math.min(getConfig().abyssCost.get(), getConfig().maxHeat.get()));
        ensureGeckoId(stack, level, player);
        tickGeckoAnimation(stack, level.getGameTime());
        boolean held = selected || player.getOffhandItem() == stack;
        if (!held && isFireballMode(stack)) exitFireballMode(level, player, stack);
        // Heat/cooldowns must also advance in inventory; switching slots cannot freeze them.
        tickHeatAndMode(stack, level, player);
        if (held) {
            tickBlueFire(stack, level, player);
            String animation = actionAnimation(stack);
            if ((ANIM_FIRE.equals(animation) || ANIM_FIRE_SOUL.equals(animation))
                    && (!player.isUsingItem() || player.getUseItem() != stack)) clearAnimation(stack);
            if (player.tickCount % 4 == 0) syncState(player, stack);
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        super.releaseUsing(stack, level, entity, timeLeft);
        String animation = actionAnimation(stack);
        if (ANIM_FIRE.equals(animation) || ANIM_FIRE_SOUL.equals(animation)) {
            clearAnimation(stack);
            if (!level.isClientSide && entity instanceof Player player) syncState(player, stack);
        }
    }

    private static void tickHeatAndMode(ItemStack stack, Level level, Player player) {
        if (level.isClientSide) return;
        CompoundTag tag = stack.getOrCreateTag();
        long now = level.getGameTime();
        if (isFireballMode(stack) && !getConfig().skillEnabled.get()) exitFireballMode(level, player, stack);
        if (isFireballMode(stack)) {
            if (now >= tag.getLong(MODE_DRAIN_TIMER_TAG)) {
                setHeat(stack, getHeat(stack) - getConfig().modeDrainPerSecond.get());
                tag.putLong(MODE_DRAIN_TIMER_TAG, now + 20);
            }
            // Let the last heavy shot finish before shutdown so its recoil isn't cut off.
            if (getHeat(stack) <= 0 && !isActionLocked(stack, now)) exitFireballMode(level, player, stack);
            return;
        }
        if (getHeat(stack) > 0 && now - tag.getLong(LAST_HEAT_SHOT_TAG) >= getConfig().coolDelayTicks.get()
                && now >= tag.getLong(HEAT_COOL_TIMER_TAG)) {
            setHeat(stack, getHeat(stack) - getConfig().coolPerSecond.get());
            tag.putLong(HEAT_COOL_TIMER_TAG, now + 20);
        }
    }

    private static void tickBlueFire(ItemStack stack, Level level, Player player) {
        boolean wasSoul = isBlueFireActive(stack);
        boolean belowHalf = player.getHealth() <= player.getMaxHealth() * 0.5F;
        CompoundTag tag = stack.getOrCreateTag();
        boolean wasBelowHalf = tag.getBoolean(WAS_BELOW_HALF_TAG);
        int ticks = getBlueFireTicks(stack);
        if (belowHalf && !wasBelowHalf) {
            ticks = getBlueFireDurationTicks(stack); tag.putInt(BLUE_FIRE_TICKS_TAG, ticks);
            if (!level.isClientSide && level instanceof ServerLevel server) spawnBlueFireBurst(server, player);
        } else if (!belowHalf && ticks > 0) {
            ticks--; if (ticks > 0) tag.putInt(BLUE_FIRE_TICKS_TAG, ticks); else tag.remove(BLUE_FIRE_TICKS_TAG);
        }
        tag.putBoolean(WAS_BELOW_HALF_TAG, belowHalf);
        if (belowHalf && !wasBelowHalf) {
            startSoulTransition(stack, ANIM_SOUL_ENTER, level.getGameTime(), wasSoul);
            syncState(player, stack);
        } else if (wasSoul && !isBlueFireActive(stack)) {
            startSoulTransition(stack, ANIM_SOUL_EXIT, level.getGameTime(), wasSoul);
            syncState(player, stack);
        }
    }

    private static void ensureGeckoId(ItemStack stack, Level level, Player holder) {
        if (stack.isEmpty() || !(stack.getItem() instanceof IgnitiumGatlingItem) || !(level instanceof ServerLevel server)) return;
        var ids = SERVER_ANIMATION_STACKS.computeIfAbsent(server.getServer(), ignored -> new HashMap<>());
        long id = GeoItem.getOrAssignId(stack, server);
        var reference = ids.get(id);
        ItemStack previous = reference == null ? null : reference.stack().get();
        if (previous != null && previous != stack && reference.isCarried() && GeoItem.getId(previous) == id) {
            // Creative copies and NBT clones carry GeckoLibID too. Give the new physical
            // stack its own instance, and do not inherit the original's running animation.
            stack.getOrCreateTag().remove(GeoItem.ID_NBT_KEY);
            id = GeoItem.getOrAssignId(stack, server);
            for (String key : List.of(NBT_GECKO_ANIMATION, NBT_GECKO_ANIMATION_TIMER, NBT_GECKO_ANIMATION_SEQUENCE,
                    NBT_SOUL_TRANSITION, NBT_SOUL_TRANSITION_END, NBT_SOUL_TRANSITION_SEQUENCE,
                    NBT_SOUL_TRANSITION_FROM_SOUL, NBT_MODE_ENTER_START, FIREBALL_MODE_TAG, FIREBALL_READY_TAG))
                stack.getOrCreateTag().remove(key);
        }
        ids.put(id, new AnimationStack(new WeakReference<>(stack), new WeakReference<>(holder)));
        if (ids.size() > 1024) ids.values().removeIf(ref -> !ref.isCarried());
    }
    private static void syncState(Player player, ItemStack stack) {
        if (!(player instanceof ServerPlayer serverPlayer) || serverPlayer.connection == null) return;
        InteractionHand hand;
        if (player.getMainHandItem() == stack) hand = InteractionHand.MAIN_HAND;
        else if (player.getOffhandItem() == stack) hand = InteractionHand.OFF_HAND;
        else return;
        GWRENetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                new IgnitiumStatePacket(player.getId(), hand, GeoItem.getId(stack), snapshot(stack)));
    }

    public static CompoundTag snapshot(ItemStack stack) {
        CompoundTag copy = new CompoundTag();
        if (stack.hasTag()) for (String key : stack.getTag().getAllKeys()) {
            if (key.startsWith("Ignitium")) copy.put(key, stack.getTag().get(key).copy());
        }
        copy.putInt("IgnitiumMaxHeat", getConfig().maxHeat.get());
        copy.putBoolean("IgnitiumSkillEnabled", getConfig().skillEnabled.get());
        copy.putInt("IgnitiumAbyssCost", Math.min(getConfig().abyssCost.get(), getConfig().maxHeat.get()));
        if (hasAnimationId(stack)) copy.putLong(GeoItem.ID_NBT_KEY, GeoItem.getId(stack));
        return copy;
    }

    public static boolean hasAnimationId(ItemStack stack) {
        return stack.hasTag() && stack.getTag().contains(GeoItem.ID_NBT_KEY, net.minecraft.nbt.Tag.TAG_ANY_NUMERIC);
    }

    public static boolean applySnapshotIfMatches(ItemStack stack, long id, CompoundTag snapshot) {
        if (!(stack.getItem() instanceof IgnitiumGatlingItem) || !hasAnimationId(stack) || GeoItem.getId(stack) != id) return false;
        if (!snapshot.contains(GeoItem.ID_NBT_KEY, net.minecraft.nbt.Tag.TAG_ANY_NUMERIC)
                || snapshot.getLong(GeoItem.ID_NBT_KEY) != id) return false;
        applySnapshot(stack, snapshot);
        return true;
    }

    public static void applySnapshot(ItemStack stack, CompoundTag snapshot) {
        CompoundTag tag = stack.getOrCreateTag();
        for (String key : List.copyOf(tag.getAllKeys())) {
            if (key.startsWith("Ignitium")) tag.remove(key);
        }
        tag.merge(snapshot);
    }
    public static String currentAnimation(ItemStack stack) {
        if (!stack.hasTag()) return "";
        String transition = stack.getTag().getString(NBT_SOUL_TRANSITION);
        return transition.isEmpty() ? actionAnimation(stack) : transition;
    }
    public static boolean isSoulTransition(ItemStack stack) {
        String animation = currentAnimation(stack);
        return ANIM_SOUL_ENTER.equals(animation) || ANIM_SOUL_EXIT.equals(animation);
    }
    public static boolean isSoulColorSweep(ItemStack stack) {
        return isSoulTransition(stack)
                && stack.getTag().getBoolean(NBT_SOUL_TRANSITION_FROM_SOUL) != isBlueFireActive(stack);
    }
    public static float modeVaneAngle(ItemStack stack, long gameTime, float partialTick) {
        if (!isFireballMode(stack)) return 0;
        if (!stack.getTag().contains(NBT_MODE_ENTER_START)) return 12;
        float seconds = Math.max(0, gameTime - stack.getTag().getLong(NBT_MODE_ENTER_START) + partialTick) / 20F;
        // The exported entry clip opens 0 -> 7 degrees at .05s -> 12 at .12s.
        return seconds < 0.05F ? seconds / 0.05F * 7
                : seconds < 0.12F ? 7 + (seconds - 0.05F) / 0.07F * 5 : 12;
    }
    private static String actionAnimation(ItemStack stack) {
        return stack.hasTag() ? stack.getTag().getString(NBT_GECKO_ANIMATION) : "";
    }
    public static long animationEndsAt(ItemStack stack) {
        return stack.hasTag() ? stack.getTag().getLong(NBT_GECKO_ANIMATION_TIMER) : 0;
    }
    private static boolean isActionLocked(ItemStack stack, long now) {
        String animation = actionAnimation(stack);
        return animationEndsAt(stack) > now && (animation.equals(ANIM_ENTER) || animation.equals(ANIM_ENTER_SOUL)
                || animation.equals(ANIM_ABYSS) || animation.equals(ANIM_ABYSS_SOUL));
    }
    private static void setGeckoAnimation(ItemStack stack, String animation, long now, int ticks) {
        CompoundTag tag = stack.getOrCreateTag();
        boolean continuous = animation.equals(ANIM_FIRE) || animation.equals(ANIM_FIRE_SOUL);
        if (!continuous || !animation.equals(actionAnimation(stack)) || animationEndsAt(stack) <= now) {
            tag.putInt(NBT_GECKO_ANIMATION_SEQUENCE, tag.getInt(NBT_GECKO_ANIMATION_SEQUENCE) + 1);
        }
        tag.putString(NBT_GECKO_ANIMATION, animation);
        tag.putLong(NBT_GECKO_ANIMATION_TIMER, now + ticks);
    }
    private static void clearAnimation(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.remove(NBT_GECKO_ANIMATION);
        tag.remove(NBT_GECKO_ANIMATION_TIMER);
        tag.putInt(NBT_GECKO_ANIMATION_SEQUENCE, tag.getInt(NBT_GECKO_ANIMATION_SEQUENCE) + 1);
    }
    private static void tickGeckoAnimation(ItemStack stack, long now) {
        if (!stack.hasTag()) return;
        if (!actionAnimation(stack).isEmpty() && now >= animationEndsAt(stack)) clearAnimation(stack);
        if (now >= stack.getTag().getLong(NBT_SOUL_TRANSITION_END)) {
            stack.getTag().remove(NBT_SOUL_TRANSITION);
            stack.getTag().remove(NBT_SOUL_TRANSITION_END);
            stack.getTag().remove(NBT_SOUL_TRANSITION_FROM_SOUL);
        }
    }
    private static void startSoulTransition(ItemStack stack, String animation, long now, boolean fromSoul) {
        CompoundTag tag = stack.getOrCreateTag();
        // Shooting keeps updating its own action state, but cannot restart or truncate
        // the transformation. Rapid heal/re-cross also preserves an ongoing enter clip.
        if (animation.equals(tag.getString(NBT_SOUL_TRANSITION)) && tag.getLong(NBT_SOUL_TRANSITION_END) > now) return;
        tag.putString(NBT_SOUL_TRANSITION, animation);
        tag.putLong(NBT_SOUL_TRANSITION_END, now + SOUL_TRANSITION_TICKS);
        tag.putBoolean(NBT_SOUL_TRANSITION_FROM_SOUL, fromSoul);
        tag.putInt(NBT_SOUL_TRANSITION_SEQUENCE, tag.getInt(NBT_SOUL_TRANSITION_SEQUENCE) + 1);
    }
    private static RawAnimation getGeckoAnimation(@Nullable ItemStack stack) {
        if (stack == null || !stack.hasTag()) return IDLE_ANIM;
        String animation = currentAnimation(stack);
        if (!animation.isEmpty()) {
            return switch (animation) {
                case ANIM_FIRE -> FIRE_ANIM; case ANIM_FIRE_SOUL -> FIRE_SOUL_ANIM; case ANIM_ENTER -> ENTER_ANIM; case ANIM_ENTER_SOUL -> ENTER_SOUL_ANIM;
                case ANIM_FIREBALL -> FIREBALL_ANIM; case ANIM_FIREBALL_SOUL -> FIREBALL_SOUL_ANIM; case ANIM_ABYSS -> ABYSS_ANIM; case ANIM_ABYSS_SOUL -> ABYSS_SOUL_ANIM;
                case ANIM_EXIT -> EXIT_ANIM; case ANIM_EXIT_SOUL -> EXIT_SOUL_ANIM;
                case ANIM_SOUL_ENTER -> SOUL_ENTER_ANIM; case ANIM_SOUL_EXIT -> SOUL_EXIT_ANIM; default -> IDLE_ANIM;
            };
        }
        return isBlueFireActive(stack) ? IDLE_SOUL_ANIM : IDLE_ANIM;
    }
    private static AnimationPlayback playback(@Nullable ItemStack stack) {
        if (stack == null || stack.isEmpty() || !stack.hasTag()) return new AnimationPlayback("", 0);
        CompoundTag tag = stack.getTag();
        int sequence = tag.getInt(tag.getString(NBT_SOUL_TRANSITION).isEmpty()
                ? NBT_GECKO_ANIMATION_SEQUENCE : NBT_SOUL_TRANSITION_SEQUENCE);
        return new AnimationPlayback(currentAnimation(stack), sequence);
    }

    public ResourceLocation getGeoTexture(Part part) {
        ItemStack stack = RENDER_STACK.get();
        return getGeoTexture(part, stack != null && isBlueFireActive(stack));
    }
    public ResourceLocation getGeoTexture(Part part, boolean soul) {
        String path = switch (part) {
            case LAVA -> soul ? "textures/item/ignitium_gatling_soul_lava_geo.png" : "textures/item/ignitium_gatling_lava_geo.png";
            default -> soul ? "textures/item/ignitium_gatling_soul_geo.png" : "textures/item/ignitium_gatling_geo.png";
        };
        return GWRexpansions.resource(path);
    }
    public static boolean isAbyssAnimation(ItemStack stack) {
        String animation = currentAnimation(stack);
        return animation.equals(ANIM_ABYSS) || animation.equals(ANIM_ABYSS_SOUL);
    }
    public static void setRenderStack(ItemStack stack) { RENDER_STACK.set(stack); }
    public static void clearRenderStack() { RENDER_STACK.remove(); }

    @Override
    protected ItemStack overrideFiredStack(LivingEntity shooter, ItemStack gun, ItemStack ammo, IBullet bulletItem, boolean bulletFree) {
        return ammo.is(CompatCataclysm.tagBaseBullets) ? new ItemStack(CompatCataclysm.ignitium_bullet.get()) : ammo;
    }
    @Override
    public double getBonusDamage(ItemStack stack, @Nullable LivingEntity shooter) {
        if (!isBlueFireActive(stack)) return super.getBonusDamage(stack, shooter);
        int impact = stack.getEnchantmentLevel(GWREnchantments.impact.get()); double bonus = impact >= 1 ? GWREnchantments.impactBonus(impact) : 0.0D;
        if (shooter != null && shooter.getAttribute(GWRAttributes.dmgBase.get()) != null) bonus += shooter.getAttributeValue(GWRAttributes.dmgBase.get());
        return getConfig().blueFireBonusDamage.get() + bonus;
    }
    @Override
    public double getFireDelayFractional(ItemStack stack, @Nullable LivingEntity shooter) {
        if (!isBlueFireActive(stack)) return super.getFireDelayFractional(stack, shooter);
        double delay = getConfig().blueFireDelay.get().doubleValue(); int sleight = stack.getEnchantmentLevel(GWREnchantments.sleightOfHand.get());
        if (sleight > 0) delay = GWREnchantments.sleightModifyFractional(sleight, delay);
        if (shooter != null && shooter.getAttribute(GWRAttributes.fireDelay.get()) != null) delay *= shooter.getAttributeValue(GWRAttributes.fireDelay.get());
        return Math.max(1.0D, delay);
    }
    @Override
    protected void affectBulletEntity(LivingEntity shooter, ItemStack gun, BulletEntity bullet, boolean bulletFree) {
        super.affectBulletEntity(shooter, gun, bullet, bulletFree); if (bullet instanceof IgnitiumBulletEntity ignitiumBullet) ignitiumBullet.setBlueFire(isBlueFireActive(gun));
    }
    @Override
    protected void addExtraStatsTooltip(ItemStack stack, @Nullable Level world, List<Component> tooltip) {
        tooltip.add(Component.translatable("tooltip.gwrexpansions.ignitium_gatling.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.gwrexpansions.ignitium_gatling.desc2").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.gwrexpansions.ignitium_gatling.blue_fire", getBlueFireDurationTicks(stack) / 20.0D).withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("tooltip.gwrexpansions.ignitium_gatling.gun_skill", GunSkillTooltip.keyName()).withStyle(ChatFormatting.GOLD));
        tooltip.add(Component.translatable("tooltip.gwrexpansions.ignitium_gatling.controls", GunSkillTooltip.keyName()).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.gwrexpansions.ignitium_gatling.heat", getHeat(stack), getMaxHeat(stack)).withStyle(ChatFormatting.RED));
    }
    private static GWREConfig.IgnitiumGatlingConfig getConfig() { return GWREConfig.GATLING.Ignitium; }
    private static int getBlueFireDurationTicks(ItemStack stack) {
        int ticks = getConfig().blueFireDurationTicks.get();
        return GWRECataclysmEnchantments.has(stack, GWRECataclysmEnchantments.BLUE_FLAME_BRINK) ? Math.max(1, (int) Math.ceil(ticks * BLUE_FLAME_BRINK_DURATION_MULTIPLIER)) : ticks;
    }
    private static void spawnBlueFireBurst(ServerLevel level, Player player) {
        double y = player.getY() + player.getBbHeight() * 0.5D;
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME, player.getX(), y, player.getZ(), 80, player.getBbWidth() * 0.8D, player.getBbHeight() * 0.35D, player.getBbWidth() * 0.8D, 0.12D);
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.SOUL, player.getX(), y, player.getZ(), 24, player.getBbWidth() * 0.55D, player.getBbHeight() * 0.25D, player.getBbWidth() * 0.55D, 0.05D);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.FLAME_BURST.get(), SoundSource.PLAYERS, 0.9F, 1.45F);
    }
}
