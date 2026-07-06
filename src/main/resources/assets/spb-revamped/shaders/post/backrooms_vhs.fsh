#version 330

// VHS camcorder pass — barrel distortion + chromatic aberration + bloom combine +
// YUV tape noise. Port of vhs/vhs_post.fsh + the combine half of vhs/bloom.fsh from
// the 1.20.1 Veil chain. Dropped vs original: motion blur (needed Veil motion-vector
// uniforms), jumpscare face composites (now drawn by BackroomsHudOverlay, which
// renders after this chain anyway).

#moj_import <minecraft:globals.glsl>
#moj_import <spb-revamped:spb_post.glsl>

uniform sampler2D InSampler;
uniform sampler2D BloomSampler;
uniform sampler2D VhsNoiseSampler;

in vec2 texCoord;
out vec4 fragColor;

// Parts of this code from: https://agatedragon.blog/2023/12/24/barrel-distortion-shader/
// and https://www.shadertoy.com/view/XtlSD7
vec2 BarrelDistortionCoordinates(vec2 uv) {
    vec2 pos = 2.0 * uv - 1.0;

    float len = distance(pos, vec2(0.0));
    len = (len / 1.5) * SpbState.w;

    pos = pos + pos * len * len;

    pos = 0.5 * (pos + 1.0);

    return pos;
}

void main() {
    vec2 uv = BarrelDistortionCoordinates(texCoord);

    // Chromatic aberration (vhs/bloom.fsh).
    float chromAbb = 0.003 * abs(uv.x - 0.5);
    vec4 color = texture(InSampler, uv);
    color.r = texture(InSampler, uv + vec2(chromAbb)).r;
    color.g = texture(InSampler, uv - vec2(chromAbb)).g;

    // Bloom combine (additive; strength fed from Java).
    color.rgb += texture(BloomSampler, uv).rgb * SpbSunDir.w;

    fragColor = vec4(color.rgb, 1.0);

    // VHS tape noise (vhs/vhs_post.fsh, verbatim; fract() wrap on the noise texture).
    fragColor.rgb = rgb2yuv(fragColor.rgb);
    fragColor.rgb += (fragColor.rgb * vec3((hash12(uv * 260.23535 + GameTime * 70.0) + hash12(uv * 737.36346 + GameTime * 100.0)) * 2.0 - 1.0)) * 0.05;
    fragColor.r += step(0.99994, (hash12(uv * 260.23535 + GameTime * 70.0))) * 10.0;
    vec2 vhsNoise = texture(VhsNoiseSampler, fract(vec2(uv.x - GameTime * 3000.0, uv.y + GameTime * 5000.0))).gb * 0.1;
    fragColor.gb += vec2(vhsNoise.x * 0.9, vhsNoise.y * 0.9) * 0.2;
    fragColor.rgb = yuv2rgb(fragColor.rgb);
    fragColor.a = 1.0;
}
