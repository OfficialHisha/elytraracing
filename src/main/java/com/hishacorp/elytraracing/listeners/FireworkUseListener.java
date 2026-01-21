package com.hishacorp.elytraracing.listeners;

import com.hishacorp.elytraracing.Elytraracing;
import com.hishacorp.elytraracing.RaceManager;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;

public class FireworkUseListener implements Listener {

    private final Elytraracing plugin;

    public FireworkUseListener(Elytraracing plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (event.getEntityType() != EntityType.FIREWORK_ROCKET) {
            return;
        }

        Firework firework = (Firework) event.getEntity();
        if (!(firework.getShooter() instanceof Player player)) {
            return;
        }

        RaceManager raceManager = plugin.getRaceManager();
        if (raceManager.isPlayerInRace(player)) {
            raceManager.getRace(player).ifPresent(race -> {
                if (race.isInProgress()) {
                    race.startFireworkCooldown(player);
                }
            });
        }
    }
}
