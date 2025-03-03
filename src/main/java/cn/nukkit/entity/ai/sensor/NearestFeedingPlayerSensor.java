package cn.nukkit.entity.ai.sensor;

import cn.nukkit.Player;
import cn.nukkit.entity.EntityIntelligent;
import cn.nukkit.entity.ai.memory.CoreMemoryTypes;
import cn.nukkit.entity.mob.animal.EntityAnimal;


public class NearestFeedingPlayerSensor implements ISensor {

    protected double range;

    protected double minRange;

    protected int period;

    public NearestFeedingPlayerSensor(double range, double minRange) {
        this(range, minRange, 1);
    }

    public NearestFeedingPlayerSensor(double range, double minRange, int period) {
        this.range = range;
        this.minRange = minRange;
        this.period = period;
    }

    @Override
    public void sense(EntityIntelligent entity) {
        if (entity instanceof EntityAnimal entityAnimal) {
            Player player = null;
            double rangeSquared = this.range * this.range;
            double minRangeSquared = this.minRange * this.minRange;
            //寻找范围内最近满足乞食要求的玩家
            for (Player p : entity.getLevel().getPlayers().values()) {
                if (entity.pos.distanceSquared(p.pos) <= rangeSquared && entity.pos.distanceSquared(p.pos) >= minRangeSquared && entityAnimal.isBreedingItem(p.getInventory().getItemInHand())) {
                    if (player == null) {
                        player = p;
                    } else {
                        if (entity.pos.distanceSquared(p.pos) < entity.pos.distanceSquared(player.pos)) {
                            player = p;
                        }
                    }
                }
            }
            entity.getMemoryStorage().put(CoreMemoryTypes.NEAREST_FEEDING_PLAYER, player);
            return;
        }
        entity.getMemoryStorage().clear(CoreMemoryTypes.NEAREST_FEEDING_PLAYER);
    }

    @Override
    public int getPeriod() {
        return period;
    }
}
