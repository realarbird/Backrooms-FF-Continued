# Mixin Triage — MC 1.20.1 (Yarn) → 26.2 (Mojmap)

Classification of all **72** mixins in `src/main/java/com/sp/mixin/**` for the port. Verdicts on the
non-obvious targets were confirmed by `javap`/`unzip -l` against the merged-deobf 26.2 jar
(`~/.gradle/.../minecraft-merged-deobf-26.2.jar`), not assumed. This is the foundation for Phase 5
(mixin re-targeting) and feeds Phase 6 (Veil removal / Mac-safe render rebuild).

Veil (`foundry.veil.*`) has **no 26.2 build** and is REMOVED — any mixin importing it is render-tier.
The 26.2 client render pipeline is a **deferred `submit()` model**: `MultiBufferSource`/`VertexConsumerProvider`
gone, `GameRenderer.renderWorld`/`bobView`/`bobHurt`/`renderHand`/`getFov`/`updateTargetedEntity` gone
(only `renderLevel(DeltaTracker)` remains), `LevelRenderer.renderSky`/`renderClouds`/`renderWeather`/
`tickRainSplashing`/`processWorldEvent`/`render` gone, HUD moved off `InGameHud.render*` to
`GuiGraphicsExtractor` + `HudElementRegistry`. These facts drive most VEIL/RENDER verdicts below.

---

## Summary

### Counts per verdict
| Verdict | Count |
|---|---|
| REMAP (easy: mojmap names only) | 11 |
| NEW-INJECTION (target exists, method moved/renamed/changed sig) | 22 |
| REPLACE-FABRIC-API | 1 |
| VEIL/RENDER (Phase 6 — drop or heavy rewrite) | 33 |
| DROP (target/feature gone) | 5 |
| **Total** | **72** |

### Counts per subsystem
| Subsystem | Count | Notes |
|---|---|---|
| Veil deferred-light / shadows | 12 | all `foundry.veil.*` — Phase 6, mostly DROP |
| render pipeline (Level/Game/World renderer, PBR, renderlayer, grass, lightmap) | 17 | Phase 6 rebuild |
| HUD / resolution | 4 | HUD reworked; resolution parts survivable |
| player-movement / collision / stamina | 5 | mostly server-logic |
| entity server-logic (sound, damage, AI, particles) | 7 | REMAP/NEW-INJECTION |
| world / chunk / worldgen | 3 | chunk-status gen pipeline gone |
| respawn system | 3 | RespawnConfig rework |
| skin-stolen (chat/menu/spectate) | 5 | client/server, method renames |
| cutscene | 4 | 1 Veil (PathAccessor), rest input/render |
| sound-physics-remastered compat | 3 | `@Pseudo remap=false` — stable, keep |
| networking / server props | 2 | sprint fix, server properties |
| loading screen | 2 | DownloadingTerrainScreen gone |
| misc client (options, inventory, camera, input) | 5 | input/options reworked |

### Server-logic vs Veil/render
- **Veil + render-pipeline (Phase 6):** **33** mixins (12 Veil-light/shadow + 17 render + 4 HUD-ish; some overlap counted once below).
- **Server-side / non-render logic (Phase 5):** ~**31** mixins (entity, AI, world, respawn, networking, sound, compat, props).
- **Compat `@Pseudo` (third-party, `remap=false`):** **3** (Sound Physics Remastered) — untouched by the mojmap remap.

### Surprises (targets fully gone / method vanished)
- `GameRenderer.renderWorld / bobView / bobHurt / renderHand / getFov / updateTargetedEntity / getReachDistance` — **all gone**; only `renderLevel(DeltaTracker)` survives. `GameRendererMixin` is unsalvageable as-is.
- `LevelRenderer` (was `WorldRenderer`) lost `render / renderSky / renderClouds / renderWeather / tickRainSplashing / processWorldEvent` — every WorldRenderer mixin must be re-derived.
- `BodyRotationControl.slowlyAdjustBody` — **gone**; only `clientTick()` remains. `BodyControlMixin` needs a new strategy.
- `ChunkStatus` no longer carries a generation task (the Yarn lambda `method_38284` is gone); generation moved to `ChunkGenerator`/`ChunkStep`/`ChunkPyramid`. `ChunkStatusMixin` cannot retarget 1:1.
- `client.gui.screen.DownloadingTerrainScreen` — **class gone** (replaced by `ReceivingLevelScreen`/`ProgressScreen`).
- `Keyboard.processF3` — `KeyboardHandler` has no debug-key handler method anymore (input reworked).
- `KeyboardInput.jumping` field — not present; input reworked (`calculateImpulse(...)` instead). `@Redirect` PUTFIELD target gone.
- `PlayerInventory.scrollInHotbar` — gone from `Inventory` (selection handling moved).
- `EntityRenderer.renderLabelIfPresent` and `EntityRenderDispatcher.setRenderShadows` — gone (render-state pipeline).
- HUD: `InGameHud.renderHotbar/renderHotbarItem/renderHealthBar/renderStatusBars/...` — none survive on `Gui`.
- Respawn: `ServerPlayerEntity.getSpawnPointPosition / findRespawnPosition / setSpawnPoint` — gone (RespawnConfig). `PlayerList.respawn` signature gained a `RemovalReason`.

