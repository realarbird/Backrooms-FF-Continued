# Mac-Safe Rendering — Phase 6 plan (Veil removal → no-GLSL visual rebuild)

Authoritative plan for **Phase 6** of the 1.20.1→26.2 port. Veil (`foundry.veil.*`) has **no 26.2
build** and is removed; macOS cannot run Veil's GLSL deferred pipeline. Every render effect is rebuilt
with **no custom GLSL**: HUD overlays (`HudElementRegistry` + `GuiGraphicsExtractor`), vanilla
`Blocks.LIGHT` for dynamic light, emissive textures, vanilla post — or **DROPPED** and documented here.

Fidelity target (per spec §7): the camcorder / Found-Footage **feel** is preserved; exact shader
output is **not** reproduced. Deltas from the original are stated per-effect.

Cross-reference: `docs/MIXIN_TRIAGE.md` (33 VEIL/RENDER mixins), `docs/PORTING_MAP.md`, spec §7.

---

## How the 26.2 render API forces the rebuild

- **No `MultiBufferSource`/`VertexConsumerProvider` in render methods** — 26.2 is a deferred `submit()`
  model. Entity/HUD/world render signatures that took a buffer provider are gone.
- **HUD** moved off `InGameHud.render*` (none of `renderHotbar/renderCrosshair/renderVignetteOverlay/…`
  survive on `Gui`) to `HudElementRegistry.addLast/addFirst(Identifier, HudElement)` where
  `HudElement.extractRenderState(GuiGraphicsExtractor, DeltaTracker)` draws via `fill/text/blit/blitSprite`.
- **`GameRenderer.renderWorld/bobView/bobHurt/renderHand/getFov`** and **`LevelRenderer.render/renderSky/
  renderClouds/renderWeather/tickRainSplashing/processWorldEvent`** are all gone — only
  `GameRenderer.renderLevel(DeltaTracker)` survives. Every world-render injection must be re-derived.
- **`client.gl.VertexBuffer` / immediate-mode `BufferBuilder.begin/next` / raw `GL43` compute** — the
  basis of the indirect-draw grass & birds and the PBR vertex pipeline — are gone/Mac-unsafe.
- **`ShaderProgram` (Veil & vanilla `core` shaders)** — the uniform-poke API (`setInt/setFloat/setVector`)
  is gone; no custom GLSL programs.
- **Entity rendering** is render-state-based: GeckoLib 5 `GeoEntityRenderer<T,R extends EntityRenderState>`.

---

## Effect table

Legend — **Replacement** column: `HUD` = HUD overlay via `GuiGraphicsExtractor`; `LIGHT` = vanilla
`Blocks.LIGHT`; `EMISSIVE` = emissive/glow texture or `RenderTypes.entityTranslucentEmissive`;
`VANILLA` = vanilla render/post feature; `GECKO5` = GeckoLib 5 render-state; `DROP` = removed (delta noted).

### Group A — KEEP-REBUILD (effect preserved or approximated)

