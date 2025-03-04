package cn.nukkit.entity.ai.executor;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityID;
import cn.nukkit.entity.EntityLiving;
import cn.nukkit.entity.ai.memory.MemoryType;
import cn.nukkit.entity.mob.EntityMob;
import cn.nukkit.entity.projectile.EntityProjectile;
import cn.nukkit.entity.projectile.throwable.EntitySnowball;
import cn.nukkit.event.entity.ProjectileLaunchEvent;
import cn.nukkit.level.Location;
import cn.nukkit.level.Sound;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.FloatTag;
import cn.nukkit.nbt.tag.ListTag;

import java.util.concurrent.ThreadLocalRandom;

public class SnowGolemShootExecutor implements EntityControl, IBehaviorExecutor {
    protected MemoryType<? extends Entity> memory;
    protected float speed;
    protected int maxShootDistanceSquared;
    protected boolean clearDataWhenLose;
    protected final int coolDownTick;
    protected final int fireTick;
    /**
     * <p>
     * Used to specify a specific attack target.
     **/
    protected Entity target;

    private int tick1;//control the coolDownTick
    private int tick2;//control the pullBowTick

    /**
     *
     * @param memory            <br>Used to read the memory of the attack target
     * @param speed             <br>The speed of movement towards the attacking target
     * @param maxShootDistance  <br>The maximum distance at which it is permissible to shoot, and only at this distance can be fired
     * @param clearDataWhenLose <br>Clear your memory when you lose your target
     * @param coolDownTick      <br>Attack cooldown (tick)
     * @param fireTick          <br>Attack Animation time(tick)
     */
    public SnowGolemShootExecutor(MemoryType<? extends Entity> memory, float speed, int maxShootDistance, boolean clearDataWhenLose, int coolDownTick, int fireTick) {
        this.memory = memory;
        this.speed = speed;
        this.maxShootDistanceSquared = maxShootDistance * maxShootDistance;
        this.clearDataWhenLose = clearDataWhenLose;
        this.coolDownTick = coolDownTick;
        this.fireTick = fireTick;
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

        if (!target.isAlive()) return false;
        else if (target instanceof Player player) {
            if (player.isCreative() || player.isSpectator() || !player.isOnline() || !entity.level.getName().equals(player.level.getName())) {
                return false;
            }
        }

        if (!this.target.getPosition().equals(newTarget.getPosition())) {
            target = newTarget;
        }

        if (entity.getMovementSpeed() != speed) entity.setMovementSpeed(speed);
        Location clone = this.target.getLocation();

        if (entity.pos.distanceSquared(target.pos) > maxShootDistanceSquared) {
            setRouteTarget(entity, clone);
        } else {
            setRouteTarget(entity, null);
        }
        setLookTarget(entity, clone);

        if (tick2 == 0 && tick1 > coolDownTick) {
            if (entity.pos.distanceSquared(target.pos) <= maxShootDistanceSquared) {
                this.tick1 = 0;
                this.tick2++;
            }
        } else if (tick2 != 0) {
            tick2++;
            if (tick2 > fireTick) {
                shootSnowball(entity);
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
        entity.setMovementSpeed(EntityLiving.DEFAULT_SPEED);
        if (clearDataWhenLose) {
            entity.getBehaviorGroup().getMemoryStorage().clear(memory);
        }
        entity.setEnablePitch(false);
        this.target = null;
    }

    @Override
    public void onInterrupt(EntityMob entity) {
        removeRouteTarget(entity);
        removeLookTarget(entity);
        entity.setMovementSpeed(EntityLiving.DEFAULT_SPEED);
        if (clearDataWhenLose) {
            entity.getBehaviorGroup().getMemoryStorage().clear(memory);
        }
        entity.setEnablePitch(false);
        this.target = null;
    }

    protected void shootSnowball(EntityMob entity) {

        Location fireballLocation = entity.getLocation();
        Vector3 directionVector = entity.getDirectionVector().multiply(1 + ThreadLocalRandom.current().nextFloat(0.2f));
        fireballLocation.setY(entity.pos.y + entity.getEyeHeight() + directionVector.getY());
        CompoundTag nbt = new CompoundTag()
                .putList("Pos", new ListTag<FloatTag>()
                        .add(new FloatTag(fireballLocation.x))
                        .add(new FloatTag(fireballLocation.y))
                        .add(new FloatTag(fireballLocation.z)))
                .putList("Motion", new ListTag<FloatTag>()
                        .add(new FloatTag(-Math.sin(entity.headYaw / 180 * Math.PI) * Math.cos(entity.rotation.pitch / 180 * Math.PI)))
                        .add(new FloatTag(-Math.sin(entity.rotation.pitch / 180 * Math.PI)))
                        .add(new FloatTag(Math.cos(entity.headYaw / 180 * Math.PI) * Math.cos(entity.rotation.pitch / 180 * Math.PI))))
                .putList("Rotation", new ListTag<FloatTag>()
                        .add(new FloatTag((float) (entity.headYaw > 180 ? 360 : 0) - entity.headYaw))
                        .add(new FloatTag((float) -entity.rotation.pitch)));

        Entity projectile = Entity.createEntity(EntityID.SNOWBALL, entity.level.getChunk(entity.pos.getChunkX(), entity.pos.getChunkZ()), nbt);

        if (projectile == null) {
            return;
        }
        if(projectile instanceof EntitySnowball fireball) {
            fireball.shootingEntity = entity;
        }

        ProjectileLaunchEvent projectev = new ProjectileLaunchEvent((EntityProjectile) projectile, entity);
        Server.getInstance().getPluginManager().callEvent(projectev);
        if (projectev.isCancelled()) {
            projectile.kill();
        } else {
            projectile.spawnToAll();
            entity.level.addSound(entity.pos, Sound.MOB_SNOWGOLEM_SHOOT);
        }
    }

}
