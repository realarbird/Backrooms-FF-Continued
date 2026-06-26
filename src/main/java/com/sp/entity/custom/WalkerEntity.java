package com.sp.entity.custom;

import com.sp.clientWrapper.ClientWrapper;
import com.sp.entity.ik.components.IKAnimatable;
import com.sp.entity.ik.components.IKLegComponent;
import com.sp.entity.ik.components.IKModelComponent;
import com.sp.entity.ik.components.IKWalkerComponent;
import com.sp.entity.ik.parts.Segment;
import com.sp.entity.ik.parts.ik_chains.BendReachingIKChain;
import com.sp.entity.ik.parts.sever_limbs.ServerLimb;
import com.sp.entity.ik.util.MathUtil;
import com.sp.init.ModEntities;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaterniond;
import org.joml.Quaternionf;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

public class WalkerEntity extends Entity implements GeoEntity, GeoAnimatable, IKAnimatable<WalkerEntity> {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    @Nullable
    private Entity target = null;

    private final Quaterniond rotation = new Quaterniond(0,0,0,1); // Default rotation facing down the Z-axis
    private final List<IKModelComponent<WalkerEntity>> components = new ArrayList<>();
    private static final double SPEED_MULTIPLIER = 0.01;
    private static final double SQUARED_MAX_VELOCITY = 16; // 4.0 * 4.0
    public static final double TILTING_TEST_RANGE = 5.0;
    private static final double COLLISION_TEST_RANGE = 3;
    public boolean isWalking = false;
    public Quaterniond upDirection = new Quaterniond(1, 0 ,0 ,1);
    private double roll = 0;

    public WalkerEntity(EntityType<WalkerEntity> entityType, Level world) {
        super(ModEntities.WALKER_ENTITY, world);
        this.addComponent(new IKWalkerComponent<>(
                new IKLegComponent.LegSetting.Builder()
                        .maxDistance(2)
                        .stepInFront(1)
                        .movementSpeed(0.2)
                        .maxStandingStillDistance(0.2)
                        .standStillCounter(20).build(),
                List.of(
                        new ServerLimb(2.984375, 0, 1.21875, (limb, legComponent, i, movementSpeed) -> ClientWrapper.walkerPlayStepSound(limb)),
                        new ServerLimb(-2.984375, 0, 1.21875, (limb, legComponent, i, movementSpeed) -> ClientWrapper.walkerPlayStepSound(limb)),
                        new ServerLimb(5.1875, 0, -1.25, (limb, legComponent, i, movementSpeed) -> ClientWrapper.walkerPlayStepSound(limb)),
                        new ServerLimb(-5.1875, 0, -1.25, (limb, legComponent, i, movementSpeed) -> ClientWrapper.walkerPlayStepSound(limb)),
                        new ServerLimb(3.875, 0, -3.875, (limb, legComponent, i, movementSpeed) -> ClientWrapper.walkerPlayStepSound(limb)),
                        new ServerLimb(-3.875, 0, -3.875, (limb, legComponent, i, movementSpeed) -> ClientWrapper.walkerPlayStepSound(limb))
                ),
                new BendReachingIKChain(this, new Segment.Builder().length(0.7).build(), new Segment.Builder().length(0.93).build(), new Segment.Builder().length(1.98).build(), new Segment.Builder().length(1.92).build(), new Segment.Builder().length(2).build()),
                new BendReachingIKChain(this, new Segment.Builder().length(0.7).build(), new Segment.Builder().length(0.93).build(), new Segment.Builder().length(1.98).build(), new Segment.Builder().length(1.92).build(), new Segment.Builder().length(2).build()),
                new BendReachingIKChain(this, new Segment.Builder().length(0.7).build(), new Segment.Builder().length(1.3).build(), new Segment.Builder().length(2.1).build(), new Segment.Builder().length(2.1).build(), new Segment.Builder().length(1.98).build(), new Segment.Builder().length(2.3).build()),
                new BendReachingIKChain(this, new Segment.Builder().length(0.7).build(), new Segment.Builder().length(1.3).build(), new Segment.Builder().length(2.1).build(), new Segment.Builder().length(2.1).build(), new Segment.Builder().length(1.98).build(), new Segment.Builder().length(2.3).build()),
                new BendReachingIKChain(this, new Segment.Builder().length(0.7).build(), new Segment.Builder().length(1.3).build(), new Segment.Builder().length(2.1).build(), new Segment.Builder().length(2.1).build(), new Segment.Builder().length(1.98).build(), new Segment.Builder().length(2.3).build()),
                new BendReachingIKChain(this, new Segment.Builder().length(0.7).build(), new Segment.Builder().length(1.3).build(), new Segment.Builder().length(2.1).build(), new Segment.Builder().length(2.1).build(), new Segment.Builder().length(1.98).build(), new Segment.Builder().length(2.3).build())
        ));
    }

