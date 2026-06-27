package com.sp.networking;

import com.sp.networking.C2S.*;
import com.sp.networking.S2C.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class InitializePackets {

    // 26.2: payload TYPES must be registered on both sides before they can be sent/received.
    // This runs from the common entrypoint (SPBRevamped#onInitialize) so both C2S and S2C
    // payload types exist on the server; the client re-runs onInitialize too.
    public static void registerC2SPackets() {
        // Register every payload type. 26.2 fabric-api: serverboundPlay() == C2S, clientboundPlay() == S2C
        // (the playC2S()/playS2C() names from earlier API revisions were renamed).
        PayloadTypeRegistry.serverboundPlay().register(TargetEntitySync.TYPE, TargetEntitySync.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(SeeActiveSkinwalkerSync.TYPE, SeeActiveSkinwalkerSync.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(SyncServerComponent.TYPE, SyncServerComponent.CODEC);

        PayloadTypeRegistry.clientboundPlay().register(InvokeScreenShakePacket.TYPE, InvokeScreenShakePacket.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(InvokeBlackScreenPacket.TYPE, InvokeBlackScreenPacket.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ReloadLightsPacket.TYPE, ReloadLightsPacket.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(SoundPacket.TYPE, SoundPacket.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(LevelTransitionLightsOut.TYPE, LevelTransitionLightsOut.CODEC);

        // Server-side receivers.
        ServerPlayNetworking.registerGlobalReceiver(TargetEntitySync.TYPE, TargetEntitySync::receive);
        ServerPlayNetworking.registerGlobalReceiver(SeeActiveSkinwalkerSync.TYPE, SeeActiveSkinwalkerSync::receive);
        ServerPlayNetworking.registerGlobalReceiver(SyncServerComponent.TYPE, SyncServerComponent::receive);
    }

    // Called from the client entrypoint (phase 6) to wire up clientbound receivers.
    public static void registerS2CPackets() {
        ClientPlayNetworking.registerGlobalReceiver(InvokeScreenShakePacket.TYPE, InvokeScreenShakePacket::receive);
        ClientPlayNetworking.registerGlobalReceiver(InvokeBlackScreenPacket.TYPE, InvokeBlackScreenPacket::receive);
        ClientPlayNetworking.registerGlobalReceiver(ReloadLightsPacket.TYPE, ReloadLightsPacket::receive);
        ClientPlayNetworking.registerGlobalReceiver(SoundPacket.TYPE, SoundPacket::receive);
        ClientPlayNetworking.registerGlobalReceiver(LevelTransitionLightsOut.TYPE, LevelTransitionLightsOut::receive);
    }
}
