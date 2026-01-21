package com.hishacorp.elytraracing.scoreboard.model;

import com.r4g3baby.simplescore.api.scoreboard.ScoreboardLine;
import com.r4g3baby.simplescore.api.scoreboard.VarReplacer;
import com.r4g3baby.simplescore.api.scoreboard.condition.Condition;
import com.r4g3baby.simplescore.api.scoreboard.effect.TextEffect;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class StaticScoreboardLine implements ScoreboardLine<Player> {
    private final String text;

    public StaticScoreboardLine(String text) {
        this.text = text;
    }

    @Override
    public List<TextEffect> getTextEffects() {
        return Collections.emptyList();
    }

    @Override
    public String getUid() {
        return text;
    }

    @Override
    public void tick() {
    }

    @Override
    public boolean shouldRender() {
        return true;
    }

    @Override
    public String currentText(Player player, VarReplacer<Player> varReplacer) {
        return text;
    }

    @Override
    public List<Condition<Player>> getConditions() {
        return Collections.emptyList();
    }
}
