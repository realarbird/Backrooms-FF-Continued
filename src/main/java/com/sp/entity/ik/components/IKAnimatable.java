package com.sp.entity.ik.components;

import com.sp.entity.ik.model.ModelAccessor;

import java.util.List;

public interface IKAnimatable<E extends IKAnimatable<E>> {
    List<IKModelComponent<E>> getComponents();

    double getSize();

    default boolean containsComponent(Class type) {
        return !this.getComponents().stream().filter(eikModelComponent -> eikModelComponent.getClass() == type).toList().isEmpty();
    }

    default List<? extends IKModelComponent<E>> getComponentOfType(Class type) {
        return this.getComponents().stream().filter(eikModelComponent -> eikModelComponent.getClass() == type).toList();
    }

    default void addComponent(IKModelComponent<E> component) {
        this.getComponents().add(component);
    }

    default void tickComponentsClient(E animatable, ModelAccessor model) {
        this.getComponents().forEach(ikModelComponent -> ikModelComponent.tickClient(animatable, model));
    }

    default void tickComponentsServer(E animatable) {
        this.getComponents().forEach(ikModelComponent -> ikModelComponent.tickServer(animatable));
    }

    default void getModelPositions(E animatable, ModelAccessor model) {
        this.getComponents().forEach(ikModelComponent -> ikModelComponent.getModelPositions(animatable, model));
    }

    // PORT: delta - the IK debug renderers (renderDebug hooks) were DROPPED in Phase 6b (the debug
    // render pipeline relied on Yarn RenderLayer/VertexConsumerProvider/MatrixStack and was not ported).
    // The dead declarations + their Yarn render imports are removed; the IK solver logic is untouched.
}