| # | Effect | What it did (Veil/GLSL) | Mac-safe replacement | Source files / mixins touched | Notes / delta |
|---|---|---|---|---|---|
| A1 | **VHS / camcorder post-FX + HUD** | Full-screen GLSL post chain: scanlines, grain, chromatic aberration, vignette, bloom, REC dot, timestamp, battery; `everything/vhs_post/glitch/bloom/highlights/mixed` programs + `post/vhs.json` `post/glitch.json` framebuffers | **HUD** overlay element (`HudElementRegistry.addLast`): translucent `fill` scanlines + vignette, tiled `blit` of `vhs_noise.png`, `text` for REC/timestamp/battery, color-fringe approximated by 2–3 offset translucent fills. Bloom → drop or cheap additive blit. | `SkinwalkerJumpscare.java` (uniform timeline → HUD state); `InGameHudMixin` (DROP, rebuilt as HudElement); assets `pinwheel/shaders/program/vhs/**`, `pinwheel/post/vhs.json`, `pinwheel/post/glitch.json` (deleted) | Keeps the camcorder *feel*. Delta: no true per-pixel bloom / lens distortion / full-screen chromatic warp; approximated with overlays. `textures/shaders/vhs_noise.png` reused. |
| A2 | **Skinwalker jumpscare** | GLSL uniforms (`Jumpscare/CreepyFace1/CreepyFace2/Rand`) drove a full-screen creepy-face overlay on a timed 0→2s→12s→14s sequence | **HUD** full-screen `blit` of `creepy_face1/2.png` + `you_cant_escape.png`, driven by the existing timeline + sound + `hudHidden` + packet-sync logic (all non-Veil, salvageable) | `SkinwalkerJumpscare.java` (KEEP timeline, swap `program.setInt/setVector`→HUD flags) | Timeline/sound/sync logic kept verbatim. Only the uniform pokes become HUD-overlay booleans. Reuses `creepy_face1.png`, `creepy_face2.png`, `you_cant_escape.png`. |
| A3 | **Flashlight (dynamic player light)** | Veil deferred `AreaLight` (spot) per player, slerp-oriented along look vector, brightness/distance/angle | **LIGHT**: project vanilla `Blocks.LIGHT` (level 15) along the camera look-vector a few blocks out, with flicker; soft-detect LambDynamicLights if installed and defer to it | `FlashlightRenderer.java` (full rewrite — Veil-only body, per-player tracking concept reused as reference); `PlayerComponent.isFlashLightOn()` stays | Delta: a moving point light, not a true cone/spot; no soft penumbra or 25-block throw. Per-player flashlight for *other* players likely drops to self-only. |
| A4 | **Level / fixture lights (fluorescent, ceiling, emergency, tiny/thin)** | Veil deferred point/area lights spawned per light block-entity + `light/fluorescent_light` GLSL | **LIGHT** + **EMISSIVE**: emissive light-block textures for the glow look; optional `Blocks.LIGHT` companion blocks for actual illumination at placement | Light block-entities `CeilingLightBlockEntity`, `EmergencyLightBlockEntity`, `FluorescentLightBlockEntity`, `ThinFluorescentLightBlockEntity`, `TinyFluorescentLightBlockEntity`; BERs `ThinFluorescentLightBlockEntityRenderer`, `TinyFluorescentLightBlockEntityRenderer`; assets `pinwheel/shaders/program/light/**` (deleted) | Delta: no real-time deferred bounce; flat emissive + static light level. Brightness/flicker animation can stay if it only toggles light level / texture frame. |
| A5 | **Poolrooms day-cycle sky tint** | `PoolroomsDayCycle` (Veil `Easings`) + `lightmap` redirect tinted skylight color over a day cycle | **VANILLA**: keep the day-cycle math; redirect `LightTexture` (was `LightmapTextureManager`) skylight color; swap Veil `Easings`→`com.sp.util.Easings` (already ported) | `PoolroomsDayCycle.java` (KEEP, swap one import); `lightmap/PoolroomsSkylightColor` mixin (rebuild against `LightTexture.update`) | Delta: minimal — color-lerp logic is vanilla-compatible; only the Veil easing import and the lightmap-redirect anchor change. |
| A6 | **Custom block render layers (PBR/atlas)** | `RenderLayers` (Veil `VeilRenderBridge`) + `PbrRegistry`/`BlockIdMap` mapped blocks to custom GLSL render layers via custom atlas | **VANILLA** `RenderType`/`RenderTypes` (`net.minecraft.client.renderer.rendertype.*`) + Fabric `BlockRenderLayerMap`; keep the block→layer mapping, drop the Veil bridge & GLSL layer | `RenderLayers.java` (rebuild — drop `VeilRenderBridge`), `PbrRegistry.java` (KEEP — already pure Fabric), `BlockIdMap.java` (KEEP — pure registry map), `customatlas/BakedModelManagerMixin`, `renderlayer/RenderLayerMixin`, `renderlayer/WorldRendererMixin` | Delta: blocks render with vanilla cutout/translucent layers, no PBR normal/specular. The custom atlas can stay for sprite packing; the PBR sampler layer is dropped (see B7). |
| A7 | **GeckoLib entity rendering (Walker, Smiler, Skinwalker)** | GeckoLib 4 renderers/models in immediate `VertexConsumerProvider` mode | **GECKO5**: `GeoEntityRenderer<T, R extends EntityRenderState>` (pkg `com.geckolib.renderer`), render-state extraction; ctor `(EntityRendererProvider$Context, GeoModel<T>)` | `entity/client/renderer/{WalkerRenderer,SmilerRenderer,SkinWalkerRenderer}.java`, `entity/client/model/{WalkerModel,SmilerModel,SkinWalkerModel}.java`, `entity/client/SkinWalkerCapturedFlavorText.java` (none import Veil) | Delta: none intended — straight GeckoLib 4→5 + render-state migration. Smiler opacity fade (was `@ModifyConstant` in `LivingEntityRendererMixin`) re-done via render-state alpha. |
| A8 | **GeckoLib↔IK bridge (Mowzie bones)** | `MowzieGeoBone`/`MowzieModelFactory`/`GeoModelAccessor` adapt GeckoLib bones into the IK system | Rebuild against GeckoLib 5 bone/model API (no Veil) | `entity/ik/model/GeckoLib/{MowzieGeoBone,MowzieModelFactory,GeoModelAccessor}.java` | Delta: API-shape only (GeckoLib 5 `GeoBone`/`GeoModel`); IK math (`entity/ik/**` parts/components) is engine-agnostic and already ported. |
| A9 | **Entity name-tag hiding** | `EntityRendererMixin` cancelled `renderLabelIfPresent` (took `VertexConsumerProvider`) | Re-done in the render-state pipeline (label is part of `EntityRenderState`); gate label submission | `EntityRendererMixin` (rebuild — method gone) | Delta: same outcome (hidden name tags), new hook. |
| A10 | **Hide first-person hand / held item** | `DontRenderHandMixin` redirected `renderArmHoldingItem(…VertexConsumerProvider…)` | Re-target against the deferred hand-render path / `hudHidden`-style gate | `hudandresolution/DontRenderHandMixin` (rebuild) | Delta: same outcome. |
| A11 | **Flattened dropped items** | `ItemEntityRendererMixin` modified `render(…VertexConsumerProvider…)` to flatten ground items | Re-target against `ItemEntityRenderer` render-state | `ItemEntityRendererMixin` (rebuild — sig gone) | Delta: same outcome; cosmetic. |
| A12 | **Player model pose tweak** | `PlayerEntityRendererMixin` `setModelPose` + `PlayerEntityModel` local | Re-target `PlayerRenderer` (was `PlayerEntityRenderer`) render-state | `PlayerEntityRendererMixin` (rebuild) | Delta: same outcome. |
| A13 | **Respawn fade-in** | `respawnsystem/LivingEntityRendererMixin` used Veil `VeilRenderSystem`/`ShaderProgram`/`Easings` to fade the player on respawn | Vanilla alpha via render-state + `com.sp.util.Easings`; or a brief HUD black-fade | `respawnsystem/LivingEntityRendererMixin` (rebuild — drop Veil), `respawnsystem/DeathScreenMixin` (button rename, NEW-INJECTION) | Delta: a fade approximation, not the exact shader fade. |
| A14 | **Stamina bar / title text HUD** | Fabric `HudRenderCallback` + `DrawContext` (no Veil) | **HUD** via `HudElementRegistry`/`GuiGraphicsExtractor` (`HudRenderCallback`+`DrawContext` reworked) | `render/gui/StaminaBar.java`, `render/gui/TitleText.java` (no Veil; rebuild HUD API only) | Delta: none intended; pure HUD-API migration. |
| A15 | **Camera shake / roll / black-screen** | `CameraShake`/`CameraRoll` (Perlin noise, no Veil) applied to camera; black-screen via HUD | Keep math; apply via `CameraMixin` (`update(DeltaTracker)`), black-screen via HUD `fill` | `render/camera/CameraShake.java`, `render/camera/CameraRoll.java` (no Veil — KEEP), `CameraMixin` (NEW-INJECTION) | Delta: none — these are vanilla-math; only the camera-update injection signature changes. |
| A16 | **Cutscene path animation** | `CutsceneManager` used Veil `Path`/`Frame`/`Keyframe`/`Easings` + `PathAccessor` mixin for camera keyframe paths | Replace Veil anim `Path` with a small in-repo keyframe/spline class (Catmull-Rom) + `com.sp.util.Easings`; drop `PathAccessor` | `render/camera/CutsceneManager.java` (rebuild path layer; gameplay/packet logic kept), `cutscene/PathAccessor` (DROP), `cutscene/MinecraftClientMixin` & `cutscene/ClientPlayer*` (NEW-INJECTION) | Delta: re-implements the keyframe path container ourselves; cutscene timing/triggers unchanged. |
| A17 | **Birds (flock)** | `BirdRenderer` indirect-draw via Veil framebuffers/compute (`bird.fsh/vsh`, `compute/positions`) + `FlockManager` (boids, no Veil) | Render birds as ordinary small quads/entities via vanilla `RenderType`; keep `FlockManager` boids math on CPU | `render/bird/FlockManager.java` (KEEP — boids, no Veil), `render/bird/BirdQuality.java` (KEEP — enum), `render/bird/BirdRenderer.java` (rebuild — Veil+GL+compute), assets `pinwheel/shaders/program/bird/**` (deleted) | Delta: CPU-batched quads instead of GPU compute instancing; lower max flock count (`BirdQuality` already gates this). |
| A18 | **Grass (instanced)** | `GrassRenderer` indirect-draw tessellation via Veil framebuffers/compute (`grass.fsh/vsh`, `compute/positions`) + raw GL43 + `VertexBufferMixin`/`WindowMixin` GLFW hint | Vanilla cross-quad grass via `RenderType` (cutout), CPU-placed; or **DROP** grass if perf-prohibitive | `render/grass/GrassRenderer.java` (rebuild or drop), `render/grass/GrassQuality.java` (KEEP — enum), `grass/VertexBufferMixin` (DROP — Veil+GL), `grass/WindowMixin` (likely DROP — tessellation GLFW hint), assets `pinwheel/shaders/program/grass/**` (deleted) | Delta: far fewer blades, no GPU wind animation; `GrassQuality.OFF` is the fallback. Decide rebuild-vs-drop at 6d/6f based on perf. |

