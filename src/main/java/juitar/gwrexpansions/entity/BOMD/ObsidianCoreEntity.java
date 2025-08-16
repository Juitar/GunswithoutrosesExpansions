package juitar.gwrexpansions.entity.BOMD;

import juitar.gwrexpansions.registry.CompatBOMD;
import juitar.gwrexpansions.registry.GWREItems;
import juitar.gwrexpansions.registry.GWRESounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.MobType;

import java.util.List;
import java.util.Random;

public class ObsidianCoreEntity extends AbstractArrow {
    private static final EntityDataAccessor<Boolean> RETURNING = SynchedEntityData.defineId(ObsidianCoreEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> OWNER_ID = SynchedEntityData.defineId(ObsidianCoreEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> HIT_ENTITY_ID = SynchedEntityData.defineId(ObsidianCoreEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> AOE_RADIUS_MULTIPLIER = SynchedEntityData.defineId(ObsidianCoreEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> MAX_RANGE = SynchedEntityData.defineId(ObsidianCoreEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> SPELL_TYPE = SynchedEntityData.defineId(ObsidianCoreEntity.class, EntityDataSerializers.INT);
    private static final double RETURN_SPEED = 1.5D; // 回归速度
    private static final float AOE_RADIUS = 1.0f; // AOE范围
    private static final float AOE_DAMAGE_FACTOR = 1.0F; // AOE伤害系数
    private static final float RETURN_DAMAGE_FACTOR = 0.5F; // 回归伤害系数
    private static final int MAX_LIFETIME = 200; // 最大存在时间
    private static final float BASE_MAX_RANGE = 30.0f; // 基础最大射程，单位：方块
    private static final int BASE_MIN_RANGE = 15; // 最小射程
    private boolean hasHit = false; // 是否已击中目标
    private boolean hasDealtAOE = false; // 是否已造成AOE伤害
    private Vec3 startPos; // 发射起始位置
    
    /**
     * 施法属性枚举
     */
    public enum SpellType {
        FIRE(0xFF0000),   // 火焰 (红色)
        FROST(0x00AAFF),  // 冰霜 (蓝色)
        HOLY(0xFFFF00);   // 神圣 (黄色)
        
        private final int color;
        
        SpellType(int color) {
            this.color = color;
        }
        
        public int getColor() {
            return color;
        }
    }
    
    public ObsidianCoreEntity(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
        this.pickup = Pickup.DISALLOWED; // 不可拾取
        this.setNoGravity(true); // 无重力
    }
    
    public ObsidianCoreEntity(EntityType<? extends ObsidianCoreEntity> entityType, Level level, LivingEntity shooter) {
        super(entityType, shooter, level);
        this.setOwner(shooter);
        this.pickup = Pickup.DISALLOWED; // 不可拾取
        
        // 保存发射起始位置，用于计算飞行距离
        this.startPos = shooter.position().add(0, shooter.getEyeHeight(), 0);
        
        // 设置基础属性
        this.setBaseDamage(5.0D); // 基础伤害
        this.setPierceLevel((byte) 0); // 不穿透
        this.setNoGravity(true); // 无重力，由自定义物理逻辑控制
        
        // 设置所有者ID
        if (shooter != null) {
            this.entityData.set(OWNER_ID, shooter.getId());
        }
        
        // 设置碰撞箱大小为1.5倍
        this.setBoundingBox(new AABB(-0.375D, -0.375D, -0.375D, 0.375D, 0.375D, 0.375D));
    }
    
    /**
     * 创建带有指定法术类型的黑曜石核心实体
     */
    public ObsidianCoreEntity(EntityType<? extends ObsidianCoreEntity> entityType, Level level, LivingEntity shooter, SpellType spellType) {
        this(entityType, level, shooter);
        
        // 设置指定的法术类型
        this.setSpellType(spellType);
    }
    
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(RETURNING, false);
        this.entityData.define(OWNER_ID, -1);
        this.entityData.define(HIT_ENTITY_ID, -1);
        this.entityData.define(AOE_RADIUS_MULTIPLIER, 1.0f); // 默认AOE范围乘数为1.0
        this.entityData.define(MAX_RANGE, BASE_MAX_RANGE); // 默认最大射程
        this.entityData.define(SPELL_TYPE, SpellType.FIRE.ordinal()); // 默认为火焰属性
    }
    
    @Override
    public void tick() {
        // 保存上一次的位置，用于检测碰撞
        Vec3 prevPos = this.position();
        
        // 调用父类tick处理基础逻辑，但不使用其碰撞逻辑
        // 我们将使用自定义的碰撞检测
        Entity owner = this.getOwner();
        if (owner != null && !owner.isAlive() && !this.level().isClientSide) {
            this.discard();
            return;
        }

        super.baseTick(); // 只调用基础tick，不使用AbstractArrow的tick

        // 确保碰撞箱大小保持为1.5倍
        if (this.getBoundingBox().getXsize() != 0.75D) {
            this.setBoundingBox(new AABB(-0.375D, -0.375D, -0.375D, 0.375D, 0.375D, 0.375D));
        }
        
        // 如果正在回归，使用自定义移动逻辑
        if (this.isReturning()) {
            this.handleReturn();
        } else {
            // 非回归状态下，应用自定义物理和检查碰撞
            this.customPhysicsTick();
            
            // 只有在非回归状态下才检测碰撞
            if (!this.hasHit) {
                this.checkCollisions();
            }
            
            // 检查是否超出最大射程
            if (!this.level().isClientSide && !this.isReturning() && !this.hasHit) {
                if (this.startPos != null) {
                    double distanceTraveled = this.position().distanceTo(this.startPos);
                    float maxRange = this.getMaxRange();
                    
                    // 确保最大射程不小于BASE_MIN_RANGE
                    if (maxRange < BASE_MIN_RANGE) {
                        maxRange = BASE_MIN_RANGE;
                    }
                    
                    if (distanceTraveled > maxRange) {
                        // 超出射程，设置为回归状态
                        this.setReturning(true);
                        this.playSound(SoundEvents.ARROW_HIT, 1.0F, 1.2F);
                    }
                }
            }
        }
        
        // 超时移除
        if (this.tickCount > MAX_LIFETIME) {
            this.discard();
        }
        
        // 生成轨迹粒子
        if (this.level().isClientSide && !this.isInWater()) {
            this.spawnTrailParticles();
        }
    }
    
    /**
     * 自定义物理逻辑，只应用垂直重力，不受液体影响水平速度
     */
    private void customPhysicsTick() {
        Vec3 movement = this.getDeltaMovement();
        
        // 应用垂直重力
        if (!this.isNoGravity()) {
            movement = movement.add(0.0D, -0.05D, 0.0D);
        }
        
        // 更新位置
        this.setPos(this.getX() + movement.x, this.getY() + movement.y, this.getZ() + movement.z);
        this.setDeltaMovement(movement);
        
        // 检查是否在方块内部
        this.checkInsideBlocks();
        
        // 更新旋转
        this.updateRotation();
    }
    
    /**
     * 检查碰撞并处理命中
     */
    private void checkCollisions() {
        Vec3 currentPos = this.position();
        Vec3 nextPos = currentPos.add(this.getDeltaMovement());
        
        // 首先检查实体碰撞，优先处理实体碰撞
        EntityHitResult entityHitResult = this.findHitEntity(currentPos, nextPos);
        if (entityHitResult != null) {
            Entity hitEntity = entityHitResult.getEntity();
            
            // 确保不会击中发射者
            if (hitEntity != this.getOwner()) {
                // 如果击中实体，直接处理实体碰撞
                this.onHitEntity(entityHitResult);
                return;
            }
        }
        
        // 如果没有击中实体，再检查方块碰撞
        HitResult hitResult = this.level().clip(new ClipContext(currentPos, nextPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult blockHitResult = (BlockHitResult) hitResult;
            this.onHitBlock(blockHitResult);
        }
    }
    
    /**
     * 查找命中的实体
     */
    @Nullable
    @Override
    protected EntityHitResult findHitEntity(Vec3 startPos, Vec3 endPos) {
        // 增大碰撞检测范围，确保近距离目标也能被检测到
        return ProjectileUtil.getEntityHitResult(
            this.level(), 
            this, 
            startPos, 
            endPos, 
            this.getBoundingBox().expandTowards(this.getDeltaMovement()).inflate(1.0D), 
            (entity) -> {
                // 不击中自身和所有者
                return entity != this && entity != this.getOwner() && entity.isPickable() && !entity.noPhysics;
            }
        );
    }
    
    @Override
    protected void updateRotation() {
        // 更新旋转，使实体朝向移动方向
        Vec3 movement = this.getDeltaMovement();
        double horizontalDistance = movement.horizontalDistance();
        
        // 只有当水平移动足够大时才更新旋转
        if (horizontalDistance > 0.001D) {
            this.setYRot((float)(Mth.atan2(movement.z, movement.x) * 180.0D / Math.PI) - 90.0F);
        }
        
        // 计算俯仰角
        if (horizontalDistance > 0.001D) {
            this.setXRot((float)(Mth.atan2(movement.y, horizontalDistance) * 180.0D / Math.PI));
        }
        
        // 应用旋转
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }
    
    @Override
    public boolean isNoPhysics() {
        // 禁用原版物理
        return true;
    }
    
    @Override
    protected float getWaterInertia() {
        // 在水中不减速
        return 1.0F;
    }
    
    private void handleReturn() {
        // 获取所有者
        Entity owner = this.getOwner();
        if (owner == null || !owner.isAlive()) {
            this.discard();
            return;
        }
        
        // 计算回归向量
        Vec3 ownerPos = owner.position().add(0, owner.getEyeHeight() * 0.8, 0);
        Vec3 currentPos = this.position();
        Vec3 returnVec = ownerPos.subtract(currentPos).normalize().scale(RETURN_SPEED);
        
        // 检查是否接近所有者
        double distanceToOwner = currentPos.distanceTo(ownerPos);
        
        // 如果距离小于1.75，则立即移除实体
        if (distanceToOwner < 1.75) {
            // 确保实体被立即移除
            this.discard();
            this.level().broadcastEntityEvent(this, (byte)3); // 生成粒子效果
            
            // 播放捡起音效
            this.level().playSound(null, owner.getX(), owner.getY(), owner.getZ(), 
                SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.2F, 
                ((this.random.nextFloat() - this.random.nextFloat()) * 0.7F + 1.0F) * 2.0F);
            
            return;
        }
        
        // 设置移动速度
        this.setDeltaMovement(returnVec);
        
        // 更新位置
        this.setPos(this.getX() + this.getDeltaMovement().x, 
                    this.getY() + this.getDeltaMovement().y, 
                    this.getZ() + this.getDeltaMovement().z);
        
        // 检查是否撞到实体
        this.checkReturnEntityCollision();
    }
    
    private void checkReturnEntityCollision() {
        // 获取碰撞箱附近的实体
        AABB boundingBox = this.getBoundingBox().inflate(0.2);
        List<Entity> entities = this.level().getEntities(this, boundingBox);
        
        for (Entity entity : entities) {
            // 忽略所有者和已经死亡的实体
            if (entity == this.getOwner() || !entity.isAlive() || entity == this) {
                continue;
            }
            
            // 对实体造成伤害
            if (entity instanceof LivingEntity livingEntity) {
                float damage = (float)(this.getBaseDamage() * RETURN_DAMAGE_FACTOR);
                entity.hurt(this.damageSources().arrow(this, this.getOwner() instanceof LivingEntity ? (LivingEntity)this.getOwner() : null), damage);
            }
        }
    }
    
    @Override
    protected void onHitEntity(EntityHitResult result) {
        // 不调用super方法，防止卡在实体中
        // super.onHitEntity(result);
        
        Entity hitEntity = result.getEntity();
        
        // 标记已击中
        this.hasHit = true;
        
        // 播放击中声音
        this.level().playSound(null, hitEntity.getX(), hitEntity.getY(), hitEntity.getZ(),
        GWRESounds.OBSIDIAN_CORE_HIT.get(), SoundSource.NEUTRAL, 1.0F, 1.0F);
        
        // 处理伤害
        if (hitEntity.isAttackable()) {
            Entity owner = this.getOwner();
            
            // 计算基础伤害
            float damage = (float)this.getBaseDamage();
            
            // 获取施法属性
            SpellType spellType = getSpellType();
            
            // 对直接击中的目标应用施法效果
            if (hitEntity instanceof LivingEntity livingTarget) {
                switch (spellType) {
                    case FIRE:
                        // 火焰伤害增强25%
                        damage *= 1.25f;
                        // 点燃目标，持续5-7秒
                        int fireDuration = 5 + this.random.nextInt(3);
                        livingTarget.setSecondsOnFire(fireDuration);
                        break;
                        
                    case FROST:
                        // 减速效果，50%减速，持续5秒
                        livingTarget.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1));
                        // 对火焰免疫实体额外伤害
                        if (livingTarget.fireImmune()) {
                            damage *= 1.5f;
                        }
                        break;
                        
                    case HOLY:
                        // 神圣伤害对亡灵生物更有效
                        if (livingTarget.getMobType().equals(MobType.UNDEAD)) {
                            damage *= 1.25f;
                        }
                        // 直接击中目标也给予伤害吸收效果
                        if (owner instanceof LivingEntity ownerLiving) {
                            // 给予1级伤害吸收效果，持续30秒
                            ownerLiving.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 600, 0));
                            // 播放神圣效果音效
                            this.level().playSound(null, ownerLiving.getX(), ownerLiving.getY(), ownerLiving.getZ(), 
                                SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.0F);
                        }
                        break;
                }
                
                // 应用伤害
                if (hitEntity.hurt(this.damageSources().arrow(this, owner instanceof LivingEntity ? (LivingEntity)owner : null), damage)) {
                    // 如果伤害成功应用
                    
                    // 击退效果
                    livingTarget.knockback(0.5F, this.getX() - livingTarget.getX(), this.getZ() - livingTarget.getZ());
                    
                    // 破盾效果：只对玩家生效，检查是否正在使用盾牌
                    if (livingTarget instanceof Player player && player.isBlocking()) {
                        // 禁用盾牌 - 参考斧头的破盾机制
                        // 使用getCooldowns().addCooldown方法禁用玩家当前使用的盾牌
                        // 获取玩家正在使用的物品（主手或副手）
                        ItemStack activeShield = player.getUseItem();
                        if (!activeShield.isEmpty() && activeShield.getItem() instanceof ShieldItem) {
                            // 添加5秒冷却（100 ticks）
                            player.getCooldowns().addCooldown(activeShield.getItem(), 100);
                        }
                        
                        // 播放盾牌禁用的声音和粒子效果
                        this.level().playSound(null, player.getX(), player.getY(), player.getZ(), 
                            SoundEvents.SHIELD_BREAK, SoundSource.PLAYERS, 0.8F, 0.8F + this.level().random.nextFloat() * 0.4F);
                    }
                }
            }
        }
        
        // 处理AOE伤害 - 只在未处理过AOE的情况下处理
        if (!this.hasDealtAOE) {
            // 在客户端生成粒子效果
            if (this.level().isClientSide) {
                this.spawnAOEParticles(hitEntity);
            }
            
            // 仅在服务端处理AOE伤害
            if (!this.level().isClientSide) {
                // 传递已击中实体，确保AOE伤害不重复应用于直接击中目标
                this.dealAOEDamage(hitEntity);
                this.hasDealtAOE = true;
            }
        }
        
        // 设置为回归状态 - 必须在AOE伤害处理后设置，否则会影响AOE逻辑
        this.setReturning(true);
        
        // 停止移动
        this.setDeltaMovement(Vec3.ZERO);
    }
    
    @Override
    protected void onHitBlock(BlockHitResult result) {
        // 不调用super方法，防止卡在方块中
        // super.onHitBlock(result);
        
        // 标记已击中
        this.hasHit = true;
        
        // 播放击中声音
        BlockPos hitPos = result.getBlockPos();
        this.level().playSound(null, hitPos, 
        GWRESounds.OBSIDIAN_CORE_MISS.get(), SoundSource.NEUTRAL, 1.0F, 1.0F);
        
        // 处理AOE伤害 - 只在未处理过AOE的情况下处理
        if (!this.hasDealtAOE) {
            // 在客户端生成粒子效果
            if (this.level().isClientSide) {
                this.spawnAOEParticles(hitPos);
            }
            
            // 仅在服务端处理AOE伤害
            if (!this.level().isClientSide) {
                this.dealAOEDamage(hitPos);
                this.hasDealtAOE = true;
            }
        }
        
        // 设置为回归状态 - 必须在AOE伤害处理后设置
        this.setReturning(true);
        
        // 停止移动
        this.setDeltaMovement(Vec3.ZERO);
    }
    
    private void dealAOEDamage(Entity hitEntity) {
        // 获取AOE范围内的所有实体
        float actualRadius = AOE_RADIUS * getAOERadiusMultiplier();
        List<Entity> entities = this.level().getEntities(this, 
            hitEntity.getBoundingBox().inflate(actualRadius), 
            entity -> entity != this && entity != hitEntity && entity != this.getOwner());
        
        // 获取施法属性
        SpellType spellType = getSpellType();
        
        // 计算神圣属性的伤害吸收累计等级
        int absorptionLevel = 0;
        
        // 对每个实体造成伤害
        for (Entity entity : entities) {
            if (entity instanceof LivingEntity livingTarget) {
                // 计算距离
                double distance = entity.distanceTo(hitEntity);
                
                // 计算伤害系数，从1.0衰减到0.5
                float damageFactor = AOE_DAMAGE_FACTOR;
                if (distance > 0) {
                    // 线性衰减：近处为100%，远处为50%
                    float distanceFactor = 1.0f - (float)(distance / actualRadius) * 0.5f;
                    damageFactor = AOE_DAMAGE_FACTOR * Math.max(0.5f, distanceFactor);
                }
                
                // 应用伤害
                float damage = (float)(this.getBaseDamage() * damageFactor);
                
                // 根据施法属性应用不同效果
                switch (spellType) {
                    case FIRE:
                        // 火焰伤害增强25%
                        damage *= 1.25f;
                        // 点燃目标，持续5-7秒
                        int fireDuration = 5 + this.random.nextInt(3);
                        livingTarget.setSecondsOnFire(fireDuration);
                        // 向上击飞
                        livingTarget.push(0, 0.5, 0);
                        break;
                        
                    case FROST:
                        // 减速效果，50%减速，持续5秒
                        livingTarget.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1));
                        // 对火焰免疫实体额外伤害
                        if (livingTarget.fireImmune()) {
                            damage *= 1.5f;
                        }
                        break;
                        
                    case HOLY:
                        // 神圣伤害对亡灵生物更有效
                        if (livingTarget.getMobType().equals(MobType.UNDEAD)) {
                            damage *= 1.25f;
                        }
                        // 每击中一个敌人累计1级伤害吸收等级
                        absorptionLevel++;
                        break;
                }
                
                // 应用最终伤害
                entity.hurt(this.damageSources().arrow(this, this.getOwner() instanceof LivingEntity ? (LivingEntity)this.getOwner() : null), damage);
                
                // 添加击退效果
                Vec3 knockbackVector = entity.position().subtract(hitEntity.position()).normalize();
                entity.push(knockbackVector.x * 0.4, 0.2, knockbackVector.z * 0.4);
            }
        }
        
