package juitar.gwrexpansions.mixin;

import juitar.gwrexpansions.item.vanilla.RedstoneBulletItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SignalGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DiodeBlock;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Level.class)
public abstract class SignalGetterMixin implements SignalGetter {
    @Override
    public int getSignal(BlockPos pos, Direction direction) {
        int signal = RedstoneBulletItem.getVirtualRedstoneSignal((Level) (Object) this, pos);
        if (signal > 0) {
            return signal;
        }

        BlockState state = getBlockState(pos);
        int blockSignal = state.getSignal(this, pos, direction);
        return state.shouldCheckWeakPower(this, pos, direction)
                ? Math.max(blockSignal, getDirectSignalTo(pos))
                : blockSignal;
    }

    @Override
    public int getDirectSignal(BlockPos pos, Direction direction) {
        int signal = RedstoneBulletItem.getVirtualRedstoneSignal((Level) (Object) this, pos);
        return signal > 0 ? signal : getBlockState(pos).getDirectSignal(this, pos, direction);
    }

    @Override
    public boolean hasSignal(BlockPos pos, Direction direction) {
        return getSignal(pos, direction) > 0;
    }

    @Override
    public boolean hasNeighborSignal(BlockPos pos) {
        if (RedstoneBulletItem.getVirtualNeighborRedstoneSignal((Level) (Object) this, pos) > 0) {
            return true;
        }

        return getSignal(pos.below(), Direction.DOWN) > 0
                || getSignal(pos.above(), Direction.UP) > 0
                || getSignal(pos.north(), Direction.NORTH) > 0
                || getSignal(pos.south(), Direction.SOUTH) > 0
                || getSignal(pos.west(), Direction.WEST) > 0
                || getSignal(pos.east(), Direction.EAST) > 0;
    }

    @Override
    public int getBestNeighborSignal(BlockPos pos) {
        int signal = RedstoneBulletItem.getVirtualNeighborRedstoneSignal((Level) (Object) this, pos);
        if (signal > 0) {
            return signal;
        }

        int bestSignal = 0;
        for (Direction direction : Direction.values()) {
            int neighborSignal = getSignal(pos.relative(direction), direction);
            if (neighborSignal >= 15) {
                return 15;
            }
            if (neighborSignal > bestSignal) {
                bestSignal = neighborSignal;
            }
        }
        return bestSignal;
    }

    @Override
    public int getControlInputSignal(BlockPos pos, Direction direction, boolean diodeMode) {
        int signal = RedstoneBulletItem.getVirtualRedstoneSignal((Level) (Object) this, pos);
        if (signal > 0) {
            return signal;
        }

        BlockState state = getBlockState(pos);
        if (diodeMode) {
            return DiodeBlock.isDiode(state) ? getDirectSignal(pos, direction) : 0;
        }
        if (state.is(Blocks.REDSTONE_BLOCK)) {
            return 15;
        }
        if (state.is(Blocks.REDSTONE_WIRE)) {
            return state.getValue(RedStoneWireBlock.POWER);
        }
        return state.isSignalSource() ? getDirectSignal(pos, direction) : 0;
    }
}