### Recommended execution order
1. **Phase 5a — REMAP (do first, lowest risk):** the 11 REMAP mixins; pure mojmap renames.
2. **Phase 5b — NEW-INJECTION server-logic:** entity/AI/world/respawn/networking/options/compat that need a new injection point but no render coupling.
3. **Phase 5c — DROP / decide:** the 5 DROP mixins — confirm the lost behavior is acceptable or schedule a rewrite.
4. **Phase 6 — VEIL/RENDER:** the 33 render-tier mixins, alongside the Mac-safe render rebuild. Most Veil-light/shadow mixins are DROP (no Veil to mix into); render-pipeline ones are rewrites against the deferred API.

---

## Full table

Legend for "Target exists?": ✓ class present in 26.2 / ✗ class gone / ⚠ class present but injected method moved/renamed/changed.

### Root package `com.sp.mixin`

| File | @Mixin target (Yarn) | Mojmap target | Injection(s) | Target exists? | Verdict | Subsystem | Note |
|---|---|---|---|---|---|---|---|
| AOFixMixin | `BlockModelRenderer$AmbientOcclusionCalculator` (targets=) | `client.renderer.block.ModelBlockRenderer$AmbientOcclusionFace` (verify nested name) | `@Inject apply HEAD` | ⚠ + Veil import | VEIL/RENDER | render (AO/chunk) | Imports `foundry.veil.*` + Indigo internals; AO baking reworked. Likely DROP unless AO bug still reproduces. |
| BodyControlMixin | `entity.ai.control.BodyControl` | `world.entity.ai.control.BodyRotationControl` | `@Inject slowlyAdjustBody HEAD cancellable`; `@Shadow entity` | ⚠ method gone | NEW-INJECTION | entity AI | `slowlyAdjustBody` GONE; only `clientTick()`. Field `entity`→`mob`. Re-implement "don't snap body" via `clientTick` wrap or `getMaxHeadYRot`. |
| CameraMixin | `client.render.Camera` | `client.Camera` | `@Inject update TAIL` | ⚠ sig changed | NEW-INJECTION | camera/render | `update(BlockView,Entity,bool,bool,float)` → `update(DeltaTracker)`. Inject point survives, params change. Camera-shake logic ok. |
| ChunkStatusMixin | `world.chunk.ChunkStatus` | `world.level.chunk.status.ChunkStatus` | `@Inject "method_38284(...)" HEAD` (static gen lambda) | ✗ gen task gone | DROP | world/chunk gen | Generation no longer lives on ChunkStatus; the Yarn synthetic lambda is gone. Re-hook backrooms gen elsewhere (ChunkGenerator) — Phase 6/worldgen. |
| DinoDebugRendererMixin | `client.Keyboard` | `client.KeyboardHandler` | `@Inject processF3 HEAD cancellable`; `@Shadow debugLog` | ⚠ method gone | NEW-INJECTION | input/debug | `processF3`/`debugLog` not on `KeyboardHandler`. Debug-key handling reworked; find new hook or drop the IK debug toggle. |
| DisableF3Mixin | `client.gui.hud.DebugHud` | `client.gui.components.DebugScreenOverlay` | `@Inject render HEAD cancellable` | ⚠ method changed | NEW-INJECTION | HUD | `render`→`extractRenderState(GuiGraphicsExtractor)` (deferred). Re-target or just gate via `DebugScreenOverlay.showDebugScreen`. |
| DisableJumpMixin | `client.input.KeyboardInput` | `client.player.KeyboardInput` | `@Redirect tick FIELD jumping:Z PUTFIELD` | ⚠ field gone | NEW-INJECTION | input/movement | `jumping` field not exposed (input reworked, `calculateImpulse`). Needs a different redirect or `ModifyExpressionValue` on the new impulse calc. |
| EntityMixin | `entity.Entity` | `world.entity.Entity` | `@Inject baseTick INVOKE updateSwimming`; `@Inject getSwimSound RETURN`; `@ModifyArg playSwimSound INVOKE playSwimSound(F)` | ✓ all present | REMAP | entity sound/damage | `baseTick`,`getSwimSound`,`playSwimSound(float)`,`updateSwimming` all exist. `@Shadow damage`→`hurtServer(ServerLevel,...)`; `world` field→`level`. Mostly rename. |
| EntityRenderDispatcherMixin | `client.render.entity.EntityRenderDispatcher` | `client.renderer.entity.EntityRenderDispatcher` | `@Inject setRenderShadows TAIL` | ⚠ method gone | VEIL/RENDER | render (shadows) | `setRenderShadows` gone (shadow rendering reworked). Tied to render rebuild. |
| EntityRendererMixin | `client.render.entity.EntityRenderer` | `client.renderer.entity.EntityRenderer` | `@Inject renderLabelIfPresent HEAD cancellable` (uses `VertexConsumerProvider`) | ⚠ method gone | VEIL/RENDER | render (name tags) | `renderLabelIfPresent` gone; name rendering moved into render-state extraction. Hide-name-tags must be re-done in the new pipeline. |
| GameOptionsMixin | `client.option.GameOptions` | `client.Options` | `@ModifyReturnValue getEntityShadows/getGraphicsMode RETURN`; `@Inject getFovEffectScale HEAD`; `@Shadow fovEffectScale:SimpleOption<Double>` | ⚠ renamed | NEW-INJECTION | options | Getters renamed to fluent `entityShadows()`/`fovEffectScale()`; `SimpleOption`→`OptionInstance`. `getGraphicsMode` likely removed. Re-target each. |
| GameRendererMixin | `client.render.GameRenderer` | `client.renderer.GameRenderer` | renderWorld inject/redirect, bobView `@Overwrite`, getFov/reach, shader getters (`renderTypeEntityTranslucentProgram`) + Veil import | ✗ methods gone | VEIL/RENDER | render | `renderWorld/bobView/bobHurt/renderHand/getFov/updateTargetedEntity/getReachDistance` ALL gone; `ShaderProgram` gone. Full Phase 6 rewrite. |
| ItemEntityRendererMixin | `client.render.entity.ItemEntityRenderer` | `client.renderer.entity.ItemEntityRenderer` | `@ModifyArg`×2 + `@Inject render(...VertexConsumerProvider...)` (flatten dropped items) | ⚠ render sig gone | VEIL/RENDER | render | `render(...,VertexConsumerProvider,...)` signature gone (deferred submit). MatrixStack→PoseStack. Phase 6. |
| LevelPropertiesMixin | `world.level.LevelProperties` | `world.level.storage.PrimaryLevelData` (LevelData impl) | `@Inject getLifecycle HEAD cancellable`; `@Shadow lifecycle` | ⚠ verify impl class | NEW-INJECTION | world | `LevelProperties`→`PrimaryLevelData`/`LevelData`. Verify `getLifecycle` survives on the impl; retarget @Mixin to the concrete class. |
| LivingEntityMixin | `entity.LivingEntity` | `world.entity.LivingEntity` | `@Redirect fall INVOKE ServerWorld.spawnParticles(...)` | ⚠ both renamed | NEW-INJECTION | entity (particles) | `fall`→`checkFallDamage`; `ServerWorld.spawnParticles`→`ServerLevel.sendParticles`. Redirect target both move. |
| LivingEntityRendererMixin | `client.render.entity.LivingEntityRenderer` | `client.renderer.entity.LivingEntityRenderer` | `@Inject render(...VertexConsumerProvider...) HEAD`; `@ModifyConstant render opacity` | ⚠ render sig gone | VEIL/RENDER | render | Smiler opacity / capture entity ref; `render(...VertexConsumerProvider...)` gone. Phase 6. |
| MinecraftClientMixin (root) | `client.MinecraftClient` | `client.Minecraft` | `@ModifyArg handleInputEvents setPerspective` (disable F5); `@Inject disconnect HEAD`; `@Inject run INVOKE render(Z)` | ⚠ partly | NEW-INJECTION | client/input | `disconnect(Screen)` survives (rename only); `handleInputEvents`/`render(boolean)` likely renamed (`render(DeltaTracker,boolean)`). Mixed; re-verify each method. |
| MutableUniformAccessMixin | `foundry.veil...MutableUniformAccess` (remap=false) | — (Veil gone) | `@Overwrite` | ✗ Veil | DROP | Veil/shader | Veil interface — no target in 26.2. Delete. |
| PlaySprintSoundMixin | `entity.Entity` | `world.entity.Entity` | `@Redirect playStepSound INVOKE playSound(SoundEvent,F,F)`; `@Local BlockSoundGroup` | ✓ present | REMAP | entity sound | `playStepSound`/`playSound(SoundEvent,float,float)` exist. `BlockSoundGroup`→`SoundType`. Custom `SprintBlockSoundGroup` interface must port too. |
| PlayerEntityMixin | `entity.player.PlayerEntity` | `world.entity.player.Player` | `@Inject tick FIELD noClip:Z AFTER`; `@Inject onDeath HEAD` | ⚠ onDeath renamed | NEW-INJECTION | player-movement | `tick`/`noClip` field survive; `onDeath`→`die(DamageSource)`. Extends Entity ctor sig changes (defineSynchedData etc not needed here). |
| PlayerEntityRendererMixin | `client.render.entity.PlayerEntityRenderer` | `client.renderer.entity.player.PlayerRenderer` | `@Inject setModelPose TAIL`; `@Local PlayerEntityModel` | ⚠ class+model reworked | VEIL/RENDER | render | `PlayerEntityRenderer`→`PlayerRenderer`; `setModelPose` and `PlayerEntityModel` part of render-state rework. Phase 6. |
| PlayerInventoryMixin | `entity.player.PlayerInventory` | `world.entity.player.Inventory` | `@Inject scrollInHotbar HEAD` | ⚠ method gone | NEW-INJECTION | input/inventory | `scrollInHotbar` not on `Inventory` (selection moved; `getSelectedSlot()`). Re-hook hotbar-scroll detection (maybe `Minecraft`/`MouseHandler`). |
| RemoveBlockBreakParticlesMixin | `client.particle.ParticleManager` | `client.particle.ParticleEngine` | `@Inject addBlockBreakingParticles HEAD cancellable` | ⚠ method gone | NEW-INJECTION | render/particles | `ParticleEngine` has no `addBlockBreakingParticles`. Block-crack particles reworked; re-hook (LevelRenderer.destroyBlockProgress or ParticleEngine.crack). |
| ServerPropertiesHandlerMixin | `server.dedicated.ServerPropertiesHandler` | `server.dedicated.DedicatedServerProperties` | `@Unique` field + `getInt(...)`; extends `AbstractPropertiesHandler`→`Settings` | ✓ exists | REMAP | server props | `AbstractPropertiesHandler`→`Settings`; `getInt` helper present. Pure rename + `NewServerProperties` iface stays. |
| StructureBlockEntityMixin | `block.entity.StructureBlockBlockEntity` | `world.level.block.entity.StructureBlockEntity` | `@Inject readNbt RETURN`; `@Shadow size` | ⚠ method changed | NEW-INJECTION | world/BE | `readNbt`→`loadAdditional(ValueInput)`. Retarget inject + NBT read via `ValueInput`. |
| VanillaLightRenderer | `foundry.veil.impl...VanillaLightRenderer` (remap=false) | — (Veil gone) | `@Redirect render ShaderProgram.setFloat` | ✗ Veil | DROP | Veil/light | Veil impl class — gone. Delete. |
| WorldRendererAccessor | `client.render.WorldRenderer` | `client.renderer.LevelRenderer` | `@Invoker renderLayer/setupTerrain/renderEntity`; `@Accessor frustum/entityRenderDispatcher/bufferBuilders` | ✗ members gone | VEIL/RENDER | render | All invoked methods + `bufferBuilders`/`frustum` accessors belong to old immediate-mode renderer. Phase 6. |
| WorldRendererMixin (root) | `client.render.WorldRenderer` | `client.renderer.LevelRenderer` | `@Inject render`×4 (Profiler.swap ordinals), `renderSky`, `renderClouds`, `initBlockIDs`; `@Redirect processWorldEvent SoundManager.play` | ✗ methods gone | VEIL/RENDER | render | `render/renderSky/renderClouds/processWorldEvent` gone; `Profiler.swap` ordinal anchors invalid. Wireframe/warp/PBR-id/sky/cloud all Phase 6. |

