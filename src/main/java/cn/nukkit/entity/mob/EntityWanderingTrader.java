package cn.nukkit.entity.mob;

import cn.nukkit.entity.EntityCreature;
import cn.nukkit.level.format.IChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import org.jetbrains.annotations.NotNull;

public class EntityWanderingTrader extends EntityMob {
    @Override
    @NotNull public String getIdentifier() {
        return WANDERING_TRADER;
    }

    public EntityWanderingTrader(IChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public float getWidth() {
        return 0.6f;
    }

    @Override
    public float getHeight() {
        return 1.9f;
    }

    @Override
    public String getOriginalName() {
        return "Wandering Trader";
    }

    @Override
    public void initEntity() {
        this.setMaxHealth(20);
        super.initEntity();
    }

    @Override
    public Integer getExperienceDrops() {
        return 0;
    }
}
