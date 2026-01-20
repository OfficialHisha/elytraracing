package com.hishacorp.elytraracing.scoreboard;

import com.hishacorp.elytraracing.Elytraracing;
import com.hishacorp.elytraracing.scoreboard.provider.ScoreboardProvider;
import com.hishacorp.elytraracing.scoreboard.provider.ScoreboardProviderFactory;
import com.hishacorp.elytraracing.scoreboard.provider.VanillaScoreboardProvider;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ScoreboardManager {

    private final ScoreboardProvider provider;

    public ScoreboardManager(Elytraracing plugin) {
        if (Bukkit.getPluginManager().isPluginEnabled("SimpleScore")) {
            this.provider = ScoreboardProviderFactory.createSimpleScoreProvider(plugin);
        } else {
            this.provider = new VanillaScoreboardProvider();
        }
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