### `arealightfix/` — all Veil deferred light

| File | @Mixin target | Mojmap | Injection | Exists? | Verdict | Subsystem | Note |
|---|---|---|---|---|---|---|---|
| AreaLightMixin | `foundry.veil...AreaLight` (remap=false) | — | `@Inject store HEAD`; Veil `Light`/`InstancedLight`/`PositionedLight` supertypes | ✗ Veil | DROP | Veil light | Veil class gone. Delete. |
| AreaLightRendererMixin | `foundry.veil.impl...AreaLightRenderer` (remap=false) | — | `@ModifyArg <init>`, `@Inject setupBufferState HEAD` | ✗ Veil | DROP | Veil light | Veil class gone. Delete. |

### `collision/`

| File | @Mixin target | Mojmap | Injection | Exists? | Verdict | Subsystem | Note |
|---|---|---|---|---|---|---|---|
| IsGroundedCheckMixin | `entity.Entity` | `world.entity.Entity` | **fully commented out** (no active injection) | ✓ | DROP | collision | No active code; only a `CollisionHelper` import. Delete (or revive only if platform collision is reinstated). |
| PlatformCollisionMixin | `entity.Entity` | `world.entity.Entity` | **fully commented out** `@WrapMethod adjustMovementForCollisions` | ✓ method exists | NEW-INJECTION | collision | Body commented out. If re-enabled: `adjustMovementForCollisions(Vec3)` still exists → MixinExtras `@WrapMethod` should work. Low priority. |

