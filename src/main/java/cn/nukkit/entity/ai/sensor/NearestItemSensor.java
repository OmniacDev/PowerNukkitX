package cn.nukkit.entity.ai.sensor;

import cn.nukkit.entity.Entity;
import cn.nukkit.entity.ai.memory.CoreMemoryTypes;
import cn.nukkit.entity.item.EntityItem;
import cn.nukkit.entity.mob.EntityMob;
import cn.nukkit.item.Item;
import lombok.Getter;

//存储最近的玩家的Memory


@Getter
public class NearestItemSensor implements ISensor {

    protected double range;

    protected double minRange;

    protected int period;

    public NearestItemSensor(double range, double minRange) {
        this(range, minRange, 1);
    }

    public NearestItemSensor(double range, double minRange, int period) {
        this.range = range;
        this.minRange = minRange;
        this.period = period;
    }

    @Override
    public void sense(EntityMob entity) {

        Class<? extends Item> itemClass = entity.getMemoryStorage().get(CoreMemoryTypes.LOOKING_ITEM);
        if(itemClass == null) return;

        EntityItem item = null;
        double rangeSquared = this.range * this.range;
        double minRangeSquared = this.minRange * this.minRange;
        //寻找范围内最近的玩家
        for (Entity e : entity.getLevel().getEntities()) {
            if(e instanceof EntityItem entityItem) {
                if(itemClass.isAssignableFrom(entityItem.getItem().getClass())) {
                    if (entity.pos.distanceSquared(e.pos) <= rangeSquared && entity.pos.distanceSquared(e.pos) >= minRangeSquared) {
                        if (item == null) {
                            item = entityItem;
                        } else {
                            if (entity.pos.distanceSquared(entityItem.pos) < entity.pos.distanceSquared(item.pos)) {
                                 item = entityItem;
                            }
                        }
                    }
                }
            }
        }
        entity.getMemoryStorage().put(CoreMemoryTypes.NEAREST_ITEM, item);
    }

    @Override
    public int getPeriod() {
        return period;
    }
}
