package juitar.gwrexpansions.client;

import juitar.gwrexpansions.CompatModids;
import juitar.gwrexpansions.config.ClientConfig;
import juitar.gwrexpansions.item.BOMD.Skullcrusher;
import juitar.gwrexpansions.registry.GWRESounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;

public class SkullcrusherIdleSoundClient {
    private static IdleSound currentSound;

    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || !shouldPlay(player)) {
            stop(mc);
            return;
        }

        if (currentSound == null || currentSound.isStopped()) {
            currentSound = new IdleSound(player);
            mc.getSoundManager().play(currentSound);
        }
    }

    private static boolean shouldPlay(LocalPlayer player) {
        return ModList.get().isLoaded(CompatModids.BOMD)
                && ClientConfig.getBoolean(ClientConfig.INSTANCE.skullcrusherIdleSoundEnabled, true)
                && !getHeldSkullcrusher(player).isEmpty();
    }

    private static void stop(Minecraft mc) {
        if (currentSound != null) {
            currentSound.stopSound();
            mc.getSoundManager().stop(currentSound);
            currentSound = null;
        }
    }

    private static ItemStack getHeldSkullcrusher(LocalPlayer player) {
        if (player.getMainHandItem().getItem() instanceof Skullcrusher) {
            return player.getMainHandItem();
        }
        if (player.getOffhandItem().getItem() instanceof Skullcrusher) {
            return player.getOffhandItem();
        }
        return ItemStack.EMPTY;
    }

    private static class IdleSound extends AbstractTickableSoundInstance {
        private final LocalPlayer player;

        private IdleSound(LocalPlayer player) {
            super(GWRESounds.skullcrusher_idle.get(), SoundSource.PLAYERS, RandomSource.create());
            this.player = player;
            this.looping = true;
            this.delay = 0;
            this.volume = 0.55F;
            this.pitch = 1.0F;
            this.relative = true;
        }

        @Override
        public void tick() {
            if (player.isRemoved() || !shouldPlay(player)) {
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
