package cn.nukkit.entity.ai.executor;

import cn.nukkit.block.Block;
import cn.nukkit.entity.mob.EntityMob;
import cn.nukkit.level.Locator;
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

    private Locator find(Locator locator) {
        int distance = maxDistance-minDistance;
        double dx = locator.position.x + ThreadLocalRandom.current().nextInt(-distance, distance) + minDistance;
        double dz = locator.position.z + ThreadLocalRandom.current().nextInt(-distance, distance) + minDistance;
        Vector3 pos = new Vector3(Math.floor(dx), (int) Math.floor(locator.position.y + 0.1) + maxDistance, Math.floor(dz));
        for (int y = Math.min(locator.getLevel().getMaxHeight(), (int) pos.y); y > locator.getLevel().getMinHeight(); y--) {
            Block block = locator.getLevel().getBlock((int) dx, y, (int) dz);
            if(block.isSolid()) {
                return block.up().getLocator();
            }
        }
        return locator;
    }

    @Override
    public boolean execute(EntityMob entity) {
        Locator locator = entity.getLocator();
        for(int i = 0; i < maxTries; i++) {
            if(locator.position.distance(entity.position) < minDistance) {
                locator = find(entity.getTransform());
            } else break;
        }
        if(entity.position.distance(locator.position) > minDistance) {
            entity.teleport(locator);
            locator.level.addSound(locator.position, Sound.MOB_ENDERMEN_PORTAL);
        }
        return true;
    }

}