### `customatlas/`

| File | @Mixin target | Mojmap | Injection | Exists? | Verdict | Subsystem | Note |
|---|---|---|---|---|---|---|---|
| BakedModelManagerMixin | `client.render.model.BakedModelManager` | `client.resources.model.ModelManager` | `@Inject <clinit> TAIL`; `@Shadow ...`; uses `RenderLayers`/`SpriteAtlasTexture` | ⚠ class moved + render-coupled | VEIL/RENDER | render (atlas) | Registers custom render layers per atlas; depends on `com.sp.render.RenderLayers` (RenderType rework). Phase 6. |

### `cutscene/`

| File | @Mixin target | Mojmap | Injection | Exists? | Verdict | Subsystem | Note |
|---|---|---|---|---|---|---|---|
| ClientPlayerEntityMixin | `client.network.ClientPlayerEntity` | `client.player.LocalPlayer` | `@Inject isCamera HEAD cancellable` | ⚠ renamed | NEW-INJECTION | cutscene | `isCamera`→`isControlledCamera()`. Retarget. |
| ClientPlayerInteractionManagerMixin | `client.network.ClientPlayerInteractionManager` | `client.multiplayer.MultiPlayerGameMode` | `@WrapMethod attackEntity` | ⚠ verify name | NEW-INJECTION | cutscene | `ClientPlayerInteractionManager`→`MultiPlayerGameMode`; `attackEntity` likely `attack(Player,Entity)`. Verify + retarget. |
| MinecraftClientMixin (cutscene) | `client.MinecraftClient` | `client.Minecraft` | `@Inject render INVOKE GameRenderer.render(FJZ)` | ⚠ sig changed | NEW-INJECTION | cutscene/render | `GameRenderer.render(float,long,boolean)`→`render(DeltaTracker,boolean)`; injection anchor changes. |
| PathAccessor | `foundry.veil...anim.Path` (remap=false) | — | `@Accessor frames` | ✗ Veil | DROP | Veil/cutscene anim | Veil animation Path gone. Cutscene path system needs a non-Veil replacement (Phase 6/7). |

