package com.sp.entity.ai.goals;

import com.sp.cca_stuff.InitializeComponents;
import com.sp.cca_stuff.SkinWalkerComponent;
import com.sp.entity.custom.SkinWalkerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;

import java.util.Comparator;
import java.util.List;

public class FinalFormActiveTargetGoal extends TargetGoal {
    private final SkinWalkerComponent component;

    public FinalFormActiveTargetGoal(SkinWalkerEntity entity){
        super(entity, true, false);
        this.component = InitializeComponents.SKIN_WALKER.get(entity);
        this.unseenMemoryTicks = 100;
    }

    @Override
    public boolean canUse() {
        if(this.component.isInTrueForm() && !this.component.shouldBeginReveal()){
            if(this.mob.getTarget() != null){
                this.targetMob = this.mob.getTarget();
                return true;
            }

            TargetingConditions conditions = TargetingConditions.forCombat()
                    .range(100)
                    .ignoreLineOfSight()
                    .selector((target, level) -> EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(target));

            ServerLevel serverLevel = (ServerLevel) this.mob.level();
            List<Player> playerEntityList = serverLevel.getEntitiesOfClass(
                    Player.class,
                    this.mob.getBoundingBox().inflate(100),
                    player -> conditions.test(serverLevel, this.mob, player));
            playerEntityList.sort(Comparator.comparingDouble(player -> -this.mob.position().distanceToSqr(player.position().x, player.position().y, player.position().z)));

            for (Player player : playerEntityList){
                if(this.mob.hasLineOfSight(player)){
                    this.targetMob = player;
                    ((SkinWalkerEntity)this.mob).beginTargeting(player);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return super.canContinueToUse() && this.canUse();
    }

    @Override
    public void tick() {
        if(this.targetMob != null) {
            this.component.setLastKnownTargetLocation(this.targetMob.blockPosition());
        }
    }


}
