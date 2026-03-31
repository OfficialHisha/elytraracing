package com.hishacorp.elytraracing;

import com.hishacorp.elytraracing.model.Border;
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
    private Location spawnLocation;
    private final List<Border> borders = new ArrayList<>();
    private final List<Ring> rings = new ArrayList<>();
    private final Map<UUID, Racer> racers = new HashMap<>();
    private final Map<UUID, Player> spectators = new HashMap<>();
    private final Map<UUID, BukkitTask> cooldownTasks = new HashMap<>();
    private boolean inProgress = false;
    private boolean starting = false;
    private long startTime;
    private BukkitTask dnfTask;
    private BukkitTask resetTask;
    private boolean enabled = true;
    private int laps = 1;
    private int resetDelay = 0;
    private int dnfTimer = 30;
    private int rocketCooldown = 5;

    public Race(Elytraracing plugin, String name) {
        this.plugin = plugin;
        this.name = name;
    }

    public void setRings(List<Ring> rings) {
        this.rings.clear();
        this.rings.addAll(rings);
    }

    public void startCountdown(int seconds) {
        this.starting = true;
        new org.bukkit.scheduler.BukkitRunnable() {
            int count = seconds;

            @Override
            public void run() {
                if (count > 0) {
                    String color = count <= 3 ? "§c§l" : "§e§l";
                    sendTitle(color + count, "", 0, 20, 0);
                    playSound(org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
                    count--;
                } else {
                    sendTitle("§a§lGO!", "", 0, 20, 10);
                    playSound(org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f);
                    starting = false;
                    start();
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void sendTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        for (UUID uuid : racers.keySet()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) p.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
        }
        for (Player p : spectators.values()) {
            p.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
        }
    }

    private void playSound(org.bukkit.Sound sound, float volume, float pitch) {
        for (UUID uuid : racers.keySet()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) p.playSound(p.getLocation(), sound, volume, pitch);
        }
        for (Player p : spectators.values()) {
            p.playSound(p.getLocation(), sound, volume, pitch);
        }
    }

    public void start() {
        this.inProgress = true;
        this.startTime = System.currentTimeMillis();

        if (resetTask != null) {
            resetTask.cancel();
            resetTask = null;
        }

        for (UUID playerUUID : racers.keySet()) {
            Player player = Bukkit.getPlayer(playerUUID);
            if (player == null) {
                continue;
            }

            if (spawnLocation != null) {
                player.teleport(spawnLocation);
            }

            Racer racer = racers.get(playerUUID);
            racer.setStartTime(startTime);
            racer.setLastLapStartTime(startTime);
            player.sendMessage("§aThe race has started!");
            player.setGameMode(org.bukkit.GameMode.SURVIVAL);
            PlayerInventory inventory = player.getInventory();
            inventory.clear();
            player.getEquipment().setChestplate(new ItemStack(Material.ELYTRA));
            inventory.addItem(new ItemStack(Material.FIREWORK_ROCKET));
            player.updateInventory();
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
            long now = System.currentTimeMillis();
            long lapTime = now - racer.getLastLapStartTime();
            if (racer.getBestLapTime() == -1 || lapTime < racer.getBestLapTime()) {
                racer.setBestLapTime(lapTime);
            }

            if (racer.getCurrentLap() < laps) {
                racer.setCurrentLap(racer.getCurrentLap() + 1);
                racer.setCurrentRingIndex(0);
                racer.setLastLapStartTime(now);
                plugin.getRaceManager().getRingRenderer().updateRingHighlight(player, rings.get(nextRingIndex), rings.get(0));
                player.sendMessage("§aYou completed lap " + (racer.getCurrentLap() - 1) + "! Best lap: " + formatTime(racer.getBestLapTime()));
            } else {
                playerFinished(player);
            }
        } else {
            plugin.getRaceManager().getRingRenderer().updateRingHighlight(player, rings.get(nextRingIndex), rings.get(racer.getCurrentRingIndex()));
            player.sendMessage("§aYou passed a ring! Next ring: " + (racer.getCurrentRingIndex() + 1));
        }
    }

    private String formatTime(long millis) {
        long minutes = (millis / 1000) / 60;
        long seconds = (millis / 1000) % 60;
        long ms = millis % 1000;
        return String.format("%02d:%02d.%03d", minutes, seconds, ms);
    }

    public void startFireworkCooldown(Player player) {
        // Cancel any existing task for this player
        if (cooldownTasks.containsKey(player.getUniqueId())) {
            cooldownTasks.get(player.getUniqueId()).cancel();
        }

        long cooldown = (long) rocketCooldown * 20;

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

        if (resetDelay > 0) {
            resetTask = Bukkit.getScheduler().runTaskLater(plugin, () -> {
                plugin.getRaceManager().resetRace(this);
                resetTask = null;
            }, resetDelay * 20L);
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

        int raceId = -1;
        try {
            raceId = plugin.getDatabaseManager().getRaceId(name);
        } catch (Exception ignored) {}

        if (raceId != -1) {
            boolean win = racers.values().stream().filter(Racer::isCompleted).count() == 1;
            try {
                plugin.getDatabaseManager().saveRaceStat(player.getUniqueId(), raceId, racer.getFinishTime() - racer.getStartTime(), racer.getBestLapTime(), win);
            } catch (Exception e) {
                plugin.getLogger().severe("Failed to save race stats for " + player.getName() + ": " + e.getMessage());
            }
        }

        boolean allFinished = racers.values().stream().allMatch(Racer::isCompleted);

        if (allFinished) {
            end();
        } else if (dnfTask == null) {
            long dnfTime = dnfTimer * 20L;
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

    public Location getSpawnLocation() {
        return spawnLocation;
    }

    public void setSpawnLocation(Location spawnLocation) {
        this.spawnLocation = spawnLocation;
    }

    public List<Border> getBorders() {
        return borders;
    }

    public void setBorders(List<Border> borders) {
        this.borders.clear();
        this.borders.addAll(borders);
    }

    public boolean isInsideBorder(Location location) {
        if (borders.isEmpty()) {
            return true;
        }

        for (Border border : borders) {
            if (border.isInside(location)) {
                return true;
            }
        }

        return false;
    }

    public boolean isInProgress() {
        return inProgress;
    }

    public boolean isStarting() {
        return starting;
    }

    public long getStartTime() {
        return startTime;
    }

    public void resetState() {
        this.inProgress = false;
        this.startTime = 0;
        if (dnfTask != null) {
            dnfTask.cancel();
            dnfTask = null;
        }
        if (resetTask != null) {
            resetTask.cancel();
            resetTask = null;
        }
        cooldownTasks.values().forEach(BukkitTask::cancel);
        cooldownTasks.clear();
        racers.clear();
        spectators.clear();
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

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getLaps() {
        return laps;
    }

    public void setLaps(int laps) {
        this.laps = laps;
    }

    public int getResetDelay() {
        return resetDelay;
    }

    public void setResetDelay(int resetDelay) {
        this.resetDelay = resetDelay;
    }

    public int getDnfTimer() {
        return dnfTimer;
    }

    public void setDnfTimer(int dnfTimer) {
        this.dnfTimer = dnfTimer;
    }

    public int getRocketCooldown() {
        return rocketCooldown;
    }

    public void setRocketCooldown(int rocketCooldown) {
        this.rocketCooldown = rocketCooldown;
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
