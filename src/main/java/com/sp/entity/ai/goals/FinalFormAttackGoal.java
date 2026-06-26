package com.sp.entity.ai.goals;

import com.sp.cca_stuff.InitializeComponents;
import com.sp.cca_stuff.SkinWalkerComponent;
import com.sp.entity.custom.SkinWalkerEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.level.pathfinder.Path;

public class FinalFormAttackGoal extends MeleeAttackGoal {
    private final SkinWalkerComponent component;
    // MeleeAttackGoal's own path/lastCanUseCheck are private in 26.2; mirror the
    // vanilla canUse() bookkeeping here so the overridden logic stays faithful.
    private Path attackPath;
    private long lastCanUseCheck;

    public FinalFormAttackGoal(SkinWalkerEntity entity) {
        super(entity, 1.15, false);
        this.component = InitializeComponents.SKIN_WALKER.get(entity);
    }

    @Override
    public boolean canUse() {
        if(this.component.isInTrueForm()) {
            long l = this.mob.level().getGameTime();
            if (l - this.lastCanUseCheck < 20L) {
                return false;
            } else {
                this.lastCanUseCheck = l;
                LivingEntity livingEntity = this.mob.getTarget();
                if (livingEntity == null) {
                    this.moveToLastKnownLocation();
                    return false;
                } else if (!livingEntity.isAlive()) {
                    this.moveToLastKnownLocation();
                    return false;
                } else {
                    this.attackPath = this.mob.getNavigation().createPath(livingEntity, 0);
                    return this.attackPath != null
                            ? true
                            : this.getSquaredMaxAttackDistance(livingEntity) >= this.mob.distanceToSqr(livingEntity.getX(), livingEntity.getY(), livingEntity.getZ());
                }
            }
        }

        return false;
    }

    // getSquaredMaxAttackDistance was removed in 26.2; reproduce vanilla's formula.
    private double getSquaredMaxAttackDistance(LivingEntity target) {
        return (double)(this.mob.getBbWidth() * 2.0F * this.mob.getBbWidth() * 2.0F + target.getBbWidth());
    }

    private void moveToLastKnownLocation() {
        BlockPos pos = this.component.getLastKnownTargetLocation();

        if(pos != null){
            this.mob.getNavigation().moveTo(pos.getX(), pos.getY(), pos.getZ(), 1.0);
        }
    }


}
