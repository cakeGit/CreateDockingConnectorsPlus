package com.cake.more_docking_connectors.content.blocks.docking_connector.shared;

import dev.engine_room.flywheel.lib.model.baked.PartialModel;

public record DockingConnectorModels(PartialModel mainPistonBottom, PartialModel mainPistonTop, PartialModel sidePistonBottom,
                                     PartialModel sidePistonTop, PartialModel foot) {
}
