package juitar.gwrexpansions.item.cataclysm;

import juitar.gwrexpansions.entity.cataclysm.CursiumBulletEntity;

import lykrast.gunswithoutroses.entity.BulletEntity;
import lykrast.gunswithoutroses.item.BulletItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class CursiumBulletItem extends BulletItem {
    public CursiumBulletItem(Properties properties, int damage) {
        super(properties,damage);
    }
    @Override
    public BulletEntity createProjectile(Level world, ItemStack stack, LivingEntity shooter) {
        CursiumBulletEntity bullet = new CursiumBulletEntity(world, shooter);
        bullet.setItem(stack);
        bullet.setDamage(damage);
        bullet.setOwner(shooter);
        // 获取shooter视线方向上最近的实体作为目标
        double range = 50.0D; // 搜索范围
        Vec3 start = shooter.getEyePosition();
        Vec3 look = shooter.getLookAngle();
        Vec3 end = start.add(look.x * range, look.y * range, look.z * range);
        
        EntityHitResult result = ProjectileUtil.getEntityHitResult(
            world, shooter, start, end,
            shooter.getBoundingBox().expandTowards(look.scale(range)).inflate(1.0D),
            entity -> !entity.isSpectator() && entity instanceof LivingEntity && entity != shooter
        );
        
        if (result != null && result.getEntity() instanceof LivingEntity target) {
            bullet.setFinalTarget(target);
        }
        if(shooter instanceof LivingEntity){
            Item mainHand = shooter.getMainHandItem().getItem();
            Item offHand = shooter.getOffhandItem().getItem();
            if(mainHand instanceof CursiumGunItem|| offHand instanceof CursiumGunItem){
                bullet.setSHOT_FROM_CURSIUM(true);
            }
        }
        return bullet;
    }
    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        tooltip.add(Component.translatable("tooltip.gwrexpansions.cursium_bullet.desc").withStyle(ChatFormatting.GRAY));
    }
}
