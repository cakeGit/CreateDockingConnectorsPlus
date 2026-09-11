package com.cake.more_docking_connectors.events;

import com.cake.more_docking_connectors.content.blocks.docking_kinetic_connector.DockingKineticConnectorBlockEntity;
import com.cake.more_docking_connectors.content.blocks.docking_pipe_connector.DockingPipeConnectorBlockEntity;
import dev.ryanhcode.sable.sublevel.system.SubLevelPhysicsSystem;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@EventBusSubscriber
public class DockingConnectorCommonEvents {

    @SubscribeEvent
    public static void postServerTick(final ServerTickEvent.Post event) {
        final MinecraftServer server = event.getServer();
        for (final ServerLevel level : server.getAllLevels()) {
            DockingPipeConnectorBlockEntity.MAGNET_CONTROLLER.tick(level);
            DockingKineticConnectorBlockEntity.MAGNET_CONTROLLER.tick(level);
        }
    }

    public static void onPhysicsTick(final SubLevelPhysicsSystem physicsSystem, final double timeStep) {
        final ServerLevel level = physicsSystem.getLevel();
        DockingPipeConnectorBlockEntity.MAGNET_CONTROLLER.physicsTick(timeStep, level);
        DockingKineticConnectorBlockEntity.MAGNET_CONTROLLER.physicsTick(timeStep, level);
    }

}
