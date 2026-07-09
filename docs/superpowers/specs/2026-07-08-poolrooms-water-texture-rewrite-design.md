# Poolrooms water — texture-based rewrite (kill the post-process overlay)

**Date:** 2026-07-08
**Branch:** `port/mc-26.2`
**Supersedes the approach in:** HANDOFF §19–§23 (`spbWater` post-process water)

## Problem

Poolrooms water "looks bad and glitches weirdly when moving," on **all platforms** (Mac and
Windows), and still shows Minecraft's blocky **16×16** water texture.

Root cause (from the handoff history + code): the water look was produced by a **screen-space
post-process shader** `spbWater` in `backrooms_scene.fsh`. It detects water by pixel color (the
teal biome `water_color` `0x3FC6A0`) and repaints those pixels. Color-based detection is inherently
unstable under camera motion — lighting/reflection shifts the pixel hue, the mask flickers on/off
frame-to-frame, and the raw vanilla 16×16 water flashes through. This has been re-patched four times
(§21.3, §22.2, §23.2) and still glitches. The underlying block texture was never replaced, so
detection dropouts always fall back to blocky vanilla water.

## Goal

Replace Minecraft's default water **texture** with a new **128×128 animated, smooth** texture that
undulates with waves and **cannot glitch when moving**. Delete the post-process overlay entirely.

## Approach (approved)

Move the "water look" out of screen space and into the **block atlas texture**, which is
camera-independent and therefore cannot flicker under motion. This mirrors the proven §21.5 pattern
that replaced grass with HD 128px textures (`assets/minecraft/textures/block/*` stitched by
`atlases/blocks.json`).

Look target (user choice): **calm reflective pool** — smooth glassy surface, soft broad ripples,
gentle baked highlights, semi-transparent so the tiled floor shows through, matching the deep-teal
poolrooms reference.

## Changes

### 1. New HD water textures (`assets/minecraft/textures/block/`)
- `water_still.png` + `water_still.png.mcmeta` — **128×128 per frame**, 32 frames stacked
  vertically (PNG is 128 × 4096), `frametime` tuned for a slow calm drift (~2–3 s loop).
- `water_flow.png` + `water_flow.png.mcmeta` — 128px HD flow for the pool rim / side faces, so no
  16px blockiness leaks in at edges. Ripples scroll along +V (flow direction).
- `water_overlay.png` — 128×128 static, for block faces seen through water.

Texture properties:
- **Grayscale + semi-transparent (alpha ≈ 0.7 with ripple-linked variation).** Grayscale is
  deliberate — the biome `water_color` tints it teal in the poolrooms and normal blue elsewhere, so
  one texture is correct in every dimension. Semi-transparent so the floor shows through.
- **Smooth**: generated from continuous ripple functions (summed sines / value-noise), not pixel
  art — no hard edges even at 128px.
- **Spatially tileable**: ripple wavenumbers are integers over the 128px tile, so adjacent water
  blocks (each showing the full sprite) share edges with no visible grid seam.
- **Temporally seamless**: animation phase advances a whole number of cycles across the frame set,
  so the loop has no pop.

Generator: a procedural Python script (PIL/numpy) checked into `scratchpad/` (like §21.5's
`gen_grass_hd.py`); the committed deliverable is the PNGs + mcmeta.

### 2. Waves — two glitch-proof mechanisms only
- **Animated ripples baked into the texture** (primary): screen-stable, cannot glitch.
- **Gentle geometric swell** already in `assets/minecraft/shaders/core/block.vsh` `#else` branch
  (±0.09 block, world-XZ phase → seamless across blocks): **kept**. It gives real 3D wave motion and
  was never the glitch source (§22.1 attributed the glitch to the post-process, not this). May be
  re-tuned (amplitude/period) for the calm look.

### 3. Delete the post-process overlay (`backrooms_scene.fsh`)
- Remove the `spbWater(...)` call (currently line 530).
- Remove the `spbWater` function definition + its header comment (≈ lines 381–461).
- **Keep** `spbPoolLight` (smooth directional sky-lighting on floor+water — a lighting multiply, not
  a detection mask) and `spbSmoothLightmap`. Keep the biome `water_color` teal tint (unchanged).

## Non-goals / out of scope
- No live screen-space reflections. If the flat look needs more life, add baked highlights **into
  the texture** (still glitch-proof) rather than reintroducing a screen-space pass.
- No worldgen / biome / block changes. Water blocks and biome colors are unchanged.
- Other dimensions' water (rare) inherits the new texture automatically via the vanilla override —
  acceptable and desirable (smooth water everywhere).

## Verification
- `./gradlew build` succeeds (`JAVA_HOME` = JDK 25 per §4).
- Preview the generated PNG frames directly (composited with the teal tint) to confirm the calm
  reflective look before building.
- Drive the client into the poolrooms (§18.4 harness: dedicated server + `runClient` quickplay +
  RCON tour + screenshot flags; advance to daytime via `backroomsevent sunset`) and confirm under
  **camera motion**: no flicker, no blocky fallback, smooth animated teal water.
- Applies to existing saves immediately (texture + render-side only; no worldgen regen needed).

## Risk / rollback
- Single, self-contained change set; revert = restore `spbWater` + delete the four texture files.
- If water reads too flat without reflections, tune baked highlights / alpha in the generator and
  the `block.vsh` swell — no shader-detection code returns.
