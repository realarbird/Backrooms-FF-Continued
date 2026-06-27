package com.sp.networking.S2C;

import com.sp.SPBRevamped;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

// Empty payload: formerly a signal to reload Veil's deferred light renderer; with Veil removed (Mac-safe
// vanilla Blocks.LIGHT) it now only triggers the cutscene black-screen during a light transition.
public record ReloadLightsPacket() implements CustomPacketPayload {
    public static final Type<ReloadLightsPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "rl_lights"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ReloadLightsPacket> CODEC =
            StreamCodec.unit(new ReloadLightsPacket());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void receive(ReloadLightsPacket payload, ClientPlayNetworking.Context context) {
        context.client().execute(() -> {
            // PORT: delta (phase6) — the Veil deferred-light-renderer free() is dropped (no Veil; dynamic
            // light is now vanilla Blocks.LIGHT, which needs no reload). Only the cutscene black-screen
            // beat (covering the light re-place during a transition) is kept.
            if (com.sp.SPBRevampedClient.getCutsceneManager().started) {
                com.sp.SPBRevampedClient.getCutsceneManager().blackScreen.showBlackScreen(60, true, false);
            }
        });
    }
}
