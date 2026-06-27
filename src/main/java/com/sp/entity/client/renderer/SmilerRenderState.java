package com.sp.entity.client.renderer;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;

/**
 * PORT: delta (1.20.1 -> 26.2). 26.2 entity rendering is render-state based: per-entity data that the
 * old {@code getTexture(entity)} read live must be captured during extraction and carried on the state.
 * The Smiler's per-entity texture choice (default / variant1 / variant2, from its component) lives here.
 */
public class SmilerRenderState extends LivingEntityRenderState {
	public Identifier texture;
}
