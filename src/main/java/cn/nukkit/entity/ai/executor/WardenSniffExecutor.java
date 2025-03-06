package cn.nukkit.entity.ai.executor;

import cn.nukkit.entity.mob.EntityMob;
import cn.nukkit.entity.data.EntityFlag;
import cn.nukkit.entity.mob.monster.EntityWarden;
import cn.nukkit.level.Sound;


public class WardenSniffExecutor implements IBehaviorExecutor {
    protected int angerAddition;
    protected int duration;//gt
    protected int endTime;

    public WardenSniffExecutor(int duration, int angerAddition) {
        this.duration = duration;
        this.angerAddition = angerAddition;
    }

    @Override
    public boolean execute(EntityMob entity) {
        if (entity.getLevel().getTick() >= this.endTime) {
            sniff(entity);
            return false;
        } else {
            return true;
        }
    }

    @Override
    public void onStart(EntityMob entity) {
        this.endTime = entity.getLevel().getTick() + this.duration;
        entity.setDataFlag(EntityFlag.SNIFFING, true);
        entity.setDataFlagExtend(EntityFlag.SNIFFING, true);
        entity.level.addSound(entity.position.clone(), Sound.MOB_WARDEN_SNIFF);
    }

    @Override
    public void onStop(EntityMob entity) {
        entity.setDataFlag(EntityFlag.SNIFFING, false);
        entity.setDataFlagExtend(EntityFlag.SNIFFING, false);
    }

    @Override
    public void onInterrupt(EntityMob entity) {
        entity.setDataFlag(EntityFlag.SNIFFING, false);
        entity.setDataFlagExtend(EntityFlag.SNIFFING, false);
    }

    protected void sniff(EntityMob entity) {
        if (!(entity instanceof EntityWarden warden)) return;
        for (var other : entity.level.getEntities()) {
            if (!warden.isValidAngerEntity(other, true)) continue;
            warden.addEntityAngerValue(other, this.angerAddition);
        }
    }
}
