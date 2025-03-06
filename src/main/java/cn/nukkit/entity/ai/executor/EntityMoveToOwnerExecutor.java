package cn.nukkit.entity.ai.executor;

import cn.nukkit.Player;
import cn.nukkit.block.BlockID;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityOwnable;
import cn.nukkit.entity.ai.memory.CoreMemoryTypes;
import cn.nukkit.entity.data.EntityFlag;
import cn.nukkit.entity.mob.EntityMob;
import cn.nukkit.math.Vector3;

import java.util.concurrent.ThreadLocalRandom;

/**
 * 实体移动到主人身边.(只对实现了接口 {@link cn.nukkit.entity.EntityOwnable EntityOwnable} 的实体有效)
 * <p>
 * The entity moves to the master's side.(Only valid for entities that implement the interface {@link cn.nukkit.entity.EntityOwnable EntityOwnable})
 */


public class EntityMoveToOwnerExecutor implements EntityControl, IBehaviorExecutor {
    protected float speed;
    protected int maxFollowRangeSquared;
    public int minFollowRangeSquared;
    protected Vector3 oldTarget;
    protected boolean updateRouteImmediatelyWhenTargetChange;

    public EntityMoveToOwnerExecutor(float speed, boolean updateRouteImmediatelyWhenTargetChange, int maxFollowRange) {
        this(speed, updateRouteImmediatelyWhenTargetChange, maxFollowRange, 9);
    }
    public EntityMoveToOwnerExecutor(float speed, boolean updateRouteImmediatelyWhenTargetChange, int maxFollowRange, int minFollowRange) {
        this.speed = speed;
        this.updateRouteImmediatelyWhenTargetChange = updateRouteImmediatelyWhenTargetChange;
        if (maxFollowRange >= 0) {
            this.maxFollowRangeSquared = maxFollowRange * maxFollowRange;
        }
        if (minFollowRange >= 0) {
            this.minFollowRangeSquared = minFollowRange * minFollowRange;
        }
    }

    @Override
    public boolean execute(EntityMob entity) {
        if (!entity.isEnablePitch()) entity.setEnablePitch(true);

        if (entity instanceof EntityOwnable entityOwnable) {
            Player player = entityOwnable.getOwner();
            if (player == null) return false;

            //获取目的地位置（这个clone很重要）
            Player target = (Player) player.clone();
            if (target.position.distanceSquared(entity.position) <= minFollowRangeSquared) return false;

            //不允许跨世界
            if (!target.level.getName().equals(entity.level.getName()))
                return false;

            if (entity.getLocator().position.floor().equals(oldTarget)) return false;

            var distanceSquared = entity.position.distanceSquared(player.position);
            if (distanceSquared <= maxFollowRangeSquared) {
                //更新寻路target
                setRouteTarget(entity, target.position);
                //更新视线target
                setLookTarget(entity, target.position);

                if (entity.getMemoryStorage().notEmpty(CoreMemoryTypes.NEAREST_FEEDING_PLAYER)) {
                    entity.setDataFlag(EntityFlag.INTERESTED, true);
                }

                if (updateRouteImmediatelyWhenTargetChange) {
                    var floor = target.position.floor();

                    if (oldTarget == null || oldTarget.equals(floor))
                        entity.getBehaviorGroup().setForceUpdateRoute(true);

                    oldTarget = floor;
                }

                if (entity.getMovementSpeed() != speed)
                    entity.setMovementSpeed(speed);

                return true;
            } else {
                var targetVector = randomVector3(player, 4);
                if (targetVector == null || targetVector.distanceSquared(player.position) > maxFollowRangeSquared)
                    return true;//继续寻找
                else return !entity.teleport(targetVector);
            }
        }
        return false;
    }

    @Override
    public void onInterrupt(EntityMob entity) {
        //目标丢失
        removeRouteTarget(entity);
        removeLookTarget(entity);
        //重置速度
        entity.setMovementSpeed(1.2f);
        entity.setEnablePitch(false);
        if (entity.getMemoryStorage().isEmpty(CoreMemoryTypes.NEAREST_FEEDING_PLAYER)) {
            entity.setDataFlag(EntityFlag.INTERESTED, false);
        }
        oldTarget = null;
    }

    @Override
    public void onStop(EntityMob entity) {
        //目标丢失
        removeRouteTarget(entity);
        removeLookTarget(entity);
        //重置速度
        entity.setMovementSpeed(1.2f);
        entity.setEnablePitch(false);
        if (entity.getMemoryStorage().isEmpty(CoreMemoryTypes.NEAREST_FEEDING_PLAYER)) {
            entity.setDataFlag(EntityFlag.INTERESTED, false);
        }
        oldTarget = null;
    }

    protected Vector3 randomVector3(Entity player, int r) {
        var random = ThreadLocalRandom.current();
        int x = random.nextInt(r * -1, r) + player.position.getFloorX();
        int z = random.nextInt(r * -1, r) + player.position.getFloorZ();
        double y = player.level.getHighestBlockAt(x, z);
        var vector3 = new Vector3(x, y, z);
        var result = player.level.getBlock(vector3);
        if (result.isSolid() && result.getId() != BlockID.AIR) return result.up().position;
        else return null;
    }
}
