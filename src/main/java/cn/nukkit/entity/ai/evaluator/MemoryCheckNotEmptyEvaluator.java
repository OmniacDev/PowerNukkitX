package cn.nukkit.entity.ai.evaluator;

import cn.nukkit.entity.ai.memory.MemoryType;
import cn.nukkit.entity.mob.EntityMob;


public class MemoryCheckNotEmptyEvaluator implements IBehaviorEvaluator {

    protected MemoryType<?> type;

    public MemoryCheckNotEmptyEvaluator(MemoryType<?> type) {
        this.type = type;
    }

    @Override
    public boolean evaluate(EntityMob entity) {
        return entity.getBehaviorGroup().getMemoryStorage().notEmpty(type);
    }
}
