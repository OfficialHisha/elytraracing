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

    public void addRingForPlayer(Player player, Ring ring) {
        playerVisibleRings.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>()).add(ring);
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
        revertBlocksForPlayer(player);
    }

    public Ring getPlayerConfiguringRing(UUID uniqueId) {
        return playerConfiguringRing.get(uniqueId);
    }

    public void updatePlayerView(Player player) {
        // Revert any existing blocks first
        revertBlocksForPlayer(player);

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

    private void revertBlocksForPlayer(Player player) {
        Map<Location, BlockData> originalBlocks = playerOriginalBlocks.get(player.getUniqueId());
        if (originalBlocks != null) {
            for (Map.Entry<Location, BlockData> entry : originalBlocks.entrySet()) {
                player.sendBlockChange(entry.getKey(), entry.getValue());
            }
            originalBlocks.clear();
        }
    }

    private void drawRing(Player player, Ring ring, boolean isBeingConfigured) {
        Material material = isBeingConfigured ? Material.LIME_STAINED_GLASS : ring.getMaterial();
        BlockData blockData = material.createBlockData();
        Map<Location, BlockData> originalBlocks = playerOriginalBlocks.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());

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
        }
    }
}
