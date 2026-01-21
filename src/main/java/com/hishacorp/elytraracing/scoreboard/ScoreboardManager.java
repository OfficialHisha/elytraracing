package com.hishacorp.elytraracing.scoreboard;

import com.hishacorp.elytraracing.Elytraracing;
import com.hishacorp.elytraracing.scoreboard.provider.ScoreboardProvider;
import com.hishacorp.elytraracing.scoreboard.provider.ScoreboardProviderFactory;
import org.bukkit.entity.Player;

public class ScoreboardManager {
    private final ScoreboardProvider provider;

    public ScoreboardManager(Elytraracing plugin) {
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
}
