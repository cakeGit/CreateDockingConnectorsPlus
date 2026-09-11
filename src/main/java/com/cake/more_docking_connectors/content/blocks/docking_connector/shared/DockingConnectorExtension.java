package com.cake.more_docking_connectors.content.blocks.docking_connector.shared;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import dev.simulated_team.simulated.index.SimSoundEvents;
import dev.simulated_team.simulated.util.SimMathUtils;
import net.createmod.catnip.animation.LerpedFloat;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class DockingConnectorExtension<T extends SmartBlockEntity & DockingConnector> {

    private final T connector;
    private final LerpedFloat extension = LerpedFloat.linear().chase(0, 0.1, LerpedFloat.Chaser.LINEAR);
    private final LerpedFloat feet = LerpedFloat.linear().chase(0, 0.15, LerpedFloat.Chaser.LINEAR);
    private boolean powered;
    private boolean virtualLock;

    public DockingConnectorExtension(final T connector) {
        this.connector = connector;
    }

    public void tick() {
        final Level level = this.level();
        final BlockState state = this.connector.getBlockState();
        final Direction direction = state.getValue(BlockStateProperties.FACING);
        final BlockPos pos = this.connector.getBlockPos();

        final BlockPos frontPos = pos.relative(direction);
        final BlockState frontState = level.getBlockState(frontPos);

        this.powered = state.getValue(BlockStateProperties.POWERED);
        if (!frontState.isAir() && (!this.connector.isPairedMarker(frontState) || frontState.getValue(BlockStateProperties.FACING).getOpposite() != direction)) {
            this.powered = false;
        }

        final float previousExtensionTarget = this.extension.getChaseTarget();
        this.extension.updateChaseTarget(this.powered ? 1 : 0);
        if (this.extension.getChaseTarget() != previousExtensionTarget) {
            if (level.isClientSide()) {
                level.playLocalSound(
                        pos.getX() + 0.5,
                        pos.getY() + 0.5,
                        pos.getZ() + 0.5,
                        this.powered ? SimSoundEvents.DOCKING_CONNECTOR_EXTENDS.event() : SimSoundEvents.DOCKING_CONNECTOR_RETRACTS.event(),
                        SoundSource.BLOCKS,
                        0.75f,
                        1.0f,
                        false);
            } else {
                if (!this.powered && this.connector.isPairedMarker(frontState)) {
                    level.setBlock(frontPos, Blocks.AIR.defaultBlockState(), 3);
                }
            }
        }
        final boolean wasFullyExtended = this.extension.getValue() == 1.0;
        this.extension.tickChaser();
        final boolean isFullyExtended = this.extension.getValue() == 1.0;
        if (wasFullyExtended != isFullyExtended)
            level.setBlock(pos, this.connector.getBlockState().setValue(DockingConnector.EXTENDED, isFullyExtended), 6);
        this.feet.updateChaseTarget(this.connector.hasOtherConnector() || this.virtualLock ? 1 : 0);
        this.feet.tickChaser();
    }

    public void lazyTick() {
        if (this.connector.getDockingState() == DockingConnectorState.EXTENDED || this.connector.getDockingState() == DockingConnectorState.LOCKING) {
            this.level().updateNeighborsAt(this.connector.getBlockPos(), this.connector.getBlockState().getBlock());
        }
    }

    public void updateSignal() {
        final boolean shouldPower = this.level().hasNeighborSignal(this.connector.getBlockPos());
        if (this.powered != shouldPower) {
            this.powered = shouldPower;
            this.connector.sendData();
        }
    }

    public void setVirtualLock(final boolean lock) {
        this.virtualLock = lock;
    }

    public boolean isPowered() {
        return this.powered;
    }

    public boolean isExtended() {
        return this.extension.getValue() == 1 && this.powered;
    }

    public boolean isRetracted() {
        return this.extension.getValue() == 0;
    }

    public boolean isFeetExtended() {
        return this.connector.hasOtherConnector() && this.feet.getValue() == 1;
    }

    public float getExtensionDistance(final float partialTick) {
        return SimMathUtils.smoothStep(this.extension.getValue(partialTick));
    }

    public float getFeetValue(final float partialTick) {
        return this.feet.getValue(partialTick);
    }

    public float getFeetRotation(final float partialTick) {
        float rotation = this.feet.getValue(partialTick);
        final float rotationTarget = this.feet.getChaseTarget();
        if (rotationTarget == 1) {
            rotation *= rotation;
        }
        return rotation;
    }

    public float getPlateOffset() {
        return 0.5f + this.getExtensionDistance(0);
    }

    public void write(final CompoundTag tag) {
        tag.putBoolean("IsPowered", this.powered);
        tag.putFloat("Extension", this.extension.getValue());
        tag.putFloat("Target", this.extension.getChaseTarget());
        tag.putFloat("Feet", this.feet.getValue());
    }

    public void read(final CompoundTag tag) {
        this.powered = tag.getBoolean("IsPowered");
        this.extension.setValue(tag.getFloat("Extension"));
        this.extension.updateChaseTarget(tag.getFloat("Target"));
        this.feet.setValue(tag.getFloat("Feet"));

        this.extension.setValue(this.extension.getValue());
        this.feet.setValue(this.feet.getValue());
    }

    public boolean triggerEvent(final int id) {
        if (id == 1) {
            this.extension.updateChaseTarget(this.powered ? 1 : 0);
            this.feet.updateChaseTarget(this.connector.hasOtherConnector() ? 1 : 0);
            return true;
        }
        return false;
    }

    private Level level() {
        final Level level = this.connector.getLevel();
        if (level == null) {
            throw new IllegalStateException("Docking connector at " + this.connector.getBlockPos() + " has no level");
        }
        return level;
    }
}
