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

import java.util.Optional;

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
                        } else {
                            teleportLocation = racer.getJoinLocation();
                        }
                        event.setTo(teleportLocation);
                        player.sendMessage("§cYou have went out of bounds and were teleported back!");
                        return;
                    }

                    int nextRingIndex = racer.getCurrentRingIndex();
                    if (nextRingIndex < race.getRings().size()) {
                        Ring nextRing = race.getRings().get(nextRingIndex);
                        if (player.getLocation().distanceSquared(nextRing.getLocation()) <= nextRing.getRadius() * nextRing.getRadius()) {
                            race.playerPassedRing(player);
                        }
                    }
                }
            }
        });
    }
}
