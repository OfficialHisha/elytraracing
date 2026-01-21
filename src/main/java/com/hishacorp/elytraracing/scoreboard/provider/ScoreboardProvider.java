package com.hishacorp.elytraracing.scoreboard.provider;

import org.bukkit.entity.Player;

public interface ScoreboardProvider {
    void showScoreboard(Player player);
    void updateScoreboard(Player player);
    void removeScoreboard(Player player);
}
