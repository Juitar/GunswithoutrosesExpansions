package juitar.gwrexpansions.entity.vanilla;

import juitar.gwrexpansions.registry.GWREEntities;
import lykrast.gunswithoutroses.entity.BulletEntity;
import lykrast.gunswithoutroses.entity.PiercingBulletEntity;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;

public class RedstonePiercingBulletEntity extends PiercingBulletEntity {
    private static final ParticleOptions REDSTONE_TRAIL =
            new DustParticleOptions(new Vector3f(1.0F, 0.05F, 0.02F), 1.0F);

    public RedstonePiercingBulletEntity(EntityType<? extends BulletEntity> type, Level level) {
        super(type, level);
    }

    public RedstonePiercingBulletEntity(Level level, LivingEntity shooter) {
        super(GWREEntities.REDSTONE_PIERCING_BULLET.get(), level);
        setOwner(shooter);
        moveTo(shooter.getX(), shooter.getEyeY() - 0.1D, shooter.getZ(), shooter.getYRot(), shooter.getXRot());
    }

    @Override
    protected ParticleOptions getTrailParticle() {
        return REDSTONE_TRAIL;
    }
}
