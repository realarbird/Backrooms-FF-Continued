package com.sp.entity.ik.parts.ik_chains;

import com.sp.entity.custom.WalkerEntity;
import com.sp.entity.ik.parts.Segment;
import net.minecraft.world.phys.Vec3;

public class BendReachingIKChain extends StretchingIKChain {
    public final WalkerEntity entity;

    public BendReachingIKChain(WalkerEntity entity, double... lengths) {
        super(lengths);
        this.entity = entity;
    }

    public BendReachingIKChain(WalkerEntity entity, Segment... segments) {
        super(segments);
        this.entity = entity;
    }

    @Override
    public Vec3 getStretchingPos(Vec3 target, Vec3 base) {
        return target;
    }

    @Override
    public void stretch(Vec3 target, Vec3 base) {
        Vec3 flatTargetDir = target.subtract(base).add(entity.getUpDirection().scale(3)).normalize();

        this.getFirst().move(base);

        Vec3 newPos = base.add(flatTargetDir.scale(2)).add(entity.getUpDirection().scale(this.getMaxLength()));

        Vec3 directionOfTarget = newPos.subtract(base).normalize();

        for (int i = 1; i < this.segments.size(); i++) {
            Segment prevSegment = this.segments.get(i - 1);
            Segment currentSegment = this.segments.get(i);

            if (i != 1) {
                currentSegment.move(prevSegment.getPosition().add(directionOfTarget.scale(prevSegment.length * this.getScale())));
                continue;
            }

            currentSegment.move(prevSegment.getPosition().add(flatTargetDir.scale(prevSegment.length * this.getScale())));
        }

        this.endJoint = this.getLast().getPosition().add(directionOfTarget.scale(this.getLast().length * this.getScale()));
    }
}
