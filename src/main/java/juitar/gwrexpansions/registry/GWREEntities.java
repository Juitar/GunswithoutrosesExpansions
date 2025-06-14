package juitar.gwrexpansions.registry;

import juitar.gwrexpansions.CompatModids;
import juitar.gwrexpansions.GWRexpansions;
import juitar.gwrexpansions.entity.cataclysm.CursiumBulletEntity;
import juitar.gwrexpansions.entity.cataclysm.IgnitiumBulletEntity;
import juitar.gwrexpansions.entity.cataclysm.LavapowerBulletEntity;
import juitar.gwrexpansions.entity.cataclysm.TidalBulletEntity;
import juitar.gwrexpansions.entity.iceandfire.FireDragonSteelBulletEntity;
import juitar.gwrexpansions.entity.iceandfire.IceDragonSteelBulletEntity;
import juitar.gwrexpansions.entity.iceandfire.LightningDragonSteelBulletEntity;
import juitar.gwrexpansions.entity.vanilla.SlimeBulletEntity;
import juitar.gwrexpansions.entity.MeatHookEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class GWREEntities {
    public static RegistryObject<EntityType<SlimeBulletEntity>> SLIME_BULLET;
    public static RegistryObject<EntityType<CursiumBulletEntity>> CURSIUM_BULLET;
    public static RegistryObject<EntityType<LavapowerBulletEntity>> LAVAPOWER_BULLET;
    public static RegistryObject<EntityType<IceDragonSteelBulletEntity>> DRAGONSTEEL_ICE_BULLET;
    public static RegistryObject<EntityType<FireDragonSteelBulletEntity>> DRAGONSTEEL_FIRE_BULLET;
    public static RegistryObject<EntityType<LightningDragonSteelBulletEntity>> DRAGONSTEEL_LIGHTNING_BULLET;
    public static RegistryObject<EntityType<IgnitiumBulletEntity>> IGNITIUM_BULLET;
    public static RegistryObject<EntityType<TidalBulletEntity>> TIDAL_BULLET;
    public static final DeferredRegister<EntityType<?>> REG = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, GWRexpansions.MODID);

    static {
        SLIME_BULLET = REG.register("slime_bullet", () -> EntityType.Builder
                .<SlimeBulletEntity>of(SlimeBulletEntity::new, MobCategory.MISC)
                .sized(0.3125f, 0.3125f)
                .setUpdateInterval(2)
                .setTrackingRange(64)
                .setShouldReceiveVelocityUpdates(true)
                .build(GWRexpansions.MODID + ":slime_bullet"));
        if(ModList.get().isLoaded(CompatModids.CATACLYSM)) {
            LAVAPOWER_BULLET = REG.register("lavapower_bullet", () -> EntityType.Builder
                    .<LavapowerBulletEntity>of(LavapowerBulletEntity::new, MobCategory.MISC)
                    .sized(0.3125f, 0.3125f).setUpdateInterval(2).setTrackingRange(64).setShouldReceiveVelocityUpdates(true)
                    .build(GWRexpansions.MODID + ":lavapower_bullet"));
            CURSIUM_BULLET = REG.register("cursium_bullet", () -> EntityType.Builder
                    .<CursiumBulletEntity>of(CursiumBulletEntity::new, MobCategory.MISC)
                    .sized(0.3125f, 0.3125f).setUpdateInterval(2).setTrackingRange(64).setShouldReceiveVelocityUpdates(true)
                    .build(GWRexpansions.MODID + ":cursium_bullet"));
            IGNITIUM_BULLET = REG.register("ignitium_bullet", () -> EntityType.Builder
                    .<IgnitiumBulletEntity>of(IgnitiumBulletEntity::new, MobCategory.MISC)
                    .sized(0.3125f, 0.3125f).setUpdateInterval(2).setTrackingRange(64).setShouldReceiveVelocityUpdates(true)
                    .build(GWRexpansions.MODID + ":ignitium_bullet"));
            TIDAL_BULLET = REG.register("tidal_bullet", () -> EntityType.Builder
                    .<TidalBulletEntity>of(TidalBulletEntity::new, MobCategory.MISC)
                    .sized(0.3125f, 0.3125f).setUpdateInterval(2).setTrackingRange(64).setShouldReceiveVelocityUpdates(true)
                    .build(GWRexpansions.MODID + ":tidal_bullet"));
        }
        if(ModList.get().isLoaded(CompatModids.ICEANDFIRE)) {
            DRAGONSTEEL_ICE_BULLET =REG.register("dragonsteel_ice_bullet", () -> EntityType.Builder
                    .<IceDragonSteelBulletEntity>of(IceDragonSteelBulletEntity::new, MobCategory.MISC)
                    .sized(0.3125f, 0.3125f).setUpdateInterval(2).setTrackingRange(64).setShouldReceiveVelocityUpdates(true)
                    .build(GWRexpansions.MODID + ":dragonsteel_ice_bullet"));
            DRAGONSTEEL_FIRE_BULLET = REG.register("dragonsteel_fire_bullet", () -> EntityType.Builder
                    .<FireDragonSteelBulletEntity>of(FireDragonSteelBulletEntity::new, MobCategory.MISC)
                    .sized(0.3125f, 0.3125f).setUpdateInterval(2).setTrackingRange(64).setShouldReceiveVelocityUpdates(true)
                    .build(GWRexpansions.MODID + ":dragonsteel_fire_bullet"));
            DRAGONSTEEL_LIGHTNING_BULLET =REG.register("dragonsteel_lightning_bullet", () -> EntityType.Builder
                    .<LightningDragonSteelBulletEntity>of(LightningDragonSteelBulletEntity::new, MobCategory.MISC)
                    .sized(0.3125f, 0.3125f).setUpdateInterval(2).setTrackingRange(64).setShouldReceiveVelocityUpdates(true)
                    .build(GWRexpansions.MODID + ":dragonsteel_lightning_bullet"));
        }
    }

    // 注册肉钩实体
    public static final RegistryObject<EntityType<MeatHookEntity>> MEAT_HOOK = REG.register("meat_hook",
            () -> EntityType.Builder.<MeatHookEntity>of(MeatHookEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(4)
                    .updateInterval(20)
                    .build(new ResourceLocation(GWRexpansions.MODID, "meat_hook").toString()));
}
