package juitar.gwrexpansions.item.vanilla;

import lykrast.gunswithoutroses.entity.BulletEntity;
import lykrast.gunswithoutroses.item.BulletItem;

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
import net.minecraft.nbt.CompoundTag;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

public class ReshotableBulletItem extends BulletItem {
    private final Random random = new Random();
    private static final float DROP_CHANCE = 0.70f; // 70%的掉落概率
    private static final String SHOTGUN_TAG = "gunswithoutroses:gun/shotgun";

    public ReshotableBulletItem(Properties properties, int damage) {
        super(properties, damage);
    }
    @Override
    public BulletEntity createProjectile(Level world, ItemStack stack, LivingEntity shooter) {
        BulletEntity bullet = super.createProjectile(world, stack, shooter);

        // 检查发射时的武器是否是霰弹枪
        ItemStack mainHand = shooter.getMainHandItem();
        ItemStack offHand = shooter.getOffhandItem();
        boolean isShotgun = mainHand.is(net.minecraft.tags.ItemTags.create(new ResourceLocation("gunswithoutroses:gun/shotgun"))) ||
                offHand.is(net.minecraft.tags.ItemTags.create(new ResourceLocation("gunswithoutroses:gun/shotgun")));

        // 标记子弹
        CompoundTag tag = bullet.getPersistentData();
        tag.putBoolean("ShotFromShotgun", isShotgun);

        return bullet;
    }


    private boolean isShotgunShot(BulletEntity bullet,@Nullable Entity shooter) {
        if (shooter instanceof LivingEntity living) {
            // 从子弹实体中获取发射时的武器类型标记
            if (bullet instanceof BulletEntity ) {
                CompoundTag tag = bullet.getPersistentData();
                return tag.getBoolean("ShotFromShotgun");
            }
        }
        return false;
    }

    private void tryDropBullet(BulletEntity bullet,Level world, double x, double y, double z, @Nullable Entity shooter) {
        // 如果是霰弹枪发射的，直接不掉落
        if (isShotgunShot(bullet, shooter)) {
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
        tryDropBullet(bullet,world, bullet.getX(), bullet.getY(), bullet.getZ(), shooter);
    }

    @Override
    public void onBlockHit(BulletEntity projectile, BlockHitResult hit, @Nullable Entity shooter, Level world) {
        tryDropBullet(projectile,world, projectile.getX(), projectile.getY(), projectile.getZ(), shooter);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        tooltip.add(Component.translatable("tooltip.gwrexpansions.netherite_bullet.desc", DROP_CHANCE*100).withStyle(ChatFormatting.GRAY));
    }


}
