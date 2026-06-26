package com.sp.entity.ai.goals;

import com.sp.cca_stuff.InitializeComponents;
import com.sp.cca_stuff.SkinWalkerComponent;
import com.sp.entity.custom.SkinWalkerEntity;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;

public class FinalFormWanderGoal extends WaterAvoidingRandomStrollGoal {
    private final SkinWalkerComponent component;

    public FinalFormWanderGoal(SkinWalkerEntity entity, double d) {
        super(entity, d);
        this.component = InitializeComponents.SKIN_WALKER.get(entity);
    }

    @Override
    public boolean canUse() {
        return super.canUse() && component.isInTrueForm() && component.isIdle();
    }
}
