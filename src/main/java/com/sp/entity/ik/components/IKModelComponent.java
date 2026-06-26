package com.sp.entity.ik.components;


import com.sp.entity.ik.model.ModelAccessor;
// PORT: phase6 - render-pipeline imports (RenderType/MultiBufferSource/PoseStack) deferred
// import net.minecraft.client.render.RenderLayer;
// import net.minecraft.client.render.VertexConsumer;
// import net.minecraft.client.render.VertexConsumerProvider;
// import net.minecraft.client.util.math.MatrixStack;

public interface IKModelComponent<E extends IKAnimatable<E>> {
    void tickServer(E animatable);

    void tickClient(E animatable, ModelAccessor model);

    void getModelPositions(E animatable, ModelAccessor model);

    // PORT: phase6 - debug rendering uses the reworked deferred render pipeline (RenderType/MultiBufferSource)
    // void renderDebug(MatrixStack poseStack, E animatable, RenderLayer renderType, VertexConsumerProvider bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay);
}
