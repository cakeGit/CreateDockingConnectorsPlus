package com.cake.docking_connectors_plus.content.blocks.docking_pipe_connector;

import com.cake.docking_connectors_plus.content.blocks.docking_connector.shared.DockingConnector;
import com.cake.docking_connectors_plus.content.blocks.docking_connector.shared.DockingConnectorCore;
import com.cake.docking_connectors_plus.content.blocks.docking_connector.shared.DockingConnectorExtension;
import com.cake.docking_connectors_plus.content.blocks.docking_connector.shared.DockingConnectorState;
import com.cake.docking_connectors_plus.index.DockingConnectorBlocks;
import com.simibubi.create.content.fluids.FluidPropagator;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.block.BlockEntitySubLevelActor;
import dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.simulated_team.simulated.content.blocks.redstone_magnet.MagnetMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaterniondc;
import org.joml.Quaternionfc;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class DockingPipeConnectorBlockEntity extends SmartBlockEntity implements DockingConnector, BlockEntitySubLevelActor {
    public static MagnetMap<DockingPipeConnectorBlockEntity> MAGNET_CONTROLLER = new MagnetMap<>();
    private DockingConnectorCore<DockingPipeConnectorBlockEntity> dockingCore;
    private DockingConnectorExtension<DockingPipeConnectorBlockEntity> extension;
    public DockingPipeConnectorFluidBehaviour fluidBehaviour;

    public DockingPipeConnectorBlockEntity(final BlockEntityType<?> type, final BlockPos pos, final BlockState state) {
        super(type, pos, state);
    }

    @Nullable
    public DockingPipeConnectorBlockEntity getOtherConnector() {
        return this.dockingCore.getOtherConnector();
    }

    @Override
    public void addBehaviours(final List<BlockEntityBehaviour> behaviours) {
        this.dockingCore = new DockingConnectorCore<>(this, MAGNET_CONTROLLER, DockingPipeConnectorBlockEntity.class);
        behaviours.add(this.dockingCore.createBehaviour());
        this.extension = new DockingConnectorExtension<>(this);
        this.fluidBehaviour = new DockingPipeConnectorFluidBehaviour(this);
        behaviours.add(this.fluidBehaviour);
    }

    @Override
    public void initialize() {
        super.initialize();
        this.dockingCore.initialize();
    }

    @Override
    public void tick() {
        super.tick();

        this.extension.tick();
        this.dockingCore.tick();
        this.dockingCore.updateState();
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        this.extension.lazyTick();
    }

    public void setVirtualLock(final boolean lock) {
        this.extension.setVirtualLock(lock);
    }

    @Override
    public void pairTo(final DockingConnector other) {
        this.dockingCore.pairTo(other);
    }

    @Override
    public void setDock(final DockingConnector otherConnector, final boolean isLocked, @Nullable final Quaterniondc targetOrientation, @Nullable final Vector3dc relativePos, @Nullable final Quaterniondc relativeOrientation) {
        this.dockingCore.setDock(otherConnector, isLocked, targetOrientation, relativePos, relativeOrientation);
    }

    @Override
    public void unDock() {
        this.dockingCore.unDock();
    }

    @Override
    public void onDockingLinkLocked() {
        this.fluidBehaviour.wipePressure();
        this.level.updateNeighborsAt(this.worldPosition, this.getBlockState().getBlock());
        if (!this.level.isClientSide()) {
            FluidPropagator.propagateChangedPipe(this.level, this.getBlockPos(), this.getBlockState());
        }
    }

    @Override
    public void onDockingLinkBroken() {
        if (!this.level.isClientSide() && this.hasOtherConnector()) {
            final Direction facing = this.getBlockState().getValue(BlockStateProperties.FACING);
            FluidPropagator.resetAffectedFluidNetworks(this.level, this.getBlockPos(), facing);
            FluidPropagator.resetAffectedFluidNetworks(this.level, this.getBlockPos(), facing.getOpposite());
        }
        this.fluidBehaviour.wipePressure();
        if (!this.level.isClientSide()) {
            FluidPropagator.propagateChangedPipe(this.level, this.getBlockPos(), this.getBlockState());
        }
    }

    @Override
    public boolean isExtended() {
        return this.extension.isExtended();
    }

    @Override
    public boolean isRetracted() {
        return this.extension.isRetracted();
    }

    @Override
    public boolean isFeetExtended() {
        return this.extension.isFeetExtended();
    }

    @Override
    public boolean isPowered() {
        return this.extension.isPowered();
    }

    @Override
    public float getExtensionDistance(final float partialTick) {
        return this.extension.getExtensionDistance(partialTick);
    }

    @Override
    public float getFeetValue(final float partialTick) {
        return this.extension.getFeetValue(partialTick);
    }

    @Override
    public float getFeetRotation(final float partialTick) {
        return this.extension.getFeetRotation(partialTick);
    }

    public float getPlateOffset() {
        return this.extension.getPlateOffset();
    }

    @Override
    public void updateSignal() {
        this.extension.updateSignal();
    }

    @Override
    protected void write(final CompoundTag tag, final HolderLookup.Provider registries, final boolean clientPacket) {
        this.extension.write(tag);
        this.dockingCore.writeDocking(tag);

        super.write(tag, registries, clientPacket);
    }

    @Override
    protected void read(final CompoundTag tag, final HolderLookup.Provider registries, final boolean clientPacket) {
        this.extension.read(tag);
        this.dockingCore.readDocking(tag);

        super.read(tag, registries, clientPacket);
    }

    @Override
    public boolean triggerEvent(final int id, final int type) {
        return this.extension.triggerEvent(id) || super.triggerEvent(id, type);
    }

    @Override
    public void remove() {
        super.remove();
        this.dockingCore.removeConstraint();
    }

    @Override
    public Quaternionfc getOrientation() {
        return this.dockingCore.getOrientation();
    }

    @Override
    public SubLevel getLatestSubLevel() {
        return Sable.HELPER.getContaining(this);
    }

    @Override
    public Vector3d setMagneticMoment(final Vector3d v) {
        return this.dockingCore.setMagneticMoment(v);
    }

    @Override
    public Vec3 getMagnetPosition() {
        return this.dockingCore.getMagnetPosition();
    }

    @Override
    public Vec3 getTipPosition() {
        return this.dockingCore.getTipPosition();
    }

    @Override
    public boolean magnetActive() {
        return this.dockingCore.magnetActive();
    }

    @Override
    public AABB getBoundingBox(final BlockState state) {
        return Shulker.getProgressAabb(1, state.getValue(BlockStateProperties.FACING), this.getExtensionDistance(1.0F));
    }

    @Override
    public AABB createRenderBoundingBox() {
        return super.createRenderBoundingBox().inflate(1);
    }

    @Override
    public BlockPos getOtherConnectorPosition() {
        return this.dockingCore.getOtherConnectorPosition();
    }

    @Override
    public void setOtherConnectorPosition(@Nullable final BlockPos pos) {
        this.dockingCore.setOtherConnectorPosition(pos);
    }

    @Override
    public UUID getOtherConnectorSubLevelId() {
        return this.dockingCore.getOtherConnectorSubLevelId();
    }

    @Override
    public void setOtherConnectorSubLevelId(@Nullable final UUID id) {
        this.dockingCore.setOtherConnectorSubLevelId(id);
    }

    @Override
    public DockingConnectorState getDockingState() {
        return this.dockingCore.getDockingState();
    }

    @Override
    public void setDockingState(final DockingConnectorState state) {
        this.dockingCore.setDockingState(state);
    }

    @Override
    public double getClosestPairDistance() {
        return this.dockingCore.getClosestPairDistance();
    }

    @Override
    public boolean hasActiveConstraint() {
        return this.dockingCore.hasActiveConstraint();
    }

    @Override
    public Supplier<? extends Block> getPairedMarkerBlock() {
        return () -> DockingConnectorBlocks.PAIRED_DOCKING_PIPE_CONNECTOR.get();
    }

    @Override
    public void sable$physicsTick(final ServerSubLevel subLevel, final RigidBodyHandle handle, final double timeStep) {
        this.dockingCore.physicsTick();
    }

    @Override
    public @Nullable Iterable<@NotNull SubLevel> sable$getConnectionDependencies() {
        return this.dockingCore.getConnectionDependencies();
    }
}
