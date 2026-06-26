package com.sp.mixin.skinstolen;

import com.sp.cca_stuff.InitializeComponents;
import com.sp.cca_stuff.PlayerComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerEntityMixin {

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;setCamera(Lnet/minecraft/world/entity/Entity;)V", ordinal = 0))
    private void cantEscapeSpectating(ServerPlayer instance, Entity entity){
        PlayerComponent component = InitializeComponents.PLAYER.get(instance);
        if(!component.hasBeenCaptured()){
            instance.setCamera(entity);
        }
    }

    // Yarn target `canBeSpectated(ServerPlayerEntity)` is named `broadcastToPlayer(ServerPlayer)` in 26.2.
    @Inject(method = "broadcastToPlayer", at = @At("HEAD"), cancellable = true)
    private void stopSpectatorPlayersFromNotBeingCounted(ServerPlayer spectator, CallbackInfoReturnable<Boolean> cir){
        PlayerComponent component = InitializeComponents.PLAYER.get((ServerPlayer) (Object) this);
        if (component.hasBeenCaptured() || component.isBeingCaptured()){
            cir.setReturnValue(true);
        }
    }

    // PORT-REVIEW (Phase 5): The original Yarn `@Redirect(method = "attack", setCameraEntity ...)`
    // guard has no 26.2 target — neither Player.attack nor ServerPlayer calls setCamera inside any
    // attack path. The "can't switch spectator target by attacking" behavior is dropped pending review.
}
