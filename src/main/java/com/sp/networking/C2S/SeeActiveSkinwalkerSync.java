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

public record SeeActiveSkinwalkerSync(boolean canSeeSkinwalker) implements CustomPacketPayload {
    public static final Type<SeeActiveSkinwalkerSync> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "see_skin"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SeeActiveSkinwalkerSync> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, SeeActiveSkinwalkerSync::canSeeSkinwalker,
            SeeActiveSkinwalkerSync::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void receive(SeeActiveSkinwalkerSync payload, ServerPlayNetworking.Context context) {
        boolean canSeeSkinwalker = payload.canSeeSkinwalker();

        context.server().execute(() -> {
            PlayerComponent playerComponent = InitializeComponents.PLAYER.get(context.player());
            playerComponent.setCanSeeActiveSkinWalkerTarget(canSeeSkinwalker);
        });
    }
}
