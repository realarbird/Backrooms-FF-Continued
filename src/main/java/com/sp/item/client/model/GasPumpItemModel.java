package com.sp.item.client.model;

import com.sp.SPBRevamped;
import com.sp.item.custom.GasPumpItem;
import net.minecraft.resources.Identifier;

import com.geckolib.model.GeoModel;
import com.geckolib.renderer.base.GeoRenderState;

// PORT: delta (GeckoLib 4 -> 5). Package software.bernie.geckolib.model -> com.geckolib.model;
// net.minecraft.util.Identifier -> net.minecraft.resources.Identifier and
// new Identifier(ns, path) -> Identifier.fromNamespaceAndPath(ns, path).
// getModelResource/getTextureResource now receive a GeoRenderState (not the live animatable); these
// return fixed resources so they ignore the argument. getAnimationResource is still keyed on the
// animatable. Resource paths preserved verbatim from 1.20.1.
public class GasPumpItemModel extends GeoModel<GasPumpItem> {
    @Override
    public Identifier getModelResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "geo/blocks/staircase.geo.json");
    }

    @Override
    public Identifier getTextureResource(GeoRenderState renderState) {
        return Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "textures/block/staircase.png");
    }

    @Override
    public Identifier getAnimationResource(GasPumpItem animatable) {
        return Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "animations/staircase.animation.json");
    }
}
