package cn.nukkit.entity.ai.executor;

import cn.nukkit.entity.mob.EntityMob;
import cn.nukkit.entity.EntityLiving;
import cn.nukkit.entity.ai.memory.CoreMemoryTypes;
import cn.nukkit.entity.ai.memory.MemoryType;
import cn.nukkit.math.IVector3;
import cn.nukkit.math.Vector3;
import lombok.Getter;


@Getter
public class FleeFromTargetExecutor implements EntityControl, IBehaviorExecutor {

    //指示执行器应该从哪个Memory获取目标位置
    protected MemoryType<? extends IVector3> memory;
    protected float speed;
    protected Vector3 oldTarget;
    protected boolean updateRouteImmediatelyWhenTargetChange;
    protected boolean enableRangeTest = false;
    protected float minDistance;
    protected boolean clearDataWhenLose;

    public FleeFromTargetExecutor(MemoryType<? extends IVector3> memory, float speed) {
        this(memory, speed, false);
    }

    public FleeFromTargetExecutor(MemoryType<? extends IVector3> memory, float speed, boolean updateRouteImmediatelyWhenTargetChange) {
        this(memory, speed, updateRouteImmediatelyWhenTargetChange, -1);
    }

    public FleeFromTargetExecutor(MemoryType<? extends IVector3> memory, float speed, boolean updateRouteImmediatelyWhenTargetChange, float minDistance) {
        this(memory, speed, updateRouteImmediatelyWhenTargetChange, minDistance, false);
    }

    public FleeFromTargetExecutor(MemoryType<? extends IVector3> memory, float speed, boolean updateRouteImmediatelyWhenTargetChange, float minDistance, boolean clearDataWhenLose) {
        this.memory = memory;
        this.speed = speed;
        this.updateRouteImmediatelyWhenTargetChange = updateRouteImmediatelyWhenTargetChange;
        this.minDistance = minDistance;
        this.clearDataWhenLose = clearDataWhenLose;
    }

    @Override
    public boolean execute(EntityMob entity) {
        if (!entity.isEnablePitch()) entity.setEnablePitch(true);
        if (entity.getBehaviorGroup().getMemoryStorage().isEmpty(memory)) {
            return false;
        }

        Vector3 target = entity.getBehaviorGroup().getMemoryStorage().get(memory).getVector3();
        Vector3 moveTarget = target.add(new Vector3(entity.pos.x-target.x, entity.pos.y-target.y, entity.pos.z-target.z).normalize().multiply(minDistance));

        if (target.distance(entity.pos) > minDistance) {
            setLookTarget(entity, target);
            return false;
        }

        setRouteTarget(entity, moveTarget);
        setLookTarget(entity, moveTarget);

        //This gives the Evoker enough time to turn around before attacking.
        entity.getMemoryStorage().put(CoreMemoryTypes.LAST_ATTACK_TIME, entity.getLevel().getTick());

        if (updateRouteImmediatelyWhenTargetChange) {
            var floor = target.floor();

            if (oldTarget == null || oldTarget.equals(floor))
                entity.getBehaviorGroup().setForceUpdateRoute(true);

            oldTarget = floor;
        }

        if (entity.getMovementSpeed() != speed)
            entity.setMovementSpeed(speed);

        return true;
    }

    @Override
    public void onInterrupt(EntityMob entity) {
        removeRouteTarget(entity);
        removeLookTarget(entity);
        entity.setMovementSpeed(EntityMob.DEFAULT_SPEED);
        entity.setEnablePitch(false);
        if (clearDataWhenLose)
            entity.getBehaviorGroup().getMemoryStorage().clear(memory);
    }

    @Override
    public void onStop(EntityMob entity) {
        removeRouteTarget(entity);
        removeLookTarget(entity);
        entity.setMovementSpeed(EntityLiving.DEFAULT_SPEED);
        entity.setEnablePitch(false);
        if (clearDataWhenLose)
            entity.getBehaviorGroup().getMemoryStorage().clear(memory);
    }
}
