package com.hishacorp.elytraracing;

import com.hishacorp.elytraracing.input.events.CreateRaceInputEvent;
import com.hishacorp.elytraracing.input.events.DeleteRaceInputEvent;
import com.hishacorp.elytraracing.input.events.InputEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.hishacorp.elytraracing.input.AwaitInputEventType.CREATE;
import static com.hishacorp.elytraracing.input.AwaitInputEventType.DELETE;

public class RaceManager {

    private final Elytraracing plugin;
    private final List<Race> races = new ArrayList<>();

    public RaceManager(Elytraracing plugin) {
        this.plugin = plugin;
    }

    public void loadRaces() {
        plugin.getDatabaseManager().getAllRaceNames().forEach(raceName -> {
            races.add(new Race(plugin, raceName));
        });
    }

    public Optional<Race> getRace(String name) {
        return races.stream().filter(race -> race.getName().equalsIgnoreCase(name)).findFirst();
    }

    public List<Race> getRaces() {
        return races;
    }

    public boolean isPlayerInRace(Player player) {
        return races.stream().anyMatch(race -> race.getPlayers().contains(player.getUniqueId()));
    }

    public void joinRace(Player player, String raceName) {
        if (isPlayerInRace(player)) {
            player.sendMessage("§cYou are already in a race.");
            return;
        }

        getRace(raceName).ifPresentOrElse(race -> {
            if (!race.isInProgress()) {
                race.addPlayer(player);
                player.sendMessage("§aYou have joined the race: " + raceName);
            } else {
                player.sendMessage("§cThis race is already in progress.");
            }
        }, () -> player.sendMessage("§cRace not found: " + raceName));
    }

    public void leaveRace(Player player) {
        races.stream()
                .filter(race -> race.getPlayers().contains(player.getUniqueId()))
                .findFirst()
                .ifPresent(race -> {
                    race.removePlayer(player);
                    player.sendMessage("§aYou have left the race: " + race.getName());
                });
    }

    public void startRace(String raceName) {
        getRace(raceName).ifPresent(Race::start);
    }

    public void endRace(String raceName) {
        getRace(raceName).ifPresent(Race::end);
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
            plugin.getDatabaseManager().createRace(createRaceInputEvent.raceName);
            races.add(new Race(plugin, createRaceInputEvent.raceName));
            createRaceInputEvent.player.sendMessage("§aRace '" + createRaceInputEvent.raceName + "' created!");
        } catch (Exception ex) {
            createRaceInputEvent.player.sendMessage("§cA race with that name already exists.");
        }
    }

    public void deleteRace(InputEvent inputEvent) {
        if (!(inputEvent instanceof DeleteRaceInputEvent deleteRaceInputEvent)) {
            return;
        }

        String raceName = deleteRaceInputEvent.raceName;
        Player player = deleteRaceInputEvent.player;

        if (raceName.isEmpty()) {
            player.sendMessage("§cRace name cannot be empty.");
            return;
        }

        if (!player.hasPermission(Permissions.DELETE.getPermission())) {
            player.sendMessage("§cYou do not have permission to use this command.");
            return;
        }

        Optional<Race> raceOptional = getRace(raceName);
        if (raceOptional.isPresent()) {
            Race race = raceOptional.get();
            if (race.isInProgress() || !race.getPlayers().isEmpty()) {
                player.sendMessage("§cCannot delete a race that is in progress or has players.");
                return;
            }
        }

        try {
            int deletedRows = plugin.getDatabaseManager().deleteRace(raceName);
            if (deletedRows == 1) {
                getRace(raceName).ifPresent(races::remove);
                player.sendMessage("§aRace '" + raceName + "' deleted!");
            } else {
                player.sendMessage("§cA Race '" + raceName + "' was not found!");
            }
        } catch (Exception ex) {
            plugin.getLogger().severe("Failed to delete race: " + ex.getMessage());
            player.sendMessage("§cA Race could not be deleted.");
        }
    }
}
