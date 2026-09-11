package com.cake.docking_connectors_plus;

import com.cake.docking_connectors_plus.data.DockingConnectorsPlusRecipeGen;
import com.cake.docking_connectors_plus.events.DockingConnectorCommonEvents;
import com.cake.docking_connectors_plus.index.DockingConnectorBlockEntities;
import com.cake.docking_connectors_plus.index.DockingConnectorBlocks;
import com.cake.docking_connectors_plus.index.DockingConnectorDisplaySources;
import com.cake.docking_connectors_plus.index.DockingConnectorPartialModels;
import com.mojang.logging.LogUtils;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.KineticStats;
import com.simibubi.create.foundation.item.TooltipModifier;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import dev.ryanhcode.sable.platform.SableEventPlatform;
import dev.simulated_team.simulated.events.SimulatedCommonEvents;
import dev.simulated_team.simulated.index.SimBlocks;
import dev.simulated_team.simulated.registrate.SimulatedRegistrate;
import net.createmod.catnip.lang.FontHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import org.slf4j.Logger;

import java.util.List;
import java.util.function.Supplier;

@Mod(DockingConnectorsPlus.MOD_ID)
public class DockingConnectorsPlus {
    public static final String MOD_ID = "docking_connectors_plus";
    public static final String MOD_NAME = "Create: Docking Connectors+";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final NonNullSupplier<CreateRegistrate> REGISTRATE = NonNullSupplier.lazy(() ->
            CreateRegistrate.create(MOD_ID)
                    .defaultCreativeTab((ResourceKey<CreativeModeTab>) null)
                    .setTooltipModifierFactory(item ->
                            new ItemDescription.Modifier(item, FontHelper.Palette.STANDARD_CREATE)
                                    .andThen(TooltipModifier.mapNull(KineticStats.create(item)))));

    public DockingConnectorsPlus(IEventBus modEventBus, ModContainer modContainer) {
        getRegistrate().registerEventListeners(modEventBus);
        getRegistrate().addRawLang(MOD_ID + ".display_source.sublevel_name", "Contraption Name");
        getRegistrate().addRawLang(MOD_ID + ".kinetic_connector.speed_mismatch", "Kinetic Networks Mismatch");
        getRegistrate().addRawLang(MOD_ID + ".kinetic_connector.speed_mismatch_error", "It appears that this _Docking Kinetic Connector_ is linked to a kinetic network rotating in the _opposite direction_. Adjust one side's rotation until both networks _match_.");

        SableEventPlatform.INSTANCE.onPhysicsTick(DockingConnectorCommonEvents::onPhysicsTick);

        DockingConnectorBlocks.register();
        DockingConnectorBlockEntities.register();
        DockingConnectorDisplaySources.register();
        DockingConnectorPartialModels.register();

        modEventBus.addListener(FMLCommonSetupEvent.class, event -> event.enqueueWork(DockingConnectorsPlus::addToSimulatedTab));
        modEventBus.addListener(GatherDataEvent.class, DockingConnectorsPlus::gatherData);
    }

    private static void addToSimulatedTab() {
        final Item pipeConnector = DockingConnectorBlocks.DOCKING_PIPE_CONNECTOR.get().asItem();
        final Item kineticConnector = DockingConnectorBlocks.DOCKING_KINETIC_CONNECTOR.get().asItem();
        final Item reference = SimBlocks.DOCKING_CONNECTOR.get().asItem();

        final List<Supplier<Item>> tabItems = SimulatedRegistrate.TAB_ITEMS;
        int referenceIndex = -1;
        for (int i = 0; i < tabItems.size(); i++) {
            if (tabItems.get(i).get() == reference) {
                referenceIndex = i;
                break;
            }
        }

        if (referenceIndex == -1) {
            throw new IllegalStateException("Simulated's creative tab does not contain the docking connector, cannot place the docking connectors after it");
        }

        tabItems.add(referenceIndex + 1, () -> pipeConnector);
        tabItems.add(referenceIndex + 2, () -> kineticConnector);

        final ResourceLocation section = SimulatedRegistrate.sectionOf(reference);
        if (section == null) {
            throw new IllegalStateException("Simulated's creative tab has no section for the docking connector");
        }
        SimulatedRegistrate.ITEM_TO_SECTION.put(BuiltInRegistries.ITEM.getKey(pipeConnector), section);
        SimulatedRegistrate.ITEM_TO_SECTION.put(BuiltInRegistries.ITEM.getKey(kineticConnector), section);
    }

    private static void gatherData(final GatherDataEvent event) {
        event.getGenerator().addProvider(event.includeServer(),
                new DockingConnectorsPlusRecipeGen(event.getGenerator().getPackOutput(), event.getLookupProvider()));
    }

    public static CreateRegistrate getRegistrate() {
        return REGISTRATE.get();
    }
}
