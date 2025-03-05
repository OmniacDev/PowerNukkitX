package cn.nukkit.level;

import cn.nukkit.block.Block;
import cn.nukkit.block.BlockState;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.level.format.IChunk;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.Vector3;
import cn.nukkit.positiontracking.NamedPosition;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * @author MagicDroidX (Nukkit Project)
 */

@Slf4j
public class Locator extends Vector3 implements NamedPosition {
    public @NotNull Vector3 position;
    public @NotNull Level level;

    public Locator(@NotNull Level level) {
        this(0, 0, 0, level);
    }

    public Locator(double x, @NotNull Level level) {
        this(x, 0, 0, level);
    }

    public Locator(double x, double y, @NotNull Level level) {
        this(x, y, 0, level);
    }

    public Locator(double x, double y, double z, @NotNull Level level) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.level = level;
    }

    public Locator(@NotNull Vector3 position, @NotNull Level level) {
        this.position = position;
        this.level = level;
    }

    @NotNull
    public Locator getPosition() {
        return new Locator(this.x, this.y, this.z, this.level);
    }

    public static Locator fromObject(Vector3 pos, @NotNull Level level) {
        return new Locator(pos.x, pos.y, pos.z, level);
    }

    public @NotNull Level getLevel() {
        return this.level;
    }

    public Locator setLevel(Level level) {
        this.level = level;
        return this;
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
        return Locator.fromObject(super.getSide(face, step), getLevel());
    }

    // Get as a Position for better performance. Do not override it!


    public Locator getSidePos(BlockFace face) {
        return Locator.fromObject(super.getSide(face, 1), getLevel());
    }

    @Override
    public String toString() {
        return "Position(level=" + this.getLevel().getName() + ",x=" + this.x + ",y=" + this.y + ",z=" + this.z + ")";
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
        return getLevel().getBlockEntity(this);
    }

    public @Nullable final <T extends BlockEntity> T getTypedBlockEntity(@NotNull Class<T> type) {
        BlockEntity blockEntity = getLevel().getBlockEntity(this);
        return type.isInstance(blockEntity) ? type.cast(blockEntity) : null;
    }

    @NotNull public BlockState getLevelBlockState() {
        return getLevelBlockState(0);
    }

    @NotNull public BlockState getLevelBlockState(int layer) {
        return getLevel().getBlockStateAt(getFloorX(), getFloorY(), getFloorZ(), layer);
    }

    public Block getLevelBlock() {
        return getLevelBlock(true);
    }

    public Block getLevelBlock(boolean load) {
        return getLevel().getBlock(this, load);
    }

    public Block getLevelBlock(int layer) {
        return getLevel().getBlock(this, layer);
    }

    public Block getLevelBlock(int layer, boolean load) {
        return getLevel().getBlock(this, layer, load);
    }

    public Block getTickCachedLevelBlock() {
        return getLevel().getTickCachedBlock(this);
    }

    public Set<Block> getLevelBlockAround() {
        return getLevel().getBlockAround(this);
    }

    public Block getLevelBlockAtLayer(int layer) {
        return getLevel().getBlock(this, layer);
    }

    public Block getTickCachedLevelBlockAtLayer(int layer) {
        return getLevel().getTickCachedBlock(this, layer);
    }

    @NotNull public Transform getLocation() {
        return new Transform(this.x, this.y, this.z, 0, 0, getLevel());
    }

    @Override
    @NotNull public String getLevelName() {
        return getLevel().getName();
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
        return this.getLevel().getChunk(getChunkX(), getChunkZ());
    }
}
