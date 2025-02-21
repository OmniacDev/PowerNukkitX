package cn.nukkit.entity.mob;

import cn.nukkit.entity.EntitySwimmable;
import cn.nukkit.level.format.IChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import org.jetbrains.annotations.NotNull;

public class EntityGlowSquid extends EntitySquid implements EntitySwimmable {

    @Override
    @NotNull public String getIdentifier() {
        return GLOW_SQUID;
    }

    public EntityGlowSquid(IChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public float getWidth() {
        return 0.475f;
    }

    @Override
    public void initEntity() {
        this.setMaxHealth(10);
        super.initEntity();
    }

    @Override
    public String getOriginalName() {
        return "GlowSquid";
    }
}
