package com.sp.entity.ik.model.GeckoLib;

import com.sp.entity.ik.model.BoneAccessor;
import com.sp.entity.ik.model.ModelAccessor;
import com.sp.entity.ik.util.PrAnCommonClass;

import com.geckolib.cache.model.BakedGeoModel;
import com.geckolib.cache.model.GeoBone;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * PORT: delta (GeckoLib 4 -> 5). Previously wrapped a {@code GeoModel} and called
 * {@code GeoModel.getBone(String)}, which returned the (mutable) {@code GeoBone}/{@code MowzieGeoBone}
 * directly. In GeckoLib 5 {@code GeoModel} no longer exposes {@code getBone(...)} and bones are immutable,
 * so this now wraps a {@link BakedGeoModel} (whose {@code getBone(String)} survives) and adapts each
 * looked-up immutable {@link GeoBone} into a {@link MowzieGeoBone} wrapper that carries the per-frame
 * procedural IK transform.
 * <p>
 * Wrappers are cached per bone name for the lifetime of this accessor so the IK system reads back the
 * same instance it wrote {@code moveTo(...)} onto within a frame (the IK render-tick creates a fresh
 * accessor each frame, mirroring the old per-frame model lookup).
 */
public class GeoModelAccessor implements ModelAccessor {
    private final BakedGeoModel bakedModel;
    private final Map<String, MowzieGeoBone> wrapperCache = new HashMap<>();

    public GeoModelAccessor(BakedGeoModel bakedModel) {
        this.bakedModel = bakedModel;
    }

    @Override
    public Optional<BoneAccessor> getBone(String boneName) {
        MowzieGeoBone cached = this.wrapperCache.get(boneName);
        if (cached != null) {
            return Optional.of(cached);
        }

        Optional<GeoBone> optionalGeoBone = this.bakedModel == null ? Optional.empty() : this.bakedModel.getBone(boneName);

        if (optionalGeoBone.isEmpty()) {
            PrAnCommonClass.throwInDevOnly(new IllegalArgumentException("Bone not found: " + boneName));
            return Optional.empty();
        }

        MowzieGeoBone wrapper = new MowzieGeoBone(optionalGeoBone.get());
        this.wrapperCache.put(boneName, wrapper);
        return Optional.of(wrapper);
    }
}
