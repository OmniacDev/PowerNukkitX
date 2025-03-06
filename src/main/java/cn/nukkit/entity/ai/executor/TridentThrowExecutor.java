package cn.nukkit.entity.ai.executor;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityID;
import cn.nukkit.entity.EntityLiving;
import cn.nukkit.entity.ai.memory.MemoryType;
import cn.nukkit.entity.data.EntityDataTypes;
import cn.nukkit.entity.data.EntityFlag;
import cn.nukkit.entity.mob.EntityMob;
import cn.nukkit.entity.projectile.EntityProjectile;
import cn.nukkit.entity.projectile.abstract_arrow.EntityThrownTrident;
import cn.nukkit.event.entity.ProjectileLaunchEvent;
import cn.nukkit.level.Transform;
import cn.nukkit.level.Sound;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.FloatTag;
import cn.nukkit.nbt.tag.ListTag;

import java.util.concurrent.ThreadLocalRandom;

public class TridentThrowExecutor implements EntityControl, IBehaviorExecutor {
    protected MemoryType<? extends Entity> memory;
    protected float speed;
    protected int maxShootDistanceSquared;
    protected boolean clearDataWhenLose;
    protected final int coolDownTick;
    protected final int pullTridentTick;
    /**
     * 用来指定特定的攻击目标.
     * <p>
     * Used to specify a specific attack target.
     **/
    protected Entity target;
    /**
     * 用来射击的物品
     */
    private int tick1;//control the coolDownTick
    private int tick2;//control the pullBowTick

    /**
     * 射击执行器
     *
     * @param memory            用于读取攻击目标的记忆<br>Used to read the memory of the attack target
     * @param speed             移动向攻击目标的速度<br>The speed of movement towards the attacking target
     * @param maxShootDistance  允许射击的最大距离，只有在这个距离内才能射击<br>The maximum distance at which it is permissible to shoot, and only at this distance can be fired
     * @param clearDataWhenLose 失去目标时清空记忆<br>Clear your memory when you lose your target
     * @param coolDownTick      攻击冷却时间(单位tick)<br>Attack cooldown (tick)
     * @param pullTridentTick       每次攻击动画用时(单位tick)<br>Attack Animation time(tick)
     */
    public TridentThrowExecutor(MemoryType<? extends Entity> memory, float speed, int maxShootDistance, boolean clearDataWhenLose, int coolDownTick, int pullTridentTick) {
        this.memory = memory;
        this.speed = speed;
        this.maxShootDistanceSquared = maxShootDistance * maxShootDistance;
        this.clearDataWhenLose = clearDataWhenLose;
        this.coolDownTick = coolDownTick;
        this.pullTridentTick = pullTridentTick;
    }

    @Override
    public boolean execute(EntityMob entity) {
        if (tick2 == 0) {
            tick1++;
        }
        if (!entity.isEnablePitch()) entity.setEnablePitch(true);
        if (entity.getBehaviorGroup().getMemoryStorage().isEmpty(memory)) return false;
        Entity newTarget = entity.getBehaviorGroup().getMemoryStorage().get(memory);
        if (this.target == null) target = newTarget;
        //some check
        if (!target.isAlive()) return false;
        else if (target instanceof Player player) {
            if (player.isCreative() || player.isSpectator() || !player.isOnline() || !entity.level.getName().equals(player.level.getName())) {
                return false;
            }
        }

        if (!this.target.getLocator().equals(newTarget.getLocator())) {
            //更新目标
            target = newTarget;
        }

        if (entity.getMovementSpeed() != speed) entity.setMovementSpeed(speed);
        Transform clone = this.target.getTransform();

        if (entity.position.distanceSquared(target.position) > maxShootDistanceSquared) {
            //更新寻路target
            setRouteTarget(entity, clone.position);
        } else {
            setRouteTarget(entity, null);
        }
        //更新视线target
        setLookTarget(entity, clone.position);

        if (tick2 == 0 && tick1 > coolDownTick) {
            if (entity.position.distanceSquared(target.position) <= maxShootDistanceSquared) {
                this.tick1 = 0;
                this.tick2++;
                playTridentAnimation(entity);
            }
        } else if (tick2 != 0) {
            tick2++;
            if (tick2 > pullTridentTick) {
                throwTrident(entity);
                stopTridentAnimation(entity);
                tick2 = 0;
                return target.getHealth() != 0;
            }
        }
        return true;
    }

