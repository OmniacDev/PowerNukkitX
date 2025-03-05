package cn.nukkit.entity.ai.executor;

import cn.nukkit.block.Block;
import cn.nukkit.entity.mob.EntityMob;
import cn.nukkit.level.Transform;
import cn.nukkit.level.Sound;
import cn.nukkit.math.Vector3;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.ThreadLocalRandom;

@RequiredArgsConstructor
@AllArgsConstructor
public class TeleportExecutor implements IBehaviorExecutor {

    int maxDistance;
    int minDistance;
    int maxTries = 16;

    private Transform find(Transform transform) {
        int distance = maxDistance-minDistance;
        double dx = transform.x + ThreadLocalRandom.current().nextInt(-distance, distance) + minDistance;
        double dz = transform.z + ThreadLocalRandom.current().nextInt(-distance, distance) + minDistance;
        Vector3 pos = new Vector3(Math.floor(dx), (int) Math.floor(transform.y + 0.1) + maxDistance, Math.floor(dz));
        for (int y = Math.min(transform.getLevel().getMaxHeight(), (int) pos.y); y > transform.getLevel().getMinHeight(); y--) {
            Block block = transform.getValidLevel().getBlock((int) dx, y, (int) dz);
            if(block.isSolid()) {
                return block.up().getLocation();
            }
        }
        return transform;
    }

    @Override
    public boolean execute(EntityMob entity) {
        Transform transform = entity.getLocation();
        for(int i = 0; i < maxTries; i++) {
            if(transform.distance(entity.pos) < minDistance) {
                transform = find(entity.getLocation());
            } else break;
        }
        if(entity.pos.distance(transform) > minDistance) {
            entity.teleport(transform);
            transform.level.addSound(transform, Sound.MOB_ENDERMEN_PORTAL);
        }
        return true;
    }

}
