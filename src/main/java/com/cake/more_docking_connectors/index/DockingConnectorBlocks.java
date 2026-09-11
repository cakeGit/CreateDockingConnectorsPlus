package com.cake.more_docking_connectors.index;

import com.cake.more_docking_connectors.MoreDockingConnectors;
import com.cake.more_docking_connectors.content.blocks.docking_kinetic_connector.DockingKineticConnectorBlock;
import com.cake.more_docking_connectors.content.blocks.docking_kinetic_connector.PairedDockingKineticConnectorBlock;
import com.cake.more_docking_connectors.content.blocks.docking_pipe_connector.DockingPipeConnectorBlock;
import com.cake.more_docking_connectors.content.blocks.docking_pipe_connector.PairedDockingPipeConnectorBlock;
import com.simibubi.create.AllTags;
import com.simibubi.create.api.behaviour.display.DisplaySource;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.SharedProperties;
import com.tterrag.registrate.util.entry.BlockEntry;
import dev.simulated_team.simulated.data.SimBlockStateGen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;

import static com.simibubi.create.foundation.data.ModelGen.customItemModel;
import static com.simibubi.create.foundation.data.TagGen.pickaxeOnly;

public class DockingConnectorBlocks {
    private static final CreateRegistrate REGISTRATE = MoreDockingConnectors.getRegistrate();

    public static final BlockEntry<DockingPipeConnectorBlock> DOCKING_PIPE_CONNECTOR =
            REGISTRATE.block("docking_pipe_connector", DockingPipeConnectorBlock::new)
                    .lang("Docking Pipe Connector")
                    .tag(AllTags.AllBlockTags.NON_MOVABLE.tag)
                    .initialProperties(SharedProperties::stone)
                    .transform(pickaxeOnly())
                    .properties(p -> p
                            .sound(SoundType.NETHERITE_BLOCK)
                            .isRedstoneConductor(DockingConnectorBlocks::never)
                            .forceSolidOn()
                    )
                    .addLayer(() -> RenderType::cutoutMipped)
                    .transform(DisplaySource.displaySource(DockingConnectorDisplaySources.DOCKING_CONNECTOR_DISPLAY))
                    .properties(BlockBehaviour.Properties::noOcclusion)
                    .properties(BlockBehaviour.Properties::dynamicShape)
                    .blockstate(SimBlockStateGen::facingPoweredAxisBlockstate)
                    .item()
                    .transform(customItemModel())
                    .register();

    public static final BlockEntry<PairedDockingPipeConnectorBlock> PAIRED_DOCKING_PIPE_CONNECTOR =
            REGISTRATE.block("paired_docking_pipe_connector", PairedDockingPipeConnectorBlock::new)
                    .tag(AllTags.AllBlockTags.NON_MOVABLE.tag)
                    .initialProperties(SharedProperties::stone)
                    .blockstate((c, p) -> p.simpleBlock(c.get(), p.models().getExistingFile(p.modLoc("block/docking_pipe_connector/block"))))
                    .transform(pickaxeOnly())
                    .properties(p -> p.sound(SoundType.NETHERITE_BLOCK))
                    .properties(properties -> properties
                            .noOcclusion()
                            .noLootTable()
                            .pushReaction(PushReaction.BLOCK)
                            .forceSolidOff())
                    .register();

    public static final BlockEntry<DockingKineticConnectorBlock> DOCKING_KINETIC_CONNECTOR =
            REGISTRATE.block("docking_kinetic_connector", DockingKineticConnectorBlock::new)
                    .lang("Docking Kinetic Connector")
                    .tag(AllTags.AllBlockTags.NON_MOVABLE.tag)
                    .initialProperties(SharedProperties::stone)
                    .transform(pickaxeOnly())
                    .properties(p -> p
                            .sound(SoundType.NETHERITE_BLOCK)
                            .isRedstoneConductor(DockingConnectorBlocks::never)
                            .forceSolidOn()
                    )
                    .addLayer(() -> RenderType::cutoutMipped)
                    .transform(DisplaySource.displaySource(DockingConnectorDisplaySources.DOCKING_CONNECTOR_DISPLAY))
                    .properties(BlockBehaviour.Properties::noOcclusion)
                    .properties(BlockBehaviour.Properties::dynamicShape)
                    .blockstate(SimBlockStateGen::facingPoweredAxisBlockstate)
                    .item()
                    .transform(customItemModel())
                    .register();

    public static final BlockEntry<PairedDockingKineticConnectorBlock> PAIRED_DOCKING_KINETIC_CONNECTOR =
            REGISTRATE.block("paired_docking_kinetic_connector", PairedDockingKineticConnectorBlock::new)
                    .tag(AllTags.AllBlockTags.NON_MOVABLE.tag)
                    .initialProperties(SharedProperties::stone)
                    .blockstate((c, p) -> p.simpleBlock(c.get(), p.models().getExistingFile(p.modLoc("block/docking_kinetic_connector/block"))))
                    .transform(pickaxeOnly())
                    .properties(p -> p.sound(SoundType.NETHERITE_BLOCK))
                    .properties(properties -> properties
                            .noOcclusion()
                            .noLootTable()
                            .pushReaction(PushReaction.BLOCK)
                            .forceSolidOff())
                    .register();

    private static Boolean never(final BlockState state, final BlockGetter blockGetter, final BlockPos pos) {
        return false;
    }

    public static void register() {
    }
}
