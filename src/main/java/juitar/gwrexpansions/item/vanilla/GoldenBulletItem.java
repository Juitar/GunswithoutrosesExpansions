package juitar.gwrexpansions.item.vanilla;

import lykrast.gunswithoutroses.entity.BulletEntity;
import lykrast.gunswithoutroses.item.BulletItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class GoldenBulletItem extends BulletItem {
    private final Random random = new Random();
    private static final float GOLD_NUGGET_CHANCE = 0.4f; // 40%概率掉落金粒
    private static final float GOLDEN_APPLE_CHANCE = 0.033f; // 3.3%概率掉落金苹果

    public GoldenBulletItem(Properties properties, int damage) {
        super(properties, damage);
    }

    @Override
    public void onLivingEntityHit(BulletEntity bullet, LivingEntity target, @Nullable Entity shooter, Level world) {
        // 生成金币粒子效果
        for(int i = 0; i < 15; i++) {
            double d0 = target.getRandomX(0.5D);
            double d1 = target.getRandomY();
            double d2 = target.getRandomZ(0.5D);
            world.addParticle(ParticleTypes.CRIT, d0, d1, d2, 0.0D, 0.0D, 0.0D);
        }

        if (!world.isClientSide) {
            // 播放金币声音
            world.playSound(null, target.getX(), target.getY(), target.getZ(),
                    SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.NEUTRAL,
                    0.5F, 1.0F + (world.random.nextFloat() - world.random.nextFloat()) * 0.2F);

            // 掉落1-3个金粒
            if (random.nextFloat() < GOLD_NUGGET_CHANCE) {
                int count = 1 + random.nextInt(3);
                spawnItem(world, target, new ItemStack(Items.GOLD_NUGGET, count));
            }

            // 小概率掉落金苹果
            if (random.nextFloat() < GOLDEN_APPLE_CHANCE) {
                spawnItem(world, target, new ItemStack(Items.GOLDEN_APPLE));
                // 掉落金苹果时播放特殊音效
                world.playSound(null, target.getX(), target.getY(), target.getZ(),
                        SoundEvents.PLAYER_LEVELUP, SoundSource.NEUTRAL,
                        0.5F, 1.0F);
                
                // 掉落金苹果时产生更多特效
                for(int i = 0; i < 20; i++) {
                    double d0 = target.getRandomX(1.0D);
                    double d1 = target.getRandomY();
                    double d2 = target.getRandomZ(1.0D);
                    world.addParticle(ParticleTypes.TOTEM_OF_UNDYING, d0, d1, d2, 
                            random.nextGaussian() * 0.05D,
                            random.nextGaussian() * 0.05D,
                            random.nextGaussian() * 0.05D);
                }
            }
        }
    }

    private void spawnItem(Level world, LivingEntity target, ItemStack stack) {
        ItemEntity item = new ItemEntity(world, 
            target.getX(), target.getY(), target.getZ(),
            stack);
        
        // 给予随机速度，让物品散开
        item.setDeltaMovement(
            (random.nextDouble() - 0.5D) * 0.2D,
            random.nextDouble() * 0.2D,
            (random.nextDouble() - 0.5D) * 0.2D
        );
        
        world.addFreshEntity(item);
    }

    @Override
    public void onBlockHit(BulletEntity projectile, BlockHitResult hit, @Nullable Entity shooter, Level world) {
        // 生成金币粒子效果
        for(int i = 0; i < 8; i++) {
            double d0 = hit.getLocation().x + (random.nextDouble() - 0.5D) * 0.5D;
            double d1 = hit.getLocation().y + (random.nextDouble() - 0.5D) * 0.5D;
            double d2 = hit.getLocation().z + (random.nextDouble() - 0.5D) * 0.5D;
            world.addParticle(ParticleTypes.CRIT, d0, d1, d2, 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    public BulletEntity createProjectile(Level world, ItemStack stack, LivingEntity shooter) {
        BulletEntity bullet = super.createProjectile(world, stack, shooter);
        return bullet;
    }
    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);
        tooltip.add(Component.translatable("tooltip.gwrexpansions.golden_bullet.desc").withStyle(ChatFormatting.GRAY));
    }
} 