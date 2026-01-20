package com.hishacorp.elytraracing;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Race {

    private final Elytraracing plugin;
    private final String name;
    private final List<UUID> players = new ArrayList<>();
    private boolean inProgress = false;
    private BukkitTask replenishmentTask;

    public Race(Elytraracing plugin, String name) {
        this.plugin = plugin;
        this.name = name;
    }

    public void start() {
        setInProgress(true);

        for (UUID playerUUID : players) {
            Player player = Bukkit.getPlayer(playerUUID);
            if (player == null) {
                continue;
            }

            PlayerInventory inventory = player.getInventory();
            inventory.setChestplate(new ItemStack(Material.ELYTRA));
            inventory.addItem(new ItemStack(Material.FIREWORK_ROCKET));
        }

        long cooldown = plugin.getConfig().getLong("firework-replenishment-cooldown", 5) * 20;
        replenishmentTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (UUID playerUUID : players) {
                Player player = Bukkit.getPlayer(playerUUID);
                if (player != null && !player.getInventory().contains(Material.FIREWORK_ROCKET)) {
                    if (player.getInventory().firstEmpty() != -1) {
                        player.getInventory().addItem(new ItemStack(Material.FIREWORK_ROCKET));
                    }
                }
            }
        }, cooldown, cooldown);
    }

    public void end() {
        setInProgress(false);

        if (replenishmentTask != null) {
            replenishmentTask.cancel();
        }

        for (UUID playerUUID : players) {
            Player player = Bukkit.getPlayer(playerUUID);
            if (player != null) {
                player.getInventory().setContents(plugin.getDatabaseManager().loadInventory(playerUUID));
                plugin.getDatabaseManager().deleteInventory(playerUUID);
            }
        }

        players.clear();
    }

    public String getName() {
        return name;
    }

    public List<UUID> getPlayers() {
        return players;
    }

    public boolean isInProgress() {
        return inProgress;
    }

    public void setInProgress(boolean inProgress) {
        this.inProgress = inProgress;
    }

    public void addPlayer(Player player) {
        players.add(player.getUniqueId());
        plugin.getDatabaseManager().saveInventory(player.getUniqueId(), player.getInventory().getContents());
        player.getInventory().clear();
    }

    public void removePlayer(Player player) {
        players.remove(player.getUniqueId());
        player.getInventory().setContents(plugin.getDatabaseManager().loadInventory(player.getUniqueId()));
        plugin.getDatabaseManager().deleteInventory(player.getUniqueId());
    }
}
