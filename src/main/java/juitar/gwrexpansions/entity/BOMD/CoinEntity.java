package juitar.gwrexpansions.entity.BOMD;

import juitar.gwrexpansions.registry.GWREEntities;
import juitar.gwrexpansions.registry.GWRESounds;
import juitar.gwrexpansions.util.CoinTargetUtils;
import lykrast.gunswithoutroses.entity.BulletEntity;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * 硬币实体 - 由Hellforge抛射的硬币
 * 当被子弹击中时会触发特殊效果
 */
public class CoinEntity extends ThrowableItemProjectile {
    private static final EntityDataAccessor<Integer> OWNER_ID = SynchedEntityData.defineId(CoinEntity.class, EntityDataSerializers.INT);
    private static final int MAX_LIFETIME = 400; // 20秒生存时间，给玩家更多时间
    private int lifetime = 0;
    
    public CoinEntity(EntityType<? extends CoinEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(false);
        this.setBoundingBox(this.getBoundingBox().inflate(0.5)); // 大幅增大碰撞箱以便更容易被子弹击中
    }

    public CoinEntity(Level level, LivingEntity shooter) {
        super(GWREEntities.COIN.get(), shooter, level);
        this.setNoGravity(false);
        this.setBoundingBox(this.getBoundingBox().inflate(0.5)); // 大幅增大碰撞箱以便更容易被子弹击中

        if (shooter != null) {
            this.entityData.set(OWNER_ID, shooter.getId());
        }
    }
    
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(OWNER_ID, -1);
    }
    
    @Override
    protected Item getDefaultItem() {
        return Items.GOLD_NUGGET; // 使用金粒作为硬币的物品表示
    }
    
    @Override
    public void tick() {
        super.tick();

        // 增加生存时间
        lifetime++;

        // 超过最大生存时间后消失
        if (lifetime > MAX_LIFETIME) {
            this.discard();
            return;
        }

        // 添加金色粒子效果 - 增加粒子数量使硬币更明显
        if (this.level().isClientSide) {
            if (this.random.nextFloat() < 0.5f) {
                this.level().addParticle(ParticleTypes.CRIT,
                    this.getX() + (this.random.nextDouble() - 0.5) * 0.5,
                    this.getY() + (this.random.nextDouble() - 0.5) * 0.5,
                    this.getZ() + (this.random.nextDouble() - 0.5) * 0.5,
                    0, 0, 0);
            }

            // 添加额外的金色粒子，使硬币更容易被看到
            if (this.random.nextFloat() < 0.3f) {
                this.level().addParticle(ParticleTypes.END_ROD,
                    this.getX(), this.getY(), this.getZ(),
                    (this.random.nextDouble() - 0.5) * 0.1,
                    (this.random.nextDouble() - 0.5) * 0.1,
                    (this.random.nextDouble() - 0.5) * 0.1);
            }
        }

        // 检查是否被子弹击中 - 服务器端逻辑
        if (!this.level().isClientSide) {
            // 使用更大的检测范围，提高击中率
            double detectionRange = 1.5; // 进一步增大检测范围

            // 检查附近的子弹实体
            List<BulletEntity> nearbyBullets = this.level().getEntitiesOfClass(
                BulletEntity.class,
                this.getBoundingBox().inflate(detectionRange),
                bullet -> {
                    // 确保子弹存活
                    if (bullet == null || !bullet.isAlive()) return false;

                    // 检查是否是Hellforge子弹
                    if (!bullet.getPersistentData().getBoolean("HellforgeShot")) return false;

                    // 计算子弹与硬币的距离
                    double distance = bullet.distanceTo(this);

                    // 更宽松的距离判断
                    return distance < detectionRange;
                }
            );

            // 如果有子弹在附近，处理击中事件
            if (!nearbyBullets.isEmpty()) {
                // 播放音效提示子弹接近硬币
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.NEUTRAL, 0.5F, 2.0F);

                // 处理第一个子弹
                handleBulletHit(nearbyBullets.get(0));
            }
        }
    }
    
    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        
        if (!this.level().isClientSide) {
            // 播放硬币击中音效
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.NEUTRAL, 1.0F, 1.5F);
            
            // 生成金色粒子爆炸效果
            for (int i = 0; i < 10; i++) {
                this.level().addParticle(ParticleTypes.CRIT,
                    this.getX(), this.getY(), this.getZ(),
                    (this.random.nextDouble() - 0.5) * 0.5,
                    (this.random.nextDouble() - 0.5) * 0.5,
                    (this.random.nextDouble() - 0.5) * 0.5);
            }
            
            this.discard();
        }
    }
    
    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity hitEntity = result.getEntity();

        // 如果被子弹击中
        if (hitEntity instanceof BulletEntity bullet) {
            handleBulletHit(bullet);
        }

        super.onHitEntity(result);
    }


    
    /**
     * 处理子弹击中硬币的逻辑 - 支持多硬币连锁反弹增伤
     */
    public void handleBulletHit(BulletEntity bullet) {
        if (!this.level().isClientSide) {
            // 检查是否是Hellforge子弹
            CompoundTag bulletData = bullet.getPersistentData();
            if (!bulletData.getBoolean("HellforgeShot")) {
                System.out.println("DEBUG: 非Hellforge子弹击中硬币，忽略");
                return;
            }

            System.out.println("DEBUG: Hellforge子弹击中硬币！");

            Entity owner = getOwnerEntity();

            if (owner instanceof LivingEntity livingOwner) {
                // 检查是否已经反弹过（防止无限反弹）
                int bounceCount = bulletData.getInt("CoinBounceCount");

                System.out.println("DEBUG: 硬币处理 - 当前反弹次数=" + bounceCount);

                // 限制最大反弹次数
                if (bounceCount >= 5) {
                    // 超过最大反弹次数，子弹消失
                    bullet.discard();
                    this.discard();
                    return;
                }

                // 增加反弹计数
                bulletData.putInt("CoinBounceCount", bounceCount + 1);
                System.out.println("DEBUG: 硬币处理 - 设置新反弹次数=" + (bounceCount + 1));

                // 设置子弹位置到硬币位置
                bullet.setPos(this.getX(), this.getY(), this.getZ());

                // 增加伤害（第一次反弹+100%，后续+50%）
                double damageMultiplier = bounceCount == 0 ? 2.0 : 1.5;
                bullet.setDamage(bullet.getDamage() * damageMultiplier);

                // 寻找目标的优先级：硬币 > aimed目标 > 普通敌人
                Entity target = null;
                final double SEARCH_RANGE = 32.0;

                // 首先寻找其他硬币（连锁弹射优先级最高）
                CoinEntity nearestCoin = findNearestValidCoin(this.level(), this.position(), SEARCH_RANGE, this);
                if (nearestCoin != null) {
                    target = nearestCoin;
                } else {
                    // 没有硬币，寻找aimed目标
                    LivingEntity livingTarget = CoinTargetUtils.findNearestAimedTarget(this.level(), this.position(), SEARCH_RANGE);
                    if (livingTarget == null) {
                        // 没有aimed目标，寻找普通敌人
                        livingTarget = CoinTargetUtils.findNearestEnemy(this.level(), this.position(), livingOwner, SEARCH_RANGE);
                    }
                    target = livingTarget;
                }

                if (target != null) {
                    System.out.println("DEBUG: 硬币找到目标 - " + target.getClass().getSimpleName() + " ID=" + target.getId());

                    // 计算朝向目标的方向
                    Vec3 targetPos = target.position().add(0, target.getBbHeight() * 0.5, 0);
                    Vec3 currentPos = this.position();
                    Vec3 direction = targetPos.subtract(currentPos).normalize();

                    System.out.println("DEBUG: 设置子弹方向 - 从" + currentPos + "到" + targetPos);

                    // 设置子弹的新运动方向和速度
                    double speed = 3.0; // 反弹后的速度
                    bullet.setDeltaMovement(direction.scale(speed));

                    // 如果目标是生物，标记aimed效果移除（击中后移除）
                    if (target instanceof LivingEntity livingTarget) {
                        bulletData.putBoolean("RemoveAimedOnHit", true);
                        bulletData.putInt("AimedTargetId", livingTarget.getId());
                        System.out.println("DEBUG: 标记aimed目标移除 - 目标ID=" + livingTarget.getId());
                    }

                    // 播放硬币击中音效（连锁反弹音效更响亮）
                    this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        GWRESounds.HELLFORGE_REVOLVER_COIN_HIT.get(), SoundSource.PLAYERS,
                        bounceCount > 0 ? 1.5F : 1.0F, bounceCount > 0 ? 1.2F : 1.0F);
                } else {
                    // 没有目标，给子弹一个随机方向继续飞行
                    Vec3 randomDirection = new Vec3(
                        (this.random.nextDouble() - 0.5) * 2.0,
                        (this.random.nextDouble() - 0.5) * 2.0,
                        (this.random.nextDouble() - 0.5) * 2.0
                    ).normalize();
                    bullet.setDeltaMovement(randomDirection.scale(2.0));

                    // 播放不同的音效表示没有目标
                    this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
                }

                // 生成粒子效果（连锁弹射粒子更多，伤害越高粒子越多）
                int particleCount = 20 + (bounceCount * 10); // 基础20，每次反弹+10
                for (int i = 0; i < particleCount; i++) {
                    this.level().addParticle(ParticleTypes.CRIT,
                        this.getX(), this.getY(), this.getZ(),
                        (this.random.nextDouble() - 0.5) * 1.0,
                        (this.random.nextDouble() - 0.5) * 1.0,
                        (this.random.nextDouble() - 0.5) * 1.0);
                }

                // 连锁弹射时添加特殊粒子效果
                if (bounceCount > 0) {
                    int enchantedParticles = 15 + (bounceCount * 5); // 连锁越多粒子越多
                    for (int i = 0; i < enchantedParticles; i++) {
                        this.level().addParticle(ParticleTypes.ENCHANTED_HIT,
                            this.getX(), this.getY(), this.getZ(),
                            (this.random.nextDouble() - 0.5) * 1.5,
                            (this.random.nextDouble() - 0.5) * 1.5,
                            (this.random.nextDouble() - 0.5) * 1.5);
                    }
                }
            }

            // 移除硬币（但不移除子弹）
            this.discard();
        }
    }

    /**
     * 寻找最近的有效硬币（排除当前硬币）
     */
    private CoinEntity findNearestValidCoin(Level level, Vec3 position, double range, CoinEntity excludeCoin) {
        List<CoinEntity> coins = level.getEntitiesOfClass(CoinEntity.class,
            new net.minecraft.world.phys.AABB(position.subtract(range, range, range), position.add(range, range, range)));

        CoinEntity nearestCoin = null;
        double nearestDistance = Double.MAX_VALUE;

        for (CoinEntity coin : coins) {
            if (coin != excludeCoin && coin.isAlive()) {
                double distance = coin.position().distanceTo(position);
                if (distance < nearestDistance) {
                    nearestDistance = distance;
                    nearestCoin = coin;
                }
            }
        }

        return nearestCoin;
    }
    
    /**
     * 获取硬币的所有者实体
     */
    public Entity getOwnerEntity() {
        int ownerId = this.entityData.get(OWNER_ID);
        if (ownerId != -1) {
            return this.level().getEntity(ownerId);
        }
        return null;
    }
    
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Lifetime", this.lifetime);
        tag.putInt("OwnerId", this.entityData.get(OWNER_ID));
    }
    
    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.lifetime = tag.getInt("Lifetime");
        this.entityData.set(OWNER_ID, tag.getInt("OwnerId"));
    }
}
