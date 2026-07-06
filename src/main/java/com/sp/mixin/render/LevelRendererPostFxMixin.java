package com.sp.mixin.render;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.sp.render.post.BackroomsPostEffects;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Adds the Backrooms post chain to the LEVEL frame graph, right where vanilla processes its
 * entity-outline post chain (after the weather pass, before the always-on-top pass).
 *
 * <p>This placement is load-bearing: the chain's scene pass samples the main target's DEPTH
 * buffer (fog distances, SSAO, knee fog, sky detection). {@code GameRenderer} processes its
 * {@code postEffectId} chain only after the hand/HUD 3D pass has CLEARED the main depth
 * texture (to 0.0 — 26.2 is reversed-Z), so a chain hung on that slot reads an empty depth
 * buffer and every depth-driven effect silently no-ops. Inside the level frame graph the
 * depth handle is the live world depth. A side benefit: the hand/held camera renders after
 * the chain, unaffected by the VHS warp — matching the original Veil composite, which
 * overlaid the hand from its own sampler after the effects.</p>
 */
@Mixin(LevelRenderer.class)
public class LevelRendererPostFxMixin {
    @Shadow @Final private LevelTargetBundle targets;

    @WrapOperation(method = "render", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/LevelRenderer;addAlwaysOnTopPass(Lcom/mojang/blaze3d/framegraph/FrameGraphBuilder;Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher$PreparedFrame;Lcom/mojang/blaze3d/buffers/GpuBufferSlice;)V"))
    private void spbRevamped$addBackroomsPostChain(LevelRenderer instance, FrameGraphBuilder frameGraphBuilder,
                                                   FeatureRenderDispatcher.PreparedFrame preparedFrame,
                                                   GpuBufferSlice fogBuffer, Operation<Void> original) {
        BackroomsPostEffects.addToLevelFrame(frameGraphBuilder, this.targets);
        original.call(instance, frameGraphBuilder, preparedFrame, fogBuffer);
    }
}
