package com.sp.networking.S2C;

import com.sp.SPBRevamped;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

// Empty payload: a signal to the client to reload Veil's deferred light renderer.
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
            // PORT: phase6 — Veil deferred light renderer + SPBRevampedClient cutscene manager.
            // VeilRenderer renderer = VeilRenderSystem.renderer();
            // renderer.getDeferredRenderer().getLightRenderer().free();
            // if (SPBRevampedClient.getCutsceneManager().started) {
            //     SPBRevampedClient.getCutsceneManager().blackScreen.showBlackScreen(60, true, false);
            // }
        });
    }
}
