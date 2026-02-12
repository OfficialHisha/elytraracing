package com.hishacorp.elytraracing;

import com.hishacorp.elytraracing.model.Racer;
import com.hishacorp.elytraracing.model.Ring;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class Race {

    private final Elytraracing plugin;
    private final String name;
    private final List<Ring> rings = new ArrayList<>();
    private final Map<UUID, Racer> racers = new HashMap<>();
    private final Map<UUID, Player> spectators = new HashMap<>();
    private final Map<UUID, BukkitTask> cooldownTasks = new HashMap<>();
    private boolean inProgress = false;
    private long startTime;
    private BukkitTask dnfTask;

    public Race(Elytraracing plugin, String name) {
        this.plugin = plugin;
        this.name = name;
    }

    public void setRings(List<Ring> rings) {
        this.rings.clear();
        this.rings.addAll(rings);
    }

    public void start() {
        this.inProgress = true;
        this.startTime = System.currentTimeMillis();

        for (UUID playerUUID : racers.keySet()) {
            Player player = Bukkit.getPlayer(playerUUID);
            if (player == null) {
                continue;
            }

            racers.get(playerUUID).setStartTime(startTime);
            player.sendMessage("§aThe race has started!");
            PlayerInventory inventory = player.getInventory();
            inventory.setChestplate(new ItemStack(Material.ELYTRA));
            inventory.addItem(new ItemStack(Material.FIREWORK_ROCKET));
        }
    }

    public void playerPassedRing(Player player) {
        if (!inProgress) {
            return;
        }

        Racer racer = racers.get(player.getUniqueId());
        if (racer == null || racer.isCompleted()) {
            return;
        }

        int nextRingIndex = racer.getCurrentRingIndex();
        if (nextRingIndex >= rings.size()) {
            return; // Should not happen
        }

        racer.setCurrentRingIndex(nextRingIndex + 1);

        if (racer.getCurrentRingIndex() >= rings.size()) {
            playerFinished(player);
        } else {
            plugin.getRaceManager().getRingRenderer().updateRingHighlight(player, rings.get(nextRingIndex), rings.get(racer.getCurrentRingIndex()));
            player.sendMessage("§aYou passed a ring! Next ring: " + (racer.getCurrentRingIndex() + 1));
        }
    }

    public void startFireworkCooldown(Player player) {
        // Cancel any existing task for this player
        if (cooldownTasks.containsKey(player.getUniqueId())) {
            cooldownTasks.get(player.getUniqueId()).cancel();
        }

        long cooldown = plugin.getConfig().getLong("firework-replenishment-cooldown", 5) * 20;

        BukkitTask task = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline() && getRacers().containsKey(player.getUniqueId())) {
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

        if (dnfTask != null) {
            dnfTask.cancel();
            dnfTask = null;
        }

        cooldownTasks.values().forEach(BukkitTask::cancel);
        cooldownTasks.clear();

        for (UUID playerUUID : racers.keySet()) {
            Player player = Bukkit.getPlayer(playerUUID);
            if (player != null) {
                Racer racer = racers.get(playerUUID);
                if (racer != null && !racer.isCompleted()) {
                    player.sendMessage("§cYou did not finish the race in time.");
                } else if (racer != null){
                    player.sendMessage("§aThe race has ended!");
                }
                player.sendMessage("§eYou can view the final scoreboard. Use /er leave to exit.");
            }
        }

        for (Player spectator : spectators.values()) {
            spectator.sendMessage("§aThe race has ended!");
            spectator.sendMessage("§eYou can view the final scoreboard. Use /er leave to exit.");
        }
    }

    public void playerFinished(Player player) {
        Racer racer = racers.get(player.getUniqueId());
        if (racer == null || racer.isCompleted()) {
            return;
        }

        racer.setCompleted(true);
        racer.setFinishTime(System.currentTimeMillis());

        player.sendMessage(String.format(Locale.US, "§aYou finished the race in %.2f seconds!", (racer.getFinishTime() - racer.getStartTime()) / 1000.0));

        boolean allFinished = racers.values().stream().allMatch(Racer::isCompleted);

        if (allFinished) {
            end();
        } else if (dnfTask == null) {
            long dnfTimer = plugin.getConfig().getLong("dnf-timer", 30);
            long dnfTime = dnfTimer * 20;
            dnfTask = Bukkit.getScheduler().runTaskLater(plugin, this::end, dnfTime);
            for (UUID playerUUID : racers.keySet()) {
                Player p = Bukkit.getPlayer(playerUUID);
                if (p != null) {
                    p.sendMessage("§eThe first player has finished! The race will end in " + dnfTimer + " seconds.");
                }
            }
        }
    }

    public String getName() {
        return name;
    }

    public boolean isInProgress() {
        return inProgress;
    }

    public long getStartTime() {
        return startTime;
    }

    public void addPlayer(Player player) {
        racers.put(player.getUniqueId(), new Racer(player));
    }

    public void removePlayer(Player player) {
        if (cooldownTasks.containsKey(player.getUniqueId())) {
            cooldownTasks.get(player.getUniqueId()).cancel();
            cooldownTasks.remove(player.getUniqueId());
        }

        racers.remove(player.getUniqueId());
    }

    public void addSpectator(Player player) {
        spectators.put(player.getUniqueId(), player);
    }

    public void removeSpectator(Player player) {
        spectators.remove(player.getUniqueId());
    }

    public Map<UUID, Player> getSpectators() {
        return spectators;
    }

    public List<Ring> getRings() {
        return rings;
    }

    public Map<UUID, Racer> getRacers() {
        return racers;
    }

    public List<Racer> getRankings() {
        List<Racer> sortedRacers = new ArrayList<>(racers.values());
        sortedRacers.sort((r1, r2) -> {
            if (r1.isCompleted() && !r2.isCompleted()) {
                return -1;
            }
            if (!r1.isCompleted() && r2.isCompleted()) {
                return 1;
            }
            if (r1.isCompleted() && r2.isCompleted()) {
                return Long.compare(r1.getFinishTime(), r2.getFinishTime());
            }

            int ringComparison = Integer.compare(r2.getCurrentRingIndex(), r1.getCurrentRingIndex());
            if (ringComparison != 0) {
                return ringComparison;
            }

            Player p1 = Bukkit.getPlayer(r1.getUuid());
            Player p2 = Bukkit.getPlayer(r2.getUuid());

            if (p1 == null || p2 == null) {
                return 0;
            }

            Location nextRingLocation1 = rings.get(r1.getCurrentRingIndex()).getLocation();
            Location nextRingLocation2 = rings.get(r2.getCurrentRingIndex()).getLocation();

            double distance1 = p1.getLocation().distanceSquared(nextRingLocation1);
            double distance2 = p2.getLocation().distanceSquared(nextRingLocation2);

            return Double.compare(distance1, distance2);
        });
        return sortedRacers;
    }
}
