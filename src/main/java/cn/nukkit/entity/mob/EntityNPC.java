package cn.nukkit.entity.mob;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.NPCCommandSender;
import cn.nukkit.dialog.element.ElementDialogButton;
import cn.nukkit.dialog.window.FormWindowDialog;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityIntelligent;
import cn.nukkit.entity.EntityInteractable;
import cn.nukkit.entity.EntityLiving;
import cn.nukkit.entity.ai.behavior.Behavior;
import cn.nukkit.entity.ai.behavior.IBehavior;
import cn.nukkit.entity.ai.behaviorgroup.BehaviorGroup;
import cn.nukkit.entity.ai.behaviorgroup.IBehaviorGroup;
import cn.nukkit.entity.ai.controller.LookController;
import cn.nukkit.entity.ai.evaluator.DistanceEvaluator;
import cn.nukkit.entity.ai.evaluator.ProbabilityEvaluator;
import cn.nukkit.entity.ai.executor.LookAtTargetExecutor;
import cn.nukkit.entity.ai.memory.CoreMemoryTypes;
import cn.nukkit.entity.ai.route.finder.impl.SimpleFlatAStarRouteFinder;
import cn.nukkit.entity.ai.route.posevaluator.WalkingPosEvaluator;
import cn.nukkit.entity.ai.sensor.NearestPlayerSensor;
import cn.nukkit.event.entity.EntityDamageByEntityEvent;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.IChunk;
import cn.nukkit.math.Vector3;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.StringTag;
import cn.nukkit.network.protocol.NPCRequestPacket;
import cn.nukkit.utils.MainLogger;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author good777LUCKY
 */
public class EntityNPC extends EntityMob implements EntityInteractable {
    @Override
    @NotNull
    public String getIdentifier() {
        return NPC;
    }

    public static final String TAG_ACTIONS = "Actions";
    public static final String TAG_INTERACTIVE_TEXT = "InterativeText";
    public static final String TAG_PLAYER_SCENE_MAPPING = "PlayerSceneMapping";
    public static final String TAG_RAWTEXT_NAME = "RawtextName";

    protected FormWindowDialog dialog;

    public EntityNPC(IChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);

