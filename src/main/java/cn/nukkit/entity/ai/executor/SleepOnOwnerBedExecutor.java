package cn.nukkit.entity.ai.executor;

import cn.nukkit.Player;
import cn.nukkit.entity.mob.EntityMob;
import cn.nukkit.entity.EntityOwnable;
import cn.nukkit.entity.data.EntityFlag;
import cn.nukkit.entity.mob.EntityMob;

/**
 * 使有主人的生物在主人睡觉时睡到主人床上<br/>
 * 只能在实现了接口 {@link EntityOwnable} 的实体上使用<br/>
 * 需要保证实体的getOwner()方法返回非空
 */
public class SleepOnOwnerBedExecutor implements IBehaviorExecutor {
    @Override
    public boolean execute(EntityMob entity) {
        Player owner = ((EntityOwnable) entity).getOwner();
        if (entity.pos.distanceSquared(owner.pos) <= 4) {
            setSleeping(entity, true);
        }
        return owner.isSleeping();
    }

    @Override
    public void onStart(EntityMob entity) {
        Player owner = ((EntityOwnable) entity).getOwner();
        entity.setMoveTarget(owner.pos);
        entity.setLookTarget(owner.pos);
    }

    @Override
    public void onInterrupt(EntityMob entity) {
        stop(entity);
    }

    @Override
    public void onStop(EntityMob entity) {
        stop(entity);
    }

    protected void stop(EntityMob entity) {
        setSleeping(entity, false);
    }

    protected void setSleeping(EntityMob entity, boolean sleeping) {
        entity.setDataFlag(EntityFlag.RESTING, sleeping);
        entity.setDataFlagExtend(EntityFlag.RESTING, sleeping);
    }
}
