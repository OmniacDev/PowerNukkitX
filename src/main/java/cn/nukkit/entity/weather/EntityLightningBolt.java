package cn.nukkit.entity.weather;

import cn.nukkit.block.Block;
import cn.nukkit.block.BlockFire;
import cn.nukkit.block.BlockID;
import cn.nukkit.block.Oxidizable;
import cn.nukkit.block.Waxable;
import cn.nukkit.block.property.enums.OxidizationLevel;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.block.BlockFadeEvent;
import cn.nukkit.event.block.BlockIgniteEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.level.GameRule;
import cn.nukkit.level.Locator;
import cn.nukkit.level.Sound;
import cn.nukkit.level.format.IChunk;
import cn.nukkit.level.particle.ElectricSparkParticle;
import cn.nukkit.level.vibration.VibrationEvent;
import cn.nukkit.level.vibration.VibrationType;
import cn.nukkit.math.AxisAlignedBB;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.IntConsumer;
import java.util.function.Supplier;

/**
 * @author boybook
 * @since 2016/2/27
 */
public class EntityLightningBolt extends Entity implements EntityLightningStrike {
    @Override
    @NotNull public String getIdentifier() {
        return LIGHTNING_BOLT;
    }
    
    public int state;
    public int liveTime;
    protected boolean isEffect = true;

