package cn.nukkit.entity.ai.executor.armadillo;

import cn.nukkit.entity.ai.executor.EntityControl;
import cn.nukkit.entity.ai.executor.IBehaviorExecutor;
import cn.nukkit.entity.mob.EntityMob;
import cn.nukkit.entity.mob.animal.EntityArmadillo;


public class RelaxingExecutor implements EntityControl, IBehaviorExecutor {


    public RelaxingExecutor() {}
    @Override
    public boolean execute(EntityMob entity) {
        return false;
    }

    @Override
    public void onStart(EntityMob entity) {
        removeLookTarget(entity);
        removeRouteTarget(entity);
        if(entity instanceof EntityArmadillo armadillo) {
            armadillo.setRollState(EntityArmadillo.RollState.ROLLED_UP_RELAXING);
        }
    }

}
