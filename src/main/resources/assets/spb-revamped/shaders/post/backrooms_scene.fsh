#version 330

// Backrooms scene pass — SSAO + per-level fog + Level 1 knee fog + procedural sky +
// screen-space god rays + per-level color grade.
// Port of the fog/sky pieces of the 1.20.1 Veil chain (vhs/everything.fsh LEVEL1_FOG,
// vhs/mixed.fsh INFINITE_FIELD/LEVEL324 fog + poolrooms sun/stars, include/sky.glsl)
// onto the vanilla 26.2 post pipeline, plus depth-only reconstructions of two Veil
// effects: the SSAO multiply (vhs/ssao.fsh ran on G-buffer normals; this one derives
// normals from depth derivatives) and the poolrooms volumetric light (shadow-map
// raymarch originally; approximated as luminance-masked radial scattering toward the
// sun's screen position). Still-dropped pieces: shadows, puddles, reflections
// (need material ids / a shadow pass) — see docs/MAC_SAFE_RENDERING.md.

#moj_import <minecraft:globals.glsl>
#moj_import <spb-revamped:spb_post.glsl>

uniform sampler2D InSampler;
uniform sampler2D InDepthSampler;
uniform sampler2D FogNoiseSampler;
uniform sampler2D CloudNoise1Sampler;
uniform sampler2D CloudNoise2Sampler;
uniform sampler2D StarsSampler;

// Per-pixel light data fed from Java (BackroomsPostEffects#writeLights). Restores the
// Veil-era deferred lighting: in the fixture-lit levels the lightmap is forced fullbright
// (albedo passthrough) and this block is the ONLY lighting. Std140 layout MUST stay in sync
// with BackroomsPostEffects#writeLights and the default list in post_effect/backrooms.json.
#define SPB_MAX_POINT_LIGHTS 64
#define SPB_MAX_SPOTLIGHTS 8
layout(std140) uniform SpbLights {
    vec4 SpbLightMeta;                          // x = point count, y = spot count, z = deband on, w = relight mode (0 vanilla, 1 deferred)
    vec4 SpbAmbient;                            // rgb = ambient floor color (deferred), w = poolrooms water fx toggle
    vec4 SpbSpotPos[SPB_MAX_SPOTLIGHTS];        // xyz = camera-relative origin, w = cone cos(inner)
    vec4 SpbSpotDir[SPB_MAX_SPOTLIGHTS];        // xyz = direction, w = cone cos(outer)
    vec4 SpbSpotColor[SPB_MAX_SPOTLIGHTS];      // rgb = color * intensity, w = range
    vec4 SpbLightPos[SPB_MAX_POINT_LIGHTS];     // xyz = camera-relative position, w = radius
    vec4 SpbLightColor[SPB_MAX_POINT_LIGHTS];   // rgb = color * intensity, w = unused
};

in vec2 texCoord;
out vec4 fragColor;

#define OFFSET vec2(0.1965249, 0.6546237)
#define LEVEL1_FOG_COLOR vec3(0.8, 0.8, 0.8)

const vec3 SkyColor = vec3(0.42, 0.85, 1.1);
const vec3 SkyColor2 = vec3(1.0, 0.8, 0.2);

vec3 spbCameraPos() {
    return vec3(CameraBlockPos) + CameraOffset;
}

// ---- Level 1 knee fog (everything.fsh, verbatim port) ----

float map(vec3 p){
    return p.y - 21.75;
}

float noise3D(vec3 p){
    float z = p.z;
    vec2 z1 = (floor(z) * OFFSET + p.xz) / 5.0;
    vec2 z2 = ((floor(z) + 1.0) * OFFSET + p.xz) / 5.0;
    // spbMirror (mirror-repeat), NOT plain fract: the noise sampler is clamp-addressed, so a fract()
    // wrap draws a hard seam line at every integer UV. Those seams are anchored in world space and
    // sweep across the screen as the camera moves — the "fog glitches while moving" the player saw.
    // Mirror-repeat is continuous, so the fog stays smooth in motion (same fix §18.2 used for the sky).
    float n1 = texture(FogNoiseSampler, spbMirror(z1 + GameTime * 100.0)).r;
    float n2 = texture(FogNoiseSampler, spbMirror(z2 + GameTime * 100.0)).r;
    float ratio = fract(z);
    return mix(n1, n2, ratio);
}

