# Backrooms FF Continued — Minecraft 1.20.1 → 26.2 + macOS Port

**Date:** 2026-06-25
**Author:** ARBird (port) · original *SP-Backrooms Revamped* by SpacePotato & Herr Chaos
**Status:** Design — approved approach, pending spec review

---

## 1. Goal

Take the existing **SP-Backrooms Revamped** Fabric mod (Minecraft **1.20.1**, Yarn mappings, Java 17,
~24,500 LOC across 295 Java files + 72 mixins) and:

1. Update it to **Minecraft 26.2** (Fabric, mojmap/unobfuscated, Java 25).
2. Make it **run on macOS** — the original cannot, because it depends on the **Veil** GLSL
   deferred-rendering library, which macOS's OpenGL stack cannot run and which has **no 26.2 build**.
3. Rebrand: display name **"Backrooms FF Continued"**, author **ARBird** (done), original authors
   credited per LGPL-3.0.

This is a *faithful port*, not a redesign. Gameplay, worldgen, entities, audio, and the *look* of the
VHS/Found-Footage experience are preserved. Anything Veil-driven keeps its visual intent but is
rebuilt with a Mac-safe mechanism.

## 2. Confirmed decisions

- **Mod id:** keep `spb-revamped` internally (asset namespaces, CCA component ids, mixin config,
  `com.sp` package all unchanged). Only display name/author changed. Avoids a cascade through ~333
  resource files and keeps existing saves loadable.
- **Compat:** keep **all** compat layers, including the optional Essential and Sound Physics
  Remastered integrations. If a compat dependency has no 26.2 build at port time, retain its code as a
  `modCompileOnly` **soft dependency with runtime guards** so the mod still loads without it.
- **Version control:** git initialized; baseline committed; work proceeds on branch `port/mc-26.2`
  with one commit per phase. No pushes without explicit approval.
- **Shader visuals:** Veil removed; its effects rebuilt as **Mac-safe approximations** (HUD overlays +
  vanilla rendering, zero GLSL). Fidelity is "close, not pixel-identical" — the accepted cost of macOS
  support.

## 3. Environment & toolchain

| Item | Current (1.20.1) | Target (26.2) |
|---|---|---|
| Minecraft | 1.20.1 | 26.2 |
| Mappings | Yarn `1.20.1+build.10` | **mojmap** (default, no `mappings`/`yarn_mappings` line) |
| Java | 17 | **25** (installed at `/opt/homebrew/opt/openjdk@25`; system default is 24) |
| Loom | 1.9-SNAPSHOT | **1.17** |
| Gradle wrapper | (current) | **9.5.1** |
| Fabric Loader | 0.16.5 | **0.19.3** |
| Fabric API | 0.92.2+1.20.1 | **0.152.2+26.2** |

Build invocation (Java 25 not the system default):
`JAVA_HOME=/opt/homebrew/opt/openjdk@25/libexec/openjdk.jdk/Contents/Home ./gradlew build`

**Approach:** start from Fabric's official 26.2 example-mod `build.gradle`/`gradle.properties` as the
baseline (correct loom plugin id + dependency configurations for mojmap), then re-add our dependencies.
Do not hand-guess the loom/mojmap wiring.

## 4. Dependency matrix

