package cn.nukkit.positiontracking;

import org.jetbrains.annotations.NotNull;

public interface NamedPosition extends Cloneable {
    @NotNull String getLevelName();

    double getX();
    double getY();
    double getZ();

    default boolean matchesNamedPosition(NamedPosition position) {
        return getX() == position.getX() && getY() == position.getY() && getZ() == position.getZ() && getLevelName().equals(position.getLevelName());
    }

    NamedPosition clone();
}
