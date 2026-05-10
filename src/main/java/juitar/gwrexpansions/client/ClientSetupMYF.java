package juitar.gwrexpansions.client;

import juitar.gwrexpansions.client.render.DuskRoseSpiritRenderer;
import juitar.gwrexpansions.registry.GWREEntities;
import lykrast.meetyourfight.renderer.SwampMineRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraftforge.client.event.EntityRenderersEvent;

final class ClientSetupMYF {
    private ClientSetupMYF() {
    }

    static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(GWREEntities.DUSKFALL_PIERCING_BULLET.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(GWREEntities.DUSK_ROSE_SPIRIT.get(), DuskRoseSpiritRenderer::new);
        event.registerEntityRenderer(GWREEntities.MIRECALLER_SWAMP_MINE.get(), SwampMineRenderer::new);
    }
}
