package juitar.gwrexpansions.event;

import java.util.UUID;

import juitar.gwrexpansions.GWRexpansions;
import juitar.gwrexpansions.config.GWREConfig;
import juitar.gwrexpansions.item.vanilla.Supershotgun;
import lykrast.gunswithoutroses.entity.BulletEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = GWRexpansions.MODID)
public final class MeatHookKillRewardHandler {
    private static final String HOOK_OWNER_UUID_TAG = "GWREMeatHookOwnerUuid";
    private static final String HOOK_REWARD_EXPIRES_TAG = "GWREMeatHookRewardExpires";

    private MeatHookKillRewardHandler() {
    }

    public static void markHookRewardTarget(LivingEntity target, Entity owner) {
        if (target.level().isClientSide || !(owner instanceof ServerPlayer player)) {
            return;
        }

        CompoundTag data = target.getPersistentData();
        data.putUUID(HOOK_OWNER_UUID_TAG, player.getUUID());
        data.putLong(HOOK_REWARD_EXPIRES_TAG,
                target.level().getGameTime() + supershotgunConfig().meatHookKillRewardWindowTicks.get());
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

    private static GWREConfig.SupershotgunConfig supershotgunConfig() {
        return GWREConfig.SHOTGUN.Supershotgun;
    }
}
