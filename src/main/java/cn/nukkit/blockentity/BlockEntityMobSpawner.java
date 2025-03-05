package cn.nukkit.blockentity;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockFlowable;
import cn.nukkit.block.BlockID;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.mob.monster.EntityMonster;
import cn.nukkit.entity.mob.animal.EntityAnimal;
import cn.nukkit.event.entity.CreatureSpawnEvent;
import cn.nukkit.level.GameRule;
import cn.nukkit.level.Locator;
import cn.nukkit.level.format.IChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.utils.Utils;

import java.util.Objects;

public class BlockEntityMobSpawner extends BlockEntitySpawnable {
    public static final String TAG_DELAY = "Delay";
    public static final String TAG_DISPLAY_ENTITY_HEIGHT = "DisplayEntityHeight";
    public static final String TAG_DISPLAY_ENTITY_SCALE = "DisplayEntityScale";
    public static final String TAG_DISPLAY_ENTITY_WIDTH = "DisplayEntityWidth";
    public static final String TAG_ENTITY_IDENTIFIER = "EntityIdentifier";
    public static final String TAG_MAX_NEARBY_ENTITIES = "MaxNearbyEntities";
    public static final String TAG_MAX_SPAWN_DELAY = "MaxSpawnDelay";
    public static final String TAG_MIN_SPAWN_DELAY = "MinSpawnDelay";
    public static final String TAG_REQUIRED_PLAYER_RANGE = "RequiredPlayerRange";
    public static final String TAG_SPAWN_COUNT = "SpawnCount";
    public static final String TAG_SPAWN_DATA = "SpawnData";
    public static final String TAG_SPAWN_POTENTIALS = "SpawnPotentials";
    public static final String TAG_SPAWN_RANGE = "SpawnRange";

    private static final short DELAY = 0;
    private static final float DISPLAY_ENTITY_HEIGHT = 1.8f;
    private static final float DISPLAY_ENTITY_SCALE = 1.0f;
    private static final float DISPLAY_ENTITY_WIDTH = 0.8f;
    private static final String ENTITY_IDENTIFIER = null;
    private static final short MAX_NEARBY_ENTITIES = 6;
    private static final short MAX_SPAWN_DELAY = 800;
    private static final short MIN_SPAWN_DELAY = 200;
    private static final short REQUIRED_PLAYER_RANGE = 16;
    private static final short SPAWN_COUNT = 4;
    private static final CompoundTag SPAWN_DATA = null;
    private static final ListTag<CompoundTag> SPAWN_POTENTIALS = null;
    private static final short SPAWN_RANGE = 4;

    private short delay;
    private float displayEntityHeight;
    private float displayEntityScale;
    private float displayEntityWidth;
    private String entityIdentifier;
    private short maxNearbyEntities;
    private short maxSpawnDelay;
    private short minSpawnDelay;
    private short requiredPlayerRange;
    private short spawnCount;
    private CompoundTag spawnData;
    private ListTag<CompoundTag> spawnPotentials;
    private short spawnRange;

    public BlockEntityMobSpawner(IChunk chunk, CompoundTag nbt) { super(chunk, nbt); }

    @Override
    protected void initBlockEntity() {
        this.delay = this.namedTag.containsShort(TAG_DELAY) ? this.namedTag.getShort(TAG_DELAY) : DELAY;
        this.displayEntityHeight = this.namedTag.containsFloat(TAG_DISPLAY_ENTITY_HEIGHT) ? this.namedTag.getFloat(TAG_DISPLAY_ENTITY_HEIGHT) : DISPLAY_ENTITY_HEIGHT;
        this.displayEntityScale = this.namedTag.containsFloat(TAG_DISPLAY_ENTITY_SCALE) ? this.namedTag.getFloat(TAG_DISPLAY_ENTITY_SCALE) : DISPLAY_ENTITY_SCALE;
        this.displayEntityWidth = this.namedTag.containsFloat(TAG_DISPLAY_ENTITY_WIDTH) ? this.namedTag.getFloat(TAG_DISPLAY_ENTITY_WIDTH) : DISPLAY_ENTITY_WIDTH;
        this.entityIdentifier = this.namedTag.containsString(TAG_ENTITY_IDENTIFIER) ? this.namedTag.getString(TAG_ENTITY_IDENTIFIER) : ENTITY_IDENTIFIER;
        this.maxNearbyEntities = this.namedTag.containsShort(TAG_MAX_NEARBY_ENTITIES) ? this.namedTag.getShort(TAG_MAX_NEARBY_ENTITIES) : MAX_NEARBY_ENTITIES;
        this.maxSpawnDelay = this.namedTag.containsShort(TAG_MAX_SPAWN_DELAY) ? this.namedTag.getShort(TAG_MAX_SPAWN_DELAY) : MAX_SPAWN_DELAY;
        this.minSpawnDelay = this.namedTag.containsShort(TAG_MIN_SPAWN_DELAY) ? this.namedTag.getShort(TAG_MIN_SPAWN_DELAY) : MIN_SPAWN_DELAY;
        this.requiredPlayerRange = this.namedTag.containsShort(TAG_REQUIRED_PLAYER_RANGE) ? this.namedTag.getShort(TAG_REQUIRED_PLAYER_RANGE) : REQUIRED_PLAYER_RANGE;
        this.spawnCount = this.namedTag.containsShort(TAG_SPAWN_COUNT) ? this.namedTag.getShort(TAG_SPAWN_COUNT) : SPAWN_COUNT;
        this.spawnData = this.namedTag.containsCompound(TAG_SPAWN_DATA) ? this.namedTag.getCompound(TAG_SPAWN_DATA) : SPAWN_DATA;
        this.spawnPotentials = this.namedTag.containsList(TAG_SPAWN_POTENTIALS) ? this.namedTag.getList(TAG_SPAWN_POTENTIALS, CompoundTag.class) : SPAWN_POTENTIALS;
        this.spawnRange = this.namedTag.containsShort(TAG_SPAWN_RANGE) ? this.namedTag.getShort(TAG_SPAWN_RANGE) : SPAWN_RANGE;

        this.saveNBT();

        this.scheduleUpdate();
        super.initBlockEntity();
    }

