package com.sp.entity.client.renderer;

import com.sp.SPBRevamped;
import com.sp.cca_stuff.InitializeComponents;
import com.sp.cca_stuff.SmilerComponent;
import com.sp.entity.client.model.SmilerModel;
import com.sp.entity.custom.SmilerEntity;
import com.sp.init.ModModelLayers;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

/**
 * PORT: delta (1.20.1 -> 26.2). Migrated from the immediate-mode {@code render(...VertexConsumerProvider...)}
 * model to the deferred render-state pipeline:
 * <ul>
 *   <li>Per-entity texture choice moved from {@code getTexture(entity)} into
 *       {@link #extractRenderState} -> {@link SmilerRenderState#texture}; {@link #getTextureLocation}
 *       reads it back.</li>
 *   <li>The billboard "always face the camera" yaw is applied in {@link #submit} (replacing the old
 *       {@code render} override). The camera position now comes from {@link CameraRenderState#pos}
 *       instead of {@code MinecraftClient.gameRenderer.getCamera()}.</li>
 *   <li>The eyes render layer ({@code RenderLayer::getEyes}) is carried on the model (see
 *       {@code SmilerModel}); the old {@code getRenderLayer} override was a pass-through and is dropped.</li>
 * </ul>
 */
public class SmilerRenderer extends MobRenderer<SmilerEntity, SmilerRenderState, SmilerModel> {
    private static final Identifier defaultTexture = Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "textures/entity/smiler/smiler.png");
    private static final Identifier texture1 = Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "textures/entity/smiler/smiler1.png");
    private static final Identifier texture2 = Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "textures/entity/smiler/smiler2.png");

    public SmilerRenderer(EntityRendererProvider.Context context) {
        super(context, new SmilerModel(context.bakeLayer(ModModelLayers.SMILER)), 0);
    }

    @Override
    public SmilerRenderState createRenderState() {
        return new SmilerRenderState();
    }

    @Override
    public void extractRenderState(SmilerEntity entity, SmilerRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);
        SmilerComponent component = InitializeComponents.SMILER.get(entity);
        state.texture = switch (component.getRandomTexture()) {
            case 1 -> texture1;
            case 2 -> texture2;
            default -> defaultTexture;
        };
    }

    @Override
    public Identifier getTextureLocation(SmilerRenderState state) {
        return state.texture;
    }

    @Override
    public void submit(SmilerRenderState state, PoseStack matrixStack, SubmitNodeCollector collector, CameraRenderState cameraState) {
        // Billboard the flat smiler face toward the camera around the Y axis.
        Vec3 entityPos = new Vec3(state.x, state.y + state.eyeHeight, state.z);
        float angle = lookAtEntityAroundYAxis(entityPos, cameraState.pos);

        matrixStack.translate(0, 1, 0);
        matrixStack.mulPose(new Quaternionf().rotateXYZ(0, (float) Math.toRadians(angle), 0));
        matrixStack.translate(0, -1, 0);

        super.submit(state, matrixStack, collector, cameraState);
    }

    public static float lookAtEntityAroundYAxis(Vec3 position1, Vec3 position2) {
        double d = position2.x() - position1.x();
        double e = position2.z() - position1.z();
        float h = -(float) (Mth.atan2(e, d) * 180.0F / (float) Math.PI) - 90.0F;

        return h - 180.0F;
    }
}
