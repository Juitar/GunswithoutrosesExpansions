package juitar.gwrexpansions.item.BOMD;

import juitar.gwrexpansions.advancement.BOMD.AvadaKedavraTrigger;
import juitar.gwrexpansions.client.render.ObsidianLauncherHudRenderer;
import juitar.gwrexpansions.config.GWREConfig;
import juitar.gwrexpansions.entity.BOMD.ObsidianCoreEntity;
import juitar.gwrexpansions.item.ConfigurableLauncherItem;
import juitar.gwrexpansions.registry.CompatBOMD;
import juitar.gwrexpansions.registry.GWREEntities;
import juitar.gwrexpansions.registry.GWRESounds;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import lykrast.gunswithoutroses.item.IBullet;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

public class ObsidianLauncher extends ConfigurableLauncherItem {
    // 基础射程范围（方块）
    private static final int BASE_MIN_RANGE = 15; // 最小射程
    private static final int BASE_MAX_RANGE = 30; // 基础最大射程
    private static final int MAX_RANGE_WITH_FULL_CHARGE = 45; // 满充能最大射程
    private static final int MAX_CHARGE_TIME = 40; // 最大充能时间（刻）
    
    // NBT标签键
    private static final String NBT_SPELLS_CAST = "SpellsCast";
    private static final String NBT_FIRE_SPELL_CAST = "FireSpellCast";
    private static final String NBT_FROST_SPELL_CAST = "FrostSpellCast";
    private static final String NBT_HOLY_SPELL_CAST = "HolySpellCast";
    
    /**
     * 创建黑曜石发射器 
     *
     * @param properties       物品属性
     * @param bonusDamage      额外伤害（会被配置覆盖）
     * @param damageMultiplier 伤害倍率（会被配置覆盖）
     * @param fireDelay        射击延迟（会被配置覆盖）
     * @param inaccuracy       不精确度（会被配置覆盖）
     * @param enchantability   附魔能力
     * @param configSupplier   配置供应器
     */
    public ObsidianLauncher(Properties properties, int bonusDamage, double damageMultiplier, int fireDelay, double inaccuracy, int enchantability, Supplier<GWREConfig.GunConfig> configSupplier) {
        super(properties, bonusDamage, damageMultiplier, fireDelay, inaccuracy, enchantability, configSupplier);
    }
    
