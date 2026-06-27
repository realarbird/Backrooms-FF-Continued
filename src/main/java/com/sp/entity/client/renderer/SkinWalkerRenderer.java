package com.sp.entity.client.renderer;

import com.sp.SPBRevamped;
import com.sp.cca_stuff.InitializeComponents;
import com.sp.cca_stuff.SkinWalkerComponent;
import com.sp.entity.client.model.SkinWalkerModel;
import com.sp.entity.client.model.SkinWalkerRenderData;
import com.sp.entity.custom.SkinWalkerEntity;
import com.sp.entity.ik.model.GeckoLib.GeoModelAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelType;

import com.geckolib.cache.model.BakedGeoModel;
import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.BoneSnapshots;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.GeoRenderer;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.layer.builtin.CustomBoneTextureGeoLayer;

import java.util.List;

/**
 * PORT: delta (GeckoLib 4 -> 5 / 1.20.1 -> 26.2). The GeckoLib-4 renderer extended
 * {@code DynamicGeoEntityRenderer} and overrode {@code preRender}/{@code renderRecursively}/
 * {@code createVerticesOfQuad}/{@code getTextureOverrideForBone}/{@code getRenderTypeOverrideForBone}/
 * {@code boneRenderOverride}. None of those exist in GeckoLib 5 (no DynamicGeoEntityRenderer, immutable
 * bones, no custom recursion). Rebuilt on the v5 render-state pipeline:
 * <ul>
 *   <li><b>Model / texture / true-form selection</b> (slim vs default vs final-form geo, captured-player
 *       skin vs placeholder) — computed in {@link #extractRenderState} and stashed on the render-state via
 *       {@link SkinWalkerRenderData}; {@code SkinWalkerModel} reads them back. (Was {@code getModelResource(entity)}/
 *       {@code getTextureResource(entity)}.)</li>
 *   <li><b>Procedural pose</b> ({@code animateModel}: head pitch/yaw, arm/leg swing, punch, sneak) — the
 *       needed scalars are captured into {@link SkinWalkerRenderState} and re-applied per-frame through
 *       {@link #adjustModelBonesForRender} operating on {@link BoneSnapshots} (v5's supported posing hook,
 *       replacing the old direct {@code bone.setRotX(...)} during recursion).</li>
 *   <li><b>Per-bone textures</b> — eyes-emissive {@code headwear}, spider-leg texture, final-form head
 *       texture: each via a {@link CustomBoneTextureGeoLayer} (v5's per-bone texture override).</li>
 *   <li><b>IK tick + base-position read</b> — driven from {@link #extractRenderState} off the baked model.</li>
 * </ul>
 * <b>Unreproducible deltas (documented, not silently dropped):</b>
 * <ul>
 *   <li>The {@code "head"} distorted-entity render type ({@code RenderLayers.getDistortedEntity}) was a
 *       Veil/GLSL effect with no Mac-safe replacement (MAC_SAFE_RENDERING.md B7). The head now renders with
 *       the normal entity render type.</li>
 *   <li>The true-form body world-space-matrix offset and the spider-leg force-matrix IK write-back relied on
 *       per-bone world matrices fed through the old custom recursion. GeckoLib 5's snapshot API only exposes
 *       local {@code BoneSnapshot} rot/pos/scale, not arbitrary per-bone world matrices, so those legs/body
 *       fall back to standard animation posing. See {@code MowzieGeoBone} for the bone-API delta.</li>
 *   <li>{@code createVerticesOfQuad}'s UV remap for texture-override bones is now handled internally by
 *       {@link CustomBoneTextureGeoLayer}.</li>
 * </ul>
 */
