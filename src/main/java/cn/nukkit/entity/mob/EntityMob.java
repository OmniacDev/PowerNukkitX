package cn.nukkit.entity.mob;

import cn.nukkit.Player;
import cn.nukkit.block.BlockID;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityCanAttack;
import cn.nukkit.entity.EntityEquipment;
import cn.nukkit.entity.EntityIntelligent;
import cn.nukkit.entity.ai.memory.CoreMemoryTypes;
import cn.nukkit.entity.mob.monster.EntityCreeper;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.inventory.EntityInventoryHolder;
import cn.nukkit.inventory.Inventory;
import cn.nukkit.inventory.InventoryHolder;
import cn.nukkit.item.Item;
import cn.nukkit.item.enchantment.Enchantment;
import cn.nukkit.level.Sound;
import cn.nukkit.level.format.IChunk;
import cn.nukkit.math.NukkitMath;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.utils.Utils;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author MagicDroidX (Nukkit Project)
 */

public abstract class EntityMob extends EntityIntelligent implements EntityInventoryHolder, EntityCanAttack {
    private static final String TAG_ACTIVE_EFFECTS = "ActiveEffects";
    private static final String TAG_AIR = "Air";
    private static final String TAG_ARMOR = "Armor";
    private static final String TAG_ATTACK_TIME = "AttackTime";
    private static final String TAG_ATTRIBUTES = "Attributes";
    private static final String TAG_BODY_ROT = "BodyRot";
    private static final String TAG_BOUND_X = "boundX";
    private static final String TAG_BOUND_Y = "boundY";
    private static final String TAG_BOUND_Z = "boundZ";
    private static final String TAG_CAN_PICKUP_ITEMS = "canPickupItems";
    private static final String TAG_DEAD = "Dead";
    private static final String TAG_DEATH_TIME = "DeathTime";
    private static final String TAG_HAS_BOUND_ORIGIN = "hasBoundOrigin";
    private static final String TAG_HAS_SET_CAN_PICKUP_ITEMS = "hasSetCanPickupItems";
    private static final String TAG_HURT_TIME = "HurtTime";
    private static final String TAG_LEASHER_ID = "LeasherID";
    private static final String TAG_LIMITED_LIFE = "limitedLife";
    private static final String TAG_MAINHAND = "Mainhand";
    private static final String TAG_NATURAL_SPAWN = "NaturalSpawn";
    private static final String TAG_OFFHAND = "Offhand";
    private static final String TAG_PERSISTING_OFFERS = "persistingOffers";
    private static final String TAG_PERSISTING_RICHES = "persistingRiches";
    private static final String TAG_SURFACE = "Surface";
    private static final String TAG_TARGET_CAPTAIN_ID = "TargetCaptainID";
    private static final String TAG_TARGET_ID = "TargetID";
    private static final String TAG_TRADE_EXPERIENCE = "TradeExperience";
    private static final String TAG_TRADE_TIER = "TradeTier";
    private static final String TAG_WANTS_TO_BE_JOCKEY = "WantsToBeJockey";

    @Nullable public List<CompoundTag> activeEffects;
    @NotNull public Short air = 0;
    @NotNull public List<Item> armor = new ArrayList<>();
    @NotNull public Short attackTime = 0;
    @NotNull public List<CompoundTag> attributes = new ArrayList<>();
    @Nullable public Float bodyRot;
    @NotNull public Integer boundX = 0;
    @NotNull public Integer boundY = 0;
    @NotNull public Integer boundZ = 0;
    @NotNull public Boolean canPickupItems = false;
    @NotNull public Boolean dead = false;
    @NotNull public Short deathTime = 0;
    @NotNull public Boolean hasBoundOrigin = false;
    @NotNull public Boolean hasSetCanPickupItems = false;
    @NotNull public Short hurtTime = 0;
    @NotNull public Long leasherID = 0L;
    @NotNull public Long limitedLife = 0L;
    @NotNull public List<Item> mainHand = new ArrayList<>();
    @NotNull public Boolean naturalSpawn = false;
    @NotNull public List<Item> offHand = new ArrayList<>();
    @Nullable public CompoundTag persistingOffers;
    @Nullable public Integer persistingRiches;
    @NotNull public Boolean surface = true;
    @Nullable public Long targetCaptainID;
    @NotNull public Long targetID = 0L;
    @Nullable public Integer tradeExperience;
    @Nullable public Integer tradeTier;
    @Nullable public Boolean wantsToBeJockey;

    /**
     * 不同难度下实体空手能造成的伤害.
     * <p>
     * The damage that can be caused by the entity's empty hand at different difficulties.
     */
    protected float[] diffHandDamage;

    @Getter
    private EntityEquipment equipment;

    public EntityMob(IChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);