        nbt.putIfNull(TAG_RAWTEXT_NAME, new StringTag("NPC"));
    }


    @Override
    public float getWidth() {
        return 0.6f;
    }

    @Override
    public float getHeight() {
        return 2.1f;
    }

    @Override
    public boolean canDoInteraction() {
        return true;
    }

    @Override
    public String getInteractButtonText(Player player) {
        return player.isCreative() ? "action.interact.edit" : "action.interact.talk";
    }

    @Override
    public String getOriginalName() {
        return "NPC";
    }

    @Override
    public boolean canCollide() { return false; }

    @Override
    protected IBehaviorGroup requireBehaviorGroup() {
        return new BehaviorGroup(
                this.tickSpread,
                Set.of(),
                Set.of(
                        new Behavior(
                                new LookAtTargetExecutor(CoreMemoryTypes.NEAREST_PLAYER, 100),
                                new ProbabilityEvaluator(2, 100)
                        )
                ),
                Set.of(new NearestPlayerSensor(6, 0, 20)),
                Set.of(new LookController(true, false)),
                null,
                this
        );
    }

    @Override
    public Integer getExperienceDrops() { return 0; }

    @Override
    public void initEntity() {
        super.initEntity();
        this.setMaxHealth(Integer.MAX_VALUE); // Should be Float max value
        this.setHealth(20);
        this.setNameTagVisible(true);
        this.setNameTagAlwaysVisible(true);
        this.setMovementSpeed(0.5f);

        this.dialog = new FormWindowDialog(
                this.namedTag.contains(TAG_RAWTEXT_NAME) ?
                        this.namedTag.getString(TAG_RAWTEXT_NAME) :
                        this.getNameTag(),
                this.namedTag.contains(TAG_INTERACTIVE_TEXT) ?
                    this.namedTag.getString(TAG_INTERACTIVE_TEXT) : "", this);

        if (!this.namedTag.getString(TAG_ACTIONS).isEmpty())
            this.dialog.setButtonJSONData(this.namedTag.getString(TAG_ACTIONS));

        this.dialog.addHandler((player, response) -> {
            if (response.getRequestType() == NPCRequestPacket.RequestType.SET_ACTIONS) {
                if (!response.getData().isEmpty()) {
                    this.dialog.setButtonJSONData(response.getData());
                    this.setDataProperty(Entity.ACTIONS, response.getData());
                }
            }
            if (response.getRequestType() == NPCRequestPacket.RequestType.SET_INTERACTION_TEXT) {
                this.dialog.setContent(response.getData());
                this.setDataProperty(Entity.INTERACT_TEXT, response.getData());
            }
            if (response.getRequestType() == NPCRequestPacket.RequestType.SET_NAME) {
                this.dialog.setTitle(response.getData());
                this.setNameTag(response.getData());
            }
            if (response.getRequestType() == NPCRequestPacket.RequestType.SET_SKIN) {
                this.variant = response.getSkinType();
            }
            if (response.getRequestType() == NPCRequestPacket.RequestType.EXECUTE_ACTION) {
                ElementDialogButton clickedButton = response.getClickedButton();
                for (ElementDialogButton.CmdLine line : clickedButton.getData()) {
                    Server.getInstance().executeCommand(new NPCCommandSender(this, player), line.cmd_line);
                }
            }
            if (response.getRequestType() == NPCRequestPacket.RequestType.EXECUTE_OPENING_COMMANDS) {
                for (ElementDialogButton button : this.dialog.getButtons()) {
                    if (button.getMode() == ElementDialogButton.Mode.ON_ENTER) {
                        for (ElementDialogButton.CmdLine line : button.getData()) {
                            Server.getInstance().executeCommand(new NPCCommandSender(this, player), line.cmd_line);
                        }
                    }
                }
            }
            if (response.getRequestType() == NPCRequestPacket.RequestType.EXECUTE_CLOSING_COMMANDS) {
                for (ElementDialogButton button : this.dialog.getButtons()) {
                    if (button.getMode() == ElementDialogButton.Mode.ON_EXIT) {
                        for (ElementDialogButton.CmdLine line : button.getData()) {
                            Server.getInstance().executeCommand(new NPCCommandSender(this, player), line.cmd_line);
                        }
                    }
                }
            }
        });
        this.dialog.setBindEntity(this);
    }

    @Override
    public void saveNBT() {
        super.saveNBT();

        this.namedTag.putString(TAG_RAWTEXT_NAME, this.dialog.getTitle());
        this.namedTag.putString(TAG_INTERACTIVE_TEXT, this.dialog.getContent());
        this.namedTag.putString(TAG_ACTIONS, this.dialog.getButtonJSONData());
    }

    @Override
    public boolean onInteract(Player player, Item item, Vector3 clickedPos) {
        // For creative mode players, the NPC's dialog sent must have an empty sceneName; otherwise, the client will not allow the dialog box content to be modified.
        // Additionally, we do not need to record the dialog box sent to creative mode players. Firstly, because we cannot clear it, and secondly, there is no need to do so.
        player.showDialogWindow(this.dialog, !player.isCreative());
        return false;
    }

    @Override
    public void kill() {
        this.health = 0;
        this.scheduleUpdate();

        for (Entity passenger : new ArrayList<>(this.passengers)) {
            dismountEntity(passenger);
        }
    }

    @Override
    public boolean attack(EntityDamageEvent source) {
        if (source instanceof EntityDamageByEntityEvent event && event.getDamager() instanceof Player damager && damager.isCreative()) {
            this.kill();
        }
        return false;
    }

    public FormWindowDialog getDialog() {
        return dialog;
    }
}
