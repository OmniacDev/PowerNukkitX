package cn.nukkit.entity;

import cn.nukkit.level.format.IChunk;
import cn.nukkit.math.BlockFace;
import cn.nukkit.nbt.tag.CompoundTag;

/**
 * @author MagicDroidX (Nukkit Project)
 */
public abstract class EntityHanging extends Entity {
    protected int direction;

    public EntityHanging(IChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    protected void initEntity() {
        super.initEntity();

        this.setMaxHealth(1);
        this.setHealth(1);

        if (this.namedTag.contains("Direction")) {
            this.direction = this.namedTag.getByte("Direction");
        } else if (this.namedTag.contains("Dir")) {
            int d = this.namedTag.getByte("Dir");
            if (d == 2) {
                this.direction = 0;
            } else if (d == 0) {
                this.direction = 2;
            }
        }

    }

    @Override
    public void saveNBT() {
        super.saveNBT();

        this.namedTag.putByte("Direction", this.getDirection().getHorizontalIndex());
        this.namedTag.putInt("TileX", (int) this.position.x);
        this.namedTag.putInt("TileY", (int) this.position.y);
        this.namedTag.putInt("TileZ", (int) this.position.z);
    }

    @Override
    public BlockFace getDirection() {
        return BlockFace.fromIndex(this.direction);
    }

    @Override
    public boolean onUpdate(int currentTick) {
        if (this.closed) {
            return false;
        }

        if (!this.isPlayer) {
            this.blocksAround = null;
            this.collisionBlocks = null;
        }

        if (!this.isAlive()) {
            this.despawnFromAll();
            if (!this.isPlayer) {
                this.close();
            }
            return true;
        }

        this.checkBlockCollision();

        if (this.prevRotation.yaw != this.rotation.yaw || this.prevPos.x != this.position.x || this.prevPos.y != this.position.y || this.prevPos.z != this.position.z) {
            this.despawnFromAll();
            this.direction = (int) (this.rotation.yaw / 90);
            this.prevRotation.yaw = this.rotation.yaw;
            this.prevPos.x = this.position.x;
            this.prevPos.y = this.position.y;
            this.prevPos.z = this.position.z;
            this.spawnToAll();
            return true;
        }

        return false;
    }

    protected boolean isSurfaceValid() {
        return true;
    }

}
