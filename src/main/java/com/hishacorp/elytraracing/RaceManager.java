package com.hishacorp.elytraracing;

import com.hishacorp.elytraracing.input.events.CreateRaceInputEvent;
import com.hishacorp.elytraracing.input.events.DeleteRaceInputEvent;
import com.hishacorp.elytraracing.input.events.InputEvent;
import org.bukkit.event.inventory.InventoryClickEvent;

import static com.hishacorp.elytraracing.input.AwaitInputEventType.CREATE;
import static com.hishacorp.elytraracing.input.AwaitInputEventType.DELETE;

public class RaceManager {

    private final Elytraracing plugin;

    public RaceManager(Elytraracing plugin) {
        this.plugin = plugin;
    }

    public void prepareCreateRace(InventoryClickEvent event) {
        plugin.getInputManager().awaitRaceName(CREATE, event.getWhoClicked().getUniqueId(), null, false, this::createRace);

        event.getWhoClicked().closeInventory();
        event.getWhoClicked().sendMessage("§eType the name of the new race in chat.");
    }

    public void prepareDeleteRace(InventoryClickEvent event) {
        plugin.getInputManager().awaitRaceName(DELETE, event.getWhoClicked().getUniqueId(), null, false, this::deleteRace);

        event.getWhoClicked().closeInventory();
        event.getWhoClicked().sendMessage("§eType the name of the race to delete in chat.");
    }

    public void createRace(InputEvent inputEvent) {
        if (!(inputEvent instanceof CreateRaceInputEvent createRaceInputEvent)) {
            return;
        }

        if (createRaceInputEvent.raceName.isEmpty()) {
            createRaceInputEvent.player.sendMessage("§cRace name cannot be empty.");
            return;
        }

        if (!createRaceInputEvent.player.hasPermission(Permissions.CREATE.getPermission())) {
            createRaceInputEvent.player.sendMessage("§cYou do not have permission to use this command.");
            return;
        }

        try {
            plugin.getDatabaseManager().createRace(createRaceInputEvent.raceName, createRaceInputEvent.world);
            createRaceInputEvent.player.sendMessage("§aRace '" + createRaceInputEvent.raceName + "' created!");
        } catch (Exception ex) {
            createRaceInputEvent.player.sendMessage("§cA race with that name already exists.");
        }
    }

    public void deleteRace(InputEvent inputEvent) {
        if (!(inputEvent instanceof DeleteRaceInputEvent deleteRaceInputEvent)) {
            return;
        }

        if (deleteRaceInputEvent.raceName.isEmpty()) {
            deleteRaceInputEvent.player.sendMessage("§cRace name cannot be empty.");
            return;
        }

        if (!deleteRaceInputEvent.player.hasPermission(Permissions.DELETE.getPermission())) {
            deleteRaceInputEvent.player.sendMessage("§cYou do not have permission to use this command.");
            return;
        }

        try {
            int deletedRows = plugin.getDatabaseManager().deleteRace(deleteRaceInputEvent.raceName);
            if (deletedRows == 1) {
                deleteRaceInputEvent.player.sendMessage("§aRace '" + deleteRaceInputEvent.raceName + "' deleted!");
            } else {
                deleteRaceInputEvent.player.sendMessage("§cA Race '" + deleteRaceInputEvent.raceName + "' was not found!");
            }
        } catch (Exception ex) {
            plugin.getLogger().severe("Failed to delete race: " + ex.getMessage());
            deleteRaceInputEvent.player.sendMessage("§cA Race could not be deleted.");
        }
    }
}
