package com.cake.more_docking_connectors.index;

import com.cake.more_docking_connectors.MoreDockingConnectors;
import dev.engine_room.flywheel.lib.model.baked.PartialModel;
import net.minecraft.resources.ResourceLocation;

public class DockingConnectorPartialModels {

    public static final PartialModel
            DOCKING_PIPE_CONNECTOR_MAIN_PISTON_BOTTOM = block("docking_pipe_connector/main_piston_1"),
            DOCKING_PIPE_CONNECTOR_MAIN_PISTON_TOP = block("docking_pipe_connector/main_piston_2"),
            DOCKING_PIPE_CONNECTOR_SIDE_PISTON_BOTTOM = block("docking_pipe_connector/side_piston_1"),
            DOCKING_PIPE_CONNECTOR_SIDE_PISTON_TOP = block("docking_pipe_connector/side_piston_2"),
            DOCKING_PIPE_CONNECTOR_FOOT = block("docking_pipe_connector/foot"),
            DOCKING_KINETIC_CONNECTOR_MAIN_PISTON_BOTTOM = block("docking_kinetic_connector/main_piston_1"),
            DOCKING_KINETIC_CONNECTOR_MAIN_PISTON_TOP = block("docking_kinetic_connector/main_piston_2"),
            DOCKING_KINETIC_CONNECTOR_SIDE_PISTON_BOTTOM = block("docking_kinetic_connector/side_piston_1"),
            DOCKING_KINETIC_CONNECTOR_SIDE_PISTON_TOP = block("docking_kinetic_connector/side_piston_2"),
            DOCKING_KINETIC_CONNECTOR_FOOT = block("docking_kinetic_connector/foot");

    private static PartialModel block(final String path) {
        return PartialModel.of(ResourceLocation.fromNamespaceAndPath(MoreDockingConnectors.MOD_ID, "block/" + path));
    }

    public static void register() {
    }
}
