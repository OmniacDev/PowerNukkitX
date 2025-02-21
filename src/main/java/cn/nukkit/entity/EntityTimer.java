package cn.nukkit.entity;

import cn.nukkit.nbt.tag.CompoundTag;
import org.jetbrains.annotations.NotNull;

public class EntityTimer {
    public final static String TAG_TIME_STAMP = "TimeStamp";
    public final static String TAG_HAS_EXECUTED = "HasExecuted";
    public final static String TAG_COUNT_TIME = "CountTime";

    @NotNull public Long timeStamp = 0L;
    @NotNull public Boolean hasExecuted = false;
    @NotNull public Integer countTime = 0;

    public EntityTimer() {}
    public EntityTimer(CompoundTag timer) {
        this.timeStamp = timer.getLong(TAG_TIME_STAMP);
        this.hasExecuted = timer.getBoolean(TAG_HAS_EXECUTED);
        this.countTime = timer.getInt(TAG_COUNT_TIME);
    }

    public CompoundTag get() {
        CompoundTag timer = new CompoundTag();
        timer.putLong(TAG_TIME_STAMP, this.timeStamp);
        timer.putBoolean(TAG_HAS_EXECUTED, this.hasExecuted);
        timer.putInt(TAG_COUNT_TIME, this.countTime);
        return timer;
    }
}
