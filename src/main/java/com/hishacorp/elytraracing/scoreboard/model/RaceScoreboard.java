package com.hishacorp.elytraracing.scoreboard.model;

import com.r4g3baby.simplescore.api.scoreboard.Scoreboard;
import com.r4g3baby.simplescore.api.scoreboard.ScoreboardLine;
import com.r4g3baby.simplescore.api.scoreboard.ScoreboardScore;
import com.r4g3baby.simplescore.api.scoreboard.VarReplacer;
import com.r4g3baby.simplescore.api.scoreboard.condition.Condition;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class RaceScoreboard implements Scoreboard<Player> {
    private final String name;
    private final List<ScoreboardLine<Player>> titles;
    private final List<ScoreboardScore<Player>> scores;

    public RaceScoreboard(String name, List<ScoreboardLine<Player>> titles, List<ScoreboardScore<Player>> scores) {
        this.name = name;
        this.titles = titles;
        this.scores = scores;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<ScoreboardLine<Player>> getTitles() {
        return titles;
    }

    @Override
    public List<ScoreboardScore<Player>> getScores() {
        return scores;
    }

    @Override
    public void tick() {
    }

    @Override
    public ScoreboardLine<Player> getTitle(Player player, VarReplacer<Player> varReplacer) {
        return titles.get(0);
    }

    @Override
    public List<ScoreboardScore<Player>> getScores(Player player, VarReplacer<Player> varReplacer) {
        return scores;
    }

    @Override
    public List<Condition<Player>> getConditions() {
        return Collections.emptyList();
    }
}
