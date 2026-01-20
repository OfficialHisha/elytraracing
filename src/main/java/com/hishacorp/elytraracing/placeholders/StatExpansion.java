package com.hishacorp.elytraracing.placeholders;

import com.hishacorp.elytraracing.Elytraracing;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

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
        // %elytraracing_WORLDNAME_time_1%
        // %elytraracing_WORLDNAME_player_1%
        String[] parts = params.split("_");
        if (parts.length != 3) {
            return null;
        }

        String worldName = parts[0];
        String type = parts[1];
        int position;

        try {
            position = Integer.parseInt(parts[2]);
        } catch (NumberFormatException e) {
            return null;
        }

        if (position <= 0) {
            return null;
        }

        var stat = plugin.getDatabaseManager().getTopTimeByWorld(worldName, position);

        if (stat == null) {
            return "";
        }

        if (type.equalsIgnoreCase("time")) {
            return String.valueOf(stat.bestTime());
        }

        if (type.equalsIgnoreCase("player")) {
            return Bukkit.getOfflinePlayer(UUID.fromString(stat.playerUUID())).getName();
        }

        return null;
    }
}
