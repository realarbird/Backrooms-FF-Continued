package com.sp.item.client.renderer;

import com.sp.item.client.model.GasPumpItemModel;
import com.sp.item.custom.GasPumpItem;
// PORT: delta (GeckoLib 4 -> 5) - package software.bernie.geckolib.renderer -> com.geckolib.renderer.
import com.geckolib.renderer.GeoItemRenderer;

public class GasPumpItemRenderer extends GeoItemRenderer<GasPumpItem> {
    public GasPumpItemRenderer() {
        super(new GasPumpItemModel());
    }
}