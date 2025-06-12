package juitar.gwrexpansions;
import juitar.gwrexpansions.config.GWREConfig;
import juitar.gwrexpansions.loot.GWRELootModifiers;
import juitar.gwrexpansions.registry.GWREEntities;
import juitar.gwrexpansions.registry.GWREItems;
import juitar.gwrexpansions.registry.GWRESounds;
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
        GWREItems.REG.register(eventBus);
        GWRESounds.REG.register(eventBus);
        GWREEntities.REG.register(eventBus);
        GWRELootModifiers.register(eventBus);
        eventBus.addListener(GWREItems::makeCreativeTab);
    }

    public static ResourceLocation resource(String path) {
        return new ResourceLocation(MODID, path);
    }
}