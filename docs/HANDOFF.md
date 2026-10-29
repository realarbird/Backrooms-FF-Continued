# Backrooms FF Continued — Port Handoff (resume-from-here)

**Purpose:** self-contained state dump so a fresh chat/session can continue this port with zero prior
context. Read this + the four reference docs (§9) + `git log --oneline` and you're caught up.

---

## 1. What this is

Porting the Fabric mod **SP-Backrooms Revamped** ("Minecraft Found Footage", by SpacePotato & Herr
Chaos, LGPL-3.0) from **Minecraft 1.20.1 → 26.2** and making it **run on macOS**. Rebranded to display
name **"Backrooms FF Continued"**, author **ARBird** (the user). Internal mod id kept as `spb-revamped`.

> **The target platform (macOS, MC 26.2) has NOT changed.** The *development/build machine* moved to
> Windows on 2026-07-04 (§13) for a few sessions, then **moved BACK to macOS as of 2026-07-08** — the
> assistant is once again compiling/verifying on the same Mac the user plays on (project dir
> `/Users/ayanraj/Documents/MinecraftFoundFootage-master`, JDK 25 at
> `/opt/homebrew/Cellar/openjdk/25.0.1/...` — the system default `jdk-24` is too old, must set
> `JAVA_HOME` per-command or `./gradlew` fails with "release version 25 not supported"). This has never
> been more than a dev-environment detail: nothing in the goal, the Mac-safe rendering approach, or the
> "no Veil/GLSL" constraint changes because of which machine builds it (Fabric/Minecraft are
> cross-platform; nothing in the source is Windows- or macOS-specific). The §13/§15.4 Windows paths/
> commands are now historical (kept for context) — use the macOS ones (§4/§5) again.

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

> **⚠ UPDATE 2026-07-06 (§17/§18): the above is now stale — player-spawn was confirmed working long ago
> and the project moved through the full shader-restoration arc.** Current real status: the original
> Veil-era shader LOOK has been rebuilt on the vanilla 26.2 post pipeline (§17) and then actually fixed
> and visually verified level-by-level on this Windows machine (§18) — SSAO, god rays, per-level color
> grading, bloom, fog, and sky are all live and screenshot-confirmed for all 6 levels (level0, level1,
> level2, poolrooms, infinite_field, level324). Several more runtime crashes were found and fixed along
> the way (§18.3). **The only remaining gate is the user's own macOS playtest** — everything above was
> verified on the Windows dev GPU (Intel UHD 620) only; see §18.5 for what to watch for and §18.4 for the
> reusable visual-verification harness (headless screenshot hook + RCON level-tour) if another session
> needs to keep iterating without the user's machine.

> **⚠ UPDATE 2026-07-08 (§19-§24): also stale.** The Windows-only caveat above no longer applies — the
> dev machine moved back to macOS (§1/§13 callouts) and several sessions of macOS playtesting happened
> since: per-pixel lighting, poolrooms water/grass rewrites (§19-§23), and a skinwalker-capture crash +
> missing-texture fix (§24, GeckoLib 4→5 resource-root migration the port had missed). §24 is the most
> recent fix and is deployed to the user's live Lunar Client jar but **not yet confirmed by an actual
> in-game capture** — that's the next thing to check.

## 3. Locked decisions (do not relitigate)

- Keep internal modid `spb-revamped` + `com.sp` package (renaming cascades through 333 resource files).
- Keep ALL compat (incl. Sound Physics Remastered) — guarded as soft/compile-only deps.
- Mac-safe **approximations** for visuals (HUD overlays + vanilla `Blocks.LIGHT`, zero GLSL). Fidelity is
  "close, not pixel-identical." Effects that fundamentally need GLSL are dropped, documented (§10).
  **⚠ Amended 2026-07-05 (§17): "zero GLSL" is superseded at the user's explicit request** — the
  original shader look is restored via the VANILLA 26.2 post pipeline (data-driven `post_effect`
  JSON + GLSL 330 post shaders), which is the same pipeline vanilla itself runs on macOS. Still
  Mac-safe, still no Veil. "No Veil / no compute / no geometry shaders" is what remains locked.
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
   **UPDATE (resolved):** confirmed working back in the §14 client-playtest session; superseded entirely
   by the shader-restoration arc (§17/§18) — see §2's 2026-07-06 update note for the real current status.
2. **Cosmetic model/texture issues** seen in the client log (non-fatal):
   - `Cannot compute translucency out of bounds` baking `slope`, `one_sided_shelf`, `double_sided_shelf`,
     `thin_pipe_corner` — model UV/atlas issues; those blocks may show missing-texture.
     **UPDATE (§16.2, resolved):** root-caused as 64 out-of-range model UVs across 9 models (26.2's model
     baker rejects any face outside `[0,16]`, unlike 1.20.1's silent clamp); fixed via a UV normalizer
     script. `stuff=5` blockstate warning below is a separate, still-open issue.
   - `one_sided_shelf … stuff=5` blockstate variant references a value outside the property range `[0,4]`
     — fix the blockstate JSON (or the property). **Still open per §18.5.**
   - `Missing textures … block/pbr/<x>_color` — PBR color textures dropped with Veil; affected blocks fall
     back to base textures. Either supply plain textures or repoint those models. **Still open per §18.5.**
   - `Duplicate sprite …` warnings are vanilla (items+blocks atlas overlap), harmless.
3. **Bird/grass not drawn** — decide: implement a vanilla submit-draw or finalize as dropped. **Still open
   per §18.5** (not touched by the §17/§18 shader work, which is post-processing only).
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

> **⚠ DEV MACHINE MOVED BACK TO macOS (2026-07-08).** The 2026-07-04 move to the user's Windows machine
> (`C:\Users\rajay\Documents\MinecraftFoundFootage-master-20260705T011342Z-3-001\MinecraftFoundFootage-master`)
> was **temporary** — the assistant is back to building/verifying on the same Mac
> (`/Users/ayanraj/Documents/MinecraftFoundFootage-master`) the user plays on. This is still just a
> dev-environment detail — see §1's callout. **§4/§5's macOS paths/commands are live again**; §15.4's
> Windows equivalents (and the Windows path above) are now historical, kept for context in case the
> machine moves again. One live gotcha on this Mac: the system `javap`/default JDK is **24**, not 25 —
> gradle needs `JAVA_HOME=/opt/homebrew/Cellar/openjdk/25.0.1/libexec/openjdk.jdk/Contents/Home` (or
> equivalent JDK 25 install) exported before `./gradlew`, else compile fails with "release version 25
> not supported". Everything else (branch, decisions, docs, the actual porting goal) unchanged.

1. Read this file, then `docs/PORTING_MAP.md` + `docs/CLIENT_PLAYTEST.md`, then `git log --oneline | head -40`.
2. Confirm green (Windows): set `JAVA_HOME` to JDK 25 (`C:\Program Files\Eclipse Adoptium\jdk-25.0.3.9-hotspot`)
   and run `gradlew.bat build` in the project dir.
3. Ask the user for their latest client result / crash trace, or address the §11 cosmetic items.
4. Fix → rebuild → give the user the jar from `build\libs` (no launcher profile exists on the Windows
   machine yet — the old auto-deploy path was the macOS Modrinth profile). Commit each fix (no push).

> **⚠ UPDATE 2026-07-06:** steps 1-4 above are the generic loop and still apply, but as of §17/§18 the
> project is past load-blocking bugs — it's shader/visual polish. If the user hasn't reported back yet,
> **don't wait idle**: re-run the §18.4 visual-verification harness yourself (dedicated server + dev
> client + RCON level-tour + the file-flag screenshot hook) to keep iterating without needing the user's
> Mac. Only macOS-specific GL driver quirks (if any) require the user's actual hardware.

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

## 17. Shader restoration — the original Backrooms look on the vanilla post pipeline (2026-07-05)

