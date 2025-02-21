package cn.nukkit.entity.mob.water_animal;

import cn.nukkit.entity.mob.EntityMob;
import cn.nukkit.level.format.IChunk;
import cn.nukkit.nbt.tag.CompoundTag;

public abstract class EntityWaterAnimal extends EntityMob {
    public EntityWaterAnimal(IChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }
}
