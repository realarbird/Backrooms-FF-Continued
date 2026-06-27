package com.sp.entity.ik.components;


import com.sp.entity.ik.model.ModelAccessor;

public interface IKModelComponent<E extends IKAnimatable<E>> {
    void tickServer(E animatable);

    void tickClient(E animatable, ModelAccessor model);

    void getModelPositions(E animatable, ModelAccessor model);

    // PORT: delta - the renderDebug hook was DROPPED in Phase 6b along with the IK debug renderers
    // (Yarn render pipeline not ported). Declaration + Yarn render imports removed; solver logic intact.
}
