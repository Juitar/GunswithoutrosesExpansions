package juitar.gwrexpansions;

import juitar.gwrexpansions.registry.GWREItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(GWRexpanded.MODID)
public class GWRexpanded {
    public static final String MODID = "gwrexpansions";
    public static final Logger LOG = LogManager.getLogger();

    public GWRexpanded() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        GWREItem.REG.register(eventBus);
        eventBus.addListener(GWREItem::addToCreativeTab);
    }
    public static ResourceLocation resource(String path) {
        return new ResourceLocation(MODID, path);
    }
}
