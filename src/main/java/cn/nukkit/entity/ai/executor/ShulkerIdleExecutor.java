package cn.nukkit.entity.ai.executor;

import cn.nukkit.entity.mob.EntityMob;
import cn.nukkit.entity.mob.EntityShulker;
import cn.nukkit.level.Sound;
import cn.nukkit.utils.Utils;


public class ShulkerIdleExecutor implements IBehaviorExecutor {

    private int stayTicks = 0;
    private int tick = 0;

    public ShulkerIdleExecutor() {}
    @Override
    public boolean execute(EntityMob entity) {
        tick++;
        if(tick >= stayTicks) return false;
        return true;
    }

    @Override
    public void onStart(EntityMob entity) {
        tick = 0;
        stayTicks = Utils.rand(20, 61);
        if(entity instanceof EntityShulker shulker) {
            shulker.setPeeking(30);
            shulker.getLevel().addSound(shulker.pos, Sound.MOB_SHULKER_OPEN);
        }
    }

    @Override
    public void onStop(EntityMob entity) {
        if(entity instanceof EntityShulker shulker) {
            shulker.setPeeking(0);
            shulker.getLevel().addSound(shulker.pos, Sound.MOB_SHULKER_CLOSE);
        }
    }

    @Override
    public void onInterrupt(EntityMob entity) {
        onStop(entity);
    }
}
