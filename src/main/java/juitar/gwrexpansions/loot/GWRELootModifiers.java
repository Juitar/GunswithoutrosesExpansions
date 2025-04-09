package juitar.gwrexpansions.loot;

import com.mojang.serialization.Codec;
import juitar.gwrexpansions.GWRexpansions;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * 处理GWR扩展模组的掉落修改器
 * 该类负责注册全局掉落修改器
 * 
 * 本模组包含以下掉落修改器：
 * 1. leviathan_modifier - 当玩家击杀利维坦(cataclysm:the_leviathan)时，添加掉落物
 */
public class GWRELootModifiers {
    
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIER_SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, GWRexpansions.MODID);

    public static final RegistryObject<Codec<leviathan_modifier>> LEVIATHAN = 
            LOOT_MODIFIER_SERIALIZERS.register("leviathan", leviathan_modifier.CODEC);
    
    /**
     * 注册全局掉落修改器到Forge事件总线
     * 
     * @param eventBus Forge模组事件总线
     */
    public static void register(IEventBus eventBus) {
        GWRexpansions.LOG.info("Registering GWR Expansions loot modifiers");
        LOOT_MODIFIER_SERIALIZERS.register(eventBus);
    }
    
    /**
     * 创建一个属于本模组的ResourceLocation
     * @param path 资源路径
     * @return 完整的ResourceLocation
     */
    public static ResourceLocation location(String path) {
        return new ResourceLocation(GWRexpansions.MODID, path);
    }
}