package com.cake.docking_connectors_plus.content.blocks.docking_kinetic_connector;

import com.cake.docking_connectors_plus.content.blocks.docking_connector.shared.DockingConnectorModels;
import com.cake.docking_connectors_plus.content.blocks.docking_connector.shared.DockingConnectorRenderer;
import com.cake.docking_connectors_plus.index.DockingConnectorPartialModels;
import com.mojang.blaze3d.vertex.PoseStack;
import com.simibubi.create.AllPartialModels;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import net.createmod.catnip.render.CachedBuffers;
import net.createmod.catnip.render.SuperByteBuffer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class DockingKineticConnectorRenderer extends DockingConnectorRenderer<DockingKineticConnectorBlockEntity> {

    private static final DockingConnectorModels MODELS = new DockingConnectorModels(
            DockingConnectorPartialModels.DOCKING_KINETIC_CONNECTOR_MAIN_PISTON_BOTTOM,
            DockingConnectorPartialModels.DOCKING_KINETIC_CONNECTOR_MAIN_PISTON_TOP,
            DockingConnectorPartialModels.DOCKING_KINETIC_CONNECTOR_SIDE_PISTON_BOTTOM,
            DockingConnectorPartialModels.DOCKING_KINETIC_CONNECTOR_SIDE_PISTON_TOP,
            DockingConnectorPartialModels.DOCKING_KINETIC_CONNECTOR_FOOT);

    public DockingKineticConnectorRenderer(final BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected DockingConnectorModels getModels() {
        return MODELS;
    }

    @Override
    protected void renderSafe(final DockingKineticConnectorBlockEntity be, final float partialTicks, final PoseStack ms, final MultiBufferSource bufferSource, final int light, final int overlay) {
        super.renderSafe(be, partialTicks, ms, bufferSource, light, overlay);

        final Direction facing = be.getBlockState().getValue(BlockStateProperties.FACING);
        final SuperByteBuffer shaftHalf = CachedBuffers.partialFacing(AllPartialModels.SHAFT_HALF, be.getBlockState(), facing.getOpposite());

        KineticBlockEntityRenderer.standardKineticRotationTransform(shaftHalf, be, light)
                .renderInto(ms, bufferSource.getBuffer(RenderType.solid()));
    }
}
