package com.cake.docking_connectors_plus.content.blocks.docking_kinetic_connector;

import com.cake.docking_connectors_plus.DockingConnectorsPlus;
import com.cake.docking_connectors_plus.content.blocks.docking_connector.shared.DockingConnector;
import com.cake.docking_connectors_plus.content.blocks.docking_connector.shared.DockingConnectorCore;
import com.cake.docking_connectors_plus.content.blocks.docking_connector.shared.DockingConnectorExtension;
import com.cake.docking_connectors_plus.content.blocks.docking_connector.shared.DockingConnectorState;
import com.cake.docking_connectors_plus.index.DockingConnectorBlocks;
import com.simibubi.create.content.kinetics.base.IRotate;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.item.TooltipHelper;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.api.block.BlockEntitySubLevelActor;
import dev.ryanhcode.sable.api.physics.handle.RigidBodyHandle;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.SubLevel;
import dev.simulated_team.simulated.content.blocks.redstone_magnet.MagnetMap;
import net.createmod.catnip.lang.FontHelper;
import net.createmod.catnip.lang.Lang;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.RandomSource;
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
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

import static net.minecraft.ChatFormatting.GOLD;

public class DockingKineticConnectorBlockEntity extends KineticBlockEntity implements DockingConnector, BlockEntitySubLevelActor {

    public static final MagnetMap<DockingKineticConnectorBlockEntity> MAGNET_CONTROLLER = new MagnetMap<>();

    private DockingConnectorCore<DockingKineticConnectorBlockEntity> dockingCore;
    private DockingConnectorExtension<DockingKineticConnectorBlockEntity> extension;
    private boolean linkActive;
    private boolean speedMismatch;

    public DockingKineticConnectorBlockEntity(final BlockEntityType<?> type, final BlockPos pos, final BlockState state) {
        super(type, pos, state);
    }

    @Nullable
    public DockingKineticConnectorBlockEntity getOtherConnector() {
        return this.dockingCore.getOtherConnector();
    }

    @Override
    public void addBehaviours(final List<BlockEntityBehaviour> behaviours) {
        this.dockingCore = new DockingConnectorCore<>(this, MAGNET_CONTROLLER, DockingKineticConnectorBlockEntity.class);
        behaviours.add(this.dockingCore.createBehaviour());
        this.extension = new DockingConnectorExtension<>(this);
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
        this.tickLink();

        if (this.level.isClientSide() && this.speedMismatch) {
            this.playGrindingEffect();
        }
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        this.extension.lazyTick();
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
        if (this.level.isClientSide()) {
            return;
        }

        final DockingKineticConnectorBlockEntity other = this.dockingCore.getOtherConnector();
        if (other == null) {
            return;
        }

        if (this.hasOpposingSpeed(other)) {
            this.onSpeedMismatch();
            return;
        }

        this.activateLink(other);
    }

    @Override
    public void onDockingLinkBroken() {
        if (this.level.isClientSide()) {
            return;
        }

        final DockingKineticConnectorBlockEntity other = this.dockingCore.getOtherConnector();

        this.linkActive = false;
        this.speedMismatch = false;
        if (other != null) {
            other.linkActive = false;
            other.speedMismatch = false;
        }

        this.detachKinetics();
        this.updateSpeed = true;
        if (other != null) {
            other.detachKinetics();
            other.updateSpeed = true;
        }
    }

    public void onSpeedMismatch() {
        if (this.level.isClientSide()) {
            return;
        }

        this.linkActive = false;

        final DockingKineticConnectorBlockEntity other = this.dockingCore.getOtherConnector();
        if (other != null) {
            other.linkActive = false;
        }

        if (this.speedMismatch) {
            return;
        }

        this.speedMismatch = true;
        this.sendData();
        if (other != null) {
            other.speedMismatch = true;
            other.sendData();
        }
    }

    @Override
    public List<BlockPos> addPropagationLocations(final IRotate block, final BlockState state, final List<BlockPos> neighbours) {
        final BlockPos otherPos = this.dockingCore.getOtherConnectorPosition();
        if (otherPos != null && this.level != null && !this.level.isClientSide() && this.level.isLoaded(otherPos)) {
            final DockingKineticConnectorBlockEntity other = this.dockingCore.getOtherConnector();
            if (other != null && Objects.equals(other.getOtherConnectorPosition(), this.getBlockPos())) {
                neighbours.add(otherPos);
            }
        }

        return super.addPropagationLocations(block, state, neighbours);
    }

    @Override
    public boolean isCustomConnection(final KineticBlockEntity other, final BlockState state, final BlockState otherState) {
        return other == this.getLinkPartner();
    }

    @Override
    public float propagateRotationTo(final KineticBlockEntity target, final BlockState stateFrom, final BlockState stateTo, final BlockPos diff, final boolean connectedViaAxes, final boolean connectedViaCogs) {
        if (target == this.getLinkPartner()) {
            return 1;
        }

        return super.propagateRotationTo(target, stateFrom, stateTo, diff, connectedViaAxes, connectedViaCogs);
    }

