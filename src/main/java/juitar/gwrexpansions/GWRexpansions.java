package juitar.gwrexpansions;
import juitar.gwrexpansions.config.ClientConfig;
import juitar.gwrexpansions.config.GWREConfig;
import juitar.gwrexpansions.loot.GWRELootModifiers;
import juitar.gwrexpansions.registry.GWREEffects;
import juitar.gwrexpansions.registry.GWRECataclysmEnchantments;
import juitar.gwrexpansions.registry.GWREEntities;
import juitar.gwrexpansions.registry.GWREItems;
import juitar.gwrexpansions.registry.GWRESounds;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(GWRexpansions.MODID)
public class GWRexpansions {
    public static final String MODID = "gwrexpansions";
    public static final Logger LOG = LogManager.getLogger();

    public GWRexpansions() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        GWREConfig.register();

        // 只在客户端注册客户端配置和配置界面
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            ClientConfig.register();
            juitar.gwrexpansions.client.gui.GWREConfigScreen.register();
        });

        GWREItems.REG.register(eventBus);
        GWRECataclysmEnchantments.register(eventBus);
        GWRESounds.REG.register(eventBus);
        GWREEntities.REG.register(eventBus);
        GWREEffects.REG.register(eventBus);
        GWRELootModifiers.register(eventBus);
        eventBus.addListener(GWREItems::makeCreativeTab);
        juitar.gwrexpansions.advancement.GWRECriteria.register();

        if (ModList.get().isLoaded(CompatModids.CATACLYSM)) {
            MinecraftForge.EVENT_BUS.register(juitar.gwrexpansions.event.CataclysmBulletEventHandler.class);
            MinecraftForge.EVENT_BUS.register(juitar.gwrexpansions.event.CataclysmCombatEventHandler.CommonEvents.class);
        }
    }

    public static ResourceLocation resource(String path) {
        return new ResourceLocation(MODID, path);
    }
}
