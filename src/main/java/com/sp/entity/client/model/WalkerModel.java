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
	// PORT: delta (GeckoLib 4 -> 5). Bare "<subtype>/<name>" model key; file lives at
	// assets/spb-revamped/geckolib/models/entity/walker.geo.json. See SkinWalkerRenderer for the note.
	private final Identifier MODEL = Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "entity/walker");
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
