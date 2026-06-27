package com.sp.item.custom;

import com.sp.item.client.renderer.GasPumpItemRenderer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;

import com.geckolib.animatable.GeoItem;
import com.geckolib.animatable.SingletonGeoAnimatable;
import com.geckolib.animatable.client.GeoRenderProvider;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.renderer.GeoItemRenderer;
import com.geckolib.util.GeckoLibUtil;

import java.util.function.Consumer;

// PORT: delta (GeckoLib 4 -> 5). Package software.bernie.geckolib.* -> com.geckolib.*.
// The render-provider hook changed: createRenderer(Consumer<Object>) returning a RenderProvider whose
// getCustomRenderer() yielded a BuiltinModelItemRenderer is now createGeoRenderer(Consumer<GeoRenderProvider>)
// whose getGeoItemRenderer() yields a GeoItemRenderer. GeoItem.makeRenderer(this) is gone; the default
// getRenderProvider() on SingletonGeoAnimatable handles renderer caching, so the manual Supplier field
// and the getRenderProvider() override are dropped. Instance cache created via
// GeckoLibUtil.createInstanceCache(this) (was new SingletonAnimatableInstanceCache(this)).
// BlockItem moved to world.item; Block to world.level.block; Item.Settings -> Item.Properties.
public class GasPumpItem extends BlockItem implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public GasPumpItem(Block block, Properties settings) {
        super(block, settings);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private GeoItemRenderer<GasPumpItem> renderer = null;

            @Override
            public GeoItemRenderer<?> getGeoItemRenderer() {
                if (this.renderer == null) {
                    this.renderer = new GasPumpItemRenderer();
                }
                return this.renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
