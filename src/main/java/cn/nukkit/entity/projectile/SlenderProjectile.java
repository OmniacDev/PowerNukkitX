package cn.nukkit.entity.projectile;

import cn.nukkit.block.Block;
import cn.nukkit.entity.Entity;
import cn.nukkit.event.entity.ProjectileHitEvent;
import cn.nukkit.level.MovingObjectPosition;
import cn.nukkit.level.format.IChunk;
import cn.nukkit.math.BVector3;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;

import java.util.Arrays;
import java.util.Comparator;

/**
 * 这个抽象类代表较为细长的投射物实体(例如弓箭,三叉戟),它通过重写{@link Entity#move}方法实现这些实体较为准确的碰撞箱计算。
 * <p>
 * This abstract class represents slender projectile entities (e.g.arrow, trident), and it realized a more accurate collision box calculation for these entities by overriding the {@link Entity#move} method.
 */
public abstract class SlenderProjectile extends EntityProjectile {
    private static final int SPLIT_NUMBER = 10;
    private MovingObjectPosition lastHitBlock;

    public SlenderProjectile(IChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    public SlenderProjectile(IChunk chunk, CompoundTag nbt, Entity shootingEntity) {
        super(chunk, nbt, shootingEntity);
    }

    //对于SlenderProjectile你不应该把Width设置太大,如果没必要请使用默认值.
    @Override
    public float getWidth() {
        return 0.1f;
    }

    //对于SlenderProjectile你不应该把Height设置太大,如果没必要请使用默认值.
    @Override
    public float getHeight() {
        return 0.1f;
    }

    /*
     * 经过测试这个算法在大多数情况下效果不错。
     */
    @Override
    public boolean move(double dx, double dy, double dz) {
        if (dx == 0 && dz == 0 && dy == 0) {
            return true;
        }

        this.ySize *= 0.4;

        double movX = dx;
        double movY = dy;
        double movZ = dz;

        final SlenderProjectile projectile = this;
        final Entity shootEntity = shootingEntity;
        final int ticks = ticksLived;

        var currentAABB = this.boundingBox.clone();
        var dirVector = new Vector3(dx, dy, dz).multiply(1 / (double) SPLIT_NUMBER);

        Entity collisionEntity = null;
        Block collisionBlock = null;
        for (int i = 0; i < SPLIT_NUMBER; ++i) {
            var collisionBlocks = this.level.getCollisionBlocks(currentAABB.offset(dirVector.x, dirVector.y, dirVector.z));
            var collisionEntities = this.getLevel().fastCollidingEntities(currentAABB, this);
            if (collisionBlocks.length != 0) {
                currentAABB.offset(-dirVector.x, -dirVector.y, -dirVector.z);
                collisionBlock = Arrays.stream(collisionBlocks).min(Comparator.comparingDouble(block -> projectile.pos.distanceSquared(block.position))).get();
                break;
            }
            collisionEntity = collisionEntities.stream()
                    .filter(this::collideEntityFilter)
                    .min(Comparator.comparingDouble(o -> o.pos.distanceSquared(projectile.pos)))
                    .orElse(null);
            if (collisionEntity != null) {
                break;
            }
        }
        Vector3 centerPoint1 = new Vector3((currentAABB.getMinX() + currentAABB.getMaxX()) / 2,
                (currentAABB.getMinY() + currentAABB.getMaxY()) / 2,
                (currentAABB.getMinZ() + currentAABB.getMaxZ()) / 2);
        //collide with entity
        if (collisionEntity != null) {
            MovingObjectPosition movingObject = new MovingObjectPosition();
            movingObject.typeOfHit = 1;
            movingObject.entityHit = collisionEntity;
            movingObject.hitVector = centerPoint1;
            onCollideWithEntity(movingObject.entityHit);
            return true;
        }

        Vector3 centerPoint2 = new Vector3((this.boundingBox.getMinX() + this.boundingBox.getMaxX()) / 2,
                (this.boundingBox.getMinY() + this.boundingBox.getMaxY()) / 2,
                (this.boundingBox.getMinZ() + this.boundingBox.getMaxZ()) / 2);
        Vector3 diff = centerPoint1.subtract(centerPoint2);
        if (dy > 0) {
            if (diff.getY() + 0.001 < dy) {
                dy = diff.getY();
            }
        }
        if (dy < 0) {
            if (diff.getY() - 0.001 > dy) {
                dy = diff.getY();
            }
        }
        if (dx > 0) {
            if (diff.getX() + 0.001 < dx) {
                dx = diff.getX();
            }
        }
        if (dx < 0) {
            if (diff.getX() - 0.001 > dx) {
                dx = diff.getX();
            }
        }
        if (dz > 0) {
            if (diff.getZ() + 0.001 < dz) {
                dz = diff.getZ();
            }
        }
        if (dz < 0) {
            if (diff.getZ() - 0.001 > dz) {
                dz = diff.getZ();
            }
        }
        this.boundingBox.offset(0, dy, 0);
        this.boundingBox.offset(dx, 0, 0);
        this.boundingBox.offset(0, 0, dz);
        this.pos.x = (this.boundingBox.getMinX() + this.boundingBox.getMaxX()) / 2;
        this.pos.y = this.boundingBox.getMinY() - this.ySize;
        this.pos.z = (this.boundingBox.getMinZ() + this.boundingBox.getMaxZ()) / 2;

        this.checkChunks();

        this.checkGroundState(movX, movY, movZ, dx, dy, dz);
        this.updateFallState(this.onGround);

        if (movX != dx) {
            this.motion.x = 0;
        }
        if (movY != dy) {
            this.motion.y = 0;
        }
        if (movZ != dz) {
            this.motion.z = 0;
        }

        //collide with block
        if (this.isCollided && !this.hadCollision) {
            this.hadCollision = true;
            this.motion.x = 0;
            this.motion.y = 0;
            this.motion.z = 0;
            BVector3 bVector3 = BVector3.fromPos(new Vector3(dx, dy, dz));
            BlockFace blockFace = BlockFace.fromHorizontalAngle(bVector3.getYaw());
            Block block = level.getBlock(this.pos.getFloorX(), this.pos.getFloorY(), this.pos.getFloorZ()).getSide(blockFace);
            if (block.isAir()) {
                blockFace = BlockFace.DOWN;
                block = level.getBlock(this.pos.getFloorX(), this.pos.getFloorY(), this.pos.getFloorZ()).down();
            }
            if (block.isAir()) {
                blockFace = BlockFace.UP;
                block = level.getBlock(this.pos.getFloorX(), this.pos.getFloorY(), this.pos.getFloorZ()).up();
            }
            if (block.isAir() && collisionBlock != null) {
                block = collisionBlock;
            }
            this.server.getPluginManager().callEvent(new ProjectileHitEvent(this, lastHitBlock = MovingObjectPosition.fromBlock(block.position.getFloorX(), block.position.getFloorY(), block.position.getFloorZ(), blockFace, this.pos)));
            onCollideWithBlock(getLocator(), getMotion());
            addHitEffect();
        }
        return true;
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

        if (this.isCollided && this.hadCollision) {
            if (lastHitBlock != null && lastHitBlock.typeOfHit == 0 && level.getBlock(lastHitBlock.blockX, lastHitBlock.blockY, lastHitBlock.blockZ).isAir()) {
                this.motion.y -= this.getGravity();
                updateRotation();
                this.move(this.motion.x, this.motion.y, this.motion.z);
                this.updateMovement();
            }
            return this.entityBaseTick(tickDiff);
        }

        boolean hasUpdate = this.entityBaseTick(tickDiff);

        if (this.isAlive()) {
            if (!this.isCollided) {
                updateMotion();
            }
            if (!this.hadCollision || Math.abs(this.motion.x) > 0.00001 || Math.abs(this.motion.y) > 0.00001 || Math.abs(this.motion.z) > 0.00001) {
                updateRotation();
                hasUpdate = true;
            }
            this.move(this.motion.x, this.motion.y, this.motion.z);
            this.updateMovement();
        }
        return hasUpdate;
    }
}
