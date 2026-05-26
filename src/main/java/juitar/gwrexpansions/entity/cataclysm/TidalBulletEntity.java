package juitar.gwrexpansions.entity.cataclysm;

import com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.The_Leviathan.Abyss_Mine_Entity;
import juitar.gwrexpansions.config.GWREConfig;
import juitar.gwrexpansions.entity.meetyourfight.DuskfallBulletDelegate;
import juitar.gwrexpansions.item.cataclysm.TidalGunItem;
import juitar.gwrexpansions.registry.GWREEntities;
import lykrast.gunswithoutroses.entity.BulletEntity;
import lykrast.gunswithoutroses.item.IBullet;
import lykrast.gunswithoutroses.registry.GWRDamage;
import lykrast.gunswithoutroses.registry.GWRItems;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class TidalBulletEntity extends BulletEntity implements DuskfallBulletDelegate {
    private static final double AQUATIC_MULTI = 1.5D;

    public TidalBulletEntity(EntityType<? extends BulletEntity> type, Level world) {
        super(type, world);
    }

    public TidalBulletEntity(Level level, LivingEntity shooter) {
        super(GWREEntities.TIDAL_BULLET.get(), shooter, level);
    }

    @Override
    protected ParticleOptions getTrailParticle() {
        return ParticleTypes.REVERSE_PORTAL;
    }

    @Override
    public ParticleOptions gwrexpansions$getDuskfallTrailParticle() {
        return getTrailParticle();
    }

    @Override
    protected void onHitEntity(EntityHitResult raytrace) {
        if (!this.level().isClientSide) {
            Entity target = raytrace.getEntity();
            Entity shooter = this.getOwner();
            Item item = this.getItemRaw().getItem();
            IBullet bullet = item instanceof IBullet ? (IBullet) item : GWRItems.ironBullet.get();

            if (this.isOnFire()) {
                target.setSecondsOnFire(5);
            }

            int lastHurtResistant = target.invulnerableTime;
            target.invulnerableTime = 0;
            boolean headshot = this.hasHeadshot(target);
            boolean waterCreature = target.getTags().contains("aquatic") || target.isInWaterOrBubble();
            float hitdamage = (float) bullet.modifyDamage(
                    this.damage * (headshot ? this.headshotMult : 1.0D) * (waterCreature ? AQUATIC_MULTI : 1.0D),
                    this, target, shooter, this.level(), headshot);
            boolean damaged = shooter == null
                    ? target.hurt(GWRDamage.gunDamage(this.level().registryAccess(), this), hitdamage)
                    : target.hurt(GWRDamage.gunDamage(this.level().registryAccess(), this, shooter), hitdamage);

            if (damaged && target instanceof LivingEntity livingTarget) {
                if (this.knockbackStrength > 0.0D) {
                    Vec3 vec = this.getDeltaMovement().multiply(1.0F, 0.0F, 1.0F).normalize()
                            .scale(this.knockbackStrength * 0.6D);
                    if (vec.lengthSqr() > 0.0D) {
                        livingTarget.push(vec.x, 0.1D, vec.z);
                    }
                }
                if (shooter instanceof LivingEntity livingShooter) {
                    this.doEnchantDamageEffects(livingShooter, target);
                    TidalGunItem.addEnergyToHeld(livingShooter, TidalGunItem.tidalConfig().hitEnergy.get());
                    handleEntityFollowup(livingShooter, livingTarget);
                }
                bullet.onLivingEntityHit(this, livingTarget, shooter, this.level(), headshot);
            } else if (!damaged) {
                target.invulnerableTime = lastHurtResistant;
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!this.level().isClientSide && this.getOwner() instanceof LivingEntity shooter) {
            handleBlockFollowup(shooter, result);
        }
    }

    @Override
    public boolean gwrexpansions$onDuskfallHitEntity(EntityHitResult result) {
        onHitEntity(result);
        return true;
    }

    @Override
    public boolean gwrexpansions$onDuskfallHitBlock(BlockHitResult result) {
        onHitBlock(result);
        return true;
    }

    private void handleEntityFollowup(LivingEntity shooter, LivingEntity target) {
        GWREConfig.TidalPistolConfig config = TidalGunItem.tidalConfig();
        boolean fullForm = TidalGunItem.isFullForm(level(), shooter);
        int cost = fullForm ? config.orbCost.get() : config.landOrbCost.get();
        double damageMultiplier = fullForm ? config.waterSkillDamageMultiplier.get() : config.landSkillDamageMultiplier.get();
        double speedMultiplier = fullForm ? config.orbSpeedMultiplier.get() : config.landOrbSpeedMultiplier.get();
        int cooldownTicks = fullForm ? config.fullFormOrbCooldownTicks.get() : config.landOrbCooldownTicks.get();

        if (TidalGunItem.consumeHeldEnergy(shooter, cost)) {
            spawnOrb(shooter, target, true, damageMultiplier, speedMultiplier);
            return;
        }

        if (TidalGunItem.hasEchoCooldown(shooter, false, fullForm)) {
            return;
        }

        double chance = fullForm ? config.fullFormOrbChance.get() : config.landOrbChance.get();
        if (chance <= 0.0D || this.random.nextDouble() >= chance) {
            return;
        }

        spawnOrb(shooter, target, true, damageMultiplier, speedMultiplier);
        TidalGunItem.setEchoCooldown(shooter, false, fullForm, cooldownTicks);
    }

    private void handleBlockFollowup(LivingEntity shooter, BlockHitResult result) {
        GWREConfig.TidalPistolConfig config = TidalGunItem.tidalConfig();
        boolean fullForm = TidalGunItem.isFullForm(level(), shooter);
        int cost = fullForm ? config.mineCost.get() : config.landMineCost.get();
        int warmup = fullForm ? 8 : 24;
        int cooldownTicks = fullForm ? config.fullFormMineCooldownTicks.get() : config.landMineCooldownTicks.get();

        if (TidalGunItem.consumeHeldEnergy(shooter, cost)) {
            spawnMine(shooter, result, warmup);
            return;
        }

        if (TidalGunItem.hasEchoCooldown(shooter, true, fullForm)) {
            return;
        }

        double chance = fullForm ? config.fullFormMineChance.get() : config.landMineChance.get();
        if (chance <= 0.0D || this.random.nextDouble() >= chance) {
            return;
        }

        spawnMine(shooter, result, warmup);
        TidalGunItem.setEchoCooldown(shooter, true, fullForm, cooldownTicks);
    }

    private void spawnOrb(LivingEntity shooter, LivingEntity target, boolean tracking, double damageMultiplier, double speedMultiplier) {
        Vec3 start = shooter.getEyePosition().add(shooter.getLookAngle().scale(0.65D));
        Vec3 targetCenter = target.position().add(0.0D, target.getBbHeight() * 0.65D, 0.0D);
        Vec3 direction = targetCenter.subtract(start);
        if (direction.lengthSqr() < 0.01D) {
            direction = shooter.getLookAngle();
        }

        TidalAbyssOrbEntity orb = new TidalAbyssOrbEntity(
                shooter,
                direction.x,
                direction.y,
                direction.z,
                level(),
                (float) Math.max(1.0D, this.damage * damageMultiplier),
                target,
                speedMultiplier);
        orb.setPos(start.x, start.y, start.z);
        orb.setTracking(tracking);
        orb.setDeltaMovement(direction.normalize().scale(0.075D * speedMultiplier));
        level().addFreshEntity(orb);
        level().playSound(null, shooter.blockPosition(), SoundEvents.TRIDENT_RIPTIDE_1, SoundSource.PLAYERS,
                tracking ? 0.7F : 0.45F, tracking ? 1.2F : 0.85F);
    }

    private void spawnMine(LivingEntity shooter, BlockHitResult result, int warmup) {
        Vec3 hit = result.getLocation();
        Abyss_Mine_Entity mine = new Abyss_Mine_Entity(level(), hit.x, hit.y + 0.05D, hit.z, shooter.getYRot(), warmup, shooter);
        level().addFreshEntity(mine);
        level().playSound(null, hit.x, hit.y, hit.z, SoundEvents.BUBBLE_COLUMN_WHIRLPOOL_INSIDE, SoundSource.PLAYERS,
                0.75F, warmup <= 8 ? 1.15F : 0.8F);
    }
}
