package cn.nukkit.command.defaults;

import cn.nukkit.Server;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.command.tree.ParamList;
import cn.nukkit.command.utils.CommandLogger;
import cn.nukkit.level.Level;
import cn.nukkit.level.Locator;
import cn.nukkit.level.tickingarea.TickingArea;
import cn.nukkit.level.tickingarea.manager.TickingAreaManager;
import cn.nukkit.math.Vector2;
import cn.nukkit.utils.TextFormat;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class TickingAreaCommand extends VanillaCommand {

    public TickingAreaCommand(String name) {
        super(name, "commands.tickingarea.description");
        this.setPermission("nukkit.command.tickingarea");
        this.commandParameters.clear();
        this.commandParameters.put("add-pos", new CommandParameter[]{
                CommandParameter.newEnum("add", new String[]{"add"}),
                CommandParameter.newType("from", CommandParamType.POSITION),
                CommandParameter.newType("to", CommandParamType.POSITION),
                CommandParameter.newType("name", true, CommandParamType.STRING)
        });
        this.commandParameters.put("add-circle", new CommandParameter[]{
                CommandParameter.newEnum("add", new String[]{"add"}),
                CommandParameter.newEnum("circle", new String[]{"circle"}),
                CommandParameter.newType("center", CommandParamType.POSITION),
                CommandParameter.newType("radius", CommandParamType.INT),
                CommandParameter.newType("name", true, CommandParamType.STRING)
        });
        this.commandParameters.put("remove-pos", new CommandParameter[]{
                CommandParameter.newEnum("remove", new String[]{"remove"}),
                CommandParameter.newType("position", CommandParamType.POSITION)
        });
        this.commandParameters.put("remove-name", new CommandParameter[]{
                CommandParameter.newEnum("remove", new String[]{"remove"}),
                CommandParameter.newType("name", CommandParamType.STRING)
        });
        this.commandParameters.put("remove-all", new CommandParameter[]{
                CommandParameter.newEnum("remove-all", new String[]{"remove-all"})
        });
        this.commandParameters.put("list", new CommandParameter[]{
                CommandParameter.newEnum("list", new String[]{"list"}),
                CommandParameter.newEnum("all-dimensions", true, new String[]{"all-dimensions"})
        });
        this.enableParamTree();
    }

    @Override
    public int execute(CommandSender sender, String commandLabel, Map.Entry<String, ParamList> result, CommandLogger log) {
        var list = result.getValue();
        TickingAreaManager manager = Server.getInstance().getTickingAreaManager();
        Level level = sender.getLocator().getLevel();
        switch (result.getKey()) {
            case "add-pos" -> {
                Locator from = list.getResult(1);
                Locator to = list.getResult(2);
                String name = "";//will auto generate name if not set, like "Area0"
                if (list.hasResult(3))
                    name = list.getResult(3);
                if (manager.containTickingArea(name)) {
                    log.addError("commands.tickingarea-add.conflictingname", name).output();
                    return 0;
                }
                TickingArea area = new TickingArea(name, level.getName());
                for (int chunkX = Math.min(from.position.getChunkX(), to.position.getChunkX()); chunkX <= Math.max(from.position.getChunkX(), to.position.getChunkX()); chunkX++) {
                    for (int chunkZ = Math.min(from.position.getChunkZ(), to.position.getChunkZ()); chunkZ <= Math.max(from.position.getChunkZ(), to.position.getChunkZ()); chunkZ++) {
                        area.addChunk(new TickingArea.ChunkPos(chunkX, chunkZ));
                    }
                }
                manager.addTickingArea(area);
                log.addSuccess("commands.tickingarea-add-bounds.success", (int) from.position.x + "," + (int) from.position.y + "," + (int) from.position.z, (int) to.position.x + "," + (int) to.position.y + "," + (int) to.position.z).output();
                return 1;
            }
            case "add-circle" -> {
                Locator center = list.getResult(2);
                int radius = list.getResult(3);
                String name = "";//will auto generate name if not set, like "Area0"
                if (list.hasResult(4))
                    name = list.getResult(4);
                if (manager.containTickingArea(name)) {
                    log.addError("commands.tickingarea-add.conflictingname", name).output();
                    return 0;
                }
                //计算出哪些区块和圆重合
                TickingArea area = new TickingArea(name, level.getName());
                Vector2 centerVec2 = new Vector2(center.position.getChunkX(), center.position.getChunkZ());
                int radiusSquared = radius * radius;
                for (int chunkX = center.position.getChunkX() - radius; chunkX <= center.position.getChunkX() + radius; chunkX++) {
                    for (int chunkZ = center.position.getChunkZ() - radius; chunkZ <= center.position.getChunkZ() + radius; chunkZ++) {
                        double distanceSquared = new Vector2(chunkX, chunkZ).distanceSquared(centerVec2);
                        if (distanceSquared <= radiusSquared) {
                            area.addChunk(new TickingArea.ChunkPos(chunkX, chunkZ));
                        }
                    }
                }
                manager.addTickingArea(area);
                log.addSuccess("commands.tickingarea-add-circle.success", (int) center.position.x + "," + (int) center.position.y + "," + (int) center.position.z, String.valueOf(radius)).output();
                return 1;
            }
            case "remove-pos" -> {
                Locator pos = list.getResult(1);
                if (manager.getTickingAreaByPos(pos) == null) {
                    log.addSuccess("commands.tickingarea-remove.failure", String.valueOf((int) pos.position.x), String.valueOf((int) pos.position.y), String.valueOf((int) pos.position.z)).output();
                    return 0;
                }
                manager.removeTickingArea(manager.getTickingAreaByPos(pos).getName());
                log.addSuccess("commands.tickingarea-remove.success").output();
                return 1;
            }
            case "remove-name" -> {
                String name = list.getResult(1);
                if (!manager.containTickingArea(name)) {
                    log.addSuccess("commands.tickingarea-remove.byname.failure", name).output();
                    return 0;
                }
                manager.removeTickingArea(name);
                log.addSuccess("commands.tickingarea-remove.success").output();
                return 1;
            }
            case "remove-all" -> {
                if (manager.getAllTickingArea().isEmpty()) {
                    log.addSuccess("commands.tickingarea-list.failure.allDimensions").output();
                    return 0;
                }
                manager.removeAllTickingArea();
                log.addSuccess("commands.tickingarea-remove_all.success").output();
                return 1;
            }
            case "list" -> {
                var areas = manager.getAllTickingArea();
                boolean showAll = list.hasResult(1);
                if (!showAll) {
                    areas = areas.stream().filter(area -> area.getLevelName().equals(level.getName())).collect(Collectors.toSet());
                    if (areas.isEmpty()) {
                        log.addError("commands.tickingarea-remove_all.failure").output();
                        return 0;
                    }
                    log.addSuccess(TextFormat.GREEN + "%commands.tickingarea-list.success.currentDimension").output();
                    for (TickingArea area : areas) {
                        List<TickingArea.ChunkPos> minAndMax = area.minAndMaxChunkPos();
                        TickingArea.ChunkPos min = minAndMax.get(0);
                        TickingArea.ChunkPos max = minAndMax.get(1);
                        log.addSuccess(" - " + area.getName() + ": " + min.x + " " + min.z + " %commands.tickingarea-list.to " + max.x + " " + max.z).output();
                    }
                    log.addSuccess("commands.tickingarea.inuse", String.valueOf(areas.size()), "∞").output();
                } else {
                    if (areas.isEmpty()) {
                        log.addError("commands.tickingarea-list.failure.allDimensions").output();
                        return 0;
                    }
                    log.addSuccess(TextFormat.GREEN + "%commands.tickingarea-list.success.allDimensions").output();
                    for (TickingArea area : areas) {
                        var minAndMax = area.minAndMaxChunkPos();
                        TickingArea.ChunkPos min = minAndMax.get(0);
                        TickingArea.ChunkPos max = minAndMax.get(1);
                        log.addSuccess(" - " + area.getName() + ": " + min.x + " " + min.z + " %commands.tickingarea-list.to " + max.x + " " + max.z).output();
                    }
                    log.addSuccess("commands.tickingarea.inuse", String.valueOf(areas.size()), "∞").output();
                }
                return 1;
            }
            default -> {
                return 0;
            }
        }
    }
}
