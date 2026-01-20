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
    private final Map<UUID, BukkitTask> cooldownTasks = new HashMap<>();
    private boolean inProgress = false;

    public Race(Elytraracing plugin, String name) {
        this.plugin = plugin;
        this.name = name;
    }

    public void start() {
        this.inProgress = true;

        for (UUID playerUUID : players) {
            Player player = Bukkit.getPlayer(playerUUID);
            if (player == null) {
                continue;
            }

            player.sendMessage("§aThe race has started!");
            PlayerInventory inventory = player.getInventory();
            inventory.setChestplate(new ItemStack(Material.ELYTRA));
            inventory.addItem(new ItemStack(Material.FIREWORK_ROCKET));
        }
    }

    public void startFireworkCooldown(Player player) {
        // Cancel any existing task for this player
        if (cooldownTasks.containsKey(player.getUniqueId())) {
            cooldownTasks.get(player.getUniqueId()).cancel();
        }

        long cooldown = plugin.getConfig().getLong("firework-replenishment-cooldown", 5) * 20;

        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline() && getPlayers().contains(player.getUniqueId())) {
                if (player.getInventory().firstEmpty() != -1) {
                    player.getInventory().addItem(new ItemStack(Material.FIREWORK_ROCKET));
                }
            }
            cooldownTasks.remove(player.getUniqueId());
        }, cooldown);

        cooldownTasks.put(player.getUniqueId(), task);
    }

    public void end() {
        this.inProgress = false;

        cooldownTasks.values().forEach(BukkitTask::cancel);
        cooldownTasks.clear();

        for (UUID playerUUID : players) {
            Player player = Bukkit.getPlayer(playerUUID);
            if (player != null) {
                player.sendMessage("§aThe race has ended!");
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

    public void addPlayer(Player player) {
        players.add(player.getUniqueId());
    }

    public void removePlayer(Player player) {
        if (cooldownTasks.containsKey(player.getUniqueId())) {
            cooldownTasks.get(player.getUniqueId()).cancel();
            cooldownTasks.remove(player.getUniqueId());
        }

        players.remove(player.getUniqueId());
    }
}
