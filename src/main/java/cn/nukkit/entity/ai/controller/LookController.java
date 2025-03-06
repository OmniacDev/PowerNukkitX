package cn.nukkit.entity.ai.controller;

import cn.nukkit.entity.mob.EntityMob;
import cn.nukkit.math.BVector3;
import cn.nukkit.math.Vector3;

/**
 * 处理实体Pitch/Yaw/HeadYaw
 */


public class LookController implements IController {

    protected boolean lookAtTarget;
    protected boolean lookAtRoute;

    public LookController(boolean lookAtTarget, boolean lookAtRoute) {
        this.lookAtTarget = lookAtTarget;
        this.lookAtRoute = lookAtRoute;
    }

    @Override
    public boolean control(EntityMob entity) {
        Vector3 lookTarget = entity.getLookTarget();

        if (lookAtRoute && entity.hasMoveDirection()) {
            //clone防止异步导致的NPE
            Vector3 moveDirectionEnd = entity.getMoveDirectionEnd().clone();
            //构建路径方向向量
            var routeDirectionVector = new Vector3(moveDirectionEnd.x - entity.position.x, moveDirectionEnd.y - entity.position.y, moveDirectionEnd.z - entity.position.z);
            var yaw = BVector3.getYawFromVector(routeDirectionVector);
            entity.rotation.yaw = (yaw);
            if (!lookAtTarget) {
                entity.headYaw = (yaw);
                if (entity.isEnablePitch()) entity.rotation.pitch = (BVector3.getPitchFromVector(routeDirectionVector));
            }
        }
        if (lookAtTarget && lookTarget != null) {
            //构建指向玩家的向量
            var toPlayerVector = new Vector3(lookTarget.x - entity.position.x, lookTarget.y - entity.position.y, lookTarget.z - entity.position.z);
            if (entity.isEnablePitch()) entity.rotation.pitch = (BVector3.getPitchFromVector(toPlayerVector));
            entity.headYaw = (BVector3.getYawFromVector(toPlayerVector));
        }
        if (!entity.isEnablePitch()) entity.rotation.pitch = (0);
        return true;
    }
}
