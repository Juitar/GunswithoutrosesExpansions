package juitar.gwrexpansions.entity.vanilla;

import juitar.gwrexpansions.registry.GWREEntities;
import lykrast.gunswithoutroses.entity.BulletEntity;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

public class RedstoneBulletEntity extends BulletEntity {
    private static final ParticleOptions REDSTONE_TRAIL =
            new DustParticleOptions(new Vector3f(1.0F, 0.05F, 0.02F), 1.0F);

    public RedstoneBulletEntity(EntityType<? extends BulletEntity> type, Level level) {
        super(type, level);
    }

    public RedstoneBulletEntity(Level level, LivingEntity shooter) {
        super(GWREEntities.REDSTONE_BULLET.get(), shooter, level);
    }

    @Override
    protected ParticleOptions getTrailParticle() {
        return REDSTONE_TRAIL;
    }
}
