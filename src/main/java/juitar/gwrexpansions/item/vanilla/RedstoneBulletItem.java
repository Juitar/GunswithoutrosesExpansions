package juitar.gwrexpansions.item.vanilla;

import juitar.gwrexpansions.entity.vanilla.RedstoneBulletEntity;
import juitar.gwrexpansions.item.cataclysm.HarbingerRaycasterItem;
import lykrast.gunswithoutroses.entity.BulletEntity;
import lykrast.gunswithoutroses.item.BulletItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class RedstoneBulletItem extends BulletItem {
    private static final int REDSTONE_PULSE_TICKS = 20;
    private static final List<ActiveRedstonePulse> ACTIVE_PULSES = new ArrayList<>();

    public RedstoneBulletItem(Properties properties, int damage) {
        super(properties, damage);
    }

    @Override
    public BulletEntity createProjectile(Level world, ItemStack stack, LivingEntity shooter) {
        RedstoneBulletEntity bullet = new RedstoneBulletEntity(world, shooter);
        bullet.setItem(stack);
        bullet.setDamage(damage);
        return bullet;
    }

    @Override
    public void onLivingEntityHit(BulletEntity bullet, LivingEntity target, @Nullable Entity shooter, Level world,
                                  boolean headshot) {
        if (headshot) {
            HarbingerRaycasterItem.onRedstoneBulletHeadshot(bullet, target, shooter, world);
        }
    }

    @Override
    public void onBlockHit(BulletEntity projectile, BlockHitResult hit, @Nullable Entity shooter, Level world) {
        pulseRedstoneBlock(world, hit.getBlockPos(), hit.getDirection());
    }

    public static void tickRedstonePulses(ServerLevel level) {
        Iterator<ActiveRedstonePulse> iterator = ACTIVE_PULSES.iterator();
        while (iterator.hasNext()) {
            ActiveRedstonePulse pulse = iterator.next();
            if (!pulse.dimension.equals(level.dimension())) {
                continue;
            }

            if (pulse.ticksLeft-- > 0) {
                continue;
            }

            updateNeighborsForPulse(level, pulse.pos);
            iterator.remove();
        }
    }

    public static int getVirtualRedstoneSignal(Level level, BlockPos pos) {
        for (ActiveRedstonePulse pulse : ACTIVE_PULSES) {
            if (pulse.dimension.equals(level.dimension()) && pulse.pos.equals(pos) && pulse.ticksLeft > 0) {
                return 15;
            }
        }
        return 0;
    }

    public static int getVirtualNeighborRedstoneSignal(Level level, BlockPos pos) {
        if (getVirtualRedstoneSignal(level, pos) > 0) {
            return 15;
        }

        for (Direction direction : Direction.values()) {
            if (getVirtualRedstoneSignal(level, pos.relative(direction)) > 0) {
                return 15;
            }
        }
        return 0;
    }

    private static void pulseRedstoneBlock(Level level, BlockPos hitPos, Direction hitDirection) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        Set<BlockPos> pulsePositions = new LinkedHashSet<>();
        pulsePositions.add(hitPos.immutable());
        pulsePositions.add(hitPos.relative(hitDirection).immutable());
        for (Direction direction : Direction.values()) {
            pulsePositions.add(hitPos.relative(direction).immutable());
        }

        boolean started = false;
        for (BlockPos pulsePos : pulsePositions) {
            started |= pulseVirtualSignal(serverLevel, pulsePos);
        }

        if (started) {
            spawnPulseFeedback(serverLevel, hitPos);
        }
    }

    private static boolean pulseVirtualSignal(ServerLevel level, BlockPos pos) {
        for (ActiveRedstonePulse pulse : ACTIVE_PULSES) {
            if (pulse.dimension.equals(level.dimension()) && pulse.pos.equals(pos)) {
                pulse.ticksLeft = REDSTONE_PULSE_TICKS;
                updateNeighborsForPulse(level, pos);
                return true;
            }
        }

        ACTIVE_PULSES.add(new ActiveRedstonePulse(level.dimension(), pos.immutable(), REDSTONE_PULSE_TICKS));
        updateNeighborsForPulse(level, pos);
        return true;
    }

    private static void updateNeighborsForPulse(ServerLevel level, BlockPos pulsePos) {
        Block block = level.getBlockState(pulsePos).getBlock();
        level.updateNeighborsAt(pulsePos, block);
        for (Direction direction : Direction.values()) {
            BlockPos neighbor = pulsePos.relative(direction);
            level.updateNeighborsAt(neighbor, block);
            level.neighborChanged(neighbor, block, pulsePos);
        }
    }

    private static void spawnPulseFeedback(ServerLevel level, BlockPos pulsePos) {
        level.playSound(null, pulsePos, SoundEvents.REDSTONE_TORCH_BURNOUT, SoundSource.BLOCKS, 0.25F, 1.65F);
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK,
                pulsePos.getX() + 0.5D, pulsePos.getY() + 0.5D, pulsePos.getZ() + 0.5D,
                6, 0.25D, 0.18D, 0.25D, 0.02D);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.translatable("tooltip.gwrexpansions.redstone_bullet.desc").withStyle(ChatFormatting.GRAY));
    }

    private static class ActiveRedstonePulse {
        private final ResourceKey<Level> dimension;
        private final BlockPos pos;
        private int ticksLeft;

        private ActiveRedstonePulse(ResourceKey<Level> dimension, BlockPos pos, int ticksLeft) {
            this.dimension = dimension;
            this.pos = pos;
            this.ticksLeft = ticksLeft;
        }
    }
}
