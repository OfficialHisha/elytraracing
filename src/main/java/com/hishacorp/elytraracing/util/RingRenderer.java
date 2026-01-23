package com.hishacorp.elytraracing.util;

import com.hishacorp.elytraracing.model.Ring;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RingRenderer {

    private static final Material HIGHLIGHT_MATERIAL = Material.GOLD_BLOCK;
    private final Map<UUID, Ring> configuringRings = new HashMap<>();
    private final Map<UUID, Map<Location, Ring>> playerRingBlocks = new HashMap<>();

    public void showRings(Player player, List<Ring> rings, int nextRingIndex) {
        for (int i = 0; i < rings.size(); i++) {
            Ring ring = rings.get(i);
            Material material = (i == nextRingIndex) ? HIGHLIGHT_MATERIAL : ring.getMaterial();
            renderRing(player, ring, material);
        }
    }

    public void hideRings(Player player, List<Ring> rings) {
        for (Ring ring : rings) {
            unrenderRing(player, ring);
        }
    }

    public void updateRingHighlight(Player player, Ring oldRing, Ring newRing) {
        if (oldRing != null) {
            renderRing(player, oldRing, oldRing.getMaterial());
        }
        if (newRing != null) {
            renderRing(player, newRing, HIGHLIGHT_MATERIAL);
        }
    }

    private void renderRing(Player player, Ring ring, Material material) {
        for (Location loc : getRingLocations(ring)) {
            player.sendBlockChange(loc, material.createBlockData());
        }
    }

    private void unrenderRing(Player player, Ring ring) {
        for (Location loc : getRingLocations(ring)) {
            player.sendBlockChange(loc, loc.getBlock().getBlockData());
        }
    }

    private List<Location> getRingLocations(Ring ring) {
        List<Location> locations = new ArrayList<>();
        Location center = ring.getLocation();
        double radius = ring.getRadius();
        int segments = (int) (radius * 4);

        for (int i = 0; i < segments; i++) {
            double theta = 2 * Math.PI * i / segments;
            double dx = radius * Math.cos(theta);
            double dy = radius * Math.sin(theta);

            Location loc = center.clone();
            switch (ring.getOrientation()) {
                case HORIZONTAL:
                    loc.add(dx, 0, dy);
                    break;
                case VERTICAL_X:
                    loc.add(0, dy, dx);
                    break;

                case VERTICAL_Z:
                    loc.add(dx, dy, 0);
                    break;
            }
            locations.add(loc.getBlock().getLocation());
        }
        return locations;
    }

    public void addRingForPlayer(Player player, Ring ring) {
        renderRing(player, ring, ring.getMaterial());
        playerRingBlocks.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>()).put(ring.getLocation(), ring);
    }

    public void removeRingForPlayer(Player player, Ring ring) {
        unrenderRing(player, ring);
        if (playerRingBlocks.containsKey(player.getUniqueId())) {
            playerRingBlocks.get(player.getUniqueId()).remove(ring.getLocation());
        }
    }

    public void setConfiguringRingForPlayer(Player player, Ring ring) {
        if (ring == null) {
            configuringRings.remove(player.getUniqueId());
        } else {
            configuringRings.put(player.getUniqueId(), ring);
        }
        updatePlayerView(player);
    }

    public Ring getPlayerConfiguringRing(UUID uniqueId) {
        return configuringRings.get(uniqueId);
    }

    public void updatePlayerView(Player player) {
        Ring configuringRing = getPlayerConfiguringRing(player.getUniqueId());
        if (configuringRing != null) {
            renderRing(player, configuringRing, Material.YELLOW_STAINED_GLASS);
        }
    }

    public Map<Location, Ring> getPlayerRingBlocks(UUID uniqueId) {
        return playerRingBlocks.get(uniqueId);
    }

    public void clearRingsForPlayer(Player player) {
        playerRingBlocks.remove(player.getUniqueId());
    }
}
