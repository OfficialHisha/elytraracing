package com.hishacorp.elytraracing.listeners;

import com.hishacorp.elytraracing.Elytraracing;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class PlayerDamageListener implements Listener {

    private final Elytraracing plugin;

    public PlayerDamageListener(Elytraracing plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (plugin.getRaceManager().isPlayerInRace(player)) {
                event.setCancelled(true);
            }
        }
    }
}
