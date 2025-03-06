package cn.nukkit.entity.item;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.projectile.SlenderProjectile;
import cn.nukkit.event.entity.EntityDamageByChildEntityEvent;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.entity.EntityDamageEvent.DamageCause;
import cn.nukkit.event.entity.ProjectileHitEvent;
import cn.nukkit.event.player.PlayerFishEvent;
import cn.nukkit.item.Item;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.item.randomitem.Fishing;
import cn.nukkit.level.MovingObjectPosition;
import cn.nukkit.level.format.IChunk;
import cn.nukkit.level.particle.BubbleParticle;
import cn.nukkit.level.particle.WaterParticle;
import cn.nukkit.math.Vector3;
import cn.nukkit.math.Vector3f;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.AddEntityPacket;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.EntityEventPacket;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;


/**
 * @author PetteriM1
 */
public class EntityFishingHook extends SlenderProjectile {
    @Override
    @NotNull
    public String getIdentifier() {
        return FISHING_HOOK;
    }

    public int waitChance = 120;
    public int waitTimer = 240;
    public boolean attracted = false;
    public int attractTimer = 0;
    public boolean caught = false;
    public int caughtTimer = 0;
    @SuppressWarnings("java:S1845")
    public boolean canCollide = true;

    public Vector3 fish = null;

    public Item rod = null;

    public EntityFishingHook(IChunk chunk, CompoundTag nbt) {
        this(chunk, nbt, null);
    }

    public EntityFishingHook(IChunk chunk, CompoundTag nbt, Entity shootingEntity) {
        super(chunk, nbt, shootingEntity);
        // https://github.com/PowerNukkit/PowerNukkit/issues/267
        if (this.age > 0) {
            this.close();
        }
    }

    @Override
    public float getLength() {
        return 0.2f;
    }

    @Override
    public float getGravity() {
        return 0.05f;
    }

    @Override
    public float getDrag() {
        return 0.04f;
    }

    @Override
    public boolean canCollide() {
        return this.canCollide;
    }

    @Override
    public boolean onUpdate(int currentTick) {
        boolean hasUpdate;
        long target = getDataProperty(TARGET_EID);
        if (target != 0L) {
            Entity entity = this.level.getEntity(target);
            if (entity == null || !entity.isAlive()) {
                setDataProperty(TARGET_EID, 0L);
            } else {
                Vector3f offset = entity.getMountedOffset(this);
                setPosition(new Vector3(entity.position.x + offset.x, entity.position.y + offset.y, entity.position.z + offset.z));
            }
            return false;
        }

        hasUpdate = super.onUpdate(currentTick);

        boolean inWater = this.isInsideOfWater();
        if (inWater) {//防止鱼钩沉底 水中的阻力
            this.motion.x = 0;
            this.motion.y -= getGravity() * -0.04;
            this.motion.z = 0;
            hasUpdate = true;
        }

        if (inWater) {
            if (this.waitTimer == 240) {
                this.waitTimer = this.waitChance << 1;
            } else if (this.waitTimer == 360) {
                this.waitTimer = this.waitChance * 3;
            }
            if (!this.attracted) {
                if (this.waitTimer > 0) {
                    --this.waitTimer;
                }
                if (this.waitTimer == 0) {
                    ThreadLocalRandom random = ThreadLocalRandom.current();
                    if (random.nextInt(100) < 90) {
                        this.attractTimer = (random.nextInt(40) + 20);
                        this.spawnFish();
                        this.caught = false;
                        this.attracted = true;
                    } else {
                        this.waitTimer = this.waitChance;
                    }
                }
            } else if (!this.caught) {
                if (this.attractFish()) {
                    this.caughtTimer = (ThreadLocalRandom.current().nextInt(20) + 30);
                    this.fishBites();
                    this.caught = true;
                }
            } else {
                if (this.caughtTimer > 0) {
                    --this.caughtTimer;
                }
                if (this.caughtTimer == 0) {
                    this.attracted = false;
                    this.caught = false;
                    this.waitTimer = this.waitChance * 3;
                }
            }
        }
        return hasUpdate;
    }

    @Override
    protected void updateMotion() {
        //正确的浮力
        if (this.isInsideOfWater() && this.getY() < this.getWaterHeight() - 2) {
            this.motion.x = 0;
            this.motion.y += getGravity();
            this.motion.z = 0;
        } else if (this.isInsideOfWater() && this.getY() >= this.getWaterHeight() - 2) {//防止鱼钩上浮超出水面
            this.motion.x = 0;
            this.motion.z = 0;
            this.motion.y = 0;
        } else {//处理不在水中的情况
            super.updateMotion();
        }
    }

    public int getWaterHeight() {
        for (int y = this.position.getFloorY(); y <= level.getMaxHeight(); y++) {
            String id = this.level.getBlockIdAt(this.position.getFloorX(), y, this.position.getFloorZ());
            if (Objects.equals(id, Block.AIR)) {
                return y;
            }
        }
        return this.position.getFloorY();
    }

