package juitar.gwrexpansions;

import juitar.gwrexpansions.registry.GWREItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegistryObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

@Mod(GWRexpansions.MODID)
public class GWRexpansions {
    public static final String MODID = "gwrexpansions";
    public static final Logger LOG = LogManager.getLogger();
    private static List<RegistryObject<? extends Item>> orderedItemsCreative = new ArrayList<>();

    public GWRexpansions() {
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        GWREItem.REG.register(eventBus);
        eventBus.addListener(GWREItem::makeCreativeTab);
    }

    public static ResourceLocation resource(String path) {
        return new ResourceLocation(MODID, path);
    }
}
