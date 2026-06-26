package com.sp.networking.C2S;

import com.sp.SPBRevamped;
import com.sp.cca_stuff.InitializeComponents;
import com.sp.cca_stuff.PlayerComponent;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

// 26.2: PacketByteBuf channel API is gone; this is a CustomPacketPayload with a StreamCodec.
public record TargetEntitySync(int targetID) implements CustomPacketPayload {
    public static final Type<TargetEntitySync> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "targ_ent"));

    public static final StreamCodec<RegistryFriendlyByteBuf, TargetEntitySync> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, TargetEntitySync::targetID,
            TargetEntitySync::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void receive(TargetEntitySync payload, ServerPlayNetworking.Context context) {
        int targetID = payload.targetID();

        context.server().execute(() -> {
            PlayerComponent playerComponent = InitializeComponents.PLAYER.get(context.player());

            if (targetID == -1) {
                playerComponent.setTargetEntity(null);
            } else {
                playerComponent.setTargetEntity(context.player().level().getEntity(targetID));
            }
        });
    }
}
