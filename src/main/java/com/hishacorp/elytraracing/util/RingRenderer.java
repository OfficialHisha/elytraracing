package com.hishacorp.elytraracing.util;

import com.hishacorp.elytraracing.model.Ring;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.block.data.BlockData;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RingRenderer {

    // A map of players to the rings they are currently viewing.
    private final Map<UUID, Set<Ring>> playerVisibleRings = new ConcurrentHashMap<>();
    // A map of players to the ring they are currently configuring.
    private final Map<UUID, Ring> playerConfiguringRing = new ConcurrentHashMap<>();
    // A map of players to the locations of the blocks that have been changed for them.
    private final Map<UUID, Map<Location, BlockData>> playerOriginalBlocks = new ConcurrentHashMap<>();
    // A map of players to a map of block locations to the ring they belong to.
    private final Map<UUID, Map<Location, Ring>> playerRingBlocks = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> playerNextRing = new ConcurrentHashMap<>();
    private static final Material HIGHLIGHT_MATERIAL = Material.GOLD_BLOCK;

    public void addRingForPlayer(Player player, Ring ring) {
        playerVisibleRings.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>()).add(ring);
        playerRingBlocks.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>()).put(ring.getLocation(), ring);
        updatePlayerView(player);
    }

    public void removeRingForPlayer(Player player, Ring ring) {
        Set<Ring> rings = playerVisibleRings.get(player.getUniqueId());
        if (rings != null) {
            rings.remove(ring);
        }
        updatePlayerView(player);
    }

    public void setConfiguringRingForPlayer(Player player, Ring ring) {
        if (ring == null) {
            playerConfiguringRing.remove(player.getUniqueId());
        } else {
            playerConfiguringRing.put(player.getUniqueId(), ring);
        }
        updatePlayerView(player);
    }

    public void clearRingsForPlayer(Player player) {
        playerVisibleRings.remove(player.getUniqueId());
        playerConfiguringRing.remove(player.getUniqueId());
        playerRingBlocks.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>()).clear();
        playerNextRing.remove(player.getUniqueId());
        revertBlocksForPlayer(player);
    }

    public Ring getPlayerConfiguringRing(UUID uniqueId) {
        return playerConfiguringRing.get(uniqueId);
    }

    public Ring getRingAtLocation(Player player, Location location) {
        Map<Location, Ring> ringBlocks = playerRingBlocks.get(player.getUniqueId());
        if (ringBlocks != null) {
            return ringBlocks.get(location.toBlockLocation());
        }
        return null;
    }

    public Map<Location, Ring> getPlayerRingBlocks(UUID uniqueId) {
        return playerRingBlocks.get(uniqueId);
    }

    public void updatePlayerView(Player player) {
        // Revert any existing blocks first
        revertBlocksForPlayer(player);

        // Clear the mapping of blocks to rings for this player
        playerRingBlocks.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>()).clear();

        // Draw all the rings
        Set<Ring> rings = playerVisibleRings.get(player.getUniqueId());
        if (rings != null) {
            for (Ring ring : rings) {
                drawRing(player, ring, false);
            }
        }

        // Redraw the configuring ring with a different material
        Ring configuringRing = playerConfiguringRing.get(player.getUniqueId());
        if (configuringRing != null) {
            drawRing(player, configuringRing, true);
        }
    }

    public void showRaceRings(Player player, List<Ring> rings, int nextRingIndex) {
        playerVisibleRings.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>()).addAll(rings);
        playerNextRing.put(player.getUniqueId(), nextRingIndex);
        updatePlayerView(player);
    }

    public void showSpectatorRings(Player player, List<Ring> rings) {
        playerVisibleRings.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>()).addAll(rings);
        playerNextRing.remove(player.getUniqueId());
        updatePlayerView(player);
    }

    public void hideRaceRings(Player player, List<Ring> rings) {
        Set<Ring> visibleRings = playerVisibleRings.get(player.getUniqueId());
        if (visibleRings != null) {
            visibleRings.removeAll(rings);
        }
        playerNextRing.remove(player.getUniqueId());
        updatePlayerView(player);
    }

    public void updateRingHighlight(Player player, Ring oldRing, Ring newRing) {
        Integer nextRingIndex = playerNextRing.get(player.getUniqueId());
        if (nextRingIndex != null && newRing != null) {
            playerNextRing.put(player.getUniqueId(), newRing.getIndex());
        }
        updatePlayerView(player);
    }

    private void revertBlocksForPlayer(Player player) {
        Map<Location, BlockData> originalBlocks = playerOriginalBlocks.get(player.getUniqueId());
        if (originalBlocks != null) {
            for (Map.Entry<Location, BlockData> entry : originalBlocks.entrySet()) {
                player.sendBlockChange(entry.getKey(), entry.getValue());
            }
            originalBlocks.clear();
        }
    }

    public void revertPlayerView(Player player) {
        revertBlocksForPlayer(player);
    }

    public void setVisibleRings(Player player, Set<Ring> rings) {
        playerVisibleRings.put(player.getUniqueId(), rings);
    }

    private void drawRing(Player player, Ring ring, boolean isBeingConfigured) {
        Material material = ring.getMaterial();
        Integer nextRingIndex = playerNextRing.get(player.getUniqueId());
        if (nextRingIndex != null && ring.getIndex() == nextRingIndex) {
            material = HIGHLIGHT_MATERIAL;
        }

        BlockData blockData = material.createBlockData();
        Map<Location, BlockData> originalBlocks = playerOriginalBlocks.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());
        Map<Location, Ring> ringBlocks = playerRingBlocks.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());

        Location center = ring.getLocation();
        double radius = ring.getRadius();

        for (int i = 0; i < 360; i += 15) {
            double angle = Math.toRadians(i);
            double xOffset = 0;
            double yOffset = 0;
            double zOffset = 0;

            switch (ring.getOrientation()) {
                case HORIZONTAL:
                    xOffset = radius * Math.cos(angle);
                    zOffset = radius * Math.sin(angle);
                    break;
                case VERTICAL_X:
                    yOffset = radius * Math.cos(angle);
                    zOffset = radius * Math.sin(angle);
                    break;
                case VERTICAL_Z:
                    xOffset = radius * Math.cos(angle);
                    yOffset = radius * Math.sin(angle);
                    break;
            }

            Location blockLocation = center.clone().add(xOffset, yOffset, zOffset).toBlockLocation();

            // Store the original block if we haven't already, then send the change
            if (!originalBlocks.containsKey(blockLocation)) {
                originalBlocks.put(blockLocation, blockLocation.getBlock().getBlockData());
            }
            player.sendBlockChange(blockLocation, blockData);

            // Add to the ring blocks map
            ringBlocks.put(blockLocation, ring);

            if (isBeingConfigured) {
                player.spawnParticle(org.bukkit.Particle.END_ROD, blockLocation.clone().add(0.5, 0.5, 0.5), 1, 0, 0, 0, 0);
            }
        }
    }
}
