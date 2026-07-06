package com.sp;

import com.sp.cca_stuff.InitializeComponents;
import com.sp.cca_stuff.PlayerComponent;
import com.sp.cca_stuff.WorldEvents;
import com.sp.compat.modmenu.ConfigStuff;
import com.sp.entity.client.model.SmilerModel;
import com.sp.entity.client.renderer.SkinWalkerRenderer;
import com.sp.entity.client.renderer.SmilerRenderer;
import com.sp.entity.client.renderer.WalkerRenderer;
import com.sp.init.*;
import com.sp.networking.C2S.SeeActiveSkinwalkerSync;
import com.sp.networking.C2S.SyncServerComponent;
import com.sp.networking.InitializePackets;
import com.sp.networking.callbacks.ClientConnectionEvents;
import com.sp.render.FlashlightRenderer;
import com.sp.render.PoolroomsDayCycle;
import com.sp.render.bird.BirdQuality;
import com.sp.render.bird.FlockManager;
import com.sp.render.camera.CameraShake;
import com.sp.render.camera.CutsceneManager;
import com.sp.render.grass.GrassRenderer;
import com.sp.render.gui.BackroomsHudOverlay;
import com.sp.render.gui.TitleText;
import com.sp.render.pbr.BlockIdMap;
import com.sp.render.pbr.PbrRegistry;
import com.sp.render.post.BackroomsPostEffects;
import com.sp.util.MathStuff;
import com.sp.util.TickTimer;
import com.sp.world.levels.BackroomsLevel;
import com.sp.world.levels.custom.InfiniteGrassBackroomsLevel;
import com.sp.world.levels.custom.Level2BackroomsLevel;
import com.sp.world.levels.custom.PoolroomsBackroomsLevel;
import de.maxhenkel.voicechat.voice.client.ClientManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.Vector;


/**
 * Mac-safe client entrypoint (Phase 6e-b). Ported Yarn(1.20.1)→mojmap(26.2) and rebuilt without Veil.
 *
 * <p>PORT: delta — every Veil hook is removed:
 * <ul>
 *   <li>the {@code VeilEventPlatform.onVeilRenderTypeStageRender}/{@code preVeilPostProcessing} render-stage
 *       events (which drove the shadow-map renderer, the VHS/SSAO/everything/mixed/glitch GLSL programs,
 *       motion-vector uniforms and the per-stage flashlight/grass/bird draws) are GONE. 26.2 has no
 *       Mac-safe per-render-stage hook (no {@code WorldRenderEvents} in this fabric-api build, and the
 *       deferred {@code submit()} pipeline has no immediate-mode draw), so the salvageable, non-GLSL
 *       parts (flashlight light-block projection, skinwalker line-of-sight sync, bird flock tick,
 *       camera-shake sampling, camera-effect HUD flags) were moved onto {@link ClientTickEvents}.</li>
 *   <li>the VHS post chain is now the Mac-safe {@link BackroomsHudOverlay} HUD element (Phase 6c) — this
 *       class only feeds it state flags ({@code shouldRenderCameraEffect}, {@code blackScreen},
 *       {@code youCantEscape}).</li>
 *   <li>the Veil deferred light renderer enable/disable/reset calls are dropped; dynamic light is now
 *       vanilla {@code Blocks.LIGHT} via {@link FlashlightRenderer} (Phase 6d).</li>
 *   <li>{@code ShadowMapRenderer}, {@code SSAOSamples}, {@code PreviousUniforms}, {@code SkinwalkerJumpscare}
 *       uniform pokes, {@code BirdRenderer} (Veil compute) — all Veil/GLSL, dropped (HUD-flag equivalents
 *       live in {@link BackroomsHudOverlay}).</li>
 *   <li>{@code GeckoLibUtil.addCustomBakedModelFactory(MOD_ID, new MowzieModelFactory())} removed —
 *       GeckoLib 5 has no such API and {@code MowzieModelFactory} was deleted (6b/6e-b).</li>
 *   <li>the original macOS-incompatibility toast is removed: this IS the Mac-safe build.</li>
 * </ul>
 *
 * <p>PORT: phase6f — the {@code mixin/CameraMixin} applies {@link CameraShake#shakeYaw}/{@code shakePitch}
 * and {@code CameraRoll} to the camera; nothing here drives the camera rotation directly anymore. The
 * F3/debug-overlay suppression that was {@code options.debugEnabled = false} also moves to a 6f mixin
 * ({@code DisableF3Mixin}) — 26.2's {@code DebugScreenOverlay} has no public setter.</p>
 */
