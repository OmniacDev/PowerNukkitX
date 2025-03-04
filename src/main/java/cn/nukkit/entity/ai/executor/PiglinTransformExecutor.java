package cn.nukkit.entity.ai.executor;

import cn.nukkit.entity.mob.EntityMob;
import cn.nukkit.entity.data.EntityFlag;
import cn.nukkit.entity.effect.Effect;
import cn.nukkit.entity.effect.EffectType;
import cn.nukkit.entity.mob.EntityMob;
import cn.nukkit.entity.mob.monster.humanoid_monster.EntityZombiePigman;
import cn.nukkit.inventory.Inventory;
import cn.nukkit.level.Sound;

public class PiglinTransformExecutor implements EntityControl, IBehaviorExecutor {

    protected int tick = 0;

    public PiglinTransformExecutor() {}

    @Override
    public boolean execute(EntityMob entity) {
        tick++;
        if(tick >= 300) {
            transform(entity);
            return false;
        }
        return true;
    }


    @Override
    public void onStart(EntityMob entity) {
        tick = -1;
        entity.setDataFlag(EntityFlag.SHAKING);
    }

    @Override
    public void onStop(EntityMob entity) {
        entity.setDataFlag(EntityFlag.SHAKING, false);
    }

    @Override
    public void onInterrupt(EntityMob entity) {
        onStop(entity);
    }

    private void transform(EntityMob entity) {
        entity.saveNBT();
        entity.close();
        EntityZombiePigman entityZombiePigman = new EntityZombiePigman(entity.getPosition().getChunk(), entity.namedTag);
        entityZombiePigman.setPosition(entity.pos);
        entityZombiePigman.setRotation(entity.rotation.yaw, entity.rotation.pitch);
        entityZombiePigman.spawnToAll();
        entityZombiePigman.level.addSound(entityZombiePigman.pos, Sound.MOB_PIGLIN_CONVERTED_TO_ZOMBIFIED);
        Inventory inventory = entityZombiePigman.getEquipment();
        for(int i = 2; i < inventory.getSize(); i++) {
            entityZombiePigman.level.dropItem(entityZombiePigman.pos, inventory.getItem(i));
            inventory.clear(i);
        }
        // TODO
        entityZombiePigman.addEffect(Effect.get(EffectType.NAUSEA).setDuration(15));
    }

}


