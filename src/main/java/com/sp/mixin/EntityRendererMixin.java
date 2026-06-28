package com.sp.mixin;

import com.sp.init.BackroomsLevels;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Hide player name tags in the backrooms (A9). Ported Yarn→mojmap, Phase 6f.
 *
 * <p>PORT: delta — the original cancelled {@code renderLabelIfPresent(...,VertexConsumerProvider,...)},
 * which is gone in the 26.2 deferred pipeline (name tags are now part of {@code EntityRenderState} and
 * submitted via {@code submitNameDisplay}). Re-targeted to the surviving gate
 * {@code EntityRenderer.shouldShowName(Entity, double)}: returning {@code false} suppresses the name-tag
 * extraction/submission entirely. Same outcome (hidden player name tags in the backrooms), cleaner hook.
 */
@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin<T extends Entity> {

    @Inject(method = "shouldShowName(Lnet/minecraft/world/entity/Entity;D)Z", at = @At("HEAD"), cancellable = true)
    private void disablePlayerTags(T entity, double distanceToCamera, CallbackInfoReturnable<Boolean> cir){
        if(entity instanceof AbstractClientPlayer && BackroomsLevels.isInBackrooms(entity.level().dimension())) {
            cir.setReturnValue(false);
        }
    }

}
