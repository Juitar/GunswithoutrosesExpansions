package juitar.gwrexpansions.client;

import juitar.gwrexpansions.registry.GWRESounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundSource;

public class CoinHitFeedbackClient {
    private static int feedbackTicks;
    private static int currentHits;
    private static int chainTimer;
    private static int flashTicks;

    public static void onCoinHit(int hits, int timer) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            return;
        }

        currentHits = hits;
        chainTimer = timer;
        feedbackTicks = Math.min(7, 3 + Math.max(0, hits / 2));
        flashTicks = Math.min(10, 4 + hits);
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

        if (feedbackTicks > 0) {
            float strength = Math.min(1.5F, 0.2F + currentHits * 0.08F);
            float yaw = (player.getRandom().nextFloat() - 0.5F) * strength;
            float pitch = (player.getRandom().nextFloat() - 0.5F) * strength * 0.45F;
            player.turn(yaw, pitch);
            feedbackTicks--;
        }

        if (chainTimer > 0) {
            chainTimer--;
        }
        if (flashTicks > 0) {
            flashTicks--;
        }
        if (chainTimer <= 0) {
            currentHits = 0;
        }
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
}
