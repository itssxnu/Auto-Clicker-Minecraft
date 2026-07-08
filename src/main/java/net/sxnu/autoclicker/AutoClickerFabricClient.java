package net.sxnu.autoclicker;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.resources.Identifier;

public final class AutoClickerFabricClient implements ClientModInitializer {
    AutoClicker autoClicker = new AutoClicker();
    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(autoClicker::clientTickEvent);

        KeyMappingHelper.registerKeyMapping(AutoClicker.toggleHolding);
        KeyMappingHelper.registerKeyMapping(AutoClicker.openConfig);

        ClientLifecycleEvents.CLIENT_STARTED.register(autoClicker::clientReady);
        
        HudElementRegistry.attachElementAfter(
            VanillaHudElements.HOTBAR,
            Identifier.fromNamespaceAndPath(AutoClicker.MOD_ID, "hud"),
            (graphics, tickCounter) -> autoClicker.RenderGameOverlayEvent(graphics, tickCounter)
        );
    }
}
