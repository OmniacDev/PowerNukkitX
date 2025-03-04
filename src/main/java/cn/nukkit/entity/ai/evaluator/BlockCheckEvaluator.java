package cn.nukkit.entity.ai.evaluator;

import cn.nukkit.entity.mob.EntityMob;
import cn.nukkit.math.Vector3;


public class BlockCheckEvaluator implements IBehaviorEvaluator {

    protected String blockId;
    protected Vector3 offsetVec;

    public BlockCheckEvaluator(String blockId, Vector3 offsetVec) {
        this.blockId = blockId;
        this.offsetVec = offsetVec;
    }

    @Override
    public boolean evaluate(EntityMob entity) {
        return entity.level.getTickCachedBlock(entity.pos.add(offsetVec)).getId().equals(blockId);
    }
}
