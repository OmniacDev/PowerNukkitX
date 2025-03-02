package cn.nukkit.entity;

import cn.nukkit.Player;


import cn.nukkit.entity.item.EntityItem;
import cn.nukkit.inventory.BaseInventory;
import cn.nukkit.inventory.InventoryHolder;
import cn.nukkit.inventory.InventoryType;
import cn.nukkit.item.Item;
import cn.nukkit.network.protocol.MobArmorEquipmentPacket;
import cn.nukkit.network.protocol.MobEquipmentPacket;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class EntityEquipment extends BaseInventory {
    private final Entity entity;

    public final static int HEAD = 0;
    public final static int CHEST = 1;
    public final static int LEGS = 2;
    public final static int FEET = 3;

    public final static int MAIN_HAND = 4;
    public final static int OFF_HAND = 5;

    /**
     * @param holder an Entity which implements {@link InventoryHolder}.
     * @throws ClassCastException if the entity does not implements {@link InventoryHolder}
     */
    public EntityEquipment(InventoryHolder holder) {
        super(holder, InventoryType.INVENTORY, 6);
        this.entity = (Entity) holder;
    }

    @Override
    public int getSize() {
        return 6;
    }

    public Entity getEntity() {
        return entity;
    }

    @Override
    public Set<Player> getViewers() {
        Set<Player> viewers = new HashSet<>(this.viewers);
        viewers.addAll(entity.getViewers().values());
        return viewers;
    }

    @Override
    public boolean open(Player who) {
        return this.viewers.add(who);
    }

    public Item getMainHand() {
        return this.getItem(MAIN_HAND);
    }

    public Item getOffHand() {
        return this.getItem(OFF_HAND);
    }

    public boolean setMainHand(Item item) {
        return this.setMainHand(item, true);
    }

    public boolean setMainHand(Item item, boolean send) {
        return this.setItem(MAIN_HAND, item, send);
    }

    public boolean setOffHand(Item item) {
        return this.setOffHand(item, true);
    }

    public boolean setOffHand(Item item, boolean send) {
        return this.setItem(OFF_HAND, item, send);
    }

    public List<Item> getArmor() {
        List<Item> armor = new ArrayList<>();
        armor.add(HEAD, this.getHead());
        armor.add(CHEST, this.getChest());
        armor.add(LEGS, this.getLegs());
        armor.add(FEET, this.getFeet());
        return armor;
    }

    public Item getHead() {
        return this.getItem(HEAD);
    }

    public Item getChest() {
        return this.getItem(CHEST);
    }

    public Item getLegs() {
        return this.getItem(LEGS);
    }

    public Item getFeet() {
        return this.getItem(FEET);
    }

    public boolean setArmor(List<Item> items) {
        return this.setArmor(items, true);
    };

    public boolean setArmor(List<Item> items, boolean send) {
        boolean head = this.setHead(items.get(HEAD), send);
        boolean chest = this.setChest(items.get(CHEST), send);
        boolean legs = this.setLegs(items.get(LEGS), send);
        boolean feet = this.setFeet(items.get(FEET), send);
        return head && chest && legs && feet;
    }

    public boolean setHead(Item item) {
        return this.setHead(item, true);
    }

    public boolean setHead(Item item, boolean send) {
        return this.setItem(HEAD, item, send);
    }

    public boolean setChest(Item item) {
        return this.setChest(item, true);
    }

    public boolean setChest(Item item, boolean send) {
        return this.setItem(CHEST, item, send);
    }

    public boolean setLegs(Item item) {
        return this.setLegs(item, true);
    }

    public boolean setLegs(Item item, boolean send) {
        return this.setItem(LEGS, item, send);
    }

    public boolean setFeet(Item item) {
        return this.setFeet(item, true);
    }

    public boolean setFeet(Item item, boolean send) {
        return this.setItem(FEET, item, send);
    }

    public boolean canEquipByDispenser() {
        return true;
    }

    public boolean equip(Item item) {
        if (item.isHelmet()) {
            if (item.getTier() > getHead().getTier()) {
                this.entity.level.dropItem(this.entity.pos, getHead());
                this.setHead(item);
                return true;
            }
        } else if (item.isChestplate()) {
            if (item.getTier() > getChest().getTier()) {
                this.entity.level.dropItem(this.entity.pos, getChest());
                this.setChest(item);
                return true;
            }
        } else if (item.isLeggings()) {
            if (item.getTier() > getLegs().getTier()) {
                this.entity.level.dropItem(this.entity.pos, getLegs());
                this.setLegs(item);
                return true;
            }
        } else if (item.isBoots()) {
            if (item.getTier() > getFeet().getTier()) {
                this.entity.level.dropItem(this.entity.pos, getFeet());
                this.setFeet(item);
                return true;
            }
        } else if (item.getTier() > getMainHand().getTier()) {
            this.entity.level.dropItem(this.entity.pos, getMainHand());
            this.setMainHand(item);
            return true;
        }
        return false;
    }

    @Override
    public void sendSlot(int index, Player... players) {
        for (Player player : players) {
            this.sendSlot(index, player);
        }
    }

    @Override
    public void sendSlot(int index, Player player) {
        switch (index) {
            case MAIN_HAND, OFF_HAND -> {
                MobEquipmentPacket packet = new MobEquipmentPacket();
                packet.eid = this.entity.getId();
                packet.slot = index - 4;
                packet.selectedSlot = 0;
                packet.item = this.getItem(index);
                player.dataPacket(packet);
            }
            case HEAD, CHEST, LEGS, FEET -> {
                MobArmorEquipmentPacket packet = new MobArmorEquipmentPacket();
                packet.eid = this.entity.getId();
                packet.slots = this.getArmor().toArray(Item.EMPTY_ARRAY);
                player.dataPacket(packet);
            }
            default -> throw new IllegalStateException("Unexpected value: " + index);
        }
    }

    @Override
    public void sendContents(Player target) {
        this.sendSlot(HEAD, target);
        this.sendSlot(CHEST, target);
        this.sendSlot(LEGS, target);
        this.sendSlot(FEET, target);
        this.sendSlot(MAIN_HAND, target);
        this.sendSlot(OFF_HAND, target);
    }

    @Override
    public void sendContents(Player... target) {
        for (Player player : target) {
            this.sendContents(player);
        }
    }
}
