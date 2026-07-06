#version 330

// Glitch pass — the level-2 warp / skinwalker glitch tear. Verbatim port of
// vhs/glitch.fsh from the 1.20.1 Veil chain; glitchTime now arrives via SpbDynamics
// (SpbState.z) instead of a Veil uniform poke. Passthrough when not glitching.

#moj_import <minecraft:globals.glsl>
#moj_import <spb-revamped:spb_post.glsl>

uniform sampler2D InSampler;

in vec2 texCoord;
out vec4 fragColor;

vec2 BarrelDistortionCoordinates(vec2 uv){
    vec2 pos = 2.0 * uv - 1.0;

    float len = distance(pos, vec2(0.0));
    len = len / 2.0;

    pos = pos + pos * len * len;

    pos = 0.5 * (pos + 1.0);

    return pos;
}

void main() {
    float glitchTime = SpbState.z;

    if (glitchTime > 0.0) {
        vec4 color = vec4(0.0);
        vec2 uv2 = vec2(texCoord.x + octave(texCoord.y + GameTime * 2000.0) * 0.01, texCoord.y);
        vec2 offset = uv2 + ((hash12(uv2 * 260.23535 + GameTime * 70.0)) * 0.005) + ((hash12(vec2(GameTime * 4562.0))) * 0.01);

        offset = BarrelDistortionCoordinates(offset);

        offset = mix(texCoord, offset, glitchTime);

        float pixelCount = mix(1000.0, 700.0, glitchTime);
        float ratio = 1080.0 / 1920.0;
        float scaledPixelCount = pixelCount * ratio;
        vec2 finalRatio = vec2(pixelCount, scaledPixelCount);

        vec2 uv = offset * finalRatio;
        uv = floor(uv) / finalRatio;

        float chromAbb = 0.005 * glitchTime;

        color.r = texture(InSampler, uv + vec2(chromAbb)).r;
        color.g = texture(InSampler, uv - vec2(chromAbb)).g;
        color.ba = texture(InSampler, uv).ba;

        fragColor = vec4(color.rgb, 1.0);
    } else {
        fragColor = vec4(texture(InSampler, texCoord).rgb, 1.0);
    }
}
