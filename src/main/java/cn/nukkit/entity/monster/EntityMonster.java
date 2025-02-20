package cn.nukkit.entity.monster;

import cn.nukkit.Player;
import cn.nukkit.block.BlockID;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityCanAttack;
import cn.nukkit.entity.EntityIntelligent;
import cn.nukkit.entity.ai.memory.CoreMemoryTypes;
import cn.nukkit.entity.mob.EntityMob;
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
import lombok.NonNull;

import java.util.Collection;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author MagicDroidX (Nukkit Project)
 */

public abstract class EntityMonster extends EntityMob implements EntityInventoryHolder, EntityCanAttack {
    private static final String TAG_SPAWNED_BY_NIGHT = "SpawnedByNight";

    protected boolean entitySpawnedByNight;

    public EntityMonster(IChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    protected void initEntity() {
        super.initEntity();

        entitySpawnedByNight = this.namedTag.getBoolean(TAG_SPAWNED_BY_NIGHT);
    }

    @Override
    public boolean onUpdate(int currentTick) {
        if (this.getServer().getDifficulty() == 0) {
            this.close();
            return true;
        } else return super.onUpdate(currentTick);
    }

    @Override
    public void saveNBT() {
        super.saveNBT();

        this.namedTag.putBoolean(TAG_SPAWNED_BY_NIGHT, entitySpawnedByNight);
    }

    @Override
    public void setOnFire(int seconds) {
        int level = 0;

        for (Item armor : this.getArmorInventory().getContents().values()) {
            Enchantment fireProtection = armor.getEnchantment(Enchantment.ID_PROTECTION_FIRE);

            if (fireProtection != null && fireProtection.getLevel() > 0) {
                level = Math.max(level, fireProtection.getLevel());
            }
        }

        seconds = (int) (seconds * (1 - level * 0.15));

        super.setOnFire(seconds);
    }
}
