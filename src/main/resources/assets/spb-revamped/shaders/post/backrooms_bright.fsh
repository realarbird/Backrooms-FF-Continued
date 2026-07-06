#version 330

// Bloom bright-pass — extracts the fluorescent-light highlights that feed the blur.
// Replaces the Veil chain's vhs/highlights.fsh downsample-pyramid + hard >=1.0 threshold:
// the vanilla main target is LDR (RGBA8), so a soft luminance knee below 1.0 is used
// instead (the original sampled Veil's HDR framebuffers).

uniform sampler2D InSampler;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec3 color = texture(InSampler, texCoord).rgb;
    float luma = dot(color, vec3(0.2126, 0.7152, 0.0722));
    float gate = smoothstep(0.72, 0.95, luma);
    fragColor = vec4(color * gate, 1.0);
}
