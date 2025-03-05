package cn.nukkit.entity.ai.executor;

import cn.nukkit.entity.ai.memory.MemoryType;
import cn.nukkit.entity.mob.EntityMob;
import cn.nukkit.math.IVector3;


public class LookAtTargetExecutor implements EntityControl, IBehaviorExecutor {

    //指示执行器应该从哪个Memory获取目标位置
    protected MemoryType<? extends IVector3> memory;
    protected int duration;
    protected int currentTick;

    public LookAtTargetExecutor(MemoryType<? extends IVector3> memory, int duration) {
        this.memory = memory;
        this.duration = duration;
    }

    @Override
    public boolean execute(EntityMob entity) {
        currentTick++;
        if (!entity.isEnablePitch()) entity.setEnablePitch(true);
        var vector3Memory = entity.getMemoryStorage().get(memory);
        if (vector3Memory != null) {
            setLookTarget(entity, vector3Memory.getVector3());
        }
        return currentTick <= duration;
    }

    @Override
    public void onInterrupt(EntityMob entity) {
        currentTick = 0;
        entity.setEnablePitch(false);
    }

    @Override
    public void onStop(EntityMob entity) {
        currentTick = 0;
        entity.setEnablePitch(false);
    }
}
