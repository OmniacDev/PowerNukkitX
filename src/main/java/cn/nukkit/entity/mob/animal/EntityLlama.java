package cn.nukkit.entity.mob.animal;

import cn.nukkit.level.format.IChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import org.jetbrains.annotations.NotNull;

public class EntityLlama extends EntityAnimal {
    @Override
    @NotNull public String getIdentifier() {
        return LLAMA;
    }

    public EntityLlama(IChunk chunk, CompoundTag nbt) {
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
        return "Llama";
    }

    @Override
    public void initEntity() {
        this.setMaxHealth(20);
        super.initEntity();
    }
}
