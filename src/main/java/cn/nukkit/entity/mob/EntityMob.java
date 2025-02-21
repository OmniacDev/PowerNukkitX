package cn.nukkit.entity.mob;

import cn.nukkit.Player;
import cn.nukkit.block.BlockID;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityCanAttack;
import cn.nukkit.entity.EntityIntelligent;
import cn.nukkit.entity.ai.memory.CoreMemoryTypes;
import cn.nukkit.entity.mob.monster.EntityCreeper;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.inventory.EntityArmorInventory;
import cn.nukkit.inventory.EntityEquipmentInventory;
import cn.nukkit.inventory.EntityInventoryHolder;
import cn.nukkit.inventory.Inventory;
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

import java.util.Collection;
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

    /**
     * 不同难度下实体空手能造成的伤害.
     * <p>
     * The damage that can be caused by the entity's empty hand at different difficulties.
     */
    protected float[] diffHandDamage;
    @Getter
    private EntityEquipmentInventory equipmentInventory;
    @Getter
    private EntityArmorInventory armorInventory;

    public EntityMob(IChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    protected void initEntity() {
        super.initEntity();

        this.equipmentInventory = new EntityEquipmentInventory(this);
        this.armorInventory = new EntityArmorInventory(this);

        if (this.namedTag.contains(TAG_MAINHAND)) {
            this.equipmentInventory.setItemInHand(NBTIO.getItemHelper(this.namedTag.getList(TAG_MAINHAND, CompoundTag.class).get(0)), true);
        }

        if (this.namedTag.contains(TAG_OFFHAND)) {
            this.equipmentInventory.setItemInOffhand(NBTIO.getItemHelper(this.namedTag.getList(TAG_OFFHAND, CompoundTag.class).get(0)), true);
        }

        if (this.namedTag.contains(TAG_ARMOR)) {
            ListTag<CompoundTag> armorList = this.namedTag.getList(TAG_ARMOR, CompoundTag.class);
            this.armorInventory.setItem(0, NBTIO.getItemHelper(armorList.get(0)));
            this.armorInventory.setItem(1, NBTIO.getItemHelper(armorList.get(1)));
            this.armorInventory.setItem(2, NBTIO.getItemHelper(armorList.get(2)));
            this.armorInventory.setItem(3, NBTIO.getItemHelper(armorList.get(3)));
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
        this.equipmentInventory.sendContents(player);
        this.armorInventory.sendContents(player);
    }

    @Override
    public void saveNBT() {
        super.saveNBT();

        ListTag<CompoundTag> mainHandList = new ListTag<CompoundTag>();
        mainHandList.add(NBTIO.putItemHelper(this.equipmentInventory.getItemInHand()));

        ListTag<CompoundTag> offHandList = new ListTag<CompoundTag>();
        offHandList.add(NBTIO.putItemHelper(this.equipmentInventory.getItemInOffhand()));

        this.namedTag.put(TAG_MAINHAND, mainHandList);
        this.namedTag.put(TAG_OFFHAND, offHandList);

        if (this.armorInventory != null) {
            ListTag<CompoundTag> armorTag = new ListTag<>();
            armorTag.add(NBTIO.putItemHelper(this.armorInventory.getItem(0), 0));
            armorTag.add(NBTIO.putItemHelper(this.armorInventory.getItem(1), 1));
            armorTag.add(NBTIO.putItemHelper(this.armorInventory.getItem(2), 2));
            armorTag.add(NBTIO.putItemHelper(this.armorInventory.getItem(3), 3));
            this.namedTag.putList(TAG_ARMOR,armorTag);
        }
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

            var armorInventory = this.getArmorInventory();
            for (Item armor : armorInventory.getContents().values()) {
                armorPoints += armor.getArmorPoints();
                epf += calculateEnchantmentProtectionFactor(armor, source);
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
            Entity damager = null;

            if (source instanceof EntityDamageByEntityEvent) {
                damager = ((EntityDamageByEntityEvent) source).getDamager();
            }

            for (int slot = 0; slot < 4; slot++) {
                Item armor = damageArmor(armorInventory.getItem(slot), damager);
                armorInventory.setItem(slot, armor, armor.getId() != BlockID.AIR);
            }

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
        return this.armorInventory;
    }

    @Override
    public boolean canEquipByDispenser() {
        return true;
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
