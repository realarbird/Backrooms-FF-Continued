package com.sp.entity.client.renderer;

import com.sp.SPBRevamped;
import com.sp.entity.client.model.WalkerModel;
import com.sp.entity.custom.WalkerEntity;
import com.sp.entity.ik.model.GeckoLib.GeoModelAccessor;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

import com.geckolib.cache.model.BakedGeoModel;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.layer.builtin.CustomBoneTextureGeoLayer;

/**
 * Original model base by ShadowZecro, colored by VoidAtomicX, head reworked with the help of Jarton.
 * <p>
 * PORT: delta (GeckoLib 4 -> 5). The GeckoLib-4 version extended {@code DynamicGeoEntityRenderer} and
 * overrode {@code renderRecursively}/{@code renderChildBones}/{@code boneRenderOverride} to (a) re-render
 * the {@code "eyes"} bone with an emissive eyes texture and (b) consume the procedural IK transform by
 * pushing each {@code MowzieGeoBone}'s world-space matrix / rotation-override into the pose stack.
 * <p>
 * GeckoLib 5 has no {@code DynamicGeoEntityRenderer} and no {@code renderRecursively}; bones are immutable
 * and there is no per-bone world-space matrix the renderer applies. So:
 * <ul>
 *   <li><b>Eyes glow:</b> preserved via a {@link CustomBoneTextureGeoLayer} (v5's supported per-bone
 *       texture/render-type override) bound to the {@code "eyes"} bone with {@code RenderTypes.eyes}.</li>
 *   <li><b>IK base-position read + tick:</b> still driven, now from {@link #extractRenderState} (CPU side),
 *       via {@link GeoModelAccessor} over the baked model — see {@code getModelPositions}/{@code tickComponentsClient}.</li>
 *   <li><b>IK write-back (force-matrix transform of dynamic/leg joints):</b> NOT reproducible — see the
 *       class-level delta on the SkinWalker renderer and on {@code MowzieGeoBone}. The v5 {@code BoneUpdater}
 *       API only exposes local {@code BoneSnapshot} rot/pos/scale, not arbitrary per-bone world matrices,
 *       so the legs fall back to GeckoLib's standard animation posing.</li>
 * </ul>
 */
public class WalkerRenderer extends GeoEntityRenderer<WalkerEntity, EntityRenderState> {
    private static final Identifier EYES_TEXTURE = Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "textures/entity/walker/walker.png");

    public WalkerRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new WalkerModel());
        // PORT: delta - eyes-bone emissive override (was boneRenderOverride in renderRecursively).
        this.withRenderLayer(new EyesGlowLayer(this));
    }

    @Override
    public void extractRenderState(WalkerEntity entity, EntityRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);

        // PORT: delta - IK tick + base-position read formerly ran from the model's setCustomAnimations and
        // the renderer's render(); both now run here off the baked model.
        BakedGeoModel baked = this.model.getBakedModel(this.model.getModelResource((GeoRenderState) state));
        GeoModelAccessor accessor = new GeoModelAccessor(baked);
        entity.tickComponentsClient(entity, accessor);
        entity.getModelPositions(entity, accessor);
    }

    /**
     * Re-renders the {@code "eyes"} bone with the emissive eyes render type, replacing the old
     * {@code boneRenderOverride} that drew that bone into {@code RenderLayer.getEyes(EYES_TEXTURE)}.
     */
    private static class EyesGlowLayer extends CustomBoneTextureGeoLayer<WalkerEntity, Void, EntityRenderState> {
        EyesGlowLayer(GeoRenderer<WalkerEntity, Void, EntityRenderState> renderer) {
            super(renderer, "eyes", EYES_TEXTURE);
        }

        @Override
        protected RenderType getRenderType(EntityRenderState state, Identifier texture) {
            return RenderTypes.eyes(texture);
        }
    }
}