        // 神圣伤害额外效果：给予发射者伤害吸收效果
        if (spellType == SpellType.HOLY && absorptionLevel > 0) {
            Entity owner = this.getOwner();
            if (owner instanceof LivingEntity livingOwner) {
                // 最高3级伤害吸收效果，每级30秒
                int effectLevel = Math.min(2, absorptionLevel - 1); // 转换为0-2的等级
                livingOwner.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 600, effectLevel));
                
                // 播放神圣效果音效
                this.level().playSound(null, livingOwner.getX(), livingOwner.getY(), livingOwner.getZ(), 
                    SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.0F);
            }
        }
        
        // 根据施法属性播放不同AOE音效
        switch (spellType) {
            case FIRE:
                this.level().playSound(null, hitEntity.getX(), hitEntity.getY(), hitEntity.getZ(), 
                    SoundEvents.FIRECHARGE_USE, SoundSource.NEUTRAL, 1.0F, 0.8F);
                break;
            case FROST:
                this.level().playSound(null, hitEntity.getX(), hitEntity.getY(), hitEntity.getZ(), 
                    SoundEvents.GLASS_BREAK, SoundSource.NEUTRAL, 1.0F, 0.6F);
                break;
            case HOLY:
                this.level().playSound(null, hitEntity.getX(), hitEntity.getY(), hitEntity.getZ(), 
                    SoundEvents.BELL_RESONATE, SoundSource.NEUTRAL, 1.0F, 1.2F);
                break;
        }
    }
    
    /**
     * BlockPos版本的AOE伤害方法
     */
    private void dealAOEDamage(BlockPos hitPos) {
        // 获取AOE范围内的所有实体
        float actualRadius = AOE_RADIUS * getAOERadiusMultiplier();
        List<Entity> entities = this.level().getEntities(this, 
            new AABB(hitPos).inflate(actualRadius), 
            entity -> entity != this && entity != this.getOwner());
        
        // 创建表示命中点的向量
        Vec3 hitVec = new Vec3(hitPos.getX() + 0.5, hitPos.getY() + 0.5, hitPos.getZ() + 0.5);
        
        // 获取施法属性
        SpellType spellType = getSpellType();
        
        // 计算神圣属性的伤害吸收累计等级
        int absorptionLevel = 0;
        
        // 对每个实体造成伤害，根据距离衰减
        for (Entity entity : entities) {
            if (entity instanceof LivingEntity livingTarget) {
                // 计算距离
                double distance = entity.position().distanceTo(hitVec);
                
                // 计算伤害系数，从1.0衰减到0.5
                float damageFactor = AOE_DAMAGE_FACTOR;
                if (distance > 0) {
                    // 线性衰减：近处为100%，远处为50%
                    float distanceFactor = 1.0f - (float)(distance / actualRadius) * 0.5f;
                    damageFactor = AOE_DAMAGE_FACTOR * Math.max(0.5f, distanceFactor);
                }
                
                // 应用伤害
                float damage = (float)(this.getBaseDamage() * damageFactor);
                
                // 根据施法属性应用不同效果
                switch (spellType) {
                    case FIRE:
                        // 火焰伤害增强25%
                        damage *= 1.25f;
                        // 点燃目标，持续5-7秒
                        int fireDuration = 5 + this.random.nextInt(3);
                        livingTarget.setSecondsOnFire(fireDuration);
                        // 向上击飞
                        livingTarget.push(0, 0.5, 0);
                        break;
                        
                    case FROST:
                        // 减速效果，50%减速，持续5秒
                        livingTarget.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1));
                        // 对火焰免疫实体额外伤害
                        if (livingTarget.fireImmune()) {
                            damage *= 1.5f;
                        }
                        break;
                        
                    case HOLY:
                        // 神圣伤害对亡灵生物更有效
                        if (livingTarget.getMobType().equals(MobType.UNDEAD)) {
                            damage *= 1.25f;
                        }
                        // 每击中一个敌人累计1级伤害吸收等级
                        absorptionLevel++;
                        break;
                }
                
                // 应用最终伤害
                entity.hurt(this.damageSources().arrow(this, this.getOwner() instanceof LivingEntity ? (LivingEntity)this.getOwner() : null), damage);
                
                // 添加击退效果
                Vec3 knockbackVector = entity.position().subtract(hitVec).normalize();
                entity.push(knockbackVector.x * 0.4, 0.2, knockbackVector.z * 0.4);
            }
        }
        
        // 神圣伤害额外效果：给予发射者伤害吸收效果
        if (spellType == SpellType.HOLY && absorptionLevel > 0) {
            Entity owner = this.getOwner();
            if (owner instanceof LivingEntity livingOwner) {
                // 最高3级伤害吸收效果，每级30秒
                int effectLevel = Math.min(2, absorptionLevel - 1); // 转换为0-2的等级
                livingOwner.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 600, effectLevel));
                
                // 播放神圣效果音效
                this.level().playSound(null, livingOwner.getX(), livingOwner.getY(), livingOwner.getZ(), 
                    SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.0F);
            }
        }
        
        // 根据施法属性播放不同AOE音效
        switch (spellType) {
            case FIRE:
                this.level().playSound(null, hitPos, 
                    SoundEvents.FIRECHARGE_USE, SoundSource.NEUTRAL, 1.0F, 0.8F);
                break;
            case FROST:
                this.level().playSound(null, hitPos,
                    SoundEvents.GLASS_BREAK, SoundSource.NEUTRAL, 1.0F, 0.6F);
                break;
            case HOLY:
                this.level().playSound(null, hitPos,
                    SoundEvents.BELL_RESONATE, SoundSource.NEUTRAL, 1.0F, 1.2F);
                break;
        }
    }
    
    /**
     * 生成轨迹粒子效果，与施法属性相匹配
     */
    private void spawnTrailParticles() {
        Vec3 movement = this.getDeltaMovement();
        double speed = movement.length();
        
        // 获取施法属性
        SpellType spellType = getSpellType();
        
        // 只有当速度足够大时才生成粒子
        if (speed > 0.1D) {
            // 每tick生成的粒子数量与速度成正比
            int particleCount = Math.max(1, (int)(speed * 2));
            
            for (int i = 0; i < particleCount; i++) {
                // 添加随机偏移，使粒子效果更自然
                double offsetX = this.random.nextGaussian() * 0.02;
                double offsetY = this.random.nextGaussian() * 0.02;
                double offsetZ = this.random.nextGaussian() * 0.02;
                
                // 根据施法属性生成对应粒子效果
                switch (spellType) {
                    case FIRE:
                        // 火焰轨迹
                        if (this.random.nextInt(3) == 0) {
                            this.level().addParticle(
                                ParticleTypes.FLAME,
                                this.getX(), 
                                this.getY(), 
                                this.getZ(),
                                offsetX * 0.2, 
                                offsetY * 0.2, 
                                offsetZ * 0.2
                            );
                        } else {
                            this.level().addParticle(
                                ParticleTypes.SMOKE,
                                this.getX(), 
                                this.getY(), 
                                this.getZ(),
                                offsetX * 0.1, 
                                offsetY * 0.1, 
                                offsetZ * 0.1
                            );
                        }
                        break;
                        
                    case FROST:
                        // 冰霜轨迹
                        this.level().addParticle(
                            ParticleTypes.SNOWFLAKE,
                            this.getX(), 
                            this.getY(), 
                            this.getZ(),
                            offsetX * 0.1, 
                            offsetY * 0.1, 
                            offsetZ * 0.1
                        );
                        break;
                        
                    case HOLY:
                        // 神圣轨迹
                        this.level().addParticle(
                            ParticleTypes.END_ROD,
                            this.getX(), 
                            this.getY(), 
                            this.getZ(),
                            offsetX * 0.1, 
                            offsetY * 0.1, 
                            offsetZ * 0.1
                        );
                        break;
                }
            }
        }
    }
    
    /**
     * 根据施法属性生成基础AOE范围粒子环
     */
    private void spawnAOEBaseRing(double centerX, double centerY, double centerZ, float radius, int particleCount, float colorIntensity) {
        // 获取施法属性的颜色
        SpellType spellType = getSpellType();
        
        for (int i = 0; i < particleCount; i++) {
            double angle = (i * 2 * Math.PI) / particleCount;
            
            // 粒子直接在AOE半径处生成
            double x = centerX + Math.cos(angle) * radius;
            double z = centerZ + Math.sin(angle) * radius;
            double y = centerY;
            
            // 添加一些随机偏移，使圆环不那么完美，更自然
            double offsetRadius = this.random.nextGaussian() * 0.05;
            double offsetY = this.random.nextGaussian() * 0.1;
            
            // 根据施法属性生成对应粒子效果
            switch (spellType) {
                case FIRE:
                    // 火焰粒子
                    if (this.random.nextInt(3) == 0) {
                        this.level().addParticle(
                            ParticleTypes.FLAME,
                            x + offsetRadius * Math.cos(angle),
                            y + offsetY,
                            z + offsetRadius * Math.sin(angle),
                            0.0, 0.03, 0.0
                        );
                    } else {
                        this.level().addParticle(
                            ParticleTypes.SMOKE,
                            x + offsetRadius * Math.cos(angle),
                            y + offsetY,
                            z + offsetRadius * Math.sin(angle),
                            0.0, 0.01, 0.0
                        );
                    }
                    break;
                    
                case FROST:
                    // 冰霜粒子
                    this.level().addParticle(
                        ParticleTypes.SNOWFLAKE,
                        x + offsetRadius * Math.cos(angle),
                        y + offsetY,
                        z + offsetRadius * Math.sin(angle),
                        0.0, -0.01, 0.0
                    );
                    break;
                    
                case HOLY:
                    // 神圣粒子
                    this.level().addParticle(
                        ParticleTypes.END_ROD,
                        x + offsetRadius * Math.cos(angle),
                        y + offsetY,
                        z + offsetRadius * Math.sin(angle),
                        0.0, 0.02, 0.0
                    );
                    break;
            }
        }
    }
    
    private void spawnAOEParticles(Entity hitEntity) {
        // 使用实际AOE范围，随蓄力变化
        float actualRadius = AOE_RADIUS * getAOERadiusMultiplier();
        
        // 生成外圈粒子环
        spawnAOEBaseRing(
            hitEntity.getX(),
            hitEntity.getY() + hitEntity.getBbHeight() / 2,
            hitEntity.getZ(),
            actualRadius,
            100, // 粒子数量
            0.3f // 颜色强度
        );
        
        // 生成内圈粒子环
        spawnAOEBaseRing(
            hitEntity.getX(),
            hitEntity.getY() + hitEntity.getBbHeight() / 2,
            hitEntity.getZ(),
            actualRadius * 0.6f,
            50, // 粒子数量
            0.2f // 颜色强度
        );
        
        // 获取施法属性
        SpellType spellType = getSpellType();
        
        // 根据施法属性添加额外粒子效果
        switch (spellType) {
            case FIRE:
                // 添加火焰粒子
                for (int i = 0; i < 20; i++) {
                    double angle = (i * 2 * Math.PI) / 20;
                    double radius = actualRadius * this.random.nextDouble() * 0.7;
                    
                    double x = hitEntity.getX() + Math.cos(angle) * radius;
                    double z = hitEntity.getZ() + Math.sin(angle) * radius;
                    double y = hitEntity.getY() + hitEntity.getBbHeight() / 2;
                    
                    this.level().addParticle(
                        ParticleTypes.FLAME,
                        x,
                        y + this.random.nextDouble() * 0.5,
                        z,
                        0, 0.05, 0
                    );
                }
                // 添加爆炸粒子效果
                this.level().addParticle(
                    ParticleTypes.EXPLOSION,
                    hitEntity.getX(),
                    hitEntity.getY() + hitEntity.getBbHeight() / 2,
                    hitEntity.getZ(),
                    0, 0, 0
                );
                break;
                
            case FROST:
                // 添加雪粒子
                for (int i = 0; i < 30; i++) {
                    double angle = (i * 2 * Math.PI) / 30;
                    double radius = actualRadius * this.random.nextDouble() * 0.8;
                    
                    double x = hitEntity.getX() + Math.cos(angle) * radius;
                    double z = hitEntity.getZ() + Math.sin(angle) * radius;
                    double y = hitEntity.getY() + hitEntity.getBbHeight() / 2;
                    
                    this.level().addParticle(
                        ParticleTypes.SNOWFLAKE,
                        x,
                        y + this.random.nextDouble() * 0.5,
                        z,
                        0, -0.02, 0
                    );
                }
                break;
                
            case HOLY:
                // 添加发光粒子
                for (int i = 0; i < 15; i++) {
                    double angle = (i * 2 * Math.PI) / 15;
                    double radius = actualRadius * 0.5;
                    
                    double x = hitEntity.getX() + Math.cos(angle) * radius;
                    double z = hitEntity.getZ() + Math.sin(angle) * radius;
                    double y = hitEntity.getY() + hitEntity.getBbHeight() / 2 + 0.3;
                    
                    this.level().addParticle(
                        ParticleTypes.END_ROD,
                        x,
                        y,
                        z,
                        0, 0.08, 0
                    );
                }
                // 增加光柱效果
                for (int i = 0; i < 10; i++) {
                    this.level().addParticle(
                        ParticleTypes.END_ROD,
                        hitEntity.getX(),
                        hitEntity.getY() + i * 0.2,
                        hitEntity.getZ(),
                        0, 0.15, 0
                    );
                }
                break;
        }
    }
    
    private void spawnAOEParticles(BlockPos hitPos) {
        // 使用实际AOE范围，随蓄力变化
        float actualRadius = AOE_RADIUS * getAOERadiusMultiplier();
        
        // 生成外圈粒子环
        spawnAOEBaseRing(
            hitPos.getX() + 0.5,
            hitPos.getY() + 0.5,
            hitPos.getZ() + 0.5,
            actualRadius,
            100, // 粒子数量
            0.3f // 颜色强度
        );
        
        // 生成内圈粒子环
        spawnAOEBaseRing(
            hitPos.getX() + 0.5,
            hitPos.getY() + 0.5,
            hitPos.getZ() + 0.5,
            actualRadius * 0.6f,
            50, // 粒子数量
            0.2f // 颜色强度
        );
        
        // 获取施法属性
        SpellType spellType = getSpellType();
        
        // 根据施法属性添加额外粒子效果
        switch (spellType) {
            case FIRE:
                // 添加火焰粒子
                for (int i = 0; i < 20; i++) {
                    double angle = (i * 2 * Math.PI) / 20;
                    double radius = actualRadius * this.random.nextDouble() * 0.7;
                    
                    double x = hitPos.getX() + 0.5 + Math.cos(angle) * radius;
                    double z = hitPos.getZ() + 0.5 + Math.sin(angle) * radius;
                    double y = hitPos.getY() + 0.5;
                    
                    this.level().addParticle(
                        ParticleTypes.FLAME,
                        x,
                        y + this.random.nextDouble() * 0.5,
                        z,
                        0, 0.05, 0
                    );
                }
                // 添加爆炸粒子效果
                this.level().addParticle(
                    ParticleTypes.EXPLOSION,
                    hitPos.getX() + 0.5,
                    hitPos.getY() + 0.5,
                    hitPos.getZ() + 0.5,
                    0, 0, 0
                );
                break;
                
            case FROST:
                // 添加雪粒子
                for (int i = 0; i < 30; i++) {
                    double angle = (i * 2 * Math.PI) / 30;
                    double radius = actualRadius * this.random.nextDouble() * 0.8;
                    
                    double x = hitPos.getX() + 0.5 + Math.cos(angle) * radius;
                    double z = hitPos.getZ() + 0.5 + Math.sin(angle) * radius;
                    double y = hitPos.getY() + 0.5;
                    
                    this.level().addParticle(
                        ParticleTypes.SNOWFLAKE,
                        x,
                        y + this.random.nextDouble() * 0.5,
                        z,
                        0, -0.02, 0
                    );
                }
                break;
                
            case HOLY:
                // 添加发光粒子
                for (int i = 0; i < 15; i++) {
                    double angle = (i * 2 * Math.PI) / 15;
                    double radius = actualRadius * 0.5;
                    
                    double x = hitPos.getX() + 0.5 + Math.cos(angle) * radius;
                    double z = hitPos.getZ() + 0.5 + Math.sin(angle) * radius;
                    double y = hitPos.getY() + 0.8;
                    
                    this.level().addParticle(
                        ParticleTypes.END_ROD,
                        x,
                        y,
                        z,
                        0, 0.08, 0
                    );
                }
                // 增加光柱效果
                for (int i = 0; i < 10; i++) {
                    this.level().addParticle(
                        ParticleTypes.END_ROD,
                        hitPos.getX() + 0.5,
                        hitPos.getY() + 0.5 + i * 0.2,
                        hitPos.getZ() + 0.5,
                        0, 0.15, 0
                    );
                }
                break;
        }
    }
    
    public void setReturning(boolean returning) {
        this.entityData.set(RETURNING, returning);
        
        // 回归时无重力
        if (returning) {
            this.setNoGravity(true);
            this.setDeltaMovement(Vec3.ZERO); // 重置速度
        }
    }
    
    public boolean isReturning() {
        return this.entityData.get(RETURNING);
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
    protected ItemStack getPickupItem() {
        // 不可直接拾取
        return ItemStack.EMPTY;
    }
    
    @Override
    public boolean isAttackable() {
        return false;
    }
    
    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }
    
    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        
        if (tag.contains("Returning")) {
            this.entityData.set(RETURNING, tag.getBoolean("Returning"));
        }
        
        if (tag.contains("OwnerId")) {
            this.entityData.set(OWNER_ID, tag.getInt("OwnerId"));
        }

        if (tag.contains("HitEntityId")) {
            this.entityData.set(HIT_ENTITY_ID, tag.getInt("HitEntityId"));
        }
        
        this.hasHit = tag.getBoolean("HasHit");
        this.hasDealtAOE = tag.getBoolean("HasDealtAOE");
        
        // 加载AOE范围乘数
        if (tag.contains("AOERadiusMultiplier")) {
            this.entityData.set(AOE_RADIUS_MULTIPLIER, tag.getFloat("AOERadiusMultiplier"));
        }
        
        // 加载最大射程
        if (tag.contains("MaxRange")) {
            this.entityData.set(MAX_RANGE, tag.getFloat("MaxRange"));
        }
        
        // 加载起始位置
        if (tag.contains("StartPosX") && tag.contains("StartPosY") && tag.contains("StartPosZ")) {
            this.startPos = new Vec3(tag.getDouble("StartPosX"), 
                                    tag.getDouble("StartPosY"), 
                                    tag.getDouble("StartPosZ"));
        }
        
        // 加载施法属性
        if (tag.contains("SpellType")) {
            int spellTypeOrdinal = tag.getInt("SpellType");
            if (spellTypeOrdinal >= 0 && spellTypeOrdinal < SpellType.values().length) {
                this.entityData.set(SPELL_TYPE, spellTypeOrdinal);
            }
        }
    }
    
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        
        tag.putBoolean("Returning", this.entityData.get(RETURNING));
        tag.putInt("OwnerId", this.entityData.get(OWNER_ID));
        tag.putInt("HitEntityId", this.entityData.get(HIT_ENTITY_ID));
        tag.putBoolean("HasHit", this.hasHit);
        tag.putBoolean("HasDealtAOE", this.hasDealtAOE);
        
        // 保存AOE范围乘数
        tag.putFloat("AOERadiusMultiplier", this.entityData.get(AOE_RADIUS_MULTIPLIER));
        
        // 保存最大射程
        tag.putFloat("MaxRange", this.entityData.get(MAX_RANGE));
        
        // 保存起始位置
        if (this.startPos != null) {
            tag.putDouble("StartPosX", this.startPos.x);
            tag.putDouble("StartPosY", this.startPos.y);
            tag.putDouble("StartPosZ", this.startPos.z);
        }
        
        // 保存施法属性
        tag.putInt("SpellType", this.entityData.get(SPELL_TYPE));
    }
    
    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
    
    // AOE相关getter方法，供渲染器使用
    public boolean isAoeActive() {
        return this.hasHit && !this.isReturning();
    }

    public float getAoeRadius() {
        return AOE_RADIUS * getAOERadiusMultiplier();
    }

    public int getAoeAge() {
        return this.hasHit ? this.tickCount : 0;
    }
    
    /**
     * 获取AOE范围乘数
     * @return AOE范围乘数
     */
    public float getAOERadiusMultiplier() {
        return this.entityData.get(AOE_RADIUS_MULTIPLIER);
    }
    
    /**
     * 设置AOE范围乘数
     * @param multiplier 乘数，影响AOE范围
     */
    public void setAOERadiusMultiplier(float multiplier) {
        this.entityData.set(AOE_RADIUS_MULTIPLIER, multiplier);
    }

    /**
     * 获取当前施法属性
     */
    public SpellType getSpellType() {
        int ordinal = this.entityData.get(SPELL_TYPE);
        return SpellType.values()[ordinal];
    }
    
    /**
     * 设置施法属性
     */
    public void setSpellType(SpellType spellType) {
        this.entityData.set(SPELL_TYPE, spellType.ordinal());
    }

    /**
     * 获取最大射程
     * @return 最大射程，单位：方块
     */
    public float getMaxRange() {
        return this.entityData.get(MAX_RANGE);
    }
    
    /**
     * 设置最大射程
     * @param range 最大射程，单位：方块
     */
    public void setMaxRange(float range) {
        this.entityData.set(MAX_RANGE, range);
    }
}
