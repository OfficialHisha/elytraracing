package com.hishacorp.elytraracing.scoreboard;

import com.hishacorp.elytraracing.Elytraracing;
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

public class ScoreboardManager {

    private final Elytraracing plugin;
    private Manager api;

    public ScoreboardManager(Elytraracing plugin) {
        this.plugin = plugin;
        if (Bukkit.getPluginManager().isPluginEnabled("SimpleScore")) {
            RegisteredServiceProvider<Manager> rsp = Bukkit.getServicesManager().getRegistration(Manager.class);
            if (rsp != null) {
                this.api = rsp.getProvider();
            }
        }
    }

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

    public void updateScoreboard(Player player) {
        if (api == null) return;
        // TODO: Implement scoreboard update
    }

    public void removeScoreboard(Player player) {
        if (api == null) return;
        api.getViewer(player.getUniqueId()).removeScoreboard(null);
    }
}
