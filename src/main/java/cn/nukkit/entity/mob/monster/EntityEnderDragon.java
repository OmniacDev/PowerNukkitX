package cn.nukkit.entity.mob.monster;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.block.BlockEndGateway;
import cn.nukkit.entity.Attribute;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityFlyable;
import cn.nukkit.entity.mob.EntityMob;
import cn.nukkit.entity.ai.behavior.Behavior;
import cn.nukkit.entity.ai.behaviorgroup.BehaviorGroup;
import cn.nukkit.entity.ai.behaviorgroup.IBehaviorGroup;
import cn.nukkit.entity.ai.controller.IController;
import cn.nukkit.entity.ai.controller.LiftController;
import cn.nukkit.entity.ai.controller.SpaceMoveController;
import cn.nukkit.entity.ai.evaluator.EntityCheckEvaluator;
import cn.nukkit.entity.ai.evaluator.MemoryCheckEmptyEvaluator;
import cn.nukkit.entity.ai.evaluator.MemoryCheckNotEmptyEvaluator;
import cn.nukkit.entity.ai.evaluator.RandomSoundEvaluator;
import cn.nukkit.entity.ai.executor.*;
import cn.nukkit.entity.ai.executor.enderdragon.CircleMovementExecutor;
import cn.nukkit.entity.ai.executor.enderdragon.PerchingExecutor;
import cn.nukkit.entity.ai.executor.enderdragon.StrafeExecutor;
import cn.nukkit.entity.ai.memory.CoreMemoryTypes;
import cn.nukkit.entity.ai.route.data.Node;
import cn.nukkit.entity.ai.route.finder.impl.SimpleSpaceAStarRouteFinder;
import cn.nukkit.entity.ai.route.posevaluator.FlyingPosEvaluator;
import cn.nukkit.entity.ai.route.posevaluator.IPosEvaluator;
import cn.nukkit.entity.ai.sensor.NearestEntitySensor;
import cn.nukkit.entity.ai.sensor.NearestPlayerSensor;
import cn.nukkit.entity.item.EntityEnderCrystal;
import cn.nukkit.entity.mob.EntityMob;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.Sound;
import cn.nukkit.level.format.IChunk;
import cn.nukkit.math.BVector3;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.Vector2;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.AddEntityPacket;
import cn.nukkit.network.protocol.BossEventPacket;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.EntityEventPacket;
import cn.nukkit.network.protocol.LevelSoundEventPacket;
import cn.nukkit.plugin.InternalPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

public class EntityEnderDragon extends EntityBoss implements EntityFlyable {

    @Override
    @NotNull public String getIdentifier() {
        return ENDER_DRAGON;
    }

