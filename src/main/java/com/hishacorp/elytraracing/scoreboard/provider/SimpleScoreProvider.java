package com.hishacorp.elytraracing.scoreboard.provider;

import com.hishacorp.elytraracing.Elytraracing;
import com.hishacorp.elytraracing.scoreboard.model.RaceScoreboard;
import com.hishacorp.elytraracing.scoreboard.model.StaticScoreboardLine;
import com.r4g3baby.simplescore.api.Manager;
import com.r4g3baby.simplescore.api.scoreboard.ScoreboardLine;
import com.r4g3baby.simplescore.api.scoreboard.data.Provider;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.Collections;
import java.util.List;

public class SimpleScoreProvider implements ScoreboardProvider {

    private Manager api;
    private Provider provider;
    private RaceScoreboard scoreboard;

    public SimpleScoreProvider(Elytraracing plugin) {
        if (Bukkit.getPluginManager().isPluginEnabled("SimpleScore")) {
            RegisteredServiceProvider<Manager> rsp = Bukkit.getServicesManager().getRegistration(Manager.class);
            if (rsp != null) {
                this.api = rsp.getProvider();
                this.provider = new Provider(plugin.getName(), "elytraracing");

                List<ScoreboardLine<Player>> titles = Collections.singletonList(new StaticScoreboardLine("§e§lElytra Racing"));

                this.scoreboard = new RaceScoreboard(
                        "elytraracing",
                        titles,
                        plugin
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
    }

    @Override
    public void removeScoreboard(Player player) {
        if (api == null) return;
        api.getViewer(player.getUniqueId()).removeScoreboard(provider);
    }
}
