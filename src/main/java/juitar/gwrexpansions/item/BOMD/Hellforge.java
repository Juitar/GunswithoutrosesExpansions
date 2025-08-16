package juitar.gwrexpansions.item.BOMD;

import juitar.gwrexpansions.config.GWREConfig;
import juitar.gwrexpansions.entity.BOMD.CoinEntity;
import juitar.gwrexpansions.item.ConfigurableGunItem;
import juitar.gwrexpansions.registry.GWREEffects;
import juitar.gwrexpansions.registry.GWRESounds;
import lykrast.gunswithoutroses.entity.BulletEntity;
import lykrast.gunswithoutroses.item.IBullet;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class Hellforge extends ConfigurableGunItem {
    private final int conintime;
    private static final String NBT_SHOT_TIMES = "ShotTimes";
    private static final String NBT_COINS = "Coins";
    private static final String NBT_COIN_COOLDOWN = "CoinCooldown";
    private static final int MAX_COINS = 4; // 硬币上限
    private static final int COIN_COOLDOWN_TICKS = 10; // 抛硬币冷却时间

    /**
     * 创建可配置的枪支
     *
     * @param properties       物品属性
     * @param bonusDamage      额外伤害（会被配置覆盖）
     * @param damageMultiplier 伤害倍率（会被配置覆盖）
     * @param fireDelay        射击延迟（会被配置覆盖）
     * @param inaccuracy       不精确度（会被配置覆盖）
     * @param enchantability   附魔能力
     * @param conintime        连击时间阈值
     * @param configSupplier   配置供应器
     */
    public Hellforge(Properties properties, int bonusDamage, double damageMultiplier, int fireDelay, double inaccuracy, int enchantability, int conintime, Supplier<GWREConfig.GunConfig> configSupplier) {
        super(properties, bonusDamage, damageMultiplier, fireDelay, inaccuracy, enchantability, configSupplier);
        this.conintime = conintime;
    }

    @Override
    protected void shoot(Level level, Player player, ItemStack gun, ItemStack ammo, IBullet bulletItem, boolean bulletFree) {
        // 获取或创建NBT标签
        CompoundTag tag = gun.getOrCreateTag();
        int shotTimes = tag.getInt(NBT_SHOT_TIMES);

        // 增加射击次数
        shotTimes++;
        tag.putInt(NBT_SHOT_TIMES, shotTimes);

        // 检查是否达到连击阈值
        if (shotTimes >= conintime) {
            // 重置射击次数
            tag.putInt(NBT_SHOT_TIMES, 0);

            // 增加硬币数量（不超过上限）
            int coins = tag.getInt(NBT_COINS);
            if (coins < MAX_COINS) {
                coins++;
                tag.putInt(NBT_COINS, coins);
                // 播放换弹音效
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    GWRESounds.HELLFORGE_REVOLVER_RELOAD.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
            }
        }
        // 正常射击
        super.shoot(level, player, gun, ammo, bulletItem, bulletFree);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        // 检查玩家是否在潜行状态（Shift+右键抛硬币）
        if (player.isShiftKeyDown()) {
            CompoundTag tag = itemstack.getOrCreateTag();
            int coins = tag.getInt(NBT_COINS);
            int cooldown = tag.getInt(NBT_COIN_COOLDOWN);

            // 检查冷却时间
            if (cooldown > 0) {
                if (level.isClientSide) {
                    player.displayClientMessage(Component.translatable("message.gwrexpansions.coin_cooldown",
                        String.format("%.1f", cooldown / 20.0f)), true);
                }
                return InteractionResultHolder.fail(itemstack);
            }

            if (coins > 0) {
                // 消耗一枚硬币
                coins--;
                tag.putInt(NBT_COINS, coins);

                // 设置冷却时间
                tag.putInt(NBT_COIN_COOLDOWN, COIN_COOLDOWN_TICKS);

                // 抛射硬币
                if (!level.isClientSide) {
                    throwCoin(level, player);
                }

                // 播放抛硬币音效
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    GWRESounds.HELLFORGE_REVOLVER_COIN_FLIP.get(), SoundSource.PLAYERS, 1.0F, 1.0F);

                return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
            } else {
                // 没有硬币，显示提示
                if (level.isClientSide) {
                    player.displayClientMessage(Component.translatable("message.gwrexpansions.no_coins"), true);
                }
                return InteractionResultHolder.fail(itemstack);
            }
        }

        // 检查是否达到连击阈值，决定是否需要延迟
        CompoundTag tag = itemstack.getOrCreateTag();
        int shotTimes = tag.getInt(NBT_SHOT_TIMES);

        // 只有在即将达到连击阈值时才触发延迟
        if (shotTimes + 1 >= conintime) {
            // 达到连击阈值，使用延迟射击
            return super.use(level, player, hand);
        } else {
            // 未达到连击阈值，立即射击（无延迟）
            return useWithoutDelay(level, player, hand, itemstack);
        }
    }

    /**
     * 无延迟射击方法
     */
    private InteractionResultHolder<ItemStack> useWithoutDelay(Level level, Player player, InteractionHand hand, ItemStack itemstack) {
        ItemStack ammo = player.getProjectile(itemstack);

        if (!ammo.isEmpty() || player.getAbilities().instabuild) {
            if (!level.isClientSide) {
                // 创造模式处理
                if (ammo.isEmpty()) {
                    ammo = new ItemStack(lykrast.gunswithoutroses.registry.GWRItems.ironBullet.get());
                }

                // 确保是有效的子弹
                IBullet parentBullet = (IBullet) (ammo.getItem() instanceof IBullet ? ammo.getItem() : lykrast.gunswithoutroses.registry.GWRItems.ironBullet.get());

                // 处理子弹袋等委托
                ItemStack firedAmmo = ammo;
                IBullet firedBullet = parentBullet;
                if (parentBullet.hasDelegate(ammo, player)) {
                    firedAmmo = parentBullet.getDelegate(ammo, player);
                    firedBullet = (IBullet) (firedAmmo.getItem() instanceof IBullet ? firedAmmo.getItem() : lykrast.gunswithoutroses.registry.GWRItems.ironBullet.get());
                }

                boolean bulletFree = player.getAbilities().instabuild || !shouldConsumeAmmo(itemstack, player);

                // 再次确保是有效子弹
                if (!(firedAmmo.getItem() instanceof IBullet)) {
                    firedAmmo = new ItemStack(lykrast.gunswithoutroses.registry.GWRItems.ironBullet.get());
                }

                // 射击
                this.shoot(level, player, itemstack, firedAmmo, firedBullet, bulletFree);

                // 消耗耐久度
                itemstack.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(player.getUsedItemHand()));

                // 消耗弹药
                if (!bulletFree) {
                    parentBullet.consume(ammo, player);
                }
            }

            // 播放音效
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                this.getFireSound(), SoundSource.PLAYERS, 1.0F,
                level.getRandom().nextFloat() * 0.4F + 0.8F);

            // 统计
            player.awardStat(net.minecraft.stats.Stats.ITEM_USED.get(this));

            return InteractionResultHolder.consume(itemstack);
        } else {
            return InteractionResultHolder.fail(itemstack);
        }
    }

    /**
     * 检查是否应该消耗弹药
     */
    public boolean shouldConsumeAmmo(ItemStack stack, LivingEntity shooter) {
        // 基础免费射击概率
        if (chanceFreeShot > 0 && shooter.getRandom().nextDouble() < chanceFreeShot) return false;

        // 属性检查
        if (shooter.getAttribute(lykrast.gunswithoutroses.registry.GWRAttributes.chanceUseAmmo.get()) != null) {
            double chance = shooter.getAttributeValue(lykrast.gunswithoutroses.registry.GWRAttributes.chanceUseAmmo.get());
            if (chance < 1 && shooter.getRandom().nextDouble() > chance) return false;
        }

        // Preserving附魔检查
        int preserving = stack.getEnchantmentLevel(lykrast.gunswithoutroses.registry.GWREnchantments.preserving.get());
        if (preserving >= 1 && lykrast.gunswithoutroses.registry.GWREnchantments.rollPreserving(preserving, shooter.getRandom())) return false;

        return true;
    }


    @Override
    public void inventoryTick(ItemStack stack, Level level, net.minecraft.world.entity.Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);

        // 更新冷却时间
        if (!level.isClientSide) {
            CompoundTag tag = stack.getOrCreateTag();
            int cooldown = tag.getInt(NBT_COIN_COOLDOWN);
            if (cooldown > 0) {
                cooldown--;
                tag.putInt(NBT_COIN_COOLDOWN, cooldown);
            }
        }
    }



    /**
     * 抛射硬币
     */
    public static void throwCoin(Level level, Player player) {
        if (!level.isClientSide) {
            CoinEntity coin = new CoinEntity(level, player);

            // 设置硬币位置（玩家眼部位置）
            Vec3 eyePos = player.getEyePosition();
            coin.setPos(eyePos.x, eyePos.y, eyePos.z);

            // 计算抛射方向（斜上方）
            Vec3 lookDirection = player.getLookAngle();
            double upwardAngle = Math.toRadians(45); // 45度向上

            // 计算新的方向向量
            double horizontalLength = Math.sqrt(lookDirection.x * lookDirection.x + lookDirection.z * lookDirection.z);
            double newY = Math.sin(upwardAngle);
            double horizontalScale = Math.cos(upwardAngle);

            Vec3 throwDirection = new Vec3(
                lookDirection.x * horizontalScale,
                newY,
                lookDirection.z * horizontalScale
            ).normalize();

            // 设置硬币的运动（降低速度便于射击）
            double throwSpeed = 0.8;
            coin.setDeltaMovement(throwDirection.scale(throwSpeed));

            // 添加硬币到世界
            level.addFreshEntity(coin);

            // 播放抛射音效
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 0.8F);
        }
    }

    @Override
    protected void affectBulletEntity(LivingEntity shooter, ItemStack gun, BulletEntity bullet, boolean bulletFree) {
        super.affectBulletEntity(shooter, gun, bullet, bulletFree);

        // 为子弹添加特殊标记，用于识别是Hellforge发射的子弹
        bullet.getPersistentData().putBoolean("HellforgeShot", true);
    }

    /**
     * 当子弹击中生物实体时调用
     * 为目标添加aimed效果，或处理反弹子弹的aimed效果移除
     */
    public static void onBulletHitLivingEntity(BulletEntity bullet, LivingEntity target, @Nullable Entity shooter) {
        CompoundTag bulletData = bullet.getPersistentData();

        // 检查是否是反弹子弹需要移除aimed效果
        if (bulletData.getBoolean("RemoveAimedOnHit")) {
            int aimedTargetId = bulletData.getInt("AimedTargetId");
            if (target.getId() == aimedTargetId) {
                // 移除aimed效果
                target.removeEffect(GWREEffects.AIMED.get());

                // 播放特殊音效表示aimed效果被移除
                if (target.level() != null) {
                    target.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                        SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 2.0F);

                    // 添加特殊粒子效果表示aimed效果被移除
                    for (int i = 0; i < 20; i++) {
                        target.level().addParticle(ParticleTypes.HAPPY_VILLAGER,
                            target.getX() + (target.getRandom().nextDouble() - 0.5) * 1.5,
                            target.getY() + target.getRandom().nextDouble() * 2.0,
                            target.getZ() + (target.getRandom().nextDouble() - 0.5) * 1.5,
                            0, 0.1, 0);
                    }
                }

                // 清除标记，避免重复移除
                bulletData.putBoolean("RemoveAimedOnHit", false);
                return;
            }
        }

        // 检查是否是Hellforge发射的子弹（添加aimed效果）
        if (bulletData.getBoolean("HellforgeShot")) {
            // 播放标记音效
            if (target.level() != null) {
                target.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.ARROW_HIT_PLAYER, SoundSource.PLAYERS, 0.5F, 1.5F);

                // 获取当前aimed效果等级来决定粒子数量
                MobEffectInstance currentEffect = target.getEffect(GWREEffects.AIMED.get());
                int particleCount = 10;
                if (currentEffect != null) {
                    particleCount += (currentEffect.getAmplifier() + 1) * 5; // 等级越高粒子越多
                }

                // 添加粒子效果使标记更明显
                for (int i = 0; i < particleCount; i++) {
                    target.level().addParticle(ParticleTypes.ENCHANTED_HIT,
                        target.getX() + (target.getRandom().nextDouble() - 0.5) * 1.0,
                        target.getY() + target.getRandom().nextDouble() * 2.0,
                        target.getZ() + (target.getRandom().nextDouble() - 0.5) * 1.0,
                        0, 0, 0);
                }
            }
        }
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
        // 添加精简的基本描述（只保留3条）
        tooltip.add(Component.translatable("tooltip.gwrexpansions.hellforge_revolver.desc1",conintime).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.gwrexpansions.hellforge_revolver.desc2").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.gwrexpansions.hellforge_revolver.desc3").withStyle(ChatFormatting.GRAY));

        // 显示当前状态
        CompoundTag tag = stack.getOrCreateTag();
        int shotTimes = tag.getInt(NBT_SHOT_TIMES);
        int coins = tag.getInt(NBT_COINS);

        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("tooltip.gwrexpansions.hellforge_revolver.shots", shotTimes, conintime)
            .withStyle(shotTimes >= conintime ? ChatFormatting.GOLD : ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("tooltip.gwrexpansions.hellforge_revolver.coins", coins, MAX_COINS)
            .withStyle(coins > 0 ? (coins >= MAX_COINS ? ChatFormatting.GOLD : ChatFormatting.YELLOW) : ChatFormatting.RED));

        // 添加操作说明
        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("tooltip.gwrexpansions.hellforge_revolver.usage").withStyle(ChatFormatting.YELLOW));
    }
}
