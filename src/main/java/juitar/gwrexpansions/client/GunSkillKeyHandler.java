package juitar.gwrexpansions.client;

import com.mojang.blaze3d.platform.InputConstants;
import juitar.gwrexpansions.GWRexpansions;
import juitar.gwrexpansions.network.GWRENetwork;
import juitar.gwrexpansions.network.GunSkillPacket;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = GWRexpansions.MODID, value = Dist.CLIENT)
public class GunSkillKeyHandler {
    private static boolean wasGunSkillDown;

    public static final KeyMapping GUN_SKILL = new KeyMapping(
        "key.gwrexpansions.gun_skill",
        KeyConflictContext.IN_GAME,
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_R,
        "key.categories.gwrexpansions"
    );

    @Mod.EventBusSubscriber(modid = GWRexpansions.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class Registration {
        @SubscribeEvent
        public static void registerKeys(RegisterKeyMappingsEvent event) {
            event.register(GUN_SKILL);
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        boolean connected = mc.player != null && mc.level != null && mc.getConnection() != null;
        boolean down = connected && mc.screen == null && GUN_SKILL.isDown();
        boolean clicked = false;
        while (GUN_SKILL.consumeClick()) clicked = true;
        // A quick tap can begin and end between client ticks. consumeClick retains it,
        // while isDown still supplies the hold/release edges used by other guns.
        boolean pressed = connected && mc.screen == null && !wasGunSkillDown && (down || clicked);
        if (pressed) GWRENetwork.CHANNEL.sendToServer(new GunSkillPacket(true));
        if (connected && !down && (wasGunSkillDown || pressed)) {
            GWRENetwork.CHANNEL.sendToServer(new GunSkillPacket(false));
        }
        wasGunSkillDown = down;

        CoinHitFeedbackClient.tick();
        SuperShotgunFeedbackClient.tick();
        HellforgeOverheatMusicClient.tick();
        SkullcrusherIdleSoundClient.tick();
        DestinyIdleSoundClient.tick();
    }
}
