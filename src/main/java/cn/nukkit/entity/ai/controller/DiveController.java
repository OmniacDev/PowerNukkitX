package cn.nukkit.entity.ai.controller;

import cn.nukkit.entity.ai.memory.CoreMemoryTypes;
import cn.nukkit.entity.mob.EntityMob;

/**
 * 下潜运动控制器，使实体下潜
 */


public class DiveController implements IController {
    @Override
    public boolean control(EntityMob entity) {
        //add dive force
        if (entity.getMemoryStorage().get(CoreMemoryTypes.ENABLE_DIVE_FORCE))
            //                                                                  抵消额外的浮力即可
            entity.motion.y -= entity.getGravity() * (entity.getFloatingForceFactor() - 1);
        return true;
    }
}
