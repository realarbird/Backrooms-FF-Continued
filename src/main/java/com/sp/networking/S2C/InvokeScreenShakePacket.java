package com.sp.networking.S2C;

import com.sp.SPBRevamped;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record InvokeScreenShakePacket(double speed, double trauma) implements CustomPacketPayload {
    public static final Type<InvokeScreenShakePacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "scr_shake"));

    public static final StreamCodec<RegistryFriendlyByteBuf, InvokeScreenShakePacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.DOUBLE, InvokeScreenShakePacket::speed,
            ByteBufCodecs.DOUBLE, InvokeScreenShakePacket::trauma,
            InvokeScreenShakePacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void receive(InvokeScreenShakePacket payload, ClientPlayNetworking.Context context) {
        double speed = payload.speed();
        double trauma = payload.trauma();

        context.client().execute(() -> {
            com.sp.SPBRevampedClient.getCameraShake().noiseSpeed = speed;
            com.sp.SPBRevampedClient.getCameraShake().trauma = trauma;
        });
    }
}
