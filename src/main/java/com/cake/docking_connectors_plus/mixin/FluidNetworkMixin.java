package com.cake.docking_connectors_plus.mixin;

import com.cake.docking_connectors_plus.content.blocks.docking_pipe_connector.DockingFluidLink;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.fluids.FluidNetwork;
import net.createmod.catnip.math.BlockFace;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FluidNetwork.class)
public abstract class FluidNetworkMixin {

    @Shadow
    Level world;

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/createmod/catnip/math/BlockFace;getConnectedPos()Lnet/minecraft/core/BlockPos;"))
    private BlockPos dockingPipeConnector$resolveVisited(final BlockFace instance, final Operation<BlockPos> original) {
        return DockingFluidLink.resolveConnectedPos(this.world, instance);
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lnet/createmod/catnip/math/BlockFace;getOpposite()Lnet/createmod/catnip/math/BlockFace;"))
    private BlockFace dockingPipeConnector$resolveQueued(final BlockFace instance, final Operation<BlockFace> original) {
        return DockingFluidLink.resolveOpposite(this.world, instance);
    }
}
