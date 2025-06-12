package juitar.gwrexpansions.registry;

import juitar.gwrexpansions.GWRexpansions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class GWRESounds {
    public static final DeferredRegister<SoundEvent> REG = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, GWRexpansions.MODID);

    // 超级霰弹枪声音
    public static final RegistryObject<SoundEvent> supershotgun = initSound("item.supershotgun.shoot");
    public static final RegistryObject<SoundEvent> skullcrusher = initSound("item.skullcrusher.shoot");
    // 肉钩声音
    public static final RegistryObject<SoundEvent> meat_hook_launch = initSound("meat_hook_launch");
    public static final RegistryObject<SoundEvent> meat_hook_hit = initSound("meat_hook_hit");
    public static final RegistryObject<SoundEvent> meat_hook_miss = initSound("meat_hook_miss");
    public static final RegistryObject<SoundEvent> meat_hook_pull = initSound("meat_hook_pull");
    public static RegistryObject<SoundEvent> initSound(String name) {
        return REG.register(name, () -> SoundEvent.createVariableRangeEvent(GWRexpansions.resource(name)));
    }
}
