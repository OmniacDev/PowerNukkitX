package cn.nukkit.entity.ai.evaluator;

import cn.nukkit.entity.mob.EntityMob;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class NotMatchEvaluator implements IBehaviorEvaluator {

    private IBehaviorEvaluator evaluator;

    @Override
    public boolean evaluate(EntityMob entity) {
        return !evaluator.evaluate(entity);
    }
}
