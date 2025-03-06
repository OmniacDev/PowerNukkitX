package cn.nukkit.entity.ai.executor.enderdragon;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityID;
import cn.nukkit.entity.ai.executor.EntityControl;
import cn.nukkit.entity.ai.executor.IBehaviorExecutor;
import cn.nukkit.entity.ai.memory.CoreMemoryTypes;
import cn.nukkit.entity.mob.EntityMob;
import cn.nukkit.level.Transform;
import cn.nukkit.math.BVector3;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.FloatTag;
import cn.nukkit.nbt.tag.ListTag;


public class StrafeExecutor implements EntityControl, IBehaviorExecutor {

    private boolean fired = false;

    public StrafeExecutor() {}
    @Override
    public boolean execute(EntityMob entity) {

        if(fired) return false;

        Player player = entity.getMemoryStorage().get(CoreMemoryTypes.NEAREST_PLAYER);
        if(player == null) return false;
        setLookTarget(entity, player.position);
        setRouteTarget(entity, player.position);

        if(entity.position.distance(player.position) <= 64) {

            Vector3 toPlayerVector = new Vector3(player.position.x - entity.position.x, player.position.y - entity.position.y, player.position.z - entity.position.z).normalize();

            Transform fireballTransform = entity.getTransform().add(toPlayerVector.multiply(5));
            double yaw = BVector3.getYawFromVector(toPlayerVector);
            double pitch = BVector3.getPitchFromVector(toPlayerVector);
            CompoundTag nbt = new CompoundTag()
                    .putList("Pos", new ListTag<FloatTag>()
                            .add(new FloatTag(fireballTransform.position.x))
                            .add(new FloatTag(fireballTransform.position.y))
                            .add(new FloatTag(fireballTransform.position.z)))
                    .putList("Motion", new ListTag<FloatTag>()
                            .add(new FloatTag(-Math.sin(yaw / 180 * Math.PI) * Math.cos(pitch / 180 * Math.PI)))
                            .add(new FloatTag(-Math.sin(pitch / 180 * Math.PI)))
                            .add(new FloatTag(Math.cos(yaw / 180 * Math.PI) * Math.cos(pitch / 180 * Math.PI))))
                    .putList("Rotation", new ListTag<FloatTag>()
                            .add(new FloatTag(0))
                            .add(new FloatTag(0)));

            Entity projectile = Entity.createEntity(EntityID.DRAGON_FIREBALL, entity.level.getChunk(entity.position.getChunkX(), entity.position.getChunkZ()), nbt);
            projectile.spawnToAll();
            this.fired = true;
            return false;
        }
        return true;
    }


    @Override
    public void onStart(EntityMob entity) {
        Player player = entity.getMemoryStorage().get(CoreMemoryTypes.NEAREST_PLAYER);
        if(player == null) return;
        setLookTarget(entity, player.position);
        setRouteTarget(entity, player.position);
        this.fired = false;
    }

    @Override
    public void onStop(EntityMob entity) {
        entity.setEnablePitch(false);
        entity.getMemoryStorage().clear(CoreMemoryTypes.LAST_ENDER_CRYSTAL_DESTROY);
    }

    @Override
    public void onInterrupt(EntityMob entity) {
        onStop(entity);
    }

}
