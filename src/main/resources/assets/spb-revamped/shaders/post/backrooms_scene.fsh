#version 330

// Backrooms scene pass — per-level fog + Level 1 knee fog + procedural sky.
// Port of the fog/sky pieces of the 1.20.1 Veil chain (vhs/everything.fsh LEVEL1_FOG,
// vhs/mixed.fsh INFINITE_FIELD/LEVEL324 fog + poolrooms sun/stars, include/sky.glsl)
// onto the vanilla 26.2 post pipeline. The Veil-only pieces (SSAO, shadows, puddles,
// reflections, material ids) are dropped — see docs/MAC_SAFE_RENDERING.md.

#moj_import <minecraft:globals.glsl>
#moj_import <spb-revamped:spb_post.glsl>

uniform sampler2D InSampler;
uniform sampler2D InDepthSampler;
uniform sampler2D FogNoiseSampler;
uniform sampler2D CloudNoise1Sampler;
uniform sampler2D CloudNoise2Sampler;
uniform sampler2D StarsSampler;

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
    // fract() wrap: post-chain texture inputs are not guaranteed REPEAT addressing.
    float n1 = texture(FogNoiseSampler, fract(z1 + GameTime * 100.0)).r;
    float n2 = texture(FogNoiseSampler, fract(z2 + GameTime * 100.0)).r;
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
            fog += 0.001 * noise;
            p = ro + rd * travDist;
            travDist += 0.1;
            if (travDist > worldDepth || fog >= 1.0 || travDist > 50.0 || p.y < 20.5) {
                break;
            }
        }
    }

    return mix(color, LEVEL1_FOG_COLOR, clamp(fog, 0.0, 1.0));
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

    vec2 uv = (rd.xz * 0.5) / rd.y + GameTime * 10.0 + spbCameraPos().xz * 0.005;
    vec2 uv2 = (rd.xz * 0.7) / rd.y - GameTime * 10.0 + spbCameraPos().xz * 0.005;

    vec4 noise = texture(CloudNoise1Sampler, fract(uv));
    vec4 noise2 = texture(CloudNoise2Sampler, fract(uv2));
    vec4 clouds = rd.y > 0.0 ? vec4(noise * noise2) : vec4(0.0);
    float cloudFog = 1.0 + (1.0 / max(rd.y, 0.001));

    clouds.a = clouds.b;
    clouds.a = clamp((clouds.a - 0.3) * 5.0, 0.0, 2.0);
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
        color += smoothstep(0.7, 1.0, sunDot) * 0.6;
    }
    color += texture(StarsSampler, fract(rd.xz * 0.9)).rgb * clamp(sin(SpbState.x * 3.5), 0.0, 1.0);

    return color;
}

void main() {
    vec4 color = texture(InSampler, texCoord);
    float depth = texture(InDepthSampler, texCoord).r;

    if (depth >= 1.0) {
        // Sky pixel.
        float skyMode = SpbFogShape.w;
        if (skyMode > 1.5) {
            color.rgb = vec3(0.0);
        } else if (skyMode > 0.5) {
            vec4 farView = SpbInvProjMat * vec4(texCoord * 2.0 - 1.0, 1.0, 1.0);
            vec3 rd = spbViewToPlayerDir(normalize(farView.xyz / farView.w));
            color.rgb = getSky(rd, SpbState.x);
        }
    } else {
        vec3 viewPos = spbViewPosFromDepth(depth, texCoord);
        float worldDepth = length(viewPos);

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

    fragColor = vec4(color.rgb, 1.0);
}
