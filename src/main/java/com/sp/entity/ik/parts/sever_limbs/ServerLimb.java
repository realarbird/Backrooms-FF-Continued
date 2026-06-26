package com.sp.entity.ik.parts.sever_limbs;


import com.sp.entity.ik.components.IKAnimatable;
import com.sp.entity.ik.components.IKLegComponent;
import com.sp.entity.ik.parts.ik_chains.IKChain;
import com.sp.entity.ik.util.ArrayUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class ServerLimb {
    public Vec3 target = Vec3.ZERO;
    public Vec3 oldTarget = Vec3.ZERO;
    public Vec3 pos = Vec3.ZERO;
    public Vec3 baseOffset;
    public boolean hasToBeSet = true;
    public TimedDistanceFunction currentDistanceFunction = null;
    public final RandomSource random = RandomSource.create();
    public boolean playedStepSound;
    public StepCallback stepCallback;

    public ServerLimb(Vec3 baseOffset, StepCallback stepCallback) {
        this.baseOffset = baseOffset;
        this.stepCallback = stepCallback;
    }

    public ServerLimb(double x, double y, double z, StepCallback stepCallback) {
        this.baseOffset = new Vec3(x, y, z);
        this.stepCallback = stepCallback;
    }

    public void set(Vec3 newPos) {
        this.pos = newPos;
        this.oldTarget = newPos;
        this.setTarget(newPos);
        this.hasToBeSet = false;
    }

    public void setTarget(Vec3 target) {
        this.target = target;
    }

    public <C extends IKChain, E extends IKAnimatable<E>>void tick(IKLegComponent<C, E> legComponent, int i) {
        if (!(this.pos.distanceTo(this.target) < (5 * legComponent.scale * legComponent.getLimbs().get(0).getMaxLength()))) {
            this.pos = this.target;
            this.oldTarget = this.target;
        }

        // No need to compute
        if (this.pos == this.target && this.oldTarget == this.target) {
            return;
        }

        if (this.currentDistanceFunction == null) {
            this.currentDistanceFunction = new TimedDistanceFunction(legComponent.getSettings().get(i).steppingParabolaStrength() , this.target.y - this.oldTarget.y);
        }

        if (!adjacentEndPointGrounded(legComponent.getEndPoints(), i)) {
            return;
        }

        Vec3 flatTarget = new Vec3(this.target.x, 0, this.target.z);
        Vec3 flatOldTarget = new Vec3(this.oldTarget.x, 0, this.oldTarget.z);

        Vec3 targetDirection = flatTarget.subtract(flatOldTarget);

        this.pos = this.oldTarget.add(targetDirection.scale(this.currentDistanceFunction.time)).add(new Vec3(0, this.currentDistanceFunction.getHeight(), 0));

        this.currentDistanceFunction.time += 0.3;

        //this.currentDistanceFunction.time = Math.min(this.currentDistanceFunction.time + (0.2 * legComponent.getSettings().movementSpeed()), 1);

        if (this.pos.distanceTo(this.target) < 0.01 || this.currentDistanceFunction.time >= 1) {
            this.pos = this.target;
            this.oldTarget = this.target;
            this.currentDistanceFunction = null;
            if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
                if (!this.playedStepSound && this.stepCallback != null) {
                    stepCallback.onStep(this, legComponent, i, legComponent.getSettings().get(i).movementSpeed());
                    this.playedStepSound = true;
                }
            }
        } else {
            this.playedStepSound = false;
        }
    }

    public interface StepCallback {
        void onStep(ServerLimb limb, IKLegComponent<?, ?> legComponent, int i, double movementSpeed);
    }

    private boolean adjacentEndPointGrounded(List<ServerLimb> limbs, int index) {
        boolean areAllGrounded = true;

        for (int legIndex : adjacent(index)) {
            ServerLimb leg = ArrayUtil.getOrNull(limbs, legIndex);
            if (leg == null) continue;

            if (leg.isGrounded()) continue;

            areAllGrounded = false;
            break;
        }

        return areAllGrounded;
    }


    public Vec3 getPos() {
        return this.pos;
    }

    public void setPos(Vec3 pos) {
        this.pos = pos;
    }

    public boolean isGrounded() {
        return this.pos == this.oldTarget;
    }

    public static class TimedDistanceFunction extends DistanceFunction {
        public double time = 0;

        public TimedDistanceFunction(double a, double yOffset) {
            super(a, yOffset);
        }

        /**
         * @return the y value of the function for the currently stored time.
         */
        public double getHeight() {
            return this.getHeight(time);
        }
    }

    static class DistanceFunction {
        double a;
        double b;

        public DistanceFunction(double a, double yOffset) {
            this.a = a;
            b = yOffset+a;
        }

        /**
         * @param time the already passed time (x) 0 - 1
         * @return the y value of the function
         */
        double getHeight(double time) {
            return -a*(time*time)+b*time;
        }
    }

    /**
     * THIS CODE FOLLOWING IS NOT MINE!!!! <p>
     * It was politely stolen from Cymaera, with their consent, on <a href="https://github.com/TheCymaera/minecraft-spider/blob/main/src/main/java/com/heledron/spideranimation/spider/LegLookUp.kt">GitHub</a> and then translated!
     **/
    public static List<List<Integer>> diagonalPairs(List<Integer> legs) {
        List<List<Integer>> result = new ArrayList<>();
        for (int leg : legs) {
            List<Integer> diagonal = new ArrayList<>(diagonal(leg));
            diagonal.add(leg);
            result.add(diagonal);
        }
        return result;
    }

    public static boolean isLeftLeg(int leg) {
        return leg % 2 == 0;
    }

    public static boolean isRightLeg(int leg) {
        return !isLeftLeg(leg);
    }

    public static int getPairIndex(int leg) {
        return leg / 2;
    }

    public static boolean isDiagonal1(int leg) {
        return getPairIndex(leg) % 2 == 0 ? isLeftLeg(leg) : isRightLeg(leg);
    }

    public static boolean isDiagonal2(int leg) {
        return !isDiagonal1(leg);
    }

    public static int diagonalFront(int leg) {
        return isLeftLeg(leg) ? leg - 1 : leg - 3;
    }

    public static int diagonalBack(int leg) {
        return isLeftLeg(leg) ? leg + 3 : leg + 1;
    }

    public static int front(int leg) {
        return leg - 2;
    }

    public static int back(int leg) {
        return leg + 2;
    }

    public static int horizontal(int leg) {
        return isLeftLeg(leg) ? leg + 1 : leg - 1;
    }

    public static List<Integer> diagonal(int leg) {
        return List.of(diagonalFront(leg), diagonalBack(leg));
    }

    public static List<Integer> adjacent(int leg) {
        return List.of(front(leg), back(leg), horizontal(leg));
    }
}
