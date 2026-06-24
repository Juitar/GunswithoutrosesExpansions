package juitar.gwrexpansions.item.vanilla;

import juitar.gwrexpansions.config.GWREConfig;
import juitar.gwrexpansions.entity.vanilla.MeatHookEntity;
import juitar.gwrexpansions.item.ConfigurableGunItem;
import juitar.gwrexpansions.item.GunSkillItem;
import juitar.gwrexpansions.item.GunSkillTooltip;
import lykrast.gunswithoutroses.entity.BulletEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Supershotgun extends ConfigurableGunItem implements GunSkillItem {
    public static final String SUPER_SHOTGUN_SHOT_TAG = "GWRESuperShotgunShot";
    private static final double HOOK_RANGE = 32.0D; // 肉钩最大射程
    private static final double PULL_SPEED = 1.5D; // 拉近速度
    private static final int HOOK_COOLDOWN = 100; // 肉钩冷却时间（tick）
    private static final int MAX_PAUSE_TIME = 160; // 最大暂停时间（8秒 = 160 tick）
    private static final String NBT_HOOK_COOLDOWN = "Hook_Cooldown"; // 冷却时间NBT
    private static final String NBT_COOLDOWN_PAUSED = "Cooldown_Paused"; // 冷却暂停标志NBT
    private static final String NBT_PAUSE_COUNTER = "Pause_Counter"; // 暂停计数器NBT

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
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
        return super.use(world, player, hand);
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
    
    /**
     * 恢复冷却计算（肉钩返回结果后调用）
     * @param stack 物品堆栈
     */
    public void resumeCooldown(ItemStack stack) {
        setCooldownPaused(stack, false);
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
        resetPauseCounter(stack);
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
}
