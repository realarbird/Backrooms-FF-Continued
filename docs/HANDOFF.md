# Backrooms FF Continued — Port Handoff (resume-from-here)

**Purpose:** self-contained state dump so a fresh chat/session can continue this port with zero prior
context. Read this + the four reference docs (§9) + `git log --oneline` and you're caught up.

---

## 1. What this is

Porting the Fabric mod **SP-Backrooms Revamped** ("Minecraft Found Footage", by SpacePotato & Herr
Chaos, LGPL-3.0) from **Minecraft 1.20.1 → 26.2** and making it **run on macOS**. Rebranded to display
name **"Backrooms FF Continued"**, author **ARBird** (the user). Internal mod id kept as `spb-revamped`.

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

1. Read this file, then `docs/PORTING_MAP.md` + `docs/CLIENT_PLAYTEST.md`, then `git -C
   /Users/ayanraj/Documents/MinecraftFoundFootage-master log --oneline | head -40`.
2. Confirm green: `JAVA_HOME=/opt/homebrew/opt/openjdk@25/libexec/openjdk.jdk/Contents/Home ./gradlew build`.
3. Ask the user for their latest `runClient` result / crash trace, or address the §11 cosmetic items.
4. Fix → rebuild → copy jar to the Modrinth profile (§4) → user relaunches. Commit each fix (no push).
</content>
