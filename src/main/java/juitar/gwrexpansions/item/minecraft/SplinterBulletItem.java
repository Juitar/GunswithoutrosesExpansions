package juitar.gwrexpansions.item.minecraft;

import juitar.gwrexpansions.registry.VanillaItem;
import lykrast.gunswithoutroses.entity.BulletEntity;
import lykrast.gunswithoutroses.item.BulletItem;
import lykrast.gunswithoutroses.registry.GWREntities;
import lykrast.gunswithoutroses.registry.GWRItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;


import javax.annotation.Nullable;

public class SplinterBulletItem extends BulletItem {
    public SplinterBulletItem(Properties properties,int damage) {
        super(properties,damage);
    }
    @Override
    public void onLivingEntityHit(BulletEntity bullet, LivingEntity target, @Nullable Entity shooter, Level world) {
        if (!world.isClientSide) {
            // 随机生成2-4个弹片
            int fragments = world.random.nextInt(3) + 2; // 2 to 4
            for (int i = 0; i < fragments; i++) {
                spawnBulletPiece(world, target.getX(), target.getY() + 0.5, target.getZ(), shooter);
            }
        }
        world.playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.GLASS_BREAK, SoundSource.NEUTRAL,
                1.0F, 1.0F + (world.random.nextFloat() - world.random.nextFloat()) * 0.2F);
    }
    @Override
    public void onBlockHit(BulletEntity projectile, BlockHitResult hit, @Nullable Entity shooter, Level world) {
        if (!world.isClientSide) {
            // 随机生成2-4个弹片
            int fragments = world.random.nextInt(3) + 2; // 2 to 4
            for (int i = 0; i < fragments; i++) {
                spawnBulletPiece(world, hit.getLocation().x, hit.getLocation().y , hit.getLocation().z, shooter);
            }
        }
        world.playSound(null, hit.getLocation().x, hit.getLocation().y , hit.getLocation().z,
                SoundEvents.GLASS_BREAK, SoundSource.NEUTRAL,
                1.0F, 1.0F + (world.random.nextFloat() - world.random.nextFloat()) * 0.2F);
    }

    protected void spawnBulletPiece(Level world, double x, double y, double z, @Nullable Entity shooter) {
        BulletEntity bulletPiece = new BulletEntity(GWREntities.BULLET.get(), world);
        
        // 设置基本属性
        bulletPiece.setDamage(3);
        bulletPiece.setItem(new ItemStack(VanillaItem.diamond_bullet_shrapnel.get()));
        bulletPiece.setOwner(shooter);
        
        // 设置位置
        bulletPiece.setPos(x, y, z);
        
        // 生成随机角度 (水平360度，保持同一高度)
        float yRot = world.random.nextFloat() * 360.0F;
        float xRot = 0.0F; // 保持水平
        
        // 使用原版的射击方法，upArc=0保持水平，speed=1.0，spread=1.0
        bulletPiece.shootFromRotation(bulletPiece, xRot, yRot, 0.0F, 1.0F, 1.0F);
        
        world.addFreshEntity(bulletPiece);
    }

    private double getDamage() {
        return this.damage;
    }
    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        tooltip.add(Component.translatable("tooltip.gwrexpansions.diamond_bullet.desc").withStyle(ChatFormatting.GRAY));
    }
}

