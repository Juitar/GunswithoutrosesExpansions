package juitar.gwrexpansions.event;

import juitar.gwrexpansions.config.GWREConfig;
import juitar.gwrexpansions.entity.meetyourfight.DuskRoseSpiritEntity;
import juitar.gwrexpansions.item.meetyourfight.DuskfallEclipseBlasterItem;
import lykrast.gunswithoutroses.entity.BulletEntity;
import lykrast.meetyourfight.entity.ProjectileLineEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class MYFCombatEventHandler {
    private static final ThreadLocal<Boolean> PROCESSING = ThreadLocal.withInitial(() -> false);

    private MYFCombatEventHandler() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        if (event.getProjectile() instanceof ProjectileLineEntity projectile
                && event.getRayTraceResult() instanceof EntityHitResult entityHit
                && isFriendlyDuskfallSpiritProjectileHit(projectile, entityHit.getEntity())) {
            event.setImpactResult(ProjectileImpactEvent.ImpactResult.SKIP_ENTITY);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onLivingHurt(LivingHurtEvent event) {
        if (PROCESSING.get() || event.isCanceled()) {
            return;
        }

        try {
            PROCESSING.set(true);

            if (cancelFriendlyDuskfallSpiritDamage(event)) {
                return;
            }

            if (redirectDuskfallSpiritDamageToOwner(event)) {
                return;
            }

            applyDuskfallSpiritModifiers(event);
            rememberDuskfallTarget(event);
        } finally {
            PROCESSING.set(false);
        }
    }

    private static void rememberDuskfallTarget(LivingHurtEvent event) {
        if (event.getSource().getDirectEntity() instanceof BulletEntity bullet
                && bullet.getPersistentData().getBoolean(DuskfallEclipseBlasterItem.SHOT_TAG)
                && event.getSource().getEntity() instanceof Player player) {
            player.getPersistentData().putUUID(DuskfallEclipseBlasterItem.LAST_TARGET_TAG, event.getEntity().getUUID());
            player.getPersistentData().putLong(DuskfallEclipseBlasterItem.LAST_TARGET_TIME_TAG, event.getEntity().level().getGameTime());
        }
    }

    private static void applyDuskfallSpiritModifiers(LivingHurtEvent event) {
        Entity sourceEntity = event.getSource().getEntity();
        Entity directEntity = event.getSource().getDirectEntity();

        if (sourceEntity instanceof Player attacker
                && directEntity instanceof BulletEntity bullet
                && bullet.getPersistentData().getBoolean(DuskfallEclipseBlasterItem.SHOT_TAG)) {
            int spirits = DuskRoseSpiritEntity.countActiveFor(attacker);
            if (spirits > 0) {
                float multiplier = (float) (1.0D + spirits * GWREConfig.BURSTGUN.duskfallEclipse.damageBonusPerSpirit.get());
                event.setAmount(event.getAmount() * multiplier);
            }
        }

        if (event.getEntity() instanceof Player defender) {
            int spirits = DuskRoseSpiritEntity.countActiveFor(defender);
            if (spirits > 0) {
                double reduction = Math.min(0.95D, spirits * GWREConfig.BURSTGUN.duskfallEclipse.damageReductionPerSpirit.get());
                event.setAmount((float) (event.getAmount() * (1.0D - reduction)));
            }
        }
    }

    private static boolean cancelFriendlyDuskfallSpiritDamage(LivingHurtEvent event) {
        if (!(event.getSource().getDirectEntity() instanceof ProjectileLineEntity projectile)
                || !(projectile.getOwner() instanceof DuskRoseSpiritEntity)) {
            return false;
        }

        if (isFriendlyDuskfallSpiritProjectileHit(projectile, event.getEntity())) {
            event.setCanceled(true);
            return true;
        }

        return false;
    }

    private static boolean redirectDuskfallSpiritDamageToOwner(LivingHurtEvent event) {
        if (!(event.getSource().getDirectEntity() instanceof ProjectileLineEntity projectile)
                || !(projectile.getOwner() instanceof DuskRoseSpiritEntity spirit)) {
            return false;
        }

        Player owner = spirit.getOwnerPlayer();
        if (owner == null || !owner.isAlive() || event.getSource().getEntity() == owner) {
            return false;
        }

        int originalInvulnerableTime = event.getEntity().invulnerableTime;
        event.setCanceled(true);
        event.getEntity().invulnerableTime = 0;
        event.getEntity().hurt(event.getEntity().damageSources().mobProjectile(projectile, owner), event.getAmount());
        event.getEntity().invulnerableTime = Math.max(event.getEntity().invulnerableTime, originalInvulnerableTime);
        return true;
    }

    private static boolean isFriendlyDuskfallSpiritProjectileHit(ProjectileLineEntity projectile, Entity target) {
        if (!(projectile.getOwner() instanceof DuskRoseSpiritEntity spirit)) {
            return false;
        }

        if (target instanceof Player player && spirit.isOwnedBy(player)) {
            return true;
        }

        return target instanceof DuskRoseSpiritEntity otherSpirit && spirit.hasSameOwner(otherSpirit);
    }
}
