package cn.nukkit.entity;

import cn.nukkit.entity.data.EntityFlag;

/**
 * @author MagicDroidX (Nukkit Project)
 */
public interface EntityAgeable {
    boolean getDataFlag(EntityFlag id);
    void setDataFlag(EntityFlag entityFlag, boolean value);
    void setScale(float scale);

    default boolean isBaby() {
        return this.getDataFlag(EntityFlag.BABY);
    }

    default void setBaby(boolean flag) {
        this.setDataFlag(EntityFlag.BABY, flag);
        this.setScale(flag ? 0.5f : 1f);
    }
}
