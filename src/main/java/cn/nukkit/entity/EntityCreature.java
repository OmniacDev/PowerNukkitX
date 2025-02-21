package cn.nukkit.entity;

import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.IChunk;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import org.jetbrains.annotations.NotNull;

/**
 * 实体生物
 *
 * @author MagicDroidX (Nukkit Project)
 */

public abstract class EntityCreature extends EntityLiving implements EntityNameable, EntityAgeable {
    public EntityCreature(IChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public boolean onInteract(Player player, Item item, Vector3 clickedPos) {
        return EntityNameable.super.onInteract(player, item, clickedPos);
    }
}
