package com.sp.mixin;

import com.sp.entity.custom.SkinWalkerEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BodyRotationControl.class)
public class BodyControlMixin {

    @Shadow @Final private Mob mob;

    // Yarn `slowlyAdjustBody` is gone in 26.2; the body-rotation step is `rotateBodyIfNecessary`
    // (called from clientTick). Cancelling it keeps the SkinWalker's body from snapping to the head.
    @Inject(method = "rotateBodyIfNecessary", at = @At("HEAD"), cancellable = true)
    private void redirect(CallbackInfo ci){
        if(this.mob instanceof SkinWalkerEntity) {
            ci.cancel();
        }
    }
}
