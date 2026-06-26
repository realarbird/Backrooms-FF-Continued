package com.sp.mixin;

import com.mojang.serialization.Lifecycle;
import net.minecraft.world.level.storage.PrimaryLevelData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PrimaryLevelData.class)
public class LevelPropertiesMixin {

    @Shadow @Final private Lifecycle worldGenSettingsLifecycle;

    @Inject(method = "worldGenSettingsLifecycle", at = @At("HEAD"), cancellable = true)
    private void disableHereBeDragonsWarning(CallbackInfoReturnable<Lifecycle> cir){
        if(this.worldGenSettingsLifecycle == Lifecycle.experimental()){
            cir.setReturnValue(Lifecycle.stable());
        }
    }
}
