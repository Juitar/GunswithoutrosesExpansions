package juitar.gwrexpansions.entity;


import juitar.gwrexpansions.GWRexpansions;
import juitar.gwrexpansions.registry.GWREEntities;
import juitar.gwrexpansions.registry.GWRESounds;
import juitar.gwrexpansions.item.vanilla.Supershotgun;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MeatHookEntity extends AbstractArrow {
    private static final Logger LOGGER = LoggerFactory.getLogger(MeatHookEntity.class);
    private static final EntityDataAccessor<Integer> HOOKED_ENTITY_ID = SynchedEntityData.defineId(MeatHookEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> PULLING = SynchedEntityData.defineId(MeatHookEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> OWNER_ID = SynchedEntityData.defineId(MeatHookEntity.class, EntityDataSerializers.INT);
    private static final double PULL_SPEED = 1.2D; // 拉力强度
    private static final int MAX_PULL_TIME = 60; // 最大拉动时间，防止无限拉动
    private static final double MIN_DISTANCE_TO_DISCARD = 2.5D; // 玩家与目标的最小距离，低于此距离时肉钩消失
    private static final int MAX_LIFETIME = 80; // 肉钩最长存在时间
    private static final int MAX_PULL_DISTANCE = 32;

    private Entity hookedEntity;
    private int pullTime = 0;
    private boolean DEBUG = false; // 调试开关
    private boolean hitEntity = false; // 是否击中实体的标记
    private boolean cooldownSet = false; // 是否已设置冷却时间
    
    public MeatHookEntity(EntityType<? extends MeatHookEntity> type, Level level) {
        super(type, level);
        this.pickup = Pickup.DISALLOWED;
        this.setNoGravity(true);
        this.setBaseDamage(0.0); // 设置基础伤害为0
        if (DEBUG) {
            LOGGER.info("创建肉钩实体: {}", this.getId());
        }
    }
    
    public MeatHookEntity(Level level, LivingEntity shooter) {
        super(GWREEntities.MEAT_HOOK.get(), shooter, level);
        this.pickup = Pickup.DISALLOWED;
        this.setNoGravity(true);
        this.setBaseDamage(0.0); // 设置基础伤害为0
        
        // 设置所有者ID
        if (shooter != null) {
            this.entityData.set(OWNER_ID, shooter.getId());
        }
        
        if (DEBUG) {
            LOGGER.info("从射击者创建肉钩: {}, 射击者: {}", this.getId(), shooter.getName().getString());
        }
        
        if (DEBUG && shooter instanceof Player) {
            ((Player)shooter).displayClientMessage(Component.literal("肉钩已发射"), true);
        }
        
        // 播放发射声音
        level.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(), 
            GWRESounds.meat_hook_launch.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
    }
    
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(HOOKED_ENTITY_ID, -1);
        this.entityData.define(PULLING, false);
        this.entityData.define(OWNER_ID, -1);
    }
    
    @Override
    public ItemStack getPickupItem() {
        // 肉钩不可拾取，返回空物品
        return ItemStack.EMPTY;
    }
    
    @Override
    public Entity getOwner() {
        // 先尝试使用父类方法获取所有者
        Entity owner = super.getOwner();
        
        // 如果父类方法返回null，则尝试从同步数据中获取
        if (owner == null && this.entityData.get(OWNER_ID) > 0) {
            return this.level().getEntity(this.entityData.get(OWNER_ID));
        }
        
        return owner;
    }
    
    @Override
    public void setOwner(Entity entity) {
        super.setOwner(entity);
        
        // 同时更新同步数据
        if (entity != null) {
            this.entityData.set(OWNER_ID, entity.getId());
        } else {
            this.entityData.set(OWNER_ID, -1);
        }
    }
    
    @Override
    public void tick() {
        // 记录初始状态，用于调试
        boolean hadHookedEntity = this.hookedEntity != null;
        boolean wasPulling = this.entityData.get(PULLING);
        
        super.tick();
        
        // 获取发射者
        Entity shooter = this.getOwner();
        
        // 如果没有发射者，则移除实体
        if (shooter == null) {
            if (DEBUG) {
                LOGGER.info("肉钩移除：没有发射者");
            }
            this.discard();
            return;
        }
        

        
        // 尝试从实体ID同步钩中的实体
        if (this.hookedEntity == null && this.entityData.get(HOOKED_ENTITY_ID) > 0) {
            Entity entity = this.level().getEntity(this.entityData.get(HOOKED_ENTITY_ID));
            if (entity != null && entity.isAlive()) {
                this.hookedEntity = entity;
                if (DEBUG) {
                    LOGGER.info("从ID同步到钩中实体：{}", entity.getName().getString());
                }
                if (DEBUG && shooter instanceof Player) {
                    ((Player)shooter).displayClientMessage(Component.literal("从ID同步到钩中实体：" + entity.getName().getString()), true);
                }
            } else {
                if (DEBUG) {
                    LOGGER.warn("无法从ID获取实体：{}", this.entityData.get(HOOKED_ENTITY_ID));
                }
                if (DEBUG && shooter instanceof Player) {
                    ((Player)shooter).displayClientMessage(Component.literal("无法从ID获取实体：" + this.entityData.get(HOOKED_ENTITY_ID)), true);
                }
                this.entityData.set(HOOKED_ENTITY_ID, -1);
            }
        }
        
        // 如果有钩中的实体
        if (this.hookedEntity != null) {
            // 如果实体无效，则移除
            if (!this.hookedEntity.isAlive()) {
                if (DEBUG) {
                    LOGGER.info("目标实体已消失");
                }
                if (DEBUG && shooter instanceof Player) {
                    ((Player)shooter).displayClientMessage(Component.literal("目标实体已消失"), true);
                }
                this.setHookedEntity(null);
                this.discard();
                return;
            }
            
            // 固定肉钩位置到目标实体
            this.setPos(this.hookedEntity.position().x, this.hookedEntity.position().y + this.hookedEntity.getEyeHeight() * 0.5, this.hookedEntity.position().z);
            this.setDeltaMovement(Vec3.ZERO);
            
            // 执行拉动逻辑
            if (shooter instanceof Player player) {
                pullTime++;
                
                // 计算拉动向量 - 从玩家到目标的方向
                Vec3 pullVec = this.hookedEntity.position().subtract(player.position()).normalize();
                
                // 应用拉力 - 直接设置玩家的移动速度
                double speedX = pullVec.x * PULL_SPEED;
                double speedY = pullVec.y * PULL_SPEED + 0.1; // 稍微向上一点，防止卡地面
                double speedZ = pullVec.z * PULL_SPEED;
                
                player.setDeltaMovement(speedX, speedY, speedZ);
                player.hurtMarked = true; // 标记实体需要更新移动
                player.fallDistance = 0.0F; // 防止坠落伤害
                
                // 如果是拉动开始，播放拉动声音
                if (pullTime == 1) {
                    if (DEBUG) {
                        LOGGER.info("开始拉动玩家");
                    }
                    if (DEBUG) {
                        player.displayClientMessage(Component.literal("开始拉动玩家"), true);
                    }
                    this.level().playSound(null, player.getX(), player.getY(), player.getZ(), 
                        GWRESounds.meat_hook_pull.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
                }
                
                // 每隔一段时间播放拉动声音，创造持续的声音效果
                if (pullTime % 20 == 0) {
                    // 播放拉动声音，音量随距离变化
                    float distance = player.distanceTo(this.hookedEntity);
                    float pitch = 1.0F + (distance / 20.0F); // 距离越远，音调越高
                    this.level().playSound(null, player.getX(), player.getY(), player.getZ(), 
                        GWRESounds.meat_hook_pull.get(), SoundSource.PLAYERS, 0.6F, pitch);
                }
                
                if (DEBUG && pullTime % 10 == 0) {
                    LOGGER.info("拉动中：{}ticks，速度：{},{},{}", pullTime, speedX, speedY, speedZ);
                    player.displayClientMessage(Component.literal("拉动中：" + pullTime + "ticks，速度：" + 
                        String.format("%.2f, %.2f, %.2f", speedX, speedY, speedZ)), true);
                }
                
                // 只有当玩家非常接近目标或拉动时间过长时才停止拉动
                if (player.distanceTo(this.hookedEntity) < MIN_DISTANCE_TO_DISCARD || pullTime >= MAX_PULL_TIME) {
                    if (DEBUG) {
                        String reason = player.distanceTo(this.hookedEntity) < MIN_DISTANCE_TO_DISCARD ? 
                            "玩家已接近目标" : "拉动时间过长";
                        LOGGER.info("停止拉动：{}", reason);
                        player.displayClientMessage(Component.literal("停止拉动：" + reason), true);
                    }
                    
                    // 在消失前设置冷却时间
                    if (!cooldownSet) {
                        notifyOwnerForCooldown();
                        cooldownSet = true;
                    }
                    
                    this.discard();
                }
            }
        } else if (this.inGround) {
            // 如果击中地面但没有钩中实体，也移除
            if (DEBUG) {
                LOGGER.info("肉钩移除：已碰到地面");
            }
            if (DEBUG && shooter instanceof Player) {
                ((Player)shooter).displayClientMessage(Component.literal("肉钩移除：已碰到地面"), true);
            }
            
            // 在消失前设置冷却时间
            if (!cooldownSet) {
                notifyOwnerForCooldown();
                cooldownSet = true;
            }
            
            this.discard();
            return;
        }
        
        // 调试：状态变化检测
        if (DEBUG && shooter instanceof Player) {
            boolean hasHookedEntityNow = this.hookedEntity != null;
            boolean isPullingNow = this.entityData.get(PULLING);
            
            // 如果状态发生变化，显示消息
            if (hadHookedEntity != hasHookedEntityNow) {
                LOGGER.info("钩中状态变化：{} -> {}", hadHookedEntity, hasHookedEntityNow);
                ((Player)shooter).displayClientMessage(Component.literal("钩中状态变化：" + hadHookedEntity + " -> " + hasHookedEntityNow), true);
            }
            if (wasPulling != isPullingNow) {
                LOGGER.info("拉动状态变化：{} -> {}", wasPulling, isPullingNow);
                ((Player)shooter).displayClientMessage(Component.literal("拉动状态变化：" + wasPulling + " -> " + isPullingNow), true);
            }
        }
        
        // 存在时间超过MAX_LIFETIME或距离发射者超过100格，则移除
        if (this.tickCount > MAX_LIFETIME || (shooter != null && this.distanceTo(shooter) > MAX_PULL_DISTANCE)) {
            if (DEBUG) {
                String reason = this.tickCount > MAX_LIFETIME ? "存在时间过长" : "距离发射者过远";
                LOGGER.info("肉钩移除：{}", reason);
            }
            if (DEBUG && shooter instanceof Player) {
                String reason = this.tickCount > MAX_LIFETIME ? "存在时间过长" : "距离发射者过远";
                ((Player)shooter).displayClientMessage(Component.literal("肉钩移除：" + reason), true);
            }
            
            // 在消失前设置冷却时间
            if (!cooldownSet) {
                notifyOwnerForCooldown();
                cooldownSet = true;
            }
            
            this.discard();
        }
    }
    
    @Override
    protected void onHitEntity(EntityHitResult result) {
        // 不调用父类方法，防止造成伤害
        // super.onHitEntity(result);
        
        if (DEBUG) {
            LOGGER.info("肉钩击中实体: {}", result.getEntity().getName().getString());
        }
        
        // 只能钩住生物
        if (!(result.getEntity() instanceof LivingEntity) || result.getEntity() == this.getOwner()) {
            if (DEBUG) {
                String reason = !(result.getEntity() instanceof LivingEntity) ? "非生物实体" : "是发射者自己";
                LOGGER.info("不能钩住目标：{}", reason);
            }
            if (DEBUG && this.getOwner() instanceof Player) {
                String reason = !(result.getEntity() instanceof LivingEntity) ? "非生物实体" : "是发射者自己";
                ((Player)this.getOwner()).displayClientMessage(Component.literal("不能钩住目标：" + reason), true);
            }
            return;
        }
        
        // 设置击中实体标记
        this.hitEntity = true;
        
        // 直接设置目标实体为钩中的实体
        this.hookedEntity = result.getEntity();
        this.entityData.set(HOOKED_ENTITY_ID, result.getEntity().getId());
        this.entityData.set(PULLING, true);
        
        // 播放击中声音
        this.level().playSound(null, result.getEntity().getX(), result.getEntity().getY(), result.getEntity().getZ(), 
            GWRESounds.meat_hook_hit.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        
        // 停止移动
        this.setDeltaMovement(Vec3.ZERO);
        
        if (DEBUG) {
            LOGGER.info("肉钩已钩住实体: {}", result.getEntity().getName().getString());
        }
        if (DEBUG && this.getOwner() instanceof Player) {
            ((Player)this.getOwner()).displayClientMessage(Component.literal("肉钩已钩住实体: " + result.getEntity().getName().getString()), true);
        }
    }
    
    @Override
    protected void onHitBlock(BlockHitResult hitResult) {
        // 设置未击中实体标记
        this.hitEntity = false;
        
        // 播放击中声音
        this.level().playSound(null, hitResult.getBlockPos().getX(), hitResult.getBlockPos().getY(), hitResult.getBlockPos().getZ(),
                GWRESounds.meat_hook_miss.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
        if (DEBUG) {
            LOGGER.info("肉钩击中方块: {}", hitResult.getBlockPos());
        }
        if (DEBUG && this.getOwner() instanceof Player) {
            ((Player)this.getOwner()).displayClientMessage(Component.literal("肉钩击中方块"), true);
        }
    }
    
    // 覆盖此方法以防止造成伤害
    @Override
    public boolean isAttackable() {
        return false;
    }
    
    // 覆盖此方法以防止造成伤害
    @Override
    public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float amount) {
        return false;
    }
    
    private void setHookedEntity(Entity entity) {
        this.hookedEntity = entity;
        this.entityData.set(HOOKED_ENTITY_ID, entity == null ? -1 : entity.getId());
        this.entityData.set(PULLING, entity != null);
        
        if (DEBUG) {
            LOGGER.info("设置钩中实体：{}, ID: {}, 拉动: {}", 
                (entity == null ? "null" : entity.getName().getString()),
                this.entityData.get(HOOKED_ENTITY_ID),
                this.entityData.get(PULLING));
        }
        
        if (DEBUG && this.getOwner() instanceof Player) {
            ((Player)this.getOwner()).displayClientMessage(
                Component.literal("设置钩中实体：" + (entity == null ? "null" : entity.getName().getString()) + 
                ", ID:" + this.entityData.get(HOOKED_ENTITY_ID) + 
                ", 拉动:" + this.entityData.get(PULLING)), true);
        }
    }
    
    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        
        // 读取数据
        if (tag.contains("HookedEntity")) {
            this.entityData.set(HOOKED_ENTITY_ID, tag.getInt("HookedEntity"));
        }
        
        if (tag.contains("Pulling")) {
            this.entityData.set(PULLING, tag.getBoolean("Pulling"));
        }
        
        if (tag.contains("OwnerId")) {
            this.entityData.set(OWNER_ID, tag.getInt("OwnerId"));
        }
    }
    
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        
        // 保存数据
        tag.putInt("HookedEntity", this.entityData.get(HOOKED_ENTITY_ID));
        tag.putBoolean("Pulling", this.entityData.get(PULLING));
        tag.putInt("OwnerId", this.entityData.get(OWNER_ID));
    }
    
    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
    
    @Override
    protected float getWaterInertia() {
        return 0.9F;
    }
    
    // 检查玩家是否正在使用肉钩（通过霰弹枪）
    private boolean isPlayerUsingHook(Player player) {
        // 检查玩家是否在潜行并使用物品
        return player.isUsingItem() && player.isCrouching();
    }
    
    // 通知所有者设置冷却时间
    private void notifyOwnerForCooldown() {
        Entity owner = this.getOwner();
        if (owner instanceof Player player) {
            // 检查主手和副手的物品
            ItemStack mainHandItem = player.getMainHandItem();
            ItemStack offHandItem = player.getOffhandItem();
            
            // 如果未击中实体，减少冷却时间
            if (!hitEntity) {
                // 检查主手
                if (mainHandItem.getItem() instanceof Supershotgun supershotgun) {
                    supershotgun.reduceHalfCooldown(mainHandItem);
                }
                
                // 检查副手
                if (offHandItem.getItem() instanceof Supershotgun supershotgun) {
                    supershotgun.reduceHalfCooldown(offHandItem);
                }
                
                if (DEBUG) {
                    LOGGER.info("肉钩未击中实体，减少冷却时间并恢复计算");
                    player.displayClientMessage(Component.literal("肉钩未击中实体，减少冷却时间并恢复计算"), true);
                }
            } else {
                // 如果击中了实体，只恢复冷却计算，不减少冷却时间
                // 检查主手
                if (mainHandItem.getItem() instanceof Supershotgun supershotgun) {
                    supershotgun.resumeCooldown(mainHandItem);
                }
                
                // 检查副手
                if (offHandItem.getItem() instanceof Supershotgun supershotgun) {
                    supershotgun.resumeCooldown(offHandItem);
                }
                
                if (DEBUG) {
                    LOGGER.info("肉钩击中实体，恢复冷却计算");
                    player.displayClientMessage(Component.literal("肉钩击中实体，恢复冷却计算"), true);
                }
            }
            
            // 检查玩家背包中的其他霰弹枪
            boolean foundInHands = mainHandItem.getItem() instanceof Supershotgun || 
                                   offHandItem.getItem() instanceof Supershotgun;
            
            if (!foundInHands) {
                // 遍历玩家的物品栏
                for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                    ItemStack stack = player.getInventory().getItem(i);
                    if (stack.getItem() instanceof Supershotgun supershotgun) {
                        if (!hitEntity) {
                            supershotgun.reduceHalfCooldown(stack);
                        } else {
                            supershotgun.resumeCooldown(stack);
                        }
                        break; // 找到一把霰弹枪就足够了
                    }
                }
            }
        }
    }
} 