package com.cake.docking_connectors_plus.mixin;

import com.cake.docking_connectors_plus.content.blocks.docking_pipe_connector.DockingFluidLink;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.fluids.pump.PumpBlockEntity;
import net.createmod.catnip.math.BlockFace;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PumpBlockEntity.class)
public abstract class PumpBlockEntityMixin {

    @WrapOperation(method = "distributePressureTo", at = @At(value = "INVOKE", target = "Lnet/createmod/catnip/math/BlockFace;getConnectedPos()Lnet/minecraft/core/BlockPos;"))
    private BlockPos dockingPipeConnector$resolvePressureFrontier(final BlockFace instance, final Operation<BlockPos> original) {
        return DockingFluidLink.resolveConnectedPos(((PumpBlockEntity) (Object) this).getLevel(), instance);
    }

    @WrapOperation(method = "hasReachedValidEndpoint", at = @At(value = "INVOKE", target = "Lnet/createmod/catnip/math/BlockFace;getConnectedPos()Lnet/minecraft/core/BlockPos;"))
    private BlockPos dockingPipeConnector$resolveEndpoint(final BlockFace instance, final Operation<BlockPos> original, @Local(argsOnly = true, ordinal = 0) final LevelAccessor world) {
        return DockingFluidLink.resolveConnectedPos(world, instance);
    }

    @WrapOperation(method = "searchForEndpointRecursively", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos;relative(Lnet/minecraft/core/Direction;)Lnet/minecraft/core/BlockPos;"))
    private BlockPos dockingPipeConnector$resolveRecursion(final BlockPos instance, final Direction direction, final Operation<BlockPos> original) {
        return DockingFluidLink.resolveNeighbour(((PumpBlockEntity) (Object) this).getLevel(), instance, direction);
    }
}
