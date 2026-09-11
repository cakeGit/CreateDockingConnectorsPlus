package com.cake.docking_connectors_plus.mixin;

import com.cake.docking_connectors_plus.content.blocks.docking_pipe_connector.DockingFluidLink;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.fluids.FluidPropagator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FluidPropagator.class)
public abstract class FluidPropagatorMixin {

    @WrapOperation(method = "propagateChangedPipe", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos;relative(Lnet/minecraft/core/Direction;)Lnet/minecraft/core/BlockPos;"))
    private static BlockPos dockingPipeConnector$propagateAcrossDock(final BlockPos instance, final Direction direction, final Operation<BlockPos> original, @Local(argsOnly = true, ordinal = 0) final LevelAccessor world) {
        return DockingFluidLink.resolveNeighbour(world, instance, direction);
    }

    @WrapOperation(method = "resetAffectedFluidNetworks", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos;relative(Lnet/minecraft/core/Direction;)Lnet/minecraft/core/BlockPos;"))
    private static BlockPos dockingPipeConnector$resetAcrossDock(final BlockPos instance, final Direction direction, final Operation<BlockPos> original, @Local(argsOnly = true, ordinal = 0) final Level world) {
        return DockingFluidLink.resolveNeighbour(world, instance, direction);
    }

    @WrapOperation(method = "isOpenEnd", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos;relative(Lnet/minecraft/core/Direction;)Lnet/minecraft/core/BlockPos;"))
    private static BlockPos dockingPipeConnector$openEndAcrossDock(final BlockPos instance, final Direction direction, final Operation<BlockPos> original, @Local(argsOnly = true, ordinal = 0) final BlockGetter reader) {
        return DockingFluidLink.resolveNeighbour(reader, instance, direction);
    }
}
