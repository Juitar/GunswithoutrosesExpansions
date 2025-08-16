package juitar.gwrexpansions.item;

import juitar.gwrexpansions.config.GWREConfig;
import lykrast.gunswithoutroses.entity.BulletEntity;
import lykrast.gunswithoutroses.item.GunItem;
import lykrast.gunswithoutroses.item.IBullet;
import lykrast.gunswithoutroses.registry.GWRAttributes;
import lykrast.gunswithoutroses.registry.GWREnchantments;
import lykrast.gunswithoutroses.registry.GWRItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.function.Supplier;

/**
 * 可配置的发射器类
 * 使用GWREConfig中的配置项来动态设置武器属性
 * 与普通枪不同，这个类在finishUsingItem时发射弹幕
 */
public class ConfigurableLauncherItem extends GunItem {
    protected final Supplier<GWREConfig.GunConfig> config;
    private static final String TAG_USE_TICKS = "UseTicks";
    private static final int MAX_USE_TICKS = 40; // 最大蓄力时间
    
    /**
     * 创建可配置的发射器
     * @param properties 物品属性
     * @param bonusDamage 额外伤害（会被配置覆盖）
     * @param damageMultiplier 伤害倍率（会被配置覆盖）
     * @param fireDelay 射击延迟（会被配置覆盖）
     * @param inaccuracy 不精确度（会被配置覆盖）
     * @param enchantability 附魔能力
     * @param configSupplier 配置供应器
     */
    public ConfigurableLauncherItem(Properties properties, 
                               int bonusDamage,
                               double damageMultiplier,
                               int fireDelay,
                               double inaccuracy,
                               int enchantability,
                               Supplier<GWREConfig.GunConfig> configSupplier) {
        super(properties, bonusDamage, damageMultiplier, fireDelay, inaccuracy, enchantability);
        this.config = configSupplier;
    }
    
    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72000; // 允许长时间蓄力
    }
    
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        
        // 检查是否有足够的弹药
        if (player.getAbilities().instabuild || hasAmmo(player)) {
            // 开始使用物品
            player.startUsingItem(hand);
            return InteractionResultHolder.consume(stack);
        } else {
            return InteractionResultHolder.fail(stack);
        }
    }
    
    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        if (!level.isClientSide) {
            // 计算已使用时间
            int useTicks = getUseDuration(stack) - remainingUseDuration;
            
            // 存储蓄力时间
            CompoundTag tag = stack.getOrCreateTag();
            tag.putInt(TAG_USE_TICKS, Math.min(useTicks, MAX_USE_TICKS));
            
            // 播放蓄力音效
            if (useTicks % 10 == 0 && useTicks > 0) {
                level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), 
                      SoundEvents.CROSSBOW_LOADING_MIDDLE, SoundSource.PLAYERS, 
                      0.5F, 1.0F);
            }
        }
    }
    
    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        // 获取蓄力时间
        CompoundTag tag = stack.getOrCreateTag();
        int useTicks = tag.getInt(TAG_USE_TICKS);
        
        // 发射弹幕
        if (!level.isClientSide && entity instanceof Player player) {
            // 计算蓄力加成
            float chargeBonus = calculateChargeBonus(useTicks);
            
            // 发射弹幕
            fireWithCharge(level, player, stack, chargeBonus);
            
            // 记录统计信息
            player.awardStat(Stats.ITEM_USED.get(this));
            
            // 添加冷却时间
            player.getCooldowns().addCooldown(this, getFireDelay(stack, player));
        }
        
        // 清空蓄力时间
        tag.putInt(TAG_USE_TICKS, 0);
        
        return stack;
    }
    
    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        // 如果提前松开，也可以发射，但威力可能较小
        if (!level.isClientSide && entity instanceof Player player) {
            CompoundTag tag = stack.getOrCreateTag();
            int useTicks = tag.getInt(TAG_USE_TICKS);
            
            // 只有达到最小蓄力时间才发射
            if (useTicks >= 5) {
                // 计算蓄力加成
                float chargeBonus = calculateChargeBonus(useTicks);
                
                // 发射弹幕
                fireWithCharge(level, player, stack, chargeBonus);
                
                // 记录统计信息
                player.awardStat(Stats.ITEM_USED.get(this));
                
                // 添加冷却时间
                player.getCooldowns().addCooldown(this, getFireDelay(stack, player));
            }
            
            // 清空蓄力时间
            tag.putInt(TAG_USE_TICKS, 0);
        }
    }
    
    /**
     * 计算蓄力加成
     * @param useTicks 蓄力时间
     * @return 伤害加成系数
     */
    protected float calculateChargeBonus(int useTicks) {
        // 将蓄力时间转换为0.5-1.5的加成系数
        return 0.5F + Math.min(1.0F, (float)useTicks / MAX_USE_TICKS);
    }
    
    /**
     * 查找玩家背包中的弹药
     * @param player 玩家
     * @return 弹药物品栈，如果没有则返回空物品栈
     */
    protected ItemStack findAmmo(Player player) {
        // 使用player.getProjectile方法获取弹药
        ItemStack stack = new ItemStack(this); // 创建一个当前物品的副本用于查找弹药
        return player.getProjectile(stack);
    }
    
    /**
     * 检查玩家是否有弹药
     * @param player 玩家
     * @return 是否有弹药
     */
    protected boolean hasAmmo(Player player) {
        return !findAmmo(player).isEmpty();
    }
    
    /**
     * 发射弹幕
     * @param level 世界
     * @param shooter 射击者
     * @param gun 枪械物品
     * @param chargeBonus 蓄力加成
     */
    protected void fireWithCharge(Level level, Player shooter, ItemStack gun, float chargeBonus) {
        // 应用蓄力加成到伤害
        double damage = getBonusDamage(gun, shooter) * getDamageMultiplier(gun, shooter) * chargeBonus;
        
        // 获取弹药
        ItemStack ammo = findAmmo(shooter);
        boolean bulletFree = shooter.getAbilities().instabuild || !shouldConsumeAmmo(gun, shooter);
        
        if (!ammo.isEmpty() || bulletFree) {
            // 如果有弹药或者不需要消耗弹药
            if (!bulletFree && !ammo.isEmpty()) {
                ammo.shrink(1);
            }
            
            // 获取子弹类型
            IBullet bullet = ammo.getItem() instanceof IBullet ? (IBullet)ammo.getItem() : GWRItems.ironBullet.get();
            
            // 发射子弹
            shoot(level, shooter, gun, ammo, bullet, bulletFree);
            
            // 播放发射音效
            level.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(), 
                          getFireSound(), SoundSource.PLAYERS, 
                          1.0F, 1.0F / (level.getRandom().nextFloat() * 0.4F + 1.2F));
        }
    }
    
    @Override
    public void inventoryTick(ItemStack stack, Level level, net.minecraft.world.entity.Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
    }
    
    // 动态获取配置值
    @Override
    public double getBonusDamage(ItemStack stack, @Nullable LivingEntity shooter) {
        // 保留原版附魔计算
        int impact = stack.getEnchantmentLevel(GWREnchantments.impact.get());
        double bonus = impact >= 1 ? GWREnchantments.impactBonus(impact) : 0.0;

        // 保留原版属性计算
        if (shooter != null && shooter.getAttribute(GWRAttributes.dmgBase.get()) != null) {
            bonus += shooter.getAttributeValue(GWRAttributes.dmgBase.get());
        }

        // 使用配置值替换原版固定值
        return config.get().bonusDamage.get() + bonus;
    }

    @Override
    public double getDamageMultiplier(ItemStack stack, @Nullable LivingEntity shooter) {
        // 保留原版属性计算
        if (shooter != null && shooter.getAttribute(GWRAttributes.dmgTotal.get()) != null) {
            return config.get().damageMultiplier.get() * shooter.getAttributeValue(GWRAttributes.dmgTotal.get());
        }
        return config.get().damageMultiplier.get();
    }

    @Override
    public int getFireDelay(ItemStack stack, @Nullable LivingEntity shooter) {
        // 从配置获取基础值
        int baseDelay = config.get().fireDelay.get();

        // 保留原版附魔计算
        int sleight = stack.getEnchantmentLevel(GWREnchantments.sleightOfHand.get());
        int delay = sleight > 0 ? GWREnchantments.sleightModify(sleight, baseDelay) : baseDelay;

        // 保留原版属性计算
        if (shooter != null && shooter.getAttribute(GWRAttributes.fireDelay.get()) != null) {
            delay = (int)(delay * shooter.getAttributeValue(GWRAttributes.fireDelay.get()));
        }

        return Math.max(1, delay);
    }

    @Override
    public double getInaccuracy(ItemStack stack, @Nullable LivingEntity shooter) {
        // 从配置获取基础值
        double baseInaccuracy = config.get().inaccuracy.get();

        // 保留原版属性计算
        if (shooter != null && shooter.getAttribute(GWRAttributes.spread.get()) != null) {
            baseInaccuracy *= shooter.getAttributeValue(GWRAttributes.spread.get());
        }

        // 保留原版附魔计算
        int bullseye = stack.getEnchantmentLevel(GWREnchantments.bullseye.get());
        return Math.max(0.0, bullseye >= 1 ?
                GWREnchantments.bullseyeModify(bullseye, baseInaccuracy) :
                baseInaccuracy);
    }
} 