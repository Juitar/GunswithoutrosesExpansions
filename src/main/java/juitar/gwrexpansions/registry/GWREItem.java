package juitar.gwrexpansions.registry;

import juitar.gwrexpansions.CompatModids;
import juitar.gwrexpansions.GWRexpanded;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;
import java.util.ArrayList;
import java.util.function.Supplier;

public class GWREItem {
    public static final DeferredRegister<Item> REG = DeferredRegister.create(ForgeRegistries.ITEMS, GWRexpanded.MODID);
    private static List<RegistryObject<? extends Item>> orderedItemsCreative = new ArrayList<>();

    public static void addToCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if(event.getTabKey().location().equals(new ResourceLocation(CompatModids.GWR,CompatModids.GWR))){
            orderedItemsCreative.forEach(event::accept);
        }
    }
    static{
        VanillaItem.registerItems();

    }

    public static Item.Properties defP(){
        return new Item.Properties();
    }
    public static Item.Properties noStack(){
        return new Item.Properties().stacksTo(1);
    }
    public static <I extends Item> RegistryObject<I>  initItem(Supplier<I> item, String itemName){
        REG.register(itemName, item);
        RegistryObject<I> regItem = RegistryObject.create(GWRexpanded.resource(itemName),ForgeRegistries.ITEMS);
        orderedItemsCreative.add(regItem);
        return regItem;
    }
}
