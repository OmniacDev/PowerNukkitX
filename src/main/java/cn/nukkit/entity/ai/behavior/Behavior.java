package cn.nukkit.entity.ai.behavior;

import cn.nukkit.entity.ai.evaluator.IBehaviorEvaluator;
import cn.nukkit.entity.ai.executor.IBehaviorExecutor;
import cn.nukkit.entity.mob.EntityMob;
import lombok.Getter;

/**
 * 单个的行为对象，包含一个执行器和一个评估器，行为对象委托了它们的方法
 * <p>
 * A single behavior object, containing an executor and an evaluator, with the behavior object delegating their methods
 */


@Getter
public class Behavior extends AbstractBehavior {

    protected final int priority;
    protected final int weight;
    protected final int period;
    protected IBehaviorExecutor executor;
    protected IBehaviorEvaluator evaluator;
    protected boolean reevaluate;

    public Behavior(IBehaviorExecutor executor, IBehaviorEvaluator evaluator) {
        this(executor, evaluator, 1);
    }

    public Behavior(IBehaviorExecutor executor, IBehaviorEvaluator evaluator, int priority) {
        this(executor, evaluator, priority, 1);
    }

    public Behavior(IBehaviorExecutor executor, IBehaviorEvaluator evaluator, int priority, int weight) {
        this(executor, evaluator, priority, weight, 1);
    }

    public Behavior(IBehaviorExecutor executor, IBehaviorEvaluator evaluator, int priority, int weight, int period) {
        this(executor, evaluator, priority, weight, period, true);
    }

    public Behavior(IBehaviorExecutor executor, IBehaviorEvaluator evaluator, int priority, int weight, int period, boolean reevaluate) {
        this.executor = executor;
        this.evaluator = evaluator;
        this.priority = priority;
        this.weight = weight;
        this.period = period;
        this.reevaluate = reevaluate;
    }

    @Override
    public boolean evaluate(EntityMob entity) {
        return evaluator.evaluate(entity);
    }

    @Override
    public boolean execute(EntityMob entity) {
        return executor.execute(entity);
    }

    @Override
    public void onStart(EntityMob entity) {
        executor.onStart(entity);
    }

    @Override
    public void onInterrupt(EntityMob entity) {
        executor.onInterrupt(entity);
    }

    @Override
    public void onStop(EntityMob entity) {
        executor.onStop(entity);
    }

    @Override
    public String toString() {
        return "[" + priority + "] " + executor.getClass().getSimpleName() + " | " + evaluator.getClass().getSimpleName();
    }
}
