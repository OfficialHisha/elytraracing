package com.hishacorp.elytraracing.listeners;

import com.hishacorp.elytraracing.Elytraracing;
import com.hishacorp.elytraracing.RaceManager;
import com.hishacorp.elytraracing.ToolManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {

    private final Elytraracing plugin;

    public PlayerQuitListener(Elytraracing plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        handleDisconnect(event.getPlayer());
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        handleDisconnect(event.getPlayer());
    }

    private void handleDisconnect(Player player) {
        RaceManager raceManager = plugin.getRaceManager();
        ToolManager toolManager = plugin.getToolManager();

        if (raceManager.isPlayerInRace(player)) {
            raceManager.leaveRace(player);
        }
        if (toolManager.isPlayerUsingTool(player)) {
            toolManager.stopEditing(player);
        }
    }
}
