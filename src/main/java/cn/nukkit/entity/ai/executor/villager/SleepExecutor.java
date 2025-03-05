package cn.nukkit.entity.ai.executor.villager;

import cn.nukkit.block.BlockBed;
import cn.nukkit.entity.ai.executor.EntityControl;
import cn.nukkit.entity.ai.executor.IBehaviorExecutor;
import cn.nukkit.entity.ai.memory.CoreMemoryTypes;
import cn.nukkit.entity.data.EntityDataTypes;
import cn.nukkit.entity.data.EntityFlag;
import cn.nukkit.entity.mob.EntityMob;
import cn.nukkit.entity.mob.villagers.EntityVillagerV2;
import cn.nukkit.level.Transform;
import cn.nukkit.math.BVector3;
import cn.nukkit.math.BlockVector3;
import cn.nukkit.math.Vector3;


public class SleepExecutor implements EntityControl, IBehaviorExecutor {


    public SleepExecutor() {}
    @Override
    public boolean execute(EntityMob entity) {
        return true;
    }

    @Override
    public void onStart(EntityMob entity) {
        removeRouteTarget(entity);
        removeLookTarget(entity);
        if(entity.getMemoryStorage().notEmpty(CoreMemoryTypes.OCCUPIED_BED)) {
            if(entity.getMemoryStorage().get(CoreMemoryTypes.OCCUPIED_BED) instanceof BlockBed bed) {

                BlockBed head = bed.getHeadPart();
                BlockBed foot = bed.getFootPart();

                Transform sleepingTransform = new Transform(foot.getLocator()).add(switch (head.getBlockFace()) {
                    case NORTH -> new Vector3(0.5f, 0.5625f, 0);
                    case SOUTH -> new Vector3(0.5f, 0.5625f, 1);
                    case WEST -> new Vector3(0, 0.5625f, 0.5);
                    case EAST -> new Vector3(1, 0.5625f, 0.5);
                    default -> Vector3.ZERO;
                });
                sleepingTransform.setYaw(BVector3.getYawFromVector(head.getBlockFace().getOpposite().getUnitVector()));
                sleepingTransform.setHeadYaw(sleepingTransform.getYaw());
                entity.teleport(sleepingTransform);
                entity.respawnToAll();
                entity.setDataFlag(EntityFlag.SLEEPING);
                entity.setDataProperty(EntityDataTypes.BED_POSITION, head.position.asBlockVector3());
                entity.setDataFlag(EntityFlag.BODY_ROTATION_BLOCKED);
            }
        }
    }

    @Override
    public void onStop(EntityMob entity) {
        entity.setDataFlag(EntityFlag.SLEEPING, false);
        entity.setDataFlag(EntityFlag.BODY_ROTATION_BLOCKED, false);
        entity.setDataProperty(EntityDataTypes.BED_POSITION, new BlockVector3(0, 0 ,0));
        if(!entity.getLevel().isNight()) {
            if(entity instanceof EntityVillagerV2 villager) {
                villager.heal(villager.getMaxHealth());
            }
        }
    }

    @Override
    public void onInterrupt(EntityMob entity) {
        onStop(entity);
    }
}
