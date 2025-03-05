package cn.nukkit.entity.projectile;

import cn.nukkit.Player;
import cn.nukkit.block.Block;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityLiving;
import cn.nukkit.entity.item.EntityBoat;
import cn.nukkit.entity.item.EntityEnderCrystal;
import cn.nukkit.entity.item.EntityMinecartAbstract;
import cn.nukkit.event.entity.EntityCombustByEntityEvent;
import cn.nukkit.event.entity.EntityDamageByChildEntityEvent;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.entity.EntityDamageEvent.DamageCause;
import cn.nukkit.event.entity.ProjectileHitEvent;
import cn.nukkit.level.MovingObjectPosition;
import cn.nukkit.level.Locator;
import cn.nukkit.level.format.IChunk;
import cn.nukkit.level.vibration.VibrationEvent;
import cn.nukkit.level.vibration.VibrationType;
import cn.nukkit.math.AxisAlignedBB;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.NukkitMath;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author MagicDroidX (Nukkit Project)
 */
public abstract class EntityProjectile extends Entity {
    public static final int PICKUP_NONE = 0;
    public static final int PICKUP_ANY = 1;
    public static final int PICKUP_CREATIVE = 2;

    public Entity shootingEntity;
    public boolean hadCollision;
    public boolean closeOnCollide;
    /**
     * It's inverted from {@link #getHasAge()} because of the poor architecture chosen by the original devs
     * on the entity construction and initialization. It's impossible to set it to true before
     * the initialization of the child classes.
     */
    private boolean noAge;

    public EntityProjectile(IChunk chunk, CompoundTag nbt) {
        this(chunk, nbt, null);
    }

    public EntityProjectile(IChunk chunk, CompoundTag nbt, Entity shootingEntity) {
        super(chunk, nbt);
        this.shootingEntity = shootingEntity;
        if (shootingEntity != null) {
            this.setDataProperty(OWNER_EID, shootingEntity.getId());
        }
    }

    protected double getDamage() {
        return namedTag.contains("damage") ? namedTag.getDouble("damage") : getBaseDamage();
    }

    protected double getBaseDamage() {
        return 0;
    }

    public int getResultDamage(@Nullable Entity entity) {
        return getResultDamage();
    }

    public int getResultDamage() {
        return NukkitMath.ceilDouble(Math.sqrt(this.motion.x * this.motion.x + this.motion.y * this.motion.y + this.motion.z * this.motion.z) * getDamage());
    }

    @Override
    public boolean attack(EntityDamageEvent source) {
        return source.getCause() == DamageCause.VOID && super.attack(source);
    }

    public void onCollideWithEntity(Entity entity) {
        ProjectileHitEvent projectileHitEvent = new ProjectileHitEvent(this, MovingObjectPosition.fromEntity(entity));
        this.server.getPluginManager().callEvent(projectileHitEvent);

        if (projectileHitEvent.isCancelled()) {
            return;
        }

        this.level.getVibrationManager().callVibrationEvent(new VibrationEvent(this, this.getVector3(), VibrationType.PROJECTILE_LAND));

        float damage = this.getResultDamage(entity);

        EntityDamageEvent ev;
        if (this.shootingEntity == null) {
            ev = new EntityDamageByEntityEvent(this, entity, DamageCause.PROJECTILE, damage);
        } else {
            ev = new EntityDamageByChildEntityEvent(this.shootingEntity, this, entity, DamageCause.PROJECTILE, damage);
        }
        if (entity.attack(ev)) {
            addHitEffect();
            this.hadCollision = true;

            if (this.fireTicks > 0) {
                EntityCombustByEntityEvent event = new EntityCombustByEntityEvent(this, entity, 5);
                this.server.getPluginManager().callEvent(event);
                if (!event.isCancelled()) {
                    entity.setOnFire(event.getDuration());
                }
            }
        }
        afterCollisionWithEntity(entity);
        if (closeOnCollide) {
            this.close();
        }
    }

    protected void afterCollisionWithEntity(Entity entity) {

    }

    @Override
    protected void initEntity() {
        this.closeOnCollide = true;
        super.initEntity();

        this.setMaxHealth(1);
        this.setHealth(1);
        if (this.namedTag.contains("Age") && !this.noAge) {
            this.age = this.namedTag.getShort("Age");
        }
    }

    @Override
    public boolean canCollideWith(Entity entity) {
        return (entity instanceof EntityLiving || entity instanceof EntityEnderCrystal || entity instanceof EntityMinecartAbstract || entity instanceof EntityBoat) && !this.onGround;
    }

    @Override
    public void saveNBT() {
        super.saveNBT();
        if (!this.noAge) {
            this.namedTag.putShort("Age", this.age);
        }
    }

    protected void updateMotion() {
        this.motion.y -= this.getGravity();
        this.motion.x *= 1 - this.getDrag();
        this.motion.z *= 1 - this.getDrag();
    }

