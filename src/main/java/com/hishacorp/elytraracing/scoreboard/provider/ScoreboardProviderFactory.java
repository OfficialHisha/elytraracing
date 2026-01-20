package com.hishacorp.elytraracing.scoreboard.provider;

public class ScoreboardProviderFactory {
    public static ScoreboardProvider createSimpleScoreProvider() {
        return new SimpleScoreProvider();
    }
}
