package com.cake.docking_connectors_plus.index;

import com.cake.docking_connectors_plus.DockingConnectorsPlus;
import com.cake.docking_connectors_plus.content.display_sources.DockingConnectorDisplaySource;
import com.simibubi.create.api.behaviour.display.DisplaySource;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.util.entry.RegistryEntry;

public class DockingConnectorDisplaySources {
    private static final CreateRegistrate REGISTRATE = DockingConnectorsPlus.getRegistrate();

    public static final RegistryEntry<DisplaySource, DockingConnectorDisplaySource> DOCKING_CONNECTOR_DISPLAY =
            REGISTRATE.displaySource("docking_connector_display", DockingConnectorDisplaySource::new).register();

    public static void register() {
    }
}
