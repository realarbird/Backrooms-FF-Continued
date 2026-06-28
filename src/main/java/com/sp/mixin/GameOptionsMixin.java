package com.sp.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.sp.SPBRevampedClient;
import com.sp.compat.modmenu.ConfigStuff;
import net.minecraft.client.GraphicsPreset;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Camcorder-feel option overrides (NEW-INJECTION). Ported Yarn→mojmap, Phase 6f.
 *
 * <p>PORT: delta — {@code GameOptions}→{@code Options}; {@code SimpleOption}→{@code OptionInstance}
 * (get/set instead of getValue/setValue); the fluent getters renamed
 * ({@code getEntityShadows}→{@code entityShadows()}, {@code getFovEffectScale}→{@code fovEffectScale()}).
 * {@code getGraphicsMode}/{@code GraphicsMode} are gone — graphics is now {@code graphicsPreset()}
 * returning {@code OptionInstance<GraphicsPreset>} with the same {@code FABULOUS} value, so the
 * Fabulous→Fast forcing is retargeted to {@code graphicsPreset()}. Behaviour is unchanged.
 */
@Mixin(Options.class)
public class GameOptionsMixin {
    @Unique
    private Double prevFovEffectScale;

    @Shadow @Final private OptionInstance<Double> fovEffectScale;

    @ModifyReturnValue(method = "entityShadows", at = @At("RETURN"))
    private OptionInstance<Boolean> disableEntityShadows(OptionInstance<Boolean> original){
        if (SPBRevampedClient.shouldRenderCameraEffect()) {
            original.set(false);
        }
        return original;
    }

    @ModifyReturnValue(method = "graphicsPreset", at = @At("RETURN"))
    private OptionInstance<GraphicsPreset> disableGraphicsMode(OptionInstance<GraphicsPreset> original){
        if (SPBRevampedClient.shouldRenderCameraEffect() && original.get() == GraphicsPreset.FABULOUS) {
            original.set(GraphicsPreset.FAST);
        }

        return original;
    }

    @Inject(method = "fovEffectScale", at = @At("HEAD"), cancellable = true)
    private void disableSprintFOVChange(CallbackInfoReturnable<OptionInstance<Double>> cir){
        if (SPBRevampedClient.shouldRenderCameraEffect()) {
            cir.cancel();
            if (ConfigStuff.enableRealCamera){
                if(this.prevFovEffectScale == null){
                    this.prevFovEffectScale = this.fovEffectScale.get();
                }
                this.fovEffectScale.set(0.0);
                cir.setReturnValue(this.fovEffectScale);
            } else {
                if(this.prevFovEffectScale != null){
                    this.fovEffectScale.set(this.prevFovEffectScale);
                    this.prevFovEffectScale = null;
                }
                cir.setReturnValue(this.fovEffectScale);
            }
        }
    }

}