public class SkinWalkerRenderer extends GeoEntityRenderer<SkinWalkerEntity, SkinWalkerRenderState> {
    private static final Identifier SLIM_MODEL = Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "geo/entity/skin_walker_slim.geo.json");
    private static final Identifier DEFAULT_MODEL = Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "geo/entity/skin_walker_default.geo.json");
    private static final Identifier FINAL_MODEL = Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "geo/entity/skin_walker_final_default.geo.json");
    private static final Identifier PLACEHOLDER_TEXTURE = Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "textures/entity/skinwalker/placeholder.png");

    private static final Identifier SPIDER_LEGS_TEXTURE = Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "textures/entity/skinwalker/skinwalker_legs_texture.png");
    private static final Identifier HEAD_TEXTURE = Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "textures/entity/skinwalker/final_form_head_texture.png");
    private static final Identifier EYES_TEXTURE = Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "textures/entity/skinwalker/skinwalker_eyes.png");

    private static final List<String> SPIDER_LEG_BONES = List.of(
            "seg1_leg1", "seg2_leg1", "seg3_leg1", "seg4_leg1",
            "seg1_leg2", "seg2_leg2", "seg3_leg2", "seg4_leg2",
            "seg1_leg3", "seg2_leg3", "seg3_leg3", "seg4_leg3",
            "seg1_leg4", "seg2_leg4", "seg3_leg4", "seg4_leg4");

    public SkinWalkerRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new SkinWalkerModel());

        // PORT: delta - per-bone texture overrides, formerly getTextureOverrideForBone/boneRenderOverride.
        this.withRenderLayer(new EyesGlowLayer(this, "headwear", EYES_TEXTURE));
        this.withRenderLayer(new TrueFormTextureLayer(this, "head", HEAD_TEXTURE));
        this.withRenderLayer(new TrueFormTextureLayer(this, "headwear", HEAD_TEXTURE));
        for (String leg : SPIDER_LEG_BONES) {
            this.withRenderLayer(new TrueFormTextureLayer(this, leg, SPIDER_LEGS_TEXTURE));
        }
    }

    @Override
    public SkinWalkerRenderState createRenderState() {
        return new SkinWalkerRenderState();
    }

    @Override
    public void extractRenderState(SkinWalkerEntity entity, SkinWalkerRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);

        SkinWalkerComponent component = InitializeComponents.SKIN_WALKER.get(entity);
        boolean trueForm = component.isInTrueForm();

        state.inTrueForm = trueForm;
        state.shouldBeginReveal = component.shouldBeginReveal();
        state.isSneaking = component.isSneaking();

        // ---- model + texture selection (was getModelResource/getTextureResource on the entity) ----
        Identifier modelId;
        Identifier textureId = PLACEHOLDER_TEXTURE;
        AbstractClientPlayer player = resolveTargetPlayer(component);

        if (trueForm) {
            modelId = FINAL_MODEL;
        } else if (player != null && player.getSkin().model() == PlayerModelType.SLIM) {
            modelId = SLIM_MODEL;
        } else {
            modelId = DEFAULT_MODEL;
        }
        if (player != null) {
            textureId = player.getSkin().body().texturePath();
        }

        state.addGeckolibData(SkinWalkerRenderData.MODEL, modelId);
        state.addGeckolibData(SkinWalkerRenderData.TEXTURE, textureId);
        state.addGeckolibData(SkinWalkerRenderData.TRUE_FORM, trueForm);

        // ---- procedural-pose scalars (was animateModel reading live entity state in preRender) ----
        float speed = Math.min(1.0F, entity.walkAnimation.speed(partialTick));
        float pos = entity.walkAnimation.position(partialTick);
        float bodyYaw = Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
        float headYaw = Mth.rotLerp(partialTick, entity.yHeadRotO, entity.yHeadRot);

        state.limbPos = pos;
        state.limbSpeed = speed;
        state.animationProgress = entity.tickCount + partialTick;
        state.headYawDeg = -(headYaw - bodyYaw);
        state.headPitchDeg = -Mth.lerp(partialTick, entity.xRotO, entity.getXRot());
        state.handSwingProgress = entity.getAttackAnim(partialTick);

        // ---- IK tick + base-position read (was setCustomAnimations + render) ----
        BakedGeoModel baked = this.model.getBakedModel(this.model.getModelResource((GeoRenderState) state));
        GeoModelAccessor accessor = new GeoModelAccessor(baked);
        if (trueForm) {
            entity.tickComponentsClient(entity, accessor);
            entity.getModelPositions(entity, accessor);
        }
    }

    private static AbstractClientPlayer resolveTargetPlayer(SkinWalkerComponent component) {
        Minecraft client = Minecraft.getInstance();
        if (client.level == null || component.getTargetPlayerUUID() == null) {
            return null;
        }
        Player player = client.level.getPlayerInAnyDimension(component.getTargetPlayerUUID());
        return player instanceof AbstractClientPlayer acp ? acp : null;
    }

    @Override
    public void adjustModelBonesForRender(RenderPassInfo<SkinWalkerRenderState> renderPass, BoneSnapshots snapshots) {
        super.adjustModelBonesForRender(renderPass, snapshots);

        SkinWalkerRenderState state = renderPass.renderState();
        if (state.shouldBeginReveal) {
            return;
        }

        // Head look (true-form gets the +35 pitch offset the original applied).
        float pitchOffset = state.inTrueForm ? 35.0F : 0.0F;
        snapshots.ifPresent("head", head -> {
            head.setRotZ(0.0F);
            head.setRotY(state.headYawDeg * (float) (Math.PI / 180.0));
            head.setRotX((state.headPitchDeg + pitchOffset) * (float) (Math.PI / 180.0));
        });

        if (state.inTrueForm) {
            return;
        }

        // Walking arms/legs.
        float limbPos = state.limbPos;
        float limbSpeed = state.limbSpeed;
        snapshots.ifPresent("right_arm", b -> { b.setRotX(Mth.cos(limbPos * 0.6662F + (float) Math.PI) * 2.0F * limbSpeed * 0.5F); b.setRotZ(0.0F); b.setRotY(0.0F); });
        snapshots.ifPresent("left_arm", b -> { b.setRotX(Mth.cos(limbPos * 0.6662F) * 2.0F * limbSpeed * 0.5F); b.setRotZ(0.0F); b.setRotY(0.0F); });
        snapshots.ifPresent("right_leg", b -> { b.setRotX(Mth.cos(limbPos * 0.6662F) * 1.4F * limbSpeed); b.setRotY(0.005F); b.setRotZ(0.005F); });
        snapshots.ifPresent("left_leg", b -> { b.setRotX(Mth.cos(limbPos * 0.6662F + (float) Math.PI) * 1.4F * limbSpeed); b.setRotY(-0.005F); b.setRotZ(-0.005F); });

        // Punch (overlays onto the walk pose, mirroring the original ordering).
        punch(state, snapshots);

        // Sneak pose.
        if (state.isSneaking) {
            snapshots.ifPresent("body", b -> b.setRotX(-0.5F));
            snapshots.ifPresent("right_arm", b -> { b.setRotX(b.getRotX() - 0.4F); b.setTranslateY(-3.2f); });
            snapshots.ifPresent("left_arm", b -> { b.setRotX(b.getRotX() - 0.4F); b.setTranslateY(-3.2f); });
            snapshots.ifPresent("head", b -> b.setTranslateY(-3.2f));
            snapshots.ifPresent("body", b -> b.setTranslateY(-3.2f));
            snapshots.ifPresent("right_leg", b -> b.setTranslateZ(4f));
            snapshots.ifPresent("left_leg", b -> b.setTranslateZ(4f));
        } else {
            snapshots.ifPresent("right_leg", b -> b.setTranslateZ(0));
            snapshots.ifPresent("left_leg", b -> b.setTranslateZ(0));
        }

        // Idle arm sway.
        float animationProgress = state.animationProgress;
        swingArm(snapshots, "right_arm", animationProgress, 1.0f);
        swingArm(snapshots, "left_arm", animationProgress, -1.0f);
    }

    private void punch(SkinWalkerRenderState state, BoneSnapshots snapshots) {
        float swing = state.handSwingProgress;
        if (swing <= 0.0F) {
            return;
        }
        var bodyOpt = snapshots.get("body");
        var headOpt = snapshots.get("head");
        var rightOpt = snapshots.get("right_arm");
        var leftOpt = snapshots.get("left_arm");
        if (bodyOpt.isEmpty() || headOpt.isEmpty() || rightOpt.isEmpty() || leftOpt.isEmpty()) {
            return;
        }
        var body = bodyOpt.get();
        var head = headOpt.get();
        var rightArm = rightOpt.get();
        var leftArm = leftOpt.get();

        body.setRotY(Mth.sin(Mth.sqrt(swing) * (float) (Math.PI * 2)) * 0.2F);

        rightArm.setTranslateZ(-Mth.sin(body.getRotY()) * 5.0F);
        rightArm.setTranslateX(Mth.cos(body.getRotY()) * 5.0F);
        leftArm.setTranslateZ(Mth.sin(body.getRotY()) * 5.0F);
        leftArm.setTranslateX(-Mth.cos(body.getRotY()) * 5.0F);
        rightArm.setRotY(rightArm.getRotY() + body.getRotY());
        leftArm.setRotY(-(leftArm.getRotY() + body.getRotY()));
        leftArm.setRotX(-(leftArm.getRotX() + body.getRotY()));

        float f = 1.0F - swing;
        f *= f;
        f *= f;
        f = 1.0F - f;
        float g = Mth.sin(f * (float) Math.PI);
        float h = Mth.sin(swing * (float) Math.PI) * -(-head.getRotX() - 0.7F) * 0.75F;
        rightArm.setRotX(-(rightArm.getRotX() - g * 1.2F - h));
        rightArm.setRotY(-(rightArm.getRotY() + body.getRotY() * 2.0F));
        rightArm.setRotZ(rightArm.getRotZ() + Mth.sin(swing * (float) Math.PI) * -0.4F);
    }

    private void swingArm(BoneSnapshots snapshots, String boneName, float animationProgress, float sigma) {
        snapshots.ifPresent(boneName, arm -> {
            arm.setRotZ(arm.getRotZ() + sigma * (Mth.cos(animationProgress * 0.09F) * 0.05F + 0.05F));
            arm.setRotX(arm.getRotX() + sigma * Mth.sin(animationProgress * 0.067F) * 0.05F);
        });
    }

    /** Re-renders a bone with the emissive eyes render type (was boneRenderOverride for "headwear"). */
    private static class EyesGlowLayer extends TrueFormTextureLayer {
        EyesGlowLayer(GeoRenderer<SkinWalkerEntity, Void, SkinWalkerRenderState> renderer, String boneName, Identifier texture) {
            super(renderer, boneName, texture);
        }

        @Override
        protected RenderType getRenderType(SkinWalkerRenderState state, Identifier texture) {
            return RenderTypes.eyes(texture);
        }
    }

    /** Per-bone texture override that only applies while the entity is in its true (revealed) form. */
    private static class TrueFormTextureLayer extends CustomBoneTextureGeoLayer<SkinWalkerEntity, Void, SkinWalkerRenderState> {
        TrueFormTextureLayer(GeoRenderer<SkinWalkerEntity, Void, SkinWalkerRenderState> renderer, String boneName, Identifier texture) {
            super(renderer, boneName, texture);
        }

        @Override
        public boolean shouldRenderBone(SkinWalkerRenderState state) {
            return state.getOrDefaultGeckolibData(SkinWalkerRenderData.TRUE_FORM, Boolean.FALSE) && super.shouldRenderBone(state);
        }
    }
}
