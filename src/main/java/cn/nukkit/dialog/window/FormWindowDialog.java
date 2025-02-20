package cn.nukkit.dialog.window;

import cn.nukkit.Player;
import cn.nukkit.dialog.element.ElementDialogButton;
import cn.nukkit.dialog.handler.FormDialogHandler;
import cn.nukkit.entity.Entity;
import cn.nukkit.utils.JSONUtils;
import cn.nukkit.utils.MainLogger;
import com.google.common.reflect.TypeToken;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
public class FormWindowDialog implements Dialog {
    private static long dialogId = 0;

    private String title;
    private String content;
    private String skinData = "";

    //usually you shouldn't edit this
    //in pnx this value is used to be an identifier
    private String sceneName = String.valueOf(dialogId++);

    private List<ElementDialogButton> buttons;

    private Entity bindEntity;

    protected final transient List<FormDialogHandler> handlers = new ObjectArrayList<>();

    public FormWindowDialog(String title, String content, Entity bindEntity) {
        this(title, content, bindEntity, new ArrayList<>());
    }

    public FormWindowDialog(String title, String content, Entity bindEntity, List<ElementDialogButton> buttons) {
        this.title = title;
        this.content = content;
        this.buttons = buttons;
        this.bindEntity = bindEntity;
        try (var reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(this.getClass().getClassLoader().getResourceAsStream("npc_data.json"))))) {
            this.skinData = reader.lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            log.error("Failed to load npc_data.json: ", e);
        }

        if (this.bindEntity == null)
            throw new IllegalArgumentException("bindEntity cannot be null!");
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<ElementDialogButton> getButtons() {
        return buttons;
    }

    public void setButtons(@NotNull List<ElementDialogButton> buttons) {
        this.buttons = buttons;
    }

    public void addButton(String text) {
        this.addButton(new ElementDialogButton(text, text));
    }

    public void addButton(ElementDialogButton button) {
        this.buttons.add(button);
    }

    public long getEntityId() {
        return bindEntity.getId();
    }

    public Entity getBindEntity() {
        return bindEntity;
    }

    public void setBindEntity(Entity bindEntity) {
        this.bindEntity = bindEntity;
    }

    public void addHandler(FormDialogHandler handler) {
        this.handlers.add(handler);
    }

    public List<FormDialogHandler> getHandlers() {
        return handlers;
    }

    public String getButtonJSONData() {
        return JSONUtils.to(this.buttons);
    }

    public void setButtonJSONData(String json) {
        List<ElementDialogButton> buttons = JSONUtils.from(json, new TypeToken<List<ElementDialogButton>>() {
        }.getType());
        //Cannot be null
        if (buttons == null) buttons = new ArrayList<>();
        this.setButtons(buttons);
    }

    public String getSkinData(){
        return this.skinData;
    }

    public void setSkinData(String data){
        this.skinData = data;
    }

    public String getSceneName() {
        return sceneName;
    }

    //请不要随意调用此方法，否则可能会导致潜在的bug
    protected void setSceneName(String sceneName) {
        this.sceneName = sceneName;
    }

    public void updateSceneName() {
        this.sceneName = String.valueOf(dialogId++);
    }

    @Override
    public void send(@NotNull Player player) {
        player.showDialogWindow(this);
    }
}
