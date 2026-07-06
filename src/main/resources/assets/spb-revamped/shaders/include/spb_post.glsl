// Shared helpers for the Backrooms post chain (port of the 1.20.1 Veil-era
// pinwheel/shaders/include/common.glsl utility set to the vanilla 26.2 post pipeline).
// Only the functions the ported passes actually use survive.

// Conversions from https://www.shadertoy.com/view/3lycWz
vec3 rgb2yuv(vec3 rgb){
    float y = 0.299*rgb.r + 0.587*rgb.g + 0.114*rgb.b;
    return vec3(y, 0.493*(rgb.b-y), 0.877*(rgb.r-y));
}

vec3 yuv2rgb(vec3 yuv){
    float y = yuv.x;
    float u = yuv.y;
    float v = yuv.z;

    return vec3(
    y + 1.0/0.877*v,
    y - 0.39393*u - 0.58081*v,
    y + 1.0/0.493*u
    );
}

// Hash functions from https://www.shadertoy.com/view/4djSRW
float hash12(vec2 p){
    vec3 p3  = fract(vec3(p.xyx) * 0.1031);
    p3 += dot(p3, p3.yzx + 33.33);
    return fract((p3.x + p3.y) * p3.z);
}

float octave(float x){
    return mod(sin(x * 2.0) * sin(x * 4.0) * sin(x * 32.0), 1.0);
}

// Continuous tiling for the clamp-addressed post-chain noise inputs (the JSON texture
// inputs have no REPEAT mode): a mirror-repeat triangle wave. Plain fract() wrapping
// draws a hard seam at every integer crossing — on the sky's huge projected UVs those
// seams read as giant polygon edges.
vec2 spbMirror(vec2 uv) {
    return abs(fract(uv * 0.5) * 2.0 - 1.0);
}

// Per-frame dynamics fed from Java (com.sp.render.post.BackroomsPostEffects).
// Std140 layout MUST stay in sync with BackroomsPostEffects#writeDynamics and with the
// default value list in post_effect/backrooms.json (order + types).
layout(std140) uniform SpbDynamics {
    mat4 SpbInvProjMat;     // inverse of this frame's projection matrix
    mat4 SpbInvViewRotMat;  // inverse of the camera view-rotation matrix (view -> player/world axes)
    vec4 SpbFogColor;       // rgb = distance-fog color; w = mode: 0 off, 1 linear, 2 exponential
    vec4 SpbFogShape;       // x = linear fog start, y = linear fog end, z = exp density, w = sky mode (0 off, 1 procedural clouds, 2 solid black)
    vec4 SpbState;          // x = sunset timer 0..1, y = Level 1 knee-fog toggle, z = glitch time 0..1, w = barrel distortion strength
    vec4 SpbSunDir;         // xyz = sun direction (player space), w = bloom strength
    vec4 SpbGradeA;         // rgb = color-grade tint multiplier, w = saturation (1 = neutral)
    vec4 SpbGradeB;         // x = S-curve contrast amount 0..1, y = black lift, z = SSAO strength 0..1, w = god-ray strength
    vec4 SpbSunScreen;      // xy = sun position in screen UV, z = on-screen weight 0..1 (0 = sun behind/off-screen), w = unused
};

// Per-level color grade: exposure -> tint -> saturation -> filmic-ish S-curve -> black lift.
// Stands in for the tonal character the Veil deferred pipeline gave each level (its
// composite ran `color *= blur(SSAO) * 2.0`, i.e. a 2x exposure into the lights).
vec3 spbGrade(vec3 c) {
    float exposure = SpbSunScreen.w <= 0.0 ? 1.0 : SpbSunScreen.w; // 0 = unset (baked defaults)
    c *= exposure;
    c *= SpbGradeA.rgb;
    float luma = dot(c, vec3(0.2126, 0.7152, 0.0722));
    c = mix(vec3(luma), c, SpbGradeA.w);
    c = clamp(c, 0.0, 1.0);
    c = mix(c, c * c * (3.0 - 2.0 * c), SpbGradeB.x); // smoothstep S-curve: crush shadows, ease highlights
    c = c * (1.0 - SpbGradeB.y) + SpbGradeB.y;
    return clamp(c, 0.0, 1.0);
}

// Sky test: 26.2 renders with REVERSED-Z depth via ARB_clip_control (zero-to-one,
// far plane = 0.0). A cleared/sky pixel reads 0.0, NOT the classic 1.0.
bool spbIsSky(float depth) {
    return depth <= 0.000001;
}

// View-space position reconstruction from the depth buffer (replaces Veil's
// viewPosFromDepth). 26.2 renders REVERSED-Z (depth cleared to 0.0, near plane
// 0.05): view z = -near/depth to within far-plane epsilon. Only the projection's
// diagonal scale terms are used — those are identical between the standard and
// reversed-Z matrix flavors, so this holds regardless of which one the render
// state carries.
vec3 spbViewPosFromDepth(float depth, vec2 uv) {
    float viewZ = -0.05 / max(depth, 1.0e-8);
    vec2 ndc = uv * 2.0 - 1.0;
    return vec3(ndc.x * SpbInvProjMat[0][0] * -viewZ,
                ndc.y * SpbInvProjMat[1][1] * -viewZ,
                viewZ);
}

// View-space ray direction for a screen uv (used for sky pixels, depth = 0).
vec3 spbViewDirFromUv(vec2 uv) {
    vec2 ndc = uv * 2.0 - 1.0;
    return normalize(vec3(ndc.x * SpbInvProjMat[0][0], ndc.y * SpbInvProjMat[1][1], -1.0));
}

// View-space direction -> player-space (world-axis-aligned, camera-relative) direction.
// Replaces Veil's viewToPlayerSpace/viewDirFromUv pair.
vec3 spbViewToPlayerDir(vec3 viewDir) {
    return mat3(SpbInvViewRotMat) * viewDir;
}
