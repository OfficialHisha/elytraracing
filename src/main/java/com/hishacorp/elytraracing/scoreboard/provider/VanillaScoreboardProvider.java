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

        objective.getScore("§f").setScore(5);
        objective.getScore("§f§lRace Time").setScore(4);
        objective.getScore("§e00:00.000").setScore(3);
        objective.getScore("§f ").setScore(2);
        objective.getScore("§f§lPlayers").setScore(1);
        objective.getScore("§e1/1").setScore(0);

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
