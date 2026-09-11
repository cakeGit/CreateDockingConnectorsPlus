package com.cake.docking_connectors_plus.content.blocks.docking_pipe_connector;

import com.simibubi.create.content.fluids.FluidTransportBehaviour;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class DockingPipeConnectorFluidBehaviour extends FluidTransportBehaviour {

    public DockingPipeConnectorFluidBehaviour(final DockingPipeConnectorBlockEntity blockEntity) {
        super(blockEntity);
    }

    @Override
    public boolean canHaveFlowToward(final BlockState state, final Direction direction) {
        final Direction facing = state.getValue(BlockStateProperties.FACING);
        if (direction == facing.getOpposite())
            return true;
        if (direction == facing)
            return ((DockingPipeConnectorBlockEntity) this.blockEntity).isLocked();
        return false;
    }
}
