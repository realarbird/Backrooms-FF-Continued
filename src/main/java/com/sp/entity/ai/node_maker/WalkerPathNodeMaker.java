package com.sp.entity.ai.node_maker;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import org.jetbrains.annotations.Nullable;

public class WalkerPathNodeMaker extends WalkNodeEvaluator {
    private static final int HOVER_HEIGHT = 3;
    private static final int SCAN_RADIUS = 4;

    @Override
    protected Node getStartNode(BlockPos pos) {
        BlockPos hoverPos = findNearestSupportBlock(pos);
        if (hoverPos != null) {
            Node pathNode = this.getNode(hoverPos.getX(), hoverPos.getY() - HOVER_HEIGHT, hoverPos.getZ());
            pathNode.type = PathType.WALKABLE;
            pathNode.costMalus = 0.0F;
            return pathNode;
        }
        return super.getStartNode(pos);
    }

    @Override
    public int getNeighbors(Node[] successors, Node node) {
        int i = 0;

        // Check all 6 directions (including up and down)
        for (Direction direction : Direction.values()) {
            Node successor = getHoveringPathNode(
                    node.x + direction.getStepX(),
                    node.y + direction.getStepY(),
                    node.z + direction.getStepZ()
            );

            if (successor != null && !successor.closed && successor.costMalus >= 0.0F) {
                successors[i++] = successor;
            }
        }

        return i;
    }

    @Nullable
    private Node getHoveringPathNode(int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);

        // Check if current position has air
        if (!this.currentContext.getBlockState(pos).isAir()) {
            return null;
        }

        // Check if there's a support block within radius
        if (!hasSupportBlockNearby(pos)) {
            return null;
        }

        Node pathNode = this.getNode(x, y, z);
        pathNode.type = PathType.WALKABLE;
        pathNode.costMalus = 0.0F;

        return pathNode;
    }

    private boolean hasSupportBlockNearby(BlockPos center) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (int x = -SCAN_RADIUS; x <= SCAN_RADIUS; x++) {
            for (int y = -SCAN_RADIUS; y <= SCAN_RADIUS; y++) {
                for (int z = -SCAN_RADIUS; z <= SCAN_RADIUS; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;

                    mutable.set(center.getX() + x, center.getY() + y, center.getZ() + z);

                    // Check if position one block below support has air
                    BlockPos airCheck = mutable.above();
                    if (!this.currentContext.getBlockState(airCheck).isAir()) {
                        continue;
                    }

                    // Check if support block is solid
                    if (isSolidSupport(mutable)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Nullable
    private BlockPos findNearestSupportBlock(BlockPos start) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (int radius = 0; radius <= SCAN_RADIUS; radius++) {
            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        mutable.set(start.getX() + x, start.getY() + y, start.getZ() + z);

                        if (isSolidSupport(mutable) &&
                                this.currentContext.getBlockState(mutable.above()).isAir()) {
                            return mutable.immutable();
                        }
                    }
                }
            }
        }

        return null;
    }

    private boolean isSolidSupport(BlockPos pos) {
        BlockGetter world = this.currentContext.level();
        BlockState state = this.currentContext.getBlockState(pos);
        return !state.isAir() && state.isCollisionShapeFullBlock(world, pos);
    }

    @Override
    protected double getFloorLevel(BlockPos pos) {
        return pos.getY() - HOVER_HEIGHT;
    }
}
