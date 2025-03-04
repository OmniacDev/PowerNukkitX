package cn.nukkit.entity.ai.executor.villager;

import cn.nukkit.entity.ai.executor.EntityControl;
import cn.nukkit.entity.ai.executor.IBehaviorExecutor;
import cn.nukkit.entity.ai.memory.CoreMemoryTypes;
import cn.nukkit.entity.mob.EntityMob;
import cn.nukkit.entity.mob.villagers.EntityVillagerV2;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemFood;


public class WillingnessExecutor implements EntityControl, IBehaviorExecutor {


    public WillingnessExecutor() {}
    @Override
    public boolean execute(EntityMob entity) {
        return false;
    }

    @Override
    public void onStart(EntityMob entity) {
        if(entity instanceof EntityVillagerV2 villager) {
            for(int j = 0; j < villager.getInventory().getSize(); j++) {
                Item item = villager.getInventory().getItem(j);
                if(item instanceof ItemFood) {
                    villager.getInventory().clear(j);
                }
            }
        }
        entity.getMemoryStorage().put(CoreMemoryTypes.WILLING, true);
    }
}
