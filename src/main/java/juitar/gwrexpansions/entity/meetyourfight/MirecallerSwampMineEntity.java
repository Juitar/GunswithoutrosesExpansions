package juitar.gwrexpansions.entity.meetyourfight;

import juitar.gwrexpansions.advancement.MYF.MirecallerMineBurstTrigger;
import juitar.gwrexpansions.config.GWREConfig;
import juitar.gwrexpansions.registry.GWREEntities;
import lykrast.meetyourfight.entity.SwampMineEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.UUID;

public class MirecallerSwampMineEntity extends SwampMineEntity {
    private static final String OWNER_TAG = "MirecallerOwner";

    @Nullable
    private UUID ownerUuid;

    public MirecallerSwampMineEntity(EntityType<? extends SwampMineEntity> type, Level level) {
        super(type, level);
    }

    public MirecallerSwampMineEntity(Level level, double x, double y, double z, LivingEntity owner) {
        super(GWREEntities.MIRECALLER_SWAMP_MINE.get(), level);
        setPos(x, y, z);
        setFuse(200);
        xo = x;
        yo = y;
        zo = z;
        ownerUuid = owner.getUUID();
    }

    @Override
    protected void explode() {
        Entity source = getOwnerEntity();
        if (source instanceof ServerPlayer player) {
            MirecallerMineBurstTrigger.beginMineExplosion(player);
            try {
                level().explode(player,
                        getX(), getY(0.0625D), getZ(),
                        GWREConfig.SHOTGUN.Mirecaller.mineExplosionPower.get().floatValue(),
                        Level.ExplosionInteraction.NONE);
            } finally {
                MirecallerMineBurstTrigger.endMineExplosion(player);
            }
            return;
        }

        level().explode(source == null ? this : source,
            getX(), getY(0.0625D), getZ(),
            GWREConfig.SHOTGUN.Mirecaller.mineExplosionPower.get().floatValue(),
            Level.ExplosionInteraction.NONE);
    }

    @Nullable
    private Entity getOwnerEntity() {
        if (ownerUuid == null || !(level() instanceof ServerLevel serverLevel)) {
            return null;
        }
        return serverLevel.getEntity(ownerUuid);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (ownerUuid != null) {
            tag.putUUID(OWNER_TAG, ownerUuid);
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        ownerUuid = tag.hasUUID(OWNER_TAG) ? tag.getUUID(OWNER_TAG) : null;
    }
}