### `disableloadingscreen/`

| File | @Mixin target | Mojmap | Injection | Exists? | Verdict | Subsystem | Note |
|---|---|---|---|---|---|---|---|
| DownloadingTerrainScreenMixin | `client.gui.screen.DownloadingTerrainScreen` | — (class gone) | `@Inject render HEAD cancellable` | ✗ class gone | DROP | loading screen | `DownloadingTerrainScreen` replaced by `ReceivingLevelScreen`. Retarget to that if the skip is still wanted, else drop. |
| MinecraftClientMixin (loading) | `client.MinecraftClient` | `client.Minecraft` | `@Redirect joinWorld INVOKE ProgressScreen.setTitle(Text)` | ⚠ method renamed | NEW-INJECTION | loading screen | `ProgressScreen` exists; `joinWorld` likely renamed. Re-verify both. |

### `grass/`

| File | @Mixin target | Mojmap | Injection | Exists? | Verdict | Subsystem | Note |
|---|---|---|---|---|---|---|---|
| VertexBufferMixin | `client.gl.VertexBuffer` | — (`client.gl.*` removed in Mac-safe rebuild) | `@Shadow indexBufferId/drawMode/...` + custom indirect-draw, Veil import | ✗ Veil + GL | VEIL/RENDER | render (grass) | Indirect-draw tessellation grass via Veil + raw GL. Mac-unsafe. Phase 6 drop/rewrite. |
| WindowMixin | `client.util.Window` (remap=false) | `com.mojang.blaze3d.platform.Window` | `@Inject <init> INVOKE glfwWindowHint ordinal=5` | ⚠ verify class/ctor | NEW-INJECTION | render/window | `Window`→`blaze3d.platform.Window`. Adds a GLFW window hint (tessellation/GL profile). Verify ctor GLFW-call ordinal; may be tessellation-only (could drop with grass). |

### `hudandresolution/`

| File | @Mixin target | Mojmap | Injection | Exists? | Verdict | Subsystem | Note |
|---|---|---|---|---|---|---|---|
| DontRenderHandMixin | `client.render.item.HeldItemRenderer` | `client.renderer.ItemInHandRenderer` | `@Redirect renderFirstPersonItem INVOKE renderArmHoldingItem(...VertexConsumerProvider...)` | ⚠ sig gone | VEIL/RENDER | HUD/render | `VertexConsumerProvider` params gone (deferred). Phase 6. |
| InGameHudMixin | `client.gui.hud.InGameHud` | `client.gui.Gui` | many `@Inject renderHotbar/renderHotbarItem/renderHealthBar/renderStatusBars/renderExperienceBar/renderCrosshair/renderVignetteOverlay` + `DrawContext`/Veil `Easings` | ✗ methods gone | VEIL/RENDER | HUD | **DONE (Phase 6c): DELETED** — file + `mixins.json` entry removed. None of those render methods survive on `Gui` (all gone). Overlay role rebuilt as `com.sp.render.gui.BackroomsHudOverlay` (`HudElement`). The vanilla-HUD cosmetic tweaks (hotbar slide / health-hunger opacity / crosshair + experience cancel / vignette cancel) targeted now-removed `Gui.render*` methods → **deferred to Phase 6f** (re-derive against the deferred `Gui.extractRenderState` path; hide-gui is now a param there, not `Options#hudHidden`). |
| MonitorMixin | `client.util.Monitor` | `com.mojang.blaze3d.platform.Monitor` | `@Inject populateVideoModes INVOKE glfwGetMonitorPos`; `@Shadow videoModes` | ⚠ class moved + method renamed | NEW-INJECTION | resolution | `Monitor` moved; `videoModes` field present, `populateVideoModes`→`refreshVideoModes` (verify). VHS aspect-ratio video modes — survivable. |
| VideoOptionsScreenMixin | `client.gui.screen.option.VideoOptionsScreen` | `client.gui.screens.options.VideoOptionsScreen` | `@Redirect init NEW SimpleOption(...)`; `@ModifyArg addSingleOptionEntry`; `@Shadow list:OptionListWidget` | ⚠ heavily reworked | NEW-INJECTION | resolution/options | `SimpleOption`→`OptionInstance`, `OptionListWidget`→`OptionsList`, options screen rebuilt. High effort; verify each anchor. |

