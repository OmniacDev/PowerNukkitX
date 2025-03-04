package cn.nukkit.entity.ai.executor.evocation;

import cn.nukkit.entity.mob.EntityMob;
import cn.nukkit.entity.ai.memory.CoreMemoryTypes;
import cn.nukkit.entity.mob.EntityMob;
import cn.nukkit.entity.mob.monster.humanoid_monster.EntityEvocationIllager;
import cn.nukkit.level.Location;

import static cn.nukkit.entity.ai.memory.CoreMemoryTypes.LAST_MAGIC;

public class FangCircleExecutor extends FangLineExecutor {

    //Values represent ticks
    private final static int CAST_DURATION = 40;
    private final static int DELAY_BETWEEN = 3;
    private final static int SPAWN_COUNT_INNER = 5;
    private final static int SPAWN_COUNT_OUTER = 8;
    private final static float SPAWN_RADIUS_INNER = 1.5f;
    private final static float SPAWN_RADIUS_OUTER = 2.5f;


    private final static int DURATION = CAST_DURATION + DELAY_BETWEEN + 1;


    public FangCircleExecutor() {}
    @Override
    public boolean execute(EntityMob entity) {
        EntityEvocationIllager illager = (EntityEvocationIllager) entity;
        if(tick == CAST_DURATION) {
            entity.rotation.yaw = (entity.headYaw);
            summon(illager, SPAWN_RADIUS_INNER, SPAWN_COUNT_INNER);
        } else if(tick == CAST_DURATION + DELAY_BETWEEN) {
            summon(illager, SPAWN_RADIUS_OUTER, SPAWN_COUNT_OUTER);
        }
        tick++;
        if(tick >= DURATION) {
            int tick = entity.getLevel().getTick();
            entity.getMemoryStorage().put(CoreMemoryTypes.LAST_ATTACK_CAST, tick);
            entity.getMemoryStorage().put(CoreMemoryTypes.LAST_ATTACK_TIME, tick);
            return false;
        } else return true;
    }

    private void summon(EntityEvocationIllager origin, float size, int amount) {
        double angleIncrement = 360.0 / amount;
        for (int i = 0; i < amount; i++) {
            double angle = Math.toRadians((i * angleIncrement) + origin.headYaw);
            double particleX = origin.getX() + Math.cos(angle) * size;
            double particleZ = origin.getZ() + Math.sin(angle) * size;
            spawn(origin, new Location(particleX, origin.pos.y, particleZ, angle, 0, origin.level));
        }
    }

    @Override
    protected void startSpell(EntityMob entity) {
        super.startSpell(entity);
        entity.getMemoryStorage().put(LAST_MAGIC, EntityEvocationIllager.SPELL.CAST_CIRLCE);
    }
}
