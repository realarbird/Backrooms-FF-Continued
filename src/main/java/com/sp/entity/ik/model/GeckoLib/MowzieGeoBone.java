package com.sp.entity.ik.model.GeckoLib;

import com.sp.entity.ik.model.BoneAccessor;
import com.sp.entity.ik.util.MathUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3f;

import com.geckolib.cache.model.GeoBone;

import java.util.ArrayList;
import java.util.List;

/**
 * Provided by Bob Mowzie from <a href="https://www.curseforge.com/minecraft/mc-mods/mowzies-mobs">Mowzie's Mobs</a>
 * <p>
 * PORT: delta (GeckoLib 4 -> 5). In GeckoLib 4 this class {@code extends GeoBone} and stored its
 * procedural IK transform directly on the (mutable) bone, which the renderer's custom
 * {@code renderRecursively} consumed via {@code getWorldSpaceMatrix()}/{@code setWorldSpaceMatrix()}.
 * GeckoLib 5's {@link GeoBone} is an immutable abstract class (final fields, record-style accessors,
 * no setters, {@code CuboidGeoBone} is {@code final}) and there is no per-bone world-space matrix nor
 * an overridable {@code renderRecursively}. It can no longer be subclassed.
 * <p>
 * This is therefore rebuilt as a standalone <b>wrapper</b> around the real (immutable) {@link GeoBone}:
 * it holds the per-frame procedural IK transform (world-space matrix + rotation override + pos/rot/scale
 * deltas + the inherit/force flags) that the IK engine writes through {@link BoneAccessor}, and exposes
 * accessors the renderer reads back when applying the pose. The transform read/write contract that the
 * engine-agnostic IK math depends on ({@link #getPosition()}, {@link #moveTo}) is preserved; the
 * mechanism by which the renderer applies it changes (snapshot/RenderPassInfo based, see the renderers).
 */
public class MowzieGeoBone implements BoneAccessor {

    private final GeoBone delegate;

    // PORT: delta - per-frame procedural transform state formerly stored on the GeoBone itself.
    public Matrix4f rotationOverride;
    public boolean inheritRotation = true;
    public boolean inheritTranslation = true;
    protected boolean forceMatrixTransform = false;
    private boolean isDynamicJoint = false;

    // PORT: delta - world-space matrix is no longer tracked by GeckoLib per-bone; we hold it here so
    // the IK output (moveTo) survives until the renderer applies it for the frame.
    private Matrix4f worldSpaceMatrix = new Matrix4f();
    private Matrix4f modelSpaceMatrix = new Matrix4f();

    // Local pos/rot/scale deltas (radians for rot), formerly GeoBone mutable fields.
    private float posX, posY, posZ;
    private float rotX, rotY, rotZ;
    private float scaleX = 1, scaleY = 1, scaleZ = 1;

    public MowzieGeoBone(GeoBone delegate) {
        this.delegate = delegate;
        this.rotationOverride = null;
    }

    public GeoBone getDelegate() {
        return this.delegate;
    }

    public String getName() {
        return this.delegate.name();
    }

    public static void removeMatrixTranslation(Matrix4f matrix) {
        matrix.m30(0);
        matrix.m31(0);
        matrix.m32(0);
    }

    // Position utils
    public void addPos(Vec3 vec) {
        addPos((float) vec.x, (float) vec.y, (float) vec.z);
    }

    public void addPos(float x, float y, float z) {
        addPosX(x);
        addPosY(y);
        addPosZ(z);
    }

    public void addPosX(float x) {
        setPosX(getPosX() + x);
    }

    public void addPosY(float y) {
        setPosY(getPosY() + y);
    }

    public void addPosZ(float z) {
        setPosZ(getPosZ() + z);
    }

    public void setPos(float x, float y, float z) {
        setPosX(x);
        setPosY(y);
        setPosZ(z);
    }

    public Vec3 getPos() {
        return new Vec3(getPosX(), getPosY(), getPosZ());
    }

    public void setPos(Vec3 vec) {
        setPos((float) vec.x, (float) vec.y, (float) vec.z);
    }

    public float getPosX() { return posX; }
    public float getPosY() { return posY; }
    public float getPosZ() { return posZ; }
    public void setPosX(float x) { this.posX = x; }
    public void setPosY(float y) { this.posY = y; }
    public void setPosZ(float z) { this.posZ = z; }

    // Rotation utils
    public void addRot(Vec3 vec) {
        addRot((float) vec.x, (float) vec.y, (float) vec.z);
    }

    public void addRot(float x, float y, float z) {
        addRotX(x);
        addRotY(y);
        addRotZ(z);
    }

    public void addRotX(float x) {
        setRotX(getRotX() + x);
    }

    public void addRotY(float y) {
        setRotY(getRotY() + y);
    }

    public void addRotZ(float z) {
        setRotZ(getRotZ() + z);
    }

    public void setRot(float x, float y, float z) {
        setRotX(x);
        setRotY(y);
        setRotZ(z);
    }

    public Vector3d getRot() {
        return new Vector3d(getRotX(), getRotY(), getRotZ());
    }

    public void setRot(Vector3d vec) {
        setRot((float) vec.x(), (float) vec.y(), (float) vec.z());
    }

    public void setRot(Vec3 vec) {
        setRot((float) vec.x, (float) vec.y, (float) vec.z);
    }

