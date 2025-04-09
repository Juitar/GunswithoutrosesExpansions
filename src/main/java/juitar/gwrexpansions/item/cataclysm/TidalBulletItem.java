package juitar.gwrexpansions.item.cataclysm;

import juitar.gwrexpansions.entity.cataclysm.TidalBulletEntity;
import lykrast.gunswithoutroses.entity.BulletEntity;
import lykrast.gunswithoutroses.item.BulletItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class TidalBulletItem extends BulletItem {
    public TidalBulletItem(Properties properties, int damage) {
        super(properties, damage);
    }
    @Override
    public BulletEntity createProjectile(Level world, ItemStack stack, LivingEntity shooter){
       TidalBulletEntity bullet = new TidalBulletEntity(world, shooter);
       bullet.setItem(stack);
       bullet.setDamage(damage);
       return bullet;
    }
}
