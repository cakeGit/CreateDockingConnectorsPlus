package com.cake.more_docking_connectors.content.blocks.docking_pipe_connector;

import com.cake.more_docking_connectors.content.blocks.docking_connector.shared.DockingConnectorModels;
import com.cake.more_docking_connectors.content.blocks.docking_connector.shared.DockingConnectorRenderer;
import com.cake.more_docking_connectors.index.DockingConnectorPartialModels;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

public class DockingPipeConnectorRenderer extends DockingConnectorRenderer<DockingPipeConnectorBlockEntity> {

    private static final DockingConnectorModels MODELS = new DockingConnectorModels(
            DockingConnectorPartialModels.DOCKING_PIPE_CONNECTOR_MAIN_PISTON_BOTTOM,
            DockingConnectorPartialModels.DOCKING_PIPE_CONNECTOR_MAIN_PISTON_TOP,
            DockingConnectorPartialModels.DOCKING_PIPE_CONNECTOR_SIDE_PISTON_BOTTOM,
            DockingConnectorPartialModels.DOCKING_PIPE_CONNECTOR_SIDE_PISTON_TOP,
            DockingConnectorPartialModels.DOCKING_PIPE_CONNECTOR_FOOT);

    public DockingPipeConnectorRenderer(final BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected DockingConnectorModels getModels() {
        return MODELS;
    }
}
