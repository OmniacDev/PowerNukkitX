package cn.nukkit.entity.ai.executor.enderdragon;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.ai.executor.EntityControl;
import cn.nukkit.entity.ai.executor.IBehaviorExecutor;
import cn.nukkit.entity.ai.memory.CoreMemoryTypes;
import cn.nukkit.entity.effect.Effect;
import cn.nukkit.entity.effect.PotionType;
import cn.nukkit.entity.item.EntityAreaEffectCloud;
import cn.nukkit.entity.mob.EntityMob;
import cn.nukkit.level.Transform;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.FloatTag;
import cn.nukkit.nbt.tag.ListTag;

import java.util.List;


public class PerchingExecutor implements EntityControl, IBehaviorExecutor {

    private int stayTick = -1;

    public PerchingExecutor() {}
    @Override
    public boolean execute(EntityMob entity) {
        Vector3 target = new Vector3(0, entity.getLevel().getHighestBlockAt(0, 0) + 1, 0);
        if(stayTick >= 0) {
            stayTick++;
        }
        if(entity.pos.distance(target) <= 1) {
            if(stayTick == -1) stayTick=0;
            if(stayTick == 25) {
                entity.getViewers().values().stream().filter(player -> player.pos.distance(new Vector3(0, 64, 0)) <= 20).findAny().ifPresent(player -> {
                    removeRouteTarget(entity);
                    setLookTarget(entity, player.pos);
                    Vector3 toPlayerVector = new Vector3(player.pos.x - entity.pos.x, player.pos.y - entity.pos.y, player.pos.z - entity.pos.z).normalize();
                    Transform transform = entity.getLocation().add(toPlayerVector.multiply(10));
                    transform.y = transform.level.getHighestBlockAt(transform.toHorizontal()) + 1;
                    EntityAreaEffectCloud areaEffectCloud = (EntityAreaEffectCloud) Entity.createEntity(Entity.AREA_EFFECT_CLOUD, transform.getChunk(),
                            new CompoundTag().putList("Pos", new ListTag<>()
                                            .add(new FloatTag(transform.x))
                                            .add(new FloatTag(transform.y))
                                            .add(new FloatTag(transform.z))
                                    )
                                    .putList("Rotation", new ListTag<>()
                                            .add(new FloatTag(0))
                                            .add(new FloatTag(0))
                                    )
                                    .putList("Motion", new ListTag<>()
                                            .add(new FloatTag(0))
                                            .add(new FloatTag(0))
                                            .add(new FloatTag(0))
                                    )
                                    .putInt("Duration", 60)
                                    .putFloat("InitialRadius", 6)
                                    .putFloat("Radius", 6)
                                    .putFloat("Height", 1)
                                    .putFloat("RadiusChangeOnPickup", 0)
                                    .putFloat("RadiusPerTick", 0)
                    );

                    List<Effect> effects = PotionType.get(PotionType.HARMING.id()).getEffects(false);
                    for (Effect effect : effects) {
                        if (effect != null && areaEffectCloud != null) {
                            areaEffectCloud.cloudEffects.add(effect.setVisible(false).setAmbient(false));
                            areaEffectCloud.spawnToAll();
                        }
                    }
                    areaEffectCloud.spawnToAll();
                });
            }
        } else {
            setRouteTarget(entity, target);
            setLookTarget(entity, target);
        }
        if(stayTick > 100) {
            return false;
        } else if(stayTick >= 0) {
            entity.teleport(target);
        }
        return true;
    }



    @Override
    public void onStart(EntityMob entity) {
        Player player = entity.getMemoryStorage().get(CoreMemoryTypes.NEAREST_PLAYER);
        if(player == null) return;
        setLookTarget(entity, player.pos);
        setRouteTarget(entity, player.pos);
        stayTick = -1;
    }

    @Override
    public void onStop(EntityMob entity) {
        entity.getMemoryStorage().put(CoreMemoryTypes.FORCE_PERCHING, false);
        entity.setEnablePitch(false);
    }

    @Override
    public void onInterrupt(EntityMob entity) {
        onStop(entity);
    }

}
