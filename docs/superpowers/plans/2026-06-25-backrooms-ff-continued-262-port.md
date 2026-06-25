# Backrooms FF Continued — 26.2 + macOS Port Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Port the 1.20.1 Fabric mod *Backrooms FF Continued* (formerly SP-Backrooms Revamped) to Minecraft 26.2 (mojmap, Java 25) and make it run on macOS by removing the Veil GLSL dependency and rebuilding its visuals Mac-safe.

**Architecture:** Compile-driven, build-green-per-phase migration. Toolchain first, then port packages in dependency order (init/registries → worldgen/data → entities → render → mixins → Veil-removal → CCA/networking/voice/compat), keeping each phase compiling before advancing. The internal mod id `spb-revamped` and `com.sp` package are preserved; only display identity changed.

**Tech Stack:** Fabric Loom 1.17, Gradle 9.5.1, Fabric Loader 0.19.3, Fabric API 0.152.2+26.2, Java 25, GeckoLib 5.5.2, Cardinal Components 8.0.0, Simple Voice Chat 2.6.20+26.2. Veil removed.

**Spec:** [docs/superpowers/specs/2026-06-25-backrooms-ff-continued-262-port-design.md](../specs/2026-06-25-backrooms-ff-continued-262-port-design.md)

---

## Verification model (read first)

Minecraft mods are not unit-tested in the usual sense. The "test" that gates each step is:

- **Compile gate:** `JAVA_HOME=/opt/homebrew/opt/openjdk@25/libexec/openjdk.jdk/Contents/Home ./gradlew compileJava` (fast) or `./gradlew build` (full). Define this shell alias once per session:
  `export JAVA_HOME=/opt/homebrew/opt/openjdk@25/libexec/openjdk.jdk/Contents/Home` then use `./gradlew …`.
- **Headless runtime gate (Phases ≥3):** dedicated-server boot with no exceptions + a worldgen probe (added in Phase 3) that force-generates each dimension and logs block states. Server can't take piped stdin commands reliably — use the probe system property, not console commands.
- **Client gate (user-only):** `./gradlew runClient` on macOS for visuals/gameplay. The assistant cannot drive a Mac client; every client-visual claim is handed off to the user.

"Expected: PASS" below means the gate command exits 0 / logs the expected line. Commit after each green gate.

## Phasing note (honesty about granularity)

Phases 1–2 are fully knowable and written bite-sized below. Phases 3–8 are **discovery-driven**: the exact mojmap signature for each of ~24.5k LOC and the fate of each of 72 mixins cannot be pre-written without the compiler's feedback. Those phases are specified as task units with concrete scope, technique, commands, and a definition-of-done (DoD). When execution reaches Phase 5 (mixins) and Phase 6 (visuals), expand them into bite-sized steps using their triage docs (`docs/MIXIN_TRIAGE.md`, `docs/MAC_SAFE_RENDERING.md`) produced as the first task of each.

---

## Phase 1 — Toolchain to 26.2

**Files:**
- Modify: `gradle.properties`
- Modify: `build.gradle`
- Modify: `gradle/wrapper/gradle-wrapper.properties`
- Modify: `src/main/resources/fabric.mod.json` (depends block)

- [ ] **Step 1.1: Replace `gradle.properties`**

Write exactly:

```properties
# Increase memory available to gradle.
org.gradle.jvmargs=-Xmx8G
org.gradle.parallel=true
org.gradle.configuration-cache=false

# Fabric (https://fabricmc.net/develop)
minecraft_version=26.2
loader_version=0.19.3
loom_version=1.17-SNAPSHOT
fabric_version=0.152.2+26.2

# Mod Properties
mod_version=2.0.0
maven_group=com.sp
archives_base_name=backrooms-ff-continued

# Dependencies (resolved/confirmed in Step 1.4)
geckolib_version=5.5.2
cca_version=8.0.0
modmenu_version=
midnightlib_version=
voicechat_version=2.6.20+26.2
```

(Leave `modmenu_version`/`midnightlib_version` blank until confirmed in Step 1.4.)

- [ ] **Step 1.2: Rewrite `build.gradle` to the 26.2 baseline**

Use the official 26.2 wiring (plugin id `net.fabricmc.fabric-loom`, no `mappings` line, `implementation` for core, `release = 25`). Re-add our access widener and our deps. Each third-party mod's exact coordinate/configuration is pulled from its own 26.2 install docs in Step 1.4; start from this skeleton:

