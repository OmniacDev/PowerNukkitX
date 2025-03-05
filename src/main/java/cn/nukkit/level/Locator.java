package cn.nukkit.level;

import cn.nukkit.block.Block;
import cn.nukkit.block.BlockState;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.level.format.IChunk;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.IVector3;
import cn.nukkit.math.Rotator2;
import cn.nukkit.math.Vector3;
import cn.nukkit.positiontracking.NamedPosition;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

@Slf4j
public class Locator implements NamedPosition, Cloneable, IVector3 {
    public @NotNull Vector3 position;
    public @NotNull Level level;

    public Locator(@NotNull Level level) {
        this(0, 0, 0, level);
    }

    public Locator(double x, double y, double z, @NotNull Level level) {
        this(new Vector3(x, y, z), level);
    }

    public Locator(@NotNull Vector3 position, @NotNull Level level) {
        this.position = position;
        this.level = level;
    }

    public int getFloorX() {
        return this.position.getFloorX();
    }

    public int getFloorY() {
        return this.position.getFloorY();
    }

    public int getFloorZ() {
        return this.position.getFloorZ();
    }

    public Vector3 getVector3() {
        return this.position.clone();
    }

    public Locator getLocator() {
        return this.clone();
    }

    @SneakyThrows
    @Override
    public Locator clone() {
        return (Locator) super.clone();
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

    public Locator getSide(BlockFace face) {
        return this.getSide(face, 1);
    }

    public Locator getSide(BlockFace face, int step) {
        return Locator.fromObject(this.position.getSide(face, step), getLevel());
    }

    public Locator getSidePos(BlockFace face) {
        return Locator.fromObject(this.position.getSide(face, 1), getLevel());
    }

    @Override
    public String toString() {
        return "Locator(level=" + this.getLevel().getName() + ",position=" + this.position + ")";
    }

    public @Nullable BlockEntity getLevelBlockEntity() {
        return getLevel().getBlockEntity(this.position);
    }

    public @Nullable final <T extends BlockEntity> T getTypedBlockEntity(@NotNull Class<T> type) {
        BlockEntity blockEntity = getLevel().getBlockEntity(this.position);
        return type.isInstance(blockEntity) ? type.cast(blockEntity) : null;
    }

    @NotNull public BlockState getLevelBlockState() {
        return getLevelBlockState(0);
    }

    @NotNull public BlockState getLevelBlockState(int layer) {
        return getLevel().getBlockStateAt(this.position.getFloorX(), this.position.getFloorY(), this.position.getFloorZ(), layer);
    }

    public Block getLevelBlock() {
        return getLevelBlock(true);
    }

    public Block getLevelBlock(boolean load) {
        return getLevel().getBlock(this.position, load);
    }

    public Block getLevelBlock(int layer) {
        return getLevel().getBlock(this.position, layer);
    }

    public Block getLevelBlock(int layer, boolean load) {
        return getLevel().getBlock(this.position, layer, load);
    }

    public Block getTickCachedLevelBlock() {
        return getLevel().getTickCachedBlock(this.position);
    }

    public Set<Block> getLevelBlockAround() {
        return getLevel().getBlockAround(this.position);
    }

    public Block getLevelBlockAtLayer(int layer) {
        return getLevel().getBlock(this.position, layer);
    }

    public Block getTickCachedLevelBlockAtLayer(int layer) {
        return getLevel().getTickCachedBlock(this.position, layer);
    }

    @NotNull public Transform getTransform() {
        return new Transform(this.position, new Rotator2(0, 0), getLevel());
    }

    @Override
    @NotNull public String getLevelName() {
        return getLevel().getName();
    }

    @Override
    public double getX() {
        return position.x;
    }

    @Override
    public double getY() {
        return position.y;
    }

    @Override
    public double getZ() {
        return position.z;
    }

    public IChunk getChunk() {
        return this.getLevel().getChunk(position.getChunkX(), position.getChunkZ());
    }

    public Locator add(double x, double y, double z) {
        return new Locator(this.position.add(x, y, z), this.level);
    }

    public Locator subtract(double x, double y, double z) {
        return this.add(-x, -y, -z);
    }
}
