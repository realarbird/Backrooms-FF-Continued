package com.sp.entity.client.model;

import com.sp.SPBRevamped;
import com.sp.entity.custom.WalkerEntity;
import net.minecraft.resources.Identifier;

import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;

/**
 * Original model base by ShadowZecro <br>
 * Colored by VoidAtomicX <br>
 * Head reworked with the help of Jarton
 * <p>
 * PORT: delta (GeckoLib 4 -> 5). {@code getModelResource}/{@code getTextureResource} now take a
 * {@link GeoRenderState} (not the animatable), and {@code setCustomAnimations(...)} no longer exists.
 * The per-frame IK tick formerly run from {@code setCustomAnimations} is now driven from the renderer's
 * render-state extraction (see {@code WalkerRenderer}).
 */
public class WalkerModel extends GeoModel<WalkerEntity> {
	private final Identifier MODEL = Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "geo/entity/walker.geo.json");
	private final Identifier TEXTURES = Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "textures/entity/walker/walker.png");

	@Override
	public Identifier getModelResource(GeoRenderState renderState) {
		return MODEL;
	}

	@Override
	public Identifier getTextureResource(GeoRenderState renderState) {
		return TEXTURES;
	}

	@Override
	public Identifier getAnimationResource(WalkerEntity animatable) {
		return null;
	}
}
