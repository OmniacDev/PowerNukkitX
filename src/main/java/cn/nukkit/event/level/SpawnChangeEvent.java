package cn.nukkit.event.level;

import cn.nukkit.event.HandlerList;
import cn.nukkit.level.Level;
import cn.nukkit.level.LevelPosition;

/**
 * @author MagicDroidX (Nukkit Project)
 */
public class SpawnChangeEvent extends LevelEvent {

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlers() {
        return handlers;
    }

    private final LevelPosition previousSpawn;

    public SpawnChangeEvent(Level level, LevelPosition previousSpawn) {
        super(level);
        this.previousSpawn = previousSpawn;
    }

    public LevelPosition getPreviousSpawn() {
        return previousSpawn;
    }
}
