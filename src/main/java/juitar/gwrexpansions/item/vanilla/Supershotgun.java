package juitar.gwrexpansions.item.vanilla;

import juitar.gwrexpansions.config.GWREConfig;
import juitar.gwrexpansions.client.render.SupershotgunGeoRenderer;
import juitar.gwrexpansions.entity.vanilla.MeatHookEntity;
import juitar.gwrexpansions.item.ConfigurableGunItem;
import juitar.gwrexpansions.item.GunSkillItem;
import juitar.gwrexpansions.item.GunSkillTooltip;
import juitar.gwrexpansions.network.GWRENetwork;
import juitar.gwrexpansions.network.SuperShotgunFeedbackPacket;
import lykrast.gunswithoutroses.entity.BulletEntity;
import lykrast.gunswithoutroses.item.IBullet;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Supershotgun extends ConfigurableGunItem implements GunSkillItem, GeoItem {
    public static final String SUPER_SHOTGUN_SHOT_TAG = "GWRESuperShotgunShot";
    private static final double HOOK_RANGE = 32.0D; // 肉钩最大射程
    private static final double PULL_SPEED = 1.5D; // 拉近速度
    private static final int HOOK_COOLDOWN = 100; // 肉钩冷却时间（tick）
    private static final int MAX_PAUSE_TIME = 160; // 最大暂停时间（8秒 = 160 tick）
    private static final String NBT_HOOK_COOLDOWN = "Hook_Cooldown"; // 冷却时间NBT
    private static final String NBT_COOLDOWN_PAUSED = "Cooldown_Paused"; // 冷却暂停标志NBT
    private static final String NBT_PAUSE_COUNTER = "Pause_Counter"; // 暂停计数器NBT
    private static final String NBT_HOOKING = "SuperShotgunHooking";
    private static final String NBT_GECKO_ANIMATION = "SuperShotgunGeckoAnimation";
    private static final String NBT_GECKO_ANIMATION_TIMER = "SuperShotgunGeckoAnimationTimer";
    private static final String NBT_GECKO_ANIMATION_SEQUENCE = "SuperShotgunGeckoAnimationSequence";
    private static final String NBT_RELOAD_TIMER = "SuperShotgunReloadTimer";
    private static final String NBT_RELOAD_ANIMATION_TICKS = "SuperShotgunReloadAnimationTicks";
    private static final String GECKO_CONTROLLER = "controller";
    private static final String GECKO_ANIM_FIRE = "fire";
    private static final String GECKO_ANIM_HOOK = "hook";
    private static final int GECKO_FIRE_TICKS = 6;
    private static final int GECKO_HOOK_TICKS = 4;
    private static final float RELOAD_ANIMATION_TICKS = 22.5F;
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation FIRE_ANIM = RawAnimation.begin().thenPlay("fire");
    private static final RawAnimation RELOAD_ANIM = RawAnimation.begin().thenPlay("reload");
    private static final RawAnimation HOOK_ANIM = RawAnimation.begin().thenPlay("hook");
    private static final RawAnimation HOOKING_ANIM = RawAnimation.begin().thenLoop("hooking");
    private static final Map<Long, Integer> LAST_SEEN_GECKO_SEQUENCE = new ConcurrentHashMap<>();
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    /**
     * 创建可配置的枪支
     *
     * @param properties       物品属性
     * @param bonusDamage      额外伤害（会被配置覆盖）
     * @param damageMultiplier 伤害倍率（会被配置覆盖）
     * @param fireDelay        射击延迟（会被配置覆盖）
     * @param inaccuracy       不精确度（会被配置覆盖）
     * @param enchantability   附魔能力
     * @param configSupplier   配置供应器
     */
    public Supershotgun(Properties properties, int bonusDamage, double damageMultiplier, int fireDelay, double inaccuracy, int enchantability, Supplier<GWREConfig.GunConfig> configSupplier) {
        super(properties, bonusDamage, damageMultiplier, fireDelay, inaccuracy, enchantability, configSupplier);
        GeoItem.registerSyncedAnimatable(this);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        return super.use(world, player, hand);
    }

    @Override
    protected void shoot(Level level, Player player, ItemStack gun, ItemStack ammo, IBullet bulletItem, boolean bulletFree) {
        super.shoot(level, player, gun, ammo, bulletItem, bulletFree);
        triggerShotFeedback(level, player);
        ensureGeckoId(gun, level);
        setGeckoAnimation(gun, GECKO_ANIM_FIRE, GECKO_FIRE_TICKS);
        setReloadAnimation(gun, getFireDelay(gun, player));
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private SupershotgunGeoRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new SupershotgunGeoRenderer();
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
            state.setControllerSpeed(getGeckoAnimationSpeed(stack));
            state.setAnimation(getGeckoAnimation(stack));
            return PlayState.CONTINUE;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    protected void affectBulletEntity(LivingEntity shooter, ItemStack gun, BulletEntity shot, boolean bulletFree) {
        super.affectBulletEntity(shooter, gun, shot, bulletFree);
        shot.getPersistentData().putBoolean(SUPER_SHOTGUN_SHOT_TAG, true);
    }

    @Override
    public boolean canUseGunSkill(ServerPlayer player, InteractionHand hand, ItemStack stack) {
        return stack.getItem() instanceof Supershotgun && getHookCooldown(stack) <= 0;
    }

    @Override
    public void useGunSkill(ServerPlayer player, InteractionHand hand, ItemStack stack) {
        if (getHookCooldown(stack) > 0) {
            return;
        }

        MeatHookEntity hook = new MeatHookEntity(player.level(), player);
        Vec3 look = player.getLookAngle();
        hook.shoot(look.x, look.y, look.z, 3.5f, 0.0f);
        player.level().addFreshEntity(hook);

        ensureGeckoId(stack, player.level());
        setGeckoAnimation(stack, GECKO_ANIM_HOOK, GECKO_HOOK_TICKS);
        setHooking(stack, false);
        setHookCooldown(stack, HOOK_COOLDOWN);
        setCooldownPaused(stack, true);
        resetPauseCounter(stack);
    }

    @Override
    public boolean canBeDepleted() {
        return false;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean isSelected) {
        super.inventoryTick(stack, world, entity, slot, isSelected);
        tickGeckoAnimation(stack);
        if (!world.isClientSide) {
            ensureGeckoId(stack, world);
        }
        
        // 获取冷却状态
        int cooldown = getHookCooldown(stack);
        boolean isPaused = isCooldownPaused(stack);
        
        if (cooldown > 0) {
            if (isPaused) {
                // 如果冷却暂停，增加暂停计数器
                int pauseCounter = getPauseCounter(stack);
                pauseCounter++;
                
                // 如果暂停时间超过最大值，自动恢复计算冷却
                if (pauseCounter >= MAX_PAUSE_TIME) {
                    setCooldownPaused(stack, false);
                    setHooking(stack, false);
                } else {
                    setPauseCounter(stack, pauseCounter);
                }
            } else {
                // 如果冷却未暂停，正常减少冷却时间
                setHookCooldown(stack, cooldown - 1);
            }
        }
    }

    @Override
    protected void addExtraStatsTooltip(ItemStack stack, @Nullable Level world, List<Component> tooltip) {
        super.addExtraStatsTooltip(stack, world, tooltip);
        tooltip.add(Component.translatable("tooltip.gwrexpansions.supershotgun.hook",
                GunSkillTooltip.keyName()).withStyle(ChatFormatting.GRAY));
        
        int cooldown = getHookCooldown(stack);
        boolean isPaused = isCooldownPaused(stack);
        
        if (cooldown > 0) {
            tooltip.add(Component.translatable("tooltip.gwrexpansions.supershotgun.notready").withStyle(ChatFormatting.RED));
        } else {
            tooltip.add(Component.translatable("tooltip.gwrexpansions.supershotgun.ready").withStyle(ChatFormatting.GREEN));
        }
    }
    
    // 获取肉钩冷却时间
    private int getHookCooldown(ItemStack stack) {
        return stack.getOrCreateTag().getInt(NBT_HOOK_COOLDOWN);
    }
    
    // 设置肉钩冷却时间
    private void setHookCooldown(ItemStack stack, int cooldown) {
        stack.getOrCreateTag().putInt(NBT_HOOK_COOLDOWN, cooldown);
    }
    
    // 检查冷却是否暂停
    private boolean isCooldownPaused(ItemStack stack) {
        return stack.getOrCreateTag().getBoolean(NBT_COOLDOWN_PAUSED);
    }
    
    // 设置冷却暂停状态
    private void setCooldownPaused(ItemStack stack, boolean paused) {
        stack.getOrCreateTag().putBoolean(NBT_COOLDOWN_PAUSED, paused);
    }
    
    // 获取暂停计数器
    private int getPauseCounter(ItemStack stack) {
        return stack.getOrCreateTag().getInt(NBT_PAUSE_COUNTER);
    }
    
    // 设置暂停计数器
    private void setPauseCounter(ItemStack stack, int counter) {
        stack.getOrCreateTag().putInt(NBT_PAUSE_COUNTER, counter);
    }
    
    // 重置暂停计数器
    private void resetPauseCounter(ItemStack stack) {
        stack.getOrCreateTag().putInt(NBT_PAUSE_COUNTER, 0);
    }
    
    // 检查肉钩是否已准备好使用（冷却完成）
    public boolean isHookReady(ItemStack stack) {
        return getHookCooldown(stack) <= 0;
    }

    public static boolean isFlashAnimationActive(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTag()) {
            return false;
        }

        CompoundTag tag = stack.getOrCreateTag();
        return tag.getInt(NBT_GECKO_ANIMATION_TIMER) > 0
                && GECKO_ANIM_FIRE.equals(tag.getString(NBT_GECKO_ANIMATION));
    }
    
    /**
     * 恢复冷却计算（肉钩返回结果后调用）
     * @param stack 物品堆栈
     */
    public void resumeCooldown(ItemStack stack) {
        setCooldownPaused(stack, false);
        setHooking(stack, false);
    }
    
    /**
     * 减少一半的肉钩冷却时间（用于未击中实体时）
     * @param stack 物品堆栈
     */
    public void reduceHalfCooldown(ItemStack stack) {
        int currentCooldown = getHookCooldown(stack);
        if (currentCooldown > 0) {
            setHookCooldown(stack, currentCooldown / 2);
        }
        // 恢复冷却计算
        resumeCooldown(stack);
    }

    public void resetHookCooldown(ItemStack stack) {
        setHookCooldown(stack, 0);
        setCooldownPaused(stack, false);
        setHooking(stack, false);
        resetPauseCounter(stack);
    }

    public static void setHooking(Player player, boolean hooking) {
        setHookingStack(player.getMainHandItem(), hooking);
        setHookingStack(player.getOffhandItem(), hooking);

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (setHookingStack(player.getInventory().getItem(i), hooking)) {
                break;
            }
        }
    }

    public static boolean resetHookCooldown(Player player) {
        boolean reset = resetHookCooldownStack(player.getMainHandItem());
        reset |= resetHookCooldownStack(player.getOffhandItem());

        if (!reset) {
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                if (resetHookCooldownStack(player.getInventory().getItem(i))) {
                    reset = true;
                    break;
                }
            }
        }

        return reset;
    }

    private static boolean resetHookCooldownStack(ItemStack stack) {
        if (stack.getItem() instanceof Supershotgun supershotgun) {
            supershotgun.resetHookCooldown(stack);
            return true;
        }
        return false;
    }

    private static boolean setHookingStack(ItemStack stack, boolean hooking) {
        if (stack.getItem() instanceof Supershotgun) {
            setHooking(stack, hooking);
            return true;
        }
        return false;
    }

    private static void ensureGeckoId(ItemStack stack, Level level) {
        if (stack.isEmpty() || !(stack.getItem() instanceof Supershotgun) || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        GeoItem.getOrAssignId(stack, serverLevel);
    }

    private static void triggerShotFeedback(Level level, Player player) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        Vec3 look = player.getLookAngle();
        Vec3 muzzle = player.getEyePosition().add(look.scale(1.15D));
        serverLevel.sendParticles(ParticleTypes.SMOKE, muzzle.x, muzzle.y - 0.08D, muzzle.z,
                3, 0.16D, 0.12D, 0.16D, 0.025D);
        serverLevel.sendParticles(ParticleTypes.SMOKE, muzzle.x, muzzle.y - 0.08D, muzzle.z,
                4, 0.12D, 0.08D, 0.12D, 0.02D);

        if (player instanceof ServerPlayer serverPlayer) {
            GWRENetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new SuperShotgunFeedbackPacket());
        }
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

    private static void setReloadAnimation(ItemStack stack, int cooldownTicks) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(NBT_RELOAD_TIMER, cooldownTicks);
        tag.putInt(NBT_RELOAD_ANIMATION_TICKS, Math.max(1, cooldownTicks - GECKO_FIRE_TICKS));
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
            if (tag.getInt(NBT_RELOAD_TIMER) > 0) {
                tag.putInt(NBT_GECKO_ANIMATION_SEQUENCE, tag.getInt(NBT_GECKO_ANIMATION_SEQUENCE) + 1);
            }
        }

        int reloadTimer = tag.getInt(NBT_RELOAD_TIMER);
        if (reloadTimer > 1) {
            tag.putInt(NBT_RELOAD_TIMER, reloadTimer - 1);
        } else if (reloadTimer == 1) {
            tag.putInt(NBT_RELOAD_TIMER, 0);
            tag.remove(NBT_RELOAD_ANIMATION_TICKS);
        }
    }

    private static RawAnimation getGeckoAnimation(@Nullable ItemStack stack) {
        if (stack != null && stack.hasTag() && stack.getOrCreateTag().getInt(NBT_GECKO_ANIMATION_TIMER) > 0) {
            String animation = stack.getOrCreateTag().getString(NBT_GECKO_ANIMATION);
            if (GECKO_ANIM_HOOK.equals(animation)) {
                return HOOK_ANIM;
            }
            if (GECKO_ANIM_FIRE.equals(animation)) {
                return FIRE_ANIM;
            }
        }

        if (stack != null && stack.hasTag()) {
            CompoundTag tag = stack.getOrCreateTag();
            if (tag.getBoolean(NBT_HOOKING)) {
                return HOOKING_ANIM;
            }
            if (tag.getInt(NBT_RELOAD_TIMER) > 0) {
                return RELOAD_ANIM;
            }
        }

        return IDLE_ANIM;
    }

    private static float getGeckoAnimationSpeed(@Nullable ItemStack stack) {
        if (stack != null && stack.hasTag()) {
            CompoundTag tag = stack.getOrCreateTag();
            if (tag.getInt(NBT_GECKO_ANIMATION_TIMER) <= 0 && tag.getInt(NBT_RELOAD_TIMER) > 0) {
                int reloadTicks = Math.max(1, tag.getInt(NBT_RELOAD_ANIMATION_TICKS));
                return RELOAD_ANIMATION_TICKS / reloadTicks;
            }
        }

        return 1.0F;
    }

    private static void restartAnimationIfSequenceChanged(AnimationState<Supershotgun> state, @Nullable ItemStack stack) {
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

    private static void setHooking(ItemStack stack, boolean hooking) {
        stack.getOrCreateTag().putBoolean(NBT_HOOKING, hooking);
    }
}