    public EntityEnderDragon(IChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public IBehaviorGroup requireBehaviorGroup() {
        return new BehaviorGroup(
                this.tickSpread,
                Set.of(
                        new Behavior(new PlaySoundExecutor(Sound.MOB_ENDERDRAGON_GROWL), new RandomSoundEvaluator(), 2, 1)
                ),
                Set.of(
                        new Behavior(new PerchingExecutor(), entity -> getMemoryStorage().get(CoreMemoryTypes.FORCE_PERCHING), 5, 1),
                        new Behavior(new StrafeExecutor(), all(
                                new MemoryCheckNotEmptyEvaluator(CoreMemoryTypes.NEAREST_PLAYER),
                                new EntityCheckEvaluator(CoreMemoryTypes.NEAREST_PLAYER),
                                new MemoryCheckNotEmptyEvaluator(CoreMemoryTypes.LAST_ENDER_CRYSTAL_DESTROY)
                        ), 4, 1),
                        new Behavior(new CircleMovementExecutor(CoreMemoryTypes.STAY_NEARBY, 1f, true, 82, 12, 5), all(
                                new MemoryCheckNotEmptyEvaluator(CoreMemoryTypes.STAY_NEARBY),
                                new MemoryCheckEmptyEvaluator(CoreMemoryTypes.NEAREST_SHARED_ENTITY)
                        ), 3, 1),
                        new Behavior(new CircleMovementExecutor(CoreMemoryTypes.STAY_NEARBY, 1f, true, 48, 8, 4), all(
                                new MemoryCheckNotEmptyEvaluator(CoreMemoryTypes.STAY_NEARBY),
                                new MemoryCheckNotEmptyEvaluator(CoreMemoryTypes.NEAREST_SHARED_ENTITY)
                        ), 2, 1)
                ),
                Set.of(
                        new NearestPlayerSensor(512, 0, 20),
                        new NearestEntitySensor(EntityEnderCrystal.class, CoreMemoryTypes.NEAREST_SHARED_ENTITY, 192, 0, 10)
                ),
                Set.of(new SpaceMoveController(), new LookController(), new LiftController()),
                new EnderDragonRouteFinder(new EnderDragonPosEvaluator(), this),
                this
        );
    }

    @Override
    protected DataPacket createAddEntityPacket() {
        AddEntityPacket addEntity = new AddEntityPacket();
        addEntity.type = this.getNetworkId();
        addEntity.entityUniqueId = this.getId();
        addEntity.entityRuntimeId = this.getId();
        addEntity.yaw = (float) this.rotation.yaw;
        addEntity.headYaw = (float) this.rotation.yaw;
        addEntity.pitch = (float) this.rotation.pitch;
        addEntity.x = (float) this.pos.x;
        addEntity.y = (float) this.pos.y;
        addEntity.z = (float) this.pos.z;
        addEntity.speedX = (float) this.motion.x;
        addEntity.speedY = (float) this.motion.y;
        addEntity.speedZ = (float) this.motion.z;
        addEntity.entityData = this.entityDataMap;
        addEntity.attributes = new Attribute[]{Attribute.getAttribute(Attribute.MAX_HEALTH).setMaxValue(200).setValue(200)};
        return addEntity;
    }

    @Override
    public boolean attack(EntityDamageEvent source) {
        if(deathTicks != -1) return false;
        switch (source.getCause()) {
            case SUFFOCATION,
                 MAGIC-> {
                return false;
            }
        }
        return super.attack(source);
    }

    @Override
    public boolean onUpdate(int currentTick) {
        //Hack -> Ensures that Ender Dragon is always ticked.
        getLevel().getScheduler().scheduleTask(InternalPlugin.INSTANCE, this::scheduleUpdate);
        if(deathTicks != -1) {
            if(deathTicks <= 0) {
                kill();
            } else deathTicks--;
            return true;
        }
        if (currentTick % 2 == 0) {
            if(currentTick % ((this.pos.toHorizontal().distance(Vector2.ZERO) < 1) ? 10 : 20) == 0) {
                getLevel().addLevelSoundEvent(this.pos, LevelSoundEventPacket.SOUND_FLAP, -1, this.getIdentifier(), false, false);
            }
            for (Entity e : this.getLevel().getEntities()) {
                if (e instanceof EntityEnderCrystal) {
                    if (e.pos.distance(this.pos) <= 28) {
                        float health = this.getHealth();
                        if (!(health > this.getMaxHealth()) && health != 0) {
                            this.heal(0.2f);
                        }
                    }
                }
            }
        }
        return super.onUpdate(currentTick);
    }

    private int deathTicks = -1;

    @Override
    public void kill() {
        if(deathTicks == -1) {
            deathTicks = 190;
            getLevel().addLevelSoundEvent(this.pos, LevelSoundEventPacket.SOUND_DEATH, -1, getIdentifier(), false, false);
            EntityEventPacket packet = new EntityEventPacket();
            packet.event = EntityEventPacket.ENDER_DRAGON_DEATH;
            packet.eid = getId();
            Server.broadcastPacket(getViewers().values(), packet);
            setImmobile(true);
        } else {
            super.kill();
            close();
            if(!isRevived()) {
                getLevel().setBlock(new Vector3(0, getLevel().getHighestBlockAt(Vector2.ZERO)+1, 0), Block.get(Block.DRAGON_EGG));
            }
            for(int i = -2; i <= 2; i++) {
                for(int j = -1; j <= 1; j++) {
                    if(!(i == 0 && j == 0)) {
                        getLevel().setBlock(new Vector3(i, 63, j), Block.get(Block.END_PORTAL));
                        getLevel().setBlock(new Vector3(j, 63, i), Block.get(Block.END_PORTAL));
                    }
                }
            }
            for(int i = 0; i < 20; i++) {
                Vector3 origin = Vector3.ZERO;
                double angleIncrement = 360.0 / 20;
                double angle = Math.toRadians(i * angleIncrement);
                double particleX = origin.getX() + Math.cos(angle) * 96;
                double particleZ = origin.getZ() + Math.sin(angle) * 96;
                Block dest = getLevel().getBlock(new Vector3(particleX, 75, particleZ));
                if(!(dest instanceof BlockEndGateway)) {
                    Arrays.stream(BlockFace.values()).forEach(face -> getLevel().setBlock(dest.up().getSide(face), Block.get(Block.BEDROCK)));
                    Arrays.stream(BlockFace.values()).forEach(face -> getLevel().setBlock(dest.down().getSide(face), Block.get(Block.BEDROCK)));
                    getLevel().setBlock(dest, Block.get(Block.END_GATEWAY));
                    break;
                } else continue;
            }
        }
    }

    @Override
    public float getWidth() {
        return 13;
    }

    @Override
    public float getHeight() {
        return 4;
    }

    @Override
    public void initEntity() {
        this.diffHandDamage = new float[]{6f, 10f, 15f};
        this.setMaxHealth(200);
        super.initEntity();
        getMemoryStorage().put(CoreMemoryTypes.STAY_NEARBY, new Vector3(0, 84, 0));
        isActive = false;
        noClip = true;
    }

    @Override
    public boolean playerApplyNameTag(@NotNull Player player, @NotNull Item item) {
        return false;
    }

    @Override
    public String getOriginalName() {
        return "Ender Dragon";
    }

    @Override
    public boolean isBoss() {
        return true;
    }

    @Override
    public void addBossbar(Player player) {
        BossEventPacket pkBoss = new BossEventPacket();
        pkBoss.bossEid = this.id;
        pkBoss.type = BossEventPacket.TYPE_SHOW;
        pkBoss.title = this.getName();
        pkBoss.color = 5;
        pkBoss.healthPercent = health / getMaxHealth();
        player.dataPacket(pkBoss);
    }

    @Override
    public Integer getExperienceDrops() {
        return isRevived() ? 500 : 12000;
    }

    private class EnderDragonRouteFinder extends SimpleSpaceAStarRouteFinder {

        public EnderDragonRouteFinder(IPosEvaluator blockEvaluator, EntityMob entity) {
            super(blockEvaluator, entity);
        }

        @Override
        public boolean search() {
            boolean superRes = super.search();
            if(superRes && getMemoryStorage().notEmpty(CoreMemoryTypes.MOVE_TARGET)) {
                this.nodes = new ArrayList<>(Collections.singleton(nodes.getFirst()));
                nodes.add(new Node(getMemoryStorage().get(CoreMemoryTypes.MOVE_TARGET), null, 0, 0));
            }
            return superRes;
        }
    }

    private class EnderDragonPosEvaluator extends FlyingPosEvaluator {
        protected boolean isPassable(EntityMob entity, Vector3 vector3) {
            return true;
        }
    }

    @Override
    public boolean move(double dx, double dy, double dz) {
        boolean superRes = super.move(dx, dy, dz);
        if(superRes) {
            Arrays.stream(getLevel().getCollisionBlocks(getBoundingBox())).filter(block -> canBreakBlock(block)).forEach(block -> getLevel().breakBlock(block));
        }
        return superRes;
    }

    public boolean isRevived() {
        if(this.namedTag.contains("Revived")) {
            return this.namedTag.getBoolean("Revived");
        } else return false;
    }

    protected class LookController implements IController {
        @Override
        public boolean control(EntityMob entity) {
            Vector3 target = entity.getMemoryStorage().get(CoreMemoryTypes.LOOK_TARGET);
            if(target == null) return false;
            var toPlayerVector = new Vector3(entity.pos.x - target.x, entity.pos.y - target.y, entity.pos.z - target.z).normalize();
            entity.headYaw = (BVector3.getYawFromVector(toPlayerVector));
            entity.rotation.yaw = (BVector3.getYawFromVector(toPlayerVector));
            entity.rotation.pitch = (BVector3.getPitchFromVector(toPlayerVector));
            return true;
        }
    }
}