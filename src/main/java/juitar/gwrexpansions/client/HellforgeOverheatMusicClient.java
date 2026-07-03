package juitar.gwrexpansions.client;

import juitar.gwrexpansions.CompatModids;
import juitar.gwrexpansions.item.BOMD.Hellforge;
import juitar.gwrexpansions.config.ClientConfig;
import juitar.gwrexpansions.registry.GWRESounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;

public class HellforgeOverheatMusicClient {
    private static final int FADE_OUT_TICKS = 60;
    private static final float TARGET_VOLUME = 0.9F;

    private static HeatingMusicSound currentSound;
    private static boolean wasOverheated;
    private static int musicStartDelay;

    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        if (!ModList.get().isLoaded(CompatModids.BOMD)) {
            stop(mc);
            wasOverheated = false;
            musicStartDelay = 0;
            return;
        }

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
                if (currentSound == null || currentSound.isStopped()) {
                    musicStartDelay = ClientConfig.getBoolean(ClientConfig.INSTANCE.hellforgeOverheatMusicEnabled, true) ? 16 : 0;
                }
                if (ClientConfig.getBoolean(ClientConfig.INSTANCE.hellforgeOverheatVoiceEnabled, true)) {
                    player.level().playLocalSound(player.getX(), player.getY(), player.getZ(),
                            GWRESounds.HELLFORGE_REVOLVER_OVERHEAT.get(), SoundSource.PLAYERS, 2.5F, 1.0F, false);
                }
            }
            if (!ClientConfig.getBoolean(ClientConfig.INSTANCE.hellforgeOverheatMusicEnabled, true)) {
                stop(mc);
                return;
            }
            if (currentSound != null && !currentSound.isStopped()) {
                currentSound.resume(player);
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
            if (currentSound != null && !currentSound.isStopped()) {
                currentSound.beginFadeOut();
            } else {
                stop(mc);
            }
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
        private LocalPlayer player;
        private int fadeOutTicks = -1;

        private HeatingMusicSound(LocalPlayer player) {
            super(GWRESounds.HELLFORGE_REVOLVER_HEATING_MUSIC.get(), SoundSource.PLAYERS, RandomSource.create());
            this.player = player;
            this.looping = true;
            this.delay = 0;
            this.volume = TARGET_VOLUME;
            this.pitch = 1.0F;
            this.relative = true;
        }

        @Override
        public void tick() {
            if (player == null
                    || player.isRemoved()
                    || !ClientConfig.getBoolean(ClientConfig.INSTANCE.hellforgeOverheatMusicEnabled, true)) {
                stopSound();
                return;
            }
            this.x = player.getX();
            this.y = player.getY();
            this.z = player.getZ();

            if (fadeOutTicks >= 0) {
                this.volume = TARGET_VOLUME * (fadeOutTicks / (float)FADE_OUT_TICKS);
                if (fadeOutTicks == 0) {
                    stopSound();
                    return;
                }
                fadeOutTicks--;
                return;
            }

            this.volume = TARGET_VOLUME;
        }

        private void beginFadeOut() {
            if (fadeOutTicks < 0) {
                fadeOutTicks = FADE_OUT_TICKS;
            }
        }

        private void resume(LocalPlayer player) {
            this.player = player;
            this.fadeOutTicks = -1;
            this.volume = TARGET_VOLUME;
        }

        private void stopSound() {
            this.stop();
        }
    }
}
