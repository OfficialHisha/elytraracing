package com.hishacorp.elytraracing.placeholders;

import com.hishacorp.elytraracing.Elytraracing;
import com.hishacorp.elytraracing.persistance.data.RaceStat;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class StatExpansion extends PlaceholderExpansion {

    private final Elytraracing plugin;

    public StatExpansion(Elytraracing plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "elytraracing";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Hisha";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        String[] parts = params.split("_");
        if (parts.length != 3) {
            return null;
        }

        String world = parts[0];
        String type = parts[1];
        int position = Integer.parseInt(parts[2]);

        RaceStat stat = plugin.getDatabaseManager().getTopTimeByWorld(world, position);

        if (stat == null) {
            return "N/A";
        }

        if (type.equalsIgnoreCase("time")) {
            long time = stat.getBestTime();
            long minutes = TimeUnit.MILLISECONDS.toMinutes(time);
            long seconds = TimeUnit.MILLISECONDS.toSeconds(time) % 60;
            long millis = time % 1000;
            return String.format("%02d:%02d.%03d", minutes, seconds, millis);
        } else if (type.equalsIgnoreCase("player")) {
            return plugin.getServer().getOfflinePlayer(stat.getPlayerUUID()).getName();
        }

        return null;
    }
}
