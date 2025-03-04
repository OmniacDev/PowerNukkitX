package cn.nukkit.entity.ai.executor;

import cn.nukkit.entity.mob.EntityMob;
import cn.nukkit.entity.mob.animal.EntityAnimal;


public class AnimalGrowExecutor implements IBehaviorExecutor {

    @Override
    public boolean execute(EntityMob entity) {
        if (entity instanceof EntityAnimal animal) {
            animal.setBaby(false);
        }
        return false;
    }
}
