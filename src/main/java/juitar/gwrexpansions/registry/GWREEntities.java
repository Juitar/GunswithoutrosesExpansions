package juitar.gwrexpansions.registry;

import juitar.gwrexpansions.CompatModids;
import juitar.gwrexpansions.GWRexpansions;
import juitar.gwrexpansions.entity.cataclysm.CursiumBulletEntity;
import juitar.gwrexpansions.entity.cataclysm.CeraunusLightningBulletEntity;
import juitar.gwrexpansions.entity.cataclysm.CeraunusStormBulletEntity;
import juitar.gwrexpansions.entity.cataclysm.CeraunusWaterBulletEntity;
import juitar.gwrexpansions.entity.cataclysm.IgnitiumBulletEntity;
import juitar.gwrexpansions.entity.cataclysm.LavapowerBulletEntity;
import juitar.gwrexpansions.entity.cataclysm.TidalAbyssBlastPortalEntity;
import juitar.gwrexpansions.entity.cataclysm.TidalAbyssOrbEntity;
import juitar.gwrexpansions.entity.cataclysm.TidalBulletEntity;
import juitar.gwrexpansions.entity.cataclysm.TidalPortalBeamEntity;
import juitar.gwrexpansions.entity.cataclysm.TidalRiftEntity;
import juitar.gwrexpansions.entity.iceandfire.FireDragonSteelBulletEntity;
import juitar.gwrexpansions.entity.iceandfire.IceDragonSteelBulletEntity;
import juitar.gwrexpansions.entity.iceandfire.LightningDragonSteelBulletEntity;
import juitar.gwrexpansions.entity.meetyourfight.DuskfallPiercingBulletEntity;
import juitar.gwrexpansions.entity.meetyourfight.DuskRoseSpiritEntity;
import juitar.gwrexpansions.entity.meetyourfight.MirecallerSwampMineEntity;
import juitar.gwrexpansions.entity.vanilla.SlimeBulletEntity;
import juitar.gwrexpansions.entity.vanilla.MeatHookEntity;
import juitar.gwrexpansions.entity.BOMD.BudBulletEntity;
import juitar.gwrexpansions.entity.BOMD.CoinEntity;
import juitar.gwrexpansions.entity.BOMD.ObsidianCoreEntity;
import juitar.gwrexpansions.entity.BOMD.SporeEntity;
import juitar.gwrexpansions.entity.alexscaves.MagneticBulletEntity;
import juitar.gwrexpansions.entity.alexscaves.MagneticPinEntity;
import juitar.gwrexpansions.entity.vanilla.RedstoneBulletEntity;
import juitar.gwrexpansions.entity.vanilla.RedstonePiercingBulletEntity;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class GWREEntities {
    public static RegistryObject<EntityType<SlimeBulletEntity>> SLIME_BULLET;
    public static RegistryObject<EntityType<RedstoneBulletEntity>> REDSTONE_BULLET;
    public static RegistryObject<EntityType<RedstonePiercingBulletEntity>> REDSTONE_PIERCING_BULLET;
    public static RegistryObject<EntityType<CursiumBulletEntity>> CURSIUM_BULLET;
    public static RegistryObject<EntityType<LavapowerBulletEntity>> LAVAPOWER_BULLET;
    public static RegistryObject<EntityType<IceDragonSteelBulletEntity>> DRAGONSTEEL_ICE_BULLET;
    public static RegistryObject<EntityType<FireDragonSteelBulletEntity>> DRAGONSTEEL_FIRE_BULLET;
    public static RegistryObject<EntityType<LightningDragonSteelBulletEntity>> DRAGONSTEEL_LIGHTNING_BULLET;
    public static RegistryObject<EntityType<IgnitiumBulletEntity>> IGNITIUM_BULLET;
    public static RegistryObject<EntityType<TidalBulletEntity>> TIDAL_BULLET;
    public static RegistryObject<EntityType<TidalRiftEntity>> TIDAL_RIFT;
    public static RegistryObject<EntityType<TidalAbyssOrbEntity>> TIDAL_ABYSS_ORB;
    public static RegistryObject<EntityType<TidalAbyssBlastPortalEntity>> TIDAL_ABYSS_BLAST_PORTAL;
    public static RegistryObject<EntityType<TidalPortalBeamEntity>> TIDAL_PORTAL_BEAM;
    public static RegistryObject<EntityType<CeraunusWaterBulletEntity>> CERAUNUS_WATER_BULLET;
    public static RegistryObject<EntityType<CeraunusStormBulletEntity>> CERAUNUS_STORM_BULLET;
    public static RegistryObject<EntityType<CeraunusLightningBulletEntity>> CERAUNUS_LIGHTNING_BULLET;
    public static RegistryObject<EntityType<ObsidianCoreEntity>> OBSIDIAN_CORE;
    public static RegistryObject<EntityType<MeatHookEntity>> MEAT_HOOK;
    public static RegistryObject<EntityType<CoinEntity>> COIN;
    public static RegistryObject<EntityType<BudBulletEntity>> BUD;
    public static RegistryObject<EntityType<SporeEntity>> SPORE;
    public static RegistryObject<EntityType<MagneticPinEntity>> MAGNETIC_PIN;
    public static RegistryObject<EntityType<MagneticBulletEntity>> MAGNETIC_BULLET;
    public static RegistryObject<EntityType<DuskfallPiercingBulletEntity>> DUSKFALL_PIERCING_BULLET;
    public static RegistryObject<EntityType<DuskRoseSpiritEntity>> DUSK_ROSE_SPIRIT;
    public static RegistryObject<EntityType<MirecallerSwampMineEntity>> MIRECALLER_SWAMP_MINE;

    public static final DeferredRegister<EntityType<?>> REG = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, GWRexpansions.MODID);
    static {
        SLIME_BULLET = REG.register("slime_bullet", () -> EntityType.Builder
                .<SlimeBulletEntity>of(SlimeBulletEntity::new, MobCategory.MISC)
                .sized(0.3125f, 0.3125f)
                .setUpdateInterval(2)
                .setTrackingRange(64)
                .setShouldReceiveVelocityUpdates(true)
                .build(GWRexpansions.MODID + ":slime_bullet"));
        REDSTONE_BULLET = REG.register("redstone_bullet", () -> EntityType.Builder
                .<RedstoneBulletEntity>of(RedstoneBulletEntity::new, MobCategory.MISC)
                .sized(0.3125f, 0.3125f)
                .setUpdateInterval(2)
                .setTrackingRange(64)
                .setShouldReceiveVelocityUpdates(true)
                .build(GWRexpansions.MODID + ":redstone_bullet"));
        REDSTONE_PIERCING_BULLET = REG.register("redstone_piercing_bullet", () -> EntityType.Builder
                .<RedstonePiercingBulletEntity>of(RedstonePiercingBulletEntity::new, MobCategory.MISC)
                .sized(0.3125f, 0.3125f)
                .setUpdateInterval(2)
                .setTrackingRange(64)
                .setShouldReceiveVelocityUpdates(true)
                .build(GWRexpansions.MODID + ":redstone_piercing_bullet"));
        MEAT_HOOK = REG.register("meat_hook",
                () -> EntityType.Builder.<MeatHookEntity>of(MeatHookEntity::new, MobCategory.MISC)
                        .sized(0.5F, 0.5F)
                        .clientTrackingRange(4)
                        .updateInterval(20)
                        .build(new ResourceLocation(GWRexpansions.MODID, "meat_hook").toString()));
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
            TIDAL_RIFT = REG.register("tidal_rift", () -> EntityType.Builder
                    .<TidalRiftEntity>of(TidalRiftEntity::new, MobCategory.MISC)
                    .sized(1.2F, 1.2F).setUpdateInterval(2).setTrackingRange(64).setShouldReceiveVelocityUpdates(false)
                    .build(GWRexpansions.MODID + ":tidal_rift"));
            TIDAL_ABYSS_ORB = REG.register("tidal_abyss_orb", () -> EntityType.Builder
                    .<TidalAbyssOrbEntity>of(TidalAbyssOrbEntity::new, MobCategory.MISC)
                    .sized(0.3125F, 0.3125F).setUpdateInterval(2).setTrackingRange(64).setShouldReceiveVelocityUpdates(true)
                    .build(GWRexpansions.MODID + ":tidal_abyss_orb"));
            TIDAL_ABYSS_BLAST_PORTAL = REG.register("tidal_abyss_blast_portal", () -> EntityType.Builder
                    .<TidalAbyssBlastPortalEntity>of(TidalAbyssBlastPortalEntity::new, MobCategory.MISC)
                    .sized(1.0F, 1.0F).setUpdateInterval(2).setTrackingRange(96).setShouldReceiveVelocityUpdates(false)
                    .build(GWRexpansions.MODID + ":tidal_abyss_blast_portal"));
            TIDAL_PORTAL_BEAM = REG.register("tidal_portal_beam", () -> EntityType.Builder
                    .<TidalPortalBeamEntity>of(TidalPortalBeamEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F).setUpdateInterval(1).setTrackingRange(96).setShouldReceiveVelocityUpdates(false)
                    .build(GWRexpansions.MODID + ":tidal_portal_beam"));
            CERAUNUS_WATER_BULLET = REG.register("ceraunus_water_bullet", () -> EntityType.Builder
                    .<CeraunusWaterBulletEntity>of(CeraunusWaterBulletEntity::new, MobCategory.MISC)
                    .sized(0.3125f, 0.3125f).setUpdateInterval(2).setTrackingRange(64).setShouldReceiveVelocityUpdates(true)
                    .build(GWRexpansions.MODID + ":ceraunus_water_bullet"));
            CERAUNUS_STORM_BULLET = REG.register("ceraunus_storm_bullet", () -> EntityType.Builder
                    .<CeraunusStormBulletEntity>of(CeraunusStormBulletEntity::new, MobCategory.MISC)
                    .sized(0.3125f, 0.3125f).setUpdateInterval(2).setTrackingRange(64).setShouldReceiveVelocityUpdates(true)
                    .build(GWRexpansions.MODID + ":ceraunus_storm_bullet"));
            CERAUNUS_LIGHTNING_BULLET = REG.register("ceraunus_lightning_bullet", () -> EntityType.Builder
                    .<CeraunusLightningBulletEntity>of(CeraunusLightningBulletEntity::new, MobCategory.MISC)
                    .sized(0.3125f, 0.3125f).setUpdateInterval(2).setTrackingRange(64).setShouldReceiveVelocityUpdates(true)
                    .build(GWRexpansions.MODID + ":ceraunus_lightning_bullet"));
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
        if(ModList.get().isLoaded(CompatModids.BOMD)) {
            OBSIDIAN_CORE = REG.register("obsidian_core",
                    () -> EntityType.Builder.<ObsidianCoreEntity>of(ObsidianCoreEntity::new, MobCategory.MISC)
                            .sized(0.5f, 0.5f)
                            .clientTrackingRange(4)
                            .updateInterval(20)
                            .build("obsidian_core"));
            COIN = REG.register("coin",
                    () -> EntityType.Builder.<CoinEntity>of(CoinEntity::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .clientTrackingRange(4)
                            .updateInterval(10)
                            .build("coin"));
            BUD = REG.register("bud", () -> EntityType.Builder
                    .<BudBulletEntity>of(BudBulletEntity::new, MobCategory.MISC)
                    .sized(0.3125f, 0.3125f)
                    .setUpdateInterval(2)
                    .setTrackingRange(64)
                    .setShouldReceiveVelocityUpdates(true)
                    .build(GWRexpansions.MODID + ":bud"));
            SPORE = REG.register("spore", () -> EntityType.Builder
                    .<SporeEntity>of(SporeEntity::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f)
                    .setUpdateInterval(2)
                    .setTrackingRange(32)
                    .setShouldReceiveVelocityUpdates(true)
                    .build(GWRexpansions.MODID + ":spore"));
        }
        if(ModList.get().isLoaded(CompatModids.MEETYOURFIGHT)) {
            DUSKFALL_PIERCING_BULLET = REG.register("duskfall_piercing_bullet", () -> EntityType.Builder
                    .<DuskfallPiercingBulletEntity>of(DuskfallPiercingBulletEntity::new, MobCategory.MISC)
                    .sized(0.3125f, 0.3125f)
                    .setUpdateInterval(2)
                    .setTrackingRange(64)
                    .setShouldReceiveVelocityUpdates(true)
                    .build(GWRexpansions.MODID + ":duskfall_piercing_bullet"));
            DUSK_ROSE_SPIRIT = REG.register("dusk_rose_spirit", () -> EntityType.Builder
                    .<DuskRoseSpiritEntity>of(DuskRoseSpiritEntity::new, MobCategory.MONSTER)
                    .sized(0.45f, 0.45f)
                    .setUpdateInterval(2)
                    .setTrackingRange(64)
                    .setShouldReceiveVelocityUpdates(true)
                    .build(GWRexpansions.MODID + ":dusk_rose_spirit"));
            MIRECALLER_SWAMP_MINE = REG.register("mirecaller_swamp_mine", () -> EntityType.Builder
                    .<MirecallerSwampMineEntity>of(MirecallerSwampMineEntity::new, MobCategory.MISC)
                    .sized(0.98F, 0.98F)
                    .setUpdateInterval(2)
                    .setTrackingRange(64)
                    .setShouldReceiveVelocityUpdates(true)
                    .build(GWRexpansions.MODID + ":mirecaller_swamp_mine"));
        }
        if(ModList.get().isLoaded(CompatModids.ALEXSCAVES)) {
            MAGNETIC_PIN = REG.register("magnetic_pin", () -> EntityType.Builder
                    .<MagneticPinEntity>of(MagneticPinEntity::new, MobCategory.MISC)
                    .sized(0.25f, 0.25f)
                    .setUpdateInterval(2)
                    .setTrackingRange(64)
                    .setShouldReceiveVelocityUpdates(true)
                    .build(GWRexpansions.MODID + ":magnetic_pin"));
            MAGNETIC_BULLET = REG.register("magnetic_bullet", () -> EntityType.Builder
                    .<MagneticBulletEntity>of(MagneticBulletEntity::new, MobCategory.MISC)
                    .sized(0.3125f, 0.3125f)
                    .setUpdateInterval(2)
                    .setTrackingRange(64)
                    .setShouldReceiveVelocityUpdates(true)
                    .build(GWRexpansions.MODID + ":magnetic_bullet"));
        }
    }
}
