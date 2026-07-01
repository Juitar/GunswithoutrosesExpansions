package juitar.gwrexpansions.client;

import juitar.gwrexpansions.GWRexpansions;
import juitar.gwrexpansions.item.BOMD.Skullcrusher;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = GWRexpansions.MODID, value = Dist.CLIENT)
public class SkullcrusherMovementInputClient {
    private static final float VANILLA_USE_MOVEMENT_MULTIPLIER = 0.2F;

    @SubscribeEvent
    public static void onMovementInputUpdate(MovementInputUpdateEvent event) {
        if (!event.getEntity().isUsingItem()
                || event.getEntity().isPassenger()
                || !(event.getEntity().getUseItem().getItem() instanceof Skullcrusher)) {
            return;
        }

        event.getInput().leftImpulse /= VANILLA_USE_MOVEMENT_MULTIPLIER;
        event.getInput().forwardImpulse /= VANILLA_USE_MOVEMENT_MULTIPLIER;
    }
}