**User request: bring back the original mod's shader look** (reference: the Veil-era visuals, e.g.
youtu.be/XnO1GAlR3qo) — the plain-vanilla rendering after the Phase-6 Veil removal was "boring".
This supersedes §3's "zero GLSL" (amended there): the restoration uses **vanilla 26.2's data-driven
post-effect pipeline** (`post_effect/*.json` + GLSL 330 post shaders + `PostChain`), which macOS runs
fine — it's what vanilla's own spectator/menu-blur shaders use. No Veil, no compute/geometry shaders.
The original Veil GLSL sources were recovered from git history (`git show 7379952^` — Phase-6a purge
commit's parent) and ported pass-by-pass.

### 17.1 What was restored (and what stayed dropped)

Restored, per original source file:
- **VHS camcorder post** (`vhs/vhs_post.fsh` + combine half of `vhs/bloom.fsh`): barrel distortion
  (`VHSDistortionMultiplier` config), chromatic aberration, YUV tape noise + luma sparkle + chroma
  bleed off `vhs_noise.png`. → `shaders/post/backrooms_vhs.fsh`.
- **Bloom** (`vhs/highlights.fsh`/`highlight_blur.fsh` pyramid): re-architected as soft-knee luminance
  extract (`backrooms_bright.fsh`, knee 0.72–0.95 — vanilla main target is LDR; the original
  thresholded ≥1.0 against Veil's HDR buffers) + two `minecraft:post/box_blur` passes (vanilla shader
  reused) + additive combine in the vhs pass. New config `enableBloom`.
- **Level 1 knee fog** (`vhs/everything.fsh` `LEVEL1_FOG` raymarch): verbatim port incl. `super_noise`
  3D-ish noise, y=21.75 plane, 50-block cap. Gated by existing `enableLevel1Fog` config.
- **Per-level distance fog** (`vhs/mixed.fsh`): infinite_field linear(-10..90, 0.8 grey);
  level324 linear(-10..90, black) + solid-black sky. New config `enableLevelFog`. Level 0 gets a NEW
  soft warm exponential haze (approximation for the dropped deferred-light distance darkening — not
  in the original chain; tune constants in `BackroomsPostEffects` if the user wants it different).
- **Poolrooms procedural sky** (`include/sky.glsl` + `mixed.fsh` sun/stars): clouds (puddle_noise ×
  clouds2), day-cycle palette off `PoolroomsDayCycle.dayTime`, sun/moon disc from `getSunAngle()`,
  night stars. Applied at depth==1 pixels only (the original keyed windows off Veil material ids —
  gone; actual sky pixels only is the approximation). Also on in infinite_field (fixed morning).
- **Glitch** (`vhs/glitch.fsh`): verbatim port as the final pass; `glitchTime` fed per frame from
  `PlayerComponent.getGlitchTimer()` (skinwalker), 0 = passthrough.

Still dropped (Veil-deferred-only, documented in §10/MAC_SAFE_RENDERING.md): SSAO, shadow map +
volumetric light, puddles, water/block reflections, PBR, motion blur (needs motion vectors),
warp_solid vertex warp, distorted_entity, respawn static. Jumpscare faces + youCantEscape + blackScreen
stay on the HUD overlay (draws after the chain anyway).

### 17.2 Architecture (all new files)

- `assets/spb-revamped/post_effect/backrooms.json` — 6 passes: scene (fog+sky) → bright → box_blur ×2
  (ping-pong bright/temp) → vhs (bloom combine + tape look) → glitch → `minecraft:main`. Noise
  textures ride as `TextureInput`s (location+width+height+bilinear MUST match the PNGs).
- `assets/spb-revamped/shaders/post/backrooms_{scene,bright,vhs,glitch}.fsh` +
  `shaders/include/spb_post.glsl` (hash/yuv helpers + the `SpbDynamics` UBO + depth→view-pos
  reconstruction). Vertex shader is vanilla `minecraft:core/screenquad`. `#moj_import
  <minecraft:globals.glsl>` provides GameTime/ScreenSize/CameraBlockPos+CameraOffset (camera world
  pos = blockPos + offset). GLSL sticks to #version 330 (mac ceiling GL4.1; vanilla targets 3.3).
- `com.sp.render.post.BackroomsPostEffects` — per-frame driver, called from
  `mixin/render/GameRendererPostFxMixin` at `GameRenderer.renderLevel` HEAD (CameraRenderState is
  extracted by then; `render()` processes `postEffectId` right after renderLevel returns → same-frame
  matrices). Activates via the access-widened private `GameRenderer.setPostEffect(Identifier)` when
  `shouldRenderCameraEffect()`; clears only if the current effect is ours (won't stomp vanilla
  spectator shaders); also cleared on DISCONNECT (SPBRevampedClient) so it can't linger on the title
  screen.
- **Dynamic uniforms trick** (the key 26.2 mechanism): vanilla `PostPass` bakes JSON uniform blocks
  into `GpuBuffer`s with usage `USAGE_UNIFORM` only (immutable). `customUniforms` is a mutable
  HashMap bound by block name each frame (`RenderPass.setUniform`). `BackroomsPostEffects` detects
  the read-only buffer via `usage() & USAGE_COPY_DST == 0`, replaces the map entry with a writable
  (`UNIFORM|COPY_DST`) buffer, then `CommandEncoder.writeToBuffer`s a `Std140Builder` blob every
  frame (same pattern as vanilla `GlobalSettingsUniform`). Resource reloads rebuild the chain →
  usage check re-swaps automatically. Accessors: `mixin/render/PostChainAccessor` (`passes`),
  `PostPassAccessor` (`customUniforms`).
- **`SpbDynamics` std140 block — three places must stay in sync** (order+types): the GLSL block in
  `spb_post.glsl`, the JSON default lists in `backrooms.json` (every pass that declares it), and
  `BackroomsPostEffects#writeDynamics`. Layout: 2×mat4 (invProj, invViewRot) + 4×vec4
  (fogColor+mode / fogStart,end,expDensity,skyMode / sunsetTimer,kneeFog,glitchTime,distortion /
  sunDir+bloomStrength) = 192 bytes.
- HUD dedup: `BackroomsHudOverlay.postShaderActive` (set by the driver) suppresses the Phase-6c
  grain/scanlines/chromatic-fringe fills while the real chain runs (vignette + REC UI + jumpscare
  stay). If the chain fails to compile on some machine, `getPostChain` returns null → flag stays
  false → the old HUD approximation is the automatic fallback.
- Config: `enableBloom`, `enableLevelFog` (new, SHADERS category) + existing `enableVhsEffect`,
  `enableVhsEffectInTheBackrooms`, `VHSDistortionMultiplier`, `enableLevel1Fog` now all do real work.
  Lang keys added to `en_us.json`.
- AW addition: `accessible method net/minecraft/client/renderer/GameRenderer setPostEffect
  (Lnet/minecraft/resources/Identifier;)V`.

### 17.3 Verification state + dev-loop notes

- `gradlew.bat build` green.
- Windows dev machine CAN run the client: `runClient` + a gradle init-script that appends
  `--quickPlaySingleplayer <save>` args (Loom's RunGameTask is a JavaExec; `t.args(...)` appends).
  The server world from the RCON sessions was copied `run/world` → `run/saves/shadertest` (delete
  `session.lock`) and loads straight into overworld, which activates the chain (VHS is on in ALL
  dimensions when `enableVhsEffect`=true) — GLSL compile errors surface in the client log as
  `ShaderManager`/`CompilationException` lines. This is the shader smoke-test loop; visual look
  still needs the user (macOS).
- GLSL gotchas already handled: post-chain texture inputs aren't guaranteed REPEAT addressing —
  every unbounded UV is `fract()`-wrapped in-shader; `SamplerInfo` is NOT declared in our shaders
  (optional — vanilla transparency.fsh precedent) — resolution comes from Globals' ScreenSize if
  ever needed; render-target inputs are clamp-to-edge (barrel-distortion edge smear matches the
  original's behavior).

## 18. Shader look finished + visually verified on all 6 levels (2026-07-06)

User asked to "make the shaders look impressive, like the screenshot" (the original mod's per-level
gallery: L0 warm yellow/dark falloff, L1 grey garage haze, L2 dark corridors, sunlit teal Poolrooms,
dark Darkrooms, bright hazy Infinite Field). Everything below is committed; **all six levels were
screenshot-verified live on this machine** (see §18.4 for the harness — a real visual loop, not
just compile checks).

### 18.1 Two §17 bugs that made the whole chain silently do (almost) nothing

Both found by probing a live client (depth-band debug shaders + a JSON-default "red fog" A/B test):

1. **26.2 renders REVERSED-Z** (`GL_ARB_clip_control` zero-to-one; depth cleared to **0.0**,
   `javap`-confirmed via `CommandEncoder.clearDepthTexture(main, 0.0)`). §17's
   `spbViewPosFromDepth` did the classic `ndc.z = depth*2-1` and tested sky as `depth >= 1.0` —
   depth-driven everything (fog distances, sky replacement) computed garbage. Fixed in
   `spb_post.glsl`: sky test is `depth <= 1e-6` (`spbIsSky`), reconstruction is the closed form
   `viewZ = -0.05/depth` using only the projection diagonal (identical between standard and
   reversed-Z matrix flavors, so it can't regress if the render-state matrix flavor changes).
2. **`GameRenderer`'s postEffect slot receives a CLEARED depth buffer**: vanilla clears main depth
   (to 0, reversed-Z) for the hand/HUD 3D pass BEFORE `postEffectId` is processed. Any depth
   sampling there reads void. The chain now runs inside the LEVEL frame graph instead — new
   `mixin/render/LevelRendererPostFxMixin` (WrapOperation on `addAlwaysOnTopPass`, right where
   vanilla runs its entity-outline post chain) calls `BackroomsPostEffects.addToLevelFrame(builder,
   targets)`. `onFrameStart` (GameRendererPostFxMixin, unchanged hook point) now only writes
   uniforms + sets an active flag; `setPostEffect` is no longer used. Side benefit: the hand renders
   AFTER the chain, matching the original Veil composite (it overlaid the hand separately).

### 18.2 What the chain now does per level (BackroomsPostEffects + backrooms_scene.fsh)

- **New passes/effects:** depth-only SSAO (10-tap spiral, derivative normals, skips hand-range
  pixels; the Veil chain's `color *= blur(SSAO) * 2.0` equivalent), screen-space god rays
  (luma-masked march toward the projected sun — Poolrooms/InfField stand-in for the dropped
  volumetric light; sun screen pos computed Java-side, fades at screen edge/night), per-level
  color grade (exposure → tint → saturation → smoothstep S-curve → lift), banding dither, wider
  two-stage bloom (12px + 24px box-blur pairs).
- **SpbDynamics grew to 2 mat4 + 7 vec4 (240 bytes)** — `SpbGradeA` (tint+saturation), `SpbGradeB`
  (sCurve, lift, AO strength, ray strength), `SpbSunScreen` (sun uv, on-screen weight, exposure).
  Sync in THREE places: spb_post.glsl block, backrooms.json defaults (x3 passes), writeDynamics.
- **Per-level values** (in the dimension branch of `onFrameStart`): L0 warm yellow
  tint/sat 1.12/sCurve .32/AO .75/exposure 1.9/exp fog to near-black warm; L1 cool desat + knee fog
  (accum boosted 0.001→0.0035/step — LDR reads much thinner than Veil HDR) + grey exp haze,
  exposure 1.55; L2 (was: nothing) dark cold exp fog, sCurve .38, AO .85, exposure 1.6; Poolrooms
  warm sat grade, bloom .5, god rays .55, exposure 1.15; InfField lifted-black hazy grade, rays
  .35, fog+sky haze mix; L324 desat night grade. Non-Backrooms dims: neutral film grade, AO .45.
- **Sky (getSky) LDR retunes:** sun halo smoothstep(0.9,1)*0.3 (was 0.7,1 *0.6 — blew out half the
  sky), cloud alpha threshold *3.5 (was *5), `spbMirror()` mirror-repeat wrap for clamp-addressed
  noise samplers (plain `fract()` drew giant polygon seam lines across the sky), cloud UVs scaled
  *0.25 so the mirror folds mostly stay off the dome, sky clamped, and skyMode-1 skies mix 40%
  toward the level's fog color (horizon haze, kills the fog/sky seam).
- **Poolrooms water is teal-green** now (reference look): biome `water_color` 0x3FC6A0 /
  `water_fog_color` 0x0E4A3E (the original's green came from the dropped Veil water shader).

### 18.3 Non-shader bugs found live (all fixed + committed)

1. **Dedicated-server crash on lightning tick**: `rain.LightningSkyLightingUpLightningEntity`
   (common mixins) referenced client-only `Minecraft` in its `tick` wrap → "Cannot load class
   net.minecraft.client.Minecraft in environment type SERVER" the moment a bolt ticked with a
   player online (RCON-only sessions never saw it). Split: sky-flash tick wrap moved to new
   `rain.LightningSkyFlashClientMixin` (client list); `spawnFire` suppression stays common.
2. **Blackout event permanently kills Level 0's lights**: `FluorescentLightBlockEntity.tick` set
   `BLACKOUT=true` during the event but never cleared it (the thin variant did) — every full
   fixture that ever saw a blackout stayed `lightLevel 0` forever. This world had that baked in —
   level0 probed `on=true,blackout=true` everywhere, pitch black. Added the else-branch clearing
   BLACKOUT when the level light state returns to ON; re-trigger `/backroomsevent blackout` once
   in an affected world to heal it (event finish → BE ticks flip the state back).
3. **Client crash from tiny fluorescent lights** (Level 324 gas station): two more §14.5-class
   property-identity bugs — `ClientWrapper.doClientSideTinyFluorescentsTick` and
   `TinyFluorescentLightSoundInstance.tick` read `ThinFluorescentLightBlock.COPY/ON/BLACKOUT` off
   TINY light states. Both now use `TinyFluorescentLightBlock.*`. A sweep of
   `getValue((Thin|Tiny)?FluorescentLightBlock.` is now clean; re-run that grep if this crash
   class ever reappears.
4. **Per-level time of day**: 26.2 replaced `fixed_time` with clocks/timelines — the day/night
   lightmap is keyframed by the `minecraft:day` timeline (tag `#minecraft:in_overworld`) off the
   GLOBAL overworld clock, so the field/poolrooms went night when the overworld did (the original
   shipped that way too; Veil masked it). dimension_type fix: `timelines: "#minecraft:universal"`
   (no day cycle) + explicit `minecraft:visual/sky_light_factor` attribute — 1.0 for
   infinite_field/poolrooms (eternal day; poolrooms events still tint via the lightmap mixin),
   0.0 for level324 (eternal night under the forced rain). Verified decoupled: overworld at
   night, field bright.

### 18.4 The visual-verification harness this session built (REUSE THIS)

- **Loop:** dedicated server (`run_server.py` stdin-holder, §15.4) + dev client via
  `runClient -I quickplay.init.gradle` (`--quickPlayMultiplayer 127.0.0.1:25565`; server.properties
  needs `online-mode=false`, `difficulty=peaceful` — random dev usernames = fresh player each
  launch, and a husk WILL kill them at spawn otherwise). Drive everything over RCON:
  `gamemode creative @a`, `execute in <dim> run tp @a x y z yaw pitch`, `/backroomsevent ...`.
- **In-game screenshots survive a LOCKED Windows session** (glReadPixels doesn't care): dev-only
  file-flag hook in `SPBRevampedClient` END_CLIENT_TICK — `touch run/spb_screenshot.flag` → PNG in
  `run/screenshots/` (guarded by `isDevelopmentEnvironment()`, ships inert). Desktop screen capture
  (`shot_inline.ps1` window-rect CopyFromScreen) only works unlocked; clicking buttons (death
  screen Respawn at ~(0.5w, 0.585h)) needs unlocked + SetForegroundWindow.
- Level tour coords: L0 lights near spawn at (-6,25,18)/(-2,25,22) — stand (-4.5,21,14.5) yaw 0;
  L1 (8.5,21,8.5); L2 corridor (0.5,20,8.5) yaw 0; Poolrooms spawn (16.5,106,16.5) — the y=21 area
  is a deep zone, not the main rooms; InfField (0.5,31,0.5); L324 road view (20.5,66,45.5) yaw
  -140 (gas station), interior tp (52,65,21) is inside a wall.
- Poolrooms day cycle: advance with `/backroomsevent sunset` (+0.25 timeOfDay each, 8s lerp,
  event finish wraps ≥1.0→0). This world sat at midnight for a while — that's the §17-era
  "purple poolrooms" mystery, not a shader bug.
- `javap` on the merged-deobf jar remains the fastest ground truth (PostPass rebinding semantics,
  ShaderManager chain caching, clearDepthTexture(0.0), Projection.setPerspective flavors, the
  26.2 dimension_type/timeline schema — all resolved that way this session).

### 18.5 Still open (cosmetic, next session)

- InfField sky: soft haze masses read well but faint angular mirror-folds remain at some view
  angles; if it bothers the user, options are a dedicated seamless-noise texture or accepting it.
- §11 leftovers unchanged: PBR fallback textures, bird/grass draw, `stuff=5` blockstate warning.
- The **user's macOS playtest** is still the real acceptance test: everything above was verified
  on the Windows dev GPU (Intel UHD 620); Apple's GL driver may differ (the fallback path — chain
  fails to load → HUD approximation — is intact if it does).

## 19. Smooth per-pixel lighting + natural poolrooms water + defaults/hunger (2026-07-07)

User asked to: (a) make all lighting smooth instead of the per-block vanilla lightmap look
(fixtures AND flashlight); (b) make poolrooms water look natural (not vanilla water); (c) grass in
the Infinite Field per the reference gallery; (d) change two config defaults; (e) never lose hunger
in the backrooms. All done on `master` (uncommitted — no push yet), `gradlew build` green (jar in
`build\libs`), and **Level 0, Level 1, Level 2, Poolrooms water, the flashlight cone, and the new
grass were screenshot-verified live** via the §18.4 harness (dedicated server + `runClient`
quickplay + RCON tour + the file-flag screenshot hook). **Level 324 was NOT toured this session**
(the relight code path covers it identically to L0/1/2 — same `isDeferredDimension` set — but it
was never visually spot-checked; see §19.5).

### 19.1 Deferred-style per-pixel relight (the core "smooth, not per-block" fix)

The per-block banding came from the vanilla 15-step lightmap. Rebuilt as a deferred relight *inside
the existing `backrooms_scene.fsh` post pass* (no new pass; same Mac-safe vanilla PostChain):

- **`mixin/lightmap/BackroomsFullbrightLightmap`** (new client mixin, TAIL of
  `LightmapRenderStateExtractor.extract`): while the relight is active it forces
  `ambientColor = white` + `darknessEffectScale = 0`, so the world renders as pure ALBEDO and the
  lightmap contributes no banding. Gated on `SpbLightGatherer.deferredLightingActive`; off ⇒
  vanilla lighting untouched (automatic fallback).
- **`render/post/SpbLightGatherer`** (new): once per client tick, walks the BE maps of the loaded
  chunks around the camera (every light fixture is a BlockEntity — no block scan) and builds a list
  of world-space point lights (fluorescent full/thin/tiny + ceiling + emergency, honoring
  ON/BLACKOUT state; Smiler face carries a small light). `isDeferredDimension` = Level 0/1/2/324
  only (the sky-lit levels keep vanilla lighting). `MAX_POINT_LIGHTS = 64`, distance-sorted cull.
- **`backrooms_scene.fsh`** new `SpbLights` std140 UBO (block name is how vanilla binds it):
  meta + ambient floor + `SpbSpot*[8]` (flashlight cones) + `SpbLightPos/Color[64]`. `spbRelight()`
  = ambient floor + Σ point lights (windowed inverse-square falloff `(1-x)²/(1+6x²)` for moody pools
  + wrapped diffuse + fixture self-glow) + Σ spot cones, clamped to 1.5, multiplied onto albedo.
  Positions are camera-relative (light.world − cam.pos), matched in-shader to
  `mat3(SpbInvViewRotMat)*viewPos`. Normal is depth-derivative (shared with SSAO).
- **`BackroomsPostEffects#writeLights`** builds+uploads `SpbLights` each frame (same read-only→
  writable buffer swap trick as `SpbDynamics`, §17.2). Per-level ambient floor + exposure retune
  in the deferred branch of `onFrameStart`. The `SpbLights` default array (154 vec4) is declared in
  `post_effect/backrooms.json` on the scene pass so vanilla bakes the correctly-sized bound buffer.

### 19.2 Flashlight, water, non-deferred dims

- **Flashlight is now a smooth shader spot cone** (not the vanilla `Blocks.LIGHT` block, which put
  the blocky falloff back). `FlashlightRenderer` early-outs while `deferredLightingActive`. The local
  player's cone rides the camera (origin 0, dir = camera forward); other players' cones off their
  eyes. `spbSpotTerm`: smoothstep inner/outer cone (~9°/24°), range 28, near-ease so wall-hugging
  doesn't white out. Verified in Level 2 (soft circular beam down the dark corridor).
- **Non-deferred dims (Poolrooms, Infinite Field, overworld)** keep vanilla block lighting but get:
  `spbDeband()` (ring-samples + luma-windowed average to smooth lightmap steps) + `spbConeAssist()`
  (multiplicative flashlight cone shaping on top of the vanilla light block).
- **Poolrooms water `spbWater()`**: heuristic (up-facing normal + teal albedo) → animated
  refraction re-sample (hides the vanilla water texture), caustic shimmer, subtle sun glints, pull
  toward reference teal. Gated by `SpbAmbient.w` (poolrooms only). The biome `water_color` teal from
  §18.2 is unchanged.

### 19.3 Config defaults + hunger + grass

- `ConfigStuff`: `cameraShakeIntensity` 0.4→**0.3**, `VHSDistortionMultiplier` 1.0→**0.75**; new
  `enableSmoothLighting` toggle (SHADERS, default on; off ⇒ vanilla lighting). Lang keys added.
- **No hunger in the backrooms**: `PlayerEntityMixin` cancels `causeFoodExhaustion` at HEAD when
  `BackroomsLevels.isInBackrooms(dimension)` (every vanilla drain funnels through that method;
  eating still restores).
- **Infinite Field grass**: `InfGrassChunkGenerator.fillFromNoise` places vanilla short_grass (66%)
  / tall_grass (8%) per column, deterministic position hash. Biome `grass_color` added +
  `data/minecraft/tags/block/dirt.json` tags `spb-revamped:dirt` so the field tints green. The
  original GPU blade renderer stays dropped (A18). **Grass is worldgen ⇒ only fresh chunks** — the
  render-side changes (lighting/water/flashlight) apply to existing saves immediately, but grass
  needs newly-generated Infinite Field chunks.

### 19.4 Notes for next session

- Everything render-side is Mac-safe (vanilla GL 3.3 PostChain, no Veil/compute). The
  `enableSmoothLighting=false` and chain-fails-to-compile paths both restore vanilla lighting.
- All tuning constants (per-level ambient/exposure in `BackroomsPostEffects.onFrameStart`, light
  intensities/radii in `SpbLightGatherer`, flashlight intensity in `writeLights`, water look in
  `backrooms_scene.fsh`) are easy to nudge if the user wants a different mood after their Mac test.
- Harness note: `quickplay.init.gradle` (project root) appends `--quickPlayMultiplayer` to
  runClient. **Kill dev processes by matching `quickPlayMultiplayer` only** — matching `DevLaunch`
  also kills the dedicated server (both dev runs use the devlaunch injector). The Windows dev disk
  hit 0 free mid-session; freed ~300MB from `run/saves` + `run/screenshots` to finish the build.

### 19.5 Verification detail: what was actually seen, tuning iteration, and gaps

**First pass was too hot and had to be tuned down** — worth knowing if the look ever needs
re-deriving. Initial fixture intensities (~1.0-1.15 per channel) + a `(1.0-x)²` falloff + a strong
self-glow term blew Level 0's floor to white and made Level 1 (meant to be a dim grey garage)
almost entirely blown-out white in the first screenshot. Fixed by, together: dropping fixture
color/intensity ~30-40% across all fixture types in `SpbLightGatherer`, switching the falloff to
`(1-x)²/(1+6x²)` (steeper near-cutoff, moodier dark-between-fixtures look), halving the self-glow
term (`0.8`→`0.45` multiplier, `1.4`→`1.1` distance falloff), lowering the clamp ceiling (`2.2`→
`1.5`), and dropping per-level `exposure` from ~1.2-1.9 down to ~0.85-0.95 while *raising* the
ambient floor slightly (so the level doesn't go pitch black between light pools). The poolrooms
water sun-glint was also nearly halved (`0.5`→`0.22` multiplier, power `90`→`120`) after the first
screenshot showed an overexposed white band at the near shoreline. Re-verify visually if any of
these constants are changed again — small tweaks compound non-obviously once exposure + grade +
the S-curve all stack.

**Exact verification performed this session** (screenshot file names are session-local scratch,
not committed — recreate via the flag hook below if repeating):
- Level 0: spawn hallway (-4.5, 21, 14.5) — before AND after the tuning pass, to confirm the fix.
- Level 1: (8.5, 21, 8.5) — before AND after tuning (first shot was blown-out white; confirmed the
  fix produced the intended dim cold-grey garage look with a visibly smooth fixture glow).
- Level 2: (0.5, 20, 8.5) facing the corridor — dark ambient-only shot, then a flashlight-forced-on
  shot (see below) confirming the smooth spotlight cone with no blocky falloff.
- Poolrooms: (16.5, 106, 16.5) — before AND after the glint tuning; confirmed animated refraction +
  caustics + teal color read as natural, not vanilla Minecraft water.
- Infinite Field: forceloaded FRESH chunks at (5010.5, 33, 5010.5) (existing saved chunks predate
  the grass change and won't show it without regeneration) — confirmed short/tall grass generates
  and reads as a dense natural field, then tuned the tall-grass ratio down (was too jungly at first
  pass: 62/90% short/tall split → 66/74%, i.e. much less tall grass).
- Flashlight: **no in-game keybind exists to toggle it via RCON** (it's a client keypress, `R`,
  consumed via `ModKeyBinds.toggleFlashlight.consumeClick()` — RCON can't simulate that, and a
  simulated OS keypress via `keybd_event` on the unfocused/background window did NOT register,
  confirming Windows foreground-lock blocks synthetic input to a background process). Verified
  instead via a **temporary dev-only file-flag hook** added to `SPBRevampedClient`'s tick loop
  (`if (isDevelopmentEnvironment() && Files.exists(.../"spb_flashlight_on.flag")) setFlashLightOn`)
  — confirmed the cone, then **removed the hook** before the final production build (confirmed
  removed: `grep -c spb_flashlight_on` on `SPBRevampedClient.java` returns 0). If the flashlight
  ever needs re-verifying without the user, recreate that same temporary hook rather than fighting
  OS input focus.
- **NOT verified visually this session:** Level 324 (gas station/road level — same
  `isDeferredDimension` code path as L0/1/2, so it should behave identically, but never toured);
  the flashlight cone for a *second, non-local* player (only the local-player cone path was
  exercised); the `enableSmoothLighting=false` fallback path (code reviewed, not screenshot-tested);
  the shader-fails-to-compile HUD-approximation fallback (not forced/tested this session).

**Reusable harness scripts** (session scratchpad, NOT committed to the repo — recreate if needed
for a future headless verification pass, same pattern as §16.3's static-analysis scripts):
- `rcon.py` — ~40-line dependency-free source-RCON client (auth type 3, command type 2, standard
  length-prefixed framing), same technique as §14.8/§15.4, hardcoded to `127.0.0.1:25575` /
  password `probe123` (matches this repo's `run/server.properties`, already committed there).
- `hold_server.py` — Windows equivalent of the Linux FIFO-holder from §14.8: launches
  `gradlew.bat runServer` with `stdin=PIPE` (never written/closed) so the dedicated server survives
  without a foreground console, logging to `run/harness_server.log`.
- `quickplay.init.gradle` (this one IS committed, at the project root) — Gradle init script that
  appends `--quickPlayMultiplayer 127.0.0.1:25565` to any `runClient` task, so the dev client joins
  the local dedicated server directly with no title-screen clicking.
- Screenshot capture is the existing §18.4 dev-only file-flag hook (`touch
  run/spb_screenshot.flag` → PNG in `run/screenshots/`), unchanged this session.
- **Gotcha confirmed this session:** killing Windows Java processes by matching the command line
  substring `DevLaunch` kills BOTH the dev client AND the dedicated server (both `runClient` and
  `runServer` route through Loom's DevLaunchInjector). Match `quickPlayMultiplayer` specifically to
  kill only the client and leave the server running for the next relaunch.
- **Disk-full incident:** the Windows dev machine's `C:` drive hit 0 bytes free mid-session, which
  failed a `gradlew build` with a Gradle dependency-resolution IOException (misleading — looked
  like a config problem, was actually disk space). Freed space by deleting `run/saves/*` (throwaway
  RCON-tour test worlds, ~237MB), `run/screenshots/*` (~62MB), `run/logs/*`, and
  `~/.gradle/daemon/*` (stale daemon lock/log files) — all safe, regenerable harness/build
  artifacts, nothing from the user's actual save or source tree. `C:` was still reported as 100%
  full/1.9GB free immediately after cleanup, i.e. very little headroom remains on this machine for
  future sessions doing similar client+server dev-loop work.

## 20. Poolrooms per-pixel lighting + realistic pool water + swaying field grass (2026-07-07, session 2)

Continues §19 (same day). User asked to: (a) add the per-pixel lighting system to the **Poolrooms**
(it was excluded from §19's deferred set); (b) make the Poolrooms **water** look realistic, not vanilla
(reference: a real pool photo); (c) make the **Infinite Field grass** realistic AND **sway** (reference:
the original gallery's grass field). All on `port/mc-26.2` (uncommitted, layered on §19's uncommitted
work), `gradlew build` green, and **all three verified live on the Windows dev client** via the
§18.4/§19.5 harness — Poolrooms sky rooms, a Poolrooms dark room, the field grass (with a pixel-diff
proving the sway animates), plus a Level 0 no-regression check.

### 20.1 Poolrooms per-pixel lighting — deliberately NOT the fixture-fullbright path
§19's deferred relight (`isDeferredDimension` = L0/1/2/324) forces the lightmap fullbright and relights
from gathered fixture BEs. **Poolrooms stays excluded and must** — it is genuinely sky-lit (glass
`pool_sky` openings; the `poolrooms/sky` room palette is just air/water/pool_tiles, no fluorescent BEs),
so its vanilla lightmap carries real spatial variation: bright sky rooms vs the dark maze rooms
("**Darkrooms**" = the `poolrooms/dark` structure variant, some of which DO have fluorescent tubes).
Forcing fullbright would erase that contrast and blow out the Darkrooms.

Instead poolrooms get a per-pixel relight that MODULATES (preserves) the vanilla skylight, in the
non-deferred branch of `backrooms_scene.fsh`: new `spbPoolLight(color, viewNormal)` — a soft wrapped
directional term off the day-cycle sun (`SpbSunDir`), `shape = 0.82 + 0.42*(ndl*0.7+0.3)` (~0.95..1.24),
times a gentle day-cycle colour tint. Multiplicative ⇒ dark rooms stay dark (can't brighten near-black),
sky rooms gain the smooth per-pixel light SHAPE the flat lightmap lacked; with the existing `spbDeband` +
SSAO this is the "pixel-based lighting" look for a sky-lit level. **No new uniform**: reused `SpbAmbient`
(unused for non-deferred dims) — `BackroomsPostEffects.onFrameStart` sets `ambientFloor =
PoolroomsDayCycle.getLightColor()` for poolrooms and `writeLights` already wrote it as `SpbAmbient.rgb`
with `w=1.0` (the existing poolrooms/water gate). Verified: dark fixture-lit room stays moody, sky rooms
smooth+bright, L0/1/2/324 untouched (separate code path).

### 20.2 Realistic poolrooms water (`spbWater` rebuild)
Replaced §19's first-pass water with a proper pool shader (still post-process, Mac-safe): two-scale
scrolling ripple normal (finite-diff off the noise sampler), depth-tinted teal body (`shallow
(0.22,0.74,0.60)`→`deep (0.02,0.23,0.20)` by luma), refraction re-sample of the submerged scene (tiles
show through), a Fresnel blend into a REAL reflection of the procedural sky (`getSky(reflect(V,Nw))` — the
pool's defining feature), caustic shimmer, a sharp sun specular, and a final pull toward the reference
teal. First shot was too cyan/blown-out at grazing angles → tuned: Fresnel-reflection blend 0.65→0.5,
specular 0.45→0.30, palette greened, +14% teal hue pull.

### 20.3 Infinite Field grass — realistic look + swaying (`block.vsh` core-shader override)
- **Look** (`InfGrassChunkGenerator.fillFromNoise`): §19 left ~26% bare dirt (why it read sparse). Now
  near-full coverage — 62% short_grass, 8% fern, 24% tall_grass (2-high), 4.5% large_fern, ~1.5% bare.
  **Worldgen ⇒ only fresh chunks.**
- **Sway** (NEW `assets/minecraft/shaders/core/block.vsh`): ground-level restoration of the dropped GPU
  blade animation (A18) on the vanilla GL 3.3 chunk pipeline. Wind added ONLY in the `#ifdef ALPHA_CUTOUT`
  program (solid terrain + translucent water can't deform) and ONLY on GREEN vertices (`Color.g -
  max(Color.r,Color.b) > 0` ⇒ biome-tinted grass/ferns/leaves; glass panes/bars are white-tinted and stay
  rigid — dimension-independent, cannot disturb Backrooms glass). Amplitude ramps with `fract(worldY)`
  (roots planted); absolute world XZ (`CameraBlockPos+CameraOffset`) anchors the gust phase; `GameTime`
  drives a two-frequency gust. Always-on for foliage (not config-gated — a custom core-shader uniform is
  the risky 26.2 bit, avoided).
  - **Reusable feasibility finding (verified against the 26.2 jar before writing it):** terrain uses ONE
    universal `block.vsh` (solid/cutout/translucent differ only by the `ALPHA_CUTOUT` define in
    `block.fsh`), and `Globals` (GameTime, CameraBlockPos, CameraOffset) is a first-class
    `BUILT_IN_UNIFORMS` block in `net.minecraft.client.renderer.BindGroupLayouts` that
    `com.mojang.blaze3d.opengl.GlProgram.setupBindGroupLayouts` binds for ANY shader declaring it (reflects
    active UBO blocks via `glGetActiveUniformBlockName`→`glUniformBlockBinding`). So `#moj_import
    <minecraft:globals.glsl>` in a terrain override just works — GameTime/camera are live during chunk
    draws. Confirmed in-client: no `CompilationException`; pixel-diff of two 6s-apart field frames showed
    ~4% large-diff pixels in the grass band vs ~1% sky / ~0.6% static-UI = real blade motion, not grain.

### 20.4 Files changed this session (on top of §19's uncommitted tree)
- `assets/spb-revamped/shaders/post/backrooms_scene.fsh` — `spbWater` rebuild + new `spbPoolLight` +
  poolrooms branch wiring (deband→water→poolLight→cone).
- `com/sp/render/post/BackroomsPostEffects.java` — poolrooms `ambientFloor = PoolroomsDayCycle.getLightColor()`.
- `com/sp/world/generation/chunk_generator/InfGrassChunkGenerator.java` — dense layered grass placement.
- NEW `assets/minecraft/shaders/core/block.vsh` — foliage wind (grass sway).

### 20.5 Still to do / knobs
- **User's macOS playtest is the real acceptance test** (verified on the Windows dev GPU only; the
  shader-fails-to-compile fallbacks still apply — chain off ⇒ vanilla lighting; `block.vsh` is a plain
  GL 3.3 shader, and if it ever failed only foliage would be affected).
- Tuning: water palette/reflection/specular in `spbWater`; poolrooms directional strength in
  `spbPoolLight`; grass density ratios in `fillFromNoise`; wind amplitude/period in `block.vsh`
  (`amp = 0.09*...`, `t = GameTime*2000`).
- Uncommitted (project SOP — no commit/push without the user asking); the tree also still holds §19's work.

## 21. Playtest round: fog glitch, poolrooms polish, HD grass, unbreakable field (2026-07-07, session 3)

User ran the §19/§20 jar and reported five issues; all fixed on `port/mc-26.2` (uncommitted, layered on
§19/§20's uncommitted work), `gradlew build` green, and the render-side changes were **live-verified via
the §18.4 harness** (dedicated server + `runClient` quickplay + RCON tour + file-flag screenshots) — the
scene/vhs/smooth-lightmap shaders all COMPILE with zero `CompilationException` and render correctly.

### 21.1 Level 1 "purple/green/blue glitch while moving, worse jumping, with the fog on"
Root cause (not the fog itself): the **VHS pass's colored artifacts** — chromatic-aberration edge fringing
+ the YUV chroma tape-noise — are invisible against textured scenes but pop against Level 1's flat grey
knee fog and shimmer as edges sweep during motion. Fix in `shaders/post/backrooms_vhs.fsh`: a `chromaDamp`
= `mix(1.0, 0.2, fogPresence)` where `fogPresence = max(step(0.5, SpbFogColor.w), SpbState.y)` — i.e.
wherever a level runs distance fog OR the Level 1 knee fog, both the CA and the chroma tape-noise are cut
to 20%. Also trimmed base CA `0.003→0.0022` and slowed the chroma-noise scroll (`3000/5000 → 1500/2500`)
from a per-frame strobe to coherent tape drift. Monochrome luma grain + the bright sparkle are untouched
(they were never the color glitch). The vhs pass already had `SpbDynamics`, so no JSON change.

### 21.2 Poolrooms "blocky lighting → smooth like the other levels"
Poolrooms stays sky-lit (NOT the §19 fixture-fullbright path — that would erase the Darkrooms), so it kept
the vanilla 15-step lightmap banding. The §20 `spbDeband` (8 taps, 0.035 luma window) was too tight to
cross the brightness steps, so bands survived. Replaced with **`spbSmoothLightmap`** (backrooms_scene.fsh):
a depth-aware bilateral smooth — 3 rings × 6 taps, gated by VIEW-DEPTH similarity (never blurs across a
geometry edge) with a soft WIDE luma window (0.18) that DOES melt the light steps. Verified: poolrooms
walls/ceilings now read as a smooth continuous gradient. Applies to all vanilla-lit dims (`SpbLightMeta.z`).

### 21.3 Poolrooms water "still Minecrafty, glitches while moving, see the MC texture"
The §20 `spbWater` used a HARD detect branch (`N0.y >= 0.72` + teal test) on the jittery depth-derivative
normal → the effect popped on/off frame-to-frame as the player moved, flashing raw vanilla water. Rebuilt
with a **soft 0..1 mask** (`up * teal * bright`, keyed mostly on the stable biome teal hue, blended not
branched) so borderline pixels ease instead of snapping. Clearer body mix (`0.62→0.46`) so the tiled floor
shows through like the reference; calmer reflection (Fresnel blend `0.42→0.34`, max `0.80→0.70`), specular
`0.30→0.18`, and a **brightness clamp** `min(water, vec3(0.92,0.98,0.95))` to stop the steep-top-down
Fresnel+specular+bloom glare that blew the pool to a white/cyan sheet. Verified: refraction shows the tiles,
teal reads, no on/off popping.

### 21.4 "Add more sky in the poolrooms"
`PoolroomsChunkGenerator`: biased the sky/dark room pick `noise > 0 → noise > -0.35` (~50%→~65% sky rooms;
Darkrooms kept, just rarer). **Worldgen ⇒ fresh chunks only.** Confirmed via the harness that the procedural
sky renders above the rooms (flew into the void above — full sunset sky) and through the openings; the
in-room ceiling holes are small so a room's own sky patch is modest, and the sky goes dark during the
poolrooms day-cycle NIGHT band (pre-existing; advance with `execute as @a at @s run backroomsevent sunset`).

### 21.5 Infinite Field "grass is Minecraft grass → make realistic" + "unbreakable"
- **Realistic (the "pixelated" complaint):** authored **HD 128px** grayscale grass-blade textures overriding
  vanilla's blocky 16px sprites — `assets/minecraft/textures/block/{short_grass,tall_grass_top,
  tall_grass_bottom}.png`, drawn as curved tapering Bezier blades on a 4× supersampled canvas (smooth
  anti-aliased edges) with a vertical value ramp (dark roots→lit tips). Stitched by the existing
  `atlases/blocks.json` `block/` source; biome-tinted as usual. Generator: scratchpad `gen_grass_hd.py`.
  These apply to EXISTING grass immediately (texture override). Verified in-field: lush, smooth, real-looking
  blades up close.
- `InfGrassChunkGenerator.fillFromNoise`: dropped ferns/large-ferns (they'd stay vanilla-pixelated among the
  HD grass) → pure short(65%)/tall(32%)/bare(3%) grass for a consistent field. **Density/height ⇒ fresh
  chunks only** (the HD texture itself does not need regen).
- **Unbreakable:** `SPBRevamped` registers `PlayerBlockBreakEvents.BEFORE` cancelling breaks of
  short_grass/tall_grass/fern/large_fern in the `infinite_field` dim only (the ground `ModBlocks.DIRT`
  already had `destroyTime(-1)`). Server-side ⇒ works on existing saves.

### 21.6 What needs a fresh world vs. immediate
- **Immediate on existing saves (render/server-side):** Level 1 fog glitch, poolrooms smooth lighting,
  poolrooms water, HD grass TEXTURE, unbreakable grass.
- **Fresh/regenerated chunks only (worldgen):** more poolrooms sky rooms, denser/taller field grass (no
  ferns). Tell the user to delete `world/dimensions/spb-revamped/{poolrooms,infinite_field}` (or start a new
  world) to get those. Verified this session on freshly-wiped poolrooms + infinite_field.
- Still the user's macOS playtest for final acceptance (Windows dev GPU only). Day-cycle night makes
  poolrooms/sky dark — not a bug.

## 22. Follow-up: the "glitches while moving" root cause + real grass sway (2026-07-07, session 3b)

User tested §21 and reported: the fog COLOR glitch was fixed but a **motion glitch** remained in BOTH the
Level 1 fog AND the poolrooms water, the water still looked "a bit unnatural", and the **grass didn't sway**.
All fixed on `port/mc-26.2` (uncommitted), `gradlew build` green, and **live-verified via the harness**
(shaders + core `block.vsh` compile clean; screenshots below).

### 22.1 Shared root cause of "glitches while moving": fract() noise seams
Both the knee fog (`noise3D`) and the water (`spbWater`) sampled `FogNoiseSampler` with plain **`fract()`**
to wrap UVs. That sampler is clamp-addressed, so `fract()` draws a HARD SEAM line at every integer UV. The
seams are anchored in WORLD space, so as the camera moves they sweep across the screen — the "glitch while
moving" (and "worse jumping" = fast vertical motion). This is the exact bug §18.2 already fixed for the SKY;
the fog and water were simply never converted. Fix: both now use **`spbMirror()`** (mirror-repeat, continuous
— no seam) instead of `fract()` (`backrooms_scene.fsh`, `noise3D` + `spbWater` height samples). This is the
key fix for both symptoms. (Screen-space samplers — SSAO, god rays, deband — never had this; they don't
sample the world-noise textures.)

### 22.2 Water: also dropped the depth-derivative normal from detection
The water mask still multiplied an up-facing test (`smoothstep(N0.y)`) built from the depth-derivative normal
(`cross(dFdx,dFdy)`), which jitters frame-to-frame under camera motion → the effect shimmered. Removed it:
detection is now the **stable biome teal hue + brightness only** (`teal*bright`). Poolrooms walls are cream so
hue alone reliably means water. Combined with §22.1 the pool now reads as clean, natural clear water with the
tiled floor showing through — verified daytime top-down (no blowout, no seams) and eye-level.

### 22.3 Grass sway — was doubly broken, now clearly visible (`block.vsh`)
Two bugs, not one: (a) amplitude was tiny (0.09), and (b) the height ramp used `fract(worldPos.y)`, which is
~0 at a FULL-BLOCK-tall blade's TIP (tip sits at an integer world Y) — so the tips, the part that should wave
most, barely moved. Fix: taper by the **texture V** instead — `tip = 1.0 - UV0.y` (0 at root, 1 at the sprite
top), `ramp = 0.15 + 0.85*tip*tip`, amplitude up to `0.30` (tips sway ~0.2 block). `GameTime` is
`(worldGameTime%24000 + partial)/24000` ∈ [0,1) (javap-confirmed on `GlobalSettingsUniform`) so it advances
smoothly in every dim — the sway was never frozen, just invisible. **Verified**: fixed camera, two frames 2s
apart, the diff shows clear blade-shaped motion in the grass band (vs uniform grain elsewhere). Still only the
green-tinted cutout program (glass/bars stay rigid); applies to existing grass (render-side).

### 22.4 Status / knobs
- Sway amplitude/period: `block.vsh` (`amp = 0.30*...`, `t = GameTime*2200`). Tall-grass 2-tall plants taper
  per-block so there's a mild mid-height kink at the block seam (thin overlapping sprites hide it); lower the
  `ramp` base if it ever reads as a tear.
- Water look: `spbWater` in `backrooms_scene.fsh` (palette/reflection/clamp). Poolrooms has no distance fog so
  the VHS chromatic-aberration is NOT damped there → a faint red edge on the pool rim; only damped in foggy
  levels (§21.1). Harmless, but drop base CA further in `backrooms_vhs.fsh` if the user dislikes it.
- Everything this session is render/shader-side ⇒ applies to existing saves. macOS playtest still the final
  acceptance (Windows dev GPU only).

## 23. Reference-match poolrooms pass + mod link metadata (2026-07-08)

User gave a reference screenshot of the poolrooms (dark/moody room, bright ceiling skylights, calm
deep-teal reflective water) and asked to (a) make the pool water smooth/animated with "no breaks", and
(b) fix the mod's link metadata. All on `port/mc-26.2` (uncommitted, layered on §19-22), `gradlew build`
green, and **live-verified via the §18.4 harness** (dedicated server + `runClient` quickplay + RCON tour +
file-flag screenshots — server world's poolrooms day cycle was advanced to daytime via
`execute as @a at @s run backroomsevent sunset`, persists server-side).

### 23.1 Mod link metadata (`fabric.mod.json`)
- Removed `contact.YouTube` (was SpacePotato's channel) and the whole `custom.modmenu.links` block (it held
  "My Youtube" + "Join the Discord!").
- Added `contact.homepage` = `https://modrinth.com/mod/backrooms-ff-continued` and `contact.issues` =
  `https://github.com/realarbird/Backrooms-FF-Continued/issues` (ModMenu renders these as Homepage/Issues
  buttons). The `MathStuff.java` YouTube URL is a code-comment tutorial link, left as-is.

### 23.2 "No breaks" root cause + the fix (this is the important bit)
The old water was **post-process only** (`spbWater` painted over vanilla block water, keyed on a teal-hue
*and brightness* mask). Wherever detection dropped out — dark/deep water, water reflecting the dark ceiling —
it snapped back to raw blocky vanilla water. That boundary was the visible "break". Two-part fix:
1. **Seamless flowing surface at the source** — extended the existing `assets/minecraft/shaders/core/block.vsh`
   (the grass-sway override) with a gentle water swell in the `#else` (non-cutout) branch, gated to
   biome-teal vertices (`min(Color.g,Color.b) - Color.r`). Wave is a continuous function of WORLD xz so
   adjacent water blocks agree at shared edges → one connected undulating sheet, no seams. Glass/other
   translucent (white-tinted) and solid terrain are untouched. Amplitude ±0.09 block, ~4s cycle
   (`GameTime*2000`). **Same feasibility basis as the grass sway (§20.3): water is translucent terrain on
   the universal `block.vsh`, and `globals.glsl` GameTime/camera bind for it.**
2. **`spbWater` rebuild** (`backrooms_scene.fsh`) — dropped the brightness gate (hue alone now, so the whole
   surface is one continuous treated sheet, no internal snap), removed the streaky caustic web + sharp
   pinpoint specular (they read as breaks), deepened/calmed the teal body (`shallow (0.14,0.52,0.44)` →
   `deep (0.015,0.16,0.15)`, body mix 0.58), kept a smooth broad Fresnel reflection of the procedural sky
   (the skylight mirror, blend 0.40), soft broad glint only, moody clamp `min(...,(0.80,0.90,0.86))`.

### 23.3 Low-key/moody poolrooms grade (matched the reference mood)
The reference is dark with the skylights as the only bright points; the old grade was the opposite — washed
and orange. In `BackroomsPostEffects.onFrameStart` poolrooms branch: `tint 1.05/1.01/0.94 → 0.99/1.00/0.98`
(kills the permanent orange cast — colour now follows the day cycle naturally, neutral at midday / warm at
golden hour), `lift 0.02 → 0.0` (no washed haze; blacks go dark), `sCurve 0.24 → 0.32` (skylights pop out of
the dark), `exposure 1.15 → 0.92` + `bloom 0.5 → 0.32` (stops the bright tile deck blowing to pure white),
`rayStrength 0.55 → 0.22` (softer shafts — don't streak the now-glassy water or blow the sunset sky into a
beam). `spbPoolLight` overhead-skylight term added (`+0.20*max(N.y,0)`) so light pools on the floor/water
from above.

### 23.4 Verified + what's day-cycle/room dependent (watch-items)
Screenshot-confirmed on the Windows dev client: water reads as a smooth calm deep-teal pool with the deck a
proper tan (not blown), moody low-key walls, no surface breaks, shaders compile clean (block.vsh + scene).
Caveats for the next session / the user's macOS test:
- **The look tracks the poolrooms day cycle.** Walls are neutral-cream near midday, warm at golden hour, and
  the procedural sky goes saturated-magenta + god-ray-blown at sunset in OPEN-topped rooms. The reference is
  an enclosed room at ~midday. The spawn area (16.5,106,16.5) is a large open-topped sky room — good for the
  water, not for the reference's dark-ceiling-skylight-grid composition (never located an enclosed
  skylight-grid room this session; worth finding to fully match the reference framing).
- The sunset procedural-sky blowout (magenta, harsh mirror-folds when open sky fills the view) is a
  pre-existing rough spot (§18.5) made obvious here; taming god rays helped but the sky palette itself is
  still hot at dusk — a candidate for a future desaturate/soften pass if the user dislikes it.
- Knobs: water in `spbWater`; wave amp/period in `block.vsh` (`0.035`, `GameTime*2000`); poolrooms
  exposure/tint/sCurve/bloom/rays in `BackroomsPostEffects`. All render-side ⇒ apply to existing saves.

## 24. Skinwalker capture: missing-texture (magenta) + hard crash — GeckoLib 4→5 resource-root miss (2026-07-08, back on macOS)

**Symptoms (user, running the shipped jar in Lunar Client on macOS/M4, MC 26.2):** when the skinwalker
captures the player, the entity renders as the vanilla missing-texture magenta/black checkerboard cube, and
the game crashes a few seconds into the capture. Crash report
`~/Library/Application Support/minecraft/crash-reports/crash-2026-07-08_11.03.38-client.txt`:
`java.lang.IllegalArgumentException: Bone not found: base_leg1` at
`GeoModelAccessor.getBone` ← `IKLegComponent.tickClient` ← `SkinWalkerRenderer.extractRenderState:146`
(the `if (trueForm)` IK tick).

**Root cause (single, explains BOTH symptoms).** The port kept GeckoLib-4 asset locations/identifiers.
GeckoLib 5 (`5.5.x`) changed the convention and its resource scanner (`GeckoLibResources`) now scans **only**
`assets/<ns>/geckolib/models` and `assets/<ns>/geckolib/animations` — the legacy `assets/<ns>/geo/` and
`assets/<ns>/animations/` roots are **not scanned at all**. Our files lived in the legacy roots and our
`getModelResource(...)` returned full paths like `spb-revamped:geo/entity/skin_walker_default.geo.json`.
Result: `BakedModelCache.getModel(...)` found nothing, logged `Unable to find model: …` +
`Superfluous prefix or suffix … Should be 'spb-revamped:geo/entity/skin_walker_default'`, and returned the
**MISSINGNO** baked model (→ the magenta cube). Because the baked model was the missingno fallback, the
true-form IK tick's `GeoModelAccessor.getBone("base_leg1")` came up empty. The IK code is *written* to
tolerate that (`if (model.getBone(...).isEmpty()) return;`), but `GeoModelAccessor.getBone` first calls
`PrAnCommonClass.throwInDevOnly(...)`, and **`PrAnCommonClass.isDev` defaulted to `true`** and was only ever
flipped *to* true in `SPBRevamped` (`if (isDevelopmentEnvironment())`) — never cleared for production. So in
the shipped/Lunar build every "bone not found" hard-crashed instead of skipping. (Note: the default
`skin_walker_default` model was *already* MISSINGNO ~90 s before the crash, at 11:02:09 in the Lunar log —
the disguise-phase skinwalker was rendering untextured too; the crash just happened once true form ran the IK
path.) Evidence for GeckoLib's convention came straight from the 5.5.2 jar bytecode
(`GeckoLibResources.MODELS_PATH="geckolib/models"`, `stripPrefixAndSuffix`, and `DefaultedGeoModel` building
`geckolib/models/<subtype>/<name>` keyed as `<subtype>/<name>`).

**Fix.**
1. **Moved the GeckoLib assets** (`git mv`, history preserved):
   `assets/spb-revamped/geo/entity/*.geo.json` → `assets/spb-revamped/geckolib/models/entity/`,
   `assets/spb-revamped/animations/entity/*.animation.json` → `assets/spb-revamped/geckolib/animations/entity/`.
   Legacy `geo/` + `animations/` dirs removed.
2. **Updated every GeckoLib identifier** to the bare `"<subtype>/<name>"` key (no `geo/`/`animations/`
   prefix, no `.geo.json`/`.animation.json` suffix): `SkinWalkerRenderer` (slim/default/final),
   `SkinWalkerModel` (model + animation → `entity/skinwalker`), `WalkerModel` (`entity/walker`),
   `GasPumpItemModel` (`blocks/staircase` + `staircase` — those staircase assets still don't exist, a
   pre-existing gap, now just correctly-formatted). **Textures are unchanged** — GeckoLib 5 still uses the
   full `textures/…png` path (`textures/<subtype>/<name>.png`), and the log had zero texture errors.
3. **`PrAnCommonClass.isDev` now defaults to `false`** (defense-in-depth). Dev env still flips it true via
   `SPBRevamped`, so dev keeps the strict throw; production now honours the IK code's graceful
   `getBone(...).isEmpty()` guards instead of hard-crashing. This is what `throwInDevOnly` was always meant to do.

**Verified.** `./gradlew build` clean (Java 25 toolchain — must run gradle with `JAVA_HOME` = a JDK 25, e.g.
`/opt/homebrew/Cellar/openjdk/25.0.1/...`; the default `jdk-24` gives "release version 25 not supported").
Ran the dev client (`runClient`): GeckoLib now logs **`Loaded 5 models and 1 animations from resources`**
(pre-fix it loaded 0 of ours), with **zero** `Unable to find model` / `Bone not found` / `Superfluous …`
lines. The true-form geo `skin_walker_final_default` contains `base_leg1..4` + `seg*_leg*`, so the IK tick
now resolves them. New jar (clean geckolib/ layout, no stale legacy entries) built and **deployed to
`~/.lunarclient/profiles/26/mods/fabric-26.2/backrooms-ff-continued-2.0.0.jar`** (old jar backed up as
`*.bak-precheckerfix`).

**Still to confirm (user, in-game — the one thing logs can't show):** trigger an actual capture
(`/skinwalker <you>` as gamemaster, or natural spawn) and watch the full reveal → **true form**: the walker
should render with the stolen player-skin body + spider legs (no magenta) and **not crash**. Dev-client note:
dev runs with `isDev=true`, so the RCON harness (§18.4: server up first, then `runClient` auto-connects via
`quickplay.init.gradle`, `/skinwalker @a` over RCON at `:25575` pw `probe123`) is a *strict* crash test if a
fuller playtest is wanted next session — this session verified resource loading, not a live capture render.

## 25. Poolrooms water: scrapped the post-process overlay for a real HD block texture (2026-07-08)

**User report:** "the water looks bad and glitches weirdly when moving," on ALL platforms (Mac *and*
Windows), and it still shows Minecraft's blocky 16×16 texture. Directive: scrap MC's default water, give it
a **new 128×128, animated, smooth** texture with waves that **does not glitch when moving**.

**Root cause (the whole §19–§23 approach was the bug).** Every prior water pass produced the look with a
**screen-space post-process** shader `spbWater` in `backrooms_scene.fsh` that detected water by pixel colour
(the teal biome `water_color` `0x3FC6A0`) and repainted those pixels. Colour-based detection is inherently
unstable under camera motion — lighting/reflection shifts the hue, the mask flickers on/off frame-to-frame,
and the raw vanilla 16×16 water flashes through. That is the "glitches while moving," and because the
detection is cross-platform, so is the glitch. It was re-patched four times (§21.3/§22.2/§23.2) and never
cured; the underlying block texture was never replaced, so every detection dropout fell back to blocky
vanilla water. (All prior "verified" screenshots were the Windows dev GPU, not the user's Mac.)

**Fix — move the water look OUT of screen space and INTO the block atlas** (same proven pattern as §21.5's
HD grass). A real MC animated texture lives in the atlas and is camera-independent, so it *cannot* flicker.
Design doc: `docs/superpowers/specs/2026-07-08-poolrooms-water-texture-rewrite-design.md`.

1. **New HD water textures** in `assets/minecraft/textures/block/` (override vanilla via the existing
   `atlases/blocks.json` `block/` directory source — the fluid renderer fetches `block/water_still` &
   `block/water_flow` from the block atlas):
   - `water_still.png` (+`.mcmeta`) — **128×128 per frame, 32 frames** (PNG 128×4096), `frametime 3` +
     `interpolate:true` (~4.8 s calm loop).
   - `water_flow.png` (+`.mcmeta`) — 128px HD flow (pool rim / side faces), ripples biased along +V.
   - `water_overlay.png` — 128×128 static (faces seen through water).
   - **Grayscale + semi-transparent** (alpha ≈ 0.72, mean 184): the biome `water_color` tints it teal in the
     poolrooms and blue elsewhere (one texture, correct everywhere); floor shows through. Built from
     continuous sine fields → **smooth** (mean per-frame change 0.9/255), **spatially tileable** (integer
     wavenumbers → adjacent blocks share edges, seam ≈ 1/255, no grid line), **temporally seamless** (integer
     cycles → loop-wrap step == interior step, no pop). Look = "calm reflective pool" (user's pick).
   - Generator: `docs/superpowers/specs/gen_water_hd.py` (PIL/numpy; run `python gen_water_hd.py <outdir>`;
     it also emits `preview_*` images for eyeballing before shipping). Knobs: `spec_str` (highlight
     strength), `a_base` (opacity), the `STILL_WAVES`/`FLOW_WAVES` wavenumber tables, `FRAMES`, `frametime`.
2. **Waves — two glitch-proof mechanisms only:** the baked texture ripple (screen-stable) + the **kept**
   geometry swell in `assets/minecraft/shaders/core/block.vsh` `#else` branch (±0.09 block, world-XZ phase →
   seamless across blocks; it was never the glitch source per §22.1).
3. **Deleted the overlay** in `backrooms_scene.fsh`: removed the `spbWater(...)` call and the whole `spbWater`
   function (replaced with a "REMOVED / do not reintroduce screen-space hue detection" note). **Kept**
   `spbPoolLight` (smooth directional sky-lighting on floor+water — a lighting multiply, not a detection
   mask) and the biome teal tint. `getSky`/`spbMirror`/`FogNoiseSampler` stay (still used by sky/fog).

**Verified.** `./gradlew build` green (Java 25). Textures validated offline (dims/frames/mcmeta/alpha/tile-
seam/loop-wrap all pass; previews screenshot-inspected → calm reflective teal pool, no seams, floor visible).
New jar **deployed to both runtimes**: Lunar (`~/.lunarclient/profiles/26/mods/fabric-26.2/`) and Modrinth
(`.../ModrinthApp/profiles/mc backrooms/mods/`). Render/texture-side only ⇒ applies to existing saves; no
worldgen regen. **Not yet confirmed by a live capture** — GLSL post-shaders + the block atlas only
compile/stitch at runtime and the assistant can't run the client (no display, §4). The one remaining check is
the user in the poolrooms (spawn 16.5,106,16.5; `/backroomsevent sunset` for daytime): **move the camera and
confirm no flicker / no blocky fallback**, smooth animated teal water. If it reads too flat, bump `spec_str`
in the generator and rebuild — do NOT bring back a screen-space detection pass.

## 26. Skinwalker jumpscare face replays over and over in multiplayer (GitHub issues #1, #2)

**User reports:** "me and my friend are playing on a server and we keep getting faced repeatedly … its
ruining the gameplay" (#1, screenshot = `creepy_face1.png`, upside down) and "when my friend runs it makes it
show [the face]" (#2).

**Root cause.** The capture jumpscare is client-driven: the server sets `isBeingCaptured` + `hasBeenCaptured`
and syncs; the client plays the 14 s timeline (`SkinwalkerJumpscare`), then clears `isBeingCaptured` locally
and is supposed to report that back with the `"beingCaptured"` C2S sync. That send was left commented out
("SPBRevampedClient is not yet ported"), so the server's `isBeingCaptured` stayed `true` forever (and was saved
to NBT). `PlayerComponent` syncs its whole NBT, so every later sync re-sent `true` and §14.6's `reset()`
re-armed the timeline from 0: sprinting (stamina `syncWith` every 20 points, the "when my friend runs" report),
flashlight toggles, the release sequence, teleport timers, relogging. Nothing on the server ever cleared it —
the release paths only clear `hasBeenCaptured`.

**Fix.**
1. `SkinwalkerJumpscare` — re-enabled `SPBRevampedClient.sendComponentSyncPacket(..., "beingCaptured")` at the
   end of the timeline (the original's behaviour; `SyncServerComponent` already handled it).
2. `PlayerComponent.serverTick` — self-heal: `isBeingCaptured && !hasBeenCaptured` means the capture is over
   (release / death / `/skinwalker release`), so clear it and sync. Also repairs players whose saves already
   carry the stuck flag.
3. `ClientWrapper` — only run the jumpscare while **both** flags are set (the server always sets them
   together), so a stale flag in the join sync can't start the scream before the server-side heal lands.
4. `BackroomsHudOverlay.drawJumpscare` — draw the faces + `you_cant_escape.png` with v flipped (1→0). The PNGs
   are stored upside down because the original sampled them in the VHS post shader (bottom-left texture
   origin); the HUD blit is top-left, so the port showed them upside down (see the #1 screenshot).

**Not build-verified in that session** (cloud container without access to the Fabric maven / Mojang). The
code changes are a re-enabled existing call, two boolean conditions and swapped UV literals on an existing
`blit` overload. To check live: two players on a server (capture needs >1 player, or `/skinwalker <player>`),
let the jumpscare finish, then sprint / toggle flashlight / relog — the face must not come back, and it should
render upright.
