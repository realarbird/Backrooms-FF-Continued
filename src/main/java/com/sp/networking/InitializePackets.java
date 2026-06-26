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
        // Register every payload type (C2S on serverboundPlay, S2C on clientboundPlay).
        PayloadTypeRegistry.playC2S().register(TargetEntitySync.TYPE, TargetEntitySync.CODEC);
        PayloadTypeRegistry.playC2S().register(SeeActiveSkinwalkerSync.TYPE, SeeActiveSkinwalkerSync.CODEC);
        PayloadTypeRegistry.playC2S().register(SyncServerComponent.TYPE, SyncServerComponent.CODEC);

        PayloadTypeRegistry.playS2C().register(InvokeScreenShakePacket.TYPE, InvokeScreenShakePacket.CODEC);
        PayloadTypeRegistry.playS2C().register(InvokeBlackScreenPacket.TYPE, InvokeBlackScreenPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(ReloadLightsPacket.TYPE, ReloadLightsPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(SoundPacket.TYPE, SoundPacket.CODEC);
        PayloadTypeRegistry.playS2C().register(LevelTransitionLightsOut.TYPE, LevelTransitionLightsOut.CODEC);

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
