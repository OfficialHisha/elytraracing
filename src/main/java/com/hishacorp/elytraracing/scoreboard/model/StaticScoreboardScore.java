package com.hishacorp.elytraracing.scoreboard.model;

import com.r4g3baby.simplescore.api.scoreboard.ScoreboardLine;
import com.r4g3baby.simplescore.api.scoreboard.ScoreboardScore;
import com.r4g3baby.simplescore.api.scoreboard.VarReplacer;
import com.r4g3baby.simplescore.api.scoreboard.condition.Condition;
import com.r4g3baby.simplescore.api.scoreboard.data.Viewer;

import java.util.Collections;
import java.util.List;

public class StaticScoreboardScore implements ScoreboardScore<Viewer> {
    private final String value;
    private final List<ScoreboardLine<Viewer>> lines;

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
    public List<ScoreboardLine<Viewer>> getLines() {
        return lines;
    }

    @Override
    public boolean getHideNumber() {
        return false;
    }

    @Override
    public Integer getValueAsInteger(Viewer viewer, VarReplacer<Viewer> varReplacer) {
        return 0;
    }

    @Override
    public ScoreboardLine<Viewer> getLine(Viewer viewer, VarReplacer<Viewer> varReplacer) {
        return lines.get(0);
    }

    @Override
    public List<Condition<Viewer>> getConditions() {
        return Collections.emptyList();
    }
}
