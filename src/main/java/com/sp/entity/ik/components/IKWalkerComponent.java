package com.sp.entity.ik.components;

import com.sp.entity.custom.WalkerEntity;
import com.sp.entity.ik.model.BoneAccessor;
import com.sp.entity.ik.model.ModelAccessor;
import com.sp.entity.ik.parts.ik_chains.IKChain;
import com.sp.entity.ik.parts.sever_limbs.ServerLimb;
import com.sp.entity.ik.util.MathUtil;
import com.sp.entity.ik.util.PrAnCommonClass;
// PORT: phase6 - render-pipeline imports (RenderType/MultiBufferSource/PoseStack) + WalkerLegDebugRenderer deferred
// import com.sp.entity.ik.components.debug_renderers.WalkerLegDebugRenderer;
// import com.sp.entity.ik.model.GeckoLib.MowzieGeoBone;
// import net.minecraft.client.render.RenderLayer;
// import net.minecraft.client.render.VertexConsumer;
// import net.minecraft.client.render.VertexConsumerProvider;
// import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class IKWalkerComponent<C extends IKChain, E extends IKAnimatable<E>> extends IKLegComponent<C, E> {
    /// summon projectnublar:tyrannosaurus_rex ~ ~ ~ {NoAI:1b}
    public IKWalkerComponent(List<IKLegComponent.LegSetting> settings, List<ServerLimb> endpoints, C... limbs) {
        super(settings, endpoints, limbs);
    }

    public IKWalkerComponent(IKLegComponent.LegSetting settings, List<ServerLimb> endpoints, C... limbs) {
        super(settings, endpoints, limbs);
    }

    @Override
    public void tickClient(E animatable, ModelAccessor model) {
        if (!(animatable instanceof WalkerEntity entity)) {
            return;
        }

        for (int i = 0; i < this.limbs.size(); i++) {
            if (model.getBone("base_" + "leg" + (i + 1)).isEmpty()) {
                return;
            }
            //BoneAccessor baseAccessor = model.getBone("base_" + "leg" + (i + 1)).get();//

            //Vec3 basePosWorldSpace = baseAccessor.getgetPos()();
            if (this.bases.isEmpty()) {
                return;
            }

            Optional<BoneAccessor> root = model.getBone("root");

            // PORT: phase6 - MowzieGeoBone bridges into GeckoLib's reworked GeoBone/render pipeline
            // if (root.isPresent()) {
            //     BoneAccessor bone = root.get();
            //
            //     if (bone instanceof MowzieGeoBone mowzieGeoBone) {
            //         mowzieGeoBone.setForceMatrixTransform(true);
            //
            //         Matrix4f xformOverride = new Matrix4f();
            //
            //         Vec3 newModelPosWorldSpace = MathUtil.rotatePointOnAPlaneAround(entity.position(), entity.position(), -180, new Vec3(0, 1, 0));
            //
            //         xformOverride = xformOverride.translate(newModelPosWorldSpace.toVector3f());
            //         //xformOverride.rotate(new Quaternionf(entity.getRotation().x, entity.getRotation().y, -entity.getRotation().z, entity.getRotation().w));
            //
            //         xformOverride.rotateYXZ((float) -Math.toRadians(entity.getYRot()), (float) -Math.toRadians(entity.getXRot() + 90), 0);
            //
            //
            //         xformOverride.rotateZ((float) -Math.toRadians(entity.getRoll()));
            //
            //         mowzieGeoBone.setWorldSpaceMatrix(xformOverride);
            //     }
            // }

            Vec3 basePosWorldSpace = this.bases.get(i);

            C limb = this.setLimb(i, basePosWorldSpace, entity);

            for (int k = 0; k < limb.getJoints().size() - 1; k++) {
                Vec3 modelPosWorldSpace = limb.getJoints().get(k);
                Vec3 targetVecWorldSpace = limb.getJoints().get(k + 1);

                if (model.getBone("segment" + (k + 1) + "_leg" + (i + 1)).isEmpty()) {
                    return;
                }

                BoneAccessor legSegmentAccessor = model.getBone("segment" + (k + 1) + "_leg" + (i + 1)).get();

                if (PrAnCommonClass.shouldRenderDebugLegs) {
                    modelPosWorldSpace = modelPosWorldSpace.subtract(0, 200, 0);
                    targetVecWorldSpace = targetVecWorldSpace.subtract(0, 200, 0);
                }

                legSegmentAccessor.moveTo(modelPosWorldSpace, targetVecWorldSpace, entity);
            }
        }
    }

    @Override
    public void tickServer(E animatable) {
        this.setScale(animatable.getSize());

        if (!(animatable instanceof WalkerEntity entity)) {
            return;
        }
        Level world = entity.level();

        for (int i = 0; i < this.endPoints.size(); i++) {
            ServerLimb limb = this.endPoints.get(i);

            limb.tick(this, i);

            Vec3 limbOffsetMultiplier = limb.baseOffset.scale(this.getScale());

            Vec3 limbOffset = Vec3.ZERO;

            limbOffset = limbOffset.add(entity.getUpDirection().cross(entity.getForwardVector()).scale(limbOffsetMultiplier.x));

            limbOffset = limbOffset.add(entity.getUpDirection().scale(limbOffsetMultiplier.y));

            limbOffset = limbOffset.add(entity.getForwardVector().scale(limbOffsetMultiplier.z));

            if (hasMovedOverLastTick(entity)) {
                limbOffset = limbOffset.add(0, 0, this.getSettings().get(0).stepInFront() * this.getScale());
            }

            Vec3 rotatedLimbOffset = limbOffset.add(entity.position());
            Vec3 upPoint = rotatedLimbOffset.add(entity.getUpDirection().scale(1));
            HitResult baseRayCastResult = world.clip(new ClipContext(upPoint, rotatedLimbOffset.add(entity.getUpDirection().scale(-10)), ClipContext.Block.COLLIDER, this.getSettings().get(0).fluid(), entity));

            Vec3 bestHit = baseRayCastResult.getLocation();
            double bestDistance = baseRayCastResult.getType() == HitResult.Type.MISS ? Double.MAX_VALUE : 0.5;

            List<Vec3> upDirs = new ArrayList<>();
            upDirs.add(MathUtil.toVec3(entity.upDirection.transform(new Vector3d(0, 1, -2).normalize())));
            upDirs.add(MathUtil.toVec3(entity.upDirection.transform(new Vector3d(0, -1, -2).normalize())));
            upDirs.add(MathUtil.toVec3(entity.upDirection.transform(new Vector3d(1, 0, -2).normalize())));
            upDirs.add(MathUtil.toVec3(entity.upDirection.transform(new Vector3d(-1, 0, -2).normalize())));
            upDirs.add(MathUtil.toVec3(entity.upDirection.transform(new Vector3d(-0.5, 0.5, -2).normalize())));
            upDirs.add(MathUtil.toVec3(entity.upDirection.transform(new Vector3d(0.5, 0.5, -2).normalize())));
            upDirs.add(MathUtil.toVec3(entity.upDirection.transform(new Vector3d(-0.5, -0.5, -2).normalize())));
            upDirs.add(MathUtil.toVec3(entity.upDirection.transform(new Vector3d(0.5, -0.5, -2).normalize())));

            for (Vec3 upDir : upDirs) {
                BlockHitResult rayCastResult = world.clip(new ClipContext(upPoint, upPoint.add(upDir.scale(-10)), ClipContext.Block.COLLIDER, this.getSettings().get(0).fluid(), entity));

                if (rayCastResult.getType() == HitResult.Type.MISS) {
                    continue;
                }

                if (rayCastResult.getLocation().distanceToSqr(baseRayCastResult.getLocation()) < bestDistance) {
                    bestDistance = rayCastResult.getLocation().distanceToSqr(baseRayCastResult.getLocation());
                    bestHit = rayCastResult.getLocation();
                }
            }


            if (limb.hasToBeSet) {
                limb.set(bestHit);
                limb.hasToBeSet = false;
            }

            if (!bestHit.closerThan(limb.target, this.getMaxLegFormTargetDistance(entity))) {
                limb.setTarget(bestHit);
            }
        }
    }

    @Override
    public boolean hasMovedOverLastTick(Entity entity) {
        if (entity instanceof WalkerEntity walker) {
            return walker.isWalking;
        }

        return super.hasMovedOverLastTick(entity);
    }

    // PORT: phase6 - debug rendering uses the reworked deferred render pipeline + WalkerLegDebugRenderer
    // @Override
    // public void renderDebug(MatrixStack poseStack, E animatable, RenderLayer renderType, VertexConsumerProvider bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
    //     new WalkerLegDebugRenderer<E, C>().renderDebug(this, animatable, poseStack, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
    // }
}
