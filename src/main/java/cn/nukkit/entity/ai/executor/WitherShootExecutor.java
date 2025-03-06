package cn.nukkit.entity.ai.executor;

import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityID;
import cn.nukkit.entity.ai.memory.CoreMemoryTypes;
import cn.nukkit.entity.ai.memory.MemoryType;
import cn.nukkit.entity.mob.EntityMob;
import cn.nukkit.entity.projectile.EntityProjectile;
import cn.nukkit.entity.projectile.EntitySmallFireball;
import cn.nukkit.event.entity.ProjectileLaunchEvent;
import cn.nukkit.level.Transform;
import cn.nukkit.level.Sound;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.FloatTag;
import cn.nukkit.nbt.tag.ListTag;

public class WitherShootExecutor implements EntityControl, IBehaviorExecutor {

    protected int tick = 0;
    protected MemoryType<? extends Entity> targetMemory;

    public WitherShootExecutor(MemoryType<? extends Entity> targetMemory) {
        this.targetMemory = targetMemory;
    }

    @Override
    public boolean execute(EntityMob entity) {
        Entity target = entity.getMemoryStorage().get(targetMemory);
        if(target == null) return false;
        tick++;
        if (tick <= 40) {
            if (tick % 10 == 0) {
                spawn(entity, tick == 40);
            }
            setRouteTarget(entity, entity.position);
            setLookTarget(entity, target.position);
            return true;
        } else {
            entity.getMemoryStorage().put(CoreMemoryTypes.LAST_ATTACK_TIME, entity.level.getTick());
            return false;
        }
    }


    @Override
    public void onStart(EntityMob entity) {
        removeRouteTarget(entity);
        tick = 0;
    }

    protected void spawn(EntityMob entity, boolean charged) {
        Transform fireballTransform = entity.getTransform();
        fireballTransform.add(entity.getDirectionVector());
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

        Entity projectile = Entity.createEntity(charged ? EntityID.WITHER_SKULL_DANGEROUS : EntityID.WITHER_SKULL, entity.level.getChunk(entity.position.getChunkX(), entity.position.getChunkZ()), nbt);
        if (projectile == null) {
            return;
        }
        if(projectile instanceof EntitySmallFireball fireball) {
            fireball.shootingEntity = entity;
        }

        ProjectileLaunchEvent projectev = new ProjectileLaunchEvent((EntityProjectile) projectile, entity);
        Server.getInstance().getPluginManager().callEvent(projectev);
        if (projectev.isCancelled()) {
            projectile.kill();
        } else {
            projectile.spawnToAll();
            entity.level.addSound(entity.position, Sound.MOB_WITHER_SHOOT);
        }
    }
}
