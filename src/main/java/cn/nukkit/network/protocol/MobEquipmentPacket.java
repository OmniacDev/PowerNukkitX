package cn.nukkit.network.protocol;

import cn.nukkit.item.Item;
import cn.nukkit.network.connection.util.HandleByteBuf;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class MobEquipmentPacket extends DataPacket {
    public long eid;
    public Item item;
    public int slot;
    public int selectedSlot;
    public int containerId;

    @Override
    public void decode(HandleByteBuf byteBuf) {
        this.eid = byteBuf.readEntityRuntimeId(); //EntityRuntimeID
        this.item = byteBuf.readSlot();
        this.slot = byteBuf.readByte();
        this.selectedSlot = byteBuf.readByte();
        this.containerId = byteBuf.readByte();
    }

    @Override
    public void encode(HandleByteBuf byteBuf) {
        byteBuf.writeEntityRuntimeId(this.eid); //EntityRuntimeID
        byteBuf.writeSlot(this.item);
        byteBuf.writeByte((byte) this.slot);
        byteBuf.writeByte((byte) this.selectedSlot);
        byteBuf.writeByte((byte) this.containerId);
    }

    @Override
    public int pid() {
        return ProtocolInfo.MOB_EQUIPMENT_PACKET;
    }

    public void handle(PacketHandler handler) {
        handler.handle(this);
    }
}
