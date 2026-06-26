package com.sp.entity.ai.goals;

import com.sp.cca_stuff.InitializeComponents;
import com.sp.cca_stuff.SkinWalkerComponent;
import com.sp.entity.custom.SkinWalkerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;

public class ActNaturalGoal extends Goal {
    private final RandomSource random = RandomSource.create(7585889L);
    private final java.util.Random rand = new java.util.Random();
    private final SkinWalkerEntity entity;
    private final SkinWalkerComponent component;
    private Integer randomAction;
    private int actCooldown;

    private int currentActionCount = 0;
    private int currentActionCooldown = 0;
    private boolean currentActionSwitch = false;

    private Vec3 randLookDir;

    public ActNaturalGoal(SkinWalkerEntity entity) {
        this.entity = entity;
        this.component = InitializeComponents.SKIN_WALKER.get(entity);
    }

    @Override
    public boolean canUse() {
        return this.component.shouldActNatural() && !this.component.isInTrueForm() && !this.component.shouldBeginReveal();
    }

    @Override
    public void start() {
        this.actCooldown = adjustedTickDelay(40);
    }

    @Override
    public void stop() {
        this.randomAction = null;
        this.component.setSneaking(false);
        this.currentActionCooldown = 0;
        this.currentActionCount = 0;
        this.actCooldown = 0;
    }

    @Override
    public void tick() {
        if(!this.entity.level().isClientSide()) {
            ////Cooldown Checks////
            if (actCooldown > 0) {
                actCooldown--;
                return;
            }
            ///////////////////////
            if (this.randomAction == null) {
                this.randomAction = random.nextIntBetweenInclusive(1, 4);
            }
            this.component.setCurrentlyActingNatural(true);
            switch (this.randomAction) {
                case 1: this.sneakTick(); break;
                case 2: this.strafeTick();break;
                case 3: this.lookAndPunchTick(); break;
                case 4: this.lookMultTick(); break;
            }
        }

    }


    private void sneakTick() {
        if (this.currentActionCooldown <= 0) {
            if (this.currentActionCount < 2 && !this.component.isSneaking()) {
                this.component.setSneaking(true);
                this.currentActionCount++;
                this.currentActionCooldown = 0;

            } else if (this.component.isSneaking()) {
                this.component.setSneaking(false);
                this.currentActionCooldown = 0;

            } else {
                this.component.setCurrentlyActingNatural(false);
                this.randomAction = null;
                this.currentActionCount = 0;
                this.setRandomActCoolDown();

            }
        } else {
            this.currentActionCooldown--;
        }
    }

    private void strafeTick() {
        if (this.currentActionCooldown <= 0) {
            if (this.currentActionCount < 8 && !this.currentActionSwitch) {
                this.entity.xxa = 0.2f;
                this.currentActionSwitch = true;
                this.currentActionCount++;
                this.currentActionCooldown = 2;

            } else if (this.currentActionSwitch) {
                this.entity.xxa = -0.2f;
                this.currentActionSwitch = false;
                this.currentActionCooldown = 2;
                this.currentActionCount++;

            } else {
                this.component.setCurrentlyActingNatural(false);
                this.entity.xxa = 0;
                this.currentActionSwitch = false;
                this.randomAction = null;
                this.currentActionCount = 0;
                this.setRandomActCoolDown();

            }
        } else {
            this.currentActionCooldown--;
        }
    }

    private void punchPlayer() {
        this.entity.swing(InteractionHand.MAIN_HAND);
        this.entity.doHurtTarget((ServerLevel) this.entity.level(), this.component.getFollowTarget());
        this.component.setCurrentlyActingNatural(false);
        this.randomAction = null;
        this.setRandomActCoolDown();
    }

    private void lookAndPunchTick() {
        this.component.setShouldLookAtTarget(false);
        if(this.randLookDir == null){
            float randX = rand.nextFloat(-45, 45);
            float randY = rand.nextFloat(-180, 180);
            this.randLookDir = eulerToVector(randX, randY);
            ((SkinWalkerEntity.SkinWalkerLookControl)this.entity.getLookControl()).lookAt(this.randLookDir, 5);
        }


        if(this.currentActionCooldown <= 0){
            if (this.currentActionCount < 4) {
                this.entity.swing(InteractionHand.MAIN_HAND);
                this.currentActionCount++;
                this.currentActionCooldown = 2;

            } else {
                this.component.setCurrentlyActingNatural(false);
                this.component.setShouldLookAtTarget(true);
                this.randomAction = null;
                this.randLookDir = null;
                this.currentActionCount = 0;
                this.setRandomActCoolDown();

            }
        } else {
            this.currentActionCooldown--;
        }
    }

    private void lookMultTick() {
        this.component.setShouldLookAtTarget(false);


        if(this.currentActionCooldown <= 0){
            if (this.currentActionCount < 5) {
                float randX = rand.nextFloat(-45, 45);
                float randY = rand.nextFloat(-180, 180);
                ((SkinWalkerEntity.SkinWalkerLookControl)this.entity.getLookControl()).lookAt(eulerToVector(randX, randY), random.nextIntBetweenInclusive(4, 9));

                int shouldPunch = random.nextIntBetweenInclusive(1,3);
                if(shouldPunch == 1){
                    this.entity.swing(InteractionHand.MAIN_HAND);
                }

                this.currentActionCount++;
                this.currentActionCooldown = random.nextIntBetweenInclusive(6, 12);

            } else {
                this.component.setCurrentlyActingNatural(false);
                this.component.setShouldLookAtTarget(true);
                this.randomAction = null;
                this.currentActionCount = 0;
                this.setRandomActCoolDown();

            }
        } else {
            this.currentActionCooldown--;
        }
    }

    private Vec3 eulerToVector(float pitch, float yaw){

        float x = Mth.cos(yaw)*Mth.cos(pitch);
        float y = Mth.sin(yaw)*Mth.cos(pitch);
        float z = Mth.sin(pitch);

        return new Vec3(x, y, z).scale(180/Math.PI);
    }


    private void setRandomActCoolDown(){
        this.actCooldown = adjustedTickDelay(random.nextIntBetweenInclusive(60, 150));
    }
}
