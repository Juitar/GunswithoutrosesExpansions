package juitar.gwrexpansions.entity.meetyourfight;

import com.github.L_Ender.cataclysm.init.ModParticle;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import juitar.gwrexpansions.registry.CompatCataclysm;
import juitar.gwrexpansions.registry.CompatIceandfire;
import juitar.gwrexpansions.registry.GWREEntities;
import lykrast.gunswithoutroses.entity.BulletEntity;
import lykrast.gunswithoutroses.entity.PiercingBulletEntity;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import javax.annotation.Nullable;

public class DuskfallPiercingBulletEntity extends PiercingBulletEntity {
    @Nullable
    private BulletEntity effectDelegate;
    @Nullable
    private IntOpenHashSet piercedEntityIds;

    public DuskfallPiercingBulletEntity(EntityType<? extends BulletEntity> type, Level level) {
        super(type, level);
    }

    public DuskfallPiercingBulletEntity(Level level, LivingEntity shooter) {
        super(GWREEntities.DUSKFALL_PIERCING_BULLET.get(), level);
        setOwner(shooter);
        moveTo(shooter.getX(), shooter.getEyeY() - 0.1D, shooter.getZ(), shooter.getYRot(), shooter.getXRot());
    }

    public void setEffectDelegate(BulletEntity effectDelegate) {
        this.effectDelegate = effectDelegate;
        syncEffectDelegate();
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return super.canHitEntity(entity) && (piercedEntityIds == null || !piercedEntityIds.contains(entity.getId()));
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        boolean handled = invokeDelegateHitEntity(result);
        if (!handled) {
            super.onHitEntity(result);
            return;
        }

        if (piercedEntityIds == null) {
            piercedEntityIds = new IntOpenHashSet(5);
        }
        piercedEntityIds.add(result.getEntity().getId());
        setDamage(getDamage() * getPierceMultiplier());
        if (piercedEntityIds.size() > getPierce()) {
            remove(RemovalReason.KILLED);
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult hit) {
        if (!invokeDelegateHitBlock(hit)) {
            super.onHitBlock(hit);
        }
    }

    @Override
    protected ParticleOptions getTrailParticle() {
        ParticleOptions particle = getTrailParticleFromSyncedItem();
        if (particle != null) {
            return particle;
        }
        if (effectDelegate instanceof DuskfallBulletDelegate delegate) {
            particle = delegate.gwrexpansions$getDuskfallTrailParticle();
            if (particle != null) {
                return particle;
            }
        }
        return super.getTrailParticle();
    }

    @Nullable
    private ParticleOptions getTrailParticleFromSyncedItem() {
        ItemStack ammo = getItem();
        if (ammo.isEmpty()) {
            return null;
        }
        if (CompatCataclysm.lavapower_bullet != null && CompatCataclysm.lavapower_bullet.isPresent()
                && ammo.is(CompatCataclysm.lavapower_bullet.get())) {
            return ParticleTypes.LAVA;
        }
        if (CompatCataclysm.ignitium_bullet != null && CompatCataclysm.ignitium_bullet.isPresent()
                && ammo.is(CompatCataclysm.ignitium_bullet.get())) {
            return ParticleTypes.FLAME;
        }
        if (CompatCataclysm.tidal_bullet != null && CompatCataclysm.tidal_bullet.isPresent()
                && ammo.is(CompatCataclysm.tidal_bullet.get())) {
            return ParticleTypes.REVERSE_PORTAL;
        }
        if (CompatCataclysm.cursium_bullet != null && CompatCataclysm.cursium_bullet.isPresent()
                && ammo.is(CompatCataclysm.cursium_bullet.get())) {
            return ModParticle.PHANTOM_WING_FLAME.get();
        }
        if (CompatIceandfire.dragonsteel_fire_bullet != null && CompatIceandfire.dragonsteel_fire_bullet.isPresent()
                && ammo.is(CompatIceandfire.dragonsteel_fire_bullet.get())) {
            return ParticleTypes.FLAME;
        }
        if (CompatIceandfire.dragonsteel_ice_bullet != null && CompatIceandfire.dragonsteel_ice_bullet.isPresent()
                && ammo.is(CompatIceandfire.dragonsteel_ice_bullet.get())) {
            return ParticleTypes.SNOWFLAKE;
        }
        if (CompatIceandfire.dragonsteel_lightning_bullet != null && CompatIceandfire.dragonsteel_lightning_bullet.isPresent()
                && ammo.is(CompatIceandfire.dragonsteel_lightning_bullet.get())) {
            return ParticleTypes.ELECTRIC_SPARK;
        }
        return null;
    }

    @Override
    protected boolean shouldDespawnOnHit(HitResult hit) {
        if (hit.getType() == HitResult.Type.ENTITY) {
            return piercedEntityIds != null && piercedEntityIds.size() > getPierce();
        }
        return true;
    }

    private boolean invokeDelegateHitEntity(EntityHitResult result) {
        if (!(effectDelegate instanceof DuskfallBulletDelegate delegate)) {
            return false;
        }
        syncEffectDelegate();
        boolean handled = delegate.gwrexpansions$onDuskfallHitEntity(result);
        if (handled) {
            setRemainingFireTicks(effectDelegate.getRemainingFireTicks());
        }
        return handled;
    }

    private boolean invokeDelegateHitBlock(BlockHitResult result) {
        if (!(effectDelegate instanceof DuskfallBulletDelegate delegate)) {
            return false;
        }
        syncEffectDelegate();
        boolean handled = delegate.gwrexpansions$onDuskfallHitBlock(result);
        if (handled) {
            setRemainingFireTicks(effectDelegate.getRemainingFireTicks());
        }
        return handled;
    }

    private void syncEffectDelegate() {
        if (effectDelegate == null) {
            return;
        }
        effectDelegate.setOwner(getOwner());
        effectDelegate.setItem(getItemRaw());
        effectDelegate.setDamage(getDamage());
        effectDelegate.setWaterInertia(getWaterInertia());
        effectDelegate.setKnockbackStrength(getKnockbackStrength());
        effectDelegate.setHeadshotMultiplier(getHeadshotMultiplier());
        effectDelegate.setDeltaMovement(getDeltaMovement());
        effectDelegate.setRemainingFireTicks(getRemainingFireTicks());
        effectDelegate.getPersistentData().merge(getPersistentData());
        effectDelegate.moveTo(getX(), getY(), getZ(), getYRot(), getXRot());
    }
}
