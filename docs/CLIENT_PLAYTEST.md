# Client Playtest Checklist (macOS)

The 1.20.1 -> 26.2 port is feature-complete and the **dedicated server boots clean** (all 8
dimensions load, no mixin/registry/datapack errors). The remaining verification is **client-only**
visuals and interactions that a headless server cannot exercise. This doc is your macOS
`runClient` checklist.

## How to launch

```sh
cd /Users/ayanraj/Documents/MinecraftFoundFootage-master
JAVA_HOME=/opt/homebrew/opt/openjdk@25/libexec/openjdk.jdk/Contents/Home \
  ./gradlew runClient
```

Then: **Singleplayer -> Create New World**. Pick a Backrooms dimension entry / use the mod's
entry item to descend (the mod teleports you into `spb-revamped:level0` and friends). Java 25 is
required (the toolchain above). First launch downloads assets and may take a minute.

If `runClient` complains the port/instance is busy, close any stray server:
`lsof -ti :25565 | xargs kill -9`.

---

## What to verify (each is client-rendered, so untested by the server boot)

1. **VHS / camcorder HUD overlay** — REC dot, timestamp, battery, scanlines + vignette overlay are
   drawn over the screen while in the backrooms. Confirm the camcorder *feel* is present (see the
   "expected deltas" below — it is an overlay approximation, not the original GLSL post chain).
2. **Entity rendering (GeckoLib 5)** — spawn / encounter the **Walker**, **Smiler**, and
   **SkinWalker**. Confirm models load, animate, and are positioned/oriented correctly (these were
   migrated GeckoLib 4 -> 5 render-state renderers). Name tags should be hidden.
3. **Flashlight + fixture dynamic light** — toggle the flashlight; confirm a moving light follows
   your look direction. Confirm ceiling/fluorescent/emergency light blocks glow and illuminate.
   (Vanilla `Blocks.LIGHT` + emissive textures — see deltas.)
4. **Simple Voice Chat proximity audio** — with a second client (or a friend) connected, confirm
   proximity voice works. The SVC server itself starts on the dedicated server (observed at port
   24454 during boot), so this is the client mic/audio path only.
5. **Level traversal / dimension transitions** — walk between backrooms levels; trigger a level
   transition. Confirm the black-screen transition beat plays and you arrive in the next dimension
   (`level0/level1/level2/level324/infinite_field/poolrooms` all loaded server-side).
6. **Cutscene + camera shake** — trigger an event that drives a cutscene / screen shake (e.g. a
   SkinWalker capture). Confirm the camera-shake (Perlin) and cutscene camera path play, and the
   jumpscare overlay (creepy-face) appears on the SkinWalker jumpscare timeline.
7. **Stamina bar / title text HUD** — confirm the stamina bar and any title/subtitle text render.
8. **Poolrooms day-cycle sky tint** — in the poolrooms, confirm the skylight color shifts over the
   day cycle.

---

## Expected Mac-safe deltas (these are intentional, NOT bugs)

This port removed **Veil** (no 26.2 build; its GLSL deferred pipeline cannot run on macOS) and
rebuilt every effect with no custom GLSL, or dropped it. Full rationale:
`docs/MAC_SAFE_RENDERING.md` and `docs/MIXIN_TRIAGE.md`. Expect the following differences from the
original 1.20.1 mod:

- **VHS post-FX is an overlay, not a shader.** No true per-pixel bloom, lens distortion, or
  full-screen chromatic warp; scanlines/vignette/grain are approximated with translucent HUD fills
  and a tiled noise blit. The 4:3 / camcorder resolution list is not applied.
- **No Smiler opacity fade-out.** The Smiler renders at full opacity (the `@ModifyConstant` opacity
  fade mixin was dropped).
- **No flattened dropped items.** Ground items render normally (the item-entity flatten mixin was
  dropped).
- **No respawn warp shader.** Respawn uses a plain fade / no special shader warp.
- **Dynamic light is a point light, not a cone/spot.** The flashlight is a moving `Blocks.LIGHT`
  point following your look vector with flicker — no soft penumbra, no true 25-block spotlight
  throw, likely self-only (not other players' flashlights). Fixture lights are flat emissive +
  static light level, no real-time deferred bounce.
- **No PBR / custom block render layers.** Blocks use vanilla cutout/translucent layers; no
  normal/specular maps.
- **No dynamic cast shadows / SSAO.** Vanilla ambient occlusion + lightmap only. The Veil AO-fix
  mixin is gone (revisit only if a visible AO seam appears in 26.2 vanilla).
- **No GPU-instanced grass/birds.** If present, grass is far fewer vanilla cross-quad blades with no
  GPU wind animation (and may be off by default via `GrassQuality`); birds are CPU-batched quads at
  a lower max flock count.
- **No animated custom sky / water reflections / light cones / puddles / player-warp / entity
  distortion.** All were custom GLSL world programs and are dropped.
- **No IK debug renderers** (debug-only, not player-facing) and **no F3 debug toggles** tied to the
  old immediate-mode renderer.

If a delta above looks broken rather than intentionally simplified, capture the screen + the
`runClient` console and report it. Anything that the dedicated server already exercises (registry
load, dimension load, entity registration, networking payload registration, mod load) is already
verified and should not need re-checking.
