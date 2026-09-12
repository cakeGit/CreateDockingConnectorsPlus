package com.cake.docking_connectors_plus.mixin;

import com.cake.docking_connectors_plus.content.blocks.docking_pipe_connector.DockingFluidLink;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.fluids.FluidPropagator;
import com.simibubi.create.content.fluids.FluidTransportBehaviour;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FluidPropagator.class)
public abstract class FluidPropagatorMixin {

    @WrapOperation(method = "propagateChangedPipe", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos;relative(Lnet/minecraft/core/Direction;)Lnet/minecraft/core/BlockPos;"))
    private static BlockPos dockingPipeConnector$propagateAcrossDock(final BlockPos instance, final Direction direction, final Operation<BlockPos> original, @Local(argsOnly = true, ordinal = 0) final LevelAccessor world) {
        return DockingFluidLink.resolveNeighbour(world, instance, direction);
    }

    @WrapOperation(method = "propagateChangedPipe", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/fluids/FluidTransportBehaviour;canHaveFlowToward(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;)Z"))
    private static boolean dockingPipeConnector$propagateAcrossDockFace(final FluidTransportBehaviour pipe, final BlockState state, final Direction direction, final Operation<Boolean> original, @Local(argsOnly = true, ordinal = 0) final LevelAccessor world, @Local(ordinal = 1) final BlockPos currentPos) {
        if (DockingFluidLink.isCrossing(world, currentPos, direction.getOpposite(), pipe)) {
            return true;
        }

        return original.call(pipe, state, direction);
    }

    @WrapOperation(method = "resetAffectedFluidNetworks", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos;relative(Lnet/minecraft/core/Direction;)Lnet/minecraft/core/BlockPos;"))
    private static BlockPos dockingPipeConnector$resetAcrossDock(final BlockPos instance, final Direction direction, final Operation<BlockPos> original, @Local(argsOnly = true, ordinal = 0) final Level world) {
        return DockingFluidLink.resolveNeighbour(world, instance, direction);
    }

    @WrapOperation(method = "isOpenEnd", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos;relative(Lnet/minecraft/core/Direction;)Lnet/minecraft/core/BlockPos;"))
    private static BlockPos dockingPipeConnector$openEndAcrossDock(final BlockPos instance, final Direction direction, final Operation<BlockPos> original, @Local(argsOnly = true, ordinal = 0) final BlockGetter reader) {
        return DockingFluidLink.resolveNeighbour(reader, instance, direction);
    }

    @WrapOperation(method = "isOpenEnd", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/fluids/FluidTransportBehaviour;canHaveFlowToward(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;)Z"))
    private static boolean dockingPipeConnector$openEndAcrossDockFace(final FluidTransportBehaviour pipe, final BlockState state, final Direction direction, final Operation<Boolean> original, @Local(argsOnly = true, ordinal = 0) final BlockGetter reader, @Local(argsOnly = true, ordinal = 0) final BlockPos pos) {
        if (DockingFluidLink.isCrossing(reader, pos, direction.getOpposite(), pipe)) {
            return true;
        }

        return original.call(pipe, state, direction);
    }
}
