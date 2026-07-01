package juitar.gwrexpansions.item.BOMD;
import juitar.gwrexpansions.config.GWREConfig;
import juitar.gwrexpansions.client.render.SkullcrusherGeoRenderer;
import juitar.gwrexpansions.item.ConfigurableGatlingItem;
import juitar.gwrexpansions.registry.CompatBOMD;
import juitar.gwrexpansions.registry.GWRECataclysmEnchantments;
import juitar.gwrexpansions.registry.GWREItems;
import juitar.gwrexpansions.registry.VanillaItem;
import lykrast.gunswithoutroses.entity.BulletEntity;
import lykrast.gunswithoutroses.item.IBullet;
import lykrast.gunswithoutroses.registry.GWRAttributes;
import lykrast.gunswithoutroses.registry.GWREnchantments;
import lykrast.gunswithoutroses.registry.GWRItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.Predicate;

public class Skullcrusher extends ConfigurableGatlingItem implements GeoItem {
    private static final String CONSECUTIVE_TIME_KEY = "ConsecutiveShootTime";
    private static final String LAST_SHOT_TIME_KEY = "LastShotTime";
    private static final String GECKO_FIRING_KEY = "GeckoFiring";
    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final RawAnimation FIRE_ANIM = RawAnimation.begin().thenLoop("fire");
    private static final float MAX_FIRE_ANIMATION_SPEED = 2.0F;
    private static final int DECAY_RATE = 1; // 每次减少的点数
    private static final double MAX_SPEED_REDUCTION = 0.5; // 最大射速减少50%
    private static final double SLOW_BURNING_FURY_MAX_SPEED_REDUCTION = 0.7D;
    private static final double GILDED_SKULL_DIAMOND_CHANCE = 0.25D;
    private static final int MAX_CONSECUTIVE_TIME = 100; // 重新定义最大连续射击时间计数
    private static final UUID CHARGE_MOVEMENT_SPEED_UUID = UUID.fromString("ff402a19-bc6c-4a83-a64f-bcc962a1d8e0");
    private static final double MAX_EFFECTIVE_MOVEMENT_MULTIPLIER = 1.5D;
    private static final double MAX_AMMO_SAVE_CHANCE = 0.5D;
    private static final int MAX_CHARGED_PROJECTILE_BONUS = 2;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    
    /**
     * 创建可配置的加特林
     *
     * @param properties       物品属性
     * @param bonusDamage      额外伤害（会被配置覆盖）
     * @param damageMultiplier 伤害倍率（会被配置覆盖）
     * @param fireDelay        射击延迟（会被配置覆盖）
     * @param inaccuracy       不精确度（会被配置覆盖）
     * @param enchantability   附魔能力
     * @param configSupplier   配置供应器
     */
    public Skullcrusher(Properties properties, int bonusDamage, double damageMultiplier, int fireDelay, double inaccuracy, int enchantability, Supplier<GWREConfig.GunConfig> configSupplier) {
        super(properties, bonusDamage, damageMultiplier, fireDelay, inaccuracy, enchantability, configSupplier);
        this.chanceFreeShot = (double)0.25F;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private SkullcrusherGeoRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null) {
                    this.renderer = new SkullcrusherGeoRenderer();
                }
                return this.renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, state -> {
            ItemStack stack = state.getData(software.bernie.geckolib.constant.DataTickets.ITEMSTACK);
            boolean firing = isGeckoFiring(stack);
            state.setControllerSpeed(firing ? getFireAnimationSpeed(stack) : 1.0F);
            state.setAnimation(firing ? FIRE_ANIM : IDLE_ANIM);
            return PlayState.CONTINUE;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
    
    @Override
    protected void addExtraStatsTooltip(ItemStack stack, @Nullable Level world, List<Component> tooltip) {
        tooltip.add(Component.translatable("tooltip.gwrexpansions.skullcrusher.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.gwrexpansions.skullcrusher.desc2").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.NONE;
    }
    
    // 使用inventoryTick方法实现武器在物品栏中的计数衰减
    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean isSelected) {
        super.inventoryTick(stack, world, entity, slot, isSelected);
        
        // 只有在不是正在使用中的物品才进行衰减
        if (!world.isClientSide  && entity instanceof Player player) {
            // 检查玩家是否正在使用这把武器
            boolean isUsingThisGun = player.isUsingItem() && (
                (player.getUseItem() == stack) || 
                (player.getUseItem().getItem() instanceof Skullcrusher && player.getUseItem().getItem() == stack.getItem())
            );
            
            // 只有在不使用武器时才衰减
            if (!isUsingThisGun) {
                removeChargeSpeedModifier(player);
                ensureGeckoId(stack, world);
                setGeckoFiring(stack, false);
                int consecutiveTime = stack.getOrCreateTag().getInt(CONSECUTIVE_TIME_KEY);
                if (consecutiveTime > 0) {
                    // 每次减少DECAY_RATE点
                    stack.getOrCreateTag().putInt(CONSECUTIVE_TIME_KEY, Math.max(0, consecutiveTime - DECAY_RATE));
                }
            }
        }
    }
    
    @Override
    public void onUseTick(Level world, LivingEntity user, ItemStack gun, int ticks) {
        if (user instanceof Player player) {
            ensureGeckoId(gun, world);
            setGeckoFiring(gun, true);
            int used = this.getUseDuration(gun) - ticks;
            applyChargeSpeedModifier(player, gun, used);
            
            // 更新连续射击计数
            long currentTime = world.getGameTime();
            int consecutiveTime = gun.getOrCreateTag().getInt(CONSECUTIVE_TIME_KEY);
            consecutiveTime = Math.min(MAX_CONSECUTIVE_TIME, consecutiveTime + 2);
            gun.getOrCreateTag().putInt(CONSECUTIVE_TIME_KEY, consecutiveTime);
            gun.getOrCreateTag().putLong(LAST_SHOT_TIME_KEY, currentTime);
            
            if (used > 0 && used % this.getFireDelay(gun, player) == 0) {
                // 检查玩家是否有skull物品
                boolean hasSkull = false;
                ItemStack skullAmmo = ItemStack.EMPTY;
                
                // 检查玩家背包中是否有skull弹药
                for (ItemStack stack : player.getInventory().items) {
                    if (!stack.isEmpty() && stack.getItem().equals(CompatBOMD.skull.get())) {
                        hasSkull = true;
                        skullAmmo = stack;
                        break;
                    }
                }
                
                // 检查副手
                if (!hasSkull && !player.getOffhandItem().isEmpty() && player.getOffhandItem().getItem().equals(CompatBOMD.skull.get())) {
                    hasSkull = true;
                    skullAmmo = player.getOffhandItem();
                }
                
                // 只有当玩家有skull或者是创造模式时才能射击
                if (hasSkull || player.getAbilities().instabuild) {
                    if (!world.isClientSide) {
                        // 生成3-5个骨头碎片弹射物，蓄满后额外+2
                        int shots = 3 + world.getRandom().nextInt(3) + getChargedProjectileBonus(used);
                        
                        for (int i = 0; i < shots; ++i) {
                            Item projectileItem = getProjectileItem(gun, world);
                            BulletEntity shot = ((IBullet) projectileItem).createProjectile(world, new ItemStack(projectileItem), player);
                            shot.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, (float)this.getProjectileSpeed(gun, player), (float)this.getInaccuracy(gun, player));
                            shot.setDamage(Math.max((double)0.0F, shot.getDamage() + this.getBonusDamage(gun, player)) * this.getDamageMultiplier(gun, player));
                            if (player.getAttribute((Attribute) GWRAttributes.knockback.get()) != null) {
                                shot.setKnockbackStrength(shot.getKnockbackStrength() + player.getAttributeValue((Attribute)GWRAttributes.knockback.get()));
                            }
                            shot.setHeadshotMultiplier(this.getHeadshotMultiplier(gun, player));
                            this.affectBulletEntity(player, gun, shot, player.getAbilities().instabuild);
                            world.addFreshEntity(shot);
                        }
                        
                        // 消耗弹药
                        boolean bulletFree = player.getAbilities().instabuild || !this.shouldConsumeAmmo(gun, player);
                        if (!bulletFree) {
                            skullAmmo.shrink(1);
                        }
                        
                        gun.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(player.getUsedItemHand()));
                    }

                    world.playSound((Player)null, player.getX(), player.getY(), player.getZ(), this.getFireSound(), SoundSource.PLAYERS, 1.0F, world.getRandom().nextFloat() * 0.4F + 0.8F);
                    player.awardStat(Stats.ITEM_USED.get(this));
                }
            }
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level world, LivingEntity entity, int timeLeft) {
        if (entity instanceof Player player) {
            removeChargeSpeedModifier(player);
        }
        ensureGeckoId(stack, world);
        setGeckoFiring(stack, false);
        super.releaseUsing(stack, world, entity, timeLeft);
    }

