package cn.nukkit.entity.ai.evaluator;

import cn.nukkit.entity.mob.EntityMob;

import java.util.concurrent.ThreadLocalRandom;


public class ProbabilityEvaluator implements IBehaviorEvaluator {

    protected int probability;
    protected int total;

    public ProbabilityEvaluator(int probability, int total) {
        this.probability = probability;
        this.total = total;
    }

    @Override
    public boolean evaluate(EntityMob entity) {
        return ThreadLocalRandom.current().nextInt(total) < probability;
    }
}
