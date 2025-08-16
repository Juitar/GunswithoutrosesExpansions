package juitar.gwrexpansions.registry;

import juitar.gwrexpansions.GWRexpansions;
import juitar.gwrexpansions.effect.AimedEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * GWR扩展模组的药水效果注册类
 */
public class GWREEffects {
    public static final DeferredRegister<MobEffect> REG = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, GWRexpansions.MODID);
    
    /**
     * Aimed效果 - 标记被Hellforge击中的目标
     */
    public static final RegistryObject<MobEffect> AIMED = REG.register("aimed", AimedEffect::new);
}
