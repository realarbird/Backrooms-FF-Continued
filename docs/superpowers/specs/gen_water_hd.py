#!/usr/bin/env python3
"""
Backrooms FF Continued — HD 128px animated water textures.

Replaces Minecraft's blocky 16x16 water_still / water_flow with a smooth, calm,
reflective pool surface. Grayscale (the biome water_color tints it teal in the
poolrooms, blue elsewhere) + semi-transparent (the tiled floor shows through).

Three "cannot glitch when moving" invariants, all baked into the texture (which
is atlas/screen-independent, unlike the deleted spbWater post-process):
  * spatial tileability  — integer wavenumbers over the 128px tile, so adjacent
                           water blocks share edges with no seam line.
  * temporal loop        — each wave advances an integer number of cycles across
                           the frame set, so the animation loops with no pop.
  * smoothness           — built from continuous sine fields, not pixel art.
"""
import numpy as np
from PIL import Image
import sys

N = 128           # px per frame (the "128x128, not 16x16" requirement)
FRAMES = 32       # stacked vertically -> PNG is 128 x (128*32)
TWO_PI = 2.0 * np.pi

# tileable 0..1 coordinate grid (endpoint excluded so x=0 and x=1 are the same phase)
_x = np.linspace(0.0, 1.0, N, endpoint=False)
X, Y = np.meshgrid(_x, _x)


def surface(waves, frame, slope, light, shininess, spec_str, body_str, base):
    """Return (lum, spec) fields in [0,1]-ish for one animation frame.

    waves: list of (kx, ky, f, amp, phase) — kx,ky,f MUST be ints (tileable+loop).
    """
    pt = TWO_PI * frame / FRAMES
    H = np.zeros((N, N))
    dHx = np.zeros((N, N))
    dHy = np.zeros((N, N))
    amp_sum = 0.0
    for kx, ky, f, amp, ph in waves:
        arg = TWO_PI * (kx * X + ky * Y) + f * pt + ph
        H += amp * np.sin(arg)
        dHx += amp * (TWO_PI * kx) * np.cos(arg)
        dHy += amp * (TWO_PI * ky) * np.cos(arg)
        amp_sum += amp
    H /= amp_sum                      # normalize height to ~[-1,1]
    dHx /= amp_sum
    dHy /= amp_sum

    # surface normal from the slope of the height field
    nx = -dHx * slope
    ny = -dHy * slope
    nz = np.ones_like(H)
    inv = 1.0 / np.sqrt(nx * nx + ny * ny + nz * nz)
    nx *= inv; ny *= inv; nz *= inv

    # Blinn specular for the reflective glints (broad = calm, soft = won't read as a dot)
    lx, ly, lz = light
    vx, vy, vz = 0.0, 0.0, 1.0
    hx, hy, hz = lx + vx, ly + vy, lz + vz
    hn = 1.0 / np.sqrt(hx * hx + hy * hy + hz * hz)
    hx *= hn; hy *= hn; hz *= hn
    ndoth = np.clip(nx * hx + ny * hy + nz * hz, 0.0, 1.0)
    spec = ndoth ** shininess

    lum = base + spec_str * spec + body_str * H
    lum = np.clip(lum, 0.0, 1.0)
    return lum, spec, H


def build(waves, slope, light, shininess, spec_str, body_str, base,
          a_base, a_spec, a_body):
    strip = np.zeros((N * FRAMES, N, 4), dtype=np.uint8)
    for fr in range(FRAMES):
        lum, spec, H = surface(waves, fr, slope, light, shininess,
                               spec_str, body_str, base)
        alpha = np.clip(a_base + a_spec * spec + a_body * H, 0.35, 0.94)
        g = (lum * 255.0 + 0.5).astype(np.uint8)
        a = (alpha * 255.0 + 0.5).astype(np.uint8)
        y0 = fr * N
        strip[y0:y0 + N, :, 0] = g
        strip[y0:y0 + N, :, 1] = g
        strip[y0:y0 + N, :, 2] = g
        strip[y0:y0 + N, :, 3] = a
    return strip


# ---- STILL: bright, lively reflective pool -----------------------------------
# Reference: SpacePotato's found-footage poolrooms video — a vivid cyan pool with a
# LIVELY rippling surface catching light (not glassy-flat), clear enough to show the
# tiled floor through it, with broad soft sky/sun glints reading as reflection. So vs.
# the old calm surface: more ripple detail + amplitude (livelier), higher slope +
# body_str (ripples visibly shade), stronger/broader glints (spec), brighter base, and
# a lower base alpha (clearer — the deck shows through like the video). Still all
# integer wavenumbers so it stays spatially tileable + temporally loop-seamless.
STILL_WAVES = [
    (1, 2, 1, 1.00, 0.00),
    (2, 1, 1, 0.85, 1.30),
    (2, 3, 1, 0.55, 0.70),
    (3, 2, 2, 0.50, 2.10),
    (3, 4, 2, 0.34, 1.05),   # extra mid ripple for a livelier surface
    (4, 3, 2, 0.28, 3.00),
    (5, 4, 3, 0.16, 0.50),   # fine glints, tiny amplitude
]
still = build(
    STILL_WAVES,
    slope=7.5, light=(0.34, -0.42, 0.84), shininess=13.0,
    spec_str=0.52, body_str=0.105, base=0.66,
    a_base=0.74, a_spec=0.22, a_body=0.06,
)

