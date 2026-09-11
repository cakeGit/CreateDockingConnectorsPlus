package com.cake.docking_connectors_plus.content.blocks.docking_pipe_connector;

import net.createmod.catnip.math.BlockFace;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class DockingFluidLink {

    @Nullable
    public static DockingPipeConnectorBlockEntity getLinkedConnector(final BlockGetter level, final BlockPos pos, final Direction face) {
        if (level == null)
            return null;

        final BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof DockingPipeConnectorBlock))
            return null;
        if (state.getValue(BlockStateProperties.FACING) != face)
            return null;
        if (!(level.getBlockEntity(pos) instanceof final DockingPipeConnectorBlockEntity connector))
            return null;
        if (!connector.isLocked())
            return null;

        final DockingPipeConnectorBlockEntity other = connector.getOtherConnector();
        if (other == null || !other.isLocked())
            return null;
        if (!Objects.equals(other.getOtherConnectorPosition(), pos))
            return null;

        return other;
    }

    public static BlockPos resolveNeighbour(final BlockGetter level, final BlockPos pos, final Direction face) {
        final DockingPipeConnectorBlockEntity other = getLinkedConnector(level, pos, face);
        return other == null ? pos.relative(face) : other.getBlockPos();
    }

    public static BlockPos resolveConnectedPos(final BlockGetter level, final BlockFace blockFace) {
        return resolveNeighbour(level, blockFace.getPos(), blockFace.getFace());
    }

    public static BlockFace resolve(final BlockGetter level, final BlockFace blockFace) {
        return new BlockFace(resolveConnectedPos(level, blockFace), blockFace.getFace());
    }

    public static BlockFace resolveOpposite(final BlockGetter level, final BlockFace blockFace) {
        return new BlockFace(resolveConnectedPos(level, blockFace), blockFace.getOppositeFace());
    }
}
