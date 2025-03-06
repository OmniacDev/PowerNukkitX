package cn.nukkit.entity.projectile.throwable;

import cn.nukkit.entity.Entity;
import cn.nukkit.level.Sound;
import cn.nukkit.level.format.IChunk;
import cn.nukkit.level.particle.Particle;
import cn.nukkit.level.particle.SpellParticle;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.LevelSoundEventPacket;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @author xtypr
 */
public class EntityXpBottle extends EntityThrowable {
    @Override
    @NotNull public String getIdentifier() {
        return XP_BOTTLE;
    }
    

    public EntityXpBottle(IChunk chunk, CompoundTag nbt) {
        this(chunk, nbt, null);
    }

    public EntityXpBottle(IChunk chunk, CompoundTag nbt, Entity shootingEntity) {
        super(chunk, nbt, shootingEntity);
    }

    

    @Override
    public float getWidth() {
        return 0.25f;
    }

    @Override
    public float getLength() {
        return 0.25f;
    }

    @Override
    public float getHeight() {
        return 0.25f;
    }

    @Override
    protected float getGravity() {
        return 0.1f;
    }

    @Override
    protected float getDrag() {
        return 0.01f;
    }

    @Override
    public boolean onUpdate(int currentTick) {
        if (this.closed) {
            return false;
        }

        boolean hasUpdate = super.onUpdate(currentTick);

        if (this.age > 1200) {
            this.kill();
            hasUpdate = true;
        }

        if (this.isCollided) {
            this.kill();
            this.dropXp();
            hasUpdate = true;
        }

        return hasUpdate;
    }

    @Override
    public void onCollideWithEntity(Entity entity) {
        this.kill();
        this.dropXp();
    }

    public void dropXp() {
        Particle particle2 = new SpellParticle(this.position, 0x00385dc6);
        this.level.addParticle(particle2);

        this.level.addLevelSoundEvent(this.position, LevelSoundEventPacket.SOUND_GLASS);

        this.level.dropExpOrb(this.position, ThreadLocalRandom.current().nextInt(3, 12));
    }

    @Override
    protected void addHitEffect() {
        this.level.addSound(this.position, Sound.RANDOM_GLASS);
    }

    @Override
    public String getOriginalName() {
        return "Bottle o' Enchanting";
    }
}
