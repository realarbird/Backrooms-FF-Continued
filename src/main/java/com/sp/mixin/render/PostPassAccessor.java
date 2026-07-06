package com.sp.mixin.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import net.minecraft.client.renderer.PostPass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

/**
 * Exposes a pass's custom uniform buffers (a mutable HashMap bound by block name each frame via
 * RenderPass.setUniform). BackroomsPostEffects swaps the baked read-only "SpbDynamics" entry for
 * a writable buffer it updates per frame.
 */
@Mixin(PostPass.class)
public interface PostPassAccessor {
    @Accessor("customUniforms")
    Map<String, GpuBuffer> spbRevamped$getCustomUniforms();
}