    public float getRotX() { return rotX; }
    public float getRotY() { return rotY; }
    public float getRotZ() { return rotZ; }
    public void setRotX(float x) { this.rotX = x; }
    public void setRotY(float y) { this.rotY = y; }
    public void setRotZ(float z) { this.rotZ = z; }

    // Scale utils
    public void multiplyScale(Vec3 vec) {
        multiplyScale((float) vec.x, (float) vec.y, (float) vec.z);
    }

    public void multiplyScale(float x, float y, float z) {
        setScaleX(getScaleX() * x);
        setScaleY(getScaleY() * y);
        setScaleZ(getScaleZ() * z);
    }

    public void setScale(float x, float y, float z) {
        setScaleX(x);
        setScaleY(y);
        setScaleZ(z);
    }

    public Vector3d getScale() {
        return new Vector3d(getScaleX(), getScaleY(), getScaleZ());
    }

    public void setScale(Vec3 vec) {
        setScale((float) vec.x, (float) vec.y, (float) vec.z);
    }

    public void setScale(Vector3d vec) {
        setScale((float) vec.x(), (float) vec.y(), (float) vec.z());
    }

    public void setScale(float scale) {
        setScale(scale, scale, scale);
    }

    public float getScaleX() { return scaleX; }
    public float getScaleY() { return scaleY; }
    public float getScaleZ() { return scaleZ; }
    public void setScaleX(float x) { this.scaleX = x; }
    public void setScaleY(float y) { this.scaleY = y; }
    public void setScaleZ(float z) { this.scaleZ = z; }

    public boolean isForceMatrixTransform() {
        return forceMatrixTransform;
    }

    public void setForceMatrixTransform(boolean forceMatrixTransform) {
        this.forceMatrixTransform = forceMatrixTransform;
    }

    public Matrix4f getWorldSpaceMatrix() {
        return new Matrix4f(this.worldSpaceMatrix);
    }

    public void setWorldSpaceMatrix(Matrix4f matrix) {
        this.worldSpaceMatrix = matrix;
    }

    public Matrix4f getModelSpaceMatrix() {
        return new Matrix4f(this.modelSpaceMatrix);
    }

    public void setModelSpaceMatrix(Matrix4f matrix) {
        this.modelSpaceMatrix = matrix;
    }

    public Matrix4f getModelRotationMat() {
        Matrix4f matrix = new Matrix4f(getModelSpaceMatrix());
        removeMatrixTranslation(matrix);
        return matrix;
    }

    public void setModelXformOverride(Matrix4f mat) {
        rotationOverride = mat;
    }

    public boolean isDynamicJoint() {
        return isDynamicJoint;
    }

    public void setDynamicJoint(boolean dynamicJoint) {
        isDynamicJoint = dynamicJoint;
    }

    private Vector3f getWorldPosition() {
        // PORT: delta - was RenderUtils-derived from the bone's world-space matrix; now read from the
        // matrix this wrapper tracks (translation column).
        return new Vector3f(worldSpaceMatrix.m30(), worldSpaceMatrix.m31(), worldSpaceMatrix.m32());
    }

    @Override
    public Vec3 getPosition() {
        return MathUtil.toVec3(new Vector3d(getWorldPosition()));
    }

    @Override
    public void moveTo(Vec3 to, @Nullable Vec3 facing, Entity entity) {
        this.setForceMatrixTransform(true);

        Matrix4f xformOverride = new Matrix4f();

        Vec3 newModelPosWorldSpace;
        if (entity instanceof LivingEntity) {
            newModelPosWorldSpace = MathUtil.rotatePointOnAPlaneAround(to, entity.position(), -180 + entity.getVisualRotationYInDegrees(), new Vec3(0, 1, 0));
        } else {
            newModelPosWorldSpace = MathUtil.rotatePointOnAPlaneAround(to, entity.position(), -180, new Vec3(0, 1, 0));
        }
        // Translation
        xformOverride = xformOverride.translate(newModelPosWorldSpace.toVector3f());

        if (facing != null) {
            Vec3 newTargetVecWorldSpace;
            if (entity instanceof LivingEntity) {
                newTargetVecWorldSpace = MathUtil.rotatePointOnAPlaneAround(facing, entity.position(), -180 + entity.getVisualRotationYInDegrees(), new Vec3(0, 1, 0));
            } else {
                newTargetVecWorldSpace = MathUtil.rotatePointOnAPlaneAround(facing, entity.position(), -180, new Vec3(0, 1, 0));
            }

            Quaternionf q;
            Vector3f desiredDir = newTargetVecWorldSpace.toVector3f().sub(newModelPosWorldSpace.toVector3f(), new Vector3f()).normalize();

            Vector3f startingDir = new Vector3f(0, 0, 1);
            double dot = desiredDir.dot(startingDir);
            if (dot > 0.9999999) {
                q = new Quaternionf();
            } else {
                q = new Quaternionf().rotateTo(startingDir, desiredDir).normalize();
            }
            xformOverride.rotate(q);
        }

        this.setWorldSpaceMatrix(xformOverride);
    }

    @Override
    public List<BoneAccessor> getChildren() {
        // PORT: delta - children come from the immutable delegate's GeoBone[] children(); we wrap each
        // in a fresh MowzieGeoBone so the IK engine sees BoneAccessor wrappers. (GeoModelAccessor caches
        // wrappers per name; transient wrappers here are only used for traversal/position reads.)
        List<BoneAccessor> children = new ArrayList<>();
        for (GeoBone child : this.delegate.children()) {
            children.add(new MowzieGeoBone(child));
        }
        return children;
    }
}
