package juitar.gwrexpansions.client;

import juitar.gwrexpansions.GWRexpansions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = GWRexpansions.MODID, value = Dist.CLIENT)
public class SuperShotgunFeedbackClient {
    private static final int SHOCK_DURATION = 7;
    private static final int FOV_DURATION = 8;
    private static int shockTicks;
    private static int fovPunchTicks;
    private static float recoilDirection;

    public static void onShot() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            return;
        }

        shockTicks = SHOCK_DURATION;
        fovPunchTicks = FOV_DURATION;
        recoilDirection = player.getRandom().nextBoolean() ? 1.0F : -1.0F;
    }

    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            return;
        }

        if (shockTicks > 0) {
            float progress = shockTicks / (float)SHOCK_DURATION;
            float kick = progress * progress;
            float yaw = recoilDirection * 0.7F * kick + (player.getRandom().nextFloat() - 0.5F) * 0.55F * kick;
            float pitch = -2.15F * kick + (player.getRandom().nextFloat() - 0.5F) * 0.35F * kick;
            player.turn(yaw, pitch);
            shockTicks--;
        }
        if (fovPunchTicks > 0) {
            fovPunchTicks--;
        }
    }

    @SubscribeEvent
    public static void onComputeFov(ViewportEvent.ComputeFov event) {
        if (fovPunchTicks <= 0) {
            return;
        }

        float progress = fovPunchTicks / (float)FOV_DURATION;
        event.setFOV(event.getFOV() * (1.0D + 0.065D * progress * progress));
    }
}
