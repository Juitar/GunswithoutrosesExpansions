package juitar.gwrexpansions.entity.cataclysm;

import lykrast.gunswithoutroses.entity.BulletEntity;
import lykrast.gunswithoutroses.registry.GWRDamage;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import javax.annotation.Nullable;

import com.github.L_Ender.cataclysm.entity.projectile.Flame_Jet_Entity;

public class LavapowerBulletEntity extends BulletEntity {
    private int jetCount = 5;

    public LavapowerBulletEntity(EntityType<? extends BulletEntity> type, Level level) {
        super(type, level);
    }

    public LavapowerBulletEntity(Level level, LivingEntity shooter) {
        super(level, shooter);
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
            if(!level().isClientSide) {
                Entity target = result.getEntity();
                Entity shooter = getOwner();
                // 保存并重置无敌时间
                int lastHurtResistant = target.invulnerableTime;
                target.invulnerableTime = 0;
                // 伤害计算
                float damage = (float) getDamage();
                boolean headshot = hasHeadshot(target);
                if (headshot) {
                    damage *= getHeadshotMultiplier();
                }
                boolean damaged = shooter == null
                        ? target.hurt(GWRDamage.gunDamage(level().registryAccess(), this), damage)
                        : target.hurt(GWRDamage.gunDamage(level().registryAccess(), this, shooter), damage);
                // 如果伤害未生效,恢复无敌时间
                if (!damaged) {
                    target.invulnerableTime = lastHurtResistant;
                }
                // 生成火焰弹幕
                if (random.nextBoolean()) {
                    createXStrikeJet(target.getX(), target.getY(), target.getZ(), shooter, jetCount, 2);
                } else {
                    createPlusStrikeJet(target.getX(), target.getY(), target.getZ(), shooter, jetCount, 2);
                }
            }
    }

    @Override
    protected void onHitBlock(BlockHitResult hit) {
            // 生成火焰弹幕
            if (random.nextBoolean()) {
                createXStrikeJet(hit.getLocation().x, hit.getLocation().y, hit.getLocation().z, getOwner(), jetCount, 2);
            } else {
                createPlusStrikeJet(hit.getLocation().x, hit.getLocation().y, hit.getLocation().z, getOwner(), jetCount, 2);
            }
    }

    public void setJetCount(int count) {
        this.jetCount = count;
    }

    public int getJetCount() {
        return this.jetCount;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("JetCount", jetCount);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        jetCount = compound.getInt("JetCount");
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
                level.addFreshEntity(new Flame_Jet_Entity(level, x, (double)blockpos.getY() + d0, z, rotation, delay, 7, living));
            } else {
                level.addFreshEntity(new Flame_Jet_Entity(level, x, (double)blockpos.getY() + d0, z, rotation, delay, 7, null));
            }
        }
    }
}
