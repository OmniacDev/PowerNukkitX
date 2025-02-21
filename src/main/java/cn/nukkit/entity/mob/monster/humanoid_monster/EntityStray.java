package cn.nukkit.entity.mob.monster.humanoid_monster;

import cn.nukkit.entity.EntitySmite;
import cn.nukkit.entity.EntityWalkable;
import cn.nukkit.level.format.IChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import org.jetbrains.annotations.NotNull;

/**
 * @author PikyCZ
 */
public class EntityStray extends EntitySkeleton implements EntityWalkable, EntitySmite {

    @Override
    @NotNull public String getIdentifier() {
        return STRAY;
    }

    public EntityStray(IChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    protected void initEntity() {
        this.setMaxHealth(20);
        super.initEntity();
    }

    @Override
    public String getOriginalName() {
        return "Stray";
    }

    @Override
    public boolean onUpdate(int currentTick) {
        burn(this);
        return super.onUpdate(currentTick);
    }
}