    @Override
    public boolean onDroppedByPlayer(ItemStack item, Player player) {
        removeChargeSpeedModifier(player);
        ensureGeckoId(item, player.level());
        setGeckoFiring(item, false);
        return super.onDroppedByPlayer(item, player);
    }

    private static void ensureGeckoId(ItemStack stack, Level world) {
        if (world instanceof ServerLevel serverLevel) {
            GeoItem.getOrAssignId(stack, serverLevel);
        }
    }

    private static void setGeckoFiring(ItemStack stack, boolean firing) {
        stack.getOrCreateTag().putBoolean(GECKO_FIRING_KEY, firing);
    }

    private static boolean isGeckoFiring(ItemStack stack) {
        return stack != null && stack.hasTag() && stack.getOrCreateTag().getBoolean(GECKO_FIRING_KEY);
    }

    private static float getFireAnimationSpeed(ItemStack stack) {
        if (stack == null || !stack.hasTag()) {
            return 1.0F;
        }

        int consecutiveTime = stack.getOrCreateTag().getInt(CONSECUTIVE_TIME_KEY);
        float progress = Math.min(1.0F, Math.max(0.0F, consecutiveTime / (float)MAX_CONSECUTIVE_TIME));
        return 1.0F + (MAX_FIRE_ANIMATION_SPEED - 1.0F) * progress;
    }

