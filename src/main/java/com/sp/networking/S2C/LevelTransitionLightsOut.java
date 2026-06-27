package com.sp.networking.S2C;

import com.sp.SPBRevamped;
import com.sp.cca_stuff.InitializeComponents;
import com.sp.cca_stuff.PlayerComponent;
import com.sp.init.ModSounds;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public record LevelTransitionLightsOut(int time) implements CustomPacketPayload {
    public static final Type<LevelTransitionLightsOut> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "ltos"));

    public static final StreamCodec<RegistryFriendlyByteBuf, LevelTransitionLightsOut> CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, LevelTransitionLightsOut::time,
            LevelTransitionLightsOut::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void receive(LevelTransitionLightsOut payload, ClientPlayNetworking.Context context) {
        int time = payload.time();

        context.client().execute(() -> {
            if (context.player() != null) {
                PlayerComponent playerComponent = InitializeComponents.PLAYER.get(context.player());
                ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

                //Turn off the lights
                playerComponent.player.playSound(ModSounds.LIGHTS_OUT, 1, 1);
                com.sp.SPBRevampedClient.getCutsceneManager().blackScreen.showBlackScreen(time, false, false);

                //PlaySound after black screen is over
                executorService.schedule(() -> {
                    playerComponent.player.playSound(ModSounds.LIGHTS_ON, 1, 1);
                    com.sp.SPBRevampedClient.sendComponentSyncPacket(false, "teleporting");
                    executorService.shutdown();
                }, (time * 100L) / 2, TimeUnit.MILLISECONDS);
            }
        });
    }
}
