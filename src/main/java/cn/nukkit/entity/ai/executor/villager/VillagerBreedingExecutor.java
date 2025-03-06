package cn.nukkit.entity.ai.executor.villager;

import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockBed;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.mob.EntityMob;
import cn.nukkit.entity.ai.executor.EntityBreedingExecutor;
import cn.nukkit.entity.ai.memory.CoreMemoryTypes;
import cn.nukkit.entity.mob.villagers.EntityVillagerV2;
import cn.nukkit.level.Transform;
import cn.nukkit.network.protocol.EntityEventPacket;

import java.util.Arrays;

public class VillagerBreedingExecutor extends EntityBreedingExecutor {

    public VillagerBreedingExecutor(Class entityClass, int findingRangeSquared, int duration, float moveSpeed) {
        super(entityClass, findingRangeSquared, duration, moveSpeed);
    }

    @Override
    public void onStart(EntityMob entity) {
        super.onStart(entity);
        finded = true;
        another = (EntityMob) entity.getMemoryStorage().get(CoreMemoryTypes.ENTITY_SPOUSE);
    }

    @Override
    public void onStop(EntityMob entity) {
        clearData(entity);
        if(another != null) {
            clearData(another);
        }
    }

    @Override
    public boolean execute(EntityMob uncasted) {
        if(another == null) return false;
        return super.execute(uncasted);
    }

    @Override
    protected void bear(EntityMob entity) {

        int range = 48;
        int lookY = 5;
        BlockBed block = null;
        for (int x = -range; x <= range; x++) {
            for (int z = -range; z <= range; z++) {
                for (int y = -lookY; y <= lookY; y++) {
                    Transform lookTransform = entity.getTransform().add(x, y, z);
                    Block lookBlock = lookTransform.getLevelBlock();
                    if (lookBlock instanceof BlockBed bed) {
                        if (!bed.isHeadPiece() && Arrays.stream(entity.level.getEntities()).noneMatch(entity1 -> entity1 instanceof EntityVillagerV2 v && v.getMemoryStorage().notEmpty(CoreMemoryTypes.OCCUPIED_BED) && v.getBed().equals(bed))) {
                            block = bed.getFootPart();
                        }
                    }
                }
            }
        }
        if(block == null) {
            sendAngryParticles(entity);
            sendAngryParticles(another);
            return;
        } else {
            sendInLoveParticles(entity);
            sendInLoveParticles(another);
        }

        EntityVillagerV2 baby = (EntityVillagerV2) Entity.createEntity(entity.getNetworkId(), entity.getLocator());
        baby.setBaby(true);
        //防止小屁孩去生baby
        baby.getMemoryStorage().put(CoreMemoryTypes.LAST_IN_LOVE_TIME, entity.level.getTick());
        baby.getMemoryStorage().put(CoreMemoryTypes.PARENT, entity);
        baby.spawnToAll();
    }

    @Override
    protected void clearData(EntityMob entity) {
        entity.getMemoryStorage().clear(CoreMemoryTypes.ENTITY_SPOUSE);
        //clear move target
        entity.setMoveTarget(null);
        //clear look target
        entity.setLookTarget(null);
        //reset move speed
        entity.setMovementSpeed(0.1f);
        //interrupt in love status
        entity.getMemoryStorage().put(CoreMemoryTypes.WILLING, false);
        entity.getMemoryStorage().put(CoreMemoryTypes.LAST_IN_LOVE_TIME, entity.level.getTick());
    }

    protected void sendInLoveParticles(EntityMob entity) {
        EntityEventPacket pk = new EntityEventPacket();
        pk.eid = entity.getId();
        pk.event = EntityEventPacket.LOVE_PARTICLES;
        Server.broadcastPacket(entity.getViewers().values(), pk);
    }

    protected void sendAngryParticles(EntityMob entity) {
        EntityEventPacket pk = new EntityEventPacket();
        pk.eid = entity.getId();
        pk.event = EntityEventPacket.VILLAGER_ANGRY;
        Server.broadcastPacket(entity.getViewers().values(), pk);
    }
}
