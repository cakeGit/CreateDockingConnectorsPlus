package com.cake.docking_connectors_plus.index;

import com.cake.docking_connectors_plus.DockingConnectorsPlus;
import com.cake.docking_connectors_plus.content.blocks.docking_kinetic_connector.DockingKineticConnectorBlockEntity;
import com.cake.docking_connectors_plus.content.blocks.docking_kinetic_connector.DockingKineticConnectorRenderer;
import com.cake.docking_connectors_plus.content.blocks.docking_pipe_connector.DockingPipeConnectorBlockEntity;
import com.cake.docking_connectors_plus.content.blocks.docking_pipe_connector.DockingPipeConnectorRenderer;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.BlockEntityEntry;

public class DockingConnectorBlockEntities {
    private static final CreateRegistrate REGISTRATE = DockingConnectorsPlus.getRegistrate();

    public static final BlockEntityEntry<DockingPipeConnectorBlockEntity> DOCKING_PIPE_CONNECTOR = REGISTRATE
            .blockEntity("docking_pipe_connector", DockingPipeConnectorBlockEntity::new)
            .validBlocks(DockingConnectorBlocks.DOCKING_PIPE_CONNECTOR)
            .renderer(() -> DockingPipeConnectorRenderer::new)
            .register();

    public static final BlockEntityEntry<DockingKineticConnectorBlockEntity> DOCKING_KINETIC_CONNECTOR = REGISTRATE
            .blockEntity("docking_kinetic_connector", DockingKineticConnectorBlockEntity::new)
            .validBlocks(DockingConnectorBlocks.DOCKING_KINETIC_CONNECTOR)
            .renderer(() -> DockingKineticConnectorRenderer::new)
            .register();

    public static void register() {
    }
}
