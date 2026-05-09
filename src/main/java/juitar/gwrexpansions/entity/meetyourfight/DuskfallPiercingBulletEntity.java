package juitar.gwrexpansions.entity.meetyourfight;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import juitar.gwrexpansions.registry.GWREEntities;
import lykrast.gunswithoutroses.entity.BulletEntity;
import lykrast.gunswithoutroses.entity.PiercingBulletEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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
        boolean handled = invokeDelegateHit("onHitEntity", EntityHitResult.class, result);
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
        if (!invokeDelegateHit("onHitBlock", BlockHitResult.class, hit)) {
            super.onHitBlock(hit);
        }
    }

    @Override
    protected boolean shouldDespawnOnHit(HitResult hit) {
        if (hit.getType() == HitResult.Type.ENTITY) {
            return piercedEntityIds != null && piercedEntityIds.size() > getPierce();
        }
        return true;
    }

    private boolean invokeDelegateHit(String methodName, Class<?> parameterType, Object hit) {
        if (effectDelegate == null) {
            return false;
        }
        syncEffectDelegate();
        try {
            Method method = findHitMethod(effectDelegate.getClass(), methodName, parameterType);
            method.invoke(effectDelegate, hit);
            setRemainingFireTicks(effectDelegate.getRemainingFireTicks());
            return true;
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | LinkageError ignored) {
            return false;
        }
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

    private static Method findHitMethod(Class<?> clazz, String methodName, Class<?> parameterType) throws NoSuchMethodException {
        Class<?> current = clazz;
        while (current != null) {
            try {
                Method method = current.getDeclaredMethod(methodName, parameterType);
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException ignored) {
                current = current.getSuperclass();
            }
        }
        throw new NoSuchMethodException(methodName);
    }
}
