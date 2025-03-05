package cn.nukkit.event.player;

import cn.nukkit.Player;
import cn.nukkit.event.Cancellable;
import cn.nukkit.event.HandlerList;
import cn.nukkit.level.Transform;

public class PlayerMoveEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlers() {
        return handlers;
    }

    private Transform from;
    private Transform to;

    private boolean resetBlocksAround;

    public PlayerMoveEvent(Player player, Transform from, Transform to) {
        this(player, from, to, true);
    }

    public PlayerMoveEvent(Player player, Transform from, Transform to, boolean resetBlocks) {
        this.player = player;
        this.from = from;
        this.to = to;
        this.resetBlocksAround = resetBlocks;
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

    public boolean isResetBlocksAround() {
        return resetBlocksAround;
    }

    public void setResetBlocksAround(boolean value) {
        this.resetBlocksAround = value;
    }

    @Override
    public void setCancelled() {
        super.setCancelled();
    }
}
