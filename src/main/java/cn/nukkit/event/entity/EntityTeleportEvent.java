package cn.nukkit.event.entity;

import cn.nukkit.entity.Entity;
import cn.nukkit.event.Cancellable;
import cn.nukkit.event.HandlerList;
import cn.nukkit.event.player.PlayerTeleportEvent;
import cn.nukkit.level.Transform;

/**
 * @author MagicDroidX (Nukkit Project)
 */
public class EntityTeleportEvent extends EntityEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlers() {
        return handlers;
    }

    private Transform from;
    private Transform to;
    private final PlayerTeleportEvent.TeleportCause cause;

    public EntityTeleportEvent(Entity entity, Transform from, Transform to) {
        this(entity, from, to, PlayerTeleportEvent.TeleportCause.UNKNOWN);
    }

    public EntityTeleportEvent(Entity entity, Transform from, Transform to, PlayerTeleportEvent.TeleportCause cause) {
        this.entity = entity;
        this.from = from;
        this.to = to;
        this.cause = cause;
    }

    public Transform getFrom() {
        return from;
    }

    public void setFrom(Transform from) {
        this.from = from;
    }

    public Transform getTo() {
        return to;
    }

    public void setTo(Transform to) {
        this.to = to;
    }

    public PlayerTeleportEvent.TeleportCause getCause() {
        return cause;
    }
}
