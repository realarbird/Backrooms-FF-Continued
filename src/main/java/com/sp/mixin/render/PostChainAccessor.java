package com.sp.mixin.render;

import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

/** Exposes the compiled pass list so BackroomsPostEffects can find its dynamic uniform blocks. */
@Mixin(PostChain.class)
public interface PostChainAccessor {
    @Accessor("passes")
    List<PostPass> spbRevamped$getPasses();
}
