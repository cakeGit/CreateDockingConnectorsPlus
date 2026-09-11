package com.cake.docking_connectors_plus.mixin;

import com.cake.docking_connectors_plus.DockingConnectorsPlus;
import dev.simulated_team.simulated.client.sections.SimulatedSection;
import dev.simulated_team.simulated.index.SimResourceManagers;
import dev.simulated_team.simulated.registrate.SimulatedRegistrate;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mixin(CreativeModeInventoryScreen.class)
public abstract class CreativeModeInventoryScreenMixin {

    @Inject(method = "getTooltipFromContainerItem", at = @At("RETURN"))
    private void dockingConnectorsPlus$attributeCreativeTab(final ItemStack stack, final CallbackInfoReturnable<List<Component>> cir) {
        final ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (!DockingConnectorsPlus.MOD_ID.equals(itemId.getNamespace())) {
            return;
        }

        final Set<String> simulatedLines = new HashSet<>();
        final ResourceLocation sectionId = SimulatedRegistrate.ITEM_TO_SECTION.get(itemId);
        if (sectionId != null) {
            final SimulatedSection section = SimResourceManagers.SIMULATED_SECTION.get(sectionId);
            if (section != null) {
                simulatedLines.add(section.title().text().getString());
            }
        }
        for (final CreativeModeTab tab : CreativeModeTabs.tabs()) {
            if (!tab.hasSearchBar() && tab.contains(stack)) {
                simulatedLines.add(tab.getDisplayName().getString());
            }
        }

        final List<Component> tooltip = cir.getReturnValue();
        int insertAt = tooltip.isEmpty() ? -1 : 1;
        for (int i = tooltip.size() - 1; i >= 0; i--) {
            if (simulatedLines.contains(tooltip.get(i).getString())) {
                tooltip.remove(i);
                insertAt = i;
            }
        }

        if (insertAt >= 0) {
            tooltip.add(insertAt, Component.literal(DockingConnectorsPlus.MOD_NAME).withStyle(ChatFormatting.BLUE));
        }
    }
}