vec3 kneeFog(vec3 color, vec3 ro, vec3 rd, float worldDepth) {
    float travDist = 0.0;
    bool inside = false;
    float fog = 0.0;
    vec3 p = ro;

    for (int i = 0; i < 100; i++) {
        if (!inside) {
            if (worldDepth > 500.0) {
                break;
            }

            p = ro + rd * travDist;
            float d = map(p);
            travDist += d;

            if (d < 0.001) {
                inside = true;
            }
        } else {
            float noise = noise3D(p);
            // 3.5x the original 0.001/step: the Veil chain accumulated against HDR
            // buffers where the same constant read much thicker; on the LDR vanilla
            // target the verbatim value was nearly invisible.
            fog += 0.0035 * noise;
            p = ro + rd * travDist;
            travDist += 0.1;
            if (travDist > worldDepth || fog >= 1.0 || travDist > 50.0 || p.y < 20.5) {
                break;
            }
        }
    }

    return mix(color, LEVEL1_FOG_COLOR, clamp(fog, 0.0, 1.0));
}

// ---- SSAO (depth-only reconstruction of the Veil chain's vhs/ssao.fsh multiply) ----

#define AO_TAPS 10
#define AO_RADIUS 0.85        // world-space sample radius (blocks)
#define AO_FALLOFF 1.6        // occluders beyond this distance stop contributing

float spbAO(vec3 viewPos, vec2 uv) {
    float dist = -viewPos.z;
    // Skip the held camera/hand (very near plane) and far pixels where taps collapse.
    if (dist < 0.9 || dist > 90.0) {
        return 1.0;
    }

    vec3 normal = normalize(cross(dFdx(viewPos), dFdy(viewPos)));
    normal *= sign(dot(normal, -viewPos));

    // Screen-space UV radius of a world-space offset at this depth (P00 = 1/invP00).
    vec2 projScale = 0.5 * vec2(1.0 / SpbInvProjMat[0][0], 1.0 / SpbInvProjMat[1][1]);
    float rotation = hash12(gl_FragCoord.xy) * 6.2831853;

    float occlusion = 0.0;
    for (int i = 0; i < AO_TAPS; i++) {
        float angle = rotation + float(i) * 2.39996323; // golden-angle spiral
        float radius = AO_RADIUS * (float(i) + 0.7) / float(AO_TAPS);
        vec2 tapUv = uv + vec2(cos(angle), sin(angle)) * radius * projScale / dist;
        if (tapUv.x <= 0.0 || tapUv.x >= 1.0 || tapUv.y <= 0.0 || tapUv.y >= 1.0) {
            continue;
        }

        float tapDepth = texture(InDepthSampler, tapUv).r;
        if (spbIsSky(tapDepth)) {
            continue;
        }
        vec3 diff = spbViewPosFromDepth(tapDepth, tapUv) - viewPos;
        float len = length(diff);
        occlusion += clamp(dot(normal, diff / max(len, 0.0001)) - 0.10, 0.0, 1.0)
                   * clamp(1.0 - len / AO_FALLOFF, 0.0, 1.0);
    }

    float ao = 1.0 - (occlusion / float(AO_TAPS)) * 1.7 * SpbGradeB.z;
    return clamp(ao, 0.0, 1.0);
}

// ---- God rays (screen-space stand-in for the Veil poolrooms volumetric light) ----

#define RAY_TAPS 24

vec3 spbGodRays(vec2 uv) {
    vec2 delta = (SpbSunScreen.xy - uv) / float(RAY_TAPS);
    // Cap the march length so pixels far from the sun gather local light only.
    float deltaLen = length(delta);
    if (deltaLen > 0.03) {
        delta *= 0.03 / deltaLen;
    }

    vec3 accum = vec3(0.0);
    float illum = 1.0;
    vec2 p = uv;
    for (int i = 0; i < RAY_TAPS; i++) {
        p += delta;
        vec3 s = texture(InSampler, clamp(p, vec2(0.001), vec2(0.999))).rgb;
        float luma = dot(s, vec3(0.2126, 0.7152, 0.0722));
        accum += s * smoothstep(0.55, 0.9, luma) * illum;
        illum *= 0.93;
    }

    return accum * (SpbGradeB.w * SpbSunScreen.z / float(RAY_TAPS));
}

// ---- Procedural sky (include/sky.glsl, verbatim port; fract-wrapped sampling) ----

