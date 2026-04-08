package com.hishacorp.elytraracing.listeners;

import com.hishacorp.elytraracing.Elytraracing;
import com.hishacorp.elytraracing.Race;
import com.hishacorp.elytraracing.model.Racer;
import com.hishacorp.elytraracing.model.Ring;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveListener implements Listener {

    private final Elytraracing plugin;

    public PlayerMoveListener(Elytraracing plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        plugin.getRaceManager().getRace(player).ifPresent(race -> {
            if (race.isInProgress()) {
                Racer racer = race.getRacers().get(player.getUniqueId());
                if (racer != null && !racer.isCompleted()) {
                    // Border check
                    if (!race.isInsideBorder(player.getLocation())) {
                        Location teleportLocation;
                        if (racer.getCurrentRingIndex() > 0) {
                            teleportLocation = race.getRings().get(racer.getCurrentRingIndex() - 1).getLocation();
                        } else if (racer.getCurrentLap() > 1) {
                            teleportLocation = race.getRings().get(race.getRings().size() - 1).getLocation();
                        } else {
                            teleportLocation = race.getSpawnLocation() != null ? race.getSpawnLocation() : racer.getJoinLocation();
                        }
                        event.setTo(teleportLocation);
                        player.sendMessage("§cYou have went out of bounds and were teleported back!");
                        return;
                    }

                    int nextRingIndex = racer.getCurrentRingIndex();
                    if (nextRingIndex < race.getRequiredRings().size()) {
                        Ring nextRing = race.getRequiredRings().get(nextRingIndex);
                        if (player.getLocation().distanceSquared(nextRing.getLocation()) <= nextRing.getRadius() * nextRing.getRadius()) {
                            race.playerPassedRing(player);
                        }
                    }

                    for (Ring specialRing : race.getSpecialRings()) {
                        if (player.getLocation().distanceSquared(specialRing.getLocation()) <= specialRing.getRadius() * specialRing.getRadius()) {
                            race.playerPassedSpecialRing(player, specialRing);
                        }
                    }
                }
            }
        });
    }
}