        if (nbt.contains(TAG_ACTIVE_EFFECTS)) this.activeEffects = nbt.getList(TAG_ACTIVE_EFFECTS, CompoundTag.class).getAll();
        this.air = nbt.getShort(TAG_AIR);
        this.armor = nbt.getList(TAG_ARMOR, CompoundTag.class).getAll().stream().map(NBTIO::getItemHelper).toList();
        this.attackTime = nbt.getShort(TAG_ATTACK_TIME);
        this.attributes = nbt.getList(TAG_ATTRIBUTES, CompoundTag.class).getAll();
        if (nbt.contains(TAG_BODY_ROT)) this.bodyRot = nbt.getFloat(TAG_BODY_ROT);
        this.boundX = nbt.getInt(TAG_BOUND_X);
        this.boundY = nbt.getInt(TAG_BOUND_Y);
        this.boundZ = nbt.getInt(TAG_BOUND_Z);
        this.canPickupItems = nbt.getBoolean(TAG_CAN_PICKUP_ITEMS);
        this.dead = nbt.getBoolean(TAG_DEAD);
        this.deathTime = nbt.getShort(TAG_DEATH_TIME);
        this.hasBoundOrigin = nbt.getBoolean(TAG_HAS_BOUND_ORIGIN);
        this.hasSetCanPickupItems = nbt.getBoolean(TAG_HAS_SET_CAN_PICKUP_ITEMS);
        this.hurtTime = nbt.getShort(TAG_HURT_TIME);
        this.leasherID = nbt.getLong(TAG_LEASHER_ID);
        this.limitedLife = nbt.getLong(TAG_LIMITED_LIFE);
        this.mainHand = nbt.getList(TAG_MAINHAND, CompoundTag.class).getAll().stream().map(NBTIO::getItemHelper).toList();
        this.naturalSpawn = nbt.getBoolean(TAG_NATURAL_SPAWN);
        this.offHand = nbt.getList(TAG_OFFHAND, CompoundTag.class).getAll().stream().map(NBTIO::getItemHelper).toList();
        if (nbt.contains(TAG_PERSISTING_OFFERS)) this.persistingOffers = nbt.getCompound(TAG_PERSISTING_OFFERS);
        if (nbt.contains(TAG_PERSISTING_RICHES)) this.persistingRiches = nbt.getInt(TAG_PERSISTING_RICHES);
        this.surface = nbt.getBoolean(TAG_SURFACE);
        if (nbt.contains(TAG_TARGET_CAPTAIN_ID)) this.targetCaptainID = nbt.getLong(TAG_TARGET_CAPTAIN_ID);
        this.targetID = nbt.getLong(TAG_TARGET_ID);
        if (nbt.contains(TAG_TRADE_EXPERIENCE)) this.tradeExperience = nbt.getInt(TAG_TRADE_EXPERIENCE);
        if (nbt.contains(TAG_TRADE_TIER)) this.tradeTier = nbt.getInt(TAG_TRADE_TIER);
        if (nbt.contains(TAG_WANTS_TO_BE_JOCKEY)) this.wantsToBeJockey = nbt.getBoolean(TAG_WANTS_TO_BE_JOCKEY);
    }

    @Override
    protected void initEntity() {
        super.initEntity();

        this.equipment = new EntityEquipment(this);

        if (this.namedTag.contains(TAG_MAINHAND)) {
            this.equipment.setMainHand(NBTIO.getItemHelper(this.namedTag.getList(TAG_MAINHAND, CompoundTag.class).get(0)), true);
        }

        if (this.namedTag.contains(TAG_OFFHAND)) {
            this.equipment.setOffHand(NBTIO.getItemHelper(this.namedTag.getList(TAG_OFFHAND, CompoundTag.class).get(0)), true);
        }

        if (this.namedTag.contains(TAG_ARMOR)) {
            ListTag<CompoundTag> armorList = this.namedTag.getList(TAG_ARMOR, CompoundTag.class);
            this.equipment.setArmor(armorList.getAll().stream().map(NBTIO::getItemHelper).toList());
        }
    }

    public void spawnToAll() {
        if (this.chunk != null && !this.closed) {
            Collection<Player> chunkPlayers = this.level.getChunkPlayers(this.chunk.getX(), this.chunk.getZ()).values();
            for (Player chunkPlayer : chunkPlayers) {
                this.spawnTo(chunkPlayer);
            }
        }
    }

    @Override
    public void spawnTo(Player player) {
        super.spawnTo(player);
        this.equipment.sendContents(player);
    }

    @Override
    public void saveNBT() {
        super.saveNBT();

        ListTag<CompoundTag> mainHand = new ListTag<>();
        mainHand.add(NBTIO.putItemHelper(this.equipment.getMainHand()));

        ListTag<CompoundTag> offHand = new ListTag<>();
        offHand.add(NBTIO.putItemHelper(this.equipment.getOffHand()));

        ListTag<CompoundTag> armor = new ListTag<>();
        armor.setAll(this.equipment.getArmor().stream().map(NBTIO::putItemHelper).toList());

        this.namedTag.put(TAG_MAINHAND, mainHand);
        this.namedTag.put(TAG_OFFHAND, offHand);
        this.namedTag.putList(TAG_ARMOR, armor);
    }

    public int getAdditionalArmor() {
        return 0;
    }

    @Override
    public boolean attack(EntityDamageEvent source) {
        if (this.isClosed() || !this.isAlive()) {
            return false;
        }

        if (source instanceof EntityDamageByEntityEvent entityDamageByEntityEvent && !(entityDamageByEntityEvent.getDamager() instanceof EntityCreeper)) {
            //更新仇恨目标
            getMemoryStorage().put(CoreMemoryTypes.ATTACK_TARGET, entityDamageByEntityEvent.getDamager());
        }

        if (source.getCause() != EntityDamageEvent.DamageCause.VOID && source.getCause() != EntityDamageEvent.DamageCause.CUSTOM && source.getCause() != EntityDamageEvent.DamageCause.MAGIC && source.getCause() != EntityDamageEvent.DamageCause.HUNGER) {
            int armorPoints = getAdditionalArmor();
            int epf = 0;
//            int toughness = 0;

            for (Item armor : equipment.getArmor()) {
                armorPoints += armor.getArmorPoints();
                epf += (int) calculateEnchantmentProtectionFactor(armor, source);
                //toughness += armor.getToughness();
            }

            if (source.canBeReducedByArmor()) {
                source.setDamage(-source.getFinalDamage() * armorPoints * 0.04f, EntityDamageEvent.DamageModifier.ARMOR);
            }

            source.setDamage(-source.getFinalDamage() * Math.min(NukkitMath.ceilFloat(Math.min(epf, 25) * ((float) ThreadLocalRandom.current().nextInt(50, 100) / 100)), 20) * 0.04f,
                    EntityDamageEvent.DamageModifier.ARMOR_ENCHANTMENTS);

            source.setDamage(-Math.min(this.getAbsorption(), source.getFinalDamage()), EntityDamageEvent.DamageModifier.ABSORPTION);
        }

        if (super.attack(source)) {
            Entity damager;

            if (source instanceof EntityDamageByEntityEvent) {
                damager = ((EntityDamageByEntityEvent) source).getDamager();
            } else damager = null;

            List<Item> damaged_armor = this.equipment.getArmor().stream().map(i -> damageArmor(i, damager)).toList();
            this.equipment.setArmor(damaged_armor);

            return true;
        } else {
            return false;
        }
    }

    protected double calculateEnchantmentProtectionFactor(Item item, EntityDamageEvent source) {
        if (!item.hasEnchantments()) {
            return 0;
        }

        double epf = 0;

        if (item.applyEnchantments()) {
            for (Enchantment ench : item.getEnchantments()) {
                epf += ench.getProtectionFactor(source);
            }
        }

        return epf;
    }

    protected Item damageArmor(Item armor, Entity damager) {
        if (armor.hasEnchantments()) {
            if (damager != null) {
                if (armor.applyEnchantments()) {
                    for (Enchantment enchantment : armor.getEnchantments()) {
                        enchantment.doPostAttack(damager, this);
                    }
                }
            }

            Enchantment durability = armor.getEnchantment(Enchantment.ID_DURABILITY);
            if (durability != null
                    && durability.getLevel() > 0
                    && (100 / (durability.getLevel() + 1)) <= Utils.random.nextInt(100)) {
                return armor;
            }
        }

        if (armor.isUnbreakable() || armor.getMaxDurability() < 0) {
            return armor;
        }

        armor.setDamage(armor.getDamage() + 1);

        if (armor.getDamage() >= armor.getMaxDurability()) {
            getLevel().addSound(this, Sound.RANDOM_BREAK);
            return Item.get(BlockID.AIR, 0, 0);
        }

        return armor;
    }

    @Override
    public Inventory getInventory() {
        return this.equipment;
    }

    @Override
    public float[] getDiffHandDamage() {
        return this.diffHandDamage;
    }

    @Override
    public boolean attackTarget(Entity entity) {
        return entity instanceof Player;
    }

    @Override
    public Item[] getDrops() {
        return getInventory().getContents().values().stream().filter(item -> !item.hasEnchantment(Enchantment.ID_VANISHING_CURSE)).toArray(Item[]::new);
    }

    @Override
    public Integer getExperienceDrops() {
        return 5;
    }
}
