package juitar.gwrexpansions.client;

import com.github.L_Ender.cataclysm.client.render.entity.Abyss_Mine_Renderer;
import com.github.L_Ender.cataclysm.client.render.entity.Abyss_Orb_Renderer;
import juitar.gwrexpansions.GWRexpansions;
import juitar.gwrexpansions.client.render.CeraunusBurstHudRenderer;
import juitar.gwrexpansions.client.render.CursiumSniperHudRenderer;
import juitar.gwrexpansions.client.render.HarbingerRaycasterHudRenderer;
import juitar.gwrexpansions.client.render.RemnantFangshotHudRenderer;
import juitar.gwrexpansions.client.render.TidalAbyssBlastPortalRenderer;
import juitar.gwrexpansions.client.render.TidalPistolHudRenderer;
import juitar.gwrexpansions.client.render.TidalPortalBeamRenderer;
import juitar.gwrexpansions.client.render.TidalRiftRenderer;
import juitar.gwrexpansions.item.cataclysm.IgnitiumGatlingItem;
import juitar.gwrexpansions.registry.CompatCataclysm;
import juitar.gwrexpansions.registry.GWREEntities;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;

final class ClientSetupCataclysm {
    private ClientSetupCataclysm() {
    }

    static void registerHudRenderers() {
        MinecraftForge.EVENT_BUS.register(new RemnantFangshotHudRenderer());
        MinecraftForge.EVENT_BUS.register(new CeraunusBurstHudRenderer());
        MinecraftForge.EVENT_BUS.register(new HarbingerRaycasterHudRenderer());
        MinecraftForge.EVENT_BUS.register(new TidalPistolHudRenderer());
        MinecraftForge.EVENT_BUS.register(new CursiumSniperHudRenderer());
        ItemProperties.register(CompatCataclysm.ignitium_gatling.get(), GWRexpansions.resource("blue_fire"),
                (stack, level, entity, seed) -> IgnitiumGatlingItem.isBlueFireActive(stack) ? 1.0F : 0.0F);
    }

    static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(GWREEntities.LAVAPOWER_BULLET.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(GWREEntities.CURSIUM_BULLET.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(GWREEntities.IGNITIUM_BULLET.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(GWREEntities.TIDAL_BULLET.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(GWREEntities.TIDAL_RIFT.get(), TidalRiftRenderer::new);
        event.registerEntityRenderer(GWREEntities.TIDAL_ABYSS_ORB.get(), context -> new Abyss_Orb_Renderer(context));
        event.registerEntityRenderer(GWREEntities.TIDAL_ABYSS_MINE.get(), context -> new Abyss_Mine_Renderer(context));
        event.registerEntityRenderer(GWREEntities.TIDAL_ABYSS_BLAST_PORTAL.get(), TidalAbyssBlastPortalRenderer::new);
        event.registerEntityRenderer(GWREEntities.TIDAL_PORTAL_BEAM.get(), TidalPortalBeamRenderer::new);
        event.registerEntityRenderer(GWREEntities.CERAUNUS_WATER_BULLET.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(GWREEntities.CERAUNUS_STORM_BULLET.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(GWREEntities.CERAUNUS_LIGHTNING_BULLET.get(), ThrownItemRenderer::new);
    }
}
