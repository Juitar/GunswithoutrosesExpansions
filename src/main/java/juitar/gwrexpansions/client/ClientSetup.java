package juitar.gwrexpansions.client;

import juitar.gwrexpansions.GWRexpansions;
import juitar.gwrexpansions.client.render.HudRenderHandler;
import juitar.gwrexpansions.client.render.MeatHookRenderer;
import juitar.gwrexpansions.client.render.SupershotgunHudRenderer;
import juitar.gwrexpansions.registry.GWREEntities;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
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
        });
    }

    @SubscribeEvent
    public static void registerEntityRenders(EntityRenderersEvent.RegisterRenderers event) {
        // 注册肉钩渲染器
        event.registerEntityRenderer(GWREEntities.MEAT_HOOK.get(), MeatHookRenderer::new);
        
        // 注册其他实体渲染器
    }
} 