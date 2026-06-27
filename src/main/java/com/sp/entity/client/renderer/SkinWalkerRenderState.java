package com.sp.entity.client.renderer;

import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

/**
 * PORT: delta (GeckoLib 4 -> 5 / 1.20.1 -> 26.2). The GeckoLib-4 SkinWalker renderer's
 * {@code animateModel(...)} read live entity state ({@code limbAnimator}, body/head yaw, pitch,
 * hand-swing) during {@code preRender}. In the deferred pipeline that data must be captured at
 * extraction time and carried on the render-state, then re-applied during the (animation) render pass
 * via a {@code BoneUpdater}. These fields carry the scalars the pose math needs.
 */
public class SkinWalkerRenderState extends LivingEntityRenderState {
	public boolean inTrueForm;
	public boolean shouldBeginReveal;
	public boolean isSneaking;

	public float limbPos;
	public float limbSpeed;
	public float animationProgress;
	public float headYawDeg;   // (headYaw - bodyYaw), degrees
	public float headPitchDeg; // entity pitch, degrees
	public float handSwingProgress;
}
