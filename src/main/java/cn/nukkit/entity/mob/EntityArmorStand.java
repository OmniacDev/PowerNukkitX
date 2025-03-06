package cn.nukkit.entity.mob;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockID;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityEquipment;
import cn.nukkit.entity.EntityInteractable;
import cn.nukkit.entity.EntityNameable;
import cn.nukkit.entity.effect.EffectType;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.player.PlayerChangeArmorStandEvent;
import cn.nukkit.inventory.BaseInventory;
import cn.nukkit.inventory.EntityArmorInventory;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemArmor;
import cn.nukkit.item.ItemID;
import cn.nukkit.item.ItemShield;
import cn.nukkit.level.Sound;
import cn.nukkit.level.format.IChunk;
import cn.nukkit.level.particle.DestroyBlockParticle;
import cn.nukkit.level.vibration.VibrationEvent;
import cn.nukkit.level.vibration.VibrationType;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.SetEntityDataPacket;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Objects;

public class EntityArmorStand extends EntityMob implements EntityInteractable, EntityNameable {
    @Override
    @NotNull
    public String getIdentifier() {
        return ARMOR_STAND;
    }

    private static final String TAG_POSE = "Pose";
    private static final String TAG_LAST_SIGNAL = "LastSignal";
    private static final String TAG_POSE_INDEX = "PoseIndex";

    public EntityArmorStand(IChunk chunk, CompoundTag nbt) { super(chunk, nbt); }

    private static int getArmorSlot(ItemArmor armorItem) {
        if (armorItem.isHelmet()) {
            return EntityArmorInventory.SLOT_HEAD;
        } else if (armorItem.isChestplate()) {
            return EntityArmorInventory.SLOT_CHEST;
        } else if (armorItem.isLeggings()) {
            return EntityArmorInventory.SLOT_LEGS;
        } else {
            return EntityArmorInventory.SLOT_FEET;
        }
    }

    @Override
    public float getHeight() {
        return 1.975f;
    }

    @Override
    public float getWidth() {
        return 0.5f;
    }

    @Override
    public float getGravity() {
        return 0.04f;
    }

    @Override
    protected void initEntity() {
        this.setHealth(6);
        this.setMaxHealth(6);
        this.setImmobile(true);

        super.initEntity();

        CompoundTag Pose = this.namedTag.getCompound(TAG_POSE);
        this.setPose(Pose.getInt(TAG_POSE_INDEX));
    }

    @Override
    public boolean isPersistent() {
        return true;
    }

    @Override
    public void setPersistent(boolean persistent) {
        // Armor stands are always persistent
    }

    @Override
    public Integer getExperienceDrops() { return 0; }

    @Override
    public Item[] getDrops() {
        return Item.EMPTY_ARRAY;
    }

    @Override
    public boolean canCollide() { return false; }

    @Override
    public boolean onInteract(Player player, Item item, Vector3 clickedPos) {
        if (player.isSpectator() || !isValid()) {
            return false;
        }

        // Name tag
        if (!item.isNull() && item.getId().equals(ItemID.NAME_TAG) && playerApplyNameTag(player, item, false)) {
            return true;
        }

        //Pose
        if (player.isSneaking() || player.isFlySneaking()) {
            if (this.getPose() >= 12) {
                this.setPose(0);
            } else {
                this.setPose(this.getPose() + 1);
            }
            return false; // Returning true would consume the item
        }

        //Inventory
        boolean isArmor;

        boolean hasItemInHand = !item.isNull();
        int slot;

        if (hasItemInHand && item instanceof ItemArmor itemArmor) {
            isArmor = true;
            slot = getArmorSlot(itemArmor);
        } else if (hasItemInHand && (Objects.equals(item.getId(), BlockID.SKULL)) || Objects.equals(item.getBlockId(), BlockID.CARVED_PUMPKIN)) {
            isArmor = true;
            slot = EntityEquipment.HEAD;
        } else if (hasItemInHand) {
            isArmor = false;
            if (item instanceof ItemShield) {
                slot = EntityEquipment.OFF_HAND;
            } else {
                slot = EntityEquipment.MAIN_HAND;
            }
        } else {
            double clickHeight = clickedPos.y - this.position.y;
            if (clickHeight >= 0.1 && clickHeight < 0.55 && !getBoots().isNull()) {
                isArmor = true;
                slot = EntityEquipment.FEET;
            } else if (clickHeight >= 0.9 && clickHeight < 1.6) {
                if (!getItemInOffhand().isNull()) {
                    isArmor = false;
                    slot = EntityEquipment.OFF_HAND;
                } else if (!getItemInHand().isNull()) {
                    isArmor = false;
                    slot = EntityEquipment.MAIN_HAND;
                } else if (!getChestplate().isNull()) {
                    isArmor = true;
                    slot = EntityEquipment.CHEST;
                } else {
                    return false;
                }
            } else if (clickHeight >= 0.4 && clickHeight < 1.2 && !getLeggings().isNull()) {
                isArmor = true;
                slot = EntityEquipment.LEGS;
            } else if (clickHeight >= 1.6 && !getHelmet().isNull()) {
                isArmor = true;
                slot = EntityEquipment.HEAD;
            } else if (!getItemInOffhand().isNull()) {
                isArmor = false;
                slot = EntityEquipment.OFF_HAND;
            } else if (!getItemInHand().isNull()) {
                isArmor = false;
                slot = EntityEquipment.MAIN_HAND;
            } else {
                return false;
            }
        }

        var ev = new PlayerChangeArmorStandEvent(player, this, item, slot);
        this.getServer().getPluginManager().callEvent(ev);
        if (ev.isCancelled()) return false;

        boolean changed = false;
        if (isArmor) {
            changed = this.tryChangeEquipment(player, ev.getItem(), slot);
            slot = EntityEquipment.MAIN_HAND;
        }

        if (!changed) {
            changed = this.tryChangeEquipment(player, ev.getItem(), slot);
        }

        if (changed) {
            level.addSound(this.position, Sound.MOB_ARMOR_STAND_PLACE);
        }

        return false; // Returning true would consume the item but tryChangeEquipment already manages the inventory
    }

