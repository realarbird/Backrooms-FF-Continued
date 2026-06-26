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

public record SyncServerComponent(boolean value, String component) implements CustomPacketPayload {
    public static final Type<SyncServerComponent> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "comp_sync"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncServerComponent> CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, SyncServerComponent::value,
            ByteBufCodecs.STRING_UTF8, SyncServerComponent::component,
            SyncServerComponent::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void receive(SyncServerComponent payload, ServerPlayNetworking.Context context) {
        boolean readBoolean = payload.value();
        String component = payload.component();

        context.server().execute(() -> {
            PlayerComponent playerComponent = InitializeComponents.PLAYER.get(context.player());

            switch (component) {
                case "beingCaptured": playerComponent.setBeingCaptured(readBoolean); break;
                case "cutscene": playerComponent.setDoingCutscene(readBoolean); break;
                case "flashlight": playerComponent.setFlashLightOn(readBoolean); break;
                case "glitch": playerComponent.setShouldInflictGlitchDamage(readBoolean); break;
                case "teleporting": playerComponent.setTeleporting(readBoolean); break;
            }

            playerComponent.sync();
        });
    }
}