vec3 sunsetToNightColor(float timer, vec3 rd) {
    vec3 sunset = vec3(mix(SkyColor2, vec3(0.9921, 0.3686, 0.3254) - 0.1, clamp(rd.z + rd.y * (3.0 * timer), 0.0, 1.0)));
    vec3 night = vec3(0.0);
    return mix(night, sunset, timer);
}

vec3 noonToSunsetColor(float timer, vec3 rd) {
    vec3 sunset = vec3(mix(SkyColor2, vec3(0.9921, 0.3686, 0.3254) - 0.1, clamp(rd.z + rd.y * (3.0 * timer), 0.0, 1.0)));
    vec3 day = vec3(SkyColor - rd.y * 0.4);
    return mix(day, sunset, timer);
}

vec3 getSky(vec3 rd, float sunsetTimer) {
    float timer = sunsetTimer;

    vec3 color = vec3(0.0);

    // 0.25x the original's projected UV scale: the clamp-addressed samplers force a
    // mirror-repeat wrap (see spbMirror), and at the original scale its fold lines
    // criss-crossed the whole sky; zoomed out the pattern stays inside one tile for
    // most of the dome and the clouds read as soft masses instead of kaleidoscope.
    vec2 uv = ((rd.xz * 0.5) / rd.y + GameTime * 10.0 + spbCameraPos().xz * 0.005) * 0.25;
    vec2 uv2 = ((rd.xz * 0.7) / rd.y - GameTime * 10.0 + spbCameraPos().xz * 0.005) * 0.25;

    vec4 noise = texture(CloudNoise1Sampler, spbMirror(uv));
    vec4 noise2 = texture(CloudNoise2Sampler, spbMirror(uv2));
    vec4 clouds = rd.y > 0.0 ? vec4(noise * noise2) : vec4(0.0);
    float cloudFog = 1.0 + (1.0 / max(rd.y, 0.001));

    clouds.a = clouds.b;
    // 3.5x (was the original's 5.0): the hard threshold read as soft wisps against Veil's
    // HDR buffers but punches out harsh polygonal cutouts on the LDR vanilla target.
    clouds.a = clamp((clouds.a - 0.3) * 3.5, 0.0, 2.0);
    clouds.rgb = vec3(1.0);
    clouds.rgb *= 1.0 - clamp((clouds.a - 0.5) * 0.1, 0.0, 0.25);

    if (timer <= 0.25) {
        timer *= 4.0;
        color = noonToSunsetColor(timer, rd);
    } else if (timer <= 0.35) {
        timer = (0.35 - timer) / 0.1;
        color = sunsetToNightColor(timer, rd);
        clouds.rgb *= mix(vec3(0.2, 0.2, 0.3), vec3(1.0), timer);
    } else if (timer <= 0.65) {
        color = vec3(0.0);
        clouds.rgb *= vec3(0.2, 0.2, 0.3);
    } else if (timer <= 0.75) {
        timer = (timer - 0.65) / 0.1;
        rd.z = -rd.z;
        color = sunsetToNightColor(timer, rd);
        clouds.rgb *= mix(vec3(0.2, 0.2, 0.3), vec3(1.0), timer);
    } else {
        timer = (1.0 - timer) * 4.0;
        rd.z = -rd.z;
        color = noonToSunsetColor(timer, rd);
    }

    color = mix(color, clouds.rgb, min(clouds.a, 1.0) / max(1.0, cloudFog * 0.5));

    // Sun disc + night stars (vhs/mixed.fsh poolrooms-sky branch).
    vec3 sunDir = normalize(SpbSunDir.xyz);
    float sunDot = dot(rd, sunDir);
    color += smoothstep(0.998, 1.0, sunDot);
    if (SpbState.x < 0.35 || SpbState.x > 0.65) {
        // Tightened from the original smoothstep(0.7, 1.0) * 0.6 — that 45-degree halo was
        // balanced against HDR; on LDR + bloom it blew out half the sky into white.
        color += smoothstep(0.9, 1.0, sunDot) * 0.3;
    }
    color += texture(StarsSampler, spbMirror(rd.xz * 0.9)).rgb * clamp(sin(SpbState.x * 3.5), 0.0, 1.0);

    return clamp(color, 0.0, 1.0);
}

// ---- Per-pixel relight (deferred-lighting restoration) ----

