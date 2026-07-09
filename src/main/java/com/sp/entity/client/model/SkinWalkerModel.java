package com.sp.entity.client.model;

import com.sp.SPBRevamped;
import com.sp.entity.custom.SkinWalkerEntity;
import net.minecraft.resources.Identifier;

import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;

/**
 * PORT: delta (GeckoLib 4 -> 5). {@code getModelResource}/{@code getTextureResource} now receive a
 * {@link GeoRenderState} rather than the live entity, so the slim/default/final-form model choice and
 * the captured-player-skin texture choice (which need component + client-world state) are computed in
 * {@code SkinWalkerRenderer.extractRenderState(...)} and stashed on the render-state via
 * {@link SkinWalkerRenderData}. This model just reads them back. The animation resource is still keyed
 * on the animatable. The former {@code setCustomAnimations(...)} IK tick now runs from the renderer.
 */
public class SkinWalkerModel extends GeoModel<SkinWalkerEntity> {
	private final Identifier PLACEHOLDER_TEXTURE = Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "textures/entity/skinwalker/placeholder.png");
	// PORT: delta (GeckoLib 4 -> 5). Model/animation identifiers are now the bare "<subtype>/<name>" key
	// (no geo//animations/ prefix, no .geo.json/.animation.json suffix); GeckoLib 5 resolves them under
	// assets/<ns>/geckolib/models and .../geckolib/animations. See SkinWalkerRenderer for the full note.
	private final Identifier DEFAULT_MODEL = Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "entity/skin_walker_default");

	private final Identifier ANIMATION = Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "entity/skinwalker");

	@Override
	public Identifier getModelResource(GeoRenderState renderState) {
		return renderState.getOrDefaultGeckolibData(SkinWalkerRenderData.MODEL, DEFAULT_MODEL);
	}

	@Override
	public Identifier getTextureResource(GeoRenderState renderState) {
		return renderState.getOrDefaultGeckolibData(SkinWalkerRenderData.TEXTURE, PLACEHOLDER_TEXTURE);
	}

	@Override
	public Identifier getAnimationResource(SkinWalkerEntity animatable) {
		return ANIMATION;
	}
}
