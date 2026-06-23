package juitar.gwrexpansions.client;

import juitar.gwrexpansions.item.BOMD.Hellforge;
import juitar.gwrexpansions.config.ClientConfig;
import juitar.gwrexpansions.registry.GWRESounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

public class HellforgeOverheatMusicClient {
    private static HeatingMusicSound currentSound;
    private static boolean wasOverheated;
    private static int musicStartDelay;

    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            stop(mc);
            wasOverheated = false;
            musicStartDelay = 0;
            return;
        }

        boolean shouldPlay = getHeldHellforge(player).getOrCreateTag().getInt(Hellforge.NBT_COIN_OVERHEAT_TIMER) > 0;
        if (shouldPlay) {
            if (!wasOverheated) {
                wasOverheated = true;
                musicStartDelay = ClientConfig.getBoolean(ClientConfig.INSTANCE.hellforgeOverheatMusicEnabled, true) ? 16 : 0;
                if (ClientConfig.getBoolean(ClientConfig.INSTANCE.hellforgeOverheatVoiceEnabled, true)) {
                    player.level().playLocalSound(player.getX(), player.getY(), player.getZ(),
                            GWRESounds.HELLFORGE_REVOLVER_OVERHEAT.get(), SoundSource.PLAYERS, 2.5F, 1.0F, false);
                }
            }
            if (!ClientConfig.getBoolean(ClientConfig.INSTANCE.hellforgeOverheatMusicEnabled, true)) {
                stop(mc);
                return;
            }
            if ((currentSound == null || currentSound.isStopped()) && musicStartDelay > 0) {
                musicStartDelay--;
                return;
            }
            if (currentSound == null || currentSound.isStopped()) {
                currentSound = new HeatingMusicSound(player);
                mc.getSoundManager().play(currentSound);
            }
        } else {
            wasOverheated = false;
            musicStartDelay = 0;
            stop(mc);
        }
    }

    private static void stop(Minecraft mc) {
        if (currentSound != null) {
            currentSound.stopSound();
            mc.getSoundManager().stop(currentSound);
            currentSound = null;
        }
    }

    private static ItemStack getHeldHellforge(LocalPlayer player) {
        if (player.getMainHandItem().getItem() instanceof Hellforge) {
            return player.getMainHandItem();
        }
        if (player.getOffhandItem().getItem() instanceof Hellforge) {
            return player.getOffhandItem();
        }
        return ItemStack.EMPTY;
    }

    private static class HeatingMusicSound extends AbstractTickableSoundInstance {
        private final LocalPlayer player;

        private HeatingMusicSound(LocalPlayer player) {
            super(GWRESounds.HELLFORGE_REVOLVER_HEATING_MUSIC.get(), SoundSource.PLAYERS, RandomSource.create());
            this.player = player;
            this.looping = true;
            this.delay = 0;
            this.volume = 0.9F;
            this.pitch = 1.0F;
            this.relative = true;
        }

        @Override
        public void tick() {
            if (player.isRemoved()
                    || !ClientConfig.getBoolean(ClientConfig.INSTANCE.hellforgeOverheatMusicEnabled, true)
                    || getHeldHellforge(player).getOrCreateTag().getInt(Hellforge.NBT_COIN_OVERHEAT_TIMER) <= 0) {
                stopSound();
                return;
            }
            this.x = player.getX();
            this.y = player.getY();
            this.z = player.getZ();
        }

        private void stopSound() {
            this.stop();
        }
    }
}