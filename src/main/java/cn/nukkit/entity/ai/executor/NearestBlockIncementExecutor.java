package cn.nukkit.entity.ai.executor;

import cn.nukkit.entity.ai.memory.CoreMemoryTypes;
import cn.nukkit.entity.mob.EntityMob;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class NearestBlockIncementExecutor implements IBehaviorExecutor {

    @Override
    public boolean execute(EntityMob entity) {
        if(!entity.getMemoryStorage().isEmpty(CoreMemoryTypes.NEAREST_BLOCK)) {
            entity.getMemoryStorage().put(CoreMemoryTypes.NEAREST_BLOCK, entity.getMemoryStorage().get(CoreMemoryTypes.NEAREST_BLOCK).up());
        }
        return true;
    }
}