### `lightmap/`

| File | @Mixin target | Mojmap | Injection | Exists? | Verdict | Subsystem | Note |
|---|---|---|---|---|---|---|---|
| PoolroomsSkylightColor | `client.render.LightmapTextureManager` | `client.renderer.LightTexture` | `@Redirect update INVOKE Vector3f.lerp(...) ordinal=0` | ⚠ class moved + method | VEIL/RENDER | render (lightmap) | `LightmapTextureManager`→`LightTexture`; `update` reworked. Poolrooms day-cycle sky tint. Phase 6 (verify `update`/`updateLightTexture`). |

### `lightshadows/` — all Veil deferred-light / shadow renderer

| File | @Mixin target | Mojmap | Injection | Exists? | Verdict | Subsystem | Note |
|---|---|---|---|---|---|---|---|
| IndirectLightRendererMixin | `foundry.veil...IndirectLightRenderer` (remap=false) | — | `@Overwrite` + Veil shadows | ✗ Veil | DROP | Veil shadows | Veil gone. Delete. |
| IndirectPointLightRendererMixin | `foundry.veil.impl...IndirectPointLightRenderer` (remap=false) | — | `@Inject setupBufferState` ×3 | ✗ Veil | DROP | Veil shadows | Veil gone. Delete. |
| InstancedLightRendererMixin | `foundry.veil...InstancedLightRenderer` (remap=false) | — | `@Shadow lightSize` (redirect commented) | ✗ Veil | DROP | Veil shadows | Veil gone. Delete. |
| InstancedPointLightRendererMixin | `foundry.veil.impl...InstancedPointLightRenderer` (remap=false) | — | `@Inject setupBufferState` ×3 | ✗ Veil | DROP | Veil shadows | Veil gone. Delete. |
| LightRendererMixin | `foundry.veil...LightRenderer` (remap=false) | — | `@Inject applyShader INVOKE ShaderProgram.bind` (shadow-map hook) | ✗ Veil | DROP | Veil shadows | Drives `com.sp.render.ShadowMapRenderer` via Veil. Whole shadow-map feature is Phase 6 rewrite/drop. |
| VeilShadersMixin | `foundry.veil...VeilShaders` | — | `@Shadow` ×2 | ✗ Veil | DROP | Veil shaders | Veil gone. Delete. |

### `pbr/` — custom PBR vertex pipeline (immediate-mode)

| File | @Mixin target | Mojmap | Injection | Exists? | Verdict | Subsystem | Note |
|---|---|---|---|---|---|---|---|
| BufferBuilderMixin | `client.render.BufferBuilder` | — (`BufferBuilder` reworked) | `@ModifyVariable begin`, `@Inject next`; custom `VertexFormats` + `BlockMaterial` | ✗ API gone | VEIL/RENDER | render (PBR) | Old `BufferBuilder.begin/next/nextElement/putFloat` immediate-mode API gone. Custom PBR vertex format. Phase 6 rewrite/drop. |
| ChunkBuilderMixin | `client.render.chunk.ChunkBuilder$BuiltChunk$RebuildTask` | — (chunk render reworked) | `@Redirect render beginBufferBuilding(BufferBuilder)` | ✗ nested gone | VEIL/RENDER | render (PBR) | Chunk build pipeline (`SectionRenderDispatcher` now) restructured. Phase 6. |
| RebuildTaskMixin | `client.render.chunk.ChunkBuilder$BuiltChunk$RebuildTask` | — | `@Inject render INVOKE BlockRenderManager.renderBlock(...)` ×2 | ✗ nested gone | VEIL/RENDER | render (PBR) | Tags PBR block ids during chunk rebuild; pipeline gone. Phase 6. |

### `rain/`

| File | @Mixin target | Mojmap | Injection | Exists? | Verdict | Subsystem | Note |
|---|---|---|---|---|---|---|---|
| AlwaysRainLevelPropertiesMixin | `world.level.LevelProperties` | `world.level.storage.PrimaryLevelData` | `@WrapMethod getRainTime/isRaining/getThunderTime/isThundering` | ⚠ impl class | NEW-INJECTION | world/weather | Retarget to `PrimaryLevelData` (LevelData impl). Methods exist; `@WrapMethod` should hold. Server-logic. |
| LightningSkyLightingUpLightningEntity | `entity.LightningEntity` | `world.entity.LightningBolt` | `@WrapMethod tick/spawnFire` | ⚠ verify spawnFire | NEW-INJECTION | entity/weather | `LightningEntity`→`LightningBolt`. `tick` exists; verify `spawnFire`. Mostly rename. |
| NoRainParticlesWorldRendererMixin | `client.render.WorldRenderer` | `client.renderer.LevelRenderer` | `@WrapOperation tickRainSplashing addParticle`, `renderWeather BufferBuilder.vertex(...)` ×2 | ✗ methods gone | VEIL/RENDER | render (weather) | `tickRainSplashing`/`renderWeather` + `BufferBuilder.vertex` gone. Phase 6. |
| ServerWorldNoHorseTrapMixin | `server.world.ServerWorld` | `server.level.ServerLevel` | `@WrapOperation tickChunk spawnEntity(Entity)` | ⚠ method names | NEW-INJECTION | world (server) | `ServerLevel.tickChunk` exists; `spawnEntity`→`addFreshEntity`. Server-logic; verify `tickChunk` anchor. |

