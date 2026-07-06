# Backrooms FF Continued — Port Handoff (resume-from-here)

**Purpose:** self-contained state dump so a fresh chat/session can continue this port with zero prior
context. Read this + the four reference docs (§9) + `git log --oneline` and you're caught up.

---

## 1. What this is

Porting the Fabric mod **SP-Backrooms Revamped** ("Minecraft Found Footage", by SpacePotato & Herr
Chaos, LGPL-3.0) from **Minecraft 1.20.1 → 26.2** and making it **run on macOS**. Rebranded to display
name **"Backrooms FF Continued"**, author **ARBird** (the user). Internal mod id kept as `spb-revamped`.

> **The target platform (macOS, MC 26.2) has NOT changed.** As of 2026-07-04 the *development/build
> machine* moved to Windows (§13) — that's purely where the assistant compiles and RCON-verifies from.
> The user still plays on macOS; nothing in the goal, the Mac-safe rendering approach, or the "no Veil/
> GLSL" constraint changes because of this. Windows is only relevant for local build tooling paths
> (JDK location, `gradlew.bat`, no FIFO trick for headless RCON — see §15.4) and is otherwise invisible
> to the mod itself (Fabric/Minecraft are cross-platform; nothing in the source is Windows- or
> macOS-specific).

- **Project dir:** `/Users/ayanraj/Documents/MinecraftFoundFootage-master`
- **Git branch:** `port/mc-26.2` (off `main`). ~84 commits. **Nothing pushed or merged** (no remote needed).
- **Scale:** 295 Java files / ~24.5k LOC / originally 72 mixins / 333 json / 285 nbt. A Yarn→mojmap
  rewrite, not a version bump.
- **Why macOS was broken originally:** the mod used **Veil** (GLSL deferred-rendering lib). Veil has no
  26.2 build and macOS can't run its pipeline. Veil is fully removed; visuals rebuilt Mac-safe (no GLSL).

## 2. CURRENT STATUS — essentially done, in client-playtest polish

- ✅ **`./gradlew build` SUCCEEDS** → `build/libs/backrooms-ff-continued-2.0.0.jar`.
- ✅ **Headless dedicated server boots clean** (`Done`), all Backrooms dims generate (level0/1/2/324,
  infinite_field, poolrooms) + voicechat server, no fatal exceptions.
- ✅ **Client (`runClient` / the built jar in Modrinth) loads fully**: title screen, resource reload, world
  creation, all dimensions generate.
- ✅ Fixed a series of runtime-only crashes the client surfaced (mixin injection, CCA module, player-spawn).
- 🔜 **NEXT: user re-launches after the latest fix (player-spawn NPE).** Awaiting confirmation they spawn
  into the world. After that it's cosmetic/visual polish (§11), not load-blockers.

**The iteration loop right now:** user runs the client on their Mac → pastes any crash trace → we fix the
code → `./gradlew build` → copy jar to their Modrinth profile (§4) → they relaunch. Each launch has been
surfacing exactly one fix. We are at the tail of this loop.

## 3. Locked decisions (do not relitigate)

- Keep internal modid `spb-revamped` + `com.sp` package (renaming cascades through 333 resource files).
- Keep ALL compat (incl. Sound Physics Remastered) — guarded as soft/compile-only deps.
- Mac-safe **approximations** for visuals (HUD overlays + vanilla `Blocks.LIGHT`, zero GLSL). Fidelity is
  "close, not pixel-identical." Effects that fundamentally need GLSL are dropped, documented (§10).
- Faithful full port (the user explicitly chose this over a trimmed/rewrite approach).
- git checkpoint per phase; **commit, never push/merge without the user asking.**

## 4. Build / run / iterate commands (macOS, this machine)

**Java 25 is required and is NOT the system default (system is 24).** Always set JAVA_HOME:
```
export JH=/opt/homebrew/opt/openjdk@25/libexec/openjdk.jdk/Contents/Home
JAVA_HOME="$JH" ./gradlew -p /Users/ayanraj/Documents/MinecraftFoundFootage-master build
```
- **Build jar:** the above → `build/libs/backrooms-ff-continued-2.0.0.jar`.
- **The user runs the BUILT jar in a Modrinth profile**, not `runClient`. After every rebuild, copy it:
  ```
  cp build/libs/backrooms-ff-continued-2.0.0.jar \
    "/Users/ayanraj/Library/Application Support/ModrinthApp/profiles/mc backrooms/mods/"
  ```
  (their runtime: fabric-api 0.153.0+26.2, geckolib 5.5.3 — a hair newer than build deps; harmless.)
