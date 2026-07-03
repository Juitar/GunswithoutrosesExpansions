package juitar.gwrexpansions.event;

import java.util.UUID;

import juitar.gwrexpansions.GWRexpansions;
import juitar.gwrexpansions.config.GWREConfig;
import juitar.gwrexpansions.item.vanilla.Supershotgun;
import juitar.gwrexpansions.network.GWRENetwork;
import juitar.gwrexpansions.network.MeatHookMarkPacket;
import lykrast.gunswithoutroses.entity.BulletEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

@Mod.EventBusSubscriber(modid = GWRexpansions.MODID)
public final class MeatHookKillRewardHandler {
    private static final String HOOK_OWNER_UUID_TAG = "GWREMeatHookOwnerUuid";
    private static final String HOOK_REWARD_EXPIRES_TAG = "GWREMeatHookRewardExpires";
    private static final String HOOK_MARK_OWNER_UUID_TAG = "GWREMeatHookMarkOwnerUuid";
    private static final String HOOK_MARK_EXPIRES_TAG = "GWREMeatHookMarkExpires";
    private static final String HOOK_MARK_NEXT_SYNC_TAG = "GWREMeatHookMarkNextSync";
    private static final String HOOK_MARK_ORIGINAL_GLOWING_TAG = "GWREMeatHookOriginalGlowing";

    private MeatHookKillRewardHandler() {
    }

    public static void markHookRewardTarget(LivingEntity target, Entity owner) {
        if (target.level().isClientSide || !(owner instanceof ServerPlayer player)) {
            return;
        }

        CompoundTag data = target.getPersistentData();
        long gameTime = target.level().getGameTime();
        int markDuration = supershotgunConfig().meatHookMarkDurationTicks.get();
        boolean ownerChanged = !data.hasUUID(HOOK_MARK_OWNER_UUID_TAG)
                || !data.getUUID(HOOK_MARK_OWNER_UUID_TAG).equals(player.getUUID());
        data.putUUID(HOOK_OWNER_UUID_TAG, player.getUUID());
        data.putLong(HOOK_REWARD_EXPIRES_TAG,
                gameTime + supershotgunConfig().meatHookKillRewardWindowTicks.get());
        data.putUUID(HOOK_MARK_OWNER_UUID_TAG, player.getUUID());
        data.putLong(HOOK_MARK_EXPIRES_TAG,
                gameTime + markDuration);
        if (!data.contains(HOOK_MARK_ORIGINAL_GLOWING_TAG)) {
            data.putBoolean(HOOK_MARK_ORIGINAL_GLOWING_TAG, target.isCurrentlyGlowing());
        }
        target.setGlowingTag(true);

        if (ownerChanged || gameTime >= data.getLong(HOOK_MARK_NEXT_SYNC_TAG)) {
            syncMarkedTarget(target, markDuration, true);
            data.putLong(HOOK_MARK_NEXT_SYNC_TAG, gameTime + Math.max(10, markDuration / 3));
        }
    }

    public static boolean hasActiveMeatHookMark(LivingEntity target) {
        return getActiveMeatHookMarkOwner(target) != null;
    }

    public static boolean hasActiveMeatHookMark(LivingEntity target, @Nullable Entity owner) {
        if (!(owner instanceof ServerPlayer player)) {
            return false;
        }

        UUID markOwner = getActiveMeatHookMarkOwner(target);
        return markOwner != null && markOwner.equals(player.getUUID());
    }

