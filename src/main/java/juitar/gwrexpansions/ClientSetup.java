package juitar.gwrexpansions;

import juitar.gwrexpansions.registry.GWREEntities;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = GWRexpansions.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {
    @SubscribeEvent
    public static void registerEntityRenders(EntityRenderersEvent.RegisterRenderers event) {
        //Same renderer as potions
        event.registerEntityRenderer(GWREEntities.SLIME_BULLET.get(), ThrownItemRenderer::new);
        if (ModList.get().isLoaded(CompatModids.CATACLYSM)) {
            event.registerEntityRenderer(GWREEntities.IGNITIUM_BULLET.get(), ThrownItemRenderer::new);
            event.registerEntityRenderer(GWREEntities.LAVAPOWER_BULLET.get(), ThrownItemRenderer::new);
            event.registerEntityRenderer(GWREEntities.CURSIUM_BULLET.get(), ThrownItemRenderer::new);
        }
        if (ModList.get().isLoaded(CompatModids.ICEANDFIRE)) {
            event.registerEntityRenderer(GWREEntities.DRAGONSTEEL_ICE_BULLET.get(), ThrownItemRenderer::new);
            event.registerEntityRenderer(GWREEntities.DRAGONSTEEL_FIRE_BULLET.get(), ThrownItemRenderer::new);
            event.registerEntityRenderer(GWREEntities.DRAGONSTEEL_LIGHTNING_BULLET.get(), ThrownItemRenderer::new);
        }
    }
}