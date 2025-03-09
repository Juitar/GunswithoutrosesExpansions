package juitar.gwrexpansions.item.minecraft;

import lykrast.gunswithoutroses.entity.BulletEntity;
import lykrast.gunswithoutroses.item.BulletItem;
import lykrast.gunswithoutroses.item.GunItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

public class ReshotableBulletItem extends BulletItem {
    private final Random random = new Random();
    private static final float DROP_CHANCE = 0.8f; // 80%的掉落概率
    private static final String SHOTGUN_TAG = "gunswithoutroses:gun/shotgun";

    public ReshotableBulletItem(Properties properties, int damage) {
        super(properties, damage);
    }

    private boolean isShotgunShot(@Nullable Entity shooter) {
        if (shooter instanceof LivingEntity living) {
            ItemStack mainHand = living.getMainHandItem();
            ItemStack offHand = living.getOffhandItem();
            
            
            // 修改检查逻辑：直接检查物品是否有这个tag
            boolean isShotgun = mainHand.is(net.minecraft.tags.ItemTags.create(new ResourceLocation("gunswithoutroses:gun/shotgun"))) ||
                              offHand.is(net.minecraft.tags.ItemTags.create(new ResourceLocation("gunswithoutroses:gun/shotgun")));
            
            return isShotgun;
        }
        return false;
    }

    private void tryDropBullet(Level world, double x, double y, double z, @Nullable Entity shooter) {
        // 如果是霰弹枪发射的，直接不掉落
        if (isShotgunShot(shooter)) {
            return;
        }

        if (!world.isClientSide && random.nextFloat() < DROP_CHANCE) {
            // 创建物品实体
            ItemStack dropStack = new ItemStack(this, 1);
            ItemEntity itemEntity = new ItemEntity(world, x, y, z, dropStack);
            
            // 给予随机速度
            itemEntity.setDeltaMovement(
                (random.nextDouble() - 0.5D) * 0.1D,
                random.nextDouble() * 0.2D,
                (random.nextDouble() - 0.5D) * 0.1D
            );
            
            // 设置拾取延迟
            itemEntity.setPickUpDelay(10);
            
            // 生成物品实体
            world.addFreshEntity(itemEntity);
            
        }
    }

    @Override
    public void onLivingEntityHit(BulletEntity bullet, LivingEntity target, @Nullable Entity shooter, Level world) {
        tryDropBullet(world, bullet.getX(), bullet.getY(), bullet.getZ(), shooter);
    }

    @Override
    public void onBlockHit(BulletEntity projectile, BlockHitResult hit, @Nullable Entity shooter, Level world) {
        tryDropBullet(world, projectile.getX(), projectile.getY(), projectile.getZ(), shooter);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        tooltip.add(Component.translatable("tooltip.gwrexpansions.netherite_bullet.desc", DROP_CHANCE).withStyle(ChatFormatting.GRAY));
    }
}