```gradle
plugins {
	id 'net.fabricmc.fabric-loom' version "${loom_version}"
	id 'maven-publish'
}

loom {
	accessWidenerPath = file("src/main/resources/spb-revamped.accesswidener")
}

version = project.mod_version
group = project.maven_group
base { archivesName = project.archives_base_name }

repositories {
	maven { name = 'Ladysnake Mods'; url = 'https://maven.ladysnake.org/releases' }
	maven { name = 'Terraformers'; url = 'https://maven.terraformersmc.com/' }
	maven { name = 'GeckoLib'; url = 'https://dl.cloudsmith.io/public/geckolib3/geckolib/maven/'
		content { includeGroupByRegex("software\\.bernie.*"); includeGroup("com.eliotlash.mclib") } }
	maven { name = 'Henkel (Voice Chat)'; url = 'https://maven.maxhenkel.de/repository/public' }
	maven { name = 'Modrinth'; url = 'https://api.modrinth.com/maven'
		content { includeGroup "maven.modrinth" } }
	mavenCentral()
}

fabricApi { configureDataGeneration() }

dependencies {
	minecraft "com.mojang:minecraft:${project.minecraft_version}"
	implementation "net.fabricmc:fabric-loader:${project.loader_version}"
	implementation "net.fabricmc.fabric-api:fabric-api:${project.fabric_version}"
	// Third-party mod deps re-added in Step 1.4 using each mod's documented 26.2 coordinates.
}

processResources {
	inputs.property "version", project.version
	filesMatching("fabric.mod.json") { expand "version": project.version }
}

tasks.withType(JavaCompile).configureEach { it.options.release = 25 }

java {
	withSourcesJar()
	sourceCompatibility = JavaVersion.VERSION_25
	targetCompatibility = JavaVersion.VERSION_25
}

jar { from("LICENSE") { rename { "${it}_${project.base.archivesName.get()}" } } }

publishing {
	publications { create("mavenJava", MavenPublication) { artifactId = project.archives_base_name; from components.java } }
	repositories {}
}
```

NOTE: the **Veil** repo + dependency lines from the old `build.gradle` are intentionally **not** carried over.

- [ ] **Step 1.3: Bump the Gradle wrapper to 9.5.1**

Run: `JAVA_HOME=/opt/homebrew/opt/openjdk@25/libexec/openjdk.jdk/Contents/Home ./gradlew wrapper --gradle-version 9.5.1`
Expected: `gradle/wrapper/gradle-wrapper.properties` now references `gradle-9.5.1-bin.zip`. If the current wrapper can't run under Java 25, edit `distributionUrl` in `gradle/wrapper/gradle-wrapper.properties` to `https\://services.gradle.org/distributions/gradle-9.5.1-bin.zip` by hand, then re-run.

- [ ] **Step 1.4: Resolve every third-party dependency's 26.2 coordinate**

For each of GeckoLib, Cardinal Components, Simple Voice Chat, ModMenu, MidnightLib, Sound Physics Remastered, Essential — open its Modrinth/CurseForge 26.2 file page and copy the **documented** gradle dependency line(s) (configuration + JIJ `include` where the original used it). Fill the blank versions in `gradle.properties` and add the dependency lines to `build.gradle`. For any compat dep (Sound Physics, Essential) **without** a 26.2 build, keep its `com.sp.compat.*` code but make the dependency `modCompileOnly` and ensure the runtime entrypoint guards on `FabricLoader.getInstance().isModLoaded(...)`.

Run: `./gradlew dependencies --configuration runtimeClasspath` (with JAVA_HOME set)
Expected: all declared deps resolve (no "Could not find" errors). Veil absent.

- [ ] **Step 1.5: Update `fabric.mod.json` depends block**

Change in `src/main/resources/fabric.mod.json`:

```json
	"depends": {
		"fabricloader": ">=0.19.3",
		"minecraft": "~26.2",
		"java": ">=25",
		"fabric-api": "*",
		"fabric-key-binding-api-v1": "*",
		"voicechat": ">=2.6.20",
		"geckolib": ">=5.5.2"
	},
```

(Leave `breaks`/`suggests` as-is; revisit obsolete entries in Phase 7.)

- [ ] **Step 1.6: Generate sources for reference & confirm the toolchain resolves**

