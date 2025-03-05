package cn.nukkit.event.level;

import cn.nukkit.event.HandlerList;
import cn.nukkit.level.Level;
import cn.nukkit.level.Locator;

/**
 * @author MagicDroidX (Nukkit Project)
 */
public class SpawnChangeEvent extends LevelEvent {

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlers() {
        return handlers;
    }

    private final Locator previousSpawn;

    public SpawnChangeEvent(Level level, Locator previousSpawn) {
        super(level);
        this.previousSpawn = previousSpawn;
    }

    public Locator getPreviousSpawn() {
        return previousSpawn;
    }
}
