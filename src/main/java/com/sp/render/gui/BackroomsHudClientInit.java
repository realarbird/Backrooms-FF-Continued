package com.sp.render.gui;

import net.fabricmc.api.ClientModInitializer;

/**
 * Dedicated client initializer for the Mac-safe HUD overlays (Phase 6c).
 *
 * <p>Registers the VHS camcorder overlay, the stamina bar and the intro title as Fabric
 * {@code HudElement}s. This is a standalone entrypoint so the HUD can be wired and verified before the
 * main client entrypoint ({@code com.sp.SPBRevampedClient}) is ported in Phase 6e.</p>
 *
 * <p>PORT: phase6e — once {@code SPBRevampedClient} compiles, either fold these three {@code register()}
 * calls into its {@code onInitializeClient()} and drop this class + its {@code fabric.mod.json} entry, or
 * leave it as-is (both approaches are valid; the elements register exactly once either way).</p>
 */
public class BackroomsHudClientInit implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BackroomsHudOverlay.register();
        StaminaBar.register();
        TitleText.register();
    }
}
