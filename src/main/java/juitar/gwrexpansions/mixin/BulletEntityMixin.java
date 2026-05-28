package juitar.gwrexpansions.mixin;

import juitar.gwrexpansions.item.cataclysm.CursiumGunItem;
import lykrast.gunswithoutroses.entity.BulletEntity;
import lykrast.gunswithoutroses.item.IBullet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = BulletEntity.class, remap = false)
public abstract class BulletEntityMixin {
    @Redirect(
            method = { "onHitEntity", "m_5790_" },
            at = @At(
                    value = "INVOKE",
                    target = "Llykrast/gunswithoutroses/item/IBullet;onLivingEntityHit(Llykrast/gunswithoutroses/entity/BulletEntity;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/level/Level;Z)V"),
            remap = false)
    private void gwrexpansions$onLivingEntityHit(IBullet bulletItem, BulletEntity bullet, LivingEntity target,
            Entity shooter, Level level, boolean headshot) {
        CursiumGunItem.onBulletHeadshot(bullet, shooter, headshot);
        bulletItem.onLivingEntityHit(bullet, target, shooter, level, headshot);
    }
}
