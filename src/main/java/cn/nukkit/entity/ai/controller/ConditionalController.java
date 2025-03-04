package cn.nukkit.entity.ai.controller;

import cn.nukkit.entity.mob.EntityMob;
import cn.nukkit.entity.mob.EntityMob;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;

import java.util.Arrays;
import java.util.function.Predicate;

public class ConditionalController implements IController {

    private Object2ObjectArrayMap<Predicate<EntityMob>, IController> controllers = new Object2ObjectArrayMap<>();

    public ConditionalController(Pair<Predicate<EntityMob>, IController>... controllers) {
        Arrays.stream(controllers).forEach(pair -> this.controllers.put(pair.first(), pair.second()));
    }

    @Override
    public boolean control(EntityMob entity) {
        boolean successful = false;
        for(Object2ObjectMap.Entry<Predicate<EntityMob>, IController> entry : controllers.object2ObjectEntrySet()) {
            if(entry.getKey().test(entity)) {
                if(entry.getValue().control(entity)) {
                    successful = true;
                }
            }
        }
        return successful;
    }
}
