package juitar.gwrexpansions.entity;

import lykrast.gunswithoutroses.entity.BulletEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;


public class LavapowerBulletEntity extends BulletEntity {
    private int jetCount = 3;

    public LavapowerBulletEntity (EntityType<? extends BulletEntity> type, Level level) {
        super(type, level);
    }

    public LavapowerBulletEntity (Level level, LivingEntity shooter) {
        super(level, shooter);
    }
}
