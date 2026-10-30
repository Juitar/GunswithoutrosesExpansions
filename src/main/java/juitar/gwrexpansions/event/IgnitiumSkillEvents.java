package juitar.gwrexpansions.event;

import com.github.L_Ender.cataclysm.entity.projectile.Ignis_Abyss_Fireball_Entity;
import com.github.L_Ender.cataclysm.entity.projectile.Ignis_Fireball_Entity;
import juitar.gwrexpansions.GWRexpansions;
import juitar.gwrexpansions.config.GWREConfig;
import juitar.gwrexpansions.entity.cataclysm.IgnitiumFlameSigilEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.*;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import javax.annotation.Nullable;

@Mod.EventBusSubscriber(modid = GWRexpansions.MODID)
public final class IgnitiumSkillEvents {
    private IgnitiumSkillEvents() {}

    @SubscribeEvent
    public static void impact(ProjectileImpactEvent event) {
        if (!(event.getProjectile() instanceof Ignis_Abyss_Fireball_Entity projectile)
                || !projectile.getPersistentData().getBoolean("IgnitiumAbyssShot")) return;
        if (event.getRayTraceResult() instanceof EntityHitResult hit
                && (hit.getEntity() == projectile.getOwner() || hit.getEntity() instanceof Ignis_Fireball_Entity
                || hit.getEntity() instanceof Ignis_Abyss_Fireball_Entity)) {
            event.setImpactResult(ProjectileImpactEvent.ImpactResult.SKIP_ENTITY);
            return;
        }
        // Only this gun's abyss shots change behaviour; boss projectiles keep their bounces.
        event.setImpactResult(ProjectileImpactEvent.ImpactResult.STOP_AT_CURRENT_NO_DAMAGE);
        if (!(projectile.level() instanceof ServerLevel level) || projectile.isRemoved()
                || !(projectile.getOwner() instanceof LivingEntity owner)) return;
        Vec3 impact = event.getRayTraceResult().getLocation();
        abyssImpact(level, projectile, owner, impact, projectile.getPersistentData().getBoolean("IgnitiumSoulShot"));
        projectile.discard();
    }

    public static void abyssImpact(ServerLevel level, Entity source, @Nullable LivingEntity owner, Vec3 impact, boolean soul) {
        var config = GWREConfig.GATLING.Ignitium;
        IgnitiumFlameSigilEntity.blast(level, source, owner, impact, config.abyssImpactRadius.get().floatValue(),
                (soul ? config.soulAbyssImpactDamage : config.abyssImpactDamage).get().floatValue(), soul);
        var ground = level.clip(new ClipContext(impact.add(0, 0.5, 0), impact.add(0, -32, 0),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, source));
        if (ground.getType() == HitResult.Type.BLOCK) {
            var sigil = new IgnitiumFlameSigilEntity(level, ground.getLocation().add(0, 0.03, 0), owner, soul);
            level.addFreshEntity(sigil);
            level.playSound(null, sigil.blockPosition(), SoundEvents.BLAZE_BURN, SoundSource.PLAYERS, 0.5F, 1.0F);
        }
    }
}
