package juitar.gwrexpansions.entity.cataclysm;

import com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.The_Leviathan.Abyss_Orb_Entity;
import juitar.gwrexpansions.registry.GWREEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.Field;

public class TidalAbyssOrbEntity extends Abyss_Orb_Entity {
    private static final EntityDataAccessor<Float> SPEED_MULTIPLIER = SynchedEntityData.defineId(TidalAbyssOrbEntity.class,
            EntityDataSerializers.FLOAT);
    private static final double VANILLA_TRACKING_SPEED = 0.075D;
    private static final Field FINAL_TARGET_FIELD = findFinalTargetField();

    public TidalAbyssOrbEntity(EntityType<? extends TidalAbyssOrbEntity> type, Level level) {
        super(type, level);
    }

    public TidalAbyssOrbEntity(LivingEntity shooter, double dx, double dy, double dz, Level level, float damage,
            LivingEntity target, double speedMultiplier) {
        super(GWREEntities.TIDAL_ABYSS_ORB.get(), shooter.getX(), shooter.getY(), shooter.getZ(), dx, dy, dz, level);
        setOwner(shooter);
        setDamage(damage);
        setFinalTarget(target);
        setRot(shooter.getYRot(), shooter.getXRot());
        setTrackingSpeedMultiplier(speedMultiplier);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(SPEED_MULTIPLIER, 1.0F);
    }

    public void setTrackingSpeedMultiplier(double multiplier) {
        this.entityData.set(SPEED_MULTIPLIER, (float) Mth.clamp(multiplier, 0.1D, 10.0D));
    }

    public double getTrackingSpeedMultiplier() {
        return this.entityData.get(SPEED_MULTIPLIER);
    }

    @Override
    public void tick() {
        super.tick();
        if (!isRemoved() && getTracking()) {
            Vec3 steering = new Vec3(this.xPower, this.yPower, this.zPower);
            if (steering.lengthSqr() < 1.0E-6D) {
                steering = getDeltaMovement();
            }
            if (steering.lengthSqr() > 1.0E-6D) {
                setDeltaMovement(steering.normalize().scale(VANILLA_TRACKING_SPEED * getTrackingSpeedMultiplier()));
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("TidalSpeedMultiplier", (float) getTrackingSpeedMultiplier());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("TidalSpeedMultiplier")) {
            setTrackingSpeedMultiplier(tag.getFloat("TidalSpeedMultiplier"));
        }
    }

    private void setFinalTarget(LivingEntity target) {
        if (FINAL_TARGET_FIELD != null) {
            try {
                FINAL_TARGET_FIELD.set(this, target);
            } catch (IllegalAccessException ignored) {
            }
        }
    }

    private static Field findFinalTargetField() {
        try {
            Field field = Abyss_Orb_Entity.class.getDeclaredField("finalTarget");
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException exception) {
            return null;
        }
    }
}
