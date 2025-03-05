package cn.nukkit.block;

import cn.nukkit.block.property.CommonBlockProperties;
import cn.nukkit.event.block.FarmLandDecayEvent;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemBlock;
import cn.nukkit.item.ItemTool;
import cn.nukkit.level.Level;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.Vector3;
import org.jetbrains.annotations.NotNull;

public class BlockFarmland extends BlockTransparent {
    public static final BlockProperties PROPERTIES = new BlockProperties(FARMLAND, CommonBlockProperties.MOISTURIZED_AMOUNT);

    @Override
    @NotNull public BlockProperties getProperties() {
        return PROPERTIES;
    }

    public BlockFarmland() {
        this(PROPERTIES.getDefaultState());
    }

    public BlockFarmland(BlockState blockstate) {
        super(blockstate);
    }

    @Override
    public String getName() {
        return "Farmland";
    }

    @Override
    public double getResistance() {
        return 3;
    }

    @Override
    public double getHardness() {
        return 0.6;
    }

    @Override
    public int getToolType() {
        return ItemTool.TYPE_SHOVEL;
    }

    @Override
    public double getMaxY() {
        return this.position.y + 1;
    }

    @Override
    public int onUpdate(int type) {
        if (type == Level.BLOCK_UPDATE_NORMAL) {
            if (this.up().isSolid()) {
                var farmEvent = new FarmLandDecayEvent(null, this);
                this.level.getServer().getPluginManager().callEvent(farmEvent);
                if (farmEvent.isCancelled()) return 0;

                this.level.setBlock(this.position, Block.get(BlockID.DIRT), false, true);

                return type;
            }
        } else if (type == Level.BLOCK_UPDATE_RANDOM) {
            Vector3 v = new Vector3();
            if (this.level.getBlock(v.setComponents(this.position.x, this.position.y + 1, this.position.z)) instanceof BlockCrops) {
                return 0;
            }

            boolean found = false;

            if (this.level.isRaining()) {
                found = true;
            } else {
                end:
                for (int x = (int) this.position.x - 4; x <= this.position.x + 4; x++) {
                    for (int z = (int) this.position.z - 4; z <= this.position.z + 4; z++) {
                        for (int y = (int) this.position.y; y <= this.position.y + 1; y++) {
                            if (z == this.position.z && x == this.position.x && y == this.position.y) {
                                continue;
                            }

                            v.setComponents(x, y, z);
                            String block = this.level.getBlockIdAt(v.getFloorX(), v.getFloorY(), v.getFloorZ());

                            if (block.equals(FLOWING_WATER) || block.equals(WATER) || block.equals(FROSTED_ICE)) {
                                found = true;
                                break end;
                            } else {
                                block = this.level.getBlockIdAt(v.getFloorX(), v.getFloorY(), v.getFloorZ(), 1);
                                if (block.equals(FLOWING_WATER) || block.equals(WATER) || block.equals(FROSTED_ICE)) {
                                    found = true;
                                    break end;
                                }
                            }
                        }
                    }
                }
            }

            Block block = this.level.getBlock(v.setComponents(this.position.x, this.position.y - 1, this.position.z));
            if (found || block instanceof BlockFlowingWater || block instanceof BlockFrostedIce) {
                if (getMoistureAmount() < 7) {
                    setMoistureAmount(7);
                    this.level.setBlock(this.position, this, false, getMoistureAmount() == 0);
                }
                return Level.BLOCK_UPDATE_RANDOM;
            }

            if (getMoistureAmount() > 0) {
                this.setMoistureAmount(getMoistureAmount() - 1);
                this.level.setBlock(this.position, this, false, getMoistureAmount() == 1);
            } else {
                var farmEvent = new FarmLandDecayEvent(null, this);
                this.level.getServer().getPluginManager().callEvent(farmEvent);
                if (farmEvent.isCancelled()) return 0;
                this.level.setBlock(this.position, Block.get(Block.DIRT), false, true);
            }

            return Level.BLOCK_UPDATE_RANDOM;
        }

        return 0;
    }

    @Override
    public Item toItem() {
        return new ItemBlock(Block.get(BlockID.DIRT));
    }

    @Override
    public boolean isSolid(BlockFace side) {
        return true;
    }

    @Override
    public boolean isTransparent() {
        return true;
    }

    public int getMoistureAmount() {
        return getPropertyValue(CommonBlockProperties.MOISTURIZED_AMOUNT);
    }

    public void setMoistureAmount(int value) {
        setPropertyValue(CommonBlockProperties.MOISTURIZED_AMOUNT, value);
    }
}
