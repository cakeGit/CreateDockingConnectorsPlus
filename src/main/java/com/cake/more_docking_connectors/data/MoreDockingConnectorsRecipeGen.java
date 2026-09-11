package com.cake.more_docking_connectors.data;

import com.cake.more_docking_connectors.MoreDockingConnectors;
import com.cake.more_docking_connectors.index.DockingConnectorBlocks;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.AllItems;
import com.simibubi.create.api.data.recipe.MechanicalCraftingRecipeGen;
import com.simibubi.create.foundation.data.recipe.CommonMetal;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Blocks;

import java.util.concurrent.CompletableFuture;

public class MoreDockingConnectorsRecipeGen extends MechanicalCraftingRecipeGen {

    private final GeneratedRecipe DOCKING_PIPE_CONNECTOR = this.create(DockingConnectorBlocks.DOCKING_PIPE_CONNECTOR::get)
            .returns(2)
            .recipe(b -> b
                    .patternLine("ICI")
                    .patternLine(" C ")
                    .patternLine("PAP")
                    .patternLine("BEB")
                    .key('B', CommonMetal.COPPER.plates)
                    .key('E', AllItems.ELECTRON_TUBE)
                    .key('P', Blocks.PISTON)
                    .key('A', AllBlocks.COPPER_CASING)
                    .key('C', AllBlocks.FLUID_PIPE)
                    .key('I', CommonMetal.IRON.plates)
            );

    private final GeneratedRecipe DOCKING_KINETIC_CONNECTOR = this.create(DockingConnectorBlocks.DOCKING_KINETIC_CONNECTOR::get)
            .returns(1)
            .recipe(b -> b
                    .patternLine("ICI")
                    .patternLine(" S ")
                    .patternLine("PAP")
                    .patternLine("LTL")
                    .key('I', CommonMetal.IRON.plates)
                    .key('C', AllBlocks.COGWHEEL)
                    .key('S', AllBlocks.SHAFT)
                    .key('P', Blocks.PISTON)
                    .key('A', AllBlocks.ANDESITE_CASING)
                    .key('L', AllItems.ANDESITE_ALLOY)
                    .key('T', AllItems.ELECTRON_TUBE)
            );

    public MoreDockingConnectorsRecipeGen(final PackOutput output, final CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, MoreDockingConnectors.MOD_ID);
    }
}