    private boolean tryChangeEquipment(Player player, Item handItem, int slot) {
        BaseInventory inventory = getEquipment();
        Item item = inventory.getItem(slot);

        if (item.isNull() && !handItem.isNull()) {
            // Adding item to the armor stand
            Item itemClone = handItem.clone();
            itemClone.setCount(1);
            inventory.setItem(slot, itemClone);
            if (!player.isCreative()) {
                handItem.count--;
                player.getInventory().setItem(player.getInventory().getHeldItemIndex(), handItem);
            }
            return true;
        } else if (!item.isNull()) {
            Item itemtoAddToArmorStand = Item.AIR;
            if (!handItem.isNull()) {
                if (handItem.equals(item, true, true)) {
                    // Attempted to replace with the same item type
                    return false;
                }

                if (item.count > 1) {
                    // The armor stand have more items than 1, item swapping is not supported in this situation
                    return false;
                }

                Item itemToSetToPlayerInv;
                if (handItem.count > 1) {
                    itemtoAddToArmorStand = handItem.clone();
                    itemtoAddToArmorStand.setCount(1);

                    itemToSetToPlayerInv = handItem.clone();
                    itemToSetToPlayerInv.count--;
                } else {
                    itemtoAddToArmorStand = handItem.clone();
                    itemToSetToPlayerInv = Item.AIR;
                }
                player.getInventory().setItem(player.getInventory().getHeldItemIndex(), itemToSetToPlayerInv);
            }

            // Removing item from the armor stand
            Item[] notAdded = player.getInventory().addItem(item);
            if (notAdded.length > 0) {
                if (notAdded[0].count == item.count) {
                    if (!handItem.isNull()) {
                        player.getInventory().setItem(player.getInventory().getHeldItemIndex(), handItem);
                    }
                    return false;
                }

                Item itemClone = item.clone();
                itemClone.count -= notAdded[0].count;
                inventory.setItem(slot, itemClone);
            } else {
                inventory.setItem(slot, itemtoAddToArmorStand);
            }
            return true;
        }
        return false;
    }

    private int getPose() {
        return this.entityDataMap.get(Entity.ARMOR_STAND_POSE_INDEX);
    }

    private void setPose(int pose) {
        this.entityDataMap.put(Entity.ARMOR_STAND_POSE_INDEX, pose);
        SetEntityDataPacket setEntityDataPacket = new SetEntityDataPacket();
        setEntityDataPacket.eid = this.getId();
        setEntityDataPacket.entityData = this.getEntityDataMap();
        Server.getInstance().getOnlinePlayers().values().forEach(all -> all.dataPacket(setEntityDataPacket));
    }

    @Override
    public void saveNBT() {
        super.saveNBT();

        CompoundTag Pose = new CompoundTag();
        Pose.putInt(TAG_POSE_INDEX, this.getPose());
        Pose.putInt(TAG_LAST_SIGNAL, 0);

        this.namedTag.putCompound(TAG_POSE, Pose);
    }

    @Override
    public void fall(float fallDistance) {
        super.fall(fallDistance);

        this.level.addSound(this.position, Sound.MOB_ARMOR_STAND_LAND);
    }

