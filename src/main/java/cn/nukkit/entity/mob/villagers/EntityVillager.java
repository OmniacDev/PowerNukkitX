package cn.nukkit.entity.mob.villagers;

import cn.nukkit.entity.mob.EntityMob;
import cn.nukkit.entity.mob.monster.humanoid_monster.EntityDrowned;
import cn.nukkit.entity.mob.monster.humanoid_monster.EntityZombie;
import cn.nukkit.entity.mob.monster.humanoid_monster.EntityZombieVillager;
import cn.nukkit.entity.projectile.abstract_arrow.EntityThrownTrident;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.level.format.IChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import org.jetbrains.annotations.NotNull;

/**
 * @author Pub4Game
 * @since 21.06.2016
 */

public class EntityVillager extends EntityMob {
    @Override
    @NotNull public String getIdentifier() {
        return VILLAGER;
    }
    public static final int PROFESSION_FARMER = 0;
    public static final int PROFESSION_LIBRARIAN = 1;
    public static final int PROFESSION_PRIEST = 2;
    public static final int PROFESSION_BLACKSMITH = 3;
    public static final int PROFESSION_BUTCHER = 4;
    public static final int PROFESSION_GENERIC = 5;
    

    public EntityVillager(IChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public float getWidth() {
        if (this.isBaby()) {
            return 0.3f;
        }
        return 0.6f;
    }

    @Override
    public float getHeight() {
        if (this.isBaby()) {
            return 0.95f;
        }
        return 1.9f;
    }

    @Override
    public String getOriginalName() {
        return "Villager";
    }

    @Override
    public void initEntity() {
        this.setMaxHealth(20);
        super.initEntity();

        if (!this.namedTag.contains("Profession")) {
            this.setProfession(PROFESSION_GENERIC);
        }
    }

    public int getProfession() {
        return this.namedTag.getInt("Profession");
    }

    public void setProfession(int profession) {
        this.namedTag.putInt("Profession", profession);
    }

    @Override
    public boolean attack(EntityDamageEvent source) {
        if(getHealth()-source.getFinalDamage() <= 1) {
            if(source instanceof EntityDamageByEntityEvent entityEvent) {
                if(entityEvent.getDamager() instanceof EntityThrownTrident trident) {
                    if(trident.shootingEntity instanceof EntityDrowned) {
                        transform();
                        return true;
                    }
                } else if(entityEvent.getDamager() instanceof EntityZombie) {
                    transform();
                    return true;
                }
            }
        }
        return super.attack(source);
    }

    private void transform() {
        this.close();
        EntityZombieVillager zombieVillager = new EntityZombieVillager(this.getLocator().getChunk(), this.namedTag);
        zombieVillager.setPosition(this.pos);
        zombieVillager.setRotation(this.rotation.yaw, this.rotation.pitch);
        zombieVillager.spawnToAll();
    }

    @Override
    public Integer getExperienceDrops() {
        return 0;
    }
}
