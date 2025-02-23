package cn.nukkit.entity.mob.monster.humanoid_monster;

import cn.nukkit.entity.EntityCanAttack;
import cn.nukkit.entity.mob.monster.EntityMonster;
import cn.nukkit.inventory.EntityInventoryHolder;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.IChunk;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import org.jetbrains.annotations.Nullable;

/**
 * @author MagicDroidX (Nukkit Project)
 */

public abstract class EntityHumanoidMonster extends EntityMonster implements EntityCanAttack {
    private static final String TAG_ITEM_IN_HAND = "ItemInHand";

    @Nullable protected Item itemInHand;

    public EntityHumanoidMonster(IChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    protected void initEntity() {
        super.initEntity();

        if (this.namedTag.contains(TAG_ITEM_IN_HAND)) {
            itemInHand = NBTIO.getItemHelper(this.namedTag.getCompound(TAG_ITEM_IN_HAND));
        }
    }

    @Override
    public void saveNBT() {
        super.saveNBT();

        if (itemInHand != null) {
            this.namedTag.putCompound(TAG_ITEM_IN_HAND, NBTIO.putItemHelper(itemInHand));
        }
    }
}