public class SPBRevampedClient implements ClientModInitializer {
    private GrassRenderer grassRenderer;
    private static final CutsceneManager cutsceneManager = new CutsceneManager();
    private static final CameraShake cameraShake = new CameraShake();
    private final FlashlightRenderer flashlightRenderer = new FlashlightRenderer();

    static boolean inBackrooms = false;
    public static boolean isLightning = false;

    public static TickTimer tickTimer = new TickTimer();
    public static boolean blackScreen;
    public static boolean youCantEscape;

    private static boolean shouldBeUnmuted = false;

    public static boolean shouldRenderWarp = false;

    @Override
    public void onInitializeClient() {
        // HUD elements (BackroomsHudOverlay / StaminaBar / TitleText) are registered by
        // com.sp.render.gui.BackroomsHudClientInit (a separate client entrypoint) — not re-registered here.

        InitializePackets.registerS2CPackets();

        ModKeyBinds.initializeKeyBinds();

        FlockManager.init();

        // PORT: delta (A6) — the Fabric `BlockRenderLayerMap` (block→RenderType mapping) used in 1.20.1 was
        // REMOVED from fabric-api in 26.2; block render type is now DATA-DRIVEN via the `"render_type"` field
        // in each block's blockstate/model JSON. The original cutout mappings (BOTTOM_TRIM, the WALL_TEXT_*/
        // WALL_ARROW_*/WALL_SMALL_*/WALL_DRAWING_* signs, RUG_1/2, RED_METAL_CASING), the WINDOW translucent
        // mapping, and the POOLROOMS_SKY_BLOCK layer therefore have no Java registration here — they belong in
        // the model JSON (asset task, out of scope for this Java-only phase). RenderLayers.getPoolroomsSky()
        // (a vanilla translucent layer) is the value those JSONs should use for the sky block.

        // PORT: phase6d — the 3 fluorescent-light BlockEntityRenderers were DELETED (light is now a flat
        // emissive texture + vanilla light level). Their BlockEntityRenderers.register(...) calls are
        // removed; no surviving BER needs registration.

        EntityRendererRegistry.register(ModEntities.SKIN_WALKER_ENTITY, SkinWalkerRenderer::new);
        EntityRendererRegistry.register(ModEntities.WALKER_ENTITY, WalkerRenderer::new);
        EntityRendererRegistry.register(ModEntities.SMILER_ENTITY, SmilerRenderer::new);

        // PORT: EntityModelLayerRegistry.registerModelLayer → ModelLayerRegistry.registerModelLayer in
        // this fabric-api build; the supplier is a TexturedLayerDefinitionProvider (createLayerDefinition()).
        ModelLayerRegistry.registerModelLayer(ModModelLayers.SMILER, SmilerModel::getTexturedModelData);

        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "after_resources");
            }

            @Override
            public void onResourceManagerReload(ResourceManager manager) {
                BlockIdMap.registerBlockID((blockIdMap) -> {
                    blockIdMap.put(ModBlocks.POOL_TILES, 18);
                    blockIdMap.put(ModBlocks.CEILINGLIGHT, 15);
                    blockIdMap.put(ModBlocks.EMERGENCY_LIGHT, 19);
                    blockIdMap.put(ModBlocks.POWER_POLE, 20);
                    blockIdMap.put(ModBlocks.PLASTIC, 22);
                    blockIdMap.put(ModBlocks.PILLAR, 22);
                    blockIdMap.put(ModBlocks.RED_METAL_CASING, 23);
                    blockIdMap.put(ModBlocks.POLE, 24);
                    blockIdMap.put(ModBlocks.WINDOW, 25);
                    blockIdMap.put(ModBlocks.FLOOR_TILING, 26);
                });

                PbrRegistry.registerPBR(ModBlocks.CARPET_BLOCK,      new PbrRegistry.PbrMaterial(false, 0.0f,1.25f, 1024));
                PbrRegistry.registerPBR(ModBlocks.CEILING_TILE,      new PbrRegistry.PbrMaterial(false, 0.0f,1.0f,  512));
                PbrRegistry.registerPBR(ModBlocks.GHOST_CEILING_TILE,new PbrRegistry.PbrMaterial(false, 0.0f,1.0f,  512));

                PbrRegistry.registerPBR(ModBlocks.CONCRETE_BLOCK_11, new PbrRegistry.PbrMaterial(true, 0.4f,8.0f,  1024));
                PbrRegistry.registerPBR(ModBlocks.BRICKS,            new PbrRegistry.PbrMaterial(true, 0.45f,5.0f,   2048));
                PbrRegistry.registerPBR(ModBlocks.CHAINFENCE,        new PbrRegistry.PbrMaterial(true, 0.21f,2.8f,   1024));
                PbrRegistry.registerPBR(ModBlocks.WOODEN_CRATE,      new PbrRegistry.PbrMaterial(true, 0.9f, 1.0f,   1024));
                PbrRegistry.registerPBR(ModBlocks.ROAD,              new PbrRegistry.PbrMaterial(true, 0.37f,8.0f,   1024));
                PbrRegistry.registerPBR(ModBlocks.FLOOR_TILING,      new PbrRegistry.PbrMaterial(true, 0.37f,8.0f,   1024));
                PbrRegistry.registerPBR(ModBlocks.PAVEMENT,          new PbrRegistry.PbrMaterial(true, 0.37f,8.0f,   1024));

                BlockIdMap.init = false;
            }
        });

        ClientPlayConnectionEvents.JOIN.register(((handler, sender, client) -> {
            // PORT: phase6 — Veil deferred-renderer reset() dropped (no Veil renderer).
            if (client.level != null) {
                HelpfulHintManager.sendMessages(client.player);

                //*Just in case it become unsynced
                BackroomsLevels.getLevel(client.level).ifPresent((backroomsLevel -> {
                    if (backroomsLevel instanceof PoolroomsBackroomsLevel poolroomsBackroomsLevel) {
                        PoolroomsDayCycle.dayTime = poolroomsBackroomsLevel.getTimeOfDay();
                    }
                }));
            }
        }));

        ClientConnectionEvents.DISCONNECT.register(client -> {
            LocalPlayer player = client.player;
            if (player != null) {
                PlayerComponent playerComponent = InitializeComponents.PLAYER.get(player);
                playerComponent.setFlashLightOn(false);
                flashlightRenderer.clearFlashlights();
                playerComponent.setDoingCutscene(false);
            }

            BackroomsPostEffects.onDisconnect(client);

            cutsceneManager.reset();

            if (this.grassRenderer != null) {
                this.grassRenderer.close();
                this.grassRenderer = null;
            }
        });

        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            cutsceneManager.reset();
            if (this.grassRenderer != null) {
                this.grassRenderer.close();
                this.grassRenderer = null;
            }
        });


        // PORT: ClientTickEvents.END_WORLD_TICK → END_LEVEL_TICK (renamed in 26.2 fabric-api); the lambda
        // now receives the ClientLevel, which this body does not use (it reads Minecraft.getInstance()).
        ClientTickEvents.END_LEVEL_TICK.register((clientLevel) -> {
            Vector<TickTimer> tickTimers = TickTimer.getAllInstances();
            if (!tickTimers.isEmpty()) {
                for (TickTimer timer : tickTimers) {
                    timer.addCurrentTick();
                }
            }

            //Fixes Minecraft spectating not loading chunks bug
            Minecraft client1 = Minecraft.getInstance();
            Player player = client1.player;
            if (player != null) {
                if (player != client1.getCameraEntity() && client1.getCameraEntity() != null) {
                    Vec3 pos = client1.getCameraEntity().position();
                    player.setPos(pos.x, pos.y, pos.z);
                }
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register((client) -> {
            // Cutscene drives the camera path + black-screen state (was ticked off the Veil render stage).
            cutsceneManager.tick();

            if (cutsceneManager.isPlaying) {
                if (!ClientManager.getPlayerStateManager().isMuted()) {
                    shouldBeUnmuted = true;
                    ClientManager.getPlayerStateManager().setMuted(true);
                }
            } else if (shouldBeUnmuted) {
                ClientManager.getPlayerStateManager().setMuted(false);
                shouldBeUnmuted = false;
            }

            Player playerClient = client.player;
            if (playerClient != null && client.level != null) {
                //*Main Set in Backrooms
                setInBackrooms(BackroomsLevels.isInBackrooms(playerClient.level().dimension()));

                // Camera shake (sampled each tick; the 6f CameraMixin applies shakeYaw/shakePitch + roll).
                if (client.gameRenderer != null && client.gameRenderer.mainCamera() != null) {
                    cameraShake.tick(client.gameRenderer.mainCamera());
                }

                // PORT: delta — flashlight light-block projection was a per-render-stage Veil draw;
                // it is now ticked here (vanilla Blocks.LIGHT, Phase 6d).
                float partialTicks = client.getDeltaTracker().getGameTimeDeltaPartialTick(true);
                flashlightRenderer.renderFlashlightForEveryPlayer(partialTicks);

                PlayerComponent localComponent = InitializeComponents.PLAYER.get(playerClient);

                // Feed the Mac-safe VHS HUD overlay (Phase 6c) its state.
                BackroomsHudOverlay.cameraEffectEnabled = shouldRenderCameraEffect();
                BackroomsHudOverlay.blackScreen = blackScreen
                        || (playerClient.isInWall() && !getCutsceneManager().isPlaying)
                        || localComponent.isBeingReleased();
                BackroomsHudOverlay.youCantEscape = youCantEscape;
                // Intro title overlay gating (was driven by the Veil HUD path).
                TitleText.introActive = cutsceneManager.backroomsBySP;
                TitleText.introBlackScreen = cutsceneManager.blackScreen.isBlackScreen;

                // PORT: delta — skinwalker visibility sync. The original tested the Veil frustum +
                // line-of-sight from the render stage; with no per-frame frustum here it is reduced to a
                // line-of-sight check on the client tick (the box-in-frustum gate is dropped).
                WorldEvents events = InitializeComponents.EVENTS.get(client.level);
                Entity activeSkinwalker = events.getActiveSkinwalkerTarget();
                if (activeSkinwalker != null) {
                    boolean visible = playerClient.hasLineOfSight(activeSkinwalker);
                    if (visible) {
                        if (!localComponent.canSeeActiveSkinWalkerTarget()) {
                            localComponent.setCanSeeActiveSkinWalkerTarget(true);
                            ClientPlayNetworking.send(new SeeActiveSkinwalkerSync(true));
                        }
                    } else if (localComponent.canSeeActiveSkinWalkerTarget()) {
                        localComponent.setCanSeeActiveSkinWalkerTarget(false);
                        ClientPlayNetworking.send(new SeeActiveSkinwalkerSync(false));
                    }
                }

                if (shouldRenderCameraEffect() && isInBackrooms()) {
                    HelpfulHintManager.disableSuffocateHint();
                }

                getCurrentBackroomsLevel().ifPresent((backroomsLevel -> {
                    if (backroomsLevel instanceof InfiniteGrassBackroomsLevel && ConfigStuff.birdQuality != BirdQuality.DISABLED) {
                        FlockManager.tick();
                    }
                }));
            }
        });

    }

    public static float getWarpTimer(Level world) {
        if (!(BackroomsLevels.getLevel(world).orElse(BackroomsLevels.OVERWORLD_REPRESENTING_BACKROOMS_LEVEL) instanceof Level2BackroomsLevel level)) {
            return 0;
        }

        if (level.isWarping() || tickTimer.getCurrentTick() != 0) {
            tickTimer.setOnOrOff(true);
            float x = tickTimer.getCurrentTick();
            float w = 0.03141592f;
            float result = MathStuff.mod((x * w) * 0.002f, w);
            if (result == 0 || (!level.isWarping() && result == 0.03141592f / 2)) {
                tickTimer.resetToZero();
            }
            return result;

        } else {
            tickTimer.setOnOrOff(false);
            return 0;
        }
    }

    public static boolean finishedWarp(Level world) {
        float warp = getWarpTimer(world);
        return warp == 0 || warp == 0.03141592f / 2;
    }

    public static void sendComponentSyncPacket(boolean value, String component) {
        ClientPlayNetworking.send(new SyncServerComponent(value, component));
    }

    public static boolean isInBackrooms() {
        return inBackrooms;
    }

    public static boolean shouldRenderCameraEffect() {
        return (!isInBackrooms() && ConfigStuff.enableVhsEffect) || (isInBackrooms() && ConfigStuff.enableVhsEffectInTheBackrooms);
    }

    public static void setInBackrooms(boolean inBackrooms) {
        SPBRevampedClient.inBackrooms = inBackrooms;
    }

    public static CutsceneManager getCutsceneManager() {
        return cutsceneManager;
    }

    public static CameraShake getCameraShake() {
        return cameraShake;
    }

    public static Optional<BackroomsLevel> getCurrentBackroomsLevel() {
        return BackroomsLevels.getLevel(Minecraft.getInstance().level);
    }

    // Client-only UI sound playback for SoundPacket. Lives here (a @Environment(CLIENT) class) so the
    // SoundPacket payload record stays free of net.minecraft.client.* references and can be class-loaded
    // on a dedicated server when its TYPE/CODEC are registered. Mirrors how the other S2C packets delegate
    // their client-side effect to SPBRevampedClient.
    public static void playUISound(Holder<SoundEvent> sound, float volume, float pitch) {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(sound.value(), pitch, volume));
    }

    public static boolean isInLevel(BackroomsLevel level) {
        if (Minecraft.getInstance().level == null) {
            return false;
        }

        return BackroomsLevels.isInBackroomsLevel(Minecraft.getInstance().level, level);
    }
}
