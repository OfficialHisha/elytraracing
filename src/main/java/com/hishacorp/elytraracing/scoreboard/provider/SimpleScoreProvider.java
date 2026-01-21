package com.hishacorp.elytraracing.scoreboard.provider;

import com.hishacorp.elytraracing.Elytraracing;
import com.hishacorp.elytraracing.scoreboard.model.RaceScoreboard;
import com.hishacorp.elytraracing.scoreboard.model.StaticScoreboardLine;
import com.hishacorp.elytraracing.scoreboard.model.StaticScoreboardScore;
import com.r4g3baby.simplescore.api.Manager;
import com.r4g3baby.simplescore.api.scoreboard.ScoreboardLine;
import com.r4g3baby.simplescore.api.scoreboard.ScoreboardScore;
import com.r4g3baby.simplescore.api.scoreboard.data.Provider;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SimpleScoreProvider implements ScoreboardProvider {

    private final Elytraracing plugin;
    private Manager api;
    private Provider provider;
    private RaceScoreboard scoreboard;

    public SimpleScoreProvider(Elytraracing plugin) {
        this.plugin = plugin;
        if (Bukkit.getPluginManager().isPluginEnabled("SimpleScore")) {
            RegisteredServiceProvider<Manager> rsp = Bukkit.getServicesManager().getRegistration(Manager.class);
            if (rsp != null) {
                this.api = rsp.getProvider();
                this.provider = new Provider(plugin.getName(), "elytraracing");

                List<String> lines = Arrays.asList(
                        "§f",
                        "§f§lRace Time",
                        "§e00:00",
                        "§f",
                        "§f§lPlayers",
                        "§e1/1",
                        "§f",
                        "§f§lLineup",
                        "§e1. §aPlayer1 §7- §f00:00.000",
                        "§e2. §aPlayer2 §7- §f00:00.000",
                        "§e3. §aPlayer3 §7- §f00:00.000",
                        "§e4. §aPlayer4 §7- §f00:00.000",
                        "§e5. §aPlayer5 §7- §f00:00.000"
                );
                List<ScoreboardLine<Player>> titles = Collections.singletonList(new StaticScoreboardLine("§e§lElytra Racing"));
                final int[] i = {lines.size()};
                List<ScoreboardScore<Player>> scores = lines.stream()
                        .map(line -> new StaticScoreboardScore(line, i[0]--))
                        .collect(Collectors.toList());

                this.scoreboard = new RaceScoreboard(
                        "elytraracing",
                        titles,
                        scores
                );
                api.addScoreboard(scoreboard);
            }
        }
    }

    @Override
    public void showScoreboard(Player player) {
        if (api == null) return;
        api.getViewer(player.getUniqueId()).setScoreboard(scoreboard, provider);
    }

    @Override
    public void updateScoreboard(Player player) {
        // TODO: Implement scoreboard update
    }

    @Override
    public void removeScoreboard(Player player) {
        if (api == null) return;
        api.getViewer(player.getUniqueId()).removeScoreboard(provider);
    }
}
