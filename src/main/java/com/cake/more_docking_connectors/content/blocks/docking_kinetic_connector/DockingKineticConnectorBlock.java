package com.cake.more_docking_connectors.content.blocks.docking_kinetic_connector;

import com.cake.more_docking_connectors.content.blocks.docking_connector.shared.DockingConnector;
import com.cake.more_docking_connectors.content.blocks.docking_connector.shared.DockingConnectorBlockOps;
import com.cake.more_docking_connectors.index.DockingConnectorBlockEntities;
import com.cake.more_docking_connectors.index.DockingConnectorBlocks;
import com.simibubi.create.content.kinetics.base.DirectionalKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import dev.ryanhcode.sable.api.block.BlockSubLevelAssemblyListener;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

public class DockingKineticConnectorBlock extends DirectionalKineticBlock implements IBE<DockingKineticConnectorBlockEntity>, BlockSubLevelAssemblyListener {

    public static final BooleanProperty POWERED = DockingConnector.POWERED;
    public static final BooleanProperty EXTENDED = DockingConnector.EXTENDED;

    public DockingKineticConnectorBlock(final Properties properties) {
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
        return super.getStateForPlacement(context).setValue(POWERED, context.getLevel().hasNeighborSignal(context.getClickedPos()));
    }

    @Override
    public InteractionResult onWrenched(final BlockState state, final UseOnContext context) {
        if (this.getRotatedBlockState(state, context.getClickedFace()).equals(state)) {
            return super.onWrenched(state, context);
        }

        if (!context.getLevel().isClientSide()) {
            this.withBlockEntityDo(context.getLevel(), context.getClickedPos(), DockingConnector::unDock);
        }

        return super.onWrenched(state, context);
    }

    @Override
    public void onRemove(final @NotNull BlockState state, final @NotNull Level level, final @NotNull BlockPos pos, final @NotNull BlockState newState, final boolean isMoving) {
        DockingConnectorBlockOps.onRemove(state, level, pos, newState, isMoving, () -> DockingConnectorBlocks.PAIRED_DOCKING_KINETIC_CONNECTOR.get());
        super.onRemove(state, level, pos, newState, isMoving);
    }

    @Override
    public void neighborChanged(final @NotNull BlockState state, final Level level, final @NotNull BlockPos pos, final Block block, final @NotNull BlockPos fromPos, final boolean isMoving) {
        DockingConnectorBlockOps.neighborChanged(state, level, pos);
    }

    @Override
    public @NotNull VoxelShape getShape(final @NotNull BlockState state, final BlockGetter level, final @NotNull BlockPos pos, final @NotNull CollisionContext context) {
        return DockingConnectorBlockOps.getShape(state, level, pos);
    }

    @Override
    public @NotNull VoxelShape getBlockSupportShape(final @NotNull BlockState state, final BlockGetter level, final @NotNull BlockPos pos) {
        return Shapes.block();
    }

    @Override
    public Direction.Axis getRotationAxis(final BlockState state) {
        return state.getValue(FACING).getAxis();
    }

    @Override
    public boolean hasShaftTowards(final LevelReader level, final BlockPos pos, final BlockState state, final Direction face) {
        return face == state.getValue(FACING).getOpposite();
    }

    @Override
    public Class<DockingKineticConnectorBlockEntity> getBlockEntityClass() {
        return DockingKineticConnectorBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends DockingKineticConnectorBlockEntity> getBlockEntityType() {
        return DockingConnectorBlockEntities.DOCKING_KINETIC_CONNECTOR.get();
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
