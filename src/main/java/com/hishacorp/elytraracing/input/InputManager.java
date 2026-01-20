package com.hishacorp.elytraracing.input;

import com.hishacorp.elytraracing.Elytraracing;
import com.hishacorp.elytraracing.gui.Gui;
import com.hishacorp.elytraracing.gui.GuiManager;
import com.hishacorp.elytraracing.input.events.CreateRaceInputEvent;
import com.hishacorp.elytraracing.input.events.DeleteRaceInputEvent;
import com.hishacorp.elytraracing.input.events.GenericInputEvent;
import com.hishacorp.elytraracing.input.events.InputEvent;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class InputManager {
    private final Elytraracing plugin;
    private final GuiManager guiManager;
    private final Map<UUID, AwaitInputEvent> awaitInputEvents = new HashMap<>();

    public InputManager(Elytraracing plugin, GuiManager guiManager) {
        this.plugin = plugin;
        this.guiManager = guiManager;
    }

    public void awaitRaceName(AwaitInputEventType eventType, UUID uuid, Gui gui, boolean reopenGui, Consumer<InputEvent> action) {
        awaitInputEvents.put(uuid, new AwaitInputEvent(eventType, gui, reopenGui, action));
    }

    public boolean invokeAwaitingInput(Player player, String input) {
        if (!awaitInputEvents.containsKey(player.getUniqueId())) {
            return false;
        }

        AwaitInputEvent event = awaitInputEvents.get(player.getUniqueId());
        InputEvent inputEvent = createInputEvent(event.eventType, player, input);

        plugin.getServer().getScheduler().runTask(plugin, () -> {
            event.action.accept(inputEvent);

            awaitInputEvents.remove(player.getUniqueId());

            if (event.reopenGui) {
                guiManager.openGui(player, event.fromGui);
            }
        });

        return true;
    }

    private InputEvent createInputEvent(AwaitInputEventType eventType, Player player, String input) {
        return switch (eventType) {
            case CREATE -> new CreateRaceInputEvent(player, input);
            case DELETE -> new DeleteRaceInputEvent(player, input);
            case GENERIC -> new GenericInputEvent(player, input);
        };
    }

    public void awaitChatInput(Player player, Consumer<String> consumer) {
        awaitRaceName(AwaitInputEventType.GENERIC, player.getUniqueId(), null, false, (event) -> {
            if (event instanceof GenericInputEvent genericInputEvent) {
                consumer.accept(genericInputEvent.getValue());
            }
        });
    }
}