    private static void applyChargeSpeedModifier(Player player, ItemStack stack, int useTicks) {
        AttributeInstance movementSpeed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movementSpeed == null) {
            return;
        }

        movementSpeed.removeModifier(CHARGE_MOVEMENT_SPEED_UUID);
        double progress = getUseProgress(useTicks);
        double maxEffectiveMovementMultiplier = getMaxEffectiveMovementMultiplier(stack);
        double effectiveMultiplier = 1.0D + (maxEffectiveMovementMultiplier - 1.0D) * progress;
        double attributeMultiplier = effectiveMultiplier - 1.0D;
        movementSpeed.addTransientModifier(new AttributeModifier(CHARGE_MOVEMENT_SPEED_UUID,
                "Skullcrusher charge movement speed", attributeMultiplier, AttributeModifier.Operation.MULTIPLY_TOTAL));
    }

    private static void removeChargeSpeedModifier(Player player) {
        AttributeInstance movementSpeed = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (movementSpeed != null) {
            movementSpeed.removeModifier(CHARGE_MOVEMENT_SPEED_UUID);
        }
    }

    private static double getUseProgress(int useTicks) {
        return Math.min(1.0D, Math.max(0.0D, useTicks / (double)MAX_CONSECUTIVE_TIME));
    }

    private static double getMaxEffectiveMovementMultiplier(ItemStack stack) {
        return GWRECataclysmEnchantments.has(stack, GWRECataclysmEnchantments.SLOW_BURNING_FURY)
                ? MAX_EFFECTIVE_MOVEMENT_MULTIPLIER * 1.2D
                : MAX_EFFECTIVE_MOVEMENT_MULTIPLIER;
    }

    private static int getChargedProjectileBonus(int useTicks) {
        return getUseProgress(useTicks) >= 1.0D ? MAX_CHARGED_PROJECTILE_BONUS : 0;
    }

    private static int getChargedProjectileBonus(ItemStack stack) {
        return getChargedProjectileBonus(stack.getOrCreateTag().getInt(CONSECUTIVE_TIME_KEY));
    }

    @Override
    public boolean hasMultipleProjectiles() {
        return true;
    }

    @Override
    public int getProjectilesPerShot(ItemStack stack, @Nullable LivingEntity shooter) {
        return 5 + getChargedProjectileBonus(stack);
    }

    @Override
    protected boolean isProjectileCountModified(ItemStack stack) {
        return getChargedProjectileBonus(stack) > 0 || super.isProjectileCountModified(stack);
    }
