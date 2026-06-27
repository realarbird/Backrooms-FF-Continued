package com.sp.entity.client.model;

import net.minecraft.resources.Identifier;

import com.geckolib.constant.dataticket.DataTicket;

/**
 * PORT: delta (GeckoLib 4 -> 5). In GeckoLib 4 the SkinWalker model picked its geo/texture per-entity
 * inside {@code getModelResource(entity)}/{@code getTextureResource(entity)} by reading live component
 * state. GeckoLib 5 passes only a {@link com.geckolib.renderer.base.GeoRenderState} to those methods,
 * so the per-entity choices must be computed during render-state extraction (renderer) and stashed on
 * the render-state, then read back in the model. These {@link DataTicket}s are the typed keys for that.
 */
public final class SkinWalkerRenderData {
    private SkinWalkerRenderData() {}

    /** Selected geometry resource (slim / default / final-form). */
    public static final DataTicket<Identifier> MODEL = DataTicket.create("spb_skinwalker_model", Identifier.class);
    /** Selected texture (player skin / placeholder). */
    public static final DataTicket<Identifier> TEXTURE = DataTicket.create("spb_skinwalker_texture", Identifier.class);
    /** Whether the entity is in its true (revealed) form this frame. */
    public static final DataTicket<Boolean> TRUE_FORM = DataTicket.create("spb_skinwalker_true_form", Boolean.class);
}
