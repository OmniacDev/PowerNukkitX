package cn.nukkit.entity.ai.executor.armadillo;

import cn.nukkit.entity.ai.executor.EntityControl;
import cn.nukkit.entity.ai.executor.IBehaviorExecutor;
import cn.nukkit.entity.mob.EntityMob;
import cn.nukkit.entity.mob.animal.EntityArmadillo;

public class PeekExecutor implements EntityControl, IBehaviorExecutor {

    protected int tick = 0;

    private static final int STAY_TICKS = 60;


    public PeekExecutor() {}
    @Override
    public boolean execute(EntityMob entity) {
        if(tick < STAY_TICKS) {
            tick++;
            return true;
        }
        return false;
    }

    @Override
    public void onStart(EntityMob entity) {
        this.tick = 0;
    }

    @Override
    public void onStop(EntityMob entity) {
        if(entity instanceof EntityArmadillo armadillo) {
            if(!new EntityArmadillo.RollupEvaluator().evaluate(entity)) {
                armadillo.setRollState(EntityArmadillo.RollState.ROLLED_UP_UNROLLING);
            } else armadillo.setRollState(EntityArmadillo.RollState.ROLLED_UP);
        }
    }

    @Override
    public void onInterrupt(EntityMob entity) {
        onStop(entity);
    }
}