    public void fishBites() {
        Collection<Player> viewers = this.getViewers().values();

        EntityEventPacket pk = new EntityEventPacket();
        pk.eid = this.getId();
        pk.event = EntityEventPacket.FISH_HOOK_HOOK;
        Server.broadcastPacket(viewers, pk);

        EntityEventPacket bubblePk = new EntityEventPacket();
        bubblePk.eid = this.getId();
        bubblePk.event = EntityEventPacket.FISH_HOOK_BUBBLE;
        Server.broadcastPacket(viewers, bubblePk);

        EntityEventPacket teasePk = new EntityEventPacket();
        teasePk.eid = this.getId();
        teasePk.event = EntityEventPacket.FISH_HOOK_TEASE;
        Server.broadcastPacket(viewers, teasePk);

        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (int i = 0; i < 5; i++) {
            this.level.addParticle(new BubbleParticle(this.position.setComponents(
                    this.position.x + random.nextDouble() * 0.5 - 0.25,
                    this.getWaterHeight(),
                    this.position.z + random.nextDouble() * 0.5 - 0.25
            )));
        }
    }

    public void spawnFish() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        this.fish = new Vector3(
                this.position.x + (random.nextDouble() * 1.2 + 1) * (random.nextBoolean() ? -1 : 1),
                this.getWaterHeight(),
                this.position.z + (random.nextDouble() * 1.2 + 1) * (random.nextBoolean() ? -1 : 1)
        );
    }

    public boolean attractFish() {
        double multiply = 0.1;
        this.fish.setComponents(
                this.fish.x + (this.position.x - this.fish.x) * multiply,
                this.fish.y,
                this.fish.z + (this.position.z - this.fish.z) * multiply
        );
        if (ThreadLocalRandom.current().nextInt(100) < 85) {
            this.level.addParticle(new WaterParticle(this.fish));
        }
        double dist = Math.abs(Math.sqrt(this.position.x * this.position.x + this.position.z * this.position.z) - Math.sqrt(this.fish.x * this.fish.x + this.fish.z * this.fish.z));
        return dist < 0.15;
    }

    public void reelLine() {
        if (this.shootingEntity instanceof Player player && this.caught) {
            Item item = Fishing.getFishingResult(this.rod);
            int experience = ThreadLocalRandom.current().nextInt(3) + 1;
            Vector3 pos = new Vector3(this.position.x, this.getWaterHeight(), this.position.z); //实体生成在水面上
            Vector3 motion = player.position.subtract(pos).multiply(0.1);
            motion.y += Math.sqrt(player.position.distance(pos)) * 0.08;

            PlayerFishEvent event = new PlayerFishEvent(player, this, item, experience, motion);
            this.getServer().getPluginManager().callEvent(event);

            if (!event.isCancelled()) {
                EntityItem itemEntity = (EntityItem) Entity.createEntity(Entity.ITEM,
                        this.level.getChunk((int) this.position.x >> 4, (int) this.position.z >> 4, true),
                        Entity.getDefaultNBT(
                                        pos,
                                        event.getMotion(), ThreadLocalRandom.current().nextFloat() * 360,
                                        0
                                ).putCompound("Item", NBTIO.putItemHelper(event.getLoot()))
                                .putShort("Health", 5)
                                .putShort("PickupDelay", 1));

                if (itemEntity != null) {
                    itemEntity.setOwner(player.getName());
                    itemEntity.spawnToAll();
                    player.level.dropExpOrb(player.position, event.getExperience());
                }
            }
        } else if (this.shootingEntity != null) {
            var eid = this.getDataProperty(TARGET_EID);
            var targetEntity = this.level.getEntity(eid);
            if (targetEntity != null && targetEntity.isAlive()) { // 钓鱼竿收杆应该牵引被钓生物
                targetEntity.setMotion(this.shootingEntity.position.subtract(targetEntity.position).divide(8).add(0, 0.3, 0));
            }
        }
        this.close();
    }

    @Override
    protected DataPacket createAddEntityPacket() {
        AddEntityPacket pk = new AddEntityPacket();
        pk.entityRuntimeId = this.getId();
        pk.entityUniqueId = this.getId();
        pk.type = getNetworkId();
        pk.x = (float) this.position.x;
        pk.y = (float) this.position.y;
        pk.z = (float) this.position.z;
        pk.speedX = (float) this.motion.x;
        pk.speedY = (float) this.motion.y;
        pk.speedZ = (float) this.motion.z;
        pk.yaw = (float) this.rotation.yaw;
        pk.pitch = (float) this.rotation.pitch;

        long ownerId = -1;
        if (this.shootingEntity != null) {
            ownerId = this.shootingEntity.getId();
        }
        this.entityDataMap.put(OWNER_EID, ownerId);
        pk.entityData = entityDataMap;
        return pk;
    }



    @Override
    public void onCollideWithEntity(Entity entity) {
        this.server.getPluginManager().callEvent(new ProjectileHitEvent(this, MovingObjectPosition.fromEntity(entity)));
        float damage = this.getResultDamage();

        EntityDamageEvent ev;
        if (this.shootingEntity == null) {
            ev = new EntityDamageByEntityEvent(this, entity, DamageCause.PROJECTILE, damage);
        } else {
            ev = new EntityDamageByChildEntityEvent(this.shootingEntity, this, entity, DamageCause.PROJECTILE, damage);
        }

        if (entity.attack(ev)) {
            this.setTarget(entity.getId());
        }
    }

    public void checkLure() {
        if (rod != null) {
            Enchantment ench = rod.getEnchantment(Enchantment.ID_LURE);
            if (ench != null) {
                this.waitChance = 120 - (25 * ench.getLevel());
            }
        }
    }

    public void setTarget(long eid) {
        this.setDataProperty(TARGET_EID, eid);
        this.canCollide = eid == 0;
    }

    @Override
    public String getOriginalName() {
        return "Fishing Hook";
    }
}