- **Dev client (alt):** `JAVA_HOME="$JH" ./gradlew runClient` (needs a display; assistant can't run it).
- **Headless server verify:** create `run/eula.txt`=`eula=true`; `JAVA_HOME="$JH" ./gradlew runServer`,
  watch the log for `Done (` vs `Exception`/`Mixin apply`. Dev server hangs on piped stdin — use a
  timeout/background + grep the log. Free port: `lsof -ti :25565 | xargs kill -9`.
- **runDatagen is BROKEN** (mod-load: fabric modules not exposed as loadable mods because deps use plain
  `implementation`, not Loom `mod*`). Not needed for build/run. If regenerating data, first move fabric
  deps to `modImplementation`. The committed `src/main/generated/` output stands.

## 5. Per-file compile verification harness (no full build needed)

The whole project compiles, but to check an individual file fast:
```
# one-time: classpath cache
cat > /tmp/cp.init.gradle <<'EOF'
gradle.projectsEvaluated { rootProject.tasks.register('printCompileCp'){ doLast{ println "CPSTART"+rootProject.sourceSets.main.compileClasspath.asPath+"CPEND" } } }
EOF
JAVA_HOME="$JH" ./gradlew -p /Users/ayanraj/Documents/MinecraftFoundFootage-master -I /tmp/cp.init.gradle printCompileCp -q | sed -n 's/.*CPSTART\(.*\)CPEND.*/\1/p' > /tmp/ffc_cp.txt
# compile one or more files (errors only in YOUR files matter; render/mixin boundary errors are expected mid-port)
"$JH/bin/javac" --release 25 -cp "$(cat /tmp/ffc_cp.txt)" -d /tmp/out -sourcepath src/main/java <files>
# whole-tree error scan:
find src/main/java -name '*.java' > /tmp/srcs.txt && "$JH/bin/javac" --release 25 -cp "$(cat /tmp/ffc_cp.txt)" -d /tmp/out -Xmaxerrs 2000 @/tmp/srcs.txt 2>&1 | grep 'error:'
```
- If `--release` complains about duplicate fabric-api jars, dedupe `/tmp/ffc_cp.txt` to one jar per artifact.
- **Signature ground truth:** the decompiled merged jar
  `/Users/ayanraj/.gradle/caches/fabric-loom/minecraftMaven/net/minecraft/minecraft-merged-deobf/26.2/minecraft-merged-deobf-26.2.jar`.
  **Use Java-25 `javap`** (`"$JH/bin/javap"`) — the system javap can't read class-file v69. Useful:
  `javap -p -c -s -cp <jar> <FQN>` (bytecode shows exact `@At` INVOKE/FIELD descriptors);
  `unzip -l <jar> | grep -i Name` to find a class; class index = `unzip -l <jar> | grep .class` (7055 classes).

## 6. Toolchain & dependency versions (in gradle.properties / build.gradle)

- minecraft `26.2`, **mojmap** (NO yarn/mappings line), loom `1.17-SNAPSHOT`, gradle wrapper `9.5.1`,
  loader `0.19.3`, fabric-api `0.152.2+26.2`, Java release `25`.
- build.gradle plugin id is **`net.fabricmc.fabric-loom`**; mods use plain `implementation` (26.2 is
  unobf, no remap — `modImplementation` doesn't exist in this loom variant), `include` for JIJ still works.
- **GeckoLib** `5.5.2` (pkg relocated `software.bernie.geckolib` → **`com.geckolib`**); **Cardinal
  Components** `8.0.0` — modules `cardinal-components-base` + `-entity` + **`-level`** (⚠ CCA 8 renamed
  `cardinal-components-world` → `-level`; the `-world` artifact 404s — both impl+include must say `-level`);
  **Simple Voice Chat** `fabric-2.6.20+26.2` + `voicechat-api 2.6.20`; **ModMenu** `20.0.0-beta.4`;
  **MidnightLib** `1.9.3+26.2-fabric`; **Sound Physics Remastered** `compileOnly fabric-1.5.1+26.2`.
- `fabric.mod.json` depends were updated: `fabric-key-binding-api-v1` → **`fabric-key-mapping-api-v1`**
  (renamed in 26.2); minecraft `~26.2`, java `>=25`, fabricloader `>=0.19.3`, voicechat `>=2.6.20`,
  geckolib `>=5.5.2`. Client entrypoints: `com.sp.SPBRevampedClient`, `com.sp.render.gui.BackroomsHudClientInit`.
- **Access widener** (`src/main/resources/spb-revamped.accesswidener`) is ENABLED in build.gradle; header
  MUST be **`accessWidener v2 official`** (loom rejects `named` in this mojmap workspace). Current entries:
  `ChunkPos.x`, `ChunkPos.z`, `SoundEngine.instanceBySource`, + `BlockModelGenerators` private helpers
  (`createTrivialCube`/`family`) and the package-private `BlockFamilyProvider`.

## 7. Phases completed (all 8; see git log for per-commit detail)

1. **Toolchain** → 26.2/mojmap/Java25/loom1.17; Veil removed; genSources + deps resolve.
2. **Init/registries/entrypoints** (`com.sp.{SPBRevamped,SPBRevampedClient,ModKeyBinds}`, `init/`, `item/`, `util/`).
3. **Blocks + worldgen + data** (`block/**`, `world/**`, datagen, data JSON → 26.2 schemas:
   loot_tables→loot_table, dimension_type/biome reworked, 76 `assets/items/<id>.json` model defs).
4. **Entities + GeckoLib 5** (`entity/**`, GeoModel/GeoEntityRenderer on the deferred render-state pipeline).
5. **Mixins** — triaged 72, executed non-render ones (11 REMAP, server-logic NEW-INJECTION, 14 dropped).
6. **Veil removal / Mac-safe visuals** — 91 shader assets purged; VHS HUD via HudElementRegistry; dynamic
   light via vanilla Blocks.LIGHT; bird/grass/camera/cutscene rebuilt; SPBRevampedClient rewired; 9 render
   mixins rewritten incl. CameraMixin shake+roll, 20 dead Veil/immediate-mode render mixins dropped
   (mixins.json client 34→15, common 18→16).
7. **CCA/networking/voice/commands/compat** — CCA 5→8, networking → CustomPacketPayload+StreamCodec,
   sounds + SVC plugin, brigadier commands, ModMenu/MidnightLib config, SFR compat soft-dep.
8. **Full build + headless verify + handoff** — build green, server boots clean, then fixed the
   client-runtime crashes (mixin injection, CCA -level module, player-spawn NPE).

## 8. Reference docs IN THE REPO (read these to resume)

- `docs/PORTING_MAP.md` — verified Yarn→mojmap FQN table for all 263 imports + large "Discovered (TRUST
  THESE)" sections per area (registries, blocks, block entities, NBT, networking, render, GeckoLib 5,
  CCA 8, datagen, render-tier mixins, compile-tail). **The single most useful reference.**
- `docs/MIXIN_TRIAGE.md` — all 72 mixins classified (verdict/target/notes).
- `docs/MAC_SAFE_RENDERING.md` — every Veil/render effect → Mac-safe rebuild or documented drop (18 rebuilt,
  11 GLSL-only dropped).
- `docs/CLIENT_PLAYTEST.md` — the macOS playtest checklist + expected deltas (for the user).
- `docs/superpowers/specs/2026-06-25-...-design.md` and `docs/superpowers/plans/2026-06-25-...-port.md` —
  the original spec + 8-phase plan.

