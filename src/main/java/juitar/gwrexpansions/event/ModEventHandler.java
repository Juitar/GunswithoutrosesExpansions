package juitar.gwrexpansions.event;

import juitar.gwrexpansions.CompatModids;
import juitar.gwrexpansions.GWRexpansions;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = GWRexpansions.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEventHandler {
    @SubscribeEvent
    public static void registerEntityAttributes(EntityAttributeCreationEvent event) {
        if (ModList.get().isLoaded(CompatModids.MEETYOURFIGHT)) {
            MYFModEventHandler.registerEntityAttributes(event);
        }
    }
}
