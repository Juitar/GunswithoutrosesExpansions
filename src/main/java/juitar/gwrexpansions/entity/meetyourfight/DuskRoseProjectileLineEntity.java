package juitar.gwrexpansions.entity.meetyourfight;

import juitar.gwrexpansions.config.GWREConfig;
import lykrast.meetyourfight.entity.ProjectileLineEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class DuskRoseProjectileLineEntity extends ProjectileLineEntity {
    public DuskRoseProjectileLineEntity(Level level, LivingEntity owner) {
        super(level, owner);
    }

    @Override
    protected float getDamage(LivingEntity owner, Entity target) {
        return GWREConfig.BURSTGUN.duskfallEclipse.spiritAttackDamage.get().floatValue();
    }
}
