package juitar.gwrexpansions;


import juitar.gwrexpansions.config.GWREConfig;
import juitar.gwrexpansions.registry.GWREEntities;
import juitar.gwrexpansions.registry.GWREItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
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
        GWREItem.REG.register(eventBus);
        GWREEntities.REG.register(eventBus);
        eventBus.addListener(GWREItem::makeCreativeTab);
    }

    public static ResourceLocation resource(String path) {
        return new ResourceLocation(MODID, path);
    }
}
