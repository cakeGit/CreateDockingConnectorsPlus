package com.cake.more_docking_connectors.content.blocks.docking_connector.shared;

import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import dev.ryanhcode.sable.api.physics.constraint.FixedConstraintConfiguration;
import dev.ryanhcode.sable.api.physics.constraint.FixedConstraintHandle;
import dev.ryanhcode.sable.api.sublevel.ServerSubLevelContainer;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import dev.simulated_team.simulated.content.blocks.redstone_magnet.MagnetBehaviour;
import dev.simulated_team.simulated.content.blocks.redstone_magnet.MagnetMap;
import dev.simulated_team.simulated.content.blocks.redstone_magnet.MagnetPair;
import dev.simulated_team.simulated.content.blocks.redstone_magnet.MagnetPairIdentifier;
import dev.simulated_team.simulated.service.SimConfigService;
import dev.simulated_team.simulated.util.SimMovementContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.joml.Quaternionfc;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class DockingConnectorCore<T extends SmartBlockEntity & DockingConnector> {

    private final T connector;
    private final MagnetMap<T> magnetMap;
    private final Class<T> connectorClass;
    private FixedConstraintHandle constraintHandle;
    private ConstraintSmoother constraintSmoother;
    private BlockPos otherConnectorPosition;
    private UUID otherConnectorSubLevelId;
    private DockingConnectorState state = DockingConnectorState.UNPOWERED;
    private double closestPairDistance = 0;

    public DockingConnectorCore(final T connector, final MagnetMap<T> magnetMap, final Class<T> connectorClass) {
        this.connector = connector;
        this.magnetMap = magnetMap;
        this.connectorClass = connectorClass;
    }

    public MagnetBehaviour createBehaviour() {
        return new MagnetBehaviour(this.connector, this.magnetMap);
    }

    public void initialize() {
        final T otherConnector = this.getOtherConnector();

        if (otherConnector != null && this.constraintHandle == null && !otherConnector.hasActiveConstraint()) {
            if (this.magnetMap.getPair(this.level(), this.connector.getBlockPos(), this.otherConnectorPosition) == null) {
                this.magnetMap.tryAddPair(this.level(), this.connector.getBlockPos(), this.otherConnectorPosition, DockingConnectorPair::new);
                final DockingConnectorPair<?> pair = this.getPairAt(this.otherConnectorPosition);
                if (pair != null) {
                    pair.dock(true);
                    this.connector.notifyUpdate();
                }
            }
        }
    }

    public void tick() {
        final Level level = this.level();

        if (!level.isClientSide() && this.connector.isExtended()) {
            this.searchForPairs();

            final Direction direction = this.connector.getBlockState().getValue(BlockStateProperties.FACING);
            final BlockPos frontPos = this.connector.getBlockPos().relative(direction);
            if (!this.connector.isPairedMarker(level.getBlockState(frontPos))) {
                level.setBlock(frontPos, this.connector.getPairedMarkerBlock().get().defaultBlockState().setValue(BlockStateProperties.FACING, direction.getOpposite()), 3);
            }
        }

        if (this.otherConnectorPosition != null && !level.isClientSide) {
            final T other = this.getOtherConnector();
            if (other == null || !Objects.equals(other.getOtherConnectorPosition(), this.connector.getBlockPos())) {
                this.unDock();
                this.connector.setDockingState(DockingConnectorState.EXTENDED);
                this.connector.sendData();
            }
        }
    }

    @Nullable
    public T getOtherConnector() {
        if (this.otherConnectorPosition == null) {
            return null;
        }

        return this.asConnector(this.level().getBlockEntity(this.otherConnectorPosition));
    }

    public void searchForPairs() {
        final Direction direction = this.connector.getBlockState().getValue(BlockStateProperties.FACING);

        if (this.connector.hasOtherConnector()) {
            this.magnetMap.tryAddPair(this.level(), this.connector.getBlockPos(), this.otherConnectorPosition, DockingConnectorPair::new);
            return;
        }

        final Vector3d tempRelativePos = new Vector3d();
        this.closestPairDistance = Double.MAX_VALUE;

        final SubLevel subLevel = this.connector.getLatestSubLevel();
        final SimMovementContext context = SimMovementContext.getMovementContext(this.level(), this.connector.getBlockPos().getCenter());
        final List<SimMovementContext> contexts = this.magnetMap.findNearby(context);

        for (final SimMovementContext movementContext : contexts) {
            final T other = this.asConnector(this.level().getBlockEntity(movementContext.localBlockPos()));
            if (movementContext.subLevel() != subLevel && other != null && !other.hasOtherConnector() && other.magnetActive()) {
                this.magnetMap.tryAddPair(this.level(), this.connector.getBlockPos(), movementContext.localBlockPos(), DockingConnectorPair::new);
                MagnetPair.getRelativePosition(this.connector, other, tempRelativePos);
                this.closestPairDistance = Math.min(tempRelativePos.length(), this.closestPairDistance);
            }
        }

        final BlockPos sameGridConnection = this.connector.getBlockPos().offset(direction.getNormal().multiply(3));
        final T other = this.asConnector(this.level().getBlockEntity(sameGridConnection));

        if (this.connector.isExtended() && other != null) {
            if (other.getBlockState().getValue(BlockStateProperties.FACING).getOpposite() == direction && other.isExtended()) {
                this.magnetMap.tryAddPair(this.level(), this.connector.getBlockPos(), other.getBlockPos(), DockingConnectorPair::new);
            }
        }
    }

    public void updateState() {
        if (this.connector.isPowered()) {
            if (this.state != DockingConnectorState.LOCKED && this.connector.isExtended()) {
                this.state = this.connector.hasOtherConnector() ? DockingConnectorState.LOCKING : DockingConnectorState.EXTENDED;
            }
        } else {
            if (this.state != DockingConnectorState.UNPOWERED && this.connector.hasOtherConnector()) {
                final Map<MagnetPairIdentifier, MagnetPair<T>> map = this.magnetMap.pairMap.get(this.level());
                if (map != null && map.get(new MagnetPairIdentifier(this.otherConnectorPosition, this.connector.getBlockPos())) instanceof final DockingConnectorPair<?> pair) {
                    pair.unDock();
                }
            }
            this.state = DockingConnectorState.UNPOWERED;
        }
    }

    public void pairTo(final DockingConnector other) {
        if (other.getBlockPos().equals(this.otherConnectorPosition)) {
            return;
        }

        final T otherConnector = this.getOtherConnector();
        if (otherConnector != null && this.connector.getBlockPos().equals(otherConnector.getOtherConnectorPosition())) {
            otherConnector.unDock();
        }

        this.unDock();
        this.magnetMap.tryAddPair(this.level(), this.connector.getBlockPos(), other.getBlockPos(), DockingConnectorPair::new);
        final DockingConnectorPair<?> pair = this.getPairAt(other.getBlockPos());

        if (pair != null) {
            pair.dock(true);
            this.connector.notifyUpdate();
        }
    }

    public void setDock(final DockingConnector otherConnector, final boolean isLocked, @Nullable final Quaterniondc targetOrientation, @Nullable final Vector3dc relativePos, @Nullable final Quaterniondc relativeOrientation) {
        final BlockPos previous = this.otherConnectorPosition;

        final SubLevel otherSubLevel = otherConnector.getLatestSubLevel();
        this.otherConnectorPosition = otherConnector.getBlockPos();
        this.otherConnectorSubLevelId = otherSubLevel != null ? otherSubLevel.getUniqueId() : null;

        this.updateState();

        if (this.state == DockingConnectorState.LOCKING) {
            if (targetOrientation != null && this.constraintSmoother == null) {
                this.constraintSmoother = new ConstraintSmoother(otherConnector, targetOrientation, relativePos, relativeOrientation);
            }
            if (isLocked) {
                this.state = DockingConnectorState.LOCKED;
                if (this.constraintSmoother != null) {
                    final ServerSubLevelContainer container = SubLevelContainer.getContainer((ServerLevel) this.level());
                    this.constraintSmoother.step(this, 1);
                }
                this.constraintSmoother = null;
                this.connector.onDockingLinkLocked();
            }
        }

        if (previous != this.otherConnectorPosition) {
            if (targetOrientation == null) {
                this.removeConstraint();
            }
            this.connector.sendData();
        }
    }

    public void unDock() {
        this.closestPairDistance = Double.MAX_VALUE;
        this.state = this.connector.isExtended() ? DockingConnectorState.EXTENDED : DockingConnectorState.UNPOWERED;

        if (this.otherConnectorPosition != null) {
            this.connector.onDockingLinkBroken();
        }

        this.otherConnectorSubLevelId = null;
        this.otherConnectorPosition = null;

        this.removeConstraint();
        this.connector.sendData();

        this.level().updateNeighborsAt(this.connector.getBlockPos(), this.connector.getBlockState().getBlock());
    }

    public void attachConstraints(final DockingConnector other, final Quaterniondc targetOrientation, final Vector3dc relativePos, final Quaterniondc relativeOrientation, final boolean isLocked) {
        final ServerSubLevel thisSubLevel = (ServerSubLevel) this.connector.getLatestSubLevel();
        final ServerSubLevel otherSubLevel = (ServerSubLevel) other.getLatestSubLevel();
        assert thisSubLevel != null;

        final Vector3d anchorPos = JOMLConversion.toJOML(this.connector.getTipPosition());
        final Vector3d otherAnchorPos = JOMLConversion.toJOML(other.getTipPosition());

        final ServerSubLevelContainer container = SubLevelContainer.getContainer((ServerLevel) this.level());
        final SubLevelPhysicsSystem physicsSystem = container.physicsSystem();

        final double partialPhysicsTick = physicsSystem.getPartialPhysicsTick();
        final double physicsTime = this.connector.getFeetValue((float) partialPhysicsTick);

        double lerpFactor = Mth.clamp(physicsTime * physicsTime, 0.0, 1.0);

        if (isLocked) {
            lerpFactor = 1;
        }

        final double rotationLerpFactor = Mth.clamp(lerpFactor * 2.0, 0.0, 1.0);
        if (this.constraintHandle != null) {
            this.constraintHandle.remove();
        }

        otherAnchorPos.fma(1 - lerpFactor, relativePos);

        final FixedConstraintConfiguration constraint = new FixedConstraintConfiguration(
                anchorPos,
                otherAnchorPos,
                relativeOrientation.slerp(targetOrientation, rotationLerpFactor, new Quaterniond()));

        this.constraintHandle = container.physicsSystem().getPipeline().addConstraint(thisSubLevel, otherSubLevel, constraint);
    }

    public void removeConstraint() {
        if (this.constraintHandle != null) {
            this.constraintHandle.remove();
            this.constraintHandle = null;
        }
        this.constraintSmoother = null;
    }

    public void physicsTick() {
        if (this.constraintSmoother != null) {
            this.constraintSmoother.partialStep(this);
        }
    }

    @Nullable
    public Iterable<SubLevel> getConnectionDependencies() {
        if (this.otherConnectorSubLevelId == null) {
            return null;
        }

        final SubLevelContainer container = SubLevelContainer.getContainer(this.level());
        final SubLevel otherSubLevel = container.getSubLevel(this.otherConnectorSubLevelId);

        if (otherSubLevel == null) {
            return null;
        }

        return List.of(otherSubLevel);
    }

    public void writeDocking(final CompoundTag tag) {
        if (this.otherConnectorPosition != null) {
            tag.put("OtherConnector", NbtUtils.writeBlockPos(this.otherConnectorPosition));
        }

        if (this.otherConnectorSubLevelId != null) {
            tag.putUUID("OtherConnectorSubLevelId", this.otherConnectorSubLevelId);
        }
    }

    public void readDocking(final CompoundTag tag) {
        if (tag.contains("OtherConnector")) {
            this.otherConnectorPosition = NbtUtils.readBlockPos(tag, "OtherConnector").orElse(null);
        } else {
            this.otherConnectorPosition = null;
        }

        if (tag.contains("OtherConnectorSubLevelId")) {
            this.otherConnectorSubLevelId = tag.getUUID("OtherConnectorSubLevelId");
        }
    }

    public Vec3 getTipPosition() {
        return Vec3.atCenterOf(this.connector.getBlockPos()).add(Vec3.atLowerCornerOf(this.connector.getBlockState().getValue(BlockStateProperties.FACING).getNormal()).scale(1.5));
    }

    public Vec3 getMagnetPosition() {
        return Vec3.atCenterOf(this.connector.getBlockPos()).add(Vec3.atLowerCornerOf(this.connector.getBlockState().getValue(BlockStateProperties.FACING).getNormal()).scale(1.4));
    }

    public boolean magnetActive() {
        return this.connector.isExtended() && this.constraintHandle == null;
    }

    public Quaternionfc getOrientation() {
        return this.connector.getBlockState().getValue(BlockStateProperties.FACING).getRotation();
    }

    public Vector3d setMagneticMoment(final Vector3d v) {
        v.set(JOMLConversion.toJOML(Vec3.atLowerCornerOf(this.connector.getBlockState().getValue(BlockStateProperties.FACING).getNormal())));
        v.mul(Math.sqrt(SimConfigService.INSTANCE.server().physics.dockingConnectorStrength.get()));
        return v;
    }

    @Nullable
    public BlockPos getOtherConnectorPosition() {
        return this.otherConnectorPosition;
    }

    public void setOtherConnectorPosition(@Nullable final BlockPos otherConnectorPosition) {
        this.otherConnectorPosition = otherConnectorPosition;
    }

    @Nullable
    public UUID getOtherConnectorSubLevelId() {
        return this.otherConnectorSubLevelId;
    }

    public void setOtherConnectorSubLevelId(@Nullable final UUID otherConnectorSubLevelId) {
        this.otherConnectorSubLevelId = otherConnectorSubLevelId;
    }

    public DockingConnectorState getDockingState() {
        return this.state;
    }

    public void setDockingState(final DockingConnectorState state) {
        this.state = state;
    }

    public double getClosestPairDistance() {
        return this.closestPairDistance;
    }

    public boolean hasActiveConstraint() {
        return this.constraintHandle != null;
    }

    @Nullable
    private T asConnector(@Nullable final BlockEntity blockEntity) {
        return this.connectorClass.isInstance(blockEntity) ? this.connectorClass.cast(blockEntity) : null;
    }

    @Nullable
    private DockingConnectorPair<?> getPairAt(final BlockPos otherPos) {
        final MagnetPair<T> pair = this.magnetMap.getPair(this.level(), this.connector.getBlockPos(), otherPos);
        return pair instanceof DockingConnectorPair<?> ? (DockingConnectorPair<?>) pair : null;
    }

    private Level level() {
        final Level level = this.connector.getLevel();
        if (level == null) {
            throw new IllegalStateException("Docking connector at " + this.connector.getBlockPos() + " has no level");
        }
        return level;
    }

    private static class ConstraintSmoother {

        private final BlockPos otherConnectorPos;
        private final Quaterniond targetRelativeOrientation;
        private final Vector3d initialRelativePosition;
        private final Quaterniond initialRelativeOrientation;

        private ConstraintSmoother(final DockingConnector otherConnector, final Quaterniondc targetOrientation, final Vector3dc relativePos, final Quaterniondc relativeOrientation) {
            this(otherConnector.getBlockPos(), new Quaterniond(targetOrientation), new Vector3d(relativePos), new Quaterniond(relativeOrientation));
        }

        private ConstraintSmoother(final BlockPos otherConnectorPos, final Quaterniond targetRelativeOrientation, final Vector3d initialRelativePosition, final Quaterniond initialRelativeOrientation) {
            this.otherConnectorPos = otherConnectorPos;
            this.targetRelativeOrientation = targetRelativeOrientation;
            this.initialRelativePosition = initialRelativePosition;
            this.initialRelativeOrientation = initialRelativeOrientation;
        }

        private void partialStep(final DockingConnectorCore<?> core) {
            final ServerSubLevelContainer container = SubLevelContainer.getContainer((ServerLevel) core.level());
            final SubLevelPhysicsSystem physicsSystem = container.physicsSystem();

            final double partialPhysicsTick = physicsSystem.getPartialPhysicsTick();
            final double physicsTime = core.connector.getFeetValue((float) partialPhysicsTick);

            final double lerpFactor = Mth.clamp(physicsTime * physicsTime, 0.0, 1.0);

            this.step(core, lerpFactor);
        }

        private void step(final DockingConnectorCore<?> core, final double lerpFactor) {
            if (core.level().getBlockEntity(this.otherConnectorPos) instanceof final DockingConnector other) {
                final ServerSubLevel thisSubLevel = (ServerSubLevel) core.connector.getLatestSubLevel();
                final ServerSubLevel otherSubLevel = (ServerSubLevel) other.getLatestSubLevel();
                assert thisSubLevel != null;

                final Vector3d anchorPos = JOMLConversion.toJOML(core.connector.getTipPosition());
                final Vector3d otherAnchorPos = JOMLConversion.toJOML(other.getTipPosition());

                final double rotationLerpFactor = Mth.clamp(lerpFactor * 2.0, 0.0, 1.0);
                if (core.constraintHandle != null) {
                    core.constraintHandle.remove();
                }

                otherAnchorPos.fma(1 - lerpFactor, this.initialRelativePosition);

                final FixedConstraintConfiguration constraint = new FixedConstraintConfiguration(
                        anchorPos,
                        otherAnchorPos,
                        this.initialRelativeOrientation.slerp(this.targetRelativeOrientation, rotationLerpFactor, new Quaterniond()));

                final ServerSubLevelContainer container = SubLevelContainer.getContainer((ServerLevel) core.level());
                core.constraintHandle = container.physicsSystem().getPipeline().addConstraint(thisSubLevel, otherSubLevel, constraint);
            }
        }
    }
}
