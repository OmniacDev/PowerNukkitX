package cn.nukkit.entity.ai.controller;

import cn.nukkit.entity.ai.memory.CoreMemoryTypes;
import cn.nukkit.entity.mob.EntityMob;

/**
 * 为飞行生物提供升力的运动控制器
 */


public class LiftController implements IController {
    @Override
    public boolean control(EntityMob entity) {
        //add lift force
        if (entity.getMemoryStorage().get(CoreMemoryTypes.ENABLE_LIFT_FORCE))
            entity.motion.y += entity.getGravity();
        return true;
    }
}
