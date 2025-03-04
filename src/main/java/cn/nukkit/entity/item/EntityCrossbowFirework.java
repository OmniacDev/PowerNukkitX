package cn.nukkit.entity.item;

import cn.nukkit.Server;
import cn.nukkit.level.format.IChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.EntityEventPacket;

import java.util.Random;

public class EntityCrossbowFirework extends EntityFireworksRocket {
    private static final Random RANDOM = new Random();
    private final int lifetime;
    private int fireworkAge = 0;

    public EntityCrossbowFirework(IChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
        this.lifetime = 10 + RANDOM.nextInt(13);
    }

    public boolean onUpdate(int currentTick) {
        if (this.closed) {
            return false;
        } else {
            int tickDiff = currentTick - this.lastUpdate;
            if (tickDiff <= 0 && !this.justCreated) {
                return true;
            } else {
                this.lastUpdate = currentTick;
                boolean hasUpdate = this.entityBaseTick(tickDiff);
                if (this.isAlive()) {
                    this.motion.x *= 1.15D;
                    this.motion.z *= 1.15D;
                    this.move(this.motion.x, this.motion.y, this.motion.z);
                    this.updateMovement();
                    float f = (float) Math.sqrt(this.motion.x * this.motion.x + this.motion.z * this.motion.z);
                    this.rotation.yaw = (float) (Math.atan2(this.motion.x, this.motion.z) * 57.29577951308232D);
                    this.rotation.pitch = (float) (Math.atan2(this.motion.y, f) * 57.29577951308232D);
                    if (this.fireworkAge == 0) {
                        this.getLevel().addLevelSoundEvent(this.pos, 56);
                    }

                    ++this.fireworkAge;
                    hasUpdate = true;
                    if (this.fireworkAge >= this.lifetime) {
                        EntityEventPacket pk = new EntityEventPacket();
                        pk.data = 0;
                        pk.event = 25;
                        pk.eid = this.getId();
                        this.level.addLevelSoundEvent(this.pos, 58, -1, 72);
                        Server.broadcastPacket(this.getViewers().values(), pk);
                        this.kill();
                    }
                }
                return hasUpdate || !this.onGround || Math.abs(this.motion.x) > 1.0E-5D || Math.abs(this.motion.y) > 1.0E-5D || Math.abs(this.motion.z) > 1.0E-5D;
            }
        }
    }
}