    @Nullable
    private static UUID getActiveMeatHookMarkOwner(LivingEntity target) {
        CompoundTag data = target.getPersistentData();
        if (!data.hasUUID(HOOK_MARK_OWNER_UUID_TAG)) {
            return null;
        }

        if (target.level().getGameTime() > data.getLong(HOOK_MARK_EXPIRES_TAG)) {
            clearMarkedVisual(target);
            return null;
        }

        return data.getUUID(HOOK_MARK_OWNER_UUID_TAG);
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getSource().getDirectEntity() instanceof BulletEntity bullet)
                || !bullet.getPersistentData().getBoolean(Supershotgun.SUPER_SHOTGUN_SHOT_TAG)) {
            return;
        }

        ServerPlayer shooter = getBulletOwnerPlayer(bullet, event.getSource().getEntity());
        if (shooter == null || !hasActiveMeatHookMark(event.getEntity(), shooter)) {
            return;
        }

        double multiplier = supershotgunConfig().meatHookMarkedDamageMultiplier.get();
        if (multiplier != 1.0D) {
            event.setAmount((float) (event.getAmount() * multiplier));
        }
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity target = event.getEntity();
        CompoundTag data = target.getPersistentData();
        if (!data.hasUUID(HOOK_MARK_OWNER_UUID_TAG)) {
            return;
        }

        if (target.level().getGameTime() > data.getLong(HOOK_MARK_EXPIRES_TAG) || !target.isAlive()) {
            clearMarkedVisual(target);
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!supershotgunConfig().meatHookKillRewardEnabled.get()) {
            return;
        }

        LivingEntity target = event.getEntity();
        if (target.level().isClientSide) {
            return;
        }

        CompoundTag targetData = target.getPersistentData();
        if (!targetData.hasUUID(HOOK_OWNER_UUID_TAG)
                || target.level().getGameTime() > targetData.getLong(HOOK_REWARD_EXPIRES_TAG)) {
            clearHookRewardTarget(targetData);
            return;
        }

        if (!(event.getSource().getDirectEntity() instanceof BulletEntity bullet)
                || !bullet.getPersistentData().getBoolean(Supershotgun.SUPER_SHOTGUN_SHOT_TAG)) {
            return;
        }

        ServerPlayer killer = getBulletOwnerPlayer(bullet, event.getSource().getEntity());
        if (killer == null) {
            return;
        }

        UUID hookOwner = targetData.getUUID(HOOK_OWNER_UUID_TAG);
        if (!killer.getUUID().equals(hookOwner)) {
            return;
        }

        Supershotgun.resetHookCooldown(killer);
        grantConfiguredAbsorption(killer);
        clearHookRewardTarget(targetData);
    }

    private static ServerPlayer getBulletOwnerPlayer(BulletEntity bullet, Entity sourceEntity) {
        if (sourceEntity instanceof ServerPlayer player) {
            return player;
        }
        if (bullet.getOwner() instanceof ServerPlayer player) {
            return player;
        }
        return null;
    }

    private static void grantConfiguredAbsorption(ServerPlayer player) {
        float absorption = (float) (supershotgunConfig().meatHookKillRewardAbsorptionHearts.get() * 2.0D);
        if (absorption <= 0.0F || player.getAbsorptionAmount() >= absorption) {
            return;
        }
        player.setAbsorptionAmount(absorption);
    }

    private static void clearHookRewardTarget(CompoundTag data) {
        data.remove(HOOK_OWNER_UUID_TAG);
        data.remove(HOOK_REWARD_EXPIRES_TAG);
    }

    private static void clearHookMark(CompoundTag data) {
        data.remove(HOOK_MARK_OWNER_UUID_TAG);
        data.remove(HOOK_MARK_EXPIRES_TAG);
        data.remove(HOOK_MARK_NEXT_SYNC_TAG);
        data.remove(HOOK_MARK_ORIGINAL_GLOWING_TAG);
    }

    private static void syncMarkedTarget(LivingEntity target, int durationTicks, boolean marked) {
        GWRENetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> target),
                new MeatHookMarkPacket(target.getId(), durationTicks, marked));
    }

    private static void clearMarkedVisual(LivingEntity target) {
        CompoundTag data = target.getPersistentData();
        boolean originalGlowing = data.getBoolean(HOOK_MARK_ORIGINAL_GLOWING_TAG);
        target.setGlowingTag(originalGlowing);
        clearHookMark(data);
        syncMarkedTarget(target, 0, false);
    }

    private static GWREConfig.SupershotgunConfig supershotgunConfig() {
        return GWREConfig.SHOTGUN.Supershotgun;
    }
}
