package com.cake.docking_connectors_plus.content.blocks.docking_connector.shared;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public abstract class PairedDockingConnectorBlock extends DirectionalBlock {

    private static final VoxelShape[] SHAPES = {
            box(0.0, -16.0, 0.0, 16.0, 16.0, 16.0),
            box(0.0, 0.0, 0.0, 16.0, 32.0, 16.0),
            box(0.0, 0.0, -16.0, 16.0, 16.0, 16.0),
            box(0.0, 0.0, 0.0, 16.0, 16.0, 32.0),
            box(-16.0, 0.0, 0.0, 16.0, 16.0, 16.0),
            box(0.0, 0.0, 0.0, 32.0, 16.0, 16.0),
    };

    protected PairedDockingConnectorBlock(final Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    protected abstract Supplier<? extends Block> getMainConnectorBlock();

    @Override
    protected @NotNull VoxelShape getShape(final @NotNull BlockState state, final @NotNull BlockGetter level, final @NotNull BlockPos pos, final @NotNull CollisionContext context) {
        return SHAPES[state.getValue(FACING).get3DDataValue()];
    }

    @Override
    protected VoxelShape getBlockSupportShape(final BlockState state, final BlockGetter level, final BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    protected @NotNull RenderShape getRenderShape(final @NotNull BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected @NotNull BlockState updateShape(final BlockState state, final @NotNull Direction direction, final @NotNull BlockState neighborState, final @NotNull LevelAccessor level, final @NotNull BlockPos pos, final @NotNull BlockPos neighborPos) {
        final Direction facing = state.getValue(FACING);
        if (facing != direction) {
            return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
        }

        if (neighborState.is(this.getMainConnectorBlock().get()) && neighborState.getValue(BlockStateProperties.FACING) == facing.getOpposite()) {
            return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
        }

        return Blocks.AIR.defaultBlockState();
    }

    @Override
    public @NotNull BlockState playerWillDestroy(final @NotNull Level level, final @NotNull BlockPos pos, final @NotNull BlockState state, final @NotNull Player player) {
        if (!level.isClientSide()) {
            if (player.hasInfiniteMaterials()) {
                final BlockPos connectorPos = pos.relative(state.getValue(FACING));
                final BlockState connectorState = level.getBlockState(connectorPos);
                if (connectorState.is(this.getMainConnectorBlock().get())) {
                    level.setBlock(connectorPos, Blocks.AIR.defaultBlockState(), 3);
                }
            } else {
                dropResources(state, level, pos, null, player, player.getMainHandItem());
            }
        }

        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void playerDestroy(final @NotNull Level level, final @NotNull Player player, final @NotNull BlockPos pos, final @NotNull BlockState state, @Nullable final BlockEntity blockEntity, final @NotNull ItemStack tool) {
        super.playerDestroy(level, player, pos, Blocks.AIR.defaultBlockState(), blockEntity, tool);
    }

    @Override
    protected boolean canSurvive(final BlockState state, final LevelReader level, final BlockPos pos) {
        final Direction facing = state.getValue(FACING);
        final BlockState connectorBlock = level.getBlockState(pos.relative(facing));
        return connectorBlock.is(this.getMainConnectorBlock().get()) && connectorBlock.getValue(BlockStateProperties.FACING) == facing.getOpposite() && connectorBlock.getValue(DockingConnector.POWERED);
    }

    @Override
    protected void createBlockStateDefinition(final StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(FACING));
    }

    @Override
    public @Nullable BlockState getStateForPlacement(final @NotNull BlockPlaceContext context) {
        return null;
    }

    @Override
    public @NotNull BlockState rotate(final BlockState state, final Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public @NotNull BlockState mirror(final BlockState state, final Mirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
    }

    @Override
    public @NotNull ItemStack getCloneItemStack(final @NotNull LevelReader level, final @NotNull BlockPos pos, final @NotNull BlockState state) {
        return new ItemStack(this.getMainConnectorBlock().get());
    }

    @Override
    protected abstract @NotNull MapCodec<? extends DirectionalBlock> codec();
}
