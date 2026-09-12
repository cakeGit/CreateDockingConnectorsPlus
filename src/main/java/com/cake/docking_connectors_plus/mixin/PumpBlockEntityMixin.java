package com.cake.docking_connectors_plus.mixin;

import com.cake.docking_connectors_plus.content.blocks.docking_pipe_connector.DockingFluidLink;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.fluids.FluidTransportBehaviour;
import com.simibubi.create.content.fluids.pump.PumpBlockEntity;
import dev.ryanhcode.sable.util.LevelAccelerator;
import net.createmod.catnip.math.BlockFace;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PumpBlockEntity.class)
public abstract class PumpBlockEntityMixin {

    @Unique
    private LevelAccelerator dockingConnectorsPlus$levelAccelerator;

    @Unique
    private LevelAccelerator dockingConnectorsPlus$accelerator() {
        if (this.dockingConnectorsPlus$levelAccelerator == null) {
            final Level level = ((PumpBlockEntity) (Object) this).getLevel();
            if (level != null) {
                this.dockingConnectorsPlus$levelAccelerator = new LevelAccelerator(level);
            }
        }

        return this.dockingConnectorsPlus$levelAccelerator;
    }

    @Inject(method = "distributePressureTo", at = @At("RETURN"))
    private void dockingConnectorsPlus$clearAccelerator(final CallbackInfo ci) {
        this.dockingConnectorsPlus$levelAccelerator = null;
    }

    @WrapOperation(method = "distributePressureTo", at = @At(value = "INVOKE", target = "Lnet/createmod/catnip/math/BlockFace;getConnectedPos()Lnet/minecraft/core/BlockPos;"))
    private BlockPos dockingPipeConnector$resolvePressureFrontier(final BlockFace instance, final Operation<BlockPos> original) {
        return DockingFluidLink.resolveConnectedPos(this.dockingConnectorsPlus$accelerator(), instance);
    }

    @WrapOperation(method = "distributePressureTo", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/Direction;getOpposite()Lnet/minecraft/core/Direction;", ordinal = 2))
    private Direction dockingPipeConnector$resolvePressureGraphFace(final Direction instance, final Operation<Direction> original, @Local(ordinal = 0) final BlockPos currentPos) {
        return DockingFluidLink.resolvePortFace(this.dockingConnectorsPlus$accelerator(), currentPos, instance, original.call(instance));
    }

    @WrapOperation(method = "hasReachedValidEndpoint", at = @At(value = "INVOKE", target = "Lnet/createmod/catnip/math/BlockFace;getConnectedPos()Lnet/minecraft/core/BlockPos;"))
    private BlockPos dockingPipeConnector$resolveEndpoint(final BlockFace instance, final Operation<BlockPos> original) {
        return DockingFluidLink.resolveConnectedPos(this.dockingConnectorsPlus$accelerator(), instance);
    }

    @WrapOperation(method = "hasReachedValidEndpoint", at = @At(value = "INVOKE", target = "Lcom/simibubi/create/content/fluids/FluidTransportBehaviour;canHaveFlowToward(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;)Z"))
    private boolean dockingPipeConnector$resolveEndpointFace(final FluidTransportBehaviour pipe, final BlockState state, final Direction direction, final Operation<Boolean> original, @Local(argsOnly = true, ordinal = 0) final BlockFace blockFace) {
        if (DockingFluidLink.isCrossing(this.dockingConnectorsPlus$accelerator(), blockFace.getPos(), direction.getOpposite(), pipe)) {
            return true;
        }

        return original.call(pipe, state, direction);
    }

    @WrapOperation(method = "searchForEndpointRecursively", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/BlockPos;relative(Lnet/minecraft/core/Direction;)Lnet/minecraft/core/BlockPos;"))
    private BlockPos dockingPipeConnector$resolveRecursion(final BlockPos instance, final Direction direction, final Operation<BlockPos> original) {
        return DockingFluidLink.resolveNeighbour(this.dockingConnectorsPlus$accelerator(), instance, direction);
    }

    @WrapOperation(method = "searchForEndpointRecursively", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/Direction;getOpposite()Lnet/minecraft/core/Direction;"))
    private Direction dockingPipeConnector$resolveRecursionFace(final Direction instance, final Operation<Direction> original, @Local(ordinal = 0) final BlockPos currentPos) {
        return DockingFluidLink.resolvePortFace(this.dockingConnectorsPlus$accelerator(), currentPos, instance, original.call(instance));
    }
}
