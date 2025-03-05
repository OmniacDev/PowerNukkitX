package cn.nukkit.block;

import cn.nukkit.Player;
import cn.nukkit.block.property.CommonBlockProperties;
import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.BlockEntityChest;
import cn.nukkit.item.Item;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.BlockFace.Plane;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.nbt.tag.Tag;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import javax.annotation.Nullable;


public class BlockTrappedChest extends BlockChest {

    public static final BlockProperties PROPERTIES = new BlockProperties(TRAPPED_CHEST, CommonBlockProperties.MINECRAFT_CARDINAL_DIRECTION);

    @Override
    @NotNull
    public BlockProperties getProperties() {
        return PROPERTIES;
    }

    public BlockTrappedChest() {
        this(PROPERTIES.getDefaultState());
    }

    public BlockTrappedChest(BlockState blockstate) {
        super(blockstate);
    }

    @Override
    public String getName() {
        return "Trapped Chest";
    }

    @Override
    public boolean place(@NotNull Item item, @NotNull Block block, @NotNull Block target, @NotNull BlockFace face, double fx, double fy, double fz, @Nullable Player player) {
        setBlockFace(player != null ? BlockFace.fromHorizontalIndex(player.getDirection().getOpposite().getHorizontalIndex()) : BlockFace.SOUTH);

        BlockEntityChest chest = null;

        for (BlockFace side : Plane.HORIZONTAL) {
            if ((getBlockFace() == BlockFace.WEST || getBlockFace() == BlockFace.EAST) && (side == BlockFace.WEST || side == BlockFace.EAST)) {
                continue;
            } else if ((getBlockFace() == BlockFace.NORTH || getBlockFace() == BlockFace.SOUTH) && (side == BlockFace.NORTH || side == BlockFace.SOUTH)) {
                continue;
            }
            Block c = this.getSide(side);
            if (c instanceof BlockTrappedChest trappedChest && trappedChest.getBlockFace() == getBlockFace()) {
                BlockEntity blockEntity = this.getLevel().getBlockEntity(trappedChest.position);
                if (blockEntity instanceof BlockEntityChest && !((BlockEntityChest) blockEntity).isPaired()) {
                    chest = (BlockEntityChest) blockEntity;
                    break;
                }
            }
        }

        this.getLevel().setBlock(block.position, this, true, true);
        CompoundTag nbt = new CompoundTag()
                .putList("Items", new ListTag<>())
                .putString("id", BlockEntity.CHEST)
                .putInt("x", (int) this.position.x)
                .putInt("y", (int) this.position.y)
                .putInt("z", (int) this.position.z);

        if (item.hasCustomName()) {
            nbt.putString("CustomName", item.getCustomName());
        }

        if (item.hasCustomBlockData()) {
            Map<String, Tag> customData = item.getCustomBlockData().getTags();
            for (Map.Entry<String, Tag> tag : customData.entrySet()) {
                nbt.put(tag.getKey(), tag.getValue());
            }
        }

        BlockEntityChest blockEntity = (BlockEntityChest) BlockEntity.createBlockEntity(BlockEntity.CHEST, this.getLevel().getChunk((int) this.position.x >> 4, (int) this.position.z >> 4), nbt);

        if (blockEntity == null) {
            return false;
        }

        if (chest != null) {
            chest.pairWith(blockEntity);
            blockEntity.pairWith(chest);
        }

        return true;
    }

    @Override
    public int getWeakPower(BlockFace face) {
        int playerCount = 0;

        BlockEntity blockEntity = this.level.getBlockEntity(this.position);

        if (blockEntity instanceof BlockEntityChest) {
            playerCount = ((BlockEntityChest) blockEntity).getInventory().getViewers().size();
        }

        return Math.min(playerCount, 15);
    }

    @Override
    public int getStrongPower(BlockFace side) {
        return side == BlockFace.UP ? this.getWeakPower(side) : 0;
    }

    @Override
    public boolean isPowerSource() {
        return true;
    }
}
