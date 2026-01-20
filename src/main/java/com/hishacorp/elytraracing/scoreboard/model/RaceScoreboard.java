package com.hishacorp.elytraracing.scoreboard.model;

import com.r4g3baby.simplescore.api.scoreboard.Scoreboard;
import com.r4g3baby.simplescore.api.scoreboard.ScoreboardLine;
import com.r4g3baby.simplescore.api.scoreboard.ScoreboardScore;
import com.r4g3baby.simplescore.api.scoreboard.VarReplacer;
import com.r4g3baby.simplescore.api.scoreboard.condition.Condition;
import com.r4g3baby.simplescore.api.scoreboard.data.Viewer;

import java.util.Collections;
import java.util.List;

public class RaceScoreboard implements Scoreboard<Viewer> {
    private final String name;
    private final List<ScoreboardLine<Viewer>> titles;
    private final List<ScoreboardScore<Viewer>> scores;

    public RaceScoreboard(String name, List<ScoreboardLine<Viewer>> titles, List<ScoreboardScore<Viewer>> scores) {
        this.name = name;
        this.titles = titles;
        this.scores = scores;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<ScoreboardLine<Viewer>> getTitles() {
        return titles;
    }

    @Override
    public List<ScoreboardScore<Viewer>> getScores() {
        return scores;
    }

    @Override
    public void tick() {
    }

    @Override
    public ScoreboardLine<Viewer> getTitle(Viewer viewer, VarReplacer<Viewer> varReplacer) {
        return titles.get(0);
    }

    @Override
    public List<ScoreboardScore<Viewer>> getScores(Viewer viewer, VarReplacer<Viewer> varReplacer) {
        return scores;
    }

    @Override
    public List<Condition<Viewer>> getConditions() {
        return Collections.emptyList();
    }
}
