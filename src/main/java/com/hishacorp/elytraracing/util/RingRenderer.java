package com.hishacorp.elytraracing.util;

import com.hishacorp.elytraracing.model.Border;
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
    private final Map<UUID, List<Border>> playerVisibleBorders = new ConcurrentHashMap<>();
    private final Map<UUID, Border> playerSelection = new ConcurrentHashMap<>();
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
        playerVisibleBorders.remove(player.getUniqueId());
        playerSelection.remove(player.getUniqueId());
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

        // Draw saved borders
        List<Border> borders = playerVisibleBorders.get(player.getUniqueId());
        if (borders != null) {
            for (Border border : borders) {
                drawBorder(player, border, Material.LIME_STAINED_GLASS);
            }
        }

        // Draw selection
        Border selection = playerSelection.get(player.getUniqueId());
        if (selection != null) {
            drawBorder(player, selection, Material.ORANGE_STAINED_GLASS);
        }
    }

    private void drawBorder(Player player, Border border, Material material) {
        Location pos1 = border.getPos1();
        Location pos2 = border.getPos2();

        if (pos1 == null || pos2 == null) return;
        if (pos1.getWorld() == null || !pos1.getWorld().equals(player.getWorld())) return;

        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

        org.bukkit.World world = pos1.getWorld();
        BlockData blockData = material.createBlockData();
        Map<Location, BlockData> originalBlocks = playerOriginalBlocks.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());

        List<Border> otherBorders = new ArrayList<>();
        List<Border> savedBorders = playerVisibleBorders.get(player.getUniqueId());
        if (savedBorders != null) otherBorders.addAll(savedBorders);
        Border selection = playerSelection.get(player.getUniqueId());
        if (selection != null) otherBorders.add(selection);

        otherBorders.removeIf(b -> b == border || (b.getId() != 0 && b.getId() == border.getId()));

        // Draw edges along X
        for (int x = minX; x <= maxX; x++) {
            setGhostBlockIfOutside(player, world, x, minY, minZ, blockData, originalBlocks, otherBorders);
            setGhostBlockIfOutside(player, world, x, maxY, minZ, blockData, originalBlocks, otherBorders);
            setGhostBlockIfOutside(player, world, x, minY, maxZ, blockData, originalBlocks, otherBorders);
            setGhostBlockIfOutside(player, world, x, maxY, maxZ, blockData, originalBlocks, otherBorders);
        }

        // Draw edges along Y
        for (int y = minY; y <= maxY; y++) {
            setGhostBlockIfOutside(player, world, minX, y, minZ, blockData, originalBlocks, otherBorders);
            setGhostBlockIfOutside(player, world, maxX, y, minZ, blockData, originalBlocks, otherBorders);
            setGhostBlockIfOutside(player, world, minX, y, maxZ, blockData, originalBlocks, otherBorders);
            setGhostBlockIfOutside(player, world, maxX, y, maxZ, blockData, originalBlocks, otherBorders);
        }

        // Draw edges along Z
        for (int z = minZ; z <= maxZ; z++) {
            setGhostBlockIfOutside(player, world, minX, minY, z, blockData, originalBlocks, otherBorders);
            setGhostBlockIfOutside(player, world, minX, maxY, z, blockData, originalBlocks, otherBorders);
            setGhostBlockIfOutside(player, world, maxX, minY, z, blockData, originalBlocks, otherBorders);
            setGhostBlockIfOutside(player, world, maxX, maxY, z, blockData, originalBlocks, otherBorders);
        }
    }

    private void setGhostBlockIfOutside(Player player, org.bukkit.World world, int x, int y, int z, BlockData data, Map<Location, BlockData> originalBlocks, List<Border> others) {
        Location loc = new Location(world, x, y, z);
        for (Border other : others) {
            if (other.isInside(loc)) return;
        }

        Location blockLoc = loc.toBlockLocation();
        if (!originalBlocks.containsKey(blockLoc)) {
            originalBlocks.put(blockLoc, blockLoc.getBlock().getBlockData());
        }
        player.sendBlockChange(blockLoc, data);
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

    public void setVisibleBorders(Player player, List<Border> borders) {
        playerVisibleBorders.put(player.getUniqueId(), borders);
    }

    public void setSelection(Player player, Location pos1, Location pos2) {
        if (pos1 == null || pos2 == null) {
            playerSelection.remove(player.getUniqueId());
        } else {
            playerSelection.put(player.getUniqueId(), new Border(0, pos1, pos2));
        }
        updatePlayerView(player);
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