    /**
     * 查找玩家背包中的黑曜石核心弹药
     * @param player 玩家
     * @return 弹药物品栈，如果没有则返回空物品栈
     */
    @Override
    protected ItemStack findAmmo(Player player) {
        // 检查玩家背包中是否有obsidian_core弹药
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.isEmpty() && stack.getItem().equals(CompatBOMD.ObsidianCore.get())) {
                return stack;
            }
        }
        
        // 检查副手
        if (!player.getOffhandItem().isEmpty() && player.getOffhandItem().getItem().equals(CompatBOMD.ObsidianCore.get())) {
            return player.getOffhandItem();
        }
        
        return ItemStack.EMPTY;
    }
    
    /**
     * 根据充能时间计算当前射程
     * @param chargeTime 充能时间（刻）
     * @return 当前射程（方块）
     */
    public int getRangeForCharge(int chargeTime) {
        // 保证最小射程
        if (chargeTime <= 0) {
            return BASE_MIN_RANGE;
        }
        
        // 计算充能百分比，最大为1.0
        float chargePercent = Math.min(1.0f, (float) chargeTime / MAX_CHARGE_TIME);
        
        // 计算射程增益（从最小射程到满充能最大射程的差值）
        int rangeBonus = (int)(chargePercent * (MAX_RANGE_WITH_FULL_CHARGE - BASE_MIN_RANGE));
        
        // 返回最终射程（最小射程 + 射程增益）
        return BASE_MIN_RANGE + rangeBonus;
    }
    
    /**
     * 添加物品提示信息
     */
    @Override
    protected void addExtraStatsTooltip(ItemStack stack, @Nullable Level world, List<Component> tooltip) {
        MutableComponent minRange = Component.literal(Integer.toString(BASE_MIN_RANGE)).withStyle(ChatFormatting.WHITE);
        MutableComponent maxRange = Component.literal(Integer.toString(MAX_RANGE_WITH_FULL_CHARGE)).withStyle(ChatFormatting.WHITE);
        tooltip.add(Component.translatable("item.gwrexpansions.obsidian_launcher.tooltip.range").withStyle(ChatFormatting.DARK_GREEN));
        tooltip.add(Component.translatable("item.gwrexpansions.obsidian_launcher.tooltip.range.values", minRange,maxRange)
                .withStyle(ChatFormatting.DARK_GREEN));
        tooltip.add(Component.translatable("item.gwrexpansions.obsidian_launcher.tooltip.charge_info")
                .withStyle(ChatFormatting.GRAY));
        
        // 显示法术释放统计
        CompoundTag tag = stack.getOrCreateTag();
        boolean fireCast = tag.getBoolean(NBT_FIRE_SPELL_CAST);
        boolean frostCast = tag.getBoolean(NBT_FROST_SPELL_CAST);
        boolean holyCast = tag.getBoolean(NBT_HOLY_SPELL_CAST);
        
        int spellsCast = 0;
        if (fireCast) spellsCast++;
        if (frostCast) spellsCast++;
        if (holyCast) spellsCast++;
        
        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatable("item.gwrexpansions.obsidian_launcher.tooltip.spells_cast", spellsCast, 3)
                .withStyle(ChatFormatting.YELLOW));
        
        if (fireCast) {
            tooltip.add(Component.translatable("item.gwrexpansions.obsidian_launcher.tooltip.fire_spell").withStyle(ChatFormatting.RED));
        }
        if (frostCast) {
            tooltip.add(Component.translatable("item.gwrexpansions.obsidian_launcher.tooltip.frost_spell").withStyle(ChatFormatting.AQUA));
        }
        if (holyCast) {
            tooltip.add(Component.translatable("item.gwrexpansions.obsidian_launcher.tooltip.holy_spell").withStyle(ChatFormatting.YELLOW));
        }
    }
    
    @Override
    protected void shoot(Level level, Player player, ItemStack gun, ItemStack ammo, IBullet bulletItem, boolean bulletFree) {
        // 随机生成法术类型
        ObsidianCoreEntity.SpellType[] spellTypes = ObsidianCoreEntity.SpellType.values();
        ObsidianCoreEntity.SpellType spellType = spellTypes[level.getRandom().nextInt(spellTypes.length)];
        
        // 记录法术释放
        recordSpellCast(gun, spellType);
        
        // 检查是否释放了三种不同的法术
        checkAndTriggerAvadaKedavra(player, gun);
        
        // 创建并发射黑曜石核心实体，传入随机法术类型
        ObsidianCoreEntity coreEntity = new ObsidianCoreEntity(GWREEntities.OBSIDIAN_CORE.get(), level, player, spellType);
        
        // 获取蓄力时间
        int useTicks = gun.getOrCreateTag().getInt("UseTicks");
        
        // 计算蓄力加成
        float chargeBonus = calculateChargeBonus(useTicks);
        
        // 设置伤害，包含蓄力加成
        double damage = getBonusDamage(gun, player) * getDamageMultiplier(gun, player) * chargeBonus;
        coreEntity.setBaseDamage(damage);
        
        // 设置AOE范围乘数
        float aoeMultiplier = 1.0f + chargeBonus * 0.5f; // 1.0-1.5倍AOE范围
        coreEntity.setAOERadiusMultiplier(aoeMultiplier);
        
        // 计算并设置最大射程
        int maxRange = getRangeForCharge(useTicks);
        coreEntity.setMaxRange(maxRange);
        
        // 调整发射位置，使其更靠近玩家前方，避免近距离穿透
        // 获取玩家视线方向
        double pitch = Math.toRadians(player.getXRot());
        double yaw = Math.toRadians(player.getYRot());
        double cosYaw = Math.cos(-yaw - Math.PI);
        double sinYaw = Math.sin(-yaw - Math.PI);
        double cosPitch = -Math.cos(-pitch);
        double sinPitch = Math.sin(-pitch);
        
        // 计算发射偏移量，使核心从玩家眼睛位置前方0.5格处发射
        double offsetX = sinYaw * cosPitch * 0.5;
        double offsetY = sinPitch * 0.5;
        double offsetZ = cosYaw * cosPitch * 0.5;
        
        // 设置实体位置
        coreEntity.setPos(
            player.getX() + offsetX,
            player.getEyeY() - 0.1 + offsetY, // 稍微降低一点，避免卡在方块中
            player.getZ() + offsetZ
        );
        
        // 设置发射方向和速度，蓄力也会提高速度
        float speedMultiplier = 1.0f; // 1.0-1.5倍速度
        coreEntity.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 
                                    (float)getProjectileSpeed(gun, player) * speedMultiplier, 
                                    0.0F); // 移除偏移，使发射更加精确

        // 添加实体到世界
        level.addFreshEntity(coreEntity);
        
        // 武器损耗
        gun.hurtAndBreak(1, player, (p) -> p.broadcastBreakEvent(player.getUsedItemHand()));
    }
    
    /**
     * 记录法术释放
     */
    private void recordSpellCast(ItemStack gun, ObsidianCoreEntity.SpellType spellType) {
        CompoundTag tag = gun.getOrCreateTag();
        
        switch (spellType) {
            case FIRE:
                tag.putBoolean(NBT_FIRE_SPELL_CAST, true);
                break;
            case FROST:
                tag.putBoolean(NBT_FROST_SPELL_CAST, true);
                break;
            case HOLY:
                tag.putBoolean(NBT_HOLY_SPELL_CAST, true);
                break;
        }
    }
    
    /**
     * 检查并触发AvadaKedavra成就
     */
    private void checkAndTriggerAvadaKedavra(Player player, ItemStack gun) {
        if (!(player instanceof net.minecraft.server.level.ServerPlayer serverPlayer)) {
            return;
        }
        
        CompoundTag tag = gun.getOrCreateTag();
        boolean fireCast = tag.getBoolean(NBT_FIRE_SPELL_CAST);
        boolean frostCast = tag.getBoolean(NBT_FROST_SPELL_CAST);
        boolean holyCast = tag.getBoolean(NBT_HOLY_SPELL_CAST);
        
        // 如果三种法术都释放过了，触发成就
        if (fireCast && frostCast && holyCast) {
            AvadaKedavraTrigger.onThreeSpellsCast(serverPlayer);
        }
    }
    
    /**
     * 重写onUseTick方法，在蓄力时播放obsidian_pull音效
     */
    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        if (!level.isClientSide) {
            // 计算已使用时间
            int useTicks = getUseDuration(stack) - remainingUseDuration;
            
            // 存储蓄力时间
            CompoundTag tag = stack.getOrCreateTag();
            tag.putInt("UseTicks", Math.min(useTicks, MAX_CHARGE_TIME));
            
            // 每10刻播放一次蓄力音效
            if (useTicks % 20 == 0 && useTicks >= 0) {
                level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), 
                      GWRESounds.OBSIDIAN_PULL.get(), SoundSource.PLAYERS, 
                      0.5F, 1.0F);
            }
        }
    }
    
    /**
     * 计算蓄力加成
     * @param chargeTicks 蓄力时间
     * @return 蓄力加成系数 (0.5-1.5)
     */
    @Override
    protected float calculateChargeBonus(int chargeTicks) {
        return 0.5F + Math.min(1.0F, (float)chargeTicks / MAX_CHARGE_TIME);
    }
}
