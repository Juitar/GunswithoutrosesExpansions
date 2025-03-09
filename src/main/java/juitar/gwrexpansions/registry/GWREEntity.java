package juitar.gwrexpansions.registry;

import juitar.gwrexpansions.GWRexpansions;
import juitar.gwrexpansions.entity.SlimeBulletEntity;
import lykrast.gunswithoutroses.entity.BulletEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class GWREEntity {
    public static RegistryObject<EntityType<SlimeBulletEntity>> SLIME_BULLET;
    public static final DeferredRegister<EntityType<?>> REG = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, GWRexpansions.MODID);

    static {
        SLIME_BULLET = REG.register("slime_bullet",()-> EntityType.Builder
                .<SlimeBulletEntity>of(SlimeBulletEntity::new, MobCategory.MISC)
                .sized(0.3125f, 0.3125f).setUpdateInterval(2).setTrackingRange(64).setShouldReceiveVelocityUpdates(true)
                .build(GWRexpansions.MODID + ":slime_bullet"));
    }
}