    @Override
    public float calculateStressApplied() {
        return 0;
    }

    @Override
    protected void write(final CompoundTag tag, final HolderLookup.Provider registries, final boolean clientPacket) {
        this.extension.write(tag);
        this.dockingCore.writeDocking(tag);
        tag.putBoolean("SpeedMismatch", this.speedMismatch);

        super.write(tag, registries, clientPacket);
    }

    @Override
    protected void read(final CompoundTag tag, final HolderLookup.Provider registries, final boolean clientPacket) {
        this.extension.read(tag);
        this.dockingCore.readDocking(tag);
        this.speedMismatch = tag.getBoolean("SpeedMismatch");

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

    @Override
    public void updateSignal() {
        this.extension.updateSignal();
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
        return () -> DockingConnectorBlocks.PAIRED_DOCKING_KINETIC_CONNECTOR.get();
    }

    @Override
    public void sable$physicsTick(final ServerSubLevel subLevel, final RigidBodyHandle handle, final double timeStep) {
        this.dockingCore.physicsTick();
    }

    @Override
    public @Nullable Iterable<@NotNull SubLevel> sable$getConnectionDependencies() {
        return this.dockingCore.getConnectionDependencies();
    }

    private void tickLink() {
        if (this.level.isClientSide()) {
            return;
        }

        final DockingKineticConnectorBlockEntity other = this.dockingCore.getOtherConnector();
        if (this.linkActive || other == null || other.linkActive || !this.isMutuallyPaired(other)) {
            return;
        }

        if (this.hasOpposingSpeed(other)) {
            this.onSpeedMismatch();
        } else {
            this.activateLink(other);
        }
    }

    private void activateLink(final DockingKineticConnectorBlockEntity other) {
        this.linkActive = true;
        other.linkActive = true;
        this.detachKinetics();
        other.detachKinetics();
        this.updateSpeed = true;
        other.updateSpeed = true;

        if (this.speedMismatch || other.speedMismatch) {
            this.speedMismatch = false;
            other.speedMismatch = false;
            this.sendData();
            other.sendData();
        }
    }

    private void playGrindingEffect() {
        final Direction facing = this.getBlockState().getValue(BlockStateProperties.FACING);
        final RandomSource random = this.level.random;

        final int stepX = facing.getStepX();
        final int stepY = facing.getStepY();
        final int stepZ = facing.getStepZ();

        for (int i = 0; i < 2; i++) {
            final Vec3 particlePos = this.getTipPosition()
                    .add((random.nextFloat() - 0.5f) * (stepX == 0 ? 1 : 0), (random.nextFloat() - 0.5f) * (stepY == 0 ? 1 : 0), (random.nextFloat() - 0.5f) * (stepZ == 0 ? 1 : 0));

            this.level.addParticle(ParticleTypes.CRIT, particlePos.x, particlePos.y, particlePos.z, 0.0, 0.0, 0.0);
        }
    }

    @Override
    public boolean addToGoggleTooltip(final List<Component> tooltip, final boolean isPlayerSneaking) {
        final boolean added = super.addToGoggleTooltip(tooltip, isPlayerSneaking);

        if (!this.speedMismatch) {
            return added;
        }

        if (!tooltip.isEmpty()) {
            tooltip.add(CommonComponents.EMPTY);
        }

        Lang.builder(DockingConnectorsPlus.MOD_ID)
                .translate("kinetic_connector.speed_mismatch")
                .style(GOLD)
                .forGoggles(tooltip);

        final MutableComponent component = Lang.builder(DockingConnectorsPlus.MOD_ID)
                .translate("kinetic_connector.speed_mismatch_error")
                .component();
        tooltip.addAll(TooltipHelper.cutTextComponent(component, FontHelper.Palette.GRAY_AND_WHITE));

        return true;
    }

    private boolean isMutuallyPaired(final DockingKineticConnectorBlockEntity other) {
        return this.isLocked()
                && other.isLocked()
                && Objects.equals(other.getOtherConnectorPosition(), this.getBlockPos());
    }

    private boolean hasOpposingSpeed(final DockingKineticConnectorBlockEntity other) {
        final float speed = this.getTheoreticalSpeed();
        final float otherSpeed = other.getTheoreticalSpeed();
        return speed != 0 && otherSpeed != 0 && Math.signum(speed) != Math.signum(otherSpeed);
    }

    @Nullable
    private DockingKineticConnectorBlockEntity getLinkPartner() {
        if (!this.linkActive || this.level == null || this.level.isClientSide()) {
            return null;
        }

        final BlockPos otherPos = this.dockingCore.getOtherConnectorPosition();
        if (otherPos == null || !this.level.isLoaded(otherPos)) {
            return null;
        }

        final DockingKineticConnectorBlockEntity other = this.dockingCore.getOtherConnector();
        if (other == null || !this.isMutuallyPaired(other) || !other.linkActive) {
            return null;
        }

        return other;
    }
}