| Dependency | 1.20.1 | 26.2 plan | Notes |
|---|---|---|---|
| Fabric API / Loader | 0.92 / 0.16 | 0.152.2+26.2 / 0.19.3 | ✅ available |
| GeckoLib | 4.4.9 | 5.5.2 | ✅ available — **4.x → 5.x API migration** for models/renderers/animation |
| Cardinal Components (base/entity/world) | 5.2.2 | 8.0.0 (26.1.2+) | ✅ available — **API migration**; verify entrypoint + registration changes |
| Simple Voice Chat | fabric-1.20.1-2.4.32 | 2.6.20+26.2 | ✅ available; bump `voicechat-api` to match |
| ModMenu | 7.2.2 | verify in Phase 1 | almost certainly available for 26.2 |
| MidnightLib | 1.4.1-fabric | verify in Phase 1 | if no 26.2 build → replace config screen with a small custom/ModMenu-only config |
| **Veil** | 1.0.0.228 | **REMOVED** | no 26.2 build; macOS-breaker; effects rebuilt Mac-safe (§7) |
| Sound Physics Remastered (compat) | modCompileOnly 1.20.1-1.4.8 | keep; resolve 26.2 build or guard as soft-dep | per "keep all compat" decision |
| Essential (compat) | modCompileOnly 1.20.1-1.4.2 + k2ZPuTBm | keep; resolve 26.2 build or guard as soft-dep | per "keep all compat" decision |

## 5. Codebase inventory (what we're porting)

Java packages under `com.sp` (file counts): `mixin` 72 · `entity` 49 · `world` 39 · `block` 39 ·
`render` 21 · `sounds` 16 · `init` 13 · `networking` 10 · `mixininterfaces` 6 · `item` 6 · `util` 5 ·
`command` 5 · `cca_stuff` 5 · `datagen` 2 · `compat` 2 · `clientWrapper` 1 + 4 root entrypoints.

Resources: 333 JSON (worldgen, dimension_type, damage_type, structures, loot_tables, biome,
noise_settings, blockstates, models, lang, atlases) · 285 NBT structures · 143 ogg · 139 png ·
Veil shader assets (24 fsh / 14 vsh / 6 glsl / `pinwheel/`) → dropped.

Veil coupling: **38 Java files** import `foundry.veil.*` — deferred point/area lights, framebuffers,
post-processing pipeline, custom shader programs, shader animation paths.

## 6. Mappings migration strategy (the bulk)

Compile-driven, **package by package**, not a blind find/replace:

