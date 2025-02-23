package cn.nukkit.inventory;



import cn.nukkit.entity.EntityEquipment;
import cn.nukkit.item.Item;

public interface EntityInventoryHolder extends InventoryHolder {

    EntityEquipment getEquipment();

    default boolean canEquipByDispenser() {
        return false;
    }

    default Item getHelmet() {
        return getEquipment().getHead();
    }

    default boolean setHelmet(Item item) {
        return getEquipment().setHead(item);
    }

    default Item getChestplate() {
        return getEquipment().getChest();
    }

    default boolean setChestplate(Item item) {
        return getEquipment().setChest(item);
    }

    default Item getLeggings() {
        return getEquipment().getLegs();
    }

    default boolean setLeggings(Item item) {
        return getEquipment().setLegs(item);
    }

    default Item getBoots() {
        return getEquipment().getFeet();
    }

    default boolean setBoots(Item item) {
        return getEquipment().setFeet(item);
    }

    default Item getItemInHand() {
        return getEquipment().getMainHand();
    }

    default Item getItemInOffhand() {
        return getEquipment().getOffHand();
    }

    default boolean setItemInHand(Item item) {
        return getEquipment().setMainHand(item);
    }

    default boolean setItemInHand(Item item, boolean send) {
        return getEquipment().setMainHand(item, send);
    }

    default boolean setItemInOffhand(Item item) {
        return getEquipment().setOffHand(item, true);
    }

    default boolean setItemInOffhand(Item item, boolean send) {
        return getEquipment().setOffHand(item, send);
    }

    default boolean equip(Item item) {
        return this.getEquipment().equip(item);
    }
}
