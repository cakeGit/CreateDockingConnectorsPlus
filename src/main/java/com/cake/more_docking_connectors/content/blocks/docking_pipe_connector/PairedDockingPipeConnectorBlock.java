package com.cake.more_docking_connectors.content.blocks.docking_pipe_connector;

import com.cake.more_docking_connectors.content.blocks.docking_connector.shared.PairedDockingConnectorBlock;
import com.cake.more_docking_connectors.index.DockingConnectorBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;

import java.util.function.Supplier;

public class PairedDockingPipeConnectorBlock extends PairedDockingConnectorBlock {

    public static final MapCodec<PairedDockingPipeConnectorBlock> CODEC = simpleCodec(PairedDockingPipeConnectorBlock::new);

    public PairedDockingPipeConnectorBlock(final Properties properties) {
        super(properties);
    }

    @Override
    protected Supplier<? extends Block> getMainConnectorBlock() {
        return () -> DockingConnectorBlocks.DOCKING_PIPE_CONNECTOR.get();
    }

    @Override
    protected MapCodec<? extends DirectionalBlock> codec() {
        return CODEC;
    }
}