### Group B — DROP (fundamentally GLSL/deferred; removed, delta noted)

| # | Effect | What it did | Why dropped | Source files / mixins | Delta from original |
|---|---|---|---|---|---|
| B1 | **Deferred shadow-map renderer** | `ShadowMapRenderer` rendered the scene to a depth FBO from a light POV (Veil `AdvancedFbo` + GL11/GL30 + `WorldRendererAccessor`) for real-time shadows; `shadowmap/*` GLSL | Requires a second scene pass + depth FBO + GLSL sampling — no Mac-safe equivalent | `render/ShadowMapRenderer.java` (DELETE), `lightshadows/LightRendererMixin` (DROP), assets `pinwheel/shaders/program/shadowmap/**`, `pinwheel/framebuffers/shadowmap.json` (deleted) | No dynamic cast shadows; rely on vanilla AO/lightmap only. |
| B2 | **Entity-glow / dynamic shadows toggle** | `EntityRenderDispatcherMixin` hooked `setRenderShadows`; deferred glow | Method gone; tied to deferred pipeline | `EntityRenderDispatcherMixin` (DROP/rebuild-if-trivial) | Vanilla entity shadow only; emissive textures (A-group) instead of deferred glow. |
| B3 | **Veil deferred point/area light renderers** | Mixins into Veil's own light renderers (`AreaLight`, `Indirect/InstancedPointLightRenderer`, `LightRenderer`, etc.) | Veil classes don't exist in 26.2 | `arealightfix/AreaLightMixin`, `arealightfix/AreaLightRendererMixin`, `lightshadows/{IndirectLightRendererMixin,IndirectPointLightRendererMixin,InstancedLightRendererMixin,InstancedPointLightRendererMixin,LightRendererMixin,VeilShadersMixin}` (all DROP) | Lighting handled by A3/A4 (vanilla `Blocks.LIGHT` + emissive). |
| B4 | **Veil shader/uniform plumbing** | `MutableUniformAccessMixin`, `VanillaLightRenderer`, `PreviousUniforms` (motion-vector uniforms for TAA/post) | Veil-only API; no GLSL = no uniforms | `mixin/MutableUniformAccessMixin`, `mixin/VanillaLightRenderer`, `render/PreviousUniforms.java` (all DELETE) | No motion vectors / TAA; not user-visible without the post chain. |
| B5 | **SSAO** | `SSAOSamples` (hemisphere kernel) feeding `vhs/ssao.fsh` ambient occlusion | SSAO needs depth-buffer GLSL | `render/SSAOSamples.java` (DELETE — pure SSAO kernel, no other use), assets `pinwheel/shaders/program/vhs/ssao.fsh`+`ssao.json`, `pinwheel/framebuffers/ssao.json` (deleted) | No screen-space AO; vanilla AO only. (Confirm no non-SSAO caller before deleting the .java.) |
| B6 | **PBR vertex pipeline** | Custom `VertexFormats` (extra normal/material elements) + `BufferBuilderMixin`/`ChunkBuilderMixin`/`RebuildTaskMixin` tagged PBR block ids during chunk build; `pbr/pbr.*` GLSL (incl. `.gsh` geometry) | Immediate-mode `BufferBuilder.begin/next` + chunk-build pipeline gone; geometry shaders Mac-hostile | `render/VertexFormats.java` (DELETE — PBR-only format), `pbr/BufferBuilderMixin`, `pbr/ChunkBuilderMixin`, `pbr/RebuildTaskMixin` (all DROP), assets `pinwheel/shaders/program/pbr/**` (deleted) | No PBR normals/specular; blocks use vanilla layers (A6). `BlockIdMap`/`PbrRegistry` survive only for the block→render-layer mapping. |
| B7 | **Custom GLSL world programs** | `distorted_entity`, `warp_player`, `warp_solid`, `sky`, `water`, `light_cone`, `puddles`, `highlight_blur`, `area`, `point`, `mix`, `minecraft_core` overrides + shader_modifiers | All custom GLSL; the deferred-light `point`/`area` programs and `minecraft_core` core-shader overrides have no Mac-safe path | assets `pinwheel/shaders/program/{distorted_entity,warp_player,warp_solid,sky,vhs/water,vhs/light_cone}.*`, `spbrevamped/pinwheel/shaders/program/{area,point,mix,minecraft_core}/**`, `pinwheel/shader_modifiers/**`, `pinwheel/framebuffers/**` (deleted) | Player-warp / entity-distortion / animated sky / water reflections / light cones / puddles all dropped. Distortion-style horror beats, if needed, become HUD overlays. |
| B8 | **AO fix** | `AOFixMixin` patched Indigo `AmbientOcclusionCalculator` (imports Veil) | AO baking reworked + Veil import | `mixin/AOFixMixin` (DROP unless the AO bug reproduces in 26.2 vanilla) | Vanilla AO; revisit only if a visible AO seam appears. |
| B9 | **No-rain-particles / weather render tweaks** | `NoRainParticlesWorldRendererMixin` wrapped `tickRainSplashing`/`renderWeather` `BufferBuilder.vertex` | Target methods gone from `LevelRenderer` | `rain/NoRainParticlesWorldRendererMixin` (DROP/rebuild against new weather render) | Re-evaluate against the always-rain feature in Phase 6f. |
| B10 | **Immediate-mode world-render hooks** | `WorldRendererMixin` (root + renderlayer), `WorldRendererAccessor` — wireframe/warp/PBR-id/sky/cloud hooks on the old renderer | `render/renderSky/renderClouds/renderLayer/processWorldEvent` + `bufferBuilders`/`frustum` accessors gone | `mixin/WorldRendererMixin`, `mixin/WorldRendererAccessor`, `renderlayer/WorldRendererMixin` (DROP/rebuild as needed) | Most hooks served B-group GLSL effects (now dropped); only the block-layer registration (A6) needs a new home. |
| B11 | **IK debug renderers** | `IKDebugRenderer` + chain/leg debug draws, `IKDebugRenderLayer`, `DinoDebugRendererMixin` toggle (F3-key) | Debug-only; `Keyboard.processF3` hook gone; immediate-mode debug draw | `entity/client/debug/IKDebugRenderLayer.java`, `entity/ik/components/debug_renderers/{IKDebugRenderer,IKChainDebugRenderer,LegDebugRenderer,WalkerLegDebugRenderer}.java`, `mixin/DinoDebugRendererMixin` | Debug visualization dropped (not a player-facing feature). Rebuild later only if needed for IK tuning. Not deleted now (no Veil; harmless until 6b). |

