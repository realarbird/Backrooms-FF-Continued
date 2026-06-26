package com.sp.entity.ik.parts.ik_chains;

import com.sp.entity.ik.parts.Segment;
import com.sp.entity.ik.util.MathUtil;
import net.minecraft.world.phys.Vec3;

public class TargetReachingIKChain extends StretchingIKChain {
    public TargetReachingIKChain(double... lengths) {
        super(lengths);
    }

    public TargetReachingIKChain(Segment... segments) {
        super(segments);
    }

    @Override
    public Vec3 getStretchingPos(Vec3 target, Vec3 base) {
        Vec3 flatTargetDir = MathUtil.convertToFlatVector(target.subtract(base)).normalize();

        Vec3 newPos = base.add(flatTargetDir.scale(this.getMaxLength())).add(0, this.getMaxLength(), 0);

        return newPos;
    }
}
