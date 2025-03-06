package cn.nukkit.entity.item;

import cn.nukkit.Player;
import cn.nukkit.block.*;
import cn.nukkit.block.Block;
import cn.nukkit.blockentity.BlockEntityHopper;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityHuman;
import cn.nukkit.entity.EntityLiving;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.event.vehicle.VehicleMoveEvent;
import cn.nukkit.event.vehicle.VehicleUpdateEvent;
import cn.nukkit.inventory.InventoryHolder;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemMinecart;
import cn.nukkit.level.GameRule;
import cn.nukkit.level.Transform;
import cn.nukkit.level.format.IChunk;
import cn.nukkit.math.*;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.MinecartType;
import cn.nukkit.utils.Rail;
import cn.nukkit.utils.Rail.Orientation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;

/**
 * @author larryTheCoder (Nukkit Project, Minecart and Riding Project)
 * @since 2017/6/26
 */
public abstract class EntityMinecartAbstract extends EntityVehicle {
    public final static String TAG_CUSTOM_DISPLAY_TILE = "CustomDisplayTile";
    public final static String TAG_DISPLAY_BLOCK = "DisplayBlock";
    public final static String TAG_DISPLAY_OFFSET = "DisplayOffset";

    @Nullable public Boolean customDisplayTile;
    @Nullable public Block displayBlock;
    @Nullable public Integer displayOffset;

    private static final int[][][] matrix = new int[][][]{
            {{0, 0, -1}, {0, 0, 1}},
            {{-1, 0, 0}, {1, 0, 0}},
            {{-1, -1, 0}, {1, 0, 0}},
            {{-1, 0, 0}, {1, -1, 0}},
            {{0, 0, -1}, {0, -1, 1}},
            {{0, -1, -1}, {0, 0, 1}},
            {{0, 0, 1}, {1, 0, 0}},
            {{0, 0, 1}, {-1, 0, 0}},
            {{0, 0, -1}, {-1, 0, 0}},
            {{0, 0, -1}, {1, 0, 0}}
    };
    private final boolean devs = false; // Avoid maintained features into production
    private double currentSpeed = 0;
    // Plugins modifiers
    private boolean slowWhenEmpty = true;
    private double derailedX = 0.5;
    private double derailedY = 0.5;
    private double derailedZ = 0.5;
    private double flyingX = 0.95;
    private double flyingY = 0.95;
    private double flyingZ = 0.95;
    private double maxSpeed = 0.4D;
    private boolean hasUpdated = false;

    public EntityMinecartAbstract(IChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);

        setMaxHealth(40);
        setHealth(40);

