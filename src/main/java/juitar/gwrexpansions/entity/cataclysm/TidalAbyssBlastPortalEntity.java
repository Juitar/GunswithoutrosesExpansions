package juitar.gwrexpansions.entity.cataclysm;

import com.github.L_Ender.cataclysm.entity.AnimationMonster.BossMonsters.The_Leviathan.Abyss_Blast_Portal_Entity;
import juitar.gwrexpansions.registry.GWREEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.UUID;

public class TidalAbyssBlastPortalEntity extends Abyss_Blast_Portal_Entity {
    private static final String WARMUP_TAG = "Warmup";
    private static final String LIFE_TAG = "Life";
    private static final String LASER_DURATION_TAG = "LaserDuration";
    private static final String OWNER_TAG = "Owner";
    private static final String DIR_X_TAG = "DirX";
    private static final String DIR_Y_TAG = "DirY";
    private static final String DIR_Z_TAG = "DirZ";

    private int warmupDelayTicks;
    private int lifeTicks = 260;
    private int laserDuration = 160;
    private boolean sentAttackEvent;
    private boolean clientSideAttackStarted;
    private UUID casterUuid;
    private Vec3 beamDirection = Vec3.ZERO;

    public TidalAbyssBlastPortalEntity(EntityType<? extends Entity> type, Level level) {
        super(type, level);
    }

    public void configure(LivingEntity caster, Vec3 aimedPoint, int warmup, int duration, float damage, float hpDamage) {
        setCaster(caster);
        this.casterUuid = caster.getUUID();
        Vec3 direction = aimedPoint.subtract(position());
        this.beamDirection = direction.lengthSqr() < 0.001D ? caster.getViewVector(1.0F) : direction.normalize();
        this.warmupDelayTicks = warmup;
        this.laserDuration = Math.max(1, duration);
        this.lifeTicks = this.laserDuration + 100;
        setDamage(Math.max(0.0F, damage));
        setHpDamage(Math.max(0.0F, hpDamage));
        setYRot((float) (Math.atan2(this.beamDirection.z, this.beamDirection.x) * 180.0D / Math.PI) + 90.0F);
    }

    @Override
    public void tick() {
        super.baseTick();
        this.prevactivateProgress = this.activateProgress;
        if (isActivate() && this.activateProgress > 0.0F) {
            this.activateProgress -= 1.0F;
        }

        if (level().isClientSide) {
            clientTick();
            return;
        }

        if (--this.warmupDelayTicks < 0) {
            if (this.warmupDelayTicks == -10 && isActivate()) {
                setActivate(false);
            }

            if (this.warmupDelayTicks == -22) {
                spawnSafeBlast();
            }

            if (!this.sentAttackEvent) {
                level().broadcastEntityEvent(this, (byte) 4);
                this.clientSideAttackStarted = true;
                this.sentAttackEvent = true;
            }

            if (--this.lifeTicks < 0) {
                discard();
            }
        }
    }

    private void clientTick() {
        if (!this.clientSideAttackStarted) {
            return;
        }

        this.lifeTicks--;
        if (!isActivate() && this.activateProgress < 10.0F) {
            this.activateProgress += 1.0F;
        }
        if (this.lifeTicks == 14) {
            setActivate(true);
        }
    }

    private void spawnSafeBlast() {
        LivingEntity caster = getResolvedCaster();
        TidalPortalBeamEntity blast = new TidalPortalBeamEntity(GWREEntities.TIDAL_PORTAL_BEAM.get(), level());
        blast.setPos(getX(), getY(), getZ());
        if (caster != null) {
            blast.configure(caster, this.beamDirection, this.laserDuration, getDamage(), getHpDamage());
        } else {
            blast.configureFromPortal(this.beamDirection, this.laserDuration, getDamage(), getHpDamage());
        }
        level().addFreshEntity(blast);
    }

    @Nullable
    private LivingEntity getResolvedCaster() {
        LivingEntity caster = getCaster();
        if (caster != null) {
            return caster;
        }
        if (this.casterUuid != null && level() instanceof ServerLevel serverLevel) {
            Entity entity = serverLevel.getEntity(this.casterUuid);
            if (entity instanceof LivingEntity living) {
                setCaster(living);
                return living;
            }
        }
        return null;
    }

    @Override
    public void handleEntityEvent(byte id) {
        super.handleEntityEvent(id);
        if (id == 4) {
            this.clientSideAttackStarted = true;
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.warmupDelayTicks = tag.getInt(WARMUP_TAG);
        this.lifeTicks = tag.contains(LIFE_TAG) ? tag.getInt(LIFE_TAG) : this.lifeTicks;
        this.laserDuration = tag.contains(LASER_DURATION_TAG) ? tag.getInt(LASER_DURATION_TAG) : this.laserDuration;
        if (tag.hasUUID(OWNER_TAG)) {
            this.casterUuid = tag.getUUID(OWNER_TAG);
        }
        this.beamDirection = new Vec3(tag.getDouble(DIR_X_TAG), tag.getDouble(DIR_Y_TAG), tag.getDouble(DIR_Z_TAG));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt(WARMUP_TAG, this.warmupDelayTicks);
        tag.putInt(LIFE_TAG, this.lifeTicks);
        tag.putInt(LASER_DURATION_TAG, this.laserDuration);
        if (this.casterUuid != null) {
            tag.putUUID(OWNER_TAG, this.casterUuid);
        }
        tag.putDouble(DIR_X_TAG, this.beamDirection.x);
        tag.putDouble(DIR_Y_TAG, this.beamDirection.y);
        tag.putDouble(DIR_Z_TAG, this.beamDirection.z);
    }

    @Override
    public PushReaction getPistonPushReaction() {
        return PushReaction.IGNORE;
    }
}
