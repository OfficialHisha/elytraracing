package com.hishacorp.elytraracing.scoreboard.provider;

import com.hishacorp.elytraracing.Elytraracing;

public class ScoreboardProviderFactory {
    public static ScoreboardProvider createSimpleScoreProvider(Elytraracing plugin) {
        return new SimpleScoreProvider(plugin);
    }
}
