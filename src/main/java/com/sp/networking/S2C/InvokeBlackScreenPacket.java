package com.sp.networking.S2C;

import com.sp.SPBRevamped;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record InvokeBlackScreenPacket(int duration, boolean shouldPauseSounds, boolean noEscape) implements CustomPacketPayload {
    public static final Type<InvokeBlackScreenPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "blk_screen"));

    public static final StreamCodec<RegistryFriendlyByteBuf, InvokeBlackScreenPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, InvokeBlackScreenPacket::duration,
            ByteBufCodecs.BOOL, InvokeBlackScreenPacket::shouldPauseSounds,
            ByteBufCodecs.BOOL, InvokeBlackScreenPacket::noEscape,
            InvokeBlackScreenPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void receive(InvokeBlackScreenPacket payload, ClientPlayNetworking.Context context) {
        int duration = payload.duration();
        boolean shouldPauseSounds = payload.shouldPauseSounds();
        boolean noEscape = payload.noEscape();

        context.client().execute(() ->
                com.sp.SPBRevampedClient.getCutsceneManager().blackScreen.showBlackScreen(duration, shouldPauseSounds, noEscape)
        );
    }
}
