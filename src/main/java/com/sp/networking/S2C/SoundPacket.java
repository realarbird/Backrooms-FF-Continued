package com.sp.networking.S2C;

import com.sp.SPBRevamped;
import com.sp.SPBRevampedClient;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

public record SoundPacket(Holder<SoundEvent> sound, float volume, float pitch) implements CustomPacketPayload {
    public static final Type<SoundPacket> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "snd"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SoundPacket> CODEC = StreamCodec.composite(
            SoundEvent.STREAM_CODEC, SoundPacket::sound,
            ByteBufCodecs.FLOAT, SoundPacket::volume,
            ByteBufCodecs.FLOAT, SoundPacket::pitch,
            SoundPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void receive(SoundPacket payload, ClientPlayNetworking.Context context) {
        Holder<SoundEvent> sound = payload.sound();
        float volume = payload.volume();
        float pitch = payload.pitch();

        // Delegate to the client class so this payload record carries no net.minecraft.client.*
        // reference (otherwise Fabric refuses to load SoundPacket on a dedicated server when its
        // TYPE/CODEC are registered). SPBRevampedClient is @Environment(CLIENT)-safe and is only
        // touched here at runtime on the client, never during server-side class loading.
        context.client().execute(() -> SPBRevampedClient.playUISound(sound, volume, pitch));
    }
}
