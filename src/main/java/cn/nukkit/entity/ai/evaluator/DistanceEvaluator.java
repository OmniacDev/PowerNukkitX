package cn.nukkit.entity.ai.evaluator;

import cn.nukkit.entity.ai.memory.MemoryType;
import cn.nukkit.entity.mob.EntityMob;
import cn.nukkit.math.IVector3;
import cn.nukkit.math.Vector3;

public class DistanceEvaluator implements IBehaviorEvaluator {

    private final MemoryType<? extends IVector3> type;
    private final double maxDistance;
    private final double minDistance ;


    public DistanceEvaluator(MemoryType<? extends IVector3> type, double maxDistance) {
        this(type, maxDistance, -1);
    }

    public DistanceEvaluator(MemoryType<? extends IVector3> type, double maxDistance, double minDistance) {
        this.type = type;
        this.maxDistance = maxDistance;
        this.minDistance = minDistance;
    }

    @Override
    public boolean evaluate(EntityMob entity) {
        if (entity.getMemoryStorage().isEmpty(type)) {
            return false;
        } else {
            Vector3 location = entity.getMemoryStorage().get(type).getVector3();
            if(location == null) return false;
            double distance = entity.position.distance(location);
            return distance <= maxDistance && distance >= minDistance;
        }
    }
}
