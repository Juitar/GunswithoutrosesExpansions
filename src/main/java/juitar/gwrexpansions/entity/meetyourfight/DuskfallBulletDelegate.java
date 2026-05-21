package juitar.gwrexpansions.entity.meetyourfight;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

import javax.annotation.Nullable;

public interface DuskfallBulletDelegate {
    default boolean gwrexpansions$onDuskfallHitEntity(EntityHitResult result) {
        return false;
    }

    default boolean gwrexpansions$onDuskfallHitBlock(BlockHitResult result) {
        return false;
    }

    @Nullable
    default ParticleOptions gwrexpansions$getDuskfallTrailParticle() {
        return null;
    }
}
