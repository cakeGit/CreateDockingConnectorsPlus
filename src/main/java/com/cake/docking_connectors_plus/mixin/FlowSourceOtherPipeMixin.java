package com.cake.docking_connectors_plus.mixin;

import com.cake.docking_connectors_plus.content.blocks.docking_pipe_connector.DockingFluidLink;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.simibubi.create.content.fluids.FlowSource;
import com.simibubi.create.content.fluids.FluidTransportBehaviour;
import net.createmod.catnip.math.BlockFace;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(FlowSource.OtherPipe.class)
public abstract class FlowSourceOtherPipeMixin {

    @WrapOperation(method = "manageSource", at = @At(value = "INVOKE", target = "Lnet/createmod/catnip/math/BlockFace;getConnectedPos()Lnet/minecraft/core/BlockPos;"))
    private BlockPos dockingPipeConnector$manageAcrossDock(final BlockFace instance, final Operation<BlockPos> original, @Local(argsOnly = true, ordinal = 0) final Level world) {
        return DockingFluidLink.resolveConnectedPos(world, instance);
    }

    @WrapOperation(method = "provideFluid", at = @At(value = "INVOKE", target = "Lnet/createmod/catnip/math/BlockFace;getOppositeFace()Lnet/minecraft/core/Direction;"))
    private Direction dockingPipeConnector$provideAcrossDock(final BlockFace instance, final Operation<Direction> original, @Local(ordinal = 0) final FluidTransportBehaviour behaviour) {
        return DockingFluidLink.resolveProvidedFace(instance, behaviour, original.call(instance));
    }
}
