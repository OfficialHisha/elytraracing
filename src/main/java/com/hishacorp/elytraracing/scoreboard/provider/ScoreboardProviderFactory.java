package com.hishacorp.elytraracing.scoreboard.provider;

import com.hishacorp.elytraracing.Elytraracing;
import org.bukkit.Bukkit;

public class ScoreboardProviderFactory {
    public static ScoreboardProvider create(Elytraracing plugin) {
        if (Bukkit.getPluginManager().isPluginEnabled("SimpleScore")) {
            return new SimpleScoreProvider(plugin);
        } else {
            return new VanillaScoreboardProvider(plugin);
        }
    }
}
