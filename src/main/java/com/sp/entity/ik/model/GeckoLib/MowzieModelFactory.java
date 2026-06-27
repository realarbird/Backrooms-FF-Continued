package com.sp.entity.ik.model.GeckoLib;

/**
 * Provided by Bob Mowzie from <a href="https://www.curseforge.com/minecraft/mc-mods/mowzies-mobs">Mowzie's Mobs</a>
 * <p>
 * PORT: delta (GeckoLib 4 -> 5) — DEFUNCT. In GeckoLib 4 this implemented
 * {@code software.bernie.geckolib.loading.object.BakedModelFactory} and was registered via
 * {@code GeckoLibUtil.addCustomBakedModelFactory(MOD_ID, new MowzieModelFactory())} so that the
 * model-bake step would produce {@link MowzieGeoBone} instances (a mutable {@code GeoBone} subclass)
 * instead of vanilla bones, giving the IK system bones it could write transforms onto.
 * <p>
 * GeckoLib 5 removed this customization point entirely:
 * <ul>
 *   <li>{@code BakedModelFactory}, {@code loading.object.{GeometryTree,BoneStructure}},
 *       {@code loading.json.raw.{Bone,Cube,ModelProperties}} and {@code util.RenderUtils.arrayToVec}
 *       no longer exist — the model loader was rewritten ({@code com.geckolib.loading.definition.*}).</li>
 *   <li>{@code GeckoLibUtil.addCustomBakedModelFactory(...)} is gone — there is no hook to swap in a
 *       custom {@code GeoBone} type at bake time.</li>
 *   <li>{@code GeoBone}/{@code CuboidGeoBone} are now immutable/final and cannot be subclassed.</li>
 * </ul>
 * Consequently there is no per-bone factory to rebuild. The IK bridge instead wraps the baked,
 * immutable bones at access time ({@link GeoModelAccessor} -> {@link MowzieGeoBone}); see those classes.
 * <p>
 * This class is retained only as a defunct placeholder so the now-removed registration call in
 * {@code SPBRevamped} resolves until that entrypoint is rewired (sub-phase 6e), at which point the
 * registration line and this class should be deleted. It performs no work.
 */
public final class MowzieModelFactory {
    public MowzieModelFactory() {
    }
}