---

## What gets deleted in this sub-phase (6a) vs rebuilt later

**Deleted now (100% Veil / GLSL, zero salvageable non-Veil logic):**

- Render source files: `render/PreviousUniforms.java` (B4).
- Veil-only mixins (already DROP in triage; no target in 26.2): `MutableUniformAccessMixin`,
  `VanillaLightRenderer`, `arealightfix/AreaLightMixin`, `arealightfix/AreaLightRendererMixin`,
  `lightshadows/{IndirectLightRendererMixin, IndirectPointLightRendererMixin, InstancedLightRendererMixin,
  InstancedPointLightRendererMixin, LightRendererMixin, VeilShadersMixin}`, `cutscene/PathAccessor`.
- All GLSL shader assets: both `pinwheel/` trees (`assets/spb-revamped/pinwheel/**` and
  `assets/spbrevamped/pinwheel/**`) — `.glsl/.fsh/.vsh/.gsh/.comp`, their `.json` program/framebuffer/post
  defs, and `shader_modifiers/`.

**KEPT (deleted/rebuilt in a later sub-phase — has salvageable logic or non-Veil code):**
`FlashlightRenderer` (A3), `SkinwalkerJumpscare` (A2), `PoolroomsDayCycle` (A5), `RenderLayers` (A6),
`ShadowMapRenderer` (B1 — deleted at 6b, kept now as the only reference for the shadow feature),
`SSAOSamples`/`VertexFormats` (B5/B6 — deleted at 6b alongside their mixins), `BlockIdMap`/`PbrRegistry`
(A6), `BirdRenderer`/`FlockManager`/`BirdQuality` (A17), `GrassRenderer`/`GrassQuality` (A18),
`StaminaBar`/`TitleText` (A14), `CameraShake`/`CameraRoll`/`CutsceneManager` (A15/A16), all
`entity/client/**` + `entity/ik/model/GeckoLib/**` (A7/A8), IK debug renderers (B11).