## 9. Biggest 26.2 API gotchas (full detail in PORTING_MAP.md)

- mojmap names; `ResourceLocation` is named **`Identifier`** (`net.minecraft.resources.Identifier`,
  `.fromNamespaceAndPath`). `World`→`Level`, `Vec3d`→`Vec3` (public x/y/z), `MathHelper`→`Mth`, `Text`→`Component`.
- **NBT:** BlockEntity/Entity read-write use **`ValueInput`/`ValueOutput`** (`loadAdditional`/`saveAdditional`,
  `addAdditionalSaveData`/`readAdditionalSaveData`), not CompoundTag.
- **`.is(Block/Item/Fluid)` REMOVED** → `getBlock()==` / `getItem()==` / `getType()==`.
- **Networking:** PacketByteBuf channels gone → `record … implements CustomPacketPayload` + `StreamCodec` +
  `PayloadTypeRegistry.serverboundPlay()/clientboundPlay()`; `ClientPlayNetworking.send(payload)`.
- **Render:** deferred `submit()` pipeline; `MultiBufferSource`/immediate-mode `BufferBuilder`/`Tesselator`
  GONE; `DrawContext`→**`GuiGraphicsExtractor`**; HUD via **`HudElementRegistry`**; `RenderType` under
  `client.renderer.rendertype.*`; **no `BlockRenderLayerMap`** (render type is model-JSON `render_type`);
  `Camera.setRotation` is protected (use `@Invoker`); `Minecraft.runTick` delegates rendering to **`renderFrame`**.
- **CCA 8:** `org.ladysnake.cca.*`; world initializer is **`org.ladysnake.cca.api.v8.level.LevelComponentInitializer`**
  (in the `cardinal-components-level` module); components built during `Entity.<init>` (BEFORE Player's
  inventory exists — see the spawn fix).
- **GeckoLib 5:** `com.geckolib.*`; `GeoEntityRenderer<T,R extends EntityRenderState>`; immutable bones.
- **Keybinds:** `fabric-key-mapping-api-v1` / `KeyMappingHelper.registerKeyMapping`; `KeyMapping`(+`Category`).
- **Teleport:** `FabricDimensions` gone → `entity.teleport(new TeleportTransition(ServerLevel,pos,delta,yRot,xRot,DO_NOTHING))`.
- Datagen: client `FabricModelProvider` (`BlockModelGenerators`/`ItemModelGenerators`), `RegistrySetBuilder`.

## 10. Documented Mac-safe deltas (intentional, not bugs)

Rebuilt: VHS/camcorder HUD overlay (scanlines/grain/vignette/REC via GuiGraphicsExtractor); flashlight +
fixture lights via vanilla `Blocks.LIGHT`/lightLevel; camera shake/roll via CameraMixin; cutscene path
(in-repo CameraPath linear-lerp replacing Veil anim.Path).
Dropped (GLSL-only or removed API; behavior lost, documented): shadow-map renderer, SSAO, PBR vertex
pipeline + per-block PBR color textures, custom GLSL world/sky/distortion shaders, AO smoothing fix,
smiler opacity fade, flattened dropped-items, player-self model hide during cutscene, respawn warp/static
shader, VHS 4:3 custom resolution list, in-backrooms jump suppression, sound-physics sound exclusion
(AllowedSoundsConfig mixin dropped — its SFR target class no longer exists). Spider-leg force-matrix IK
falls back to standard GeckoLib posing (v5 bones immutable). **Bird & grass are simulated (boids/CPU model
kept) but NOT drawn** — 26.2 removed immediate-mode draw; needs a vanilla submit-draw or a final drop call.

## 11. Known remaining work / watch-items (the next-session punch list)

1. **Confirm player spawns** after the latest fix (CCA component built before inventory → sized
   `playerSavedInventory` from `Inventory.INVENTORY_SIZE`; guarded `CutsceneManager.reset` vs null
   `client.player`). If another crash appears, it'll be the next client-only runtime issue — fix, rebuild,
   recopy jar.
2. **Cosmetic model/texture issues** seen in the client log (non-fatal):
   - `Cannot compute translucency out of bounds` baking `slope`, `one_sided_shelf`, `double_sided_shelf`,
     `thin_pipe_corner` — model UV/atlas issues; those blocks may show missing-texture.
   - `one_sided_shelf … stuff=5` blockstate variant references a value outside the property range `[0,4]`
     — fix the blockstate JSON (or the property).
   - `Missing textures … block/pbr/<x>_color` — PBR color textures dropped with Veil; affected blocks fall
     back to base textures. Either supply plain textures or repoint those models.
   - `Duplicate sprite …` warnings are vanilla (items+blocks atlas overlap), harmless.
3. **Bird/grass not drawn** — decide: implement a vanilla submit-draw or finalize as dropped.
4. **block `render_type`** — 24-block cutout/translucent tier list is in PORTING_MAP; verify glass/light/
   pool-tile blocks render with correct transparency in-game (model-JSON driven now).
5. **Re-addable dropped render features** (optional polish): smiler opacity, item flatten, etc. could be
   reimplemented in the GeckoLib/entity render-states.
6. **SFR compat:** 2 `@Pseudo` mixins skip cleanly unless Sound Physics Remastered is installed at runtime.
7. **Merge:** when the user is happy, merge `port/mc-26.2` → `main` (ask first; no push has happened).

## 12. How the work was executed (process notes)

- Done via the superpowers brainstorming→writing-plans→executing-plans flow (spec+plan committed).
- Bulk porting was delegated to **general-purpose subagents**, one package/phase each, committing per group.
  Subagents updated `docs/PORTING_MAP.md` themselves as they discovered mappings.
- **Anthropic rate limits (per-session AND weekly) hit subagents frequently.** After each agent: check
  `git status`/`git log` — some died mid-work leaving uncommitted-but-valid files (verify-compile then
  commit), or hit the limit with no report. Resume a killed agent via its `agentId` (SendMessage) — its
  research context carries over — or dispatch fresh.
