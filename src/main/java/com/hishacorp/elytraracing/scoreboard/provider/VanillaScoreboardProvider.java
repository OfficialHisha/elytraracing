package com.hishacorp.elytraracing.scoreboard.provider;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

public class VanillaScoreboardProvider implements ScoreboardProvider {

    @Override
    public void showScoreboard(Player player) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();
        Objective objective = board.registerNewObjective("er-race", "dummy", "§e§lElytra Racing");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        String[] lines = {
                "§f",
                "§f§lRace Time",
                "§e00:00",
                "§f",
                "§f§lPlayers",
                "§e1/1",
                "§f",
                "§f§lLineup",
                "§e1. §aPlayer1 §7- §f00:00.000",
                "§e2. §aPlayer2 §7- §f00:00.000",
                "§e3. §aPlayer3 §7- §f00:00.000",
                "§e4. §aPlayer4 §7- §f00:00.000",
                "§e5. §aPlayer5 §7- §f00:00.000"
        };

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (line.equals("§f")) {
                line += ChatColor.values()[i];
            }
            objective.getScore(line).setScore(15 - i);
        }

        player.setScoreboard(board);
    }

    @Override
    public void updateScoreboard(Player player) {
        // TODO: Implement scoreboard update
    }

    @Override
    public void removeScoreboard(Player player) {
        player.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
    }
}