**KEPT (not shader sources, candidate reuse for the HUD rebuild):**
`assets/spb-revamped/textures/shaders/*.png` — plain PNGs. `vhs_noise.png`, `creepy_face1.png`,
`creepy_face2.png`, `you_cant_escape.png`, `stars.png` are reused by the VHS HUD (A1) and jumpscare (A2).
The rest (`puddle_*`, `water_*`, `caustics3`, `clouds*`, `rnoise*`, `super_noise`, `contrast_noise`,
`dither`, `utility_pole_color`, `pool_tiles`) were GLSL sampler inputs — prune at the end of Phase 6
once the HUD asset set is final.

---

## Recommended rebuild execution order

1. **6a (this dispatch)** — Lock this plan; delete GLSL assets + dead pure-Veil render files/mixins;
   keep `mixins.json` valid. *(design + safe cleanup only)*
2. **6b — Renderers / GeckoLib 5** *(A7, A8; delete B1/B5/B6 source + their mixins; B11)* — migrate
   entity renderers/models to `GeoEntityRenderer<T,R>` render-state; rebuild the Mowzie IK↔Geo bridge;
   delete `ShadowMapRenderer`, `SSAOSamples`, `VertexFormats` and the PBR/shadow mixins. First sub-phase
   that should compile a meaningful slice of the client.
