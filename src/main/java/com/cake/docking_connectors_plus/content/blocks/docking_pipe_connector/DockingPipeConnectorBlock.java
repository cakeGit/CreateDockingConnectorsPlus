package com.cake.docking_connectors_plus.content.blocks.docking_pipe_connector;

import com.cake.docking_connectors_plus.content.blocks.docking_connector.shared.DockingConnector;
import com.cake.docking_connectors_plus.content.blocks.docking_connector.shared.DockingConnectorBlockOps;
import com.cake.docking_connectors_plus.index.DockingConnectorBlockEntities;
import com.cake.docking_connectors_plus.index.DockingConnectorBlocks;
import com.simibubi.create.content.fluids.FluidPropagator;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.block.WrenchableDirectionalBlock;
import dev.ryanhcode.sable.api.block.BlockSubLevelAssemblyListener;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.ticks.TickPriority;
import org.jetbrains.annotations.NotNull;

public class DockingPipeConnectorBlock extends WrenchableDirectionalBlock implements IBE<DockingPipeConnectorBlockEntity>, BlockSubLevelAssemblyListener {

    public static final BooleanProperty POWERED = DockingConnector.POWERED;
    public static final BooleanProperty EXTENDED = DockingConnector.EXTENDED;

    public DockingPipeConnectorBlock(final Properties properties) {
        super(properties);
        this.registerDefaultState(this.defaultBlockState().setValue(POWERED, false).setValue(EXTENDED, false));
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED, EXTENDED);
        super.createBlockStateDefinition(builder);
    }

    @Override
    public BlockState getStateForPlacement(final BlockPlaceContext context) {
        Direction nearestLookingDirection = context.getNearestLookingDirection();
        final Player player = context.getPlayer();
        if (player != null && player.isShiftKeyDown()) {
            nearestLookingDirection = nearestLookingDirection.getOpposite();
        }

        return super.getStateForPlacement(context).setValue(POWERED, context.getLevel().hasNeighborSignal(context.getClickedPos())).setValue(FACING, nearestLookingDirection.getOpposite());
    }

    @Override
    public void onRemove(final @NotNull BlockState state, final @NotNull Level level, final @NotNull BlockPos pos, final @NotNull BlockState newState, final boolean isMoving) {
        final boolean blockChanged = DockingConnectorBlockOps.onRemove(state, level, pos, newState, isMoving, () -> DockingConnectorBlocks.PAIRED_DOCKING_PIPE_CONNECTOR.get());

        if (blockChanged) {
            if (!level.isClientSide()) {
                FluidPropagator.propagateChangedPipe(level, pos, state);
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Override
    public void onPlace(final @NotNull BlockState state, final @NotNull Level level, final @NotNull BlockPos pos, final @NotNull BlockState oldState, final boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (level.isClientSide() || state == oldState) {
            return;
        }
        level.scheduleTick(pos, this, 1, TickPriority.HIGH);
    }

    @Override
    public void neighborChanged(final @NotNull BlockState state, final Level level, final @NotNull BlockPos pos, final @NotNull Block block, final @NotNull BlockPos fromPos, final boolean isMoving) {
        DockingConnectorBlockOps.neighborChanged(state, level, pos);

        if (level.isClientSide() || !isFluidPortNeighbour(state, pos, fromPos)) {
            return;
        }
        level.scheduleTick(pos, this, 1, TickPriority.HIGH);
    }

    @Override
    public void tick(final @NotNull BlockState state, final @NotNull ServerLevel level, final @NotNull BlockPos pos, final @NotNull RandomSource random) {
        FluidPropagator.propagateChangedPipe(level, pos, state);
    }

    private static boolean isFluidPortNeighbour(final BlockState state, final BlockPos pos, final BlockPos fromPos) {
        final Direction facing = state.getValue(FACING);
        return fromPos.equals(pos.relative(facing)) || fromPos.equals(pos.relative(facing.getOpposite()));
    }

    @Override
    public @NotNull VoxelShape getShape(final @NotNull BlockState state, final BlockGetter level, final @NotNull BlockPos pos, final @NotNull CollisionContext context) {
        return DockingConnectorBlockOps.getShape(state, level, pos);
    }

    @Override
    public @NotNull VoxelShape getBlockSupportShape(final @NotNull BlockState state, final @NotNull BlockGetter level, final @NotNull BlockPos pos) {
        return Shapes.block();
    }

    @Override
    public Class<DockingPipeConnectorBlockEntity> getBlockEntityClass() {
        return DockingPipeConnectorBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends DockingPipeConnectorBlockEntity> getBlockEntityType() {
        return DockingConnectorBlockEntities.DOCKING_PIPE_CONNECTOR.get();
    }

    @Override
    protected boolean triggerEvent(final @NotNull BlockState state, final @NotNull Level level, final @NotNull BlockPos pos, final int id, final int param) {
        super.triggerEvent(state, level, pos, id, param);
        final BlockEntity be = level.getBlockEntity(pos);
        return be != null && be.triggerEvent(id, param);
    }

    @Override
    public boolean hasAnalogOutputSignal(final BlockState pState) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(final BlockState pState, final Level pLevel, final BlockPos pPos) {
        return DockingConnectorBlockOps.getAnalogOutputSignal(pLevel, pPos);
    }

    @Override
    public void afterMove(final ServerLevel originLevel, final ServerLevel resultingLevel, final BlockState newState, final BlockPos oldPos, final BlockPos newPos) {
        DockingConnectorBlockOps.afterMove(originLevel, resultingLevel, oldPos, newPos, this);
    }
}
