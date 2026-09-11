package com.cake.docking_connectors_plus.mixin;

import com.cake.docking_connectors_plus.content.blocks.docking_kinetic_connector.DockingKineticConnectorBlockEntity;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.simibubi.create.content.kinetics.RotationPropagator;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RotationPropagator.class)
public abstract class RotationPropagatorDestroyGuardMixin {

    @WrapOperation(method = "propagateNewSource", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;destroyBlock(Lnet/minecraft/core/BlockPos;Z)Z"))
    private static boolean dockingKineticConnector$guardDestroy(final Level instance, final BlockPos pos, final boolean dropBlock, final Operation<Boolean> original) {
        if (instance.getBlockEntity(pos) instanceof final DockingKineticConnectorBlockEntity connector) {
            connector.onSpeedMismatch();
            return false;
        }

        return original.call(instance, pos, dropBlock);
    }
}