1. `genSources` and `javap` the 26.2 merged jar for real mojmap signatures; consult the Fabric
   [porting guide](https://docs.fabricmc.net/develop/porting/) for semantic API changes across the gap.
2. Port in dependency order: **init/registries → blocks/items → worldgen/dimensions/data → entities →
   render → networking/CCA/sounds/commands**. Get each layer compiling before the next.
3. Convert the access widener targets to mojmap names.

Known 26.2 API shifts to expect (from prior 26.2 work; verify each against `javap`):
`Identifier.fromNamespaceAndPath`; `ResourceKey.identifier()`; GUI draw through the 26.2
`GuiGraphics`-equivalent with deferred `submit()` rendering; items/blocks require
`.setId(ResourceKey)` before construction **plus** `assets/<ns>/items/<id>.json` model definitions;
dimension_type schema reworked; loot tables under `data/<ns>/loot_table/` (singular); entity
renderers override `submit(...)` rather than `render(...)`; permission checks via
`CommandSourceStack` predicates (`::isPlayer`) instead of `hasPermission(int)`.

## 7. Veil removal → Mac-safe visual rebuild

Remove the Veil dependency, delete `assets/.../pinwheel/` GLSL assets, and rewrite the 38 Veil-using
files. Rebuild effects with **no GLSL**:

- **VHS / camcorder grade & post-processing** → a HUD overlay render layer (registered via the 26.2
  HUD API). Composited from translucent fills + small tiled textures + text: scanlines, grain/noise,
  vignette, REC indicator, timestamp, battery, slight color-fringe. Implemented in the deferred
  `submit()` model.
- **Dynamic flashlight / point lights** → vanilla `Blocks.LIGHT` projection along the look vector
  (with flicker), soft-detecting LambDynamicLights if installed.
- **Deferred area lights, entity glow, custom GLSL effects** → emissive/glowing textures and
  approximations. Any effect that fundamentally requires GLSL is approximated or dropped, **documented
  per-effect** in `docs/MAC_SAFE_RENDERING.md` so the deltas from the original are explicit.

Acceptance: the camcorder/Found-Footage *feel* is preserved; exact shader output is not reproduced.

## 8. The 72 mixins (highest risk)

Triage every mixin against 26.2 sources:

- **(a) target still exists** → remap names/descriptors.
- **(b) target moved/changed** → locate the new injection point.
- **(c) now achievable via a Fabric API event/hook** → replace the mixin (preferred — less fragile).
- **(d) obsolete** (e.g., tied to removed internals or to Veil) → drop, noting any behavior change.

Produce a `docs/MIXIN_TRIAGE.md` table (mixin → target → verdict → action). This is the slowest,
most iterative phase and the most likely to need client runtime testing.

## 9. Data & assets

- Regenerate data via the mod's `fabric-datagen` entrypoint where possible (post-port).
- Hand-fix static JSON to 26.2 schemas: dimension_type, worldgen (noise_settings/biome), loot tables
  (dir rename), recipes/tags, block/item model definitions (new `items/` model requirement).
- Re-save the 285 NBT structures to the 26.2 data version.
- ogg/png carry over unchanged; Veil shader assets removed.

## 10. Verification & handoff

**I (assistant) verify:**
- Clean `./gradlew build` under Java 25.
- Headless dedicated-server boot with no exceptions.
- A worldgen **probe** (`-Dffc.probe=true` system property) that force-generates each Backrooms
  dimension at server start and logs block states, so worldgen is checkable without a client.

**You (user) verify on macOS** (cannot be automated here):
- `./gradlew runClient` playtest: VHS HUD overlay, entity rendering (GeckoLib 5), dynamic light,
  Simple Voice Chat proximity audio, and core gameplay loop across the levels.

## 11. Risk register

| Risk | Likelihood | Mitigation |
|---|---|---|
| Mixin targets gone/renamed across 6+ versions | High | Triage table; prefer Fabric API hooks; drop+document obsolete ones |
| Veil effects not faithfully reproducible without GLSL | High (accepted) | HUD/vanilla approximations; per-effect delta doc |
| GeckoLib 4→5 / CCA 5→8 API breaks | Medium | Migrate against current docs + `javap`; isolate behind our own wrappers |
| Compat deps (Essential/Sound Physics) lack 26.2 builds | Medium | Keep code, guard as soft-deps so base mod still loads |
| Worldgen/data schema drift breaks generation | Medium | Datagen + probe verification per dimension |
| Won't fully compile/run first pass; needs iteration | High | Phased, build-green-per-phase; user client playtests |

## 12. Execution phases (each ends on a green build)

0. **Renames / hygiene** — display name, author, README, git init. *(done)*
1. **Toolchain** — 26.2 example baseline `build.gradle`/`gradle.properties`, loom 1.17, Java 25,
   gradle 9.5.1; resolve every dependency (confirm ModMenu/MidnightLib/compat 26.2 builds).
2. **Init + registries** — entrypoints, block/item/entity/sound registration to mojmap/26.2 (`setId`).
3. **Worldgen + data** — chunk gen, dimensions, data JSON to 26.2 schemas; headless probe green.
4. **Entities** — GeckoLib 5 migration, AI, renderers to deferred `submit()`.
5. **Mixins** — triage + re-target / replace / drop (the big one).
6. **Veil → Mac-safe visuals** — remove Veil; HUD VHS overlay + dynamic light; per-effect delta doc.
7. **CCA / networking / voice / sound / commands / compat** — remaining systems + soft-dep guards.
8. **Compile-clean + headless verify + handoff** — full build green, server boot + probe clean,
   handoff notes for the Mac client playtest. Iterate on user feedback.

## 13. Out of scope

- New gameplay features or content beyond what the 1.20.1 mod already has.
- Forge/NeoForge ports (Fabric only).
- Pixel-exact reproduction of Veil shader output.
- Renaming the internal mod id / asset namespace (kept as `spb-revamped`).
