package cn.nukkit.event.player;

import cn.nukkit.Player;
import cn.nukkit.event.Cancellable;
import cn.nukkit.event.HandlerList;
import cn.nukkit.level.Level;
import cn.nukkit.level.Transform;
import cn.nukkit.level.Locator;
import cn.nukkit.math.Vector3;

public class PlayerTeleportEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlers() {
        return handlers;
    }

    private TeleportCause cause;
    private Transform from;
    private Transform to;

    private PlayerTeleportEvent(Player player) {
        this.player = player;
    }

    public PlayerTeleportEvent(Player player, Transform from, Transform to, TeleportCause cause) {
        this(player);
        this.from = from;
        this.to = to;
        this.cause = cause;
    }

    public PlayerTeleportEvent(Player player, Vector3 from, Vector3 to, TeleportCause cause) {
        this(player);
        this.from = vectorToLocation(player.getLevel(), from);
        this.from = vectorToLocation(player.getLevel(), to);
        this.cause = cause;
    }

    public Transform getFrom() {
        return from;
    }

    public Transform getTo() {
        return to;
    }

    public TeleportCause getCause() {
        return cause;
    }

    private Transform vectorToLocation(Level baseLevel, Vector3 vector) {
//        if (vector instanceof Transform) return (Transform) vector;
//        if (vector instanceof Locator) return ((Locator) vector).getLocation();
        return new Transform(vector.getX(), vector.getY(), vector.getZ(), 0, 0, baseLevel);
    }

    public enum TeleportCause {
        COMMAND,       // For Nukkit tp command only
        PLUGIN,        // Every plugin
        NETHER_PORTAL, // Teleport using Nether portal
        ENDER_PEARL,   // Teleport by ender pearl
        CHORUS_FRUIT,  // Teleport by chorus fruit
        UNKNOWN,       // Unknown cause
        END_PORTAL,    // Teleport using End Portal
        SHULKER,
        END_GATEWAY,    // Teleport using End Gateway
        PLAYER_SPAWN    // Teleport when players are spawn

    }
}
