package cn.nukkit.entity.ai.executor.enderdragon;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityID;
import cn.nukkit.entity.ai.executor.EntityControl;
import cn.nukkit.entity.ai.executor.IBehaviorExecutor;
import cn.nukkit.entity.ai.memory.CoreMemoryTypes;
import cn.nukkit.entity.mob.EntityMob;
import cn.nukkit.level.Location;
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
        setLookTarget(entity, player.pos);
        setRouteTarget(entity, player.pos);

        if(entity.pos.distance(player.pos) <= 64) {

            Vector3 toPlayerVector = new Vector3(player.pos.x - entity.pos.x, player.pos.y - entity.pos.y, player.pos.z - entity.pos.z).normalize();

            Location fireballLocation = entity.getLocation().add(toPlayerVector.multiply(5));
            double yaw = BVector3.getYawFromVector(toPlayerVector);
            double pitch = BVector3.getPitchFromVector(toPlayerVector);
            CompoundTag nbt = new CompoundTag()
                    .putList("Pos", new ListTag<FloatTag>()
                            .add(new FloatTag(fireballLocation.x))
                            .add(new FloatTag(fireballLocation.y))
                            .add(new FloatTag(fireballLocation.z)))
                    .putList("Motion", new ListTag<FloatTag>()
                            .add(new FloatTag(-Math.sin(yaw / 180 * Math.PI) * Math.cos(pitch / 180 * Math.PI)))
                            .add(new FloatTag(-Math.sin(pitch / 180 * Math.PI)))
                            .add(new FloatTag(Math.cos(yaw / 180 * Math.PI) * Math.cos(pitch / 180 * Math.PI))))
                    .putList("Rotation", new ListTag<FloatTag>()
                            .add(new FloatTag(0))
                            .add(new FloatTag(0)));

            Entity projectile = Entity.createEntity(EntityID.DRAGON_FIREBALL, entity.level.getChunk(entity.pos.getChunkX(), entity.pos.getChunkZ()), nbt);
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
        setLookTarget(entity, player.pos);
        setRouteTarget(entity, player.pos);
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
