package com.hishacorp.elytraracing;

import com.hishacorp.elytraracing.input.events.CreateRaceInputEvent;
import com.hishacorp.elytraracing.input.events.DeleteRaceInputEvent;
import com.hishacorp.elytraracing.input.events.InputEvent;
import com.hishacorp.elytraracing.model.Border;
import com.hishacorp.elytraracing.model.Racer;
import com.hishacorp.elytraracing.util.RingRenderer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static com.hishacorp.elytraracing.input.AwaitInputEventType.CREATE;
import static com.hishacorp.elytraracing.input.AwaitInputEventType.DELETE;

public class RaceManager {

    private final Elytraracing plugin;
    private final List<Race> races = new ArrayList<>();
    private final RingRenderer ringRenderer;
    private final Set<UUID> seeSpectatorsAdmins = new HashSet<>();

    public RaceManager(Elytraracing plugin) {
        this.plugin = plugin;
        this.ringRenderer = new RingRenderer();
        startScoreboardUpdateTask();
    }

    private void startScoreboardUpdateTask() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Race race : races) {
                for (UUID uuid : race.getRacers().keySet()) {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player != null) {
                        plugin.getScoreboardManager().updateScoreboard(player);
                    }
                }
                for (Player spectator : race.getSpectators().values()) {
                    plugin.getScoreboardManager().updateScoreboard(spectator);
                }
            }
        }, 1L, 1L);
    }

    public void loadRaces() {
        plugin.getDatabaseManager().getAllRaceNames().forEach(raceName -> {
            try {
                var raceData = plugin.getDatabaseManager().getRaceData(raceName);
                if (raceData != null) {
                    plugin.getRingManager().loadRings(raceData.id);
                    Race race = new Race(plugin, raceName);
                    race.setRings(plugin.getRingManager().getRings(raceData.id));
                    race.setSpawnLocation(raceData.spawn);

                    var world = Bukkit.getWorld(raceData.world);
                    List<Border> borders = plugin.getDatabaseManager().getBorders(raceData.id, world).stream()
                            .map(bd -> new Border(bd.id, bd.pos1, bd.pos2))
                            .toList();
                    race.setBorders(borders);

                    races.add(race);
                }
            } catch (java.sql.SQLException e) {
                plugin.getLogger().severe("Failed to load race " + raceName + ": " + e.getMessage());
            }
        });
    }

    public Optional<Race> getRace(String name) {
        return races.stream().filter(race -> race.getName().equalsIgnoreCase(name)).findFirst();
    }

    public Optional<Race> getRace(Player player) {
        return races.stream()
                .filter(race -> race.getRacers().containsKey(player.getUniqueId()) || race.getSpectators().containsKey(player.getUniqueId()))
                .findFirst();
    }

    public List<Race> getRaces() {
        return races;
    }

    public boolean isPlayerInRace(Player player) {
        return races.stream().anyMatch(race -> race.getRacers().containsKey(player.getUniqueId()) || race.getSpectators().containsKey(player.getUniqueId()));
    }

    public void joinRace(Player player, String raceName) {
        if (isPlayerInRace(player)) {
            player.sendMessage("§cYou are already in a race.");
            return;
        }

        getRace(raceName).ifPresentOrElse(race -> {
            if (!race.isInProgress()) {
                if (race.getStartTime() > 0) {
                    player.sendMessage("§cThis race has already finished. A new race must be started.");
                    return;
                }
                try {
                    var raceData = plugin.getDatabaseManager().getRaceData(race.getName());
                    if (raceData != null) {
                        plugin.getRingManager().loadRings(raceData.id);
                        race.setRings(plugin.getRingManager().getRings(raceData.id));
                        race.setSpawnLocation(raceData.spawn);

                        var world = Bukkit.getWorld(raceData.world);
                        List<Border> borders = plugin.getDatabaseManager().getBorders(raceData.id, world).stream()
                                .map(bd -> new Border(bd.id, bd.pos1, bd.pos2))
                                .toList();
                        race.setBorders(borders);
                    }
                } catch (java.sql.SQLException e) {
                    player.sendMessage("§cCould not load race data.");
                    return;
                }
                race.addPlayer(player);
                plugin.getScoreboardManager().showScoreboard(player);
                Racer racer = race.getRacers().get(player.getUniqueId());
                if (racer != null) {
                    ringRenderer.showRaceRings(player, race.getRings(), racer.getCurrentRingIndex());
                }
                player.sendMessage("§aYou have joined the race: " + raceName);
            } else {
                player.sendMessage("§cThis race is already in progress.");
            }
        }, () -> player.sendMessage("§cRace not found: " + raceName));
    }

    public void spectateRace(Player player, String raceName) {
        if (isPlayerInRace(player)) {
            player.sendMessage("§cYou are already in a race.");
            return;
        }

        getRace(raceName).ifPresentOrElse(race -> {
            try {
                int raceId = plugin.getDatabaseManager().getRaceId(race.getName());
                plugin.getRingManager().loadRings(raceId);
                race.setRings(plugin.getRingManager().getRings(raceId));
            } catch (java.sql.SQLException e) {
                player.sendMessage("§cCould not load race data.");
                return;
            }

            race.addSpectator(player);
            ringRenderer.showSpectatorRings(player, race.getRings());
            plugin.getScoreboardManager().showScoreboard(player);

            player.setAllowFlight(true);
            player.setFlying(true);
            for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
                if (onlinePlayer != player && !canSeeSpectators(onlinePlayer)) {
                    onlinePlayer.hidePlayer(plugin, player);
                }
            }

            player.sendMessage("§aYou are now spectating the race: " + raceName);
        }, () -> player.sendMessage("§cRace not found: " + raceName));
    }

    public void leaveRace(Player player) {
        getRace(player).ifPresentOrElse(race -> {
            plugin.getScoreboardManager().removeScoreboard(player);
            if (race.getRacers().containsKey(player.getUniqueId())) {
                ringRenderer.hideRaceRings(player, race.getRings());
                race.removePlayer(player);
            } else if (race.getSpectators().containsKey(player.getUniqueId())) {
                ringRenderer.hideRaceRings(player, race.getRings());
                race.removeSpectator(player);
                // Reset spectator specific state
                player.setAllowFlight(false);
                player.setFlying(false);
                for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
                    onlinePlayer.showPlayer(plugin, player);
                }
            }
            player.sendMessage("§aYou have left the race: " + race.getName());
        }, () -> player.sendMessage("§cYou are not in a race."));
    }

    public void startRace(CommandSender sender, String raceName) {
        getRace(raceName).ifPresentOrElse(race -> {
            if (race.getRacers().isEmpty()) {
                sender.sendMessage("§cCannot start a race with no players.");
                return;
            }
            race.start();
        }, () -> sender.sendMessage("§cRace not found: " + raceName));
    }

    public void endRace(org.bukkit.command.CommandSender sender, String raceName) {
        if (getRace(raceName).isPresent()) {
            Race race = getRace(raceName).get();
            if (!race.isInProgress()) {
                sender.sendMessage("§cRace '" + raceName + "' is not in progress.");
                return;
            }
            race.end();
            sender.sendMessage("§aRace '" + raceName + "' ended.");
        } else {
            sender.sendMessage("§cRace '" + raceName + "' not found.");
        }
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
            races.add(new Race(plugin, createRaceInputEvent.raceName));
            createRaceInputEvent.player.sendMessage("§aRace '" + createRaceInputEvent.raceName + "' created!");
            plugin.getToolManager().giveTool(createRaceInputEvent.player, createRaceInputEvent.raceName);
            createRaceInputEvent.player.sendMessage("§aYou have been given the ring tool for race " + createRaceInputEvent.raceName + ".");
        } catch (Exception ex) {
            createRaceInputEvent.player.sendMessage("§cA race with that name already exists.");
        }
    }

    public RingRenderer getRingRenderer() {
        return ringRenderer;
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
            if (race.isInProgress() || !race.getRacers().isEmpty()) {
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

    public boolean canSeeSpectators(Player player) {
        return seeSpectatorsAdmins.contains(player.getUniqueId());
    }

    public void setSeeSpectators(Player player, boolean see) {
        if (see) {
            seeSpectatorsAdmins.add(player.getUniqueId());
            // Show all spectators
            for (Race race : races) {
                for (Player spectator : race.getSpectators().values()) {
                    player.showPlayer(plugin, spectator);
                }
            }
        } else {
            seeSpectatorsAdmins.remove(player.getUniqueId());
            // Hide all spectators
            for (Race race : races) {
                for (Player spectator : race.getSpectators().values()) {
                    if (spectator != player) {
                        player.hidePlayer(plugin, spectator);
                    }
                }
            }
        }
    }

    public void toggleSeeSpectators(Player player) {
        setSeeSpectators(player, !canSeeSpectators(player));
    }
}
