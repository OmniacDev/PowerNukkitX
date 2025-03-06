package cn.nukkit.entity.ai.executor.armadillo;

import cn.nukkit.entity.ai.executor.EntityControl;
import cn.nukkit.entity.ai.executor.IBehaviorExecutor;
import cn.nukkit.entity.ai.memory.CoreMemoryTypes;
import cn.nukkit.entity.mob.EntityMob;
import cn.nukkit.item.Item;
import cn.nukkit.level.Sound;
import cn.nukkit.utils.Utils;

public class ShedExecutor implements EntityControl, IBehaviorExecutor {

    public ShedExecutor() {}
    @Override
    public boolean execute(EntityMob entity) {
        return false;
    }

    @Override
    public void onStart(EntityMob entity) {
        entity.level.dropItem(entity.position, Item.get(Item.ARMADILLO_SCUTE));
        entity.level.addSound(entity.position, Sound.MOB_ARMADILLO_SCUTE_DROP);
        entity.getMemoryStorage().put(CoreMemoryTypes.NEXT_SHED, entity.level.getTick() + Utils.rand(6_000, 10_800));
    }
}
