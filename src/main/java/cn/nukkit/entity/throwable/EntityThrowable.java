package cn.nukkit.entity.throwable;

import cn.nukkit.entity.Entity;
import cn.nukkit.entity.projectile.EntityProjectile;
import cn.nukkit.level.format.IChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import org.jetbrains.annotations.NotNull;

public abstract class EntityThrowable extends EntityProjectile {
    public final static String TAG_IN_GROUND = "InGround";
    public final static String TAG_OWNER_ID = "OwnerID";
    public final static String TAG_SHAKE = "shake";

    @NotNull public Boolean inGround = false;
    @NotNull public Long ownerId = -1L;
    @NotNull public Boolean shake = false;

    public EntityThrowable(IChunk chunk, CompoundTag nbt) {
        this(chunk, nbt, null);
    }

    public EntityThrowable(IChunk chunk, CompoundTag nbt, Entity shootingEntity) {
        super(chunk, nbt, shootingEntity);

        this.inGround = nbt.getBoolean(TAG_IN_GROUND);
        this.ownerId = nbt.getLong(TAG_OWNER_ID);
        this.shake = nbt.getBoolean(TAG_SHAKE);
    }
}
