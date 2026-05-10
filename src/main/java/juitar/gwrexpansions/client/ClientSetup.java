package juitar.gwrexpansions.client;

import juitar.gwrexpansions.CompatModids;
import juitar.gwrexpansions.GWRexpansions;
import juitar.gwrexpansions.client.model.coin;
import juitar.gwrexpansions.client.render.CoinEntityRenderer;
import juitar.gwrexpansions.client.render.HudRenderHandler;
import juitar.gwrexpansions.client.render.MagneticPinRenderer;
import juitar.gwrexpansions.client.render.MeatHookRenderer;
import juitar.gwrexpansions.client.render.ObsidianCoreRenderer;
import juitar.gwrexpansions.client.render.ObsidianLauncherHudRenderer;
import juitar.gwrexpansions.client.render.SupershotgunHudRenderer;
import juitar.gwrexpansions.client.gui.CoinCounterOverlay;
import juitar.gwrexpansions.config.ClientConfig;
import juitar.gwrexpansions.registry.GWREEntities;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = GWRexpansions.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientSetup {

    @SubscribeEvent
    public static void init(final FMLClientSetupEvent event) {
        // 客户端初始化代码
        
        // 注册HUD渲染处理器
        event.enqueueWork(() -> {
            MinecraftForge.EVENT_BUS.register(new HudRenderHandler());
            // 注册超级霰弹枪HUD渲染器
            MinecraftForge.EVENT_BUS.register(new SupershotgunHudRenderer());
            // 注册黑曜石发射器HUD渲染器
            if(ModList.get().isLoaded(CompatModids.BOMD)) {
                MinecraftForge.EVENT_BUS.register(new ObsidianLauncherHudRenderer());

                // 注册狱锻之轮硬币计数器UI渲染器
                MinecraftForge.EVENT_BUS.register(new CoinCounterOverlay());
            }
        });
    }

    @SubscribeEvent
    public static void registerEntityRenders(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(GWREEntities.SLIME_BULLET.get(), ThrownItemRenderer::new);
        // 注册肉钩渲染器
        event.registerEntityRenderer(GWREEntities.MEAT_HOOK.get(), MeatHookRenderer::new);
        // 注册其他实体渲染器
        if(ModList.get().isLoaded(CompatModids.CATACLYSM)) {
            event.registerEntityRenderer(GWREEntities.LAVAPOWER_BULLET.get(), ThrownItemRenderer::new);
            event.registerEntityRenderer(GWREEntities.CURSIUM_BULLET.get(), ThrownItemRenderer::new);
            event.registerEntityRenderer(GWREEntities.IGNITIUM_BULLET.get(), ThrownItemRenderer::new);
            event.registerEntityRenderer(GWREEntities.TIDAL_BULLET.get(), ThrownItemRenderer::new);
        }
        if(ModList.get().isLoaded(CompatModids.ICEANDFIRE)) {
            event.registerEntityRenderer(GWREEntities.DRAGONSTEEL_ICE_BULLET.get(), ThrownItemRenderer::new);
            event.registerEntityRenderer(GWREEntities.DRAGONSTEEL_FIRE_BULLET.get(), ThrownItemRenderer::new);
            event.registerEntityRenderer(GWREEntities.DRAGONSTEEL_LIGHTNING_BULLET.get(), ThrownItemRenderer::new);
        }
        if(ModList.get().isLoaded(CompatModids.BOMD)) {
            event.registerEntityRenderer(GWREEntities.BUD.get(), ThrownItemRenderer::new);
            event.registerEntityRenderer(GWREEntities.OBSIDIAN_CORE.get(), ObsidianCoreRenderer::new);
            event.registerEntityRenderer(GWREEntities.COIN.get(), CoinEntityRenderer::new);
            event.registerEntityRenderer(GWREEntities.SPORE.get(), context -> new net.minecraft.client.renderer.entity.ThrownItemRenderer<>(context, 0.5F, true));

        }
        if(ModList.get().isLoaded(CompatModids.MEETYOURFIGHT)) {
            ClientSetupMYF.registerEntityRenderers(event);
        }
        if(ModList.get().isLoaded(CompatModids.ALEXSCAVES)) {
            event.registerEntityRenderer(GWREEntities.MAGNETIC_PIN.get(), MagneticPinRenderer::new);
            event.registerEntityRenderer(GWREEntities.MAGNETIC_BULLET.get(), ThrownItemRenderer::new);
        }
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        // 注册硬币模型层
        event.registerLayerDefinition(coin.LAYER_LOCATION, coin::createBodyLayer);
    }


} 
