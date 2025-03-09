package juitar.gwrexpansions.registry;

import juitar.gwrexpansions.CompatModids;
import juitar.gwrexpansions.GWRexpansions;
import juitar.gwrexpansions.entity.cataclysm.LavapowerBulletEntity;
import juitar.gwrexpansions.entity.iceandfire.FireDragonSteelBulletEntity;
import juitar.gwrexpansions.entity.iceandfire.IceDragonSteelBulletEntity;
import juitar.gwrexpansions.entity.iceandfire.LightningDragonSteelBulletEntity;
import juitar.gwrexpansions.entity.minecraft.SlimeBulletEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class GWREEntity {
    public static RegistryObject<EntityType<SlimeBulletEntity>> SLIME_BULLET;
    public static RegistryObject<EntityType<LavapowerBulletEntity>> LAVAPOWER_BULLET;
    public static final DeferredRegister<EntityType<?>> REG = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, GWRexpansions.MODID);

    static {
        SLIME_BULLET = REG.register("slime_bullet",()-> EntityType.Builder
                .<SlimeBulletEntity>of(SlimeBulletEntity::new, MobCategory.MISC)
                .sized(0.3125f, 0.3125f).setUpdateInterval(2).setTrackingRange(64).setShouldReceiveVelocityUpdates(true)
                .build(GWRexpansions.MODID + ":slime_bullet"));
        if(ModList.get().isLoaded(CompatModids.CATACLYSM)) {
            LAVAPOWER_BULLET = REG.register("lavapower_bullet", () -> EntityType.Builder
                    .<LavapowerBulletEntity>of(LavapowerBulletEntity::new, MobCategory.MISC)
                    .sized(0.3125f, 0.3125f).setUpdateInterval(2).setTrackingRange(64).setShouldReceiveVelocityUpdates(true)
                    .build(GWRexpansions.MODID + ":lavapower_bullet"));
        }
        if(ModList.get().isLoaded(CompatModids.ICEANDFIRE)) {
            REG.register("dragonsteel_ice_bullet", () -> EntityType.Builder
                    .<IceDragonSteelBulletEntity>of(IceDragonSteelBulletEntity::new, MobCategory.MISC)
                    .sized(0.3125f, 0.3125f).setUpdateInterval(2).setTrackingRange(64).setShouldReceiveVelocityUpdates(true)
                    .build(GWRexpansions.MODID + ":dragonsteel_ice_bullet"));
            REG.register("dragonsteel_fire_bullet", () -> EntityType.Builder
                    .<FireDragonSteelBulletEntity>of(FireDragonSteelBulletEntity::new, MobCategory.MISC)
                    .sized(0.3125f, 0.3125f).setUpdateInterval(2).setTrackingRange(64).setShouldReceiveVelocityUpdates(true)
                    .build(GWRexpansions.MODID + ":dragonsteel_fire_bullet"));
            REG.register("dragonsteel_lightning_bullet", () -> EntityType.Builder
                    .<LightningDragonSteelBulletEntity>of(LightningDragonSteelBulletEntity::new, MobCategory.MISC)
                    .sized(0.3125f, 0.3125f).setUpdateInterval(2).setTrackingRange(64).setShouldReceiveVelocityUpdates(true)
                    .build(GWRexpansions.MODID + ":dragonsteel_lightning_bullet"));
        }
    }
}
