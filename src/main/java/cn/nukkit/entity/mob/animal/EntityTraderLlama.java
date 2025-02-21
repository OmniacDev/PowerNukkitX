package cn.nukkit.entity.mob.animal;

import cn.nukkit.level.format.IChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import org.jetbrains.annotations.NotNull;

public class EntityTraderLlama extends EntityLlama {
    @Override
    @NotNull public String getIdentifier() {
        return TRADER_LLAMA;
    }

    public EntityTraderLlama(IChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public String getOriginalName() {
        return "Trader Llama";
    }

    @Override
    public void initEntity() {
        this.setMaxHealth(20);
        super.initEntity();
    }
}
