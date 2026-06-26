package com.sp.mixin.cutscene;

import com.sp.SPBRevampedClient;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LocalPlayer.class)
public class ClientPlayerEntityMixin {

    @Inject(method = "isControlledCamera", at = @At("HEAD"), cancellable = true)
    private void isCamera(CallbackInfoReturnable<Boolean> cir){
        if(SPBRevampedClient.getCutsceneManager().isPlaying){
            cir.setReturnValue(true);
        }
    }

}
