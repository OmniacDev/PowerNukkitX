package cn.nukkit.entity.projectile.abstract_arrow;

import cn.nukkit.entity.Entity;
import cn.nukkit.entity.projectile.SlenderProjectile;
import cn.nukkit.level.format.IChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import org.jetbrains.annotations.NotNull;

public abstract class EntityAbstractArrow extends SlenderProjectile {
    public final static String TAG_IS_CREATIVE = "isCreative";
    public final static String TAG_OWNER_ID = "OwnerID";
    public final static String TAG_PLAYER = "player";

    @NotNull public Boolean isCreative = false;
    @NotNull public Long OwnerID = -1L;
    @NotNull public Boolean player = false;

    public EntityAbstractArrow(IChunk chunk, CompoundTag nbt) {
        this(chunk, nbt, null);
    }

    public EntityAbstractArrow(IChunk chunk, CompoundTag nbt, Entity shootingEntity) {
        super(chunk, nbt, shootingEntity);

        this.isCreative = nbt.getBoolean(TAG_IS_CREATIVE);
        this.OwnerID = nbt.getLong(TAG_OWNER_ID);
        this.player = nbt.getBoolean(TAG_PLAYER);
    }
}