Run: `JAVA_HOME=… ./gradlew genSources`
Expected: BUILD SUCCESSFUL; merged mojmap jar present under `~/.gradle/caches/fabric-loom/`. (`javap` on it is the reference for real signatures in later phases.) The mod's own Java will **not** compile yet — that's Phase 2+.

- [ ] **Step 1.7: Commit**

```bash
git add gradle.properties build.gradle gradle/wrapper/gradle-wrapper.properties src/main/resources/fabric.mod.json
git commit -m "Phase 1: 26.2 toolchain (mojmap, Java 25, loom 1.17); drop Veil"
```

DoD: dependencies resolve, genSources succeeds, Veil gone. Java compile failures expected.

---

## Phase 2 — Init, entrypoints & registries

**Files (scope):** `src/main/java/com/sp/SPBRevamped.java`, `SPBRevampedClient.java`, `SPBRevampedDataGenerator.java`, `ModKeyBinds.java`, everything under `com/sp/init/` (13), `com/sp/item/` (6), and the registration entry points of `com/sp/block/` and `com/sp/entity/` (registration only; behavior in later phases). `src/main/resources/spb-revamped.accesswidener`.

**Approach:** Translate registration/bootstrap code to mojmap/26.2. Known required changes (verify each via `javap` on the merged jar):
- `Identifier` via `Identifier.fromNamespaceAndPath(ns, path)`; `ResourceKey.identifier()`.
- Blocks/items must `.setId(ResourceKey)` before construction; registration via `Registry.register(...)` with mojmap registry refs.
- Convert access-widener targets to mojmap names; update the widener header version if loom reports a mismatch.
- Replace 1.20.1 registry helpers with the 26.2 equivalents surfaced by compile errors.

**Steps:**
- [ ] 2.1 Convert `spb-revamped.accesswidener` entries to mojmap; run `./gradlew build` and note remaining widener errors only.
- [ ] 2.2 Port the 4 root entrypoints + `ModKeyBinds` until they compile (stub references to not-yet-ported classes with `// PORT: phaseN` and a minimal compiling form only where unavoidable; prefer porting the real dependency).
- [ ] 2.3 Port `com/sp/init/*` (registry holders) — get all registration compiling.
- [ ] 2.4 Port `com/sp/item/*`.
- [ ] 2.5 Compile gate: `./gradlew compileJava` — drive the error count for these packages to zero (later packages may still fail; scope this task to init/item/registries).
- [ ] 2.6 Commit: `git commit -m "Phase 2: init, entrypoints, registries → mojmap/26.2"`

DoD: init/registry/item packages compile; remaining failures are in not-yet-ported packages.

---

## Phase 3 — Blocks, worldgen, dimensions & data

**Files (scope):** `com/sp/block/**` (39), `com/sp/world/**` (39), `com/sp/datagen/**` (2); resources under `src/main/resources/data/spb-revamped/**` (worldgen, dimension_type, damage_type, biome, noise_settings, structures, loot_tables) and `src/main/generated/**`.

**Approach:**
- Port block classes + block entities to mojmap; `BlockBehaviour.Properties`, `setId`, render layers via 26.2 API.
- Port chunk generator / dimension code; the 26.2 `submit()`/registration changes surfaced by the compiler.
- Migrate data JSON to 26.2 schemas: `dimension_type` (reworked attributes map), worldgen `noise_settings`/`biome`, **`loot_tables/` → `loot_table/`** (singular), recipes/tags, and add required `assets/spb-revamped/items/<id>.json` model definitions. Prefer regenerating via the `fabric-datagen` entrypoint where the mod already datagens; hand-fix the rest.
- Re-save the 285 NBT structures to the 26.2 data version (load via a datagen/structure-load pass or `gradlew` data run; if NBT loads without re-save, leave them).

**Steps:**
- [ ] 3.1 Add a headless worldgen probe: a server-lifecycle listener gated on `-Dffc.probe=true` that force-loads each Backrooms dimension and logs a block-state sample. File: `com/sp/util/DebugWorldgenProbe.java`, registered in `SPBRevamped`.
- [ ] 3.2 Port `com/sp/block/**` to compile.
- [ ] 3.3 Port `com/sp/world/**` to compile.
- [ ] 3.4 Migrate data JSON + run `./gradlew runDatagen`; reconcile generated output.
- [ ] 3.5 Compile gate green for block/world/datagen packages.
- [ ] 3.6 Runtime gate: `printf 'stop\n' | JAVA_HOME=… ./gradlew runServer -Dffc.probe=true` with `run/eula.txt`=`eula=true`; expect each dimension's probe line, no exceptions. Free port between runs: `lsof -ti :25565 | xargs kill -9` (adjust to the run config's port).
- [ ] 3.7 Commit: `git commit -m "Phase 3: blocks, worldgen, dimensions, data → 26.2; headless probe green"`

