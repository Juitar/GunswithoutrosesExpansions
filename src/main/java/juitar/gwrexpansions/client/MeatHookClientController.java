package juitar.gwrexpansions.client;

import java.util.Comparator;
import java.util.List;

import juitar.gwrexpansions.GWRexpansions;
import juitar.gwrexpansions.entity.vanilla.MeatHookEntity;
import juitar.gwrexpansions.network.GWRENetwork;
import juitar.gwrexpansions.network.MeatHookInputPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = GWRexpansions.MODID, value = Dist.CLIENT)
public final class MeatHookClientController {
    private static int activeHookId = -1;
    private static float lastYaw;
    private static float lastPitch;

    private MeatHookClientController() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null || minecraft.isPaused()) {
            clearActiveHook();
            return;
        }

        MeatHookEntity hook = findLocalPullingHook(minecraft.level, minecraft.player);
        if (hook == null) {
            clearActiveHook();
            return;
        }

        Entity target = minecraft.level.getEntity(hook.getHookedEntityId());
        if (target == null || !target.isAlive()) {
            clearActiveHook();
            return;
        }

        if (activeHookId != hook.getId()) {
            activeHookId = hook.getId();
            lastYaw = minecraft.player.getYRot();
            lastPitch = minecraft.player.getXRot();
            return;
        }

        Player player = minecraft.player;
        float deltaYaw = Mth.wrapDegrees(player.getYRot() - lastYaw);
        float deltaPitch = player.getXRot() - lastPitch;
        GWRENetwork.CHANNEL.sendToServer(new MeatHookInputPacket(deltaYaw, deltaPitch));
        lastYaw = player.getYRot();
        lastPitch = player.getXRot();
    }

    private static MeatHookEntity findLocalPullingHook(ClientLevel level, Player player) {
        List<MeatHookEntity> hooks = level.getEntitiesOfClass(MeatHookEntity.class,
                player.getBoundingBox().inflate(64.0D),
                hook -> hook.isPulling() && hook.getHookOwnerId() == player.getId());

        return hooks.stream()
                .min(Comparator.comparingDouble(player::distanceToSqr))
                .orElse(null);
    }

    private static void clearActiveHook() {
        activeHookId = -1;
        lastYaw = 0.0F;
        lastPitch = 0.0F;
    }
}
