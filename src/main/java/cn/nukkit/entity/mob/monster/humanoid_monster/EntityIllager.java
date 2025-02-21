package cn.nukkit.entity.mob.monster.humanoid_monster;

import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityWalkable;
import cn.nukkit.entity.mob.villagers.EntityVillager;
import cn.nukkit.level.format.IChunk;
import cn.nukkit.nbt.tag.CompoundTag;

/**
 * @author PikyCZ
 */
public abstract class EntityIllager extends EntityHumanoidMonster implements EntityWalkable {
    public EntityIllager(IChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public boolean attackTarget(Entity entity) {
        return switch (entity.getIdentifier()) {
            case VILLAGER ->
                    entity instanceof EntityVillager villager && !villager.isBaby();
            case IRON_GOLEM, WANDERING_TRADER -> true;
            default -> super.attackTarget(entity);
        };
    }
}
