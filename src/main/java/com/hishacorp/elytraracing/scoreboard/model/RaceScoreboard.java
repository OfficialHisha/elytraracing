package com.hishacorp.elytraracing.scoreboard.model;

import com.r4g3baby.simplescore.api.scoreboard.Scoreboard;
import com.r4g3baby.simplescore.api.scoreboard.ScoreboardLine;
import com.r4g3baby.simplescore.api.scoreboard.ScoreboardScore;
import com.r4g3baby.simplescore.api.scoreboard.VarReplacer;
import com.r4g3baby.simplescore.api.scoreboard.condition.Condition;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

import com.hishacorp.elytraracing.Elytraracing;
import com.hishacorp.elytraracing.scoreboard.ScoreboardManager;

import java.util.ArrayList;

public class RaceScoreboard implements Scoreboard<Player> {
    private final String name;
    private final List<ScoreboardLine<Player>> titles;
    private final Elytraracing plugin;
    private final Map<UUID, Long> lastUpdate = new HashMap<>();
    private final Map<UUID, List<ScoreboardScore<Player>>> cachedScores = new HashMap<>();

    public RaceScoreboard(String name, List<ScoreboardLine<Player>> titles, Elytraracing plugin) {
        this.name = name;
        this.titles = titles;
        this.plugin = plugin;
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
        return Collections.emptyList();
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
        long now = System.currentTimeMillis();
        long intervalTicks = plugin.getConfig().getLong("scoreboard-update-interval", 1L);
        long intervalMs = intervalTicks * 50;

        if (cachedScores.containsKey(player.getUniqueId()) && now - lastUpdate.getOrDefault(player.getUniqueId(), 0L) < intervalMs) {
            return cachedScores.get(player.getUniqueId());
        }

        ScoreboardManager scoreboardManager = plugin.getScoreboardManager();
        if (scoreboardManager == null) {
            return Collections.emptyList();
        }
        List<String> lines = scoreboardManager.getScoreboardLines(player);
        List<ScoreboardScore<Player>> scores = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            scores.add(new StaticScoreboardScore(lines.get(i), lines.size() - i));
        }

        lastUpdate.put(player.getUniqueId(), now);
        cachedScores.put(player.getUniqueId(), scores);
        return scores;
    }

    @Override
    public List<Condition<Player>> getConditions() {
        return Collections.emptyList();
    }
}