DoD: server boots, probe generates every dimension with no exceptions.

---

## Phase 4 — Entities (GeckoLib 5) & AI

**Files (scope):** `com/sp/entity/**` (49, incl. `ai/`, `ik/`, `custom/`), entity renderers in `com/sp/render/**` that are not Veil-coupled, entity assets under `assets/spb-revamped/{geo,animations,textures/entity}`.

**Approach:**
- Migrate GeckoLib 4.4.9 → 5.5.2 API (model/renderer/animatable interfaces, `GeoModel`, animation controllers — follow GeckoLib 5 docs).
- Entity renderers override the 26.2 **`submit(state, PoseStack, SubmitNodeCollector, CameraRenderState)`** deferred path, not `render(...)`.
- Port AI goals/IK to mojmap (`MobEffects.SLOWNESS` etc.).
- Defer any renderer that draws via Veil lights/shaders to Phase 6 (mark `// PORT: phase6`).

**Steps:**
- [ ] 4.1 Port entity registration + base classes; compile.
- [ ] 4.2 Migrate GeckoLib renderers/models to v5; compile.
- [ ] 4.3 Port AI/IK packages; compile.
- [ ] 4.4 Compile gate green for entity packages (Veil-coupled render bits excluded).
- [ ] 4.5 Runtime gate: extend the probe (or a spawn test) to summon each entity headlessly and confirm no exceptions at construction/registration.
- [ ] 4.6 Commit: `git commit -m "Phase 4: entities + GeckoLib 5 migration → 26.2"`

DoD: entities register and construct headlessly without exceptions; renderers compile.

---

## Phase 5 — Mixins (72) triage & re-target

**Files (scope):** `com/sp/mixin/**` (72), `com/sp/mixininterfaces/**` (6), `src/main/resources/spb-revamped.mixins.json`.

**Approach:** First task produces `docs/MIXIN_TRIAGE.md` — a table of every mixin → target class/method (verified against the merged mojmap jar via `javap`) → verdict {(a) remap, (b) new injection point, (c) replace with Fabric API hook, (d) drop} → action. Then execute the table. Veil-related mixins route to Phase 6 or get dropped. Prefer Fabric API events over fragile injections.

**Steps:**
- [ ] 5.1 Write `docs/MIXIN_TRIAGE.md` (all 72 classified). Commit the doc.
- [ ] 5.2 Expand this phase into bite-sized per-mixin steps from the triage table (group by target subsystem).
- [ ] 5.3 Execute remaps/replacements/drops; compile gate after each subsystem group.
- [ ] 5.4 Runtime gate: server boot with mixins applied, no mixin-apply errors in log (`Mixin apply` failures are fatal — grep the log).
- [ ] 5.5 Commit per subsystem group: `git commit -m "Phase 5: mixins <subsystem> → 26.2"`

DoD: all mixins apply cleanly at server boot (client-only mixins validated by user in Phase 8).

---

## Phase 6 — Veil removal → Mac-safe visual rebuild

**Files (scope):** the 38 `foundry.veil.*` importers across `com/sp/render/**`, `com/sp/block/client/**`, `com/sp/clientWrapper/**`, plus deletion of `assets/spb-revamped/pinwheel/**` and the `fsh/vsh/glsl` shader assets.

**Approach:** First task produces `docs/MAC_SAFE_RENDERING.md` cataloguing each Veil effect → Mac-safe replacement (or documented drop). Replacements (no GLSL):
- VHS/camcorder post-FX → a HUD overlay layer registered via the 26.2 HUD API (`HudElementRegistry`-style), composited from translucent fills + tiled noise/scanline textures + REC/timestamp text.
- Flashlight / point lights → vanilla `Blocks.LIGHT` projection with flicker; soft-detect LambDynamicLights.
- Area/entity glow → emissive textures; GLSL-only effects approximated or dropped (documented).

