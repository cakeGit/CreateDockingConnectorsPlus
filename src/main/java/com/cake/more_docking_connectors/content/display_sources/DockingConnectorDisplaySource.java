package com.cake.more_docking_connectors.content.display_sources;

import com.cake.more_docking_connectors.content.blocks.docking_connector.shared.DockingConnector;
import com.simibubi.create.content.redstone.displayLink.DisplayLinkContext;
import com.simibubi.create.content.redstone.displayLink.source.SingleLineDisplaySource;
import com.simibubi.create.content.redstone.displayLink.target.DisplayTargetStats;
import com.simibubi.create.foundation.gui.ModularGuiLineBuilder;
import dev.ryanhcode.sable.Sable;
import dev.ryanhcode.sable.sublevel.SubLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public class DockingConnectorDisplaySource extends SingleLineDisplaySource {

    @Override
    protected MutableComponent provideLine(final DisplayLinkContext context, final DisplayTargetStats stats) {
        if (!(context.getSourceBlockEntity() instanceof final DockingConnector connector)) {
            return EMPTY_LINE.copy();
        }

        final BlockPos otherPos = connector.getOtherConnectorPosition();
        final Level level = connector.getLevel();
        if (otherPos == null || level == null) {
            return EMPTY_LINE.copy();
        }

        final BlockEntity other = level.getBlockEntity(otherPos);
        if (!(other instanceof DockingConnector)) {
            return EMPTY_LINE.copy();
        }

        final SubLevel otherSubLevel = Sable.HELPER.getContaining(other);
        if (otherSubLevel != null) {
            final String name = otherSubLevel.getName();
            return name != null ? Component.literal(name) : EMPTY_LINE.copy();
        }

        return EMPTY_LINE.copy();
    }

    @Override
    public void initConfigurationWidgets(final DisplayLinkContext context, final ModularGuiLineBuilder builder, final boolean isFirstLine) {
        super.initConfigurationWidgets(context, builder, isFirstLine);
    }

    @Override
    protected String getTranslationKey() {
        return "sublevel_name";
    }

    @Override
    protected boolean allowsLabeling(final DisplayLinkContext context) {
        return true;
    }
}
