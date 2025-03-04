package cn.nukkit.entity.ai.executor;

import cn.nukkit.entity.mob.EntityMob;
import cn.nukkit.math.Vector3;
import org.jetbrains.annotations.NotNull;

/**
 * 封装了一些涉及控制器的方法.
 * <p>
 * Involving some methods about controller.
 */


public interface EntityControl {

    default void setRouteTarget(@NotNull EntityMob entity, Vector3 vector3) {
        entity.setMoveTarget(vector3);
    }

    default void setLookTarget(@NotNull EntityMob entity, Vector3 vector3) {
        entity.setLookTarget(vector3);
    }

    default void removeRouteTarget(@NotNull EntityMob entity) {
        entity.setMoveTarget(null);
    }

    default void removeLookTarget(@NotNull EntityMob entity) {
        entity.setLookTarget(null);
    }
}
