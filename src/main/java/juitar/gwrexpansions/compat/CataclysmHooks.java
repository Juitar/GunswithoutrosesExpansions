package juitar.gwrexpansions.compat;

import juitar.gwrexpansions.item.cataclysm.CursiumGunItem;
import juitar.gwrexpansions.item.cataclysm.HarbingerRaycasterItem;
import lykrast.gunswithoutroses.entity.BulletEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public final class CataclysmHooks {
    private CataclysmHooks() {
    }

    public static void onBulletHeadshot(BulletEntity bullet, Entity shooter, boolean headshot) {
        CursiumGunItem.onBulletHeadshot(bullet, shooter, headshot);
    }

    public static void onRedstoneBulletHeadshot(BulletEntity bullet, LivingEntity target,
            @Nullable Entity shooter, Level world) {
        HarbingerRaycasterItem.onRedstoneBulletHeadshot(bullet, target, shooter, world);
    }
}
