package juitar.gwrexpansions.client;

import juitar.gwrexpansions.GWRexpansions;
import juitar.gwrexpansions.client.render.SkullcrusherHudRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * 客户端事件处理类
 * 用于注册客户端事件如渲染器等
 */
@Mod.EventBusSubscriber(modid = GWRexpansions.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientEvents {
    
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // 注册HUD渲染器
        event.enqueueWork(() -> {
            MinecraftForge.EVENT_BUS.register(new SkullcrusherHudRenderer());
        });
    }
} 