package com.hishacorp.elytraracing.scoreboard.provider;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

public class VanillaScoreboardProvider implements ScoreboardProvider {

    @Override
    public void showScoreboard(Player player) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("elytraracing", "dummy", "§e§lElytra Racing");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        objective.getScore(ChatColor.DARK_PURPLE + "§f").setScore(15);
        objective.getScore("§f§lRace Time").setScore(14);
        objective.getScore("§e00:00.000").setScore(13);
        objective.getScore(ChatColor.DARK_BLUE + "§f").setScore(12);
        objective.getScore("§f§lPlayers").setScore(11);
        objective.getScore("§e1/1").setScore(10);
        objective.getScore(ChatColor.DARK_GREEN + "§f").setScore(9);
        objective.getScore("§f§lLineup").setScore(8);
        objective.getScore("§e1. §aPlayer1 §7- §f00:00.000").setScore(7);
        objective.getScore("§e2. §aPlayer2 §7- §f00:00.000").setScore(6);
        objective.getScore("§e3. §aPlayer3 §7- §f00:00.000").setScore(5);
        objective.getScore("§e4. §aPlayer4 §7- §f00:00.000").setScore(4);
        objective.getScore("§e5. §aPlayer5 §7- §f00:00.000").setScore(3);

        player.setScoreboard(scoreboard);
    }

    @Override
    public void updateScoreboard(Player player) {
        // TODO: Implement scoreboard update
    }

    @Override
    public void removeScoreboard(Player player) {
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }
}
