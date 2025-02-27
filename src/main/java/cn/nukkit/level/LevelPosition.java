package cn.nukkit.level;

import cn.nukkit.block.Block;
import cn.nukkit.block.BlockState;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.level.format.IChunk;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.Vector3;
import cn.nukkit.positiontracking.NamedPosition;
import cn.nukkit.utils.LevelException;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Set;

/**
 * @author MagicDroidX (Nukkit Project)
 */

@Slf4j
public class LevelPosition extends NamedPosition {
    public Level level;

    public LevelPosition() {
        this(0, 0, 0, null);
    }

    public LevelPosition(double x) {
        this(x, 0, 0, null);
    }

    public LevelPosition(double x, double y) {
        this(x, y, 0, null);
    }

    public LevelPosition(double x, double y, double z) {
        this(x, y, z, null);
    }

    public LevelPosition(double x, double y, double z, Level level) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.level = level;
    }

    public static LevelPosition fromObject(Vector3 pos) {
        return fromObject(pos, null);
    }

    public static LevelPosition fromObject(Vector3 pos, Level level) {
        return new LevelPosition(pos.x, pos.y, pos.z, level);
    }

    public Level getLevel() {
        return this.level;
    }

    public LevelPosition setLevel(Level level) {
        this.level = level;
        return this;
    }

    public boolean isValid() {
        return this.level != null;
    }

    public boolean setStrong() {
        return false;
    }

    public boolean setWeak() {
        return false;
    }

    @Override
    public LevelPosition getSide(BlockFace face) {
        return this.getSide(face, 1);
    }

    @Override
    public LevelPosition getSide(BlockFace face, int step) {
        return LevelPosition.fromObject(super.getSide(face, step), getValidLevel());
    }

    // Get as a Position for better performance. Do not override it!


    public LevelPosition getSidePos(BlockFace face) {
        return LevelPosition.fromObject(super.getSide(face, 1), getValidLevel());
    }

    @Override
    public String toString() {
        return "Position(level=" + (this.isValid() ? this.getLevel().getName() : "null") + ",x=" + this.x + ",y=" + this.y + ",z=" + this.z + ")";
    }

    @Override
    public LevelPosition setComponents(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    @Override
    public @NotNull LevelPosition setComponents(@NotNull Vector3 pos) {
        super.setComponents(pos);
        return this;
    }

    public @Nullable BlockEntity getLevelBlockEntity() {
        return getValidLevel().getBlockEntity(this);
    }

    public @Nullable final <T extends BlockEntity> T getTypedBlockEntity(@NotNull Class<T> type) {
        BlockEntity blockEntity = getValidLevel().getBlockEntity(this);
        return type.isInstance(blockEntity) ? type.cast(blockEntity) : null;
    }

    @NotNull public BlockState getLevelBlockState() {
        return getLevelBlockState(0);
    }

    @NotNull public BlockState getLevelBlockState(int layer) {
        return getValidLevel().getBlockStateAt(getFloorX(), getFloorY(), getFloorZ(), layer);
    }

    public Block getLevelBlock() {
        return getLevelBlock(true);
    }

    public Block getLevelBlock(boolean load) {
        return getValidLevel().getBlock(this, load);
    }

    public Block getLevelBlock(int layer) {
        return getValidLevel().getBlock(this, layer);
    }

    public Block getLevelBlock(int layer, boolean load) {
        return getValidLevel().getBlock(this, layer, load);
    }

    public Block getTickCachedLevelBlock() {
        return getValidLevel().getTickCachedBlock(this);
    }

    public Set<Block> getLevelBlockAround() {
        return getValidLevel().getBlockAround(this);
    }

    public Block getLevelBlockAtLayer(int layer) {
        return getValidLevel().getBlock(this, layer);
    }

    public Block getTickCachedLevelBlockAtLayer(int layer) {
        return getValidLevel().getTickCachedBlock(this, layer);
    }

    @NotNull public Transform getLocation() {
        return new Transform(this.x, this.y, this.z, 0, 0, getValidLevel());
    }

    @Override
    @NotNull public String getLevelName() {
        return getValidLevel().getName();
    }

    @NotNull public final Level getValidLevel() {
        Level level = this.level;
        if (level == null) {
            throw new LevelException("Undefined Level reference");
        }
        return level;
    }

    @Override
    public LevelPosition add(double x) {
        return this.add(x, 0, 0);
    }

    @Override
    public LevelPosition add(double x, double y) {
        return this.add(x, y, 0);
    }

    @Override
    public LevelPosition add(double x, double y, double z) {
        return new LevelPosition(this.x + x, this.y + y, this.z + z, this.level);
    }

    @Override
    public LevelPosition add(Vector3 v) {
        return new LevelPosition(this.x + v.getX(), this.y + v.getY(), this.z + v.getZ(), this.level);
    }

    @Override
    public LevelPosition subtract(double x) {
        return this.subtract(x, 0, 0);
    }

    @Override
    public LevelPosition subtract(double x, double y) {
        return this.subtract(x, y, 0);
    }

    @Override
    public LevelPosition subtract(double x, double y, double z) {
        return this.add(-x, -y, -z);
    }

    @Override
    public LevelPosition subtract(Vector3 v) {
        return this.add(-v.getX(), -v.getY(), -v.getZ());
    }

    @Override
    public LevelPosition multiply(double number) {
        return new LevelPosition(this.x * number, this.y * number, this.z * number, this.level);
    }

    @Override
    public LevelPosition divide(double number) {
        return new LevelPosition(this.x / number, this.y / number, this.z / number, this.level);
    }

    @Override
    public LevelPosition ceil() {
        return new LevelPosition((int) Math.ceil(this.x), (int) Math.ceil(this.y), (int) Math.ceil(this.z), this.level);
    }

    @Override
    public LevelPosition floor() {
        return new LevelPosition(this.getFloorX(), this.getFloorY(), this.getFloorZ(), this.level);
    }

    @Override
    public LevelPosition round() {
        return new LevelPosition(Math.round(this.x), Math.round(this.y), Math.round(this.z), this.level);
    }

    @Override
    public LevelPosition abs() {
        return new LevelPosition((int) Math.abs(this.x), (int) Math.abs(this.y), (int) Math.abs(this.z), this.level);
    }

    @Override
    public LevelPosition clone() {
        return (LevelPosition) super.clone();
    }

    public IChunk getChunk() {
        return isValid() ? level.getChunk(getChunkX(), getChunkZ()) : null;
    }
}