// One flashlight cone's contribution at player-space position P with normal N.
vec3 spbSpotTerm(int i, vec3 P, vec3 N, bool useNdl) {
    vec3 L = SpbSpotPos[i].xyz - P;
    float dist = length(L);
    float range = SpbSpotColor[i].w;
    if (dist >= range) {
        return vec3(0.0);
    }
    vec3 toPixel = -L / max(dist, 1.0e-4);
    float cone = smoothstep(SpbSpotDir[i].w, SpbSpotPos[i].w, dot(toPixel, SpbSpotDir[i].xyz));
    if (cone <= 0.0) {
        return vec3(0.0);
    }
    float att = 1.0 - dist / range;
    att *= att;
    // Ease the first blocks so hugging a wall doesn't white out the frame (gentler than before
    // so the beam still reads as a usable light close in).
    att *= 0.5 + 0.5 * clamp(dist * 0.4, 0.0, 1.0);
    float ndl = 1.0;
    if (useNdl) {
        ndl = mix(max(dot(N, L / max(dist, 1.0e-4)), 0.0), 1.0, 0.35);
    }
    return SpbSpotColor[i].rgb * (cone * att * ndl);
}

// Full relight for the fixture-lit levels: the scene arrives as raw fullbright albedo and is
// multiplied by ambient floor + smooth point lights + flashlight cones (the Veil-era look).
vec3 spbRelight(vec3 viewPos, vec3 viewNormal) {
    vec3 P = mat3(SpbInvViewRotMat) * viewPos;
    vec3 N = mat3(SpbInvViewRotMat) * viewNormal;

    vec3 light = SpbAmbient.rgb;

    int pointCount = int(SpbLightMeta.x + 0.5);
    for (int i = 0; i < SPB_MAX_POINT_LIGHTS; i++) {
        if (i >= pointCount) {
            break;
        }
        vec3 L = SpbLightPos[i].xyz - P;
        float dist = length(L);
        float radius = SpbLightPos[i].w;
        if (dist >= radius) {
            continue;
        }
        // Smooth inverse-square-ish falloff windowed to the radius: bright pool near the
        // fixture, quick fade to darkness between fixtures (the moody Backrooms look).
        float x = dist / radius;
        float att = (1.0 - x) * (1.0 - x) / (1.0 + 6.0 * x * x);
        // Wrapped diffuse: a little light bleeds around corners like bounce would.
        float ndl = mix(max(dot(N, L / max(dist, 1.0e-4)), 0.0), 1.0, 0.22);
        vec3 c = SpbLightColor[i].rgb;
        light += c * (att * ndl);
        // Fixture self-glow: the tube itself must read emissive, whatever its normal.
        light += c * smoothstep(1.1, 0.0, dist) * 0.45;
    }

    int spotCount = int(SpbLightMeta.y + 0.5);
    for (int i = 0; i < SPB_MAX_SPOTLIGHTS; i++) {
        if (i >= spotCount) {
            break;
        }
        light += spbSpotTerm(i, P, N, true);
    }

    return min(light, vec3(1.5));
}

// Vanilla-lit dims (Poolrooms/Darkrooms, Infinite Field, overworld): the flashlight keeps its
// vanilla light block for base visibility, and this shapes it into a smooth cone on top.
vec3 spbConeAssist(vec3 color, vec3 viewPos, vec3 viewNormal) {
    vec3 P = mat3(SpbInvViewRotMat) * viewPos;
    vec3 N = mat3(SpbInvViewRotMat) * viewNormal;
    vec3 boost = vec3(0.0);
    int spotCount = int(SpbLightMeta.y + 0.5);
    for (int i = 0; i < SPB_MAX_SPOTLIGHTS; i++) {
        if (i >= spotCount) {
            break;
        }
        boost += spbSpotTerm(i, P, N, false);
    }
    // Multiplicative shaping keeps surface texture; the tiny additive term keeps the beam
    // visible even where the vanilla light level bottomed out at pure black.
    return color * (1.0 + boost * 1.6) + boost * 0.02;
}