//原use方法
@Override
public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
    ItemStack itemstack = player.getItemInHand(hand);
    
    // 检查玩家是否有skull物品
    boolean hasSkull = false;
    
    // 检查玩家背包中是否有skull物品
    for (ItemStack stack : player.getInventory().items) {
        if (!stack.isEmpty() && stack.getItem().equals(CompatBOMD.skull.get())) {
            hasSkull = true;
            break;
        }
    }
    
    // 检查副手
    if (!hasSkull && !player.getOffhandItem().isEmpty() && player.getOffhandItem().getItem().equals(CompatBOMD.skull.get())) {
        hasSkull = true;
    }
    
    // 只有当玩家有skull物品或者是创造模式时才能使用
    if (!player.getAbilities().instabuild && !hasSkull) {
        return InteractionResultHolder.fail(itemstack);
    }
    else {
        ensureGeckoId(itemstack, world);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(itemstack);
    }
}

    @Override
    public int getFireDelay(ItemStack stack, @Nullable LivingEntity shooter) {
        return Math.max(1, (int)Math.ceil(getFireDelayFractional(stack, shooter)));
    }

    @Override
    public double getFireDelayFractional(ItemStack stack, @Nullable LivingEntity shooter) {
        double baseDelay = super.getFireDelayFractional(stack, shooter);
        if (GWRECataclysmEnchantments.has(stack, GWRECataclysmEnchantments.SLOW_BURNING_FURY)) {
            baseDelay += 2.0D;
        }
        return Math.max(1.0D, baseDelay
                * (1.0D - getFireDelayReductionFactor(stack)));
    }

    @Override
    protected boolean isFireDelayModified(ItemStack stack) {
        return getFireDelayReductionFactor(stack) > 0.0D || super.isFireDelayModified(stack);
    }

    private static double getFireDelayReductionFactor(ItemStack stack) {
        int consecutiveTime = stack.getOrCreateTag().getInt(CONSECUTIVE_TIME_KEY);
        return (consecutiveTime / (double)MAX_CONSECUTIVE_TIME) * getMaxFireDelayReduction(stack);
    }

    private static double getMaxFireDelayReduction(ItemStack stack) {
        return GWRECataclysmEnchantments.has(stack, GWRECataclysmEnchantments.SLOW_BURNING_FURY)
                ? SLOW_BURNING_FURY_MAX_SPEED_REDUCTION
                : MAX_SPEED_REDUCTION;
    }

    private static Item getProjectileItem(ItemStack gun, Level world) {
        if (GWRECataclysmEnchantments.has(gun, GWRECataclysmEnchantments.GILDED_SKULL)
                && world.getRandom().nextDouble() < GILDED_SKULL_DIAMOND_CHANCE) {
            return VanillaItem.diamond_bullet.get();
        }
        return CompatBOMD.bone_scrap.get();
    }
    
    @Override
    public double getBonusDamage(ItemStack stack, @Nullable LivingEntity shooter) {
        double baseDamage = super.getBonusDamage(stack, shooter);
        int   consecutiveTime = stack.getOrCreateTag().getInt(CONSECUTIVE_TIME_KEY);
        // 调整伤害计算，与连续射击计数保持一致
        // 当连续射击达到最大值时，额外伤害为1.5
        double damageBonus = consecutiveTime / (double)MAX_CONSECUTIVE_TIME;
        return baseDamage + damageBonus;
    }

    @Override
    public boolean shouldConsumeAmmo(ItemStack stack, LivingEntity shooter) {
        double saveChance = getChargedAmmoSaveChance(stack);
        if (saveChance > 0.0D && shooter.getRandom().nextDouble() < saveChance) {
            return false;
        }

        if (shooter.getAttribute((Attribute)GWRAttributes.chanceUseAmmo.get()) != null) {
            double chanceUseAmmo = shooter.getAttributeValue((Attribute)GWRAttributes.chanceUseAmmo.get());
            if (chanceUseAmmo < 1.0D && shooter.getRandom().nextDouble() > chanceUseAmmo) {
                return false;
            }
        }

        int preserving = stack.getEnchantmentLevel((Enchantment)GWREnchantments.preserving.get());
        return preserving < 1 || !GWREnchantments.rollPreserving(preserving, shooter.getRandom());
    }

    @Override
    public double getInverseChanceFreeShot(ItemStack stack) {
        double inverse = 1.0D - getChargedAmmoSaveChance(stack);
        int preserving = stack.getEnchantmentLevel((Enchantment)GWREnchantments.preserving.get());
        if (preserving >= 1) {
            inverse *= GWREnchantments.preservingInverse(preserving);
        }
        return inverse;
    }

    @Override
    protected boolean isChanceFreeShotModified(ItemStack stack) {
        return stack.getOrCreateTag().getInt(CONSECUTIVE_TIME_KEY) > 0 || super.isChanceFreeShotModified(stack);
    }

    private double getChargedAmmoSaveChance(ItemStack stack) {
        int useTicks = stack.getOrCreateTag().getInt(CONSECUTIVE_TIME_KEY);
        double progress = Math.min(1.0D, Math.max(0.0D, useTicks / (double)MAX_CONSECUTIVE_TIME));
        return this.chanceFreeShot + (MAX_AMMO_SAVE_CHANCE - this.chanceFreeShot) * progress;
    }
}
