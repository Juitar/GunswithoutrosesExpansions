package juitar.gwrexpansions.event;

import juitar.gwrexpansions.GWRexpansions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = GWRexpansions.MODID)
public final class MeatHookFallProtectionHandler {
    private static final String FALL_PROTECTION_TICKS_TAG = "GWREMeatHookFallProtectionTicks";
    private static final int FALL_PROTECTION_TICKS = 120;

    private MeatHookFallProtectionHandler() {
    }

    public static void grant(Player player) {
        if (player.level().isClientSide) {
            return;
        }

        CompoundTag data = player.getPersistentData();
        data.putInt(FALL_PROTECTION_TICKS_TAG,
                Math.max(data.getInt(FALL_PROTECTION_TICKS_TAG), FALL_PROTECTION_TICKS));
        player.fallDistance = 0.0F;
    }

    @SubscribeEvent
    public static void onLivingFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide) {
            return;
        }

        if (hasProtection(player)) {
            event.setCanceled(true);
            player.fallDistance = 0.0F;
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) {
            return;
        }

        CompoundTag data = event.player.getPersistentData();
        int ticks = data.getInt(FALL_PROTECTION_TICKS_TAG);
        if (ticks <= 0) {
            data.remove(FALL_PROTECTION_TICKS_TAG);
            return;
        }

        event.player.fallDistance = 0.0F;
        if (ticks == 1) {
            data.remove(FALL_PROTECTION_TICKS_TAG);
        } else {
            data.putInt(FALL_PROTECTION_TICKS_TAG, ticks - 1);
        }
    }

    private static boolean hasProtection(Player player) {
        return player.getPersistentData().getInt(FALL_PROTECTION_TICKS_TAG) > 0;
    }
}
