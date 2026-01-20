package com.hishacorp.elytraracing.placeholders;

import com.hishacorp.elytraracing.Elytraracing;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

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
        return "Not implemented";
    }
}
