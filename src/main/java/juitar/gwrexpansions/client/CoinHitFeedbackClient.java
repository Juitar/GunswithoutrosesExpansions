package juitar.gwrexpansions.client;

import juitar.gwrexpansions.GWRexpansions;
import juitar.gwrexpansions.config.ClientConfig;
import juitar.gwrexpansions.registry.GWRESounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = GWRexpansions.MODID, value = Dist.CLIENT)
public class CoinHitFeedbackClient {
    private static int feedbackTicks;
    private static int currentHits;
    private static int chainTimer;
    private static int flashTicks;
    private static int clearSuppressTicks;
    private static int overheatTimer;
    private static int fovPunchTicks;
    private static int fovPunchDuration;
    private static float fovPunchStrength;
    private static float recoilDirection;

    public static void onCoinHit(int hits, int timer, int newOverheatTimer) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            return;
        }

        overheatTimer = Math.max(overheatTimer, newOverheatTimer);

        if (hits <= 0 || timer <= 0) {
            currentHits = 0;
            chainTimer = 0;
            feedbackTicks = 0;
            flashTicks = 0;
            fovPunchTicks = 0;
            clearSuppressTicks = 20;
            return;
        }

        clearSuppressTicks = 0;
        currentHits = hits;
        chainTimer = timer;
        flashTicks = ClientConfig.getBoolean(ClientConfig.INSTANCE.hellforgeCoinHitHudFlashEnabled, true) ? Math.min(10, 4 + hits) : 0;

        if (ClientConfig.getBoolean(ClientConfig.INSTANCE.hellforgeCoinHitShockEnabled, true)) {
            feedbackTicks = Math.min(9, 4 + Math.max(0, hits / 2));
            recoilDirection = player.getRandom().nextBoolean() ? 1.0F : -1.0F;
        } else {
            feedbackTicks = 0;
        }

        if (ClientConfig.getBoolean(ClientConfig.INSTANCE.hellforgeCoinHitFovPunchEnabled, true)) {
            float scale = getShockScale();
            fovPunchDuration = Math.min(9, 5 + hits / 2);
            fovPunchTicks = fovPunchDuration;
            fovPunchStrength = Math.min(0.07F, (0.018F + hits * 0.0045F) * scale);
        } else {
            fovPunchDuration = 0;
            fovPunchTicks = 0;
            fovPunchStrength = 0.0F;
        }

        float pitch = Math.min(1.8F, 0.95F + hits * 0.12F);
        player.level().playLocalSound(player.getX(), player.getY(), player.getZ(),
            GWRESounds.HELLFORGE_REVOLVER_COIN_HIT.get(), SoundSource.PLAYERS, 1.0F, pitch, false);
    }

    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            return;
        }

        if (feedbackTicks > 0 && ClientConfig.getBoolean(ClientConfig.INSTANCE.hellforgeCoinHitShockEnabled, true)) {
            float progress = feedbackTicks / (float)Math.max(1, Math.min(9, 4 + Math.max(0, currentHits / 2)));
            float strength = Math.min(2.2F, 0.34F + currentHits * 0.12F) * progress * progress * getShockScale();
            float yaw = recoilDirection * strength * 0.55F + (player.getRandom().nextFloat() - 0.5F) * strength * 0.35F;
            float pitch = -strength * 0.22F + (player.getRandom().nextFloat() - 0.5F) * strength * 0.18F;
            player.turn(yaw, pitch);
            feedbackTicks--;
        } else if (feedbackTicks > 0) {
            feedbackTicks = 0;
        }

        if (chainTimer > 0) {
            chainTimer--;
        }
        if (flashTicks > 0) {
            flashTicks--;
        }
        if (fovPunchTicks > 0) {
            fovPunchTicks--;
        }
        if (clearSuppressTicks > 0) {
            clearSuppressTicks--;
        }
        if (overheatTimer > 0) {
            overheatTimer--;
        }
        if (chainTimer <= 0) {
            currentHits = 0;
        }
    }

    @SubscribeEvent
    public static void onComputeFov(ViewportEvent.ComputeFov event) {
        if (fovPunchTicks <= 0 || fovPunchDuration <= 0
                || !ClientConfig.getBoolean(ClientConfig.INSTANCE.hellforgeCoinHitFovPunchEnabled, true)) {
            return;
        }
        float progress = fovPunchTicks / (float)fovPunchDuration;
        float punch = fovPunchStrength * progress * progress;
        event.setFOV(event.getFOV() * (1.0D + punch));
    }

    private static float getShockScale() {
        return Math.max(0.0F, ClientConfig.getInt(ClientConfig.INSTANCE.hellforgeCoinHitShockStrength, 100) / 100.0F);
    }

    public static int getCurrentHits() {
        return currentHits;
    }

    public static int getChainTimer() {
        return chainTimer;
    }

    public static int getFlashTicks() {
        return flashTicks;
    }

    public static int getOverheatTimer() {
        return overheatTimer;
    }

    public static boolean isClearSuppressed() {
        return clearSuppressTicks > 0;
    }
}