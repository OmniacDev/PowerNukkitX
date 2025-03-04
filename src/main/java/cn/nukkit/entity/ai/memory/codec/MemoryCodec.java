package cn.nukkit.entity.ai.memory.codec;

import cn.nukkit.entity.mob.EntityMob;
import cn.nukkit.entity.mob.EntityMob;
import cn.nukkit.nbt.tag.CompoundTag;
import lombok.Getter;

import javax.annotation.Nullable;
import java.util.function.BiConsumer;
import java.util.function.Function;


@Getter
public class MemoryCodec<Data> implements IMemoryCodec<Data> {
    private final Function<CompoundTag, Data> decoder;
    private final BiConsumer<Data, CompoundTag> encoder;
    @Nullable
    private BiConsumer<Data, EntityMob> onInit = null;

    public MemoryCodec(
            Function<CompoundTag, Data> decoder,
            BiConsumer<Data, CompoundTag> encoder
    ) {
        this.decoder = decoder;
        this.encoder = encoder;
    }

    /**
     * BiConsumer<Data, EntityMob> Data can be Null
     */
    public MemoryCodec<Data> onInit(BiConsumer<Data, EntityMob> onInit) {
        this.onInit = onInit;
        return this;
    }

    @Override
    public void init(@Nullable Data data, EntityMob entity) {
        if (onInit != null) {
            onInit.accept(data, entity);
        }
    }
}
