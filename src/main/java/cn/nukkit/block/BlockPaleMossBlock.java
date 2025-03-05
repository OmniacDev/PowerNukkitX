package cn.nukkit.block;

import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBlock;
import cn.nukkit.level.Locator;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class BlockPaleMossBlock extends BlockMossBlock {
    public static final BlockProperties PROPERTIES = new BlockProperties(PALE_MOSS_BLOCK);

    @Override
    @NotNull
    public BlockProperties getProperties() {
        return PROPERTIES;
    }

    public BlockPaleMossBlock() {
        this(PROPERTIES.getDefaultState());
    }

    public BlockPaleMossBlock(BlockState blockstate) {
        super(blockstate);
    }

    @Override
    public String getName() {
        return "Pale Moss";
    }


    public void convertToMoss(Locator pos) {
        Random random = new Random();
        for (double x = pos.x - 3; x <= pos.x + 3; x++) {
            for (double z = pos.z - 3; z <= pos.z + 3; z++) {
                for (double y = pos.y + 5; y >= pos.y - 5; y--) {
                    if (canConvertToMoss(pos.level.getBlock(new Locator(x, y, z, pos.level))) && (random.nextDouble() < 0.6 || Math.abs(x - pos.x) < 3 && Math.abs(z - pos.z) < 3)) {
                        pos.level.setBlock(new Locator(x, y, z, pos.level), Block.get(BlockID.PALE_MOSS_BLOCK));
                        break;
                    }
                }
            }
        }
    }

    public void populateRegion(Locator pos) {
        Random random = new Random();
        for (double x = pos.x - 3; x <= pos.x + 3; x++) {
            for (double z = pos.z - 3; z <= pos.z + 3; z++) {
                for (double y = pos.y + 5; y >= pos.y - 5; y--) {
                    if (canBePopulated(new Locator(x, y, z, pos.level))) {
                        if (!canGrowPlant(new Locator(x, y, z, pos.level)))
                            break;
                        double randomDouble = random.nextDouble();
                        if (randomDouble >= 0 && randomDouble < 0.3125) {
                            pos.level.setBlock(new Locator(x, y, z, pos.level), Block.get(BlockID.TALL_GRASS), true, true);
                        }
                        if (randomDouble >= 0.3125 && randomDouble < 0.46875) {
                            pos.level.setBlock(new Locator(x, y, z, pos.level), Block.get(BlockID.PALE_MOSS_CARPET), true, true);
                        }
                        if (randomDouble >= 0.46875 && randomDouble < 0.53125) {
                            if (canBePopulated2BlockAir(new Locator(x, y, z, pos.level))) {
                                BlockLargeFern rootBlock = new BlockLargeFern();
                                rootBlock.setTopHalf(false);
                                pos.level.setBlock(new Locator(x, y, z, pos.level), rootBlock, true, true);
                                BlockLargeFern topBlock = new BlockLargeFern();
                                topBlock.setTopHalf(true);
                                pos.level.setBlock(new Locator(x, y + 1, z, pos.level), topBlock, true, true);
                            } else {
                                BlockTallGrass block = new BlockTallGrass();
                                pos.level.setBlock(new Locator(x, y, z, pos.level), block, true, true);
                            }
                        }
                        break;
                    }
                }
            }
        }
    }

    @Override
    public Item[] getDrops(Item item) {
        return new Item[]{new ItemBlock(Block.get(BlockID.PALE_MOSS_BLOCK))};
    }
}