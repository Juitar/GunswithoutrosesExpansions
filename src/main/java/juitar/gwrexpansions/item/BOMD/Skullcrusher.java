package juitar.gwrexpansions.item.BOMD;
import juitar.gwrexpansions.config.GWREConfig;
import juitar.gwrexpansions.item.ConfigurableGatlingItem;
import juitar.gwrexpansions.registry.CompatBOMD;
import juitar.gwrexpansions.registry.GWREItems;
import lykrast.gunswithoutroses.entity.BulletEntity;
import lykrast.gunswithoutroses.item.IBullet;
import lykrast.gunswithoutroses.registry.GWRAttributes;
import lykrast.gunswithoutroses.registry.GWREnchantments;
import lykrast.gunswithoutroses.registry.GWRItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

public class Skullcrusher extends ConfigurableGatlingItem {
    private static final String CONSECUTIVE_TIME_KEY = "ConsecutiveShootTime";
    private static final String LAST_SHOT_TIME_KEY = "LastShotTime";
    private static final int DECAY_RATE = 1; // 每次减少的点数
    private static final double MAX_SPEED_REDUCTION = 0.5; // 最大射速减少50%
    private static final int MAX_CONSECUTIVE_TIME = 100; // 重新定义最大连续射击时间计数
    
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
    protected void shoot(Level world, Player player, ItemStack gun, ItemStack ammo, IBullet bulletItem, boolean bulletFree) {
        ItemStack override = this.overrideFiredStack(player, gun, ammo, bulletItem, bulletFree);
        if (override != ammo) {
            ammo = override;
            bulletItem = (IBullet)override.getItem();
        }
        boolean Fire = false;
        int shots = this.getProjectilesPerShot(gun, player);
        if(bulletItem.equals(CompatBOMD.skulls.get())) {
            shots = 3 + (new Random().nextInt(3));
            Fire = true;
        }
        if(Fire) {
        for(int i = 0; i < shots; ++i) {
                BulletEntity shot = bulletItem.createProjectile(world, ammo, player);
                shot = CompatBOMD.bone_scrap.get().createProjectile(world, new ItemStack(CompatBOMD.bone_scrap.get()), player);
                shot.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, (float)this.getProjectileSpeed(gun, player), (float)this.getInaccuracy(gun, player));
                shot.setDamage(Math.max((double)0.0F, shot.getDamage() + this.getBonusDamage(gun, player)) * this.getDamageMultiplier(gun, player));
                if (player.getAttribute((Attribute) GWRAttributes.knockback.get()) != null) {
                    shot.setKnockbackStrength(shot.getKnockbackStrength() + player.getAttributeValue((Attribute)GWRAttributes.knockback.get()));
                }
                shot.setHeadshotMultiplier(this.getHeadshotMultiplier(gun, player));
                this.affectBulletEntity(player, gun, shot, bulletFree);
                world.addFreshEntity(shot);
            }
        }
        else{
            return;
        }
    }
    @Override
    protected void addExtraStatsTooltip(ItemStack stack, @Nullable Level world, List<Component> tooltip) {
        tooltip.add(Component.translatable("tooltip.gwrexpansions.skullcrusher.desc").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.gwrexpansions.skullcrusher.desc2").withStyle(ChatFormatting.GRAY));
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
            int used = this.getUseDuration(gun) - ticks;
            
            // 更新连续射击计数
            long currentTime = world.getGameTime();
            // 获取当前连续射击计数
            int consecutiveTime = gun.getOrCreateTag().getInt(CONSECUTIVE_TIME_KEY);
            // 增加计数，上限为MAX_CONSECUTIVE_TIME
            consecutiveTime = Math.min(MAX_CONSECUTIVE_TIME, consecutiveTime + 2);
            // 保存回NBT
            gun.getOrCreateTag().putInt(CONSECUTIVE_TIME_KEY, consecutiveTime);
            // 更新最后射击时间
            gun.getOrCreateTag().putLong(LAST_SHOT_TIME_KEY, currentTime);
            // 只在射击时更新计数器，不再检查超时
            if (used > 0 && used % this.getFireDelay(gun, player) == 0) {
                
                // 以下是原有的射击逻辑
                ItemStack ammo = player.getProjectile(gun);
                if (!ammo.isEmpty() || player.getAbilities().instabuild) {
                    if (!world.isClientSide) {
                        if (ammo.isEmpty()) {
                            ammo = new ItemStack((ItemLike) CompatBOMD.skulls.get());
                        }
                        IBullet parentBullet = (IBullet)(ammo.getItem() instanceof IBullet ? ammo.getItem() : (Item)CompatBOMD.skulls.get());
                        ItemStack firedAmmo = ammo;
                        IBullet firedBullet = parentBullet;
                        if (parentBullet.hasDelegate(ammo, player)) {
                            firedAmmo = parentBullet.getDelegate(ammo, player);
                            firedBullet = (IBullet)(firedAmmo.getItem() instanceof IBullet ? firedAmmo.getItem() : (Item)CompatBOMD.skulls.get());
                        }

                        boolean bulletFree = player.getAbilities().instabuild || !this.shouldConsumeAmmo(gun, player);
                        if (!(firedAmmo.getItem() instanceof IBullet)) {
                            firedAmmo = new ItemStack((ItemLike) CompatBOMD.skulls.get());
                        }

                        this.shoot(world, player, gun, firedAmmo, firedBullet, bulletFree);
                        gun.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(player.getUsedItemHand()));
                        if (!bulletFree) {
                            parentBullet.consume(ammo, player);
                        }
                    }

                    world.playSound((Player)null, player.getX(), player.getY(), player.getZ(), this.getFireSound(), SoundSource.PLAYERS, 1.0F, world.getRandom().nextFloat() * 0.4F + 0.8F);
                    player.awardStat(Stats.ITEM_USED.get(this));
                }
            }
        }
    }

    @Override
    public int getFireDelay(ItemStack stack, @Nullable LivingEntity shooter) {
        int baseDelay = super.getFireDelay(stack, shooter);
        int consecutiveTime = stack.getOrCreateTag().getInt(CONSECUTIVE_TIME_KEY);
        
        // 重新调整计算公式，使其与连续射击计数上限同步
        // 当consecutiveTime达到MAX_CONSECUTIVE_TIME(100)时，射速减少正好达到MAX_SPEED_REDUCTION(0.5)
        double reductionFactor = (consecutiveTime / (double)MAX_CONSECUTIVE_TIME) * MAX_SPEED_REDUCTION;
        return Math.max(1, (int)(baseDelay * (1.0 - reductionFactor)));
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
}
