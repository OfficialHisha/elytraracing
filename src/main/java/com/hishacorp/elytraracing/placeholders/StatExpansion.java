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
        String[] keywords = {"time", "bestlap", "wins", "rounds", "finishes"};

        // Handle me stats: %elytraracing_<race>_<stat>_me%
        if (params.toLowerCase().endsWith("_me")) {
            if (player == null) return "N/A";
            String remaining = params.substring(0, params.length() - 3);

            if (remaining.toLowerCase().endsWith("_position")) {
                String sub = remaining.substring(0, remaining.length() - 9);
                String sortBy = "time";
                for (String keyword : keywords) {
                    if (sub.toLowerCase().endsWith("_" + keyword)) {
                        sortBy = keyword;
                        sub = sub.substring(0, sub.length() - keyword.length() - 1);
                        break;
                    }
                }
                int rank = plugin.getDatabaseManager().getPlayerRank(sub, player.getUniqueId(), sortBy);
                return rank == -1 ? "N/A" : String.valueOf(rank);
            }

            for (String keyword : keywords) {
                if (remaining.toLowerCase().endsWith("_" + keyword)) {
                    String raceName = remaining.substring(0, remaining.length() - keyword.length() - 1);
                    RaceStat stat = plugin.getDatabaseManager().getPlayerStat(raceName, player.getUniqueId());
                    if (stat == null) return "N/A";
                    return formatStat(stat, keyword);
                }
            }
        }

        int lastUnderscore = params.lastIndexOf('_');
        if (lastUnderscore == -1) return null;

        int position;
        try {
            position = Integer.parseInt(params.substring(lastUnderscore + 1));
        } catch (NumberFormatException e) {
            return null;
        }

        String remaining = params.substring(0, lastUnderscore);
        String sortBy = "";
        boolean returnPlayer = false;

        if (remaining.toLowerCase().endsWith("_player")) {
            returnPlayer = true;
            String temp = remaining.substring(0, remaining.length() - 7);
            boolean foundKeyword = false;
            for (String keyword : keywords) {
                if (temp.toLowerCase().endsWith("_" + keyword)) {
                    sortBy = keyword;
                    remaining = temp.substring(0, temp.length() - keyword.length() - 1);
                    foundKeyword = true;
                    break;
                }
            }
            if (!foundKeyword) {
                // Legacy support or just %elytraracing_<race>_player_<pos>%
                // In both cases, we sort by time and return player name
                sortBy = "time";
                remaining = temp;
            }
        } else {
            for (String keyword : keywords) {
                if (remaining.toLowerCase().endsWith("_" + keyword)) {
                    sortBy = keyword;
                    remaining = remaining.substring(0, remaining.length() - keyword.length() - 1);
                    break;
                }
            }
        }

        if (sortBy.isEmpty()) return null;

        String raceName = remaining;

        RaceStat stat = plugin.getDatabaseManager().getTopStatByRace(raceName, sortBy, position);

        if (stat == null) {
            return "N/A";
        }

        if (returnPlayer) {
            OfflinePlayer p = plugin.getServer().getOfflinePlayer(stat.getPlayerUUID());
            return p.getName() != null ? p.getName() : "Unknown";
        }

        return formatStat(stat, sortBy);
    }

    private String formatStat(RaceStat stat, String sortBy) {
        switch (sortBy.toLowerCase()) {
            case "time":
                long time = stat.getBestTime();
                if (time <= 0) return "N/A";
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
            case "rounds":
                return String.valueOf(stat.getRoundsPlayed());
            case "finishes":
                return String.valueOf(stat.getFinishes());
            default:
                return "N/A";
        }
    }
}
