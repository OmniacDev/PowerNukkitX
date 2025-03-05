package cn.nukkit.event.vehicle;

import cn.nukkit.entity.Entity;
import cn.nukkit.event.HandlerList;
import cn.nukkit.level.Transform;


public class VehicleMoveEvent extends VehicleEvent {

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlers() {
        return handlers;
    }

    private final Transform from;
    private final Transform to;

    public VehicleMoveEvent(Entity vehicle, Transform from, Transform to) {
        super(vehicle);
        this.from = from;
        this.to = to;
    }

    public Transform getFrom() {
        return from;
    }

    public Transform getTo() {
        return to;
    }
}
