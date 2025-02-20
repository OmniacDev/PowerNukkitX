package cn.nukkit.entity.humanoid_monster;

import cn.nukkit.Player;
import cn.nukkit.block.BlockID;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityCanAttack;
import cn.nukkit.entity.EntityIntelligent;
import cn.nukkit.entity.ai.memory.CoreMemoryTypes;
import cn.nukkit.entity.monster.EntityCreeper;
import cn.nukkit.entity.monster.EntityMonster;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.inventory.EntityArmorInventory;
import cn.nukkit.inventory.EntityEquipmentInventory;
import cn.nukkit.inventory.EntityInventoryHolder;
import cn.nukkit.inventory.Inventory;
import cn.nukkit.item.Item;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.level.Sound;
import cn.nukkit.level.format.IChunk;
import cn.nukkit.math.NukkitMath;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.utils.Utils;
import lombok.Getter;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author MagicDroidX (Nukkit Project)
 */

public abstract class EntityHumanoidMonster extends EntityMonster implements EntityInventoryHolder, EntityCanAttack {
    private static final String TAG_ITEM_IN_HAND = "ItemInHand";

    protected Item entityItemInHand;

    public EntityHumanoidMonster(IChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    protected void initEntity() {
        super.initEntity();

        if (this.namedTag.contains(TAG_ITEM_IN_HAND)) {
            entityItemInHand = NBTIO.getItemHelper(this.namedTag.getCompound(TAG_ITEM_IN_HAND));
        }
    }

    @Override
    public void saveNBT() {
        super.saveNBT();

        if (entityItemInHand != null) {
            this.namedTag.putCompound(TAG_ITEM_IN_HAND, NBTIO.putItemHelper(entityItemInHand));
        }
    }
}
