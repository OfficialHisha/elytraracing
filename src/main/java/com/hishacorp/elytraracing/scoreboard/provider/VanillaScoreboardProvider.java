package com.hishacorp.elytraracing.scoreboard.provider;

import org.bukkit.Bukkit;
import com.hishacorp.elytraracing.Elytraracing;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.List;

public class VanillaScoreboardProvider implements ScoreboardProvider {

    private final Elytraracing plugin;

    public VanillaScoreboardProvider(Elytraracing plugin) {
        this.plugin = plugin;
    }

    @Override
    public void showScoreboard(Player player) {
        Scoreboard board = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = board.registerNewObjective("er-race", "dummy", "§e§lElytra Racing");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        player.setScoreboard(board);
        updateScoreboard(player);
    }

    @Override
    public void updateScoreboard(Player player) {
        Scoreboard board = player.getScoreboard();
        Objective objective = board.getObjective("er-race");
        if (objective == null) {
            return;
        }

        List<String> lines = plugin.getScoreboardManager().getScoreboardLines(player);

        // Remove old entries
        for (String entry : board.getEntries()) {
            board.resetScores(entry);
        }

        // Add new entries
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            objective.getScore(line).setScore(lines.size() - i);
        }
    }

    @Override
    public void removeScoreboard(Player player) {
        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
    }
}