    /**
     * Filters the entities that collide with projectile
     *
     * @param entity the collide entity
     * @return the boolean
     */
    protected boolean collideEntityFilter(Entity entity) {
        if ((entity == this.shootingEntity && this.ticksLived < 5) ||
                (entity instanceof Player player && player.getGamemode() == Player.SPECTATOR)
                || (this.shootingEntity instanceof Player && Optional.ofNullable(this.shootingEntity.riding).map(e -> e.equals(entity)).orElse(false))) {
            return false;
        } else return true;
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

        boolean hasUpdate = this.entityBaseTick(tickDiff);

        if (this.isAlive()) {

            MovingObjectPosition movingObjectPosition = null;

            if (!this.isCollided) {
                updateMotion();
            }

            Vector3 moveVector = new Vector3(this.pos.x + this.motion.x, this.pos.y + this.motion.y, this.pos.z + this.motion.z);

            Entity[] list = this.getLevel().getCollidingEntities(this.getBoundingBox().addCoord(this.motion.x, this.motion.y, this.motion.z).expand(1, 1, 1), this);

            double nearDistance = Integer.MAX_VALUE;
            Entity nearEntity = null;

            for (Entity entity : list) {
                if (!collideEntityFilter(entity)) {
                    continue;
                }

                AxisAlignedBB axisalignedbb = entity.getBoundingBox().grow(0.3, 0.3, 0.3);
                MovingObjectPosition ob = axisalignedbb.calculateIntercept(this.pos, moveVector);

                if (ob == null) {
                    continue;
                }

                double distance = this.pos.distanceSquared(ob.hitVector);

                if (distance < nearDistance) {
                    nearDistance = distance;
                    nearEntity = entity;
                }
            }

            if (nearEntity != null) {
                movingObjectPosition = MovingObjectPosition.fromEntity(nearEntity);
            }

            if (movingObjectPosition != null) {
                if (movingObjectPosition.entityHit != null) {
                    onCollideWithEntity(movingObjectPosition.entityHit);
                    hasUpdate = true;
                    if (closed) {
                        return true;
                    }
                }
            }

            Locator locator = getLocator();
            Vector3 motion = getMotion();
            this.move(this.motion.x, this.motion.y, this.motion.z);

            if (this.isCollided && !this.hadCollision) { //collide with block
                this.hadCollision = true;

                this.motion.x = 0;
                this.motion.y = 0;
                this.motion.z = 0;

                this.server.getPluginManager().callEvent(new ProjectileHitEvent(this, MovingObjectPosition.fromBlock(this.pos.getFloorX(), this.pos.getFloorY(), this.pos.getFloorZ(), BlockFace.UP, this.pos)));
                onCollideWithBlock(locator, motion);
                addHitEffect();
                return false;
            } else if (!this.isCollided && this.hadCollision) {
                this.hadCollision = false;
            }

            if (!this.hadCollision || Math.abs(this.motion.x) > 0.00001 || Math.abs(this.motion.y) > 0.00001 || Math.abs(this.motion.z) > 0.00001) {
                updateRotation();
                hasUpdate = true;
            }

            this.updateMovement();
        }

        return hasUpdate;
    }

    public void updateRotation() {
        double f = Math.sqrt((this.motion.x * this.motion.x) + (this.motion.z * this.motion.z));
        this.rotation.yaw = Math.atan2(this.motion.x, this.motion.z) * 180 / Math.PI;
        this.rotation.pitch = Math.atan2(this.motion.y, f) * 180 / Math.PI;
    }

    public void inaccurate(float modifier) {
        Random rand = ThreadLocalRandom.current();

        this.motion.x += rand.nextGaussian() * 0.007499999832361937 * modifier;
        this.motion.y += rand.nextGaussian() * 0.007499999832361937 * modifier;
        this.motion.z += rand.nextGaussian() * 0.007499999832361937 * modifier;
    }

    protected void onCollideWithBlock(Locator locator, Vector3 motion) {
        this.level.getVibrationManager().callVibrationEvent(new VibrationEvent(this, this.getVector3(), VibrationType.PROJECTILE_LAND));
        for (Block collisionBlock : level.getCollisionBlocks(getBoundingBox().grow(0.1, 0.1, 0.1))) {
            onCollideWithBlock(locator, motion, collisionBlock);
        }
    }

    protected boolean onCollideWithBlock(Locator locator, Vector3 motion, Block collisionBlock) {
        return collisionBlock.onProjectileHit(this, locator, motion);
    }

    protected void addHitEffect() {

    }

    public boolean getHasAge() {
        return !this.noAge;
    }

    public void setHasAge(boolean hasAge) {
        this.noAge = !hasAge;
    }

    @Override
    public void spawnToAll() {
        super.spawnToAll();
        //vibration: minecraft:projectile_shoot
        this.level.getVibrationManager().callVibrationEvent(new VibrationEvent(this.shootingEntity, this.getVector3(), VibrationType.PROJECTILE_SHOOT));
    }
}