// ---- Smooth-lightmap (vanilla-lit dims): melts away the per-block lightmap banding ----
// The 15-step lightmap quantization reads as wide, hard-edged blocky bands on flat walls and
// floors (worse after the exposure/S-curve grade) — this is the "blocky lighting" the poolrooms
// showed. A depth-aware bilateral smooth fixes it: three rings of taps are averaged, gated by
// VIEW-DEPTH similarity (so it never blurs across a geometry edge onto a different surface) with
// only a soft, WIDE luma window (so it DOES cross the small per-step brightness jumps that make
// the bands — the earlier 0.035 window was too tight to cross them, which is why banding
// survived). The result reads as the smooth, continuous light gradient the fixture-lit levels get
// from their per-pixel relight.
vec3 spbSmoothLightmap(vec3 color, vec2 uv, float centerViewZ) {
    float lumaC = dot(color, vec3(0.2126, 0.7152, 0.0722));
    float angle = hash12(gl_FragCoord.xy + 17.0) * 6.2831853;
    float depthTol = 0.5 + 0.05 * abs(centerViewZ); // same-surface tolerance grows with distance
    vec3 sum = color;
    float count = 1.0;
    for (int r = 0; r < 3; r++) {
        float radius = (float(r) + 1.0) * 5.0; // 5px / 10px / 15px rings
        for (int i = 0; i < 6; i++) {
            float a = angle + (float(i) + float(r) * 0.5) * 1.0471976; // 60-degree spokes, offset per ring
            vec2 offs = vec2(cos(a), sin(a)) * radius / ScreenSize;
            vec2 tuv = clamp(uv + offs, vec2(0.001), vec2(0.999));
            float td = texture(InDepthSampler, tuv).r;
            if (spbIsSky(td)) {
                continue;
            }
            float tvz = -0.05 / max(td, 1.0e-8);
            if (abs(tvz - centerViewZ) > depthTol) {
                continue; // different surface — don't blur geometry edges
            }
            vec3 s = texture(InSampler, tuv).rgb;
            float lumaS = dot(s, vec3(0.2126, 0.7152, 0.0722));
            // Soft, wide luma window: full weight for small step-to-step differences, fading out by
            // ~0.18 so real high-contrast albedo edges aren't smeared into mush.
            float w = 1.0 - smoothstep(0.0, 0.18, abs(lumaS - lumaC));
            sum += s * w;
            count += w;
        }
    }
    return sum / count;
}

// ---- Poolrooms water: REMOVED (2026-07-08) ----
// The old spbWater() painted a synthetic pool over the vanilla water in SCREEN SPACE, keyed on a
// teal-hue mask. Colour-based detection is inherently unstable under camera motion (lighting /
// reflection shifts the pixel hue, the mask flickers on/off, and the raw blocky vanilla texture
// flashes through) — this was the "water glitches while moving" bug on ALL platforms, re-patched
// four times (§21.3/§22.2/§23.2) and never fully cured. The fix moved the water look OUT of screen
// space and INTO the block atlas: a new HD 128px animated, semi-transparent, tileable water texture
// (assets/minecraft/textures/block/water_{still,flow}.png), which is camera-independent and cannot
// flicker. The biome water_color (0x3FC6A0) tints it teal; block.vsh gives the gentle geometry
// swell. See docs/superpowers/specs/2026-07-08-poolrooms-water-texture-rewrite-design.md. If the
// surface ever needs more life, bake highlights into the TEXTURE (still glitch-proof) — do NOT
// reintroduce a screen-space hue-detection pass.

// ---- Poolrooms per-pixel sky lighting ----
// The poolrooms are lit by daylight through pool_sky (glass) openings, so unlike the fixture-lit
// levels their lightmap carries real spatial variation (bright sky rooms vs the dark maze rooms) —
// forcing it fullbright would erase the Darkrooms. Instead this MODULATES the preserved vanilla
// light with a smooth per-pixel directional term (from the day-cycle sun) plus the day-cycle
// colour, giving the shaped, non-blocky "deferred" look while keeping dark rooms dark. SpbAmbient
// carries the sun/sky light colour (rgb) and w>0.5 gates the poolrooms.
vec3 spbPoolLight(vec3 color, vec3 viewNormal) {
    vec3 N = mat3(SpbInvViewRotMat) * viewNormal;
    vec3 sunDir = normalize(SpbSunDir.xyz);
    // Wrapped diffuse so faces away from the sun get soft skylight fill, not black.
    float ndl = max(dot(N, sunDir), 0.0);
    // Overhead skylight cast: the pool is lit by daylight pouring straight down through the ceiling
    // openings, so up-facing surfaces (the floor and the water plane) catch the most light. Biasing
    // by N.y pools the sky light on the floor/water the way the reference does, while walls stay
    // dimmer for the moody read. Base is a touch lower than before (0.82) so that contrast lands.
    float sky = max(N.y, 0.0);
    float shape = 0.78 + 0.28 * (ndl * 0.7 + 0.3) + 0.20 * sky;
    // Tint by the day-cycle light colour (warm at sunset, cool at night), gently.
    vec3 tinted = color * mix(vec3(1.0), SpbAmbient.rgb, 0.4);
    return tinted * shape;
}

