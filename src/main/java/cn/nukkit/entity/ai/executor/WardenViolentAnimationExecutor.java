package cn.nukkit.entity.ai.executor;

import cn.nukkit.entity.ai.memory.CoreMemoryTypes;
import cn.nukkit.entity.data.EntityFlag;
import cn.nukkit.entity.mob.EntityMob;


public class WardenViolentAnimationExecutor implements IBehaviorExecutor {

    protected int duration;
    protected int currentTick;

    public WardenViolentAnimationExecutor(int duration) {
        this.duration = duration;
    }

    @Override
    public boolean execute(EntityMob entity) {
        currentTick++;
        if (currentTick > duration) return false;
        else {
            //更新视线target
            if (entity.getMemoryStorage().notEmpty(CoreMemoryTypes.ATTACK_TARGET))
                entity.setLookTarget(entity.getMemoryStorage().get(CoreMemoryTypes.ATTACK_TARGET).pos);
            return true;
        }
    }

    @Override
    public void onInterrupt(EntityMob entity) {
        this.currentTick = 0;
        entity.setDataFlag(EntityFlag.ROARING, false);
        entity.setDataFlagExtend(EntityFlag.ROARING, false);
    }

    @Override
    public void onStart(EntityMob entity) {
        entity.getMemoryStorage().put(CoreMemoryTypes.IS_ATTACK_TARGET_CHANGED, false);
        entity.setMoveTarget(null);

        entity.setDataFlag(EntityFlag.ROARING, true);
        entity.setDataFlagExtend(EntityFlag.ROARING, true);
    }

    @Override
    public void onStop(EntityMob entity) {
        this.currentTick = 0;
        entity.setDataFlag(EntityFlag.ROARING, false);
        entity.setDataFlagExtend(EntityFlag.ROARING, false);
    }
}
