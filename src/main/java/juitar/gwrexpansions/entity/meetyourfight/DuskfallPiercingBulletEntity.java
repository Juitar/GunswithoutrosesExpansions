package juitar.gwrexpansions.entity.meetyourfight;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import juitar.gwrexpansions.CompatModids;
import juitar.gwrexpansions.GWRexpansions;
import juitar.gwrexpansions.registry.GWREEntities;
import lykrast.gunswithoutroses.entity.BulletEntity;
import lykrast.gunswithoutroses.entity.PiercingBulletEntity;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

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
        if (isAmmo(ammo, GWRexpansions.MODID, "lavapower_bullet")) {
            return ParticleTypes.LAVA;
        }
        if (isAmmo(ammo, GWRexpansions.MODID, "ignitium_bullet")) {
            return ParticleTypes.FLAME;
        }
        if (isAmmo(ammo, GWRexpansions.MODID, "tidal_bullet")) {
            return ParticleTypes.REVERSE_PORTAL;
        }
        if (isAmmo(ammo, GWRexpansions.MODID, "cursium_bullet")) {
            ParticleOptions particle = getCataclysmParticle("phantom_wing_flame");
            return particle != null ? particle : ParticleTypes.FLAME;
        }
        if (isAmmo(ammo, GWRexpansions.MODID, "dragonsteel_fire_bullet")) {
            return ParticleTypes.FLAME;
        }
        if (isAmmo(ammo, GWRexpansions.MODID, "dragonsteel_ice_bullet")) {
            return ParticleTypes.SNOWFLAKE;
        }
        if (isAmmo(ammo, GWRexpansions.MODID, "dragonsteel_lightning_bullet")) {
            return ParticleTypes.ELECTRIC_SPARK;
        }
        return null;
    }

    private static boolean isAmmo(ItemStack stack, String namespace, String path) {
        return new ResourceLocation(namespace, path).equals(ForgeRegistries.ITEMS.getKey(stack.getItem()));
    }

    @Nullable
    private static ParticleOptions getCataclysmParticle(String name) {
        if (!ModList.get().isLoaded(CompatModids.CATACLYSM)) {
            return null;
        }

        ParticleType<?> particle = ForgeRegistries.PARTICLE_TYPES.getValue(new ResourceLocation(CompatModids.CATACLYSM, name));
        return particle instanceof ParticleOptions options ? options : null;
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
