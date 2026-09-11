package com.cake.more_docking_connectors.mixin;

import com.cake.more_docking_connectors.content.blocks.docking_pipe_connector.DockingFluidLink;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.fluids.PipeConnection;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PipeConnection.class)
public abstract class PipeConnectionMixin {

    @WrapOperation(method = "determineSource", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos;relative(Lnet/minecraft/core/Direction;)Lnet/minecraft/core/BlockPos;"))
    private BlockPos dockingPipeConnector$resolveDockNeighbour(final BlockPos instance, final Direction direction, final Operation<BlockPos> original, @Local(argsOnly = true, ordinal = 0) final Level level) {
        return DockingFluidLink.resolveNeighbour(level, instance, direction);
    }
}