### `renderlayer/`

| File | @Mixin target | Mojmap | Injection | Exists? | Verdict | Subsystem | Note |
|---|---|---|---|---|---|---|---|
| RenderLayerMixin | `client.render.RenderLayer` | `client.renderer.rendertype.RenderType` | `@ModifyReturnValue getBlockLayers RETURN` | ✗ API reworked | VEIL/RENDER | render | Injects custom block render layers; `RenderLayer.getBlockLayers` + `RenderType`/`RenderTypes` rework. Phase 6. |
| WorldRendererMixin (renderlayer) | `client.render.WorldRenderer` | `client.renderer.LevelRenderer` | `@Shadow renderLayer(...)`; `@Inject render INVOKE renderLayer(...) ordinal=0` | ✗ method gone | VEIL/RENDER | render | `renderLayer` immediate-mode method gone. Phase 6. |

### `respawnsystem/`

| File | @Mixin target | Mojmap | Injection | Exists? | Verdict | Subsystem | Note |
|---|---|---|---|---|---|---|---|
| DeathScreenMixin | `client.gui.screen.DeathScreen` | `client.gui.screens.DeathScreen` | `@Redirect init ButtonWidget.builder(...)` ×2; `@Inject tick TAIL`; `@Shadow ticksSinceDeath/setButtonsActive` | ⚠ Button rework | NEW-INJECTION | respawn/HUD | `ButtonWidget`→`Button` (`builder(Component,OnPress)`); verify `init`/`setButtonsActive`. GUI button rename; survivable. |
| LivingEntityRendererMixin (respawn) | `client.render.entity.LivingEntityRenderer` | `client.renderer.entity.LivingEntityRenderer` | `@Inject render(...VertexConsumerProvider...) INVOKE getBuffer/EntityModel.render` + Veil `VeilRenderSystem`/`ShaderProgram`/`Easings` | ✗ + Veil | VEIL/RENDER | render | Respawn fade shader via Veil + dead render sigs. Phase 6. |
| PlayerManagerMixin | `server.PlayerManager` | `server.players.PlayerList` | `@Inject respawnPlayer HEAD`; `@Redirect getSpawnPointPosition/getWorld/findRespawnPosition`; `@ModifyArgs refreshPositionAndAngles/setSpawnPoint` | ⚠ respawn API gone | NEW-INJECTION | respawn (server) | `respawnPlayer`→`respawn(ServerPlayer,bool,RemovalReason)`; `getSpawnPointPosition`/`findRespawnPosition`/`setSpawnPoint` GONE (RespawnConfig). `refreshPositionAndAngles`→`snapTo`. Heavy server-logic rewrite. |

### `skinstolen/`

| File | @Mixin target | Mojmap | Injection | Exists? | Verdict | Subsystem | Note |
|---|---|---|---|---|---|---|---|
| ChatScreenMixin | `client.gui.screen.ChatScreen` | `client.gui.screens.ChatScreen` | `@Redirect sendMessage INVOKE ClientPlayNetworkHandler.sendChatMessage/sendChatCommand` | ⚠ renamed | NEW-INJECTION | skin-stolen | `ClientPlayNetworkHandler`→`ClientPacketListener`; `sendChatMessage`→`sendChat`, `sendChatCommand`→`sendCommand`. Verify `sendMessage` host method. |
| GameMenuScreenMixin | `client.gui.screen.GameMenuScreen` | `client.gui.screens.PauseScreen` | `@Redirect initWidgets ButtonWidget.builder(...) ordinal=1` | ⚠ class+button rename | NEW-INJECTION | skin-stolen | `GameMenuScreen`→`PauseScreen`; `ButtonWidget`→`Button`; verify `initWidgets`/`createPauseMenu`. |
| MinecraftClientMixin (skinstolen) | `client.MinecraftClient` | `client.Minecraft` | `@Redirect handleInputEvents INVOKE setScreen(Screen) ordinal=1` | ⚠ verify method | NEW-INJECTION | skin-stolen | `setScreen` survives; `handleInputEvents` likely present. Verify ordinal/anchor. |
| ServerPlayerEntityMixin | `server.network.ServerPlayerEntity` | `server.level.ServerPlayer` | `@Redirect tick/attack setCameraEntity(Entity)`; `@Inject canBeSpectated HEAD cancellable` | ✓ likely | REMAP | skin-stolen (server) | `setCameraEntity` exists; `canBeSpectated` verify. Mostly rename — server-logic. |