- Client-runtime crashes were fixed inline (fast iteration with the user), not via agents.

## 13. To resume in a new chat

> **⚠ DEV MACHINE MOVED (2026-07-04): the assistant now builds/verifies from the user's WINDOWS machine
> at** `C:\Users\rajay\Documents\MinecraftFoundFootage-master-20260705T011342Z-3-001\MinecraftFoundFootage-master`
> (note the extra nesting level under the -20260705… folder). This is a change of *dev environment only*
> — see §1's callout. The mod's target platform is still macOS on MC 26.2; the user still plays the
> built jar on their Mac. All §4/§5 macOS paths/commands are historical dev-machine details (kept for
> context, not because macOS support was dropped); see §15.4 for the Windows equivalents. Everything
> else (branch, decisions, docs, the actual porting goal) unchanged.

1. Read this file, then `docs/PORTING_MAP.md` + `docs/CLIENT_PLAYTEST.md`, then `git log --oneline | head -40`.
2. Confirm green (Windows): set `JAVA_HOME` to JDK 25 (`C:\Program Files\Eclipse Adoptium\jdk-25.0.3.9-hotspot`)
   and run `gradlew.bat build` in the project dir.
3. Ask the user for their latest client result / crash trace, or address the §11 cosmetic items.
4. Fix → rebuild → give the user the jar from `build\libs` (no launcher profile exists on the Windows
   machine yet — the old auto-deploy path was the macOS Modrinth profile). Commit each fix (no push).

## 14. Client playtest fixes session (2026-06-28/29, commits e8802ec..737aad7)

User ran the built jar on macOS and reported issues one at a time; each was root-caused (not
guessed) and fixed. **All committed to `port/mc-26.2` except one stashed WIP (see §14.6).**

### 14.1 Rainbow static covering the whole screen (commit e8802ec)

`BackroomsHudOverlay.drawGrain()` blitted `vhs_noise.png` (a saturated RGBA noise texture) with the
simple `blit(id,x,y,w,h,u0,u1,v0,v1)` overload — decompiling 26.2's `GuiGraphicsExtractor` showed this
overload hardcodes `iconst_m1` (opaque white) as the tint, so the full-color noise painted solid over
the world. Fixed by routing through the color-aware
`blit(pipeline,id,x,y,uOffset,vOffset,w,h,regionW,regionH,texW,texH,color)` overload with a low-alpha
white tint (`GRAIN_TINT = 0x18FFFFFF`, ~9%); `GUI_TEXTURED` uses `BlendFunction.TRANSLUCENT` so low
alpha now composites as faint translucent grain instead of an opaque fill.

### 14.2 Death loop + pitch-black on entering the Backrooms (commit 64dd5d4) — the big one

Reported as "I die as soon as I enter the backrooms and end up in a death loop" + a screenshot of a
totally black screen. Root-caused via a **headless RCON probe harness** (§14.7) rather than guessing —
every Backrooms dimension was generating as **completely empty void**. Three separate port regressions,
found and fixed together:

1. **Orphaned generation hook.** `BackroomsChunkGenerator.generate(WorldGenLevel, ChunkAccess)` was
   declared abstract but called from NOWHERE. The original 1.20.1 mod ran it from a custom
   `ChunkStatus` task; per `MIXIN_TRIAGE.md` line 83 that hook was dropped in the port with a note to
   "re-hook elsewhere." Fix: override `ChunkGenerator.applyBiomeDecoration(WorldGenLevel, ChunkAccess,
   StructureManager)` — the 26.2 mojmap rename of Yarn's `generateFeatures` — and call `generate()` from
   it. This is the per-chunk decoration stage and is the correct re-hook point.
2. **Wrong structure folder name.** All 285 room/maze `.nbt` templates lived in
   `data/spb-revamped/structures/` (plural), but 1.21+/26.2 renamed the source directory to
   **`structure/`** (singular) — confirmed via `javap` on `StructureTemplateManager`
   (`STRUCTURE_DIRECTORY_NAME` = literal `"structure"`). Same class of rename as the already-handled
   `loot_tables`→`loot_table`. Fixed by `git mv structures structure`.
3. **26.2 write-radius drops far-chunk writes.** The generators place megarooms/mazes spanning several
   chunks from one "anchor" chunk's decoration pass, but 26.2's `WorldGenRegion` enforces
   `blockStateWriteRadius=1` and silently drops (logs "Detected setBlock in a far chunk") any write
   outside that radius. Added `mixin/worldgen/BackroomsWorldGenRegionMixin` — for
   `BackroomsChunkGenerator` instances only, treats any chunk present in the region's loaded
   `StaticCache2D` (guaranteed safe — never touches an unloaded chunk) as writable/readable, overriding
   `isWithinWriteZone(II)` and silencing `warnIfReadOutsideWriteZone(II)`. Vanilla dimensions untouched.

Verified via RCON: level0 now generates a continuous floor (y=20) under spawn with headroom to the
ceiling (y=25), zero far-chunk errors, player lands safely instead of falling into the void.

**IMPORTANT caveat that shipped with this fix:** existing saves already have level0 stored as void from
before the fix — the fix only affects newly-generated chunks. User must start a new world or delete
`dimensions/spb-revamped/` from their save.

### 14.3 Camera shake far too violent (commit a8bbfee)

`CameraShake.tick()`'s port comment says the old per-tick distance-delta speed proxy (~0.2 while
walking) was replaced by `player.walkAnimation.speed(1.0f)` (26.2 dropped `prevHorizontalSpeed`), but
the old `*6` multiplier was left in place. `walkAnimation.speed` is already a normalized 0..1 amplitude,
so `*6` pinned `traumaGoal` at its 1.5 max on every step (~±9° pitch/yaw, ~±18° roll) — constant violent
shake. Fixed: `*2.0` instead (walk ≈1.0-1.4, sprint ≈2.0, matching the original feel), amplitude 4→3.

### 14.4 Intro credit text (commit 0688b99)