    @Override
    public void onStop(EntityMob entity) {
        removeRouteTarget(entity);
        removeLookTarget(entity);
        //重置速度
        entity.setMovementSpeed(EntityLiving.DEFAULT_SPEED);
        if (clearDataWhenLose) {
            entity.getBehaviorGroup().getMemoryStorage().clear(memory);
        }
        entity.setEnablePitch(false);
        stopTridentAnimation(entity);
        this.target = null;
    }

    @Override
    public void onInterrupt(EntityMob entity) {
        removeRouteTarget(entity);
        removeLookTarget(entity);
        //重置速度
        entity.setMovementSpeed(EntityLiving.DEFAULT_SPEED);
        if (clearDataWhenLose) {
            entity.getBehaviorGroup().getMemoryStorage().clear(memory);
        }
        entity.setEnablePitch(false);
        stopTridentAnimation(entity);
        this.target = null;
    }


    protected void throwTrident(EntityMob entity) {

        Transform fireballTransform = entity.getTransform();
        Vector3 directionVector = entity.getDirectionVector().multiply(1 + ThreadLocalRandom.current().nextFloat(0.2f));
        fireballTransform.setY(entity.position.y + entity.getEyeHeight() + directionVector.getY());
        CompoundTag nbt = new CompoundTag()
                .putList("Pos", new ListTag<FloatTag>()
                        .add(new FloatTag(fireballTransform.position.x))
                        .add(new FloatTag(fireballTransform.position.y))
                        .add(new FloatTag(fireballTransform.position.z)))
                .putList("Motion", new ListTag<FloatTag>()
                        .add(new FloatTag(-Math.sin(entity.headYaw / 180 * Math.PI) * Math.cos(entity.rotation.pitch / 180 * Math.PI)))
                        .add(new FloatTag(-Math.sin(entity.rotation.pitch / 180 * Math.PI)))
                        .add(new FloatTag(Math.cos(entity.headYaw / 180 * Math.PI) * Math.cos(entity.rotation.pitch / 180 * Math.PI))))
                .putList("Rotation", new ListTag<FloatTag>()
                        .add(new FloatTag((float) (entity.headYaw > 180 ? 360 : 0) - entity.headYaw))
                        .add(new FloatTag((float) -entity.rotation.pitch)))
                .putDouble("damage", 2);

        double p = 1;
        double f = Math.min((p * p + p * 2) / 3, 1) * 3;

        Entity projectile = Entity.createEntity(EntityID.THROWN_TRIDENT, entity.level.getChunk(entity.position.getChunkX(), entity.position.getChunkZ()), nbt);

        if (projectile == null) {
            return;
        }
        if(projectile instanceof EntityThrownTrident trident) {
            trident.shootingEntity = entity;
            trident.setPickupMode(EntityProjectile.PICKUP_CREATIVE);
        }

        ProjectileLaunchEvent projectev = new ProjectileLaunchEvent((EntityProjectile) projectile, entity);
        Server.getInstance().getPluginManager().callEvent(projectev);
        if (projectev.isCancelled()) {
            projectile.kill();
        } else {
            entity.level.addSound(entity.position, Sound.MOB_BREEZE_SHOOT);
            projectile.spawnToAll();
        }
    }

    private void playTridentAnimation(Entity entity) {
        entity.setDataProperty(EntityDataTypes.TARGET_EID, this.target.getId());
        entity.setDataFlag(EntityFlag.FACING_TARGET_TO_RANGE_ATTACK);
    }

    private void stopTridentAnimation(Entity entity) {
        entity.setDataProperty(EntityDataTypes.TARGET_EID, 0L);
        entity.setDataFlag(EntityFlag.FACING_TARGET_TO_RANGE_ATTACK, false);
    }
}
