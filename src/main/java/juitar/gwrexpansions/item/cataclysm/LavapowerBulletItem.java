package juitar.gwrexpansions.item.cataclysm;

import juitar.gwrexpansions.entity.cataclysm.LavapowerBulletEntity;
import lykrast.gunswithoutroses.entity.BulletEntity;
import lykrast.gunswithoutroses.item.BulletItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.shapes.VoxelShape;
import com.github.L_Ender.cataclysm.entity.projectile.Flame_Jet_Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nullable;
import java.util.List;

public class LavapowerBulletItem extends BulletItem {

    public LavapowerBulletItem(Properties properties, int damage) {
        super(properties, damage);
    }

    @Override
    public BulletEntity createProjectile(Level world, ItemStack stack, LivingEntity shooter){
        LavapowerBulletEntity bullet = new LavapowerBulletEntity(world, shooter);
        bullet.setOwner(shooter);
        bullet.setItem(stack);
        bullet.setDamage(damage);
        return bullet;
    }

    @Override
    public void onLivingEntityHit(BulletEntity bullet, LivingEntity target, @Nullable Entity shooter, Level world) {
        if (!world.isClientSide) {
            // 随机选择十字或X形状的火焰弹幕
            int jetCount = 3;

            if (shooter instanceof LivingEntity livingShooter) {
                ItemStack mainHand = livingShooter.getMainHandItem();
                ItemStack offHand = livingShooter.getOffhandItem();

                // 检查主手或副手是否持有Lavapowergun
                if (mainHand.getItem() instanceof  LavapowerGun||
                        offHand.getItem() instanceof LavapowerGun) {
                    // 如果是Lavapowergun，生成更多的火焰
                    jetCount = 5;
                }
            }

            if (world.random.nextBoolean()) {
                createXStrikeJet(target.getX(), target.getY(), target.getZ(), shooter, jetCount, 2);
            } else {
                createPlusStrikeJet(target.getX(), target.getY(), target.getZ(), shooter, jetCount, 2);
            }
        }
    }

    @Override
    public void onBlockHit(BulletEntity projectile, BlockHitResult hit, @Nullable Entity shooter, Level world) {
        if (!world.isClientSide) {
            // 随机选择十字或X形状的火焰弹幕
            int jetCount = 3;

            if (shooter instanceof LivingEntity livingShooter) {
                ItemStack mainHand = livingShooter.getMainHandItem();
                ItemStack offHand = livingShooter.getOffhandItem();

                // 检查主手或副手是否持有Lavapowergun
                if (mainHand.getItem() instanceof LavapowerGun ||
                        offHand.getItem() instanceof LavapowerGun) {
                    // 如果是Lavapowergun，生成更多的火焰
                    jetCount = 5;
                }
            }

            if (world.random.nextBoolean()) {
                createXStrikeJet(hit.getLocation().x, hit.getLocation().y, hit.getLocation().z, shooter, jetCount, 2);
            } else {
                createPlusStrikeJet(hit.getLocation().x, hit.getLocation().y, hit.getLocation().z, shooter, jetCount, 2);
            }
        }
    }

    private void createPlusStrikeJet(double x, double y, double z, @Nullable Entity shooter, int rune, double time) {
        for (int i = 0; i < 4; i++) {
            float yawRadians = (float) (Math.toRadians(90));
            float throwAngle = yawRadians + i * (float)Math.PI / 2;
            for (int k = 0; k < rune; ++k) {
                double d2 = 0.8D * (double)(k + 1);
                int d3 = (int)(time * (k + 1));
                spawnJet(shooter.level(), x + Math.cos(throwAngle) * 1.25D * d2,
                        z + Math.sin(throwAngle) * 1.25D * d2,
                        y - 2, y + 2, throwAngle, d3, shooter);
            }
        }
    }

    private void createXStrikeJet(double x, double y, double z, @Nullable Entity shooter, int rune, double time) {
        for (int i = 0; i < 4; i++) {
            float yawRadians = (float) (Math.toRadians(45));
            float throwAngle = yawRadians + i * (float)Math.PI / 2;
            for (int k = 0; k < rune; ++k) {
                double d2 = 0.8D * (double)(k + 1);
                int d3 = (int)(time * (k + 1));
                spawnJet(shooter.level(), x + Math.cos(throwAngle) * 1.25D * d2,
                        z + Math.sin(throwAngle) * 1.25D * d2,
                        y - 2, y + 2, throwAngle, d3, shooter);
            }
        }
    }

    private void spawnJet(Level level, double x, double z, double minY, double maxY, float rotation, int delay, @Nullable Entity shooter) {
        BlockPos blockpos = BlockPos.containing(x, maxY, z);
        boolean flag = false;
        double d0 = 0.0D;
        do {
            BlockPos blockpos1 = blockpos.below();
            BlockState blockstate = level.getBlockState(blockpos1);
            if (blockstate.isFaceSturdy(level, blockpos1, Direction.UP)) {
                if (!level.isEmptyBlock(blockpos)) {
                    BlockState blockstate1 = level.getBlockState(blockpos);
                    VoxelShape voxelshape = blockstate1.getCollisionShape(level, blockpos);
                    if (!voxelshape.isEmpty()) {
                        d0 = voxelshape.max(Direction.Axis.Y);
                    }
                }
                flag = true;
                break;
            }

            blockpos = blockpos.below();
        } while(blockpos.getY() >= Mth.floor(minY) - 1);

        if (flag) {
            if (shooter instanceof LivingEntity living) {
                level.addFreshEntity(new Flame_Jet_Entity(level, x, (double)blockpos.getY() + d0, z, rotation, delay, damage, living));
            } else {
                level.addFreshEntity(new Flame_Jet_Entity(level, x, (double)blockpos.getY() + d0, z, rotation, delay, damage, null));
            }
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        tooltip.add(Component.translatable("tooltip.gwrexpansions.lavapower_bullet.desc").withStyle(ChatFormatting.GRAY));
    }
}
