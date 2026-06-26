package com.sp.entity.ai.goals;

import com.sp.cca_stuff.InitializeComponents;
import com.sp.cca_stuff.PlayerComponent;
import com.sp.cca_stuff.SkinWalkerComponent;
import com.sp.entity.custom.SkinWalkerEntity;
import com.sp.init.ModSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class FinalFormIdleGoal extends Goal {
    private final SkinWalkerEntity entity;
    private final SkinWalkerComponent component;
    private final int chance;
    private int idleActionCoolDown;
    private final int maxIdleActionCoolDown;

    public FinalFormIdleGoal(SkinWalkerEntity entity, int chance, int cooldown){
        this.entity = entity;
        this.component = InitializeComponents.SKIN_WALKER.get(entity);
        this.chance = chance;
        this.maxIdleActionCoolDown = reducedTickDelay(cooldown);
    }


    @Override
    public boolean canUse() {
        if(this.component.isInTrueForm() && !this.component.isNoticing()){
            if(this.entity.getTarget() == null){
                return true;
            }
        }

        return false;
    }

    @Override
    public void start() {
        this.component.setIdle(true);
    }

    @Override
    public void stop() {
        this.component.setIdle(false);
    }

    @Override
    public void tick() {
        if(!this.entity.level().isClientSide()) {
            TargetingConditions conditions = TargetingConditions.forCombat()
                    .range(100)
                    .ignoreLineOfSight()
                    .selector((target, level) -> EntitySelector.NO_CREATIVE_OR_SPECTATOR.test(target));

            ServerLevel serverLevel = (ServerLevel) this.entity.level();
            List<Player> playerEntityList = serverLevel.getEntitiesOfClass(
                    Player.class,
                    this.entity.getBoundingBox().inflate(100),
                    player -> conditions.test(serverLevel, this.entity, player));

            for (Player player : playerEntityList) {
                PlayerComponent playerComponent = InitializeComponents.PLAYER.get(player);
                if (playerComponent.isVisibleToEntity()) {
                    this.entity.noticePlayer(player);
                    return;
                }
            }
        }

        if(this.idleActionCoolDown > 0){
            this.idleActionCoolDown--;

        } else if(this.entity.getRandom().nextInt(reducedTickDelay(this.chance)) == 0){
            if(this.entity.getRandom().nextBoolean()){
                this.entity.playSound(ModSounds.SKINWALKER_AMBIENCE, 10.0f, 1.0f);
            } else {
                this.entity.playSound(ModSounds.SKINWALKER_SNIFF, 10.0f, 1.0f);
            }

            this.idleActionCoolDown = this.maxIdleActionCoolDown;
        }

    }

}