3. **6c — VHS HUD** *(A1, A2, A14)* — `HudElementRegistry` + `GuiGraphicsExtractor` overlay element:
   scanlines/grain/vignette/REC/timestamp + jumpscare faces + stamina/title; rebuild `SkinwalkerJumpscare`
   timeline to drive HUD flags; rebuild `InGameHudMixin` as a HudElement.
4. **6d — Dynamic light** *(A3, A4, A5)* — flashlight via `Blocks.LIGHT` (+LambDynamicLights soft-dep);
   level-light emissive textures; Poolrooms day-cycle via `LightTexture` + ported `Easings`.
5. **6e — SPBRevampedClient** — re-wire the client entrypoint: register the HUD element, renderers,
   key-binds, flashlight tick, day-cycle tick; drop all Veil renderer init (flashlight/shadow/grass/bird/
   uniform hooks); reconnect to the rebuilt subsystems. *(11 Veil imports to remove)*
6. **6f — Render mixins + birds/grass/world** *(A6, A9–A13, A15–A18, B-group rebuilds-or-drops)* —
   render-tier mixins against the deferred API (name-tags, hand, item, player pose, respawn fade, camera,
   lightmap, block render layers); decide bird/grass rebuild-vs-drop on perf; rebuild/drop the
   `WorldRenderer`/weather/AO mixins. Finish by pruning unused `textures/shaders/*.png`.

