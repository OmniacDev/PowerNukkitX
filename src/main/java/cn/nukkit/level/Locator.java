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
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Set;

/**
 * @author MagicDroidX (Nukkit Project)
 */

@Slf4j
public class Locator extends NamedPosition {
    public @Nullable Level level;

    public Locator() {
        this(0, 0, 0, null);
    }

    public Locator(double x) {
        this(x, 0, 0, null);
    }

    public Locator(double x, double y) {
        this(x, y, 0, null);
    }

    public Locator(double x, double y, double z) {
        this(x, y, z, null);
    }

    public Locator(double x, double y, double z, @Nullable Level level) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.level = level;
    }

    @NotNull
    public Locator getPosition() {
        return new Locator(this.x, this.y, this.z, this.level);
    }

    public static Locator fromObject(Vector3 pos) {
        return fromObject(pos, null);
    }

    public static Locator fromObject(Vector3 pos, Level level) {
        return new Locator(pos.x, pos.y, pos.z, level);
    }

    public @Nullable Level getLevel() {
        return this.level;
    }

    public Locator setLevel(Level level) {
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
    public Locator getSide(BlockFace face) {
        return this.getSide(face, 1);
    }

    @Override
    public Locator getSide(BlockFace face, int step) {
        return Locator.fromObject(super.getSide(face, step), getValidLevel());
    }

    // Get as a Position for better performance. Do not override it!


    public Locator getSidePos(BlockFace face) {
        return Locator.fromObject(super.getSide(face, 1), getValidLevel());
    }

    @Override
    public String toString() {
        return "Position(level=" + (this.isValid() ? Objects.requireNonNull(this.getLevel()).getName() : "null") + ",x=" + this.x + ",y=" + this.y + ",z=" + this.z + ")";
    }

    @Override
    public Locator setComponents(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    @Override
    public @NotNull Locator setComponents(@NotNull Vector3 pos) {
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
    public Locator add(double x) {
        return this.add(x, 0, 0);
    }

    @Override
    public Locator add(double x, double y) {
        return this.add(x, y, 0);
    }

    @Override
    public Locator add(double x, double y, double z) {
        return new Locator(this.x + x, this.y + y, this.z + z, this.level);
    }

    @Override
    public Locator add(Vector3 x) {
        return new Locator(this.x + x.getX(), this.y + x.getY(), this.z + x.getZ(), this.level);
    }

    @Override
    public Locator subtract(double x) {
        return this.subtract(x, 0, 0);
    }

    @Override
    public Locator subtract(double x, double y) {
        return this.subtract(x, y, 0);
    }

    @Override
    public Locator subtract(double x, double y, double z) {
        return this.add(-x, -y, -z);
    }

    @Override
    public Locator subtract(Vector3 x) {
        return this.add(-x.getX(), -x.getY(), -x.getZ());
    }

    @Override
    public Locator multiply(double number) {
        return new Locator(this.x * number, this.y * number, this.z * number, this.level);
    }

    @Override
    public Locator divide(double number) {
        return new Locator(this.x / number, this.y / number, this.z / number, this.level);
    }

    @Override
    public Locator ceil() {
        return new Locator((int) Math.ceil(this.x), (int) Math.ceil(this.y), (int) Math.ceil(this.z), this.level);
    }

    @Override
    public Locator floor() {
        return new Locator(this.getFloorX(), this.getFloorY(), this.getFloorZ(), this.level);
    }

    @Override
    public Locator round() {
        return new Locator(Math.round(this.x), Math.round(this.y), Math.round(this.z), this.level);
    }

    @Override
    public Locator abs() {
        return new Locator((int) Math.abs(this.x), (int) Math.abs(this.y), (int) Math.abs(this.z), this.level);
    }

    @Override
    public Locator clone() {
        return (Locator) super.clone();
    }

    public IChunk getChunk() {
        return isValid() ? Objects.requireNonNull(this.getLevel()).getChunk(getChunkX(), getChunkZ()) : null;
    }
}
