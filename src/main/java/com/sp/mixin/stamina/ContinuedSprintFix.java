package com.sp.mixin.stamina;

import com.sp.mixininterfaces.ServerPlayNetworkSprint;
import net.minecraft.network.protocol.game.ServerboundPlayerCommandPacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public class ContinuedSprintFix implements ServerPlayNetworkSprint {
    @Unique private boolean shouldStopSprinting = true;

    @Inject(method = "handlePlayerCommand", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;setSprinting(Z)V", ordinal = 0))
    private void setToFalse(ServerboundPlayerCommandPacket packet, CallbackInfo ci){
        this.shouldStopSprinting = false;
    }

    @Inject(method = "handlePlayerCommand", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;setSprinting(Z)V", ordinal = 1))
    private void setToTrue(ServerboundPlayerCommandPacket packet, CallbackInfo ci){
        this.shouldStopSprinting = true;
    }

    @Override
    public boolean getShouldStopSprinting() {
        return this.shouldStopSprinting;
    }
}
