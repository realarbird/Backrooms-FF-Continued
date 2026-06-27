package com.sp.render;

import com.sp.SPBRevamped;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

/**
 * Block / entity render-layer mapping (A6 / B6 / B7 — Mac-safe rebuild).
 *
 * <p>PORT: phase6 delta — the 1.20.1 version built custom Veil GLSL render layers via
 * {@code VeilRenderBridge.shaderState(...)} + {@code RenderLayer.of(...)}:
 * a PBR chunk layer ({@code pbr/pbr}, B6 DROP), a poolrooms-sky layer ({@code sky}, B7 DROP),
 * a fluorescent-light layer ({@code light/fluorescent_light}, A4 → emissive texture / vanilla
 * light level), and a distorted-entity layer ({@code distorted_entity}, B7 DROP).
 *
 * <p>26.2 removed both the custom-GLSL path (no {@code ShaderProgram}/Veil) AND the public
 * block-layer {@code RenderType} factories — terrain layers are now the {@code ChunkSectionLayer}
 * enum (SOLID/CUTOUT/TRANSLUCENT) driven by each block model's {@code "render_type"} JSON field,
 * and Fabric 26.2 dropped {@code BlockRenderLayerMap}. There is therefore no Mac-safe way to
 * register a <i>custom</i> chunk render layer from Java. The surviving accessors below map to the
 * nearest vanilla {@link RenderType} so callers still resolve; blocks render with vanilla
 * cutout/translucent layers (set in their model JSON), with no PBR normal/specular and no animated
 * sky/distortion shader.
 *
 * <p>The two atlas {@link Identifier} constants are retained: the custom-atlas mixin
 * ({@code mixin/customatlas/BakedModelManagerMixin}, rebuilt in phase 6f) still references them to
 * register the normal/height sprite atlases used for sprite packing.
 */
public final class RenderLayers {

    public static final Identifier NORMAL_ATLAS_TEXTURE =
            Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "textures/atlas/normal.png");
    public static final Identifier HEIGHT_ATLAS_TEXTURE =
            Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "textures/atlas/height.png");

    // PORT: phase6 delta (B6) — the custom PBR chunk layer is dropped. Blocks that were tagged PBR now
    // use vanilla cutout/translucent terrain layers (their model JSON "render_type"). We expose a
    // vanilla translucent entity layer keyed on the block atlas so the (phase-6f) renderlayer mixins
    // that still call getPbrLayer() resolve to a real RenderType.
    private static final RenderType VANILLA_BLOCK_TRANSLUCENT =
            RenderTypes.entityTranslucent(net.minecraft.client.renderer.texture.TextureAtlas.LOCATION_BLOCKS);

    // PORT: phase6 delta (B7) — the custom poolrooms-sky GLSL layer is dropped. Mapped to the same
    // vanilla translucent layer; the sky look itself is handled by the day-cycle lightmap tint (A5).
    private static final RenderType VANILLA_POOLROOMS_SKY = VANILLA_BLOCK_TRANSLUCENT;

    // PORT: phase6 delta (B7/A7) — the distorted-entity GLSL layer is dropped. Falls back to vanilla
    // entity cutout-cull (memoized per texture) so the SkinWalker head etc. still render, just without
    // the screen-space distortion.
    private static final Map<Identifier, RenderType> DISTORTED_ENTITY_CACHE = new HashMap<>();

    private RenderLayers() {
    }

    public static synchronized RenderType getDistortedEntity(Identifier texture) {
        return DISTORTED_ENTITY_CACHE.computeIfAbsent(texture, RenderTypes::entityCutoutCull);
    }

    /**
     * PORT: phase6 delta (B6) — formerly the custom PBR chunk layer; now a vanilla translucent layer.
     */
    public static RenderType getPbrLayer() {
        return VANILLA_BLOCK_TRANSLUCENT;
    }

    /**
     * PORT: phase6 delta (B7) — formerly the custom poolrooms-sky GLSL layer; now a vanilla
     * translucent layer (sky tint handled by the A5 day-cycle lightmap redirect).
     */
    public static RenderType getPoolroomsSky() {
        return VANILLA_POOLROOMS_SKY;
    }
}