**Steps:**
- [ ] 6.1 Write `docs/MAC_SAFE_RENDERING.md` (per-effect mapping). Commit the doc.
- [ ] 6.2 Delete Veil shader assets + `pinwheel/`; remove dead Veil imports.
- [ ] 6.3 Implement the HUD overlay layer; compile.
- [ ] 6.4 Implement dynamic-light replacement; compile.
- [ ] 6.5 Re-point Phase 4's deferred renderer bits (`// PORT: phase6`) onto vanilla paths; compile.
- [ ] 6.6 Compile gate green for all render packages.
- [ ] 6.7 Commit: `git commit -m "Phase 6: remove Veil; Mac-safe HUD + dynamic-light visuals"`

DoD: render packages compile, no `foundry.veil` references remain (`grep -r foundry.veil src` empty). Visual fidelity is user-verified in Phase 8.

---

## Phase 7 — CCA, networking, sounds, commands, compat

**Files (scope):** `com/sp/cca_stuff/**` (5), `com/sp/networking/**` (10), `com/sp/sounds/**` (16, incl. voicechat), `com/sp/command/**` (5), `com/sp/compat/**` (2 + modmenu), `com/sp/util/**` (5).

**Approach:**
- CCA 5.2.2 → 8.0.0: migrate component registration/entrypoint (`InitializeComponents`) and `custom.cardinal-components` ids per CCA 8 docs.
- Networking → 26.2 payload/codec API (custom packets are `CustomPayload` with `StreamCodec`).
- Simple Voice Chat plugin → 2.6.x API; bump `voicechat-api`.
- Commands → mojmap `CommandSourceStack` predicate permissions (`::isPlayer`), brigadier unchanged.
- ModMenu/MidnightLib config → 26.2; if MidnightLib has no 26.2 build, replace its screen with a minimal ModMenu config screen.
- Finalize compat soft-dep guards from Step 1.4.

**Steps:**
- [ ] 7.1 Port CCA; compile.
- [ ] 7.2 Port networking; compile.
- [ ] 7.3 Port sounds + voicechat plugin; compile.
- [ ] 7.4 Port commands; compile.
- [ ] 7.5 Port compat + modmenu/midnightlib config; compile.
- [ ] 7.6 Compile gate: **whole project** `./gradlew compileJava` green.
- [ ] 7.7 Commit: `git commit -m "Phase 7: CCA/networking/voice/commands/compat → 26.2"`

DoD: entire project compiles.

---

## Phase 8 — Full build, headless verify & handoff

**Steps:**
- [ ] 8.1 `JAVA_HOME=… ./gradlew build` — full build green, jar produced under `build/libs/backrooms-ff-continued-2.0.0.jar`.
- [ ] 8.2 `printf 'stop\n' | JAVA_HOME=… ./gradlew runServer -Dffc.probe=true` — clean boot, all dimensions probed, no exceptions or mixin failures (grep log for `ERROR`/`Mixin apply`).
- [ ] 8.3 Write `docs/CLIENT_PLAYTEST.md`: the macOS `./gradlew runClient` checklist (VHS HUD, entity rendering, dynamic light, voice chat, level traversal) and how to read the per-effect deltas in `MAC_SAFE_RENDERING.md`.
- [ ] 8.4 Commit + final summary: `git commit -m "Phase 8: full build + headless verify green; client playtest handoff"`
- [ ] 8.5 Hand off to user for `runClient` on macOS; iterate on reported issues.

DoD: full build + headless server green; user has a runnable jar and a playtest checklist. Client visuals/gameplay confirmed by user.

---

## Self-review — spec coverage

- Spec §3 toolchain → Phase 1 ✅ · §4 deps → Step 1.4 + Phase 7 ✅ · §6 mappings → Phases 2–4,7 ✅ · §7 Veil/Mac-safe → Phase 6 ✅ · §8 mixins → Phase 5 ✅ · §9 data/assets → Phase 3 ✅ · §10 verification → verification model + Phases 3/8 ✅ · §2 decisions (keep modid, keep all compat w/ guards, git per phase, Mac-safe) → reflected throughout ✅ · §11 risks → mitigations embedded per phase ✅.
- Placeholder scan: deferred per-file code in Phases 3–8 is intentional (compile-driven) and bounded by explicit DoD + the triage-doc tasks (5.1, 6.1), not hand-waving. `// PORT: phaseN` markers are tracked and resolved by named later steps.
- Consistency: probe flag `-Dffc.probe=true`, build alias, mod id `spb-revamped`, archives_base_name `backrooms-ff-continued`, mod_version `2.0.0` used consistently.
