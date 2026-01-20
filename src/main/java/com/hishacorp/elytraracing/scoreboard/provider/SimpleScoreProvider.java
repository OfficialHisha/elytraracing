package com.hishacorp.elytraracing.scoreboard.provider;

import com.hishacorp.elytraracing.scoreboard.model.RaceScoreboard;
import com.hishacorp.elytraracing.scoreboard.model.StaticScoreboardLine;
import com.hishacorp.elytraracing.scoreboard.model.StaticScoreboardScore;
import com.r4g3baby.simplescore.api.Manager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SimpleScoreProvider implements ScoreboardProvider {

    private Manager api;

    public SimpleScoreProvider() {
        if (Bukkit.getPluginManager().isPluginEnabled("SimpleScore")) {
            RegisteredServiceProvider<Manager> rsp = Bukkit.getServicesManager().getRegistration(Manager.class);
            if (rsp != null) {
                this.api = rsp.getProvider();
            }
        }
    }

    @Override
    public void showScoreboard(Player player) {
        if (api == null) return;
        List<String> lines = Arrays.asList(
                "§f",
                "§f§lRace Time",
                "§e00:00.000",
                "§f",
                "§f§lPlayers",
                "§e1/1"
        );
        RaceScoreboard scoreboard = new RaceScoreboard(
                "elytraracing",
                Collections.singletonList(new StaticScoreboardLine("§e§lElytra Racing")),
                lines.stream().map(StaticScoreboardScore::new).collect(Collectors.toList())
        );
        api.addScoreboard(scoreboard);
        api.getViewer(player.getUniqueId()).setScoreboard(scoreboard, null);
    }

    @Override
    public void updateScoreboard(Player player) {
        // TODO: Implement scoreboard update
    }

    @Override
    public void removeScoreboard(Player player) {
        if (api == null) return;
        api.getViewer(player.getUniqueId()).removeScoreboard(null);
    }
}
