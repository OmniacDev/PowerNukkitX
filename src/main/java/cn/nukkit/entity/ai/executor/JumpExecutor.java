package cn.nukkit.entity.ai.executor;

import cn.nukkit.entity.mob.EntityMob;
import cn.nukkit.math.Vector3;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class JumpExecutor implements IBehaviorExecutor {

    @Override
    public boolean execute(EntityMob entity) {
        entity.setMotion(new Vector3(0, entity.getJumpingMotion(0.4), 0));
        return true;
    }
}
