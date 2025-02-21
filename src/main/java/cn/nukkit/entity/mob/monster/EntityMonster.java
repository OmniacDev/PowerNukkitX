package cn.nukkit.entity.mob.monster;

import cn.nukkit.entity.EntityCanAttack;
import cn.nukkit.entity.mob.EntityMob;
import cn.nukkit.inventory.EntityInventoryHolder;
import cn.nukkit.item.Item;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.level.format.IChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import org.jetbrains.annotations.NotNull;

/**
 * @author MagicDroidX (Nukkit Project)
 */

public abstract class EntityMonster extends EntityMob implements EntityInventoryHolder, EntityCanAttack {
    private static final String TAG_SPAWNED_BY_NIGHT = "SpawnedByNight";

    @NotNull protected Boolean spawnedByNight = true;

    public EntityMonster(IChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    protected void initEntity() {
        super.initEntity();

        spawnedByNight = this.namedTag.getBoolean(TAG_SPAWNED_BY_NIGHT);
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

        this.namedTag.putBoolean(TAG_SPAWNED_BY_NIGHT, spawnedByNight);
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
