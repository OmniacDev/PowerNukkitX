package cn.nukkit.entity;

import cn.nukkit.nbt.tag.CompoundTag;
import org.jetbrains.annotations.NotNull;

public class EntityAbilities {
    public final static String TAG_ATTACK_MOBS = "attackmobs";
    public final static String TAG_ATTACK_PLAYERS = "attackplayers";
    public final static String TAG_BUILD = "build";
    public final static String TAG_DOORS_AND_SWITCHES = "doorsandswitches";
    public final static String TAG_FLYING = "flying";
    public final static String TAG_FLY_SPEED = "flySpeed";
    public final static String TAG_INSTA_BUILD = "instabuild";
    public final static String TAG_INVULNERABLE = "invulnerable";
    public final static String TAG_LIGHTNING = "lightning";
    public final static String TAG_MAYFLY = "mayfly";
    public final static String TAG_MINE = "mine";
    public final static String TAG_MUTE = "mute";
    public final static String TAG_NO_CLIP = "noclip";
    public final static String TAG_OP = "op";
    public final static String TAG_OPEN_CONTAINERS = "opencontainers";
    public final static String TAG_PERMISSIONS_LEVEL = "permissionsLevel";
    public final static String TAG_PLAYER_PERMISSIONS_LEVEL = "playerPermissionsLevel";
    public final static String TAG_TELEPORT = "teleport";
    public final static String TAG_WALK_SPEED = "walkSpeed";
    public final static String TAG_WORLD_BUILDER = "worldbuilder";

    @NotNull public Boolean attackMobs = false;
    @NotNull public Boolean attackPlayers = false;
    @NotNull public Boolean build = false;
    @NotNull public Boolean doorsAndSwitches = false;
    @NotNull public Boolean flying = false;
    @NotNull public Float flySpeed = 0.05F;
    @NotNull public Boolean instaBuild = false;
    @NotNull public Boolean invulnerable = false;
    @NotNull public Boolean lightning = false;
    @NotNull public Boolean mayfly = false;
    @NotNull public Boolean mine = false;
    @NotNull public Boolean mute = false;
    @NotNull public Boolean noClip = false;
    @NotNull public Boolean op = false;
    @NotNull public Boolean openContainers = false;
    @NotNull public Integer permissionsLevel = 0;
    @NotNull public Integer playerPermissionsLevel = 0;
    @NotNull public Boolean teleport = false;
    @NotNull public Float walkSpeed = 0.1F;
    @NotNull public Boolean worldBuilder = false;

    public EntityAbilities() {}

    public EntityAbilities(CompoundTag abilities) {
        this.attackMobs = abilities.getBoolean(TAG_ATTACK_MOBS);
        this.attackPlayers = abilities.getBoolean(TAG_ATTACK_PLAYERS);
        this.build = abilities.getBoolean(TAG_BUILD);
        this.doorsAndSwitches = abilities.getBoolean(TAG_DOORS_AND_SWITCHES);
        this.flying = abilities.getBoolean(TAG_FLYING);
        this.flySpeed = abilities.getFloat(TAG_FLY_SPEED);
        this.instaBuild = abilities.getBoolean(TAG_INSTA_BUILD);
        this.invulnerable = abilities.getBoolean(TAG_INVULNERABLE);
        this.lightning = abilities.getBoolean(TAG_LIGHTNING);
        this.mayfly = abilities.getBoolean(TAG_MAYFLY);
        this.mine = abilities.getBoolean(TAG_MINE);
        this.mute = abilities.getBoolean(TAG_MUTE);
        this.noClip = abilities.getBoolean(TAG_NO_CLIP);
        this.op = abilities.getBoolean(TAG_OP);
        this.openContainers = abilities.getBoolean(TAG_OPEN_CONTAINERS);
        this.permissionsLevel = abilities.getInt(TAG_PERMISSIONS_LEVEL);
        this.playerPermissionsLevel = abilities.getInt(TAG_PLAYER_PERMISSIONS_LEVEL);
        this.teleport = abilities.getBoolean(TAG_TELEPORT);
        this.walkSpeed = abilities.getFloat(TAG_WALK_SPEED);
        this.worldBuilder = abilities.getBoolean(TAG_WORLD_BUILDER);
    }

    public CompoundTag get() {
        CompoundTag abilities = new CompoundTag();
        abilities.putBoolean(TAG_ATTACK_MOBS, this.attackMobs);
        abilities.putBoolean(TAG_ATTACK_PLAYERS, this.attackPlayers);
        abilities.putBoolean(TAG_BUILD, this.build);
        abilities.putBoolean(TAG_DOORS_AND_SWITCHES, this.doorsAndSwitches);
        abilities.putBoolean(TAG_FLYING, this.flying);
        abilities.putFloat(TAG_FLY_SPEED, this.flySpeed);
        abilities.putBoolean(TAG_INSTA_BUILD, this.instaBuild);
        abilities.putBoolean(TAG_INVULNERABLE, this.invulnerable);
        abilities.putBoolean(TAG_LIGHTNING, this.lightning);
        abilities.putBoolean(TAG_MAYFLY, this.mayfly);
        abilities.putBoolean(TAG_MINE, this.mine);
        abilities.putBoolean(TAG_MUTE, this.mute);
        abilities.putBoolean(TAG_OP, this.op);
        abilities.putBoolean(TAG_OPEN_CONTAINERS, this.openContainers);
        abilities.putInt(TAG_PERMISSIONS_LEVEL, this.permissionsLevel);
        abilities.putInt(TAG_PLAYER_PERMISSIONS_LEVEL, this.playerPermissionsLevel);
        abilities.putBoolean(TAG_TELEPORT, this.teleport);
        abilities.putFloat(TAG_WALK_SPEED, this.walkSpeed);
        abilities.putBoolean(TAG_WORLD_BUILDER, this.worldBuilder);
        return abilities;
    }
}
