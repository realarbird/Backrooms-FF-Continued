#version 330

// Backrooms FF Continued (spb-revamped) override of the vanilla terrain vertex shader.
//
// Adds a Mac-safe wind sway for foliage — the ground-level restoration of the original mod's
// dropped GPU grass-blade animation (A18), rebuilt on the vanilla GL 3.3 chunk pipeline instead
// of Veil compute/indirect draw. Everything below the wind block is byte-for-byte the vanilla
// 26.2 block.vsh, so solid/translucent terrain is untouched.
//
// Safety of the wind:
//   * It only runs in the ALPHA_CUTOUT program (grass, ferns, leaves, flowers, glass panes, bars,
//     ...). Solid terrain (walls, floors) and translucent geometry (water, stained glass) never
//     enter this path, so they can never deform.
//   * Within cutout it displaces ONLY vertices the biome tinted GREEN (grass/ferns/leaves).
//     Glass panes, iron bars, doors etc. carry a white vertex colour and stay perfectly rigid —
//     so this is dimension-independent and cannot disturb the Backrooms' glass/windows.
//   * Displacement ramps with height above the block base (roots stay planted) and is tiny.
// Absolute world XZ (via the camera UBO) anchors the gust phase to the world so it does not slide
// as the camera moves; GameTime (fraction of a MC day) drives the animation.

#moj_import <minecraft:fog.glsl>
#moj_import <minecraft:dynamictransforms.glsl>
#moj_import <minecraft:projection.glsl>
#moj_import <minecraft:sample_lightmap.glsl>
#moj_import <minecraft:globals.glsl>

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV2;

uniform sampler2D Sampler2;

out float sphericalVertexDistance;
out float cylindricalVertexDistance;
out vec4 vertexColor;
out vec2 texCoord0;

void main() {
    vec3 pos = Position + ModelOffset;

#ifdef ALPHA_CUTOUT
    // Green-dominant vertices are biome-tinted foliage; white cutout (glass/bars) reads 0.
    float foliage = clamp((Color.g - max(Color.r, Color.b)) * 3.5, 0.0, 1.0);
    if (foliage > 0.001) {
        vec3 worldPos = pos + vec3(CameraBlockPos) + CameraOffset;
        // Height taper from the TEXTURE V (blade tip = top of sprite = UV0.y 0), NOT fract(worldPos.y)
        // — a full-block-tall blade's tip sits at an integer world Y, so fract() read ~0 there and the
        // tips (the part that should wave most) barely moved, which is why the old sway was invisible.
        float tip = clamp(1.0 - UV0.y, 0.0, 1.0);
        float ramp = 0.15 + 0.85 * tip * tip; // roots ~planted, tips sway the most
        float phase = worldPos.x * 0.6 + worldPos.z * 0.5;
        float t = GameTime * 2200.0; // GameTime = day fraction [0,1); ~few-second gust period
        // Multi-frequency gust so it breathes instead of ticking like a metronome.
        float sway = sin(t + phase) * 0.6 + sin(t * 1.7 + phase * 1.6) * 0.28 + sin(t * 0.5 + phase) * 0.18;
        float amp = 0.30 * foliage * ramp; // clearly visible (tips sway ~0.2 block), still gentle
        pos.x += sway * amp;
        pos.z += (sin(t * 0.85 + phase * 1.2) * 0.7 + sin(t * 1.9 + phase) * 0.3) * amp * 0.65;
    }
#else
    // Pool water (translucent terrain, biome-teal tinted): a gentle flowing surface swell. The wave
    // is a continuous function of WORLD xz, so neighbouring water blocks always agree along their
    // shared edge — the whole surface undulates as one connected sheet with no seams or breaks. Glass
    // and other translucent geometry are white / near-white tinted and fail the teal test, so they
    // stay perfectly flat (this runs in the solid program too, where nothing reads teal).
    float water = clamp((min(Color.g, Color.b) - Color.r) * 4.0, 0.0, 1.0);
    if (water > 0.001) {
        vec3 wpos = pos + vec3(CameraBlockPos) + CameraOffset;
        float wt = GameTime * 2200.0; // GameTime = day fraction [0,1); slow, lively drift (video: a moving surface)
        float swell = sin(wpos.x * 0.5 + wt)
                    + sin(wpos.z * 0.45 + wt * 1.25)
                    + 0.6 * sin((wpos.x + wpos.z) * 0.28 + wt * 0.7)
                    + 0.35 * sin((wpos.x - wpos.z) * 0.7 + wt * 1.6); // extra shorter ripple for a livelier surface
        pos.y += swell * 0.045 * water; // ~±0.11 block, seamless across the pool
    }
#endif

    gl_Position = ProjMat * ModelViewMat * vec4(pos, 1.0);

    sphericalVertexDistance = fog_spherical_distance(pos);
    cylindricalVertexDistance = fog_cylindrical_distance(pos);
    vertexColor = Color * sample_lightmap(Sampler2, UV2);
    texCoord0 = UV0;
}
