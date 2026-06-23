package juitar.gwrexpansions.entity.BOMD;

import juitar.gwrexpansions.advancement.BloodIsFuelTrigger;
import juitar.gwrexpansions.advancement.BOMD.BOMDGameplayEventHandler;
import juitar.gwrexpansions.item.BOMD.Hellforge;
import juitar.gwrexpansions.network.CoinHitFeedbackPacket;
import juitar.gwrexpansions.network.GWRENetwork;
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
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

/**
  *
 */
public class CoinEntity extends ThrowableItemProjectile {
    private static final EntityDataAccessor<Integer> OWNER_ID = SynchedEntityData.defineId(CoinEntity.class, EntityDataSerializers.INT);
    private static final int MAX_LIFETIME = 80;
    private static final int GOLDEN_WINDOW_TICKS = 10;
    private int lifetime = 0;
    
    public CoinEntity(EntityType<? extends CoinEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.setBoundingBox(this.getBoundingBox().inflate(0.5));
    }

    public CoinEntity(Level level, LivingEntity shooter) {
        super(GWREEntities.COIN.get(), shooter, level);
        this.setNoGravity(true);
        this.setBoundingBox(this.getBoundingBox().inflate(0.5));
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
        return Items.GOLD_NUGGET;
    }
    
    @Override
    public void tick() {
        super.tick();

        lifetime++;
        if (lifetime <= GOLDEN_WINDOW_TICKS) {
            this.setNoGravity(true);
            this.setDeltaMovement(this.getDeltaMovement().scale(0.84));
        } else {
            this.setNoGravity(false);
        }

        if (lifetime > MAX_LIFETIME) {
            this.discard();
            return;
        }

        if (this.level().isClientSide) {
            if (this.random.nextFloat() < 0.5f) {
                this.level().addParticle(ParticleTypes.CRIT,
                    this.getX() + (this.random.nextDouble() - 0.5) * 0.5,
                    this.getY() + (this.random.nextDouble() - 0.5) * 0.5,
                    this.getZ() + (this.random.nextDouble() - 0.5) * 0.5,
                    0, 0, 0);
            }

            if (this.random.nextFloat() < 0.3f) {
                this.level().addParticle(ParticleTypes.END_ROD,
                    this.getX(), this.getY(), this.getZ(),
                    (this.random.nextDouble() - 0.5) * 0.1,
                    (this.random.nextDouble() - 0.5) * 0.1,
                    (this.random.nextDouble() - 0.5) * 0.1);
            }
        }

        if (!this.level().isClientSide) {
            double detectionRange = lifetime <= GOLDEN_WINDOW_TICKS ? 2.25 : 1.5;

            List<BulletEntity> nearbyBullets = this.level().getEntitiesOfClass(
                BulletEntity.class,
                this.getBoundingBox().inflate(detectionRange),
                bullet -> isValidHellforgeBullet(bullet) && bullet.distanceTo(this) < detectionRange
            );

            if (!nearbyBullets.isEmpty()) {
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.NEUTRAL, 0.5F, 2.0F);

                handleBulletHits(nearbyBullets);
            }
        }
    }
    
    @Override
    protected void onHit(HitResult result) {
        if (result.getType() == HitResult.Type.BLOCK) {
            if (shouldDisappearOnBlockHit(result)) {
                playLandingEffects();
                this.discard();
            }
            return;
        }

        super.onHit(result);
        
        if (!this.level().isClientSide) {
            playLandingEffects();
            this.discard();
        }
    }

    private boolean shouldDisappearOnBlockHit(HitResult result) {
        if (!(result instanceof BlockHitResult blockHit)) {
            return false;
        }
        return this.getDeltaMovement().y < 0.0D && blockHit.getDirection() == Direction.UP;
    }

    private void playLandingEffects() {
        if (this.level().isClientSide) {
            return;
        }
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
            SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.NEUTRAL, 1.0F, 1.5F);

        for (int i = 0; i < 10; i++) {
            this.level().addParticle(ParticleTypes.CRIT,
                this.getX(), this.getY(), this.getZ(),
                (this.random.nextDouble() - 0.5) * 0.5,
                (this.random.nextDouble() - 0.5) * 0.5,
                (this.random.nextDouble() - 0.5) * 0.5);
        }
    }
    
    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity hitEntity = result.getEntity();

        if (hitEntity instanceof BulletEntity bullet) {
            double detectionRange = 1.5;
            List<BulletEntity> nearbyBullets = this.level().getEntitiesOfClass(
                BulletEntity.class,
                this.getBoundingBox().inflate(detectionRange),
                nearbyBullet -> isValidHellforgeBullet(nearbyBullet) && nearbyBullet.distanceTo(this) < detectionRange
            );
            if (!nearbyBullets.contains(bullet)) {
                nearbyBullets.add(bullet);
            }
            handleBulletHits(nearbyBullets);
            return;
        }

        super.onHitEntity(result);
    }


    
    public void handleBulletHit(BulletEntity bullet) {
        if (!this.level().isClientSide) {
            double detectionRange = 1.5;
            List<BulletEntity> nearbyBullets = this.level().getEntitiesOfClass(
                BulletEntity.class,
                this.getBoundingBox().inflate(detectionRange),
                nearbyBullet -> isValidHellforgeBullet(nearbyBullet) && nearbyBullet.distanceTo(this) < detectionRange
            );
            if (!nearbyBullets.contains(bullet)) {
                nearbyBullets.add(bullet);
            }
            handleBulletHits(nearbyBullets);
        }
    }

    private void handleBulletHits(List<BulletEntity> bullets) {
        if (this.level().isClientSide) {
            return;
        }

        addBulletsSeekingThisCoin(bullets);

        boolean handledAny = false;
        boolean spawnedExtra = false;
        for (BulletEntity bullet : bullets) {
            if (!isValidHellforgeBullet(bullet)) {
                continue;
            }

            boolean shouldSpawnExtra = !spawnedExtra;
            if (processBulletHit(bullet, shouldSpawnExtra)) {
                handledAny = true;
                spawnedExtra |= shouldSpawnExtra;
            }
        }

        if (handledAny) {
            this.discard();
        }
    }

    private void addBulletsSeekingThisCoin(List<BulletEntity> bullets) {
        double searchRange = 64.0;
        List<BulletEntity> seekingBullets = this.level().getEntitiesOfClass(
            BulletEntity.class,
            this.getBoundingBox().inflate(searchRange),
            bullet -> isValidHellforgeBullet(bullet)
                && bullet.getPersistentData().getBoolean("SeekingCoinChain")
                && bullet.getPersistentData().getInt("TargetCoinId") == this.getId()
        );

        for (BulletEntity bullet : seekingBullets) {
            if (!bullets.contains(bullet)) {
                bullets.add(bullet);
            }
        }
    }

    private boolean processBulletHit(BulletEntity bullet, boolean spawnExtra) {
        if (!this.level().isClientSide) {
            CompoundTag bulletData = bullet.getPersistentData();
            if (!bulletData.getBoolean("HellforgeShot")) {
                return false;
            }

            Entity owner = getOwnerEntity();

            if (owner instanceof LivingEntity livingOwner) {
                int bounceCount = bulletData.getInt("CoinBounceCount");

                if (bounceCount >= 5) {
                    bullet.discard();
                    return true;
                }

                int coinLinkHits = bounceCount + 1;
                bulletData.putInt("CoinBounceCount", coinLinkHits);

                BOMDGameplayEventHandler.recordCoinBounceBullet(bullet);

                if (bounceCount == 3 && owner instanceof ServerPlayer player) {
                    BloodIsFuelTrigger.onFourCoinsHit(player);
                }

                bullet.setPos(this.getX(), this.getY(), this.getZ());

                boolean isClone = bulletData.getBoolean("HellforgeCoinClone");
                boolean canResetCooldown = !isClone && !bulletData.getBoolean("HellforgeCoinCooldownReset");
                int chainHits = isClone ? Math.max(1, bulletData.getInt("HellforgeCoinChainHits"))
                    : Hellforge.recordCoinHit(livingOwner, false, false);
                boolean shouldResetCooldown = canResetCooldown && chainHits >= 4;
                if (shouldResetCooldown) {
                    Hellforge.clearFireCooldown(livingOwner);
                }
                if (!isClone) {
                    ItemStack hellforge = Hellforge.findHellforgeStack(livingOwner);
                    Hellforge.advanceCoinRecharge(hellforge, Hellforge.getCoinRechargeAdvanceForLink(coinLinkHits));
                    int previousOverheatLink = bulletData.getInt("HellforgeCoinOverheatBestLink");
                    if (coinLinkHits >= 3 && coinLinkHits > previousOverheatLink) {
                        Hellforge.triggerCoinOverheat(livingOwner, coinLinkHits);
                        bulletData.putInt("HellforgeCoinOverheatBestLink", coinLinkHits);
                    }
                    int previousReturned = bulletData.getInt("HellforgeCoinReturnedAmount");
                    int targetReturned = Hellforge.getCoinReturnAmount(chainHits, coinLinkHits);
                    int coinsToReturn = Math.max(0, targetReturned - previousReturned);
                    if (coinsToReturn > 0) {
                        Hellforge.addCoins(hellforge, coinsToReturn);
                        bulletData.putInt("HellforgeCoinReturnedAmount", previousReturned + coinsToReturn);
                    }
                }
                if (bulletData.getInt("HellforgeCoinReturnedAmount") > 0) {
                    bulletData.putBoolean("HellforgeCoinReturned", true);
                }
                if (shouldResetCooldown) {
                    bulletData.putBoolean("HellforgeCoinCooldownReset", true);
                }
                bulletData.putInt("HellforgeCoinChainHits", chainHits);
                bulletData.putString("HellforgeCoinGrade", Hellforge.getCoinChainGrade(chainHits));

                if (!isClone && owner instanceof ServerPlayer player) {
                    GWRENetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                        new CoinHitFeedbackPacket(chainHits, Hellforge.getCoinChainWindowTicks(), Hellforge.findHellforgeStack(livingOwner).getOrCreateTag().getInt(Hellforge.NBT_COIN_OVERHEAT_TIMER)));
                }

                double baseDamage = getOrStoreBaseDamage(bullet);
                double damageMultiplier = isClone ? 0.5D : Hellforge.getCoinLinkDamageMultiplier(chainHits, coinLinkHits);
                bullet.setDamage(baseDamage * damageMultiplier);
                bulletData.putInt("HellforgeCoinLinkHits", coinLinkHits);

                Entity target = null;
                final double SEARCH_RANGE = 32.0;

                CoinEntity nearestCoin = findNearestValidCoin(this.level(), this.position(), SEARCH_RANGE, this);
                if (nearestCoin != null) {
                    target = nearestCoin;
                    int intentTargetId = getIntentTargetId();
                    if (intentTargetId >= 0 && !nearestCoin.getPersistentData().contains(Hellforge.COIN_INTENT_TARGET_ID)) {
                        nearestCoin.getPersistentData().putInt(Hellforge.COIN_INTENT_TARGET_ID, intentTargetId);
                    }
                } else {
                    LivingEntity livingTarget = CoinTargetUtils.findBestRicochetTarget(this.level(), this.position(), livingOwner, SEARCH_RANGE,
                        getIntentOrPriorityTargetId(livingOwner));
                    target = livingTarget;
                }

                if (target != null) {
                    Vec3 targetPos = target.position().add(0, target.getBbHeight() * 0.5, 0);
                    Vec3 currentPos = this.position();
                    Vec3 direction = targetPos.subtract(currentPos).normalize();

                    double speed = 3.0;
                    bullet.setDeltaMovement(direction.scale(speed));

                    if (target instanceof CoinEntity targetCoin) {
                        bulletData.putBoolean("SeekingCoinChain", true);
                        bulletData.putInt("TargetCoinId", targetCoin.getId());
                    } else {
                        bulletData.putBoolean("SeekingCoinChain", false);
                        bulletData.remove("TargetCoinId");

                        if (shouldSpawnCopyBullet(bulletData, chainHits)) {
                            spawnExtraBouncedBullet(bullet, livingOwner);
                        }
                    }

                    this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        GWRESounds.HELLFORGE_REVOLVER_COIN_HIT.get(), SoundSource.PLAYERS,
                        chainHits >= 4 ? 1.8F : 1.2F, Math.min(1.8F, 0.95F + chainHits * 0.12F));
                } else {
                    Vec3 randomDirection = new Vec3(
                        (this.random.nextDouble() - 0.5) * 2.0,
                        (this.random.nextDouble() - 0.5) * 2.0,
                        (this.random.nextDouble() - 0.5) * 2.0
                    ).normalize();
                    bullet.setDeltaMovement(randomDirection.scale(2.0));
                    bulletData.putBoolean("SeekingCoinChain", false);
                    bulletData.remove("TargetCoinId");

                    if (shouldSpawnCopyBullet(bulletData, chainHits)) {
                        spawnExtraBouncedBullet(bullet, livingOwner);
                    }

                    this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                        SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
                }

                int particleCount = 24 + (chainHits * 8);
                for (int i = 0; i < particleCount; i++) {
                    this.level().addParticle(ParticleTypes.CRIT,
                        this.getX(), this.getY(), this.getZ(),
                        (this.random.nextDouble() - 0.5) * 1.0,
                        (this.random.nextDouble() - 0.5) * 1.0,
                        (this.random.nextDouble() - 0.5) * 1.0);
                }

                if (chainHits > 1) {
                    int enchantedParticles = 14 + (chainHits * 6);
                    for (int i = 0; i < enchantedParticles; i++) {
                        this.level().addParticle(ParticleTypes.ENCHANTED_HIT,
                            this.getX(), this.getY(), this.getZ(),
                            (this.random.nextDouble() - 0.5) * 1.5,
                            (this.random.nextDouble() - 0.5) * 1.5,
                            (this.random.nextDouble() - 0.5) * 1.5);
                    }
                }
            }

            return true;
        }

        return false;
    }

    private boolean isValidHellforgeBullet(BulletEntity bullet) {
        return bullet != null && bullet.isAlive() && bullet.getPersistentData().getBoolean("HellforgeShot");
    }

    private void spawnExtraBouncedBullet(BulletEntity source, LivingEntity owner) {
        source.getPersistentData().putBoolean("HellforgeCoinCopySpawned", true);

        Entity entity = source.getType().create(this.level());
        if (!(entity instanceof BulletEntity extraBullet)) {
            return;
        }

        extraBullet.setOwner(owner);
        extraBullet.setPos(source.getX(), source.getY(), source.getZ());
        extraBullet.setDeltaMovement(source.getDeltaMovement());
        extraBullet.setXRot(source.getXRot());
        extraBullet.setYRot(source.getYRot());
        int linkHits = Math.max(1, source.getPersistentData().getInt("HellforgeCoinLinkHits"));
        extraBullet.setDamage(source.getDamage() * Hellforge.getCoinCopyDamageRatio(linkHits));
        extraBullet.setKnockbackStrength(source.getKnockbackStrength());
        extraBullet.setHeadshotMultiplier(1.0D);
        extraBullet.setWaterInertia(source.getWaterInertia());
        extraBullet.setItem(source.getItem().copy());
        extraBullet.getPersistentData().merge(source.getPersistentData().copy());
        extraBullet.getPersistentData().putBoolean("HellforgeShot", true);
        extraBullet.getPersistentData().putBoolean("HellforgeCoinClone", true);
        extraBullet.getPersistentData().putBoolean("HellforgeCoinCopySpawned", true);
        extraBullet.getPersistentData().putBoolean("HellforgeCoinReturned", true);
        extraBullet.getPersistentData().putBoolean("HellforgeCoinCooldownReset", true);
        extraBullet.getPersistentData().putBoolean("HellforgeCoinPierceArmor", false);
        extraBullet.getPersistentData().putBoolean("HellforgeCoinSplash", false);

        this.level().addFreshEntity(extraBullet);
        BOMDGameplayEventHandler.recordCoinBounceBullet(extraBullet);
    }

    private int getPreviewChainHits(LivingEntity owner) {
        ItemStack stack = Hellforge.findHellforgeStack(owner);
        if (stack.isEmpty()) {
            return 1;
        }
        CompoundTag tag = stack.getOrCreateTag();
        return tag.getInt(Hellforge.NBT_COIN_CHAIN_TIMER) > 0 ? tag.getInt(Hellforge.NBT_COIN_CHAIN_HITS) + 1 : 1;
    }

    private double getOrStoreBaseDamage(BulletEntity bullet) {
        CompoundTag bulletData = bullet.getPersistentData();
        if (!bulletData.contains("HellforgeCoinBaseDamage")) {
            bulletData.putDouble("HellforgeCoinBaseDamage", bullet.getDamage());
        }
        return bulletData.getDouble("HellforgeCoinBaseDamage");
    }

    private boolean shouldSpawnCopyBullet(CompoundTag bulletData, int chainHits) {
        return chainHits >= 4
            && !bulletData.getBoolean("HellforgeCoinClone")
            && !bulletData.getBoolean("HellforgeCoinCopySpawned");
    }

    private int getIntentOrPriorityTargetId(LivingEntity owner) {
        int intentTargetId = getIntentTargetId();
        return intentTargetId >= 0 ? intentTargetId : Hellforge.getPriorityTargetId(owner);
    }

    private int getIntentTargetId() {
        CompoundTag coinData = this.getPersistentData();
        return coinData.contains(Hellforge.COIN_INTENT_TARGET_ID) ? coinData.getInt(Hellforge.COIN_INTENT_TARGET_ID) : -1;
    }

    /**
      *
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