    public double getRoll() {
        return roll;
    }

    public void setRoll(float roll) {
        this.roll = roll;
    }

    public Quaterniond getRotation() {
        return rotation;
    }

    public Quaternionf getRotationF() {
        return new Quaternionf(rotation.x, rotation.y, rotation.z, rotation.w);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {

    }

    @Override
    public boolean hurtServer(net.minecraft.server.level.ServerLevel world, net.minecraft.world.damagesource.DamageSource source, float amount) {
        return false;
    }

    public @Nullable Entity getTarget() {
        return target;
    }

    public void setTarget(@Nullable Entity target) {
        this.target = target;
    }

    @Override
    public void tick() {
        ((Entity) this).baseTick(); // Leave this as is. Trust me.
        this.tickComponentsServer(this);

        Player nearestPlayer = this.level().getNearestPlayer(this, 100);
        if (nearestPlayer != null && nearestPlayer.getMainHandItem().getItem() == Items.BONE) {
            this.setTarget(nearestPlayer);
        } else {
            this.setTarget(null);
        }

        this.isWalking = false;

        if (this.getTarget() != null) {
            this.isWalking = true;

            Vec3 direction = this.getFacingTarget();
            double horizontalDistance = Math.sqrt(direction.x * direction.x + direction.z * direction.z);

            double yaw = Math.atan2(direction.x, direction.z);
            double pitch = -Math.atan2(direction.y, horizontalDistance);
            //double roll = Math.atan2(direction.y, direction.x);

            boolean shouldBeInverted = this.getUpDirection().reverse().distanceToSqr(this.getRightDirection()) > this.getUpStationaryDirection().distanceToSqr(this.getRightDirection());
            double roll = (shouldBeInverted ? (-1) : (1)) * Math.toDegrees(Math.acos(this.getUpDirection().reverse().dot(this.getUpStationaryDirection().reverse())));

            Quaterniond newRotation = new Quaterniond()
                    .identity()
                    .rotateY(yaw)
                    .rotateX(pitch);

            rotation.nlerp(newRotation, 0.1);

            Vec3 newVelocity = this.getForwardVector().scale(this.getTarget().distanceTo(this) * SPEED_MULTIPLIER);

            if (Math.min(SQUARED_MAX_VELOCITY, newVelocity.lengthSqr()) != SQUARED_MAX_VELOCITY) {
                this.setDeltaMovement(newVelocity);
            }

            this.setPos(
                    this.position().add(this.getDeltaMovement()).x,
                    this.position().add(this.getDeltaMovement()).y,
                    this.position().add(this.getDeltaMovement()).z);

            this.setYRot((float) Mth.wrapDegrees(-(Math.toDegrees(yaw))));
            this.setXRot((float) Mth.wrapDegrees(Math.toDegrees(pitch) + 270));
            this.setRoll((float) Mth.wrapDegrees(roll));

            updateUpDirection();
        }
    }

    private Vec3 getFacingTarget() {
        Vec3 averageDirection = this.getTarget().position().subtract(this.position()).normalize();
        int directionsApplied = 1;

        List<Vec3> testPositions = new ArrayList<>();


        testPositions.add(new Vec3(1, 0, 0).normalize());
        testPositions.add(new Vec3(-1, 0, 0).normalize());

        testPositions.add(new Vec3(1, 1, 0).normalize());
        testPositions.add(new Vec3(-1, -1, 0).normalize());
        testPositions.add(new Vec3(1, -1, 0).normalize());
        testPositions.add(new Vec3(-1, 1, 0).normalize());

        testPositions.add(new Vec3(1, 1, 1).normalize());
        testPositions.add(new Vec3(-1, -1, -1).normalize());
        testPositions.add(new Vec3(1, 1, -1).normalize());
        testPositions.add(new Vec3(-1, -1, 1).normalize());

        testPositions.add(new Vec3(1, -1, -1).normalize());
        testPositions.add(new Vec3(-1, 1, 1).normalize());
        testPositions.add(new Vec3(1, -1, 1).normalize());
        testPositions.add(new Vec3(-1, 1, -1).normalize());

        testPositions.add(new Vec3(0, 1, 0).normalize());
        testPositions.add(new Vec3(0, -1, 0).normalize());

        testPositions.add(new Vec3(0, 1, 1).normalize());
        testPositions.add(new Vec3(0, -1, -1).normalize());
        testPositions.add(new Vec3(0, 1, -1).normalize());
        testPositions.add(new Vec3(0, -1, 1).normalize());


        testPositions.add(new Vec3(0, 0, 1).normalize());
        testPositions.add(new Vec3(0, 0, -1).normalize());

        testPositions.add(new Vec3(1, 0, 1).normalize());
        testPositions.add(new Vec3(-1, 0, -1).normalize());
        testPositions.add(new Vec3(-1, 0, 1).normalize());
        testPositions.add(new Vec3(1, 0, -1).normalize());

        for (Vec3 testPosition : testPositions) {
            Vec3 targetPos = this.position().add(testPosition.scale(COLLISION_TEST_RANGE));

            HitResult hitResult = this.level().clip(new ClipContext(this.position(), targetPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
            boolean hit = hitResult.getType() != HitResult.Type.MISS;

            if (hit) {
                averageDirection = averageDirection.add(testPosition.reverse().scale(hitResult.getLocation().distanceToSqr(targetPos) / COLLISION_TEST_RANGE * COLLISION_TEST_RANGE));
                directionsApplied++;
            }
        }

        return new Vec3(averageDirection.x / directionsApplied,
                         averageDirection.y / directionsApplied,
                         averageDirection.z / directionsApplied).normalize();
    }

    public void updateUpDirection() {
        double hitAmount = 0;

        Vec3 hitAverage = Vec3.ZERO;

        List<Vec3> testPositions = new ArrayList<>();

        testPositions.add(new Vec3(1, 0, 0).normalize());
        testPositions.add(new Vec3(-1, 0, 0).normalize());

        testPositions.add(new Vec3(1, 1, 0).normalize());
        testPositions.add(new Vec3(-1, -1, 0).normalize());
        testPositions.add(new Vec3(1, -1, 0).normalize());
        testPositions.add(new Vec3(-1, 1, 0).normalize());

        testPositions.add(new Vec3(1, 1, 1).normalize());
        testPositions.add(new Vec3(-1, -1, -1).normalize());
        testPositions.add(new Vec3(1, 1, -1).normalize());
        testPositions.add(new Vec3(-1, -1, 1).normalize());

        testPositions.add(new Vec3(1, -1, -1).normalize());
        testPositions.add(new Vec3(-1, 1, 1).normalize());
        testPositions.add(new Vec3(1, -1, 1).normalize());
        testPositions.add(new Vec3(-1, 1, -1).normalize());

        testPositions.add(new Vec3(0, 1, 0).normalize());
        testPositions.add(new Vec3(0, -1, 0).normalize());

        testPositions.add(new Vec3(0, 1, 1).normalize());
        testPositions.add(new Vec3(0, -1, -1).normalize());
        testPositions.add(new Vec3(0, 1, -1).normalize());
        testPositions.add(new Vec3(0, -1, 1).normalize());


        testPositions.add(new Vec3(0, 0, 1).normalize());
        testPositions.add(new Vec3(0, 0, -1).normalize());

        testPositions.add(new Vec3(1, 0, 1).normalize());
        testPositions.add(new Vec3(-1, 0, -1).normalize());
        testPositions.add(new Vec3(-1, 0, 1).normalize());
        testPositions.add(new Vec3(1, 0, -1).normalize());

        for (Vec3 testPosition : testPositions) {
            Vec3 targetPos = this.position().add(testPosition.scale(TILTING_TEST_RANGE));

            boolean hit = this.level().clip(new ClipContext(this.position(), targetPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this)).getType() == HitResult.Type.BLOCK;

            if (hit) {
                hitAverage = hitAverage.add(testPosition);
                hitAmount++;
            }
        }

        if (hitAverage.lengthSqr() < 0.01) {
            return;
        } else {
            hitAverage = new Vec3(hitAverage.x / hitAmount, hitAverage.y / hitAmount, hitAverage.z / hitAmount);
        }

        double yaw = Math.atan2(hitAverage.x, hitAverage.z);

        double horizontalDistance = Math.sqrt(hitAverage.x * hitAverage.x + hitAverage.z * hitAverage.z);
        double pitch = -Math.atan2(hitAverage.y, horizontalDistance);

        Quaterniond newRotation = new Quaterniond()
                .identity()
                .rotateY(yaw)
                .rotateX(pitch);

        upDirection.nlerp(newRotation, 0.6);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput nbt) {

    }

    @Override
    protected void addAdditionalSaveData(ValueOutput nbt) {

    }

    // Entity.getRotationVector() now returns Vec2; this entity's forward direction is renamed.
    public Vec3 getForwardVector() {
        return MathUtil.toVec3(rotation.transform(new Vector3d(0, 0, 1))).normalize();
    }

    public Vec3 getUpDirection() {
        return MathUtil.toVec3(upDirection.transform(new Vector3d(0, 0, 1))).reverse().normalize();
    }

    public Vec3 getUpStationaryDirection() {
        return MathUtil.toVec3(rotation.transform(new Vector3d(0, 1, 0))).normalize();
    }

    public Vec3 getRightDirection() {
        return MathUtil.toVec3(rotation.transform(new Vector3d(1, 0, 0))).normalize();
    }

    @Override
    public List<IKModelComponent<WalkerEntity>> getComponents() {
        return components;
    }

    @Override
    public double getSize() {
        return 1;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {

    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