void main() {
    vec4 color = texture(InSampler, texCoord);
    float depth = texture(InDepthSampler, texCoord).r;

    if (spbIsSky(depth)) {
        // Sky pixel.
        float skyMode = SpbFogShape.w;
        if (skyMode > 1.5) {
            color.rgb = vec3(0.0);
        } else if (skyMode > 0.5) {
            vec3 rd = spbViewToPlayerDir(spbViewDirFromUv(texCoord));
            color.rgb = getSky(rd, SpbState.x);
            // Tie the sky into the terrain's distance fog where a level has one (the infinite
            // field's 0.8-grey wall): reads as horizon haze instead of a fog/sky seam.
            if (SpbFogColor.w > 0.5) {
                color.rgb = mix(color.rgb, SpbFogColor.rgb, 0.4);
            }
        }
    } else {
        vec3 viewPos = spbViewPosFromDepth(depth, texCoord);
        float worldDepth = length(viewPos);
        // Surface normal from depth derivatives (same basis the SSAO uses); flipped to face
        // the camera. Shared by relight, flashlight cones and the water heuristic.
        vec3 viewNormal = normalize(cross(dFdx(viewPos), dFdy(viewPos)));
        viewNormal *= sign(dot(viewNormal, -viewPos));

        bool deferred = SpbLightMeta.w > 0.5;

        if (deferred) {
            // Fixture-lit levels: the lightmap was forced fullbright, so color.rgb is raw
            // albedo. Re-light it per pixel — smooth fixture pools + flashlight cones — which
            // is what removes the per-block lightmap banding entirely.
            color.rgb *= spbRelight(viewPos, viewNormal);
        } else {
            // Sky-lit dims keep vanilla block lighting; melt the blocky 15-step lightmap banding
            // first (depth-aware smooth) so both the land and (below) the water read as smooth
            // per-pixel light — the same smoothness the fixture-lit levels get from their relight.
            if (SpbLightMeta.z > 0.5) {
                color.rgb = spbSmoothLightmap(color.rgb, texCoord, viewPos.z);
            }
            // Poolrooms: per-pixel directional sky lighting (before the flashlight so its cone
            // lights the surface too). Water is now an HD animated block texture (not a
            // screen-space overlay), so nothing repaints it here — spbPoolLight just lights it.
            if (SpbAmbient.w > 0.5) {
                color.rgb = spbPoolLight(color.rgb, viewNormal);
            }
            // Shape the flashlight into a soft cone on top of its vanilla light block.
            color.rgb = spbConeAssist(color.rgb, viewPos, viewNormal);
        }

        // SSAO multiply (the Veil chain did color *= blur(SSAO); corners/contact shadows).
        if (SpbGradeB.z > 0.0) {
            color.rgb *= spbAO(viewPos, texCoord);
        }

        // Distance fog (vhs/mixed.fsh linear_fog / approximation for unlit distance).
        float fogMode = SpbFogColor.w;
        if (fogMode > 1.5) {
            float f = 1.0 - exp(-worldDepth * SpbFogShape.z);
            color.rgb = mix(color.rgb, SpbFogColor.rgb, clamp(f, 0.0, 1.0));
        } else if (fogMode > 0.5) {
            float f = clamp((worldDepth - SpbFogShape.x) / (SpbFogShape.y - SpbFogShape.x), 0.0, 1.0);
            color.rgb = mix(color.rgb, SpbFogColor.rgb, f);
        }

        // Level 1 knee-deep noise fog (everything.fsh LEVEL1_FOG raymarch).
        if (SpbState.y > 0.5) {
            vec3 ro = spbCameraPos();
            vec3 rd = spbViewToPlayerDir(normalize(viewPos));
            color.rgb = kneeFog(color.rgb, ro, rd, worldDepth);
        }
    }

    // God rays (sky pixels included — the shafts read strongest against the sky).
    if (SpbGradeB.w > 0.0 && SpbSunScreen.z > 0.0) {
        color.rgb += spbGodRays(texCoord);
    }

    // Per-level color grade, then dither the RGBA8 write to kill fog banding.
    color.rgb = spbGrade(color.rgb);
    color.rgb += (hash12(gl_FragCoord.xy) - 0.5) * (2.0 / 255.0);

    fragColor = vec4(color.rgb, 1.0);
}