    public EntityLightningBolt(IChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    private static boolean isVulnerableOxidizable(@NotNull Block block) {
        return block instanceof Oxidizable && (!(block instanceof Waxable) || !((Waxable) block).isWaxed());
    }

    @Override
    protected void initEntity() {
        super.initEntity();

        this.setHealth(4);
        this.setMaxHealth(4);

        this.state = 2;
        this.liveTime = ThreadLocalRandom.current().nextInt(3) + 1;

        if (isEffect && this.level.gameRules.getBoolean(GameRule.DO_FIRE_TICK) && (this.server.getDifficulty() >= 2)) {
            Block block = this.getLocator().getLevelBlock();
            if (block.isAir() || block.getId().equals(BlockID.TALL_GRASS)) {
                BlockFire fire = (BlockFire) Block.get(BlockID.FIRE);
                fire.position = block.position.clone();
                fire.level = level;
//                this.getLevel().setBlock(fire, fire, true); WTF???
                if (fire.isBlockTopFacingSurfaceSolid(fire.down()) || fire.canNeighborBurn()) {

                    BlockIgniteEvent e = new BlockIgniteEvent(block, null, this, BlockIgniteEvent.BlockIgniteCause.LIGHTNING);
                    getServer().getPluginManager().callEvent(e);

                    if (!e.isCancelled()) {
                        level.setBlock(fire.position, fire, true);
                        level.scheduleUpdate(fire, fire.tickRate() + ThreadLocalRandom.current().nextInt(10));
                    }
                }
            }
        }
    }

    @Override
    public boolean isEffect() {
        return this.isEffect;
    }

    @Override
    public void setEffect(boolean e) {
        this.isEffect = e;
    }

    @Override
    public boolean attack(EntityDamageEvent source) {
        //false?
        source.setDamage(0);
        return super.attack(source);
    }

    @Override
    public boolean onUpdate(int currentTick) {
        if (this.closed) {
            return false;
        }

        int tickDiff = currentTick - this.lastUpdate;

        if (tickDiff <= 0 && !this.justCreated) {
            return true;
        }

        this.lastUpdate = currentTick;

        this.entityBaseTick(tickDiff);

        if (this.state == 2) {
            this.level.addSound(this.position, Sound.AMBIENT_WEATHER_THUNDER);
            this.level.addSound(this.position, Sound.RANDOM_EXPLODE);

            Block down = this.level.getBlock(this.position.down());
            if (isVulnerableOxidizable(down)) {
                Map<Locator, OxidizationLevel> changes = new LinkedHashMap<>();
                changes.put(new Locator(down.getVector3(), level), OxidizationLevel.UNAFFECTED);

                ThreadLocalRandom random = ThreadLocalRandom.current();
                int scans = random.nextInt(3) + 3;

                Vector3 directionPos = new Vector3();
                Vector3 randomPos = new Vector3();
                Supplier<Vector3> cleanOxidizationAround = () -> {
                    for (int attempt = 0; attempt < 10; attempt++) {
                        randomPos.x = directionPos.x + (random.nextInt(3) - 1);
                        randomPos.y = directionPos.y + (random.nextInt(3) - 1);
                        randomPos.z = directionPos.z + (random.nextInt(3) - 1);
                        Block possibility = level.getBlock(randomPos);
                        if (isVulnerableOxidizable(possibility)) {
                            Vector3 nextPos = randomPos.clone();
                            changes.compute(new Locator(nextPos, level), (k, v) -> {
                                int nextLevel = v == null ?
                                        ((Oxidizable) possibility).getOxidizationLevel().ordinal() - 1 :
                                        v.ordinal() - 1;
                                return OxidizationLevel.values()[Math.max(0, nextLevel)];
                            });
                            return nextPos;
                        }
                    }
                    return null;
                };

                IntConsumer cleanOxidizationAroundLoop = count -> {
                    directionPos.setComponents(down.position);
                    for (int i = 0; i < count; ++i) {
                        Vector3 next = cleanOxidizationAround.get();
                        if (next != null) {
                            directionPos.setComponents(next);
                        } else {
                            break;
                        }
                    }
                };

                for (int scan = 0; scan < scans; ++scan) {
                    int count = random.nextInt(8) + 1;
                    cleanOxidizationAroundLoop.accept(count);
                }

                for (Map.Entry<Locator, OxidizationLevel> entry : changes.entrySet()) {
                    Block current = level.getBlock(entry.getKey().position);
                    Block next = ((Oxidizable) current).getBlockWithOxidizationLevel(entry.getValue());
                    BlockFadeEvent event = new BlockFadeEvent(current, next);
                    getServer().getPluginManager().callEvent(event);
                    if (event.isCancelled()) {
                        break;
                    }
                    this.level.setBlock(entry.getKey().position, event.getNewState());
                    this.level.addParticle(new ElectricSparkParticle(entry.getKey().position));
                }
            }
        }

        this.state--;

        if (this.state < 0) {
            if (this.liveTime == 0) {
                this.close();
                return false;
            } else if (this.state < -ThreadLocalRandom.current().nextInt(10)) {
                this.liveTime--;
                this.state = 1;

                if (this.isEffect && this.level.gameRules.getBoolean(GameRule.DO_FIRE_TICK)) {
                    Block block = this.getLocator().getLevelBlock();

                    if (block.isAir() || block.getId().equals(Block.TALL_GRASS)) {
                        BlockIgniteEvent event = new BlockIgniteEvent(block, null, this, BlockIgniteEvent.BlockIgniteCause.LIGHTNING);
                        getServer().getPluginManager().callEvent(event);

                        if (!event.isCancelled()) {
                            Block fire = Block.get(BlockID.FIRE);
                            this.level.setBlock(block.position, fire);
                            this.level.scheduleUpdate(fire, fire.tickRate());
                        }
                    }
                }
            }
        }

        if (this.state >= 0) {
            if (this.isEffect) {
                AxisAlignedBB bb = getBoundingBox().grow(3, 3, 3);
                bb.setMaxX(bb.getMaxX() + 6);

                for (Entity entity : this.level.getCollidingEntities(bb, this)) {
                    entity.onStruckByLightning(this);
                }
            }
        }

        return true;
    }

    @Override
    public String getOriginalName() {
        return "Lightning Bolt";
    }

    @Override
    public void spawnToAll() {
        this.level.getVibrationManager().callVibrationEvent(new VibrationEvent(this, this.getVector3(), VibrationType.LIGHTNING_STRIKE));
        super.spawnToAll();
    }
}