        if (nbt.contains(TAG_CUSTOM_DISPLAY_TILE)) {
            this.customDisplayTile = nbt.getBoolean(TAG_CUSTOM_DISPLAY_TILE);
        }
        if (nbt.contains(TAG_DISPLAY_BLOCK)) {
            this.displayBlock = Block.get(NBTIO.getBlockStateHelper(nbt.getCompound(TAG_DISPLAY_BLOCK)));
        }
        if (nbt.contains(TAG_DISPLAY_OFFSET)) {
            this.displayOffset = nbt.getInt(TAG_DISPLAY_OFFSET);
        }
    }

    public abstract MinecartType getType();

    public abstract boolean isRideable();

    @Override
    public float getHeight() {
        return 0.7F;
    }

    @Override
    public float getWidth() {
        return 0.98F;
    }

    @Override
    protected float getDrag() {
        return 0.1F;
    }

    @Override
    public float getBaseOffset() {
        return 0.35F;
    }

    @Override
    public boolean canDoInteraction() {
        return passengers.isEmpty() && this.displayBlock == null;
    }

    @Override
    public void initEntity() {
        super.initEntity();

        prepareDataProperty();
    }

    @Override
    public boolean onUpdate(int currentTick) {
        if (this.closed) {
            return false;
        }

        if (!this.isAlive()) {
            this.despawnFromAll();
            this.close();
            return false;
        }

        int tickDiff = currentTick - this.lastUpdate;

        if (tickDiff <= 0) {
            return false;
        }

        this.lastUpdate = currentTick;

        if (isAlive()) {
            super.onUpdate(currentTick);

            // The damage token
            if (getHealth() < 20) {
                setHealth(getHealth() + 1);
            }

            // Entity variables
            this.prevPos.x = this.position.x;
            this.prevPos.y = this.position.y;
            this.prevPos.z = this.position.z;
            this.motion.y -= 0.03999999910593033D;
            int dx = MathHelper.floor(this.position.x);
            int dy = MathHelper.floor(this.position.y);
            int dz = MathHelper.floor(this.position.z);

            // Some hack to check rails
            if (Rail.isRailBlock(level.getBlockIdAt(dx, dy - 1, dz))) {
                --dy;
            }

            Block block = level.getBlock(new Vector3(dx, dy, dz));

            // Ensure that the block is a rail
            if (Rail.isRailBlock(block)) {
                processMovement(dx, dy, dz, (BlockRail) block);
                // Activate the minecart/TNT
                if (block instanceof BlockActivatorRail activator && activator.isActive()) {
                    activate(dx, dy, dz, activator.isActive());
                    if (this.isRideable() && this.getRiding() != null) {
                        this.dismountEntity(this.getRiding());
                    }
                }
                if (block instanceof BlockDetectorRail detector && !detector.isActive()) {
                    detector.updateState(true);
                }
            } else {
                setFalling();
            }
            checkBlockCollision();

            // Minecart head
            this.rotation.pitch = 0;
            double diffX = this.prevPos.x - this.position.x;
            double diffZ = this.prevPos.z - this.position.z;
            double yawToChange = this.rotation.yaw;
            if (diffX * diffX + diffZ * diffZ > 0.001D) {
                yawToChange = (Math.atan2(diffZ, diffX) * 180 / Math.PI);
            }

            // Reverse yaw if yaw is below 0
            if (yawToChange < 0) {
                // -90-(-90)-(-90) = 90
                yawToChange -= 0.0;
            }

            setRotation(yawToChange, this.rotation.pitch);

            Transform from = new Transform(this.prevPos.x, this.prevPos.y, this.prevPos.z, this.prevRotation.yaw, this.prevRotation.pitch, level);
            Transform to = new Transform(this.position.x, this.position.y, this.position.z, this.rotation.yaw, this.rotation.pitch, level);

            this.getServer().getPluginManager().callEvent(new VehicleUpdateEvent(this));

            if (!from.equals(to)) {
                this.getServer().getPluginManager().callEvent(new VehicleMoveEvent(this, from, to));
            }

            // Collisions
            for (cn.nukkit.entity.Entity entity : level.getNearbyEntities(boundingBox.grow(0.2D, 0, 0.2D), this)) {
                if (!passengers.contains(entity) && entity instanceof EntityMinecartAbstract) {
                    entity.applyEntityCollision(this);
                }
            }

            Iterator<cn.nukkit.entity.Entity> linkedIterator = this.passengers.iterator();

            while (linkedIterator.hasNext()) {
                cn.nukkit.entity.Entity linked = linkedIterator.next();

                if (!linked.isAlive()) {
                    if (linked.riding == this) {
                        linked.riding = null;
                    }

                    linkedIterator.remove();
                }
            }

            //使矿车通知漏斗更新而不是漏斗来检测矿车
            //通常情况下，矿车的数量远远少于漏斗，所以说此举能大福提高性能
            if (this instanceof InventoryHolder holder) {
                var pickupArea = new SimpleAxisAlignedBB(this.position.x, this.position.y - 1, this.position.z, this.position.x + 1, this.position.y, this.position.z + 1);
                checkPickupHopper(pickupArea, holder);
                //漏斗矿车会自行拉取物品!
                if (!(this instanceof EntityHopperMinecart)) {
                    var pushArea = new SimpleAxisAlignedBB(this.position.x, this.position.y, this.position.z, this.position.x + 1, this.position.y + 2, this.position.z + 1);
                    checkPushHopper(pushArea, holder);
                }
            }

            // No need to onGround or Motion diff! This always have an update
            return true;
        }

        return false;
    }

    @Override
    public boolean attack(EntityDamageEvent source) {
        if (invulnerable) {
            return false;
        } else {
            source.setDamage(source.getDamage() * 15);

            boolean attack = super.attack(source);

            if (isAlive()) {
                performHurtAnimation();
            }

            return attack;
        }
    }

    public void dropItem() {
        if (this.lastDamageCause instanceof EntityDamageByEntityEvent entityDamageByEntityEvent) {
            Entity damager = entityDamageByEntityEvent.getDamager();
            if (damager instanceof Player player && player.isCreative()) {
                return;
            }
        }
        level.dropItem(this.position, new ItemMinecart());
    }

    @Override
    public void kill() {
        if (!isAlive()) {
            return;
        }
        super.kill();

        if (level.getGameRules().getBoolean(GameRule.DO_ENTITY_DROPS)) {
            dropItem();
        }
    }

    @Override
    public void close() {
        super.close();

        for (Entity passenger : new ArrayList<>(this.passengers)) {
            dismountEntity(passenger);
        }
    }

    @Override
    public boolean onInteract(Player p, Item item, Vector3 clickedPos) {
        if (!passengers.isEmpty() && isRideable()) {
            return false;
        }

        if (displayBlock == null) {
            mountEntity(p);
        }

        return super.onInteract(p, item, clickedPos);
    }

    @Override
    public void applyEntityCollision(cn.nukkit.entity.Entity entity) {
        if (entity != riding && !(entity instanceof Player && ((Player) entity).isSpectator())) {
            if (entity instanceof EntityLiving
                    && !(entity instanceof EntityHuman)
                    && this.motion.x * this.motion.x + this.motion.z * this.motion.z > 0.01D
                    && passengers.isEmpty()
                    && entity.riding == null
                    && displayBlock == null) {
                if (riding == null && devs) {
                    mountEntity(entity);// TODO: rewrite (weird riding)
                }
            }

            double motiveX = entity.position.x - this.position.x;
            double motiveZ = entity.position.z - this.position.z;
            double square = motiveX * motiveX + motiveZ * motiveZ;

            if (square >= 9.999999747378752E-5D) {
                square = Math.sqrt(square);
                motiveX /= square;
                motiveZ /= square;
                double next = 1 / square;

                if (next > 1) {
                    next = 1;
                }

                motiveX *= next;
                motiveZ *= next;
                motiveX *= 0.10000000149011612D;
                motiveZ *= 0.10000000149011612D;
                motiveX *= 1 + entityCollisionReduction;
                motiveZ *= 1 + entityCollisionReduction;
                motiveX *= 0.5D;
                motiveZ *= 0.5D;
                if (entity instanceof EntityMinecartAbstract mine) {
                    double desinityX = mine.position.x - this.position.x;
                    double desinityZ = mine.position.z - this.position.z;
                    Vector3 vector = new Vector3(desinityX, 0, desinityZ).normalize();
                    Vector3 vec = new Vector3(MathHelper.cos((float) this.rotation.yaw * 0.017453292F), 0, MathHelper.sin((float) this.rotation.yaw * 0.017453292F)).normalize();
                    double desinityXZ = Math.abs(vector.dot(vec));

                    if (desinityXZ < 0.800000011920929D) {
                        return;
                    }

                    double motX = mine.motion.x + this.motion.x;
                    double motZ = mine.motion.z + this.motion.z;

                    if (mine.getType().getId() == 2 && getType().getId() != 2) {
                        this.motion.x *= 0.20000000298023224D;
                        this.motion.z *= 0.20000000298023224D;
                        this.motion.x += mine.motion.x - motiveX;
                        this.motion.z += mine.motion.z - motiveZ;
                        mine.motion.x *= 0.949999988079071D;
                        mine.motion.z *= 0.949999988079071D;
                    } else if (mine.getType().getId() != 2 && getType().getId() == 2) {
                        mine.motion.x *= 0.20000000298023224D;
                        mine.motion.z *= 0.20000000298023224D;
                        this.motion.x += mine.motion.x + motiveX;
                        this.motion.z += mine.motion.z + motiveZ;
                        this.motion.x *= 0.949999988079071D;
                        this.motion.z *= 0.949999988079071D;
                    } else {
                        motX /= 2;
                        motZ /= 2;
                        this.motion.x *= 0.20000000298023224D;
                        this.motion.z *= 0.20000000298023224D;
                        this.motion.x += motX - motiveX;
                        this.motion.z += motZ - motiveZ;
                        mine.motion.x *= 0.20000000298023224D;
                        mine.motion.z *= 0.20000000298023224D;
                        mine.motion.x += motX + motiveX;
                        mine.motion.z += motZ + motiveZ;
                    }
                } else {
                    this.motion.x -= motiveX;
                    this.motion.z -= motiveZ;
                }
            }
        }
    }

    @Override
    public void saveNBT() {
        super.saveNBT();

        if (this.customDisplayTile != null) {
            namedTag.putBoolean(TAG_CUSTOM_DISPLAY_TILE, this.customDisplayTile);
        }
        if (this.displayBlock != null) {
            namedTag.putCompound(TAG_DISPLAY_BLOCK, this.displayBlock.getBlockState().getBlockStateTag());
        }
        if (this.displayOffset != null) {
            namedTag.putInt(TAG_DISPLAY_OFFSET, this.displayOffset);
        }
    }

    public double getMaxSpeed() {
        return maxSpeed;
    }

    protected void activate(int x, int y, int z, boolean flag) {
    }

    /**
     * 检查邻近的漏斗并通知它输出物品
     *
     * @param pushArea 漏斗输出范围
     * @return 是否有漏斗被通知
     */
    private boolean checkPushHopper(AxisAlignedBB pushArea, InventoryHolder holder) {
        int minX = NukkitMath.floorDouble(pushArea.getMinX());
        int minY = NukkitMath.floorDouble(pushArea.getMinY());
        int minZ = NukkitMath.floorDouble(pushArea.getMinZ());
        int maxX = NukkitMath.ceilDouble(pushArea.getMaxX());
        int maxY = NukkitMath.ceilDouble(pushArea.getMaxY());
        int maxZ = NukkitMath.ceilDouble(pushArea.getMaxZ());
        var tmpBV = new BlockVector3();
        for (int z = minZ; z <= maxZ; ++z) {
            for (int x = minX; x <= maxX; ++x) {
                for (int y = minY; y <= maxY; ++y) {
                    tmpBV.setComponents(x, y, z);
                    var be = this.level.getBlockEntity(tmpBV);
                    if (be instanceof BlockEntityHopper blockEntityHopper) {
                        blockEntityHopper.setMinecartInvPushTo(holder);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 检查邻近的漏斗并通知它获取物品
     *
     * @param pickupArea 漏斗拉取范围
     * @return 是否有漏斗被通知
     */
    private boolean checkPickupHopper(AxisAlignedBB pickupArea, InventoryHolder holder) {
        int minX = NukkitMath.floorDouble(pickupArea.getMinX());
        int minY = NukkitMath.floorDouble(pickupArea.getMinY());
        int minZ = NukkitMath.floorDouble(pickupArea.getMinZ());
        int maxX = NukkitMath.ceilDouble(pickupArea.getMaxX());
        int maxY = NukkitMath.ceilDouble(pickupArea.getMaxY());
        int maxZ = NukkitMath.ceilDouble(pickupArea.getMaxZ());
        var tmpBV = new BlockVector3();
        for (int z = minZ; z <= maxZ; ++z) {
            for (int x = minX; x <= maxX; ++x) {
                for (int y = minY; y <= maxY; ++y) {
                    tmpBV.setComponents(x, y, z);
                    var be = this.level.getBlockEntity(tmpBV);
                    if (be instanceof BlockEntityHopper blockEntityHopper) {
                        blockEntityHopper.setMinecartInvPickupFrom(holder);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void setFalling() {
        this.motion.x = NukkitMath.clamp(this.motion.x, -getMaxSpeed(), getMaxSpeed());
        this.motion.z = NukkitMath.clamp(this.motion.z, -getMaxSpeed(), getMaxSpeed());

        if (!hasUpdated) {
            for (cn.nukkit.entity.Entity linked : passengers) {
                linked.setSeatPosition(getMountedOffset(linked).add(0, 0.35f));
                updatePassengerPosition(linked);
            }

            hasUpdated = true;
        }

        if (onGround) {
            this.motion.x *= derailedX;
            this.motion.y *= derailedY;
            this.motion.z *= derailedZ;
        }

        move(this.motion.x, this.motion.y, this.motion.z);
        if (!onGround) {
            this.motion.x *= flyingX;
            this.motion.y *= flyingY;
            this.motion.z *= flyingZ;
        }
    }

    private void processMovement(int dx, int dy, int dz, BlockRail block) {
        fallDistance = 0.0F;
        Vector3 vector = getNextRail(this.position.x, this.position.y, this.position.z);

        this.position.y = dy;
        boolean isPowered = false;
        boolean isSlowed = false;

        if (block instanceof BlockGoldenRail) {
            isPowered = block.isActive();
            isSlowed = !block.isActive();
        }

        switch (Orientation.byMetadata(block.getRealMeta())) {
            case ASCENDING_NORTH:
                this.motion.x -= 0.0078125D;
                this.position.y += 1;
                break;
            case ASCENDING_SOUTH:
                this.motion.x += 0.0078125D;
                this.position.y += 1;
                break;
            case ASCENDING_EAST:
                this.motion.z += 0.0078125D;
                this.position.y += 1;
                break;
            case ASCENDING_WEST:
                this.motion.z -= 0.0078125D;
                this.position.y += 1;
                break;
        }

        int[][] facing = matrix[block.getRealMeta()];
        double facing1 = facing[1][0] - facing[0][0];
        double facing2 = facing[1][2] - facing[0][2];
        double speedOnTurns = Math.sqrt(facing1 * facing1 + facing2 * facing2);
        double realFacing = this.motion.x * facing1 + this.motion.z * facing2;

        if (realFacing < 0) {
            facing1 = -facing1;
            facing2 = -facing2;
        }

        double squareOfFame = Math.sqrt(this.motion.x * this.motion.x + this.motion.z * this.motion.z);

        if (squareOfFame > 2) {
            squareOfFame = 2;
        }

        this.motion.x = squareOfFame * facing1 / speedOnTurns;
        this.motion.z = squareOfFame * facing2 / speedOnTurns;
        double expectedSpeed;
        double playerYawNeg; // PlayerYawNegative
        double playerYawPos; // PlayerYawPositive
        double motion;

        cn.nukkit.entity.Entity linked = getPassenger();

        if (linked instanceof EntityLiving) {
            expectedSpeed = currentSpeed;
            if (expectedSpeed > 0) {
                // This is a trajectory (Angle of elevation)
                playerYawNeg = -Math.sin(linked.rotation.yaw * Math.PI / 180.0F);
                playerYawPos = Math.cos(linked.rotation.yaw * Math.PI / 180.0F);
                motion = this.motion.x * this.motion.x + this.motion.z * this.motion.z;
                if (motion < 0.01D) {
                    this.motion.x += playerYawNeg * 0.1D;
                    this.motion.z += playerYawPos * 0.1D;

                    isSlowed = false;
                }
            }
        }

        //http://minecraft.wiki/w/Powered_Rail#Rail
        if (isSlowed) {
            expectedSpeed = Math.sqrt(this.motion.x * this.motion.x + this.motion.z * this.motion.z);
            if (expectedSpeed < 0.03D) {
                this.motion.x *= 0;
                this.motion.y *= 0;
                this.motion.z *= 0;
            } else {
                this.motion.x *= 0.5D;
                this.motion.y *= 0;
                this.motion.z *= 0.5D;
            }
        }

        playerYawNeg = (double) dx + 0.5D + (double) facing[0][0] * 0.5D;
        playerYawPos = (double) dz + 0.5D + (double) facing[0][2] * 0.5D;
        motion = (double) dx + 0.5D + (double) facing[1][0] * 0.5D;
        double wallOfFame = (double) dz + 0.5D + (double) facing[1][2] * 0.5D;

        facing1 = motion - playerYawNeg;
        facing2 = wallOfFame - playerYawPos;
        double motX;
        double motZ;

        if (facing1 == 0) {
            this.position.x = (double) dx + 0.5D;
            expectedSpeed = this.position.z - (double) dz;
        } else if (facing2 == 0) {
            this.position.z = (double) dz + 0.5D;
            expectedSpeed = this.position.x - (double) dx;
        } else {
            motX = this.position.x - playerYawNeg;
            motZ = this.position.z - playerYawPos;
            expectedSpeed = (motX * facing1 + motZ * facing2) * 2;
        }

        this.position.x = playerYawNeg + facing1 * expectedSpeed;
        this.position.z = playerYawPos + facing2 * expectedSpeed;
        setPosition(this.position.clone()); // Hehe, my minstake :3

        motX = this.motion.x;
        motZ = this.motion.z;
        if (!passengers.isEmpty()) {
            motX *= 0.75D;
            motZ *= 0.75D;
        }
        motX = NukkitMath.clamp(motX, -getMaxSpeed(), getMaxSpeed());
        motZ = NukkitMath.clamp(motZ, -getMaxSpeed(), getMaxSpeed());

        move(motX, 0, motZ);
        if (facing[0][1] != 0 && MathHelper.floor(this.position.x) - dx == facing[0][0] && MathHelper.floor(this.position.z) - dz == facing[0][2]) {
            setPosition(new Vector3(this.position.x, this.position.y + (double) facing[0][1], this.position.z));
        } else if (facing[1][1] != 0 && MathHelper.floor(this.position.x) - dx == facing[1][0] && MathHelper.floor(this.position.z) - dz == facing[1][2]) {
            setPosition(new Vector3(this.position.x, this.position.y + (double) facing[1][1], this.position.z));
        }

        applyDrag();
        Vector3 vector1 = getNextRail(this.position.x, this.position.y, this.position.z);

        if (vector1 != null && vector != null) {
            double d14 = (vector.y - vector1.y) * 0.05D;

            squareOfFame = Math.sqrt(this.motion.x * this.motion.x + this.motion.z * this.motion.z);
            if (squareOfFame > 0) {
                this.motion.x = this.motion.x / squareOfFame * (squareOfFame + d14);
                this.motion.z = this.motion.z / squareOfFame * (squareOfFame + d14);
            }

            setPosition(new Vector3(this.position.x, vector1.y, this.position.z));
        }

        int floorX = MathHelper.floor(this.position.x);
        int floorZ = MathHelper.floor(this.position.z);

        if (floorX != dx || floorZ != dz) {
            squareOfFame = Math.sqrt(this.motion.x * this.motion.x + this.motion.z * this.motion.z);
            this.motion.x = squareOfFame * (double) (floorX - dx);
            this.motion.z = squareOfFame * (double) (floorZ - dz);
        }

        if (isPowered) {
            double newMovie = Math.sqrt(this.motion.x * this.motion.x + this.motion.z * this.motion.z);

            if (newMovie > 0.01D) {
                double nextMovie = 0.06D;

                this.motion.x += this.motion.x / newMovie * nextMovie;
                this.motion.z += this.motion.z / newMovie * nextMovie;
            } else if (block.getOrientation() == Orientation.STRAIGHT_NORTH_SOUTH) {
                if (level.getBlock(new Vector3(dx - 1, dy, dz)).isNormalBlock()) {
                    this.motion.x = 0.02D;
                } else if (level.getBlock(new Vector3(dx + 1, dy, dz)).isNormalBlock()) {
                    this.motion.x = -0.02D;
                }
            } else if (block.getOrientation() == Orientation.STRAIGHT_EAST_WEST) {
                if (level.getBlock(new Vector3(dx, dy, dz - 1)).isNormalBlock()) {
                    this.motion.z = 0.02D;
                } else if (level.getBlock(new Vector3(dx, dy, dz + 1)).isNormalBlock()) {
                    this.motion.z = -0.02D;
                }
            }
        }

    }

    private void applyDrag() {
        if (!passengers.isEmpty() || !slowWhenEmpty) {
            this.motion.x *= 0.996999979019165D;
            this.motion.y *= 0.0D;
            this.motion.z *= 0.996999979019165D;
        } else {
            this.motion.x *= 0.9599999785423279D;
            this.motion.y *= 0.0D;
            this.motion.z *= 0.9599999785423279D;
        }
    }

    private Vector3 getNextRail(double dx, double dy, double dz) {
        int checkX = MathHelper.floor(dx);
        int checkY = MathHelper.floor(dy);
        int checkZ = MathHelper.floor(dz);

        if (Rail.isRailBlock(level.getBlockIdAt(checkX, checkY - 1, checkZ))) {
            --checkY;
        }

        Block block = level.getBlock(new Vector3(checkX, checkY, checkZ));

        if (Rail.isRailBlock(block)) {
            int[][] facing = matrix[((BlockRail) block).getRealMeta()];
            double rail;
            // Genisys mistake (Doesn't check surrounding more exactly)
            double nextOne = (double) checkX + 0.5D + (double) facing[0][0] * 0.5D;
            double nextTwo = (double) checkY + 0.5D + (double) facing[0][1] * 0.5D;
            double nextThree = (double) checkZ + 0.5D + (double) facing[0][2] * 0.5D;
            double nextFour = (double) checkX + 0.5D + (double) facing[1][0] * 0.5D;
            double nextFive = (double) checkY + 0.5D + (double) facing[1][1] * 0.5D;
            double nextSix = (double) checkZ + 0.5D + (double) facing[1][2] * 0.5D;
            double nextSeven = nextFour - nextOne;
            double nextEight = (nextFive - nextTwo) * 2;
            double nextMax = nextSix - nextThree;

            if (nextSeven == 0) {
                rail = dz - (double) checkZ;
            } else if (nextMax == 0) {
                rail = dx - (double) checkX;
            } else {
                double whatOne = dx - nextOne;
                double whatTwo = dz - nextThree;

                rail = (whatOne * nextSeven + whatTwo * nextMax) * 2;
            }

            dx = nextOne + nextSeven * rail;
            dy = nextTwo + nextEight * rail;
            dz = nextThree + nextMax * rail;
            if (nextEight < 0) {
                ++dy;
            }

            if (nextEight > 0) {
                dy += 0.5D;
            }

            return new Vector3(dx, dy, dz);
        } else {
            return null;
        }
    }

    /**
     * Used to multiply the minecart current speed
     *
     * @param speed The speed of the minecart that will be calculated
     */
    public void setCurrentSpeed(double speed) {
        currentSpeed = speed;
    }

    private void prepareDataProperty() {
        setRollingAmplitude(0);
        setRollingDirection(1);

        if (this.customDisplayTile != null) {
            setDataProperty(CUSTOM_DISPLAY, this.customDisplayTile ? 1 : 0);
        }
        if (this.displayBlock != null) {
            setDataProperty(HORSE_FLAGS, this.displayBlock.getRuntimeId());
        }
        if (this.displayOffset != null) {
            setDataProperty(DISPLAY_OFFSET, this.displayOffset);
        }
    }

    /**
     * Set the minecart display block
     *
     * @param block The block that will changed. Set {@code null} for BlockAir
     */
    public void setDisplayBlock(Block block) {
        setDisplayBlock(block, true);
    }

    /**
     * Set the minecart display block
     *
     * @param block  The block that will changed. Set {@code null} for BlockAir
     * @param update Do update for the block. (This state changes if you want to show the block)
     */
    public void setDisplayBlock(Block block, boolean update) {
        if (!update) {
            displayBlock = block;
            return;
        }
        displayBlock = block;
        if (displayBlock != null) {
            setCustomDisplayTile(true);
            setDataProperty(HORSE_FLAGS, displayBlock.getRuntimeId());
            setDisplayBlockOffset(6);
        } else {
            setCustomDisplayTile(false);
            setDataProperty(HORSE_FLAGS, 0);
            setDisplayBlockOffset(0);
        }
    }

    public void setCustomDisplayTile(boolean customDisplayTile) {
        this.customDisplayTile = customDisplayTile;
        setDataProperty(CUSTOM_DISPLAY, customDisplayTile ? 1 : 0);
    }

    public void setDisplayBlockOffset(int offset) {
        this.displayOffset = offset;
        setDataProperty(DISPLAY_OFFSET, offset);
    }

    /**
     * Is the minecart can be slowed when empty?
     *
     * @return boolean
     */
    public boolean isSlowWhenEmpty() {
        return slowWhenEmpty;
    }

    /**
     * Set the minecart slowdown flag
     *
     * @param slow The slowdown flag
     */
    public void setSlowWhenEmpty(boolean slow) {
        slowWhenEmpty = slow;
    }

    public Vector3 getFlyingVelocityMod() {
        return new Vector3(flyingX, flyingY, flyingZ);
    }

    public void setFlyingVelocityMod(Vector3 flying) {
        Objects.requireNonNull(flying, "Flying velocity modifiers cannot be null");
        flyingX = flying.getX();
        flyingY = flying.getY();
        flyingZ = flying.getZ();
    }

    public Vector3 getDerailedVelocityMod() {
        return new Vector3(derailedX, derailedY, derailedZ);
    }

    public void setDerailedVelocityMod(Vector3 derailed) {
        Objects.requireNonNull(derailed, "Derailed velocity modifiers cannot be null");
        derailedX = derailed.getX();
        derailedY = derailed.getY();
        derailedZ = derailed.getZ();
    }

    public void setMaximumSpeed(double speed) {
        maxSpeed = speed;
    }
}
