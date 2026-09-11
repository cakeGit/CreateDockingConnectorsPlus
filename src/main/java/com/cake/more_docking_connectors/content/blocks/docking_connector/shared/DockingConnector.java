package com.cake.more_docking_connectors.content.blocks.docking_connector.shared;

import dev.simulated_team.simulated.content.blocks.redstone_magnet.SimMagnet;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaterniondc;
import org.joml.Vector3dc;

import java.util.UUID;
import java.util.function.Supplier;

public interface DockingConnector extends SimMagnet {

    BooleanProperty POWERED = BlockStateProperties.POWERED;
    BooleanProperty EXTENDED = BlockStateProperties.EXTENDED;

    BlockPos getBlockPos();

    @Nullable
    Level getLevel();

    BlockState getBlockState();

    @Nullable
    BlockPos getOtherConnectorPosition();

    void setOtherConnectorPosition(@Nullable BlockPos pos);

    @Nullable
    UUID getOtherConnectorSubLevelId();

    void setOtherConnectorSubLevelId(@Nullable UUID id);

    DockingConnectorState getDockingState();

    void setDockingState(DockingConnectorState state);

    boolean isExtended();

    boolean isRetracted();

    boolean isFeetExtended();

    boolean isPowered();

    default boolean isLocked() {
        return this.getDockingState() == DockingConnectorState.LOCKED;
    }

    default boolean hasOtherConnector() {
        return this.getOtherConnectorPosition() != null;
    }

    double getClosestPairDistance();

    AABB getBoundingBox(BlockState state);

    float getExtensionDistance(float partialTick);

    float getFeetValue(float partialTick);

    float getFeetRotation(float partialTick);

    void updateSignal();

    boolean hasActiveConstraint();

    void setDock(DockingConnector other, boolean isLocked, @Nullable Quaterniondc targetOrientation, @Nullable Vector3dc relativePos, @Nullable Quaterniondc relativeOrientation);

    void unDock();

    void pairTo(DockingConnector other);

    void onDockingLinkLocked();

    void onDockingLinkBroken();

    Supplier<? extends Block> getPairedMarkerBlock();

    default boolean isPairedMarker(final BlockState state) {
        return state.is(this.getPairedMarkerBlock().get());
    }

    Vec3 getTipPosition();
}