    @Override
    public boolean onUpdate() {
        if(!isBlockEntityValid()) this.close();
        if (this.closed) {
            return false;
        }

        if(!getLevel().getGameRules().getBoolean(GameRule.DO_MOB_SPAWNING)) return true;

        if (this.delay++ >= Utils.rand(this.minSpawnDelay, this.maxSpawnDelay)) {
            this.delay = 0;
            int nearbyEntities = 0;
            boolean playerInRange = false;
            for (Entity entity : this.level.getEntities()) {
                if (!playerInRange && entity instanceof Player && !((Player) entity).isSpectator()) {
                    if (entity.pos.distance(this.position) <= this.requiredPlayerRange) {
                        playerInRange = true;
                    }
                } else if (entity instanceof EntityAnimal || entity instanceof EntityMonster) {
                    if (entity.pos.distance(this.position) <= this.requiredPlayerRange) {
                        nearbyEntities++;
                    }
                }
            }

            for (int i = 0; i < this.spawnCount; i++) {
                if (playerInRange && nearbyEntities <= this.maxNearbyEntities) {
                    Locator pos = new Locator
                            (
                                    this.position.x + Utils.rand(-this.spawnRange, this.spawnRange),
                                    this.getY(),
                                    this.position.z + Utils.rand(-this.spawnRange, this.spawnRange),
                                    this.level
                            );
                    Block block = level.getBlock(pos.position);
                    //Mobs shouldn't spawn in walls and they shouldn't retry to
                    if (
                            !block.getId().equals(Block.AIR) && !(block instanceof BlockFlowable) &&
                                    !block.getId().equals(BlockID.FLOWING_WATER) && !block.getId().equals(BlockID.WATER) &&
                                    !block.getId().equals(BlockID.LAVA) && !block.getId().equals(BlockID.FLOWING_LAVA)
                    ) {
                        continue;
                    }
                    if(!block.subtract(0, 1, 0).getLevelBlock().isSolid()) {
                        continue;
                    }

                    Entity ent = Entity.createEntity(this.entityIdentifier, pos);
                    if(ent != null) {
                        CreatureSpawnEvent ev = new CreatureSpawnEvent(ent.getNetworkId(), pos, new CompoundTag(), CreatureSpawnEvent.SpawnReason.SPAWNER);
                        level.getServer().getPluginManager().callEvent(ev);

                        if (ev.isCancelled()) {
                            continue;
                        }
                        ent.spawnToAll();
                    }
                }
            }
        }
        return true;
    }

    @Override
    public void saveNBT() {
        super.saveNBT();

        this.namedTag.putShort(TAG_DELAY, this.delay);
        this.namedTag.putFloat(TAG_DISPLAY_ENTITY_HEIGHT, this.displayEntityHeight);
        this.namedTag.putFloat(TAG_DISPLAY_ENTITY_SCALE, this.displayEntityScale);
        this.namedTag.putFloat(TAG_DISPLAY_ENTITY_WIDTH, this.displayEntityWidth);
        this.namedTag.putString(TAG_ENTITY_IDENTIFIER, this.entityIdentifier);
        this.namedTag.putShort(TAG_MAX_SPAWN_DELAY, this.maxSpawnDelay);
        this.namedTag.putShort(TAG_MIN_SPAWN_DELAY, this.minSpawnDelay);
        this.namedTag.putShort(TAG_REQUIRED_PLAYER_RANGE, this.requiredPlayerRange);
        this.namedTag.putShort(TAG_SPAWN_COUNT, this.spawnCount);
        if (this.spawnData != null) {
            this.namedTag.putCompound(TAG_SPAWN_DATA, this.spawnData);
        }
        if (this.spawnPotentials != null) {
            this.namedTag.putList(TAG_SPAWN_POTENTIALS, this.spawnPotentials);
        }
        this.namedTag.putShort(TAG_SPAWN_RANGE, this.spawnRange);
    }

    @Override
    public boolean isBlockEntityValid() {
        return Objects.equals(level.getBlockIdAt((int) x, (int) y, (int) z), Block.MOB_SPAWNER);
    }

    @Override
    public CompoundTag getSpawnCompound() {
        return super.getSpawnCompound()
                .putString(TAG_ENTITY_IDENTIFIER, this.entityIdentifier);
    }

    public String getSpawnEntityType() {
        return this.entityIdentifier;
    }

    public void setSpawnEntityType(String entityIdentifier) {
        this.entityIdentifier = entityIdentifier;
        this.spawnToAll();
    }

    public void setMinSpawnDelay(short minDelay) {
        if (minDelay > this.maxSpawnDelay) {
            return;
        }

        this.minSpawnDelay = minDelay;
    }

    public void setMaxSpawnDelay(short maxDelay) {
        if (this.minSpawnDelay > maxDelay) {
            return;
        }

        this.maxSpawnDelay = maxDelay;
    }

    public void setSpawnDelay(short minDelay, short maxDelay) {
        if (minDelay > maxDelay) {
            return;
        }

        this.minSpawnDelay = minDelay;
        this.maxSpawnDelay = maxDelay;
    }

    public void setRequiredPlayerRange(short range) {
        this.requiredPlayerRange = range;
    }

    public void setMaxNearbyEntities(short count) {
        this.maxNearbyEntities = count;
    }
}
