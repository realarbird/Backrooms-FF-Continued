package com.sp.mixin.hudandresolution;

import com.mojang.blaze3d.vertex.PoseStack;
import com.sp.compat.modmenu.ConfigStuff;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Hide the first-person hand / held item (A10). Ported Yarn→mojmap, Phase 6f.
 *
 * <p>PORT: delta — {@code HeldItemRenderer}→{@code ItemInHandRenderer}; the old
 * {@code renderFirstPersonItem}→{@code renderArmHoldingItem(...VertexConsumerProvider...)} redirect is
 * gone (deferred pipeline, no {@code VertexConsumerProvider}). The whole first-person hand pass is now
 * funnelled through the public {@code submitHandsWithItems(float, PoseStack, SubmitNodeCollector,
 * LocalPlayer, int)} entry point. Cancelling it at HEAD when hands are disabled suppresses both arms +
 * held items. Delta: hides the entire hand pass (arm + item + map) rather than just the
 * arm-holding-item draw — a superset that matches the "hide hands" intent.
 */
@Mixin(ItemInHandRenderer.class)
public abstract class DontRenderHandMixin {

    @Inject(method = "submitHandsWithItems", at = @At("HEAD"), cancellable = true)
    private void cancel(float partialTick, PoseStack poseStack, SubmitNodeCollector collector, LocalPlayer player, int light, CallbackInfo ci){
        if (!ConfigStuff.showHands) {
            ci.cancel();
        }
    }
}
