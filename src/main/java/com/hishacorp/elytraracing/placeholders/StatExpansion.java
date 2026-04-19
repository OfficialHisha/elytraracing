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
        if (parts.length < 3) {
            return null;
        }

        int position;
        try {
            position = Integer.parseInt(parts[parts.length - 1]);
        } catch (NumberFormatException e) {
            return null;
        }

        String raceName;
        boolean returnPlayer = false;
        String sortBy;

        // %elytraracing_<race>_<stat>_player_<pos>%
        if (parts.length >= 4 && parts[parts.length - 2].equalsIgnoreCase("player")) {
            returnPlayer = true;
            sortBy = parts[parts.length - 3].toLowerCase();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < parts.length - 3; i++) {
                if (i > 0) sb.append("_");
                sb.append(parts[i]);
            }
            raceName = sb.toString();
        } else {
            // %elytraracing_<race>_<stat>_<pos>%
            sortBy = parts[parts.length - 2].toLowerCase();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < parts.length - 2; i++) {
                if (i > 0) sb.append("_");
                sb.append(parts[i]);
            }
            raceName = sb.toString();
        }

        // Special case for old %elytraracing_<race>_player_<pos>% which should sort by time
        if (sortBy.equals("player")) {
            sortBy = "time";
            returnPlayer = true;
        }

        RaceStat stat = plugin.getDatabaseManager().getTopStatByRace(raceName, sortBy, position);

        if (stat == null) {
            return "N/A";
        }

        if (returnPlayer) {
            OfflinePlayer p = plugin.getServer().getOfflinePlayer(stat.getPlayerUUID());
            return p.getName() != null ? p.getName() : "Unknown";
        }

        switch (sortBy) {
            case "time":
                long time = stat.getBestTime();
                long minutes = TimeUnit.MILLISECONDS.toMinutes(time);
                long seconds = TimeUnit.MILLISECONDS.toSeconds(time) % 60;
                long millis = time % 1000;
                return String.format("%02d:%02d.%03d", minutes, seconds, millis);
            case "bestlap":
                long lapTime = stat.getBestLapTime();
                if (lapTime <= 0) return "N/A";
                long lapMinutes = TimeUnit.MILLISECONDS.toMinutes(lapTime);
                long lapSeconds = TimeUnit.MILLISECONDS.toSeconds(lapTime) % 60;
                long lapMillis = lapTime % 1000;
                return String.format("%02d:%02d.%03d", lapMinutes, lapSeconds, lapMillis);
            case "wins":
                return String.valueOf(stat.getWins());
            case "rounds_played":
                return String.valueOf(stat.getRoundsPlayed());
            case "finishes":
                return String.valueOf(stat.getFinishes());
        }

        return null;
    }
}
