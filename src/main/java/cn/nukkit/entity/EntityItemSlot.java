package cn.nukkit.entity;

import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.nbt.tag.StringTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class EntityItemSlot {
    public final static String TAG_SLOT = "Slot";
    public final static String TAG_BLOCK = "Block";
    public final static String TAG_CAN_DESTROY = "CanDestroy";
    public final static String TAG_CAN_PLACE_ON = "CanPlaceOn";
    public final static String TAG_COUNT = "Count";
    public final static String TAG_DAMAGE = "Damage";
    public final static String TAG_NAME = "Name";
    public final static String TAG_TAG = "tag";
    public final static String TAG_WAS_PICKED_UP = "WasPickedUp";

    @NotNull public Byte slot = 0;
    @Nullable public CompoundTag block;
    @Nullable public List<String> canDestroy;
    @Nullable public List<String> canPlaceOn;
    @NotNull public Byte count = 0;
    @NotNull public Short damage = 0;
    @NotNull public String name = "";
    @Nullable public CompoundTag tag;
    @NotNull public Boolean wasPickedUp = false;

    public EntityItemSlot() {}
    public EntityItemSlot(CompoundTag nbt) {
        this.slot = nbt.getByte(TAG_SLOT);
        this.block = nbt.contains(TAG_BLOCK) ? nbt.getCompound(TAG_BLOCK) : null;
        if (nbt.contains(TAG_CAN_DESTROY)) {
            ListTag<StringTag> canDestroy = nbt.getList(TAG_CAN_DESTROY, StringTag.class);
            this.canDestroy = canDestroy.getAll().stream().map(StringTag::parseValue).toList();
        }
        if (nbt.contains(TAG_CAN_PLACE_ON)) {
            ListTag<StringTag> canPlaceOn = nbt.getList(TAG_CAN_PLACE_ON, StringTag.class);
            this.canPlaceOn = canPlaceOn.getAll().stream().map(StringTag::parseValue).toList();
        }
        this.count = nbt.getByte(TAG_COUNT);
        this.damage = nbt.getShort(TAG_DAMAGE);
        this.name = nbt.getString(TAG_NAME);
        this.tag = nbt.contains(TAG_TAG) ? nbt.getCompound(TAG_TAG) : null;
        this.wasPickedUp = nbt.getBoolean(TAG_WAS_PICKED_UP);
    }

    public CompoundTag get() {
        CompoundTag nbt = new CompoundTag();
        nbt.putByte(TAG_SLOT, this.slot);
        if (this.block != null) {
            nbt.putCompound(TAG_BLOCK, this.block);
        }
        if (this.canDestroy != null) {
            List<StringTag> canDestroy = this.canDestroy.stream().map(StringTag::new).toList();
            nbt.putList(TAG_CAN_DESTROY, new ListTag<>(canDestroy));
        }
        if (this.canPlaceOn != null) {
            List<StringTag> canPlaceOn = this.canPlaceOn.stream().map(StringTag::new).toList();
            nbt.putList(TAG_CAN_PLACE_ON, new ListTag<>(canPlaceOn));
        }
        nbt.putByte(TAG_COUNT, this.count);
        nbt.putShort(TAG_DAMAGE, this.damage);
        nbt.putString(TAG_NAME, this.name);
        if (this.tag != null) {
            nbt.putCompound(TAG_TAG, this.tag);
        }
        nbt.putBoolean(TAG_WAS_PICKED_UP, this.wasPickedUp);
        return nbt;
    }
}
