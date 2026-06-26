package com.sp.entity.ai.goals;

import com.sp.cca_stuff.InitializeComponents;
import com.sp.cca_stuff.SkinWalkerComponent;
import com.sp.entity.custom.SkinWalkerEntity;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;

public class SkinWalkerActiveTarget extends NearestAttackableTargetGoal<Player> {
    private final SkinWalkerComponent component;

    public SkinWalkerActiveTarget(SkinWalkerEntity entity) {
        super(entity, Player.class, false);
        this.component = InitializeComponents.SKIN_WALKER.get(entity);
    }

    @Override
    public boolean canUse() {
        if(!this.component.isInTrueForm() && !this.component.shouldBeginReveal()) {
            return super.canUse();
        }

        return false;
    }
}
