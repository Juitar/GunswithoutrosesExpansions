package juitar.gwrexpansions.item.BOMD;

import juitar.gwrexpansions.entity.BOMD.BudBulletEntity;
import juitar.gwrexpansions.registry.GWREEntities;
import lykrast.gunswithoutroses.entity.BulletEntity;
import lykrast.gunswithoutroses.item.BulletItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * 花苞弹物品 - 创建花苞弹实体
 */
public class BudBulletItem extends BulletItem {

    public BudBulletItem(Properties properties, int damage) {
        super(properties, damage);
    }

    @Override
    public BulletEntity createProjectile(Level world, ItemStack stack, LivingEntity shooter) {
        BudBulletEntity bullet = new BudBulletEntity( world,shooter);
        bullet.setItem(stack);
        bullet.setDamage(damage);
        return bullet;
    }
}