---

## Key 26.2 API findings (verified via `javap` / jar inspection)

- **`HudElementRegistry`** (`fabric-rendering-v1`, `net.fabricmc.fabric.api.client.rendering.v1.hud`):
  `static void addFirst(Identifier, HudElement)`, `addLast(Identifier, HudElement)`,
  `attachElementBefore/After(Identifier, Identifier, HudElement)`, `removeElement(Identifier)`,
  `replaceElement(Identifier, Function<HudElement,HudElement>)`.
  **`HudElement`** is a functional interface: `void extractRenderState(GuiGraphicsExtractor, DeltaTracker)`.
- **`GuiGraphicsExtractor`** (`net.minecraft.client.gui`): `fill(int x1,y1,x2,y2,int argb)` and an
  overload `fill(RenderPipeline,…)`; `fillGradient(x1,y1,x2,y2,colTop,colBot)`;
  `text(Font, String|Component|FormattedCharSequence, int x, int y, int argb [, boolean shadow])`,
  `centeredText(...)`; `blit(RenderPipeline, Identifier, x, y, u, v, w, h, texW, texH[, color])` and
  `blit(Identifier, x,y,w,h, u0,u1,v0,v1)`; `blitSprite(RenderPipeline, Identifier, x,y,w,h)`;
  `pose():Matrix3x2fStack`; `enableScissor/disableScissor`; `guiWidth()/guiHeight()`. (No `DrawContext` —
  this replaces it.)
- **`GeoEntityRenderer`** (GeckoLib 5, relocated pkg **`com.geckolib.renderer`**):
  `class GeoEntityRenderer<T extends Entity & GeoAnimatable, R extends EntityRenderState> extends
  EntityRenderer<T,R> implements GeoRenderer<T,Void,R>`. Ctors:
  `(EntityRendererProvider$Context, EntityType<? extends T>)` and
  `(EntityRendererProvider$Context, GeoModel<T>)`. Note the **render-state type param `R`** — 26.2's
  deferred model; the renderer extracts into an `EntityRenderState` subtype. (GeckoLib 5.5.2.)
- **`RenderType`/`RenderTypes`** live at **`net.minecraft.client.renderer.rendertype.*`**.
  `RenderTypes` exposes entity factories: `entitySolid(Identifier)`, `entityCutout(Identifier[,boolean])`,
  `entityCutoutCull`, `entityTranslucent(Identifier[,boolean])`, `entityTranslucentEmissive(Identifier[,boolean])`,
  etc. Use `entityTranslucentEmissive` for the "glow" look that replaced deferred entity glow (A-group).
- **Veil dependency / maven repo**: already fully removed from `build.gradle` and `gradle.properties`
  (Phase 1) — verified, no remnants. Repos present: GeckoLib (cloudsmith), Simple Voice Chat (maxhenkel),
  Modrinth, mavenCentral. No `foundry`/`veil`/`pinwheel` references remain in the build scripts.
