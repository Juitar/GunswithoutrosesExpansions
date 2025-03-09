package juitar.gwrexpansions.registry;

import juitar.gwrexpansions.CompatModids;
import juitar.gwrexpansions.GWRexpansions;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryObject;
import org.checkerframework.checker.units.qual.C;

import java.util.List;
import java.util.ArrayList;
import java.util.function.Supplier;

public class GWREItem {
    public static final DeferredRegister<Item> REG = DeferredRegister.create(ForgeRegistries.ITEMS, GWRexpansions.MODID);
    private static List<RegistryObject<? extends Item>> orderedItemsCreative = new ArrayList<>();

    public static void makeCreativeTab(RegisterEvent event) {
        event.register(Registries.CREATIVE_MODE_TAB, helper -> {
            helper.register(ResourceKey.create(Registries.CREATIVE_MODE_TAB, GWRexpansions.resource("gwrexpansions")),
                    CreativeModeTab.builder().title(Component.translatable("itemGroup.gwrexpansions")).icon(() -> new ItemStack(VanillaItem.netherite_shotgun.get()))
                            .displayItems((parameters, output) -> orderedItemsCreative.forEach(i -> output.accept(i.get()))).build());
        });
    }
    
    static{
        VanillaItem.registerItems();
        if(ModList.get().isLoaded(CompatModids.CATACLYSM)) CompatCataclysm.registerItems();
        if(ModList.get().isLoaded(CompatModids.ICEANDFIRE)) CompatIceandfire.registerItems();
    }

    public static Item.Properties defP(){
        return new Item.Properties();
    }
    public static Item.Properties noStack(){
        return new Item.Properties().stacksTo(1);
    }
    public static <I extends Item> RegistryObject<I>  initItem(Supplier<I> item, String itemName){
        REG.register(itemName, item);
        RegistryObject<I> regItem = RegistryObject.create(GWRexpansions.resource(itemName),ForgeRegistries.ITEMS);
        orderedItemsCreative.add(regItem);
        return regItem;
    }
}