    @Override
    public void kill() {
        this.health = 0;
        this.scheduleUpdate();

        for (Entity passenger : new ArrayList<>(this.passengers)) {
            dismountEntity(passenger);
        }

        EntityDamageEvent lastDamageCause = this.lastDamageCause;
        boolean byAttack = lastDamageCause != null && lastDamageCause.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK;

        Vector3 pos = getLocator().position;

        pos.y += 0.2;
        level.dropItem(pos, getBoots());

        pos.y = this.position.y + 0.6;
        level.dropItem(pos, getLeggings());

        pos.y = this.position.y + 1.4;
        level.dropItem(byAttack ? pos : this.position, Item.get(ItemID.ARMOR_STAND));
        level.dropItem(pos, getChestplate());
        level.dropItem(pos, getItemInHand());
        level.dropItem(pos, getItemInOffhand());

        pos.y = this.position.y + 1.8;
        level.dropItem(pos, getHelmet());
        getEquipment().clearAll();

        level.addSound(this.position, Sound.MOB_ARMOR_STAND_BREAK);

        //todo: initiator should be a entity who kill it but not itself
        level.getVibrationManager().callVibrationEvent(new VibrationEvent(this.getLastDamageCause() instanceof EntityDamageByEntityEvent byEntity ? byEntity.getDamager() : this, this.getVector3(), VibrationType.ENTITY_DIE));
    }

    @Override
    public boolean attack(EntityDamageEvent source) {

        switch (source.getCause()) {
            case FALL:
                source.setCancelled(true);
                level.addSound(this.position, Sound.MOB_ARMOR_STAND_LAND);
                break;
            case CONTACT:
            case HUNGER:
            case MAGIC:
            case DROWNING:
            case SUFFOCATION:
            case PROJECTILE:
                source.setCancelled(true);
                break;
            case FIRE:
            case FIRE_TICK:
            case LAVA:
                if (hasEffect(EffectType.FIRE_RESISTANCE)) {
                    return false;
                }
            default:
        }

        if (source.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
            if (namedTag.getByte("InvulnerableTimer") > 0) {
                source.setCancelled(true);
            }
            if (super.attack(source)) {
                namedTag.putByte("InvulnerableTimer", 9);
                return true;
            }
            return false;
        }

        getServer().getPluginManager().callEvent(source);
        if (source.isCancelled()) {
            return false;
        }
        setLastDamageCause(source);

        if (getDataProperty(HURT_TICKS) > 0) {
            setHealth(0);
            return true;
        }

        if (source instanceof EntityDamageByEntityEvent event) {
            if (event.getDamager() instanceof Player player) {
                if (player.isCreative()) {
                    this.level.addParticle(new DestroyBlockParticle(this.position, Block.get(BlockID.OAK_PLANKS)));
                    this.close();
                    return true;
                }
            }
        }

        setDataProperty(HURT_TICKS, 9, true);
        level.addSound(this.position, Sound.MOB_ARMOR_STAND_HIT);

        return true;
    }

    @Override
    public String getOriginalName() {
        return "Armor Stand";
    }

    @Override
    public boolean entityBaseTick(int tickDiff) {
        boolean hasUpdate = super.entityBaseTick(tickDiff);

        int hurtTime = getDataProperty(HURT_TICKS);
        if (hurtTime > 0 && age % 2 == 0) {
            setDataProperty(HURT_TICKS, hurtTime - 1, true);
            hasUpdate = true;
        }
        hurtTime = namedTag.getByte("InvulnerableTimer");
        if (hurtTime > 0 && age % 2 == 0) {
            namedTag.putByte("InvulnerableTimer", hurtTime - 1);
        }

        return hasUpdate;
    }

    @Override
    public boolean onUpdate(int currentTick) {
        int tickDiff = currentTick - lastUpdate;
        boolean hasUpdated = super.onUpdate(currentTick);

        if (closed || tickDiff <= 0 && !justCreated) {
            return hasUpdated;
        }

        lastUpdate = currentTick;

        boolean hasUpdate = entityBaseTick(tickDiff);

        if (isAlive()) {
            if (getHealth() < getMaxHealth()) {
                setHealth(getHealth() + 0.001f);
            }
            this.motion.y -= getGravity();

            double highestPosition = this.highestPosition;
            move(this.motion.x, this.motion.y, this.motion.z);

            float friction = 1 - getDrag();

            this.motion.x *= friction;
            this.motion.y *= 1 - getDrag();
            this.motion.z *= friction;

            updateMovement();
            hasUpdate = true;
            if (onGround && (highestPosition - this.position.y) >= 3) {
                level.addSound(this.position, Sound.MOB_ARMOR_STAND_LAND);
            }
        }

        return hasUpdate || !onGround || Math.abs(this.motion.x) > 0.00001 || Math.abs(this.motion.y) > 0.00001 || Math.abs(this.motion.z) > 0.00001;
    }

    @Override
    protected float getDrag() {
        if (hasWaterAt(getHeight() / 2f)) {
            return 0.25f;
        }
        return 0f;
    }

    @Override
    public String getInteractButtonText(Player player) {
        return "action.interact.armorstand.equip";
    }

    @Override
    public boolean canDoInteraction() {
        return true;
    }
}
