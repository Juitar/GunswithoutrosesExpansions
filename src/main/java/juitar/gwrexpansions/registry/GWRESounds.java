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
    public static final RegistryObject<SoundEvent> skullcrusher_idle = initSound("item.skullcrusher.idle");
    public static final RegistryObject<SoundEvent> destiny_idle = initSound("item.destiny_seven.idle");
    public static final RegistryObject<SoundEvent> destiny_pull = initSound("item.destiny_seven.pull");
    public static final RegistryObject<SoundEvent> destiny_shot = initSound("item.destiny_seven.shot");
    // 肉钩声音
    public static final RegistryObject<SoundEvent> meat_hook_launch = initSound("meat_hook.launch");
    public static final RegistryObject<SoundEvent> meat_hook_hit = initSound("meat_hook.hit");
    public static final RegistryObject<SoundEvent> meat_hook_miss = initSound("meat_hook.miss");
    public static final RegistryObject<SoundEvent> meat_hook_pull = initSound("meat_hook.pull");
    public static final RegistryObject<SoundEvent> meat_hook_ready = initSound("meat_hook.ready");
    // 黑曜石发射器声音
    public static final RegistryObject<SoundEvent> OBSIDIAN_LAUNCHER_FIRE = initSound("item.obsidian_launcher.shoot");
    public static final RegistryObject<SoundEvent> OBSIDIAN_CORE_HIT  = initSound("obsidian_core.hit");
    public static final RegistryObject<SoundEvent> OBSIDIAN_CORE_MISS = initSound("obsidian_core.miss");
    public static final RegistryObject<SoundEvent> OBSIDIAN_PULL = initSound("item.obsidian_launcher.pull");
    public static final RegistryObject<SoundEvent> OBSIDIAN_CORE_RELOAD = initSound("item.obsidian_launcher.reload");

    // 狱锻之轮声音
    public static final RegistryObject<SoundEvent> HELLFORGE_REVOLVER_SHOOT = initSound("item.hellforge_revolver.shoot");
    public static final RegistryObject<SoundEvent> HELLFORGE_REVOLVER_RELOAD = initSound("item.hellforge_revolver.reload");
    public static final RegistryObject<SoundEvent> HELLFORGE_REVOLVER_COIN_FLIP = initSound("item.hellforge_revolver.coin_flip");
    public static final RegistryObject<SoundEvent> HELLFORGE_REVOLVER_COIN_HIT = initSound("item.hellforge_revolver.coin_hit");
    public static final RegistryObject<SoundEvent> HELLFORGE_REVOLVER_OVERHEAT = initSound("item.hellforge_revolver.overheat");
    public static final RegistryObject<SoundEvent> HELLFORGE_REVOLVER_HEATING_MUSIC = initSound("item.hellforge_revolver.heating_music");
    
    public static RegistryObject<SoundEvent> initSound(String name) {
        return REG.register(name, () -> SoundEvent.createVariableRangeEvent(GWRexpansions.resource(name)));
    }
}
