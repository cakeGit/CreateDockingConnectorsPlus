package com.cake.more_docking_connectors.content.blocks.docking_kinetic_connector;

import com.cake.more_docking_connectors.content.blocks.docking_connector.shared.PairedDockingConnectorBlock;
import com.cake.more_docking_connectors.index.DockingConnectorBlocks;
import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;

import java.util.function.Supplier;

public class PairedDockingKineticConnectorBlock extends PairedDockingConnectorBlock {

    public static final MapCodec<PairedDockingKineticConnectorBlock> CODEC = simpleCodec(PairedDockingKineticConnectorBlock::new);

    public PairedDockingKineticConnectorBlock(final Properties properties) {
        super(properties);
    }

    @Override
    protected Supplier<? extends Block> getMainConnectorBlock() {
        return () -> DockingConnectorBlocks.DOCKING_KINETIC_CONNECTOR.get();
    }

    @Override
    protected MapCodec<? extends DirectionalBlock> codec() {
        return CODEC;
    }
}
