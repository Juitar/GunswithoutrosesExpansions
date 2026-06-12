package juitar.gwrexpansions.event;

import com.github.L_Ender.cataclysm.entity.effect.Sandstorm_Entity;
import com.github.L_Ender.cataclysm.entity.projectile.Sandstorm_Projectile;
import com.github.L_Ender.cataclysm.entity.projectile.Storm_Serpent_Entity;
import com.github.L_Ender.cataclysm.init.ModEffect;
import juitar.gwrexpansions.item.cataclysm.CeraunusBurstItem;
import juitar.gwrexpansions.item.cataclysm.HarbingerRaycasterItem;
import juitar.gwrexpansions.item.cataclysm.RemnantFangshotItem;
import lykrast.gunswithoutroses.entity.BulletEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class CataclysmBulletEventHandler {
    private CataclysmBulletEventHandler() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRemnantFangshotStormNativeDamage(LivingHurtEvent event) {
        Entity direct = event.getSource().getDirectEntity();
        boolean directDashStorm = direct instanceof Sandstorm_Entity storm
                && storm.getPersistentData().getBoolean(RemnantFangshotItem.DASH_STORM_TAG);
        if (directDashStorm && direct.getPersistentData().getBoolean(RemnantFangshotItem.DASH_CUSTOM_DAMAGE_TAG)) {
            return;
        }
        if (!directDashStorm && !event.getSource().is(DamageTypes.MAGIC)) {
            return;
        }

        LivingEntity target = event.getEntity();
        boolean fromFangshotStorm = !target.level().getEntitiesOfClass(Sandstorm_Entity.class,
                target.getBoundingBox().inflate(0.4D),
                storm -> storm.getPersistentData().getBoolean(RemnantFangshotItem.DASH_STORM_TAG)).isEmpty();
        if (directDashStorm || fromFangshotStorm) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRemnantFangshotDashCurse(MobEffectEvent.Applicable event) {
        MobEffectInstance effect = event.getEffectInstance();
        if (effect.getEffect() != ModEffect.EFFECTCURSE_OF_DESERT.get()) {
            return;
        }

        if (event.getEntity() instanceof Player player && RemnantFangshotItem.isDashing(player)) {
            event.setResult(Event.Result.DENY);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        if (event.getProjectile() instanceof Sandstorm_Projectile sandstorm
                && sandstorm.getPersistentData().getBoolean(RemnantFangshotItem.SANDSTORM_SHOT_TAG)) {
            handleRemnantFangshotSandstormImpact(event, sandstorm);
        }
    }

    private static void handleRemnantFangshotSandstormImpact(ProjectileImpactEvent event,
            Sandstorm_Projectile sandstorm) {
        if (!(event.getRayTraceResult() instanceof EntityHitResult entityHit)) {
            sandstorm.discard();
            event.setImpactResult(ProjectileImpactEvent.ImpactResult.STOP_AT_CURRENT_NO_DAMAGE);
            return;
        }

        Entity owner = sandstorm.getOwner();
        Entity target = entityHit.getEntity();
        if (owner == null || target == owner || target.isPassengerOfSameVehicle(owner)) {
            event.setImpactResult(ProjectileImpactEvent.ImpactResult.SKIP_ENTITY);
            return;
        }

        if (target instanceof LivingEntity livingTarget) {
            CompoundTag data = sandstorm.getPersistentData();
            int invulnerableTime = target.invulnerableTime;
            target.invulnerableTime = 0;
            boolean damaged = target.hurt(sandstorm.level().damageSources().indirectMagic(sandstorm, owner),
                    (float) data.getDouble(RemnantFangshotItem.SANDSTORM_DAMAGE_TAG));
            if (!damaged) {
                target.invulnerableTime = invulnerableTime;
            }

            if (owner instanceof Player player) {
                ItemStack fangshot = RemnantFangshotItem.findHeldFangshot(player);
                if (!fangshot.isEmpty()) {
                    RemnantFangshotItem.onBulletHit(fangshot, player, livingTarget,
                            data.getInt(RemnantFangshotItem.SANDSTORM_SHOT_ID_TAG));
                }
            }
        }

        sandstorm.discard();
        event.setImpactResult(ProjectileImpactEvent.ImpactResult.STOP_AT_CURRENT_NO_DAMAGE);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getSource().getDirectEntity() instanceof Storm_Serpent_Entity serpent) {
            CeraunusBurstItem.onStormSerpentHit(serpent, event.getEntity());
        }

        if (!(event.getSource().getDirectEntity() instanceof BulletEntity bullet)) {
            return;
        }

        CompoundTag bulletData = bullet.getPersistentData();
        if (!bulletData.getBoolean(RemnantFangshotItem.BULLET_TAG)) {
            return;
        }

        Entity shooter = event.getSource().getEntity();
        if (shooter instanceof Player player) {
            ItemStack fangshot = RemnantFangshotItem.findHeldFangshot(player);
            if (!fangshot.isEmpty()) {
                RemnantFangshotItem.onBulletHit(fangshot, player, event.getEntity(),
                        bulletData.getInt(RemnantFangshotItem.BULLET_SHOT_ID_TAG));
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onRemnantFangshotDashHurt(LivingHurtEvent event) {
        if (event.getEntity() instanceof Player player && RemnantFangshotItem.isDashing(player)) {
            event.setAmount(event.getAmount() * RemnantFangshotItem.getDashIncomingDamageMultiplier());
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            event.getServer().getAllLevels().forEach(level -> {
                CeraunusBurstItem.tickScheduledCombos(level);
                HarbingerRaycasterItem.tickScheduledMissiles(level);
            });
        }
    }
}
