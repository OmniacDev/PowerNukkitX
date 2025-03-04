package cn.nukkit.entity.ai.executor;

import cn.nukkit.entity.mob.EntityMob;

public class DoNothingExecutor implements IBehaviorExecutor {

    public DoNothingExecutor() {
    }
    @Override
    public boolean execute(EntityMob entity) {
        return true;
    }
}
