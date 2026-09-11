package com.cake.more_docking_connectors.content.blocks.docking_connector.shared;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Objects;
import java.util.function.Supplier;

public class DockingConnectorBlockOps {

    public static boolean onRemove(final BlockState state, final Level level, final BlockPos pos, final BlockState newState, final boolean isMoving, final Supplier<? extends Block> markerBlock) {
        final boolean blockChanged = !state.is(newState.getBlock());

        if (state.getValue(DockingConnector.POWERED) && (blockChanged || state.getValue(BlockStateProperties.FACING) != newState.getValue(BlockStateProperties.FACING))) {
            final BlockPos pairedConnectorPos = pos.relative(state.getValue(BlockStateProperties.FACING));
            if (level.getBlockState(pairedConnectorPos).is(markerBlock.get())) {
                level.removeBlock(pairedConnectorPos, isMoving);
            }
        }

        return blockChanged;
    }

    public static void neighborChanged(final BlockState state, final Level level, final BlockPos pos) {
        if (level.isClientSide()) {
            return;
        }

        if (level.getBlockEntity(pos) instanceof final DockingConnector connector) {
            connector.updateSignal();
        }

        final boolean previouslyPowered = state.getValue(DockingConnector.POWERED);
        if (previouslyPowered != level.hasNeighborSignal(pos)) {
            level.setBlock(pos, state.cycle(DockingConnector.POWERED), 2);
        }
    }

    public static VoxelShape getShape(final BlockState state, final BlockGetter level, final BlockPos pos) {
        return level.getBlockEntity(pos) instanceof final DockingConnector connector && !connector.isRetracted() ? Shapes.create(connector.getBoundingBox(state)) : Shapes.block();
    }

    public static int getAnalogOutputSignal(final Level level, final BlockPos pos) {
        if (!(level.getBlockEntity(pos) instanceof final DockingConnector connector)) {
            return 0;
        }

        if (!connector.isExtended()) {
            return 0;
        }

        if (connector.hasOtherConnector()) {
            return 15;
        }

        return Math.min(14, Math.max(0, 14 - (int) (14 * connector.getClosestPairDistance() / 4.0)));
    }

    public static void afterMove(final ServerLevel originLevel, final ServerLevel resultingLevel, final BlockPos oldPos, final BlockPos newPos, final Block block) {
        if (!(originLevel.getBlockEntity(oldPos) instanceof final DockingConnector connector) ||
                !connector.hasOtherConnector() ||
                !(originLevel.getBlockEntity(connector.getOtherConnectorPosition()) instanceof final DockingConnector connected) ||
                !Objects.equals(connected.getOtherConnectorPosition(), oldPos) ||
                !(resultingLevel.getBlockEntity(newPos) instanceof final DockingConnector newConnector)) {
            return;
        }

        connector.unDock();
        connected.unDock();
        newConnector.unDock();
        newConnector.pairTo(connected);

        resultingLevel.blockEvent(newPos, block, 1, 0);
    }
}