# ---- FLOW: same lively surface, gently biased to travel down +V (flow dir) ---
FLOW_WAVES = [
    (0, 2, 1, 1.00, 0.00),
    (0, 3, 2, 0.60, 1.10),
    (1, 2, 1, 0.60, 2.00),
    (1, 4, 2, 0.38, 0.40),
    (2, 3, 1, 0.30, 2.60),
    (2, 5, 3, 0.18, 1.30),   # extra fine ripple travelling down the face
]
flow = build(
    FLOW_WAVES,
    slope=8.0, light=(0.30, -0.55, 0.78), shininess=15.0,
    spec_str=0.50, body_str=0.11, base=0.58,
    a_base=0.74, a_spec=0.20, a_body=0.06,
)

# ---- OVERLAY: single static frame (faces seen through water) -----------------
ov_lum, ov_spec, ov_H = surface(STILL_WAVES, 0, slope=7.5,
                                 light=(0.34, -0.42, 0.84), shininess=13.0,
                                 spec_str=0.40, body_str=0.09, base=0.62)
ov_alpha = np.clip(0.72 + 0.12 * ov_spec, 0.45, 0.94)
overlay = np.zeros((N, N, 4), dtype=np.uint8)
og = (ov_lum * 255 + 0.5).astype(np.uint8)
overlay[..., 0] = overlay[..., 1] = overlay[..., 2] = og
overlay[..., 3] = (ov_alpha * 255 + 0.5).astype(np.uint8)

out = sys.argv[1] if len(sys.argv) > 1 else "."
Image.fromarray(still, "RGBA").save(f"{out}/water_still.png")
Image.fromarray(flow, "RGBA").save(f"{out}/water_flow.png")
Image.fromarray(overlay, "RGBA").save(f"{out}/water_overlay.png")
print(f"wrote water_still.png {N}x{N*FRAMES}, water_flow.png, water_overlay.png -> {out}")


# ---- PREVIEWS (judge look/tileability/tint before shipping) ------------------
def tint(gray_rgba, rgb):
    """Simulate the biome water_color multiply on RGBA grayscale."""
    arr = gray_rgba.astype(np.float32)
    r, g, b = rgb
    arr[..., 0] *= r / 255.0
    arr[..., 1] *= g / 255.0
    arr[..., 2] *= b / 255.0
    return arr.astype(np.uint8)


def over_floor(water_rgba, floor_rgb):
    """Alpha-composite the (teal-tinted) water over a tiled-floor mock."""
    a = water_rgba[..., 3:4].astype(np.float32) / 255.0
    top = water_rgba[..., :3].astype(np.float32)
    comp = top * a + floor_rgb.astype(np.float32) * (1 - a)
    return np.clip(comp, 0, 255).astype(np.uint8)


TEAL = (16, 222, 214)  # poolrooms biome water_color 0x10DED6 (vivid cyan-turquoise, matches the video)
frame8 = still[8 * N:9 * N]            # a representative frame
tiled = np.tile(frame8, (3, 3, 1))     # 3x3 to reveal any seam line
tinted = tint(tiled, TEAL)

# tan tile floor with grout lines, to check see-through + calm look
floor = np.full((N * 3, N * 3, 3), (196, 176, 138), np.float32)
for i in range(0, N * 3, 32):
    floor[i:i + 2, :] = (150, 132, 100)
    floor[:, i:i + 2] = (150, 132, 100)
comp = over_floor(tint(tiled, TEAL), floor)

Image.fromarray(tint(np.tile(frame8, (1, 1, 1)), TEAL), "RGBA").resize(
    (256, 256), Image.NEAREST).save(f"{out}/preview_frame_tinted.png")
Image.fromarray(tinted, "RGBA").save(f"{out}/preview_tiled_seam.png")
Image.fromarray(comp, "RGB").save(f"{out}/preview_over_floor.png")

# filmstrip: 8 frames across, tinted, to eyeball the motion cadence
sel = [still[i * N:(i + 1) * N] for i in range(0, FRAMES, FRAMES // 8)]
strip = np.concatenate([tint(f, TEAL) for f in sel], axis=1)
Image.fromarray(strip, "RGBA").save(f"{out}/preview_filmstrip.png")
print("wrote previews: preview_frame_tinted / preview_tiled_seam / preview_over_floor / preview_filmstrip")
