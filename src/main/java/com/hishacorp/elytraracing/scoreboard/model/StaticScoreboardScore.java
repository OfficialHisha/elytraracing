package com.hishacorp.elytraracing.scoreboard.model;

import com.r4g3baby.simplescore.api.scoreboard.ScoreboardLine;
import com.r4g3baby.simplescore.api.scoreboard.ScoreboardScore;
import com.r4g3baby.simplescore.api.scoreboard.VarReplacer;
import com.r4g3baby.simplescore.api.scoreboard.condition.Condition;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class StaticScoreboardScore implements ScoreboardScore<Player> {
    private final String value;
    private final List<ScoreboardLine<Player>> lines;

    public StaticScoreboardScore(String value) {
        this.value = value;
        this.lines = Collections.singletonList(new StaticScoreboardLine(value));
    }

    @Override
    public String getUid() {
        return value;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public List<ScoreboardLine<Player>> getLines() {
        return lines;
    }

    @Override
    public boolean getHideNumber() {
        return false;
    }

    @Override
    public Integer getValueAsInteger(Player player, VarReplacer<Player> varReplacer) {
        return 0;
    }

    @Override
    public ScoreboardLine<Player> getLine(Player player, VarReplacer<Player> varReplacer) {
        return lines.get(0);
    }

    @Override
    public List<Condition<Player>> getConditions() {
        return Collections.emptyList();
    }
}