### `soundphysicsremasteredcompat/` — third-party `@Pseudo remap=false`

| File | @Mixin target | Mojmap | Injection | Exists? | Verdict | Subsystem | Note |
|---|---|---|---|---|---|---|---|
| AllowedSoundsConfigMixin | `com.sonicether...AllowedSoundConfig` `@Pseudo` | n/a (third-party) | `@Inject createDefaultMap RETURN remap=false` | n/a | REMAP | SFR compat | Targets SoundPhysics, not MC. `remap=false` — survives the MC remap untouched. Keep as-is; only `ModSounds` refs port. |
| SFRReflectivityMixin | `com.sonicether...ReflectivityConfig` (targets=) `@Pseudo` | n/a | `@Inject addDefaults TAIL remap=false`; extends `BlockSoundConfigBase` | n/a | REMAP | SFR compat | Third-party; keep. Only `SprintBlockSoundGroup` (mod side) must port. |
| SoundTypesMixin | `com.sonicether...SoundTypes` (targets=) `@Pseudo` | n/a | `@Inject <clinit> INVOKE Collections.unmodifiableMap remap=false` | n/a | REPLACE-FABRIC-API | SFR compat | Third-party; `BlockSoundGroup`→`SoundType` on the MC-referenced side. Keep mixin; check SFR 26.2 build's internal API. |

### `stamina/`

| File | @Mixin target | Mojmap | Injection | Exists? | Verdict | Subsystem | Note |
|---|---|---|---|---|---|---|---|
| ContinuedSprintFix | `server.network.ServerPlayNetworkHandler` | `server.network.ServerGamePacketListenerImpl` | `@Inject onClientCommand INVOKE ServerPlayerEntity.setSprinting(Z)` ×2; `implements ServerPlayNetworkSprint` | ⚠ method renamed | NEW-INJECTION | networking/stamina | `onClientCommand`→`handlePlayerCommand(ServerboundPlayerCommandPacket)`; `setSprinting` exists. `ServerPlayerEntity`→`ServerPlayer`. Server-logic; retarget method + 2 ordinals. |

---

## Access widener needs (rebuild minimally — mojmap names)

The current `spb-revamped.accesswidener` is **disabled** in `build.gradle` and almost entirely dead
render internals (Yarn names + Veil-era classes). Only entries used by **surviving non-render** mixins
should be reinstated; everything tied to `WorldRenderer`/`BufferBuilder`/`SoundSystem`/`ChunkBuilder`/
`BlockModelRenderer`/`Frustum` belongs to the Phase 6 render rebuild and should stay out until then.

**Likely still needed by surviving mixins (mojmap):**
- `accessible field net/minecraft/world/entity/Mob goalSelector Lnet/minecraft/world/entity/ai/goal/GoalSelector;` — was `MobEntity.goalSelector`. Needed only if entity-AI code still pokes the selector directly (mod adds goals in entity classes; confirm during Phase 4/5). Mojmap field is `goalSelector` on `Mob`.
- `accessible method net/minecraft/world/entity/player/Inventory add (Lnet/minecraft/world/item/ItemStack;)I` — was `PlayerInventory.addStack`. Verify the new method name (`add`) and whether any surviving code needs it.
- `MeleeAttackGoal` fields (`path`, `lastUpdateTime`) — only if the custom melee-goal subclass still extends it; per PORTING_MAP these are now PRIVATE so the subclass mirrors them locally and **no AW entry is needed**.

**Drop from the AW (dead — Veil/render-pipeline, gone in 26.2):**
- All `client/render/WorldRenderer*` (ChunkInfo, chunkInfos, blockBreakingProgressions, bufferBuilders, frustum, RenderableChunks), `Frustum.frustumIntersection`, `Camera.setRotation`/`GameRenderer.getFov` — old immediate-mode renderer.
- All `client/render/block/BlockModelRenderer$*` (NeighborData/BrightnessCache/Translation/BRIGHTNESS_CACHE) — AO baking rework.
- `client/sound/SoundSystem.sounds`, `client/sound/SoundManager.soundSystem` — sound internals (Yarn `SoundSystem`→`SoundEngine`); only re-add if a surviving sound mixin needs reflection.
- `RenderSystem$ShapeIndexBuffer` (`id`, `grow`), `client/render/chunk/ChunkBuilder$BuiltChunk$RebuildTask*`, `VertexFormatElement$Type$*`, `SimpleOption$Callbacks`, `SpriteAtlasTexture.getHeight/getWidth` — all render-pipeline; rebuild as needed in Phase 6 with mojmap names.
- `Chunk.blockEntityNbts` — GONE in 26.2 (use `level.removeBlockEntity(pos)`), per PORTING_MAP. Drop.

Net: the AW can probably come back as **near-empty** (0–2 entries) for the Phase 5 server-logic mixins,
with render-related widening deferred to Phase 6 and written fresh against mojmap.
