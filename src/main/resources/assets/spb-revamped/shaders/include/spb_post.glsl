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
};

// View-space position reconstruction from the depth buffer (replaces Veil's
// viewPosFromDepth; vanilla GL depth convention, ndc z in [-1,1]).
vec3 spbViewPosFromDepth(float depth, vec2 uv) {
    vec4 ndc = vec4(uv * 2.0 - 1.0, depth * 2.0 - 1.0, 1.0);
    vec4 viewPos = SpbInvProjMat * ndc;
    return viewPos.xyz / viewPos.w;
}

// View-space direction -> player-space (world-axis-aligned, camera-relative) direction.
// Replaces Veil's viewToPlayerSpace/viewDirFromUv pair.
vec3 spbViewToPlayerDir(vec3 viewDir) {
    return mat3(SpbInvViewRotMat) * viewDir;
}