`TitleText.java` hardcoded `"SpacePotato"` in the intro-cutscene credit line. Changed to `"ARBird"` to
match the rebrand (upstream attribution to SpacePotato & Herr Chaos remains in `fabric.mod.json`
description / LGPL notices, which is correct — only the in-game credit changed).

### 14.5 Missing/pink-black block textures + crash ticking fluorescent lights (commits b45c121, 719a072)

Second playtest round, after §14.2's fix, surfaced two new issues from the client log:

- **Crash:** `Cannot get property BooleanProperty{name=on...} as it does not exist in
  Block{spb-revamped:thin_fluorescent_light}` in `ThinFluorescentLightBlockEntity.tick()` (and the same
  bug in `TinyFluorescentLightBlockEntity`). Both read `FluorescentLightBlock.ON` — the FULL light
  block's property instance — off a thin/tiny light block state. **26.2 compares block-state properties
  by identity** (1.20.1 compared by name), so this throws once a thin/tiny light loads and ticks. Fixed
  both to read their own block's `ON` property (`ThinFluorescentLightBlock.ON` /
  `TinyFluorescentLightBlock.ON`). ⚠ General 26.2 gotcha: any cross-class property reference that
  happens to share a name will now fail — search for this pattern if more crashes like it appear.
- **Textures:** the pink/black floor & ceiling in the user's screenshot. `assets/minecraft/atlases/
  blocks.json` (the block atlas config) carried a leftover Veil-PBR filter —
  `{"type":"filter","pattern":{"path":"(_color)"}}` — that stripped every `_color` texture out of the
  stitched atlas. Veil used to split color/normal/height into separate atlases; without Veil the
  `_color` maps ARE the base textures for carpet, ceiling_tile, floor_tiling, bricks, road, concrete,
  crate, pavement, chain_fence, and dirt (no plain fallback exists). Removed the `_color` filter (kept
  `_normal`/`_height`, which no model samples).

Verified crash fix via RCON: placed thin/tiny/full fluorescent lights, ticked them, zero exceptions.
Texture fix is client-render only (headless can't verify) but the cause was unambiguous.

### 14.6 Skinwalker jumpscare shows only a black screen (commit 737aad7)

`SkinwalkerJumpscare.doJumpscare()` — which drives the creepy-face overlay flags on
`BackroomsHudOverlay` — was fully implemented but **called from nowhere**, so a capture only showed the
black "being pulled under" beat (`isBeingReleased`/`isInWall`) with no face ever appearing. Fixed by
calling it from `ClientWrapper`'s client tick: `if (playerComponent.isBeingCaptured())
SkinwalkerJumpscare.doJumpscare(client, playerComponent);` (+ `reset()` if a capture ends early). Also
made `reset()` clear the timeline's `started`/`startTime` fields so a later capture replays from the
start instead of instantly hitting the 14s end-of-timeline branch.

### 14.7 Holes in the roof / gaps in the world floor — ROOT-CAUSED, chunk-local rewrite ATTEMPTED AND STASHED (not merged)

After §14.2's fix the world generates and is lit, but has **systematic void chunk-columns** at the
far edges of each maze region (RCON-mapped: ceiling y=25 is 100% solid; floor y=20 has ~15% holes,
occurring at chunks ≡ anchor±2, i.e. every `FINAL_MAZE_SIZE`=5 chunks). Root cause: the write-radius
mixin from §14.2 only recovers writes that land on chunks the region cache happens to finalize; writes
to the far-edge chunks (distance 2-3 from the anchor) still land on throwaway placeholder chunks that
get discarded.

**Attempted fix (built, tested, ultimately reverted — see below):** made all 6 level generators +
3 maze generators (`Level0/1/Poolrooms MazeGenerator`, `MazeCell`) fully deterministic (every
`RandomSource.create()` reseeded from world position via a new `BackroomsGenBuffer.seed(x,z)` helper)
and refactored `generate()` to take `(WorldGenLevel, ChunkPos anchorPos)` instead of `ChunkAccess`. Added
`world/generation/BackroomsGenBuffer.java`: a `ThreadLocal` in-memory block buffer + per-thread
small-LRU cache keyed by anchor `ChunkPos`, plus rewired `BackroomsWorldGenRegionMixin` to redirect
`getBlockState`/`setBlock`/`getBlockEntity` into the active buffer. `BackroomsChunkGenerator
.applyBiomeDecoration` was changed to rebuild every anchor overlapping the current chunk (±`ANCHOR_REACH`
= 2) into its own deterministic buffer and copy out only the current chunk's slice — so every chunk that
touches an anchor's region reconstructs the identical buffer and writes only its own piece, eliminating
cross-chunk writes entirely.

**This compiled clean (`./gradlew build` SUCCESS) but failed at runtime and was reverted:**
`StructureTemplate.placeInWorld` internally calls `WorldGenRegion.getChunk(pos)` /
`markPosForPostProcessing` on far positions — a code path the buffer redirect did NOT intercept (only
`getBlockState`/`setBlock`/`getBlockEntity` were hooked). This produced **~7,000,000 "unsafe terrain
read" log warnings and hung the server** on a fresh-world boot. The ±2-anchor rebuild window is also
slow (~25 chunks rebuild the same region). **Rolled back via `git stash`** (not deleted — recoverable):
run `git stash list` on `port/mc-26.2`, entry titled *"WIP chunk-local worldgen rewrite — buffer engine
+ all 6 generators refactored deterministic; INCOMPLETE: placeInWorld getChunk/post-processing reads
not intercepted (7M unsafe-read warnings) + 25x-per-chunk perf; needs read interception + perf tuning +
visual playtest"*. Working tree is back to the §14.2/§14.5 write-zone-bypass mixin (playable, lit,
traversable, gaps at region edges) — verified with a clean reboot (`Done`, 0 unsafe-read warnings, 0
generation exceptions) before shipping.

**To finish this properly (next session):**
1. `git stash pop` (or `git stash apply` to keep the stash as a fallback) on `port/mc-26.2`.
2. Also intercept `WorldGenRegion.getChunk(BlockPos)` (and whatever triggers
   `markPosForPostProcessing`) in the buffer redirect mixin — same pattern as the existing
   `getBlockState`/`setBlock` injects.
3. Reduce the anchor-rebuild window / cache more aggressively — `ANCHOR_REACH` on the copy loop in
   `BackroomsChunkGenerator.applyBiomeDecoration` is what drives the 25×-per-chunk cost; the per-thread
   `BackroomsGenBuffer` LRU cache (`CACHE_SIZE = 8`) already avoids re-running an anchor already built
   in the same session, but confirm cache hits are actually landing before optimizing further.
4. **Critical: this needs the user to visually playtest each of the 6 levels after the fix.** RCON can
   only confirm "no void columns" (block presence) — it CANNOT verify a maze is connected, walls line
   up, or corridors make sense. A subtle determinism bug in the reseeded RNG chains could produce a
   gap-free but broken/disconnected maze that only shows up by walking through it. Don't consider this
   done until the user has walked all 6 levels.
5. If the buffered approach proves too fragile, the fallback discussed with the user was: leave the
   current write-zone-bypass gaps as a known cosmetic limitation (world is playable, just imperfect at
   room edges) rather than risk a broken maze.

### 14.8 Headless RCON probe harness (reusable technique, discovered/refined this session)

For verifying worldgen/block-state changes on this project without a display:

- **Keep `./gradlew runServer` alive for RCON** (it stops on stdin EOF otherwise): create a FIFO, hold it
  open with a `tail -f /dev/null` writer, and run the server itself as the **foreground** command of a
  harness-tracked background job so its process tree doesn't get reaped:
  ```
  mkfifo /tmp/ffc_stdin
  tail -f /dev/null > /tmp/ffc_stdin &
  # then the ONLY thing in the backgrounded shell is the server, in foreground of that job:
  ./gradlew runServer < /tmp/ffc_stdin > /tmp/ffc_server.log 2>&1
  ```
- Enable RCON in `run/server.properties` (gitignored, safe to edit): `enable-rcon=true`,
  `rcon.password=<anything>`.
- No `mcrcon` binary on this machine — a ~40-line dependency-free Python RCON client was written to the
  scratchpad (auth packet type 3, command packet type 2, standard length-prefixed framing) and is
  reusable for future sessions.
- Probe a block with `execute in <dim> if block X Y Z <id>` sent over RCON — the pass/fail comes back
  directly in the RCON response ("Test passed"/"Test failed"). **`run say ...` does NOT come back over
  RCON** (it only logs), so don't use that pattern for probing.
- `forceload add x1 z1 x2 z2` (scoped `execute in <dim> run ...`) forces chunks to generate/load for
  probing. Delete `world/dimensions/spb-revamped/<level>/{region,entities,poi,data}` to force a dimension
  to regenerate fully with new code (a `forceload` alone will reload but not regenerate already-saved
  chunks).
- Inspecting a structure `.nbt`'s block palette (to check what a room actually contains, e.g. whether it
  has light blocks) — they're gzipped NBT:
  `python3 -c "import gzip,re; print(sorted(set(re.findall(b'(?:spb-revamped|minecraft):[a-z0-9_/]+', gzip.open(f,'rb').read()))))"`.
- Decompiling 26.2 vanilla classes for exact bytecode-level behavior (e.g. what tint a `blit` overload
  hardcodes, what a property-identity check does) via `javap -p -c -s -cp <merged-deobf-jar> <FQN>` was
  used repeatedly this session and is faster/more reliable than guessing from method names alone.

### 14.9 Jar deployment note

Every fix in §14.1–14.6 (NOT §14.7, which is stashed/unreleased) is in the jar currently copied to
`/Users/ayanraj/Library/Application Support/ModrinthApp/profiles/mc backrooms/mods/
backrooms-ff-continued-2.0.0.jar` as of commit 737aad7. The user should start a fresh world (or delete
their save's `dimensions/spb-revamped/`) to get correct worldgen — existing saves have void chunks baked
in from before the fix.

## 15. Gaps-and-lights session, finished on Windows (2026-07-04/05)

Continues §14.7. The session started on the Mac (second attempt at the chunk-local rewrite, all changes
working-tree only), died to rate limits mid-verification, and was RESUMED ON THE USER'S WINDOWS MACHINE
(§13 note) where verification completed. Everything below is committed on `port/mc-26.2`.

### 15.1 The gaps fix — chunk-local rewrite, second attempt (WORKS; supersedes the §14.7 stash)

Same architecture as §14.7's attempt (deterministic generators + per-anchor in-memory buffer + copy out
only the current chunk's slice) with the two things that killed attempt #1 fixed:

- `BackroomsWorldGenRegionMixin` now intercepts the full surface `StructureTemplate.placeInWorld`
  touches while a buffer is active: `getBlockState`, **`getFluidState`** (the waterlogging check — the
  missing piece that caused attempt #1's 7M-warning flood), `getBlockEntity` (detached instances so
  template NBT/loot loads; transferred to the real BE on copy-out), `setBlock`, and both tick schedulers
  (`getBlockTicks`/`getFluidTicks` → `BlackholeTickAccess`, reached via waterlogged pool blocks).
- `BackroomsChunkGenerator.applyBiomeDecoration` splits per level into `generateAnchored(world, anchorPos)`
  (cross-chunk content, buffered, rebuilt identically by every overlapping chunk) + `generateLocal(world,
  chunk)` (strictly chunk-local, e.g. Level 0's 8x8 roof quadrants, runs after the slices are copied so it
  can read them). Per-generator `anchorReach`: L0/L1/InfGrass 2, L2 1, L324 3 (gas station).
- All 6 level generators + 3 maze generators + `MazeCell` fully deterministic: every `RandomSource.create()`
  seeded via `BackroomsGenBuffer.seed(world, x, z)` = world seed + **dimension id salt** + position.
  Level 0 megaroom adjacency ("first placed wins" via red-wool scan of neighbor regions, generation-order
  dependent) replaced by `hasEarlierMegaRoomNeighbor` — replays neighbor anchors' first seeded roll,
  lexicographic (z,x) priority, depth-capped recursion. The base class's `ImprovedNoise` sampler is now
  seeded from the world seed (was wall-clock — would have produced cross-restart seams).

**Bug found during verification (would have shipped broken): cross-dimension buffer-cache pollution.**
The per-worker-thread LRU cache was keyed by packed anchor `ChunkPos` alone; all Backrooms dimensions
share worker threads and use overlapping anchor coords, so one level's buffer got copied into another
level's chunks (RCON showed backrooms rooms inside Level 2's void, level-1/0 template air carving holes
into infinite_field's ground, gas-station slab interference). Fix: cache key = `(generator identity,
packed anchor)` record + the dimension salt in `seed()` above. Post-fix probes confirm each level clean
AND distinct.

### 15.2 The lights fix ("lights are just air on the roof")

`getRenderShape` on all three fluorescent-light blocks returned `RenderShape.INVISIBLE` whenever
`on=true && blackout=false` — the original drew lit tubes with a Veil block-entity renderer, dropped with
Veil, so every lit fixture rendered as a hole. Fix: always `RenderShape.MODEL`; lit look moved into
blockstates → new `*_on` model variants (`fluorescent_light_on`, `thin_…_on` multipart, `tiny_…_on`)
sharing a new emissive-looking `fluorescent_light_on.png` texture (`"shade": false`). Light emission
itself was already vanilla `lightLevel` — unchanged. Verified server-side (blocks present at roof y=25
where probes previously found air); the visual needs the user's client playtest.

### 15.3 Verification (headless RCON probe suite, fresh worldgen)

Method hardened vs §14.7: every probe position is gated on `execute if loaded` (unloaded positions
return "That position is not loaded" — NEVER "Test passed"/"failed" — a naive unless-block scan
misclassifies them), and a "hole" means a fully-air VOID COLUMN (air at y=quick AND all of
y∈{5,10,15,18,19,22,24,25,28,32}), which is the actual bug signature. Results on a fresh world:

- level0 spawn swath (-80..79)²: floor y=20 **0/1600** void, ceiling y=25 **0/1600** void.
- level0 distant swath (720..879)²: floor **0/1600** (12 columns air@20 = megaroom interiors, floors at
  y=18/19 — legit), ceiling **0/1600**. Exercises the megaroom-adjacency path far from spawn.
- level1 & poolrooms (-80..79)²: **0/1600** void each.
- level2: corridor is BY DESIGN only x=-1..2 (rooms are 4x5x16 along z; stairwell 17x16x16 at chunk 0,0)
  — corridor floor continuous, everything outside void (see §15.1 pollution bug for why it wasn't).
- infinite_field: uniform dirt ground plane y=30 (from `fillFromNoise`, chunk-local, never affected);
  clean post-fix. level324: slab y=0 + ceiling y=63 + road y=64 all `fillFromNoise` (unaffected); gas
  station template spot-checked.
- Lights: `spb-revamped:fluorescent_light` present in level0 roof plane y=25 (42 hits on a stride-2
  one-region scan of the final world; roof2 is the 1-in-5 roof variant carrying lights) and sampled
  state is `on=true` — the state that used to render INVISIBLE and now maps to the `_on` model.
- Server log across all of it: ZERO far-chunk drops / unsafe reads / generation exceptions (attempt #1
  produced ~7M).

### 15.4 Windows build/verify equivalents (§4/§5 are macOS-historical)

- **JDK 25:** `C:\Program Files\Eclipse Adoptium\jdk-25.0.3.9-hotspot` (winget Temurin; system Java is 22).
  `$env:JAVA_HOME = <that>; .\gradlew.bat build` → `build\libs\backrooms-ff-continued-2.0.0.jar`.
- **Headless server:** Windows has no FIFO trick — hold stdin open via a Python wrapper
  (`subprocess.Popen([...gradlew.bat, "runServer"], stdin=PIPE)`, never write/close; kill wrapper to stop,
  or send `stop` over RCON). RCON already enabled in `run/server.properties` (`probe123`@25575). Python
  3.12 is on PATH; the dependency-free RCON client + probe scripts pattern is in §14.8/§15.3.
- Wipe `run\world\dimensions\spb-revamped\` before probing so chunks regenerate with new code.
- The user has NO 26.2 launcher profile on this machine yet — hand them the jar path; deps at runtime:
  fabric-api 0.153.0+26.2, geckolib 5.5.3+, simple voice chat (JIJ'd libs: CCA, MidnightLib bundled).

### 15.5 Benign boot warnings (do not chase)

`FabricLoader/Mixin` WARNs at boot: 2× SoundPhysics (`ReflectivityConfig`, `SoundTypes`) — @Pseudo
mixins for the absent soft-dep; 1× `cardinal-components-entity` `BucketableMixin` targeting
`net.minecraft.world.entity.animal.Bucketable` (moved in 26.2) — a CCA-library mixin whose only job is
copying components on fish bucketing; this mod attaches components to players/SkinWalker/Smiler/world
only. All three are skip-and-continue warnings with zero impact.

### 15.6 Remaining for the user (unchanged expectations)

1. **Visual playtest of all 6 levels on a FRESH world** — RCON proves block presence, not that mazes
   read right or lights look right (§14.7 caveat 4 still stands).
2. §11 cosmetic items (translucency-bake warnings, `stuff=5` blockstate, PBR fallbacks, bird/grass draw).

## 16. Second Windows playtest round (2026-07-05, commits 81c721f, 99ee142)

User actually ran the jar from §15 and reported two more issues, same fix→build→commit loop as §14.
Both root-caused (not guessed) and fixed; both build-verified (headless can't verify client visuals).

### 16.1 Camera shake still way too violent, "hurts my head" (commit 81c721f)

The §14.3 fix (`a8bbfee`, `walkAnimation.speed(1.0f) * 2`) wasn't actually correct — re-derived the unit
conversion properly this time by decompiling 26.2's `WalkAnimationState` (`javap` on the merged-deobf
jar, see §5): vanilla feeds the walk animation `min(distance * 4, 1.0)` per tick, while the ORIGINAL
1.20.1 mod's shake-speed proxy was `distance * 6`. So the faithful port multiplier is `speed(1.0f) * 1.5`
(reproduces the original's input up to the point `speed()` saturates at 1.0, i.e. below sprint) — the
`§14.3 * 2` was ~35% hotter than the original mod at walking speed (±3.2° pitch/yaw + ±6.4° roll
oscillating several times a second, mostly from the `* 2` roll term).

**But even the original mod's shake intensity turned out to be too much for this user** (motion-sickness
territory, not just "a bug"). Rather than re-guess a magic number, added a proper escape hatch:

- `CameraShake.java`: multiplier corrected to `* 1.5`; `amplitude` restored to the original mod's `4`
  (was trimmed to `3` in §14.3) but now scaled by a new config value:
  `this.amplitude = 4 * ConfigStuff.cameraShakeIntensity;`
- `ConfigStuff.java`: new slider `cameraShakeIntensity` (video category, 0..1, default **0.4**, one
  decimal precision). `0` = shake fully disabled, `1.0` = original mod's full handheld-camcorder feel.
- `en_us.json`: added the ModMenu label `spb-revamped.midnightconfig.cameraShakeIntensity` = "Camera
  Shake Intensity" (video settings, alongside the existing roll-multiplier sliders).

Live-tunable in ModMenu → Backrooms FF Continued → Video Settings, no rebuild needed if 0.4 is still
too much or too little — tell the user to try the slider before requesting another code change.

### 16.2 Pink/black checkerboard ("missing model") blocks — Level 1 pipes, Level 0/Poolrooms slopes (commit 99ee142)

User sent 3 screenshots: a giant checkerboard cube on a Level 0 floor F3 debug shot, four
floor-to-ceiling checkerboard columns in what turned out to be Level 1, and a checkerboard diagonal
strip next to a pool in Poolrooms. All three are the SAME bug, not three different ones.

**Root cause (static analysis, no server needed — this is a resource-file bug):** wrote a Python
blockstate→model→texture graph walker (scratchpad `validate_assets.py`) that found the full asset
chain resolves fine (no missing files, nothing atlas-filtered) — so the breakage had to be inside a
model that fails to BAKE, matching the §11 "Cannot compute translucency out of bounds" log lines that
were filed as "cosmetic" before anyone had screenshots to prove what they meant. Wrote a second script
(`scan_all_models.py`) that parses every block model's `elements[].faces[].uv` and flags anything
outside Minecraft's legal `[0,16]` UV range. Found 64 out-of-range faces across 9 models:
`big_pipe`, `big_pipe_middle` (Level 1's checkerboard columns — literally the pipe models), `slope`
(Level 0/Poolrooms diagonal strips), `thin_pipe_corner`, `power_pole`, `shelf_stuff1`, `shelf_stuff2`,
`double_sided_shelf_stuff1`, `double_sided_shelf_stuff4` — some UVs as far out as 48, some just
fractionally over 16 (e.g. `16.0373`, `16.5`).

These models are Blockbench exports; 1.20.1 silently wrapped/clamped out-of-range UVs when sampling,
but 26.2's model baker validates the range and REJECTS the whole model if any face is out of bounds —
every blockstate variant that references a rejected model falls back to the pink/black "missing model"
placeholder cube, which is exactly what the screenshots show.

**Fix (scratchpad `fix_uvs.py`):** normalized all 64 faces into `[0,16]`, in three passes per axis pair
in order: (1) modulo-16 shift (`u -= floor(min(u)/16)*16`) — moves e.g. `28..34` to `10..16` and
preserves which texels get sampled, since the intended source rect just happened to be authored past
the sprite's tile boundary; (2) if a boundary-straddling span still pokes past 16 after the shift (e.g.
`12.5..16.5`), nudge the whole pair down so the max lands exactly on 16; (3) spans still too wide are
clamped to the sprite edge (didn't actually trigger for any of the 64 — no model needed it). In-bounds
faces on these same models were left untouched. `texture_size` fields present on these models are
vestigial Blockbench UI metadata that vanilla Minecraft ignores — confirmed only a minority of faces
were out of range (not a systematic scale mismatch), so a wholesale rescale was not the right fix.

Verified: `scan_all_models.py` re-run finds zero remaining out-of-range faces project-wide;
`validate_assets.py` (full blockstate→model→texture resolution, including atlas-filter checks) reports
zero problems. `./gradlew build` green. **Not RCON-verifiable** — model baking is client-side only; the
user needs to confirm in their next playtest that the checkerboards are gone and the pipe/slope/shelf
textures look correctly oriented (a UV nudge can leave a face sampling a *slightly* wrong crop of the
texture even once it's back in-bounds — flag any stretched/misaligned texture on these specific blocks
if seen).

### 16.3 Scratchpad tooling worth reusing next session

Three standalone Python scripts (session scratchpad, not committed to the repo — recreate if needed):
- `validate_assets.py` — walks every `assets/spb-revamped/blockstates/*.json`, resolves variant/multipart
  model refs, follows model `parent` chains, checks every referenced texture file exists AND isn't
  stripped by an `assets/minecraft/atlases/blocks.json` filter. Static, no server/client needed.
- `scan_all_models.py` — flags any block model face with an explicit UV outside `[0,16]`, or (separately)
  an auto-UV (no explicit `uv` key) implied by element geometry outside `[0,16]`. Also dumps a level's
  `.nbt` structure palette (which mod blocks actually appear in a level) via the gzip+regex trick from
  §14.8.
- `fix_uvs.py` — the modulo-shift UV normalizer described in §16.2; parameterized by a file list, easy
  to point at new offending models if more checkerboard reports come in.

Both validators are static — no build/server round-trip — so run them FIRST on any future "missing
texture" or "pink/black block" report before touching the server. If they report 0 problems (as they
did for the pre-16.2 asset tree), the bug is in model BAKING (out-of-range UVs, bad `texture_size`,
malformed `rotation`), not in file references.
</content>
