package cn.nukkit.entity.ai.controller;

import cn.nukkit.entity.mob.EntityMob;
import cn.nukkit.entity.data.EntityFlag;
import cn.nukkit.math.Vector3;

/**
 * 处理飞行/游泳实体运动
 */


public class SpaceMoveController implements IController {
    @Override
    public boolean control(EntityMob entity) {
        if (entity.hasMoveDirection() && !entity.isShouldUpdateMoveDirection()) {
            Vector3 direction = entity.getMoveDirectionEnd();
            var speed = entity.getMovementSpeed();
            if (entity.motion.x * entity.motion.x + entity.motion.y * entity.motion.y + entity.motion.z * entity.motion.z > speed * speed * 0.4756) {
                return false;
            }
            var relativeVector = direction.clone().setComponents(direction.x - entity.position.x,
                    direction.y - entity.position.y, direction.z - entity.position.z);
            var xyzLength = Math.sqrt(relativeVector.x * relativeVector.x + relativeVector.y * relativeVector.y + relativeVector.z * relativeVector.z);
            var k = speed / xyzLength * 0.33;
            var dx = relativeVector.x * k;
            var dy = relativeVector.y * k;
            var dz = relativeVector.z * k;
            entity.addTmpMoveMotion(new Vector3(dx, dy, dz));
            entity.setDataFlag(EntityFlag.MOVING, true);
            if (xyzLength < speed) {
                needNewDirection(entity);
                return false;
            }
            return true;
        } else {
            entity.setDataFlag(EntityFlag.MOVING, false);
            return false;
        }
    }

    protected void needNewDirection(EntityMob entity) {
        //通知需要新的移动目标
        entity.setShouldUpdateMoveDirection(true);
    }
}
