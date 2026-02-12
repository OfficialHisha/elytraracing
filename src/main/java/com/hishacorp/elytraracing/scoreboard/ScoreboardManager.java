package com.hishacorp.elytraracing.scoreboard;

import com.hishacorp.elytraracing.Elytraracing;
import com.hishacorp.elytraracing.scoreboard.provider.ScoreboardProvider;
import com.hishacorp.elytraracing.scoreboard.provider.ScoreboardProviderFactory;
import org.bukkit.entity.Player;

import com.hishacorp.elytraracing.Race;
import com.hishacorp.elytraracing.model.Racer;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ScoreboardManager {
    private final Elytraracing plugin;
    private final ScoreboardProvider provider;

    public ScoreboardManager(Elytraracing plugin) {
        this.plugin = plugin;
        this.provider = ScoreboardProviderFactory.create(plugin);
    }

    public void showScoreboard(Player player) {
        provider.showScoreboard(player);
    }

    public void updateScoreboard(Player player) {
        provider.updateScoreboard(player);
    }

    public void removeScoreboard(Player player) {
        provider.removeScoreboard(player);
    }

    public List<String> getScoreboardLines(Player player) {
        Race race = plugin.getRaceManager().getRace(player).orElse(null);
        if (race == null) return Collections.emptyList();

        List<String> lines = new ArrayList<>();
        lines.add("§f");
        lines.add("§f§lRace Time");

        Racer racer = race.getRacers().get(player.getUniqueId());
        String timeDisplay = "§e00:00.000";
        if (racer != null) {
            if (racer.isCompleted()) {
                timeDisplay = "§e" + formatTime(racer.getFinishTime() - racer.getStartTime());
            } else if (race.isInProgress()) {
                timeDisplay = "§e" + formatTime(System.currentTimeMillis() - racer.getStartTime());
            } else if (race.getStartTime() > 0) {
                timeDisplay = "§cDNF";
            }
        } else {
            // Spectator
            if (race.isInProgress()) {
                timeDisplay = "§e" + formatTime(System.currentTimeMillis() - race.getStartTime());
            }
        }
        lines.add(timeDisplay);

        lines.add("§f "); // Unique empty lines for scoreboard
        lines.add("§f§lPlayers");
        long finishedCount = race.getRacers().values().stream().filter(Racer::isCompleted).count();
        lines.add("§e" + finishedCount + "/" + race.getRacers().size());

        lines.add("§f  ");
        lines.add("§f§lLineup");
        List<Racer> rankings = race.getRankings();
        for (int i = 0; i < Math.min(rankings.size(), 5); i++) {
            Racer r = rankings.get(i);
            Player p = Bukkit.getPlayer(r.getUuid());
            String name = (p != null) ? p.getName() : "Unknown";
            String rTimeDisplay;
            if (r.isCompleted()) {
                rTimeDisplay = formatTime(r.getFinishTime() - r.getStartTime());
            } else if (race.isInProgress()) {
                rTimeDisplay = formatTime(System.currentTimeMillis() - r.getStartTime());
            } else if (race.getStartTime() > 0) {
                rTimeDisplay = "§cDNF";
            } else {
                rTimeDisplay = formatTime(0);
            }
            lines.add(String.format("§e%d. §a%s §7- §f%s", i + 1, name, rTimeDisplay));
        }

        return lines;
    }

    private String formatTime(long millis) {
        long minutes = (millis / 1000) / 60;
        long seconds = (millis / 1000) % 60;
        long ms = millis % 1000;
        return String.format("%02d:%02d.%03d", minutes, seconds, ms);
    }
}
